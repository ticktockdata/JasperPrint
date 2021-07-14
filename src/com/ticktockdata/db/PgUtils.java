/*
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.ticktockdata.db;

import com.ticktockdata.jasperserver.CommandLineProcessor;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import javax.swing.*;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import org.apache.log4j.Logger;

/**
 * This class is a special "add-on" to JasperPrint.  It is for the purpose
 * of doing database backup and restore of PostgreSQL.
 * @author JAM {javajoe@programmer.net}
 * @since Jul 12, 2021
 */
public class PgUtils {
    
    private static final Logger logger;
    private static JFrame busyDialog = null;
    private static JProgressBar progress = null;
    
    
    static {
        logger = Logger.getLogger(PgUtils.class);
    }
    
    private PgUtils() {
        super();
    }
    
    
    
    /**
     * This didn't turn out so great :(  Does not work for some reason or the 
     * other, think connection gets closed before it's done.
     * @param host
     * @param dbName
     * @param file
     */
    public static void backupPgDatabase(String host, String dbName, String file) {
        
        File f = new File(file);
        if (f.exists()) {
            String msg = "File already exists, do you want to replace it?\n" + file;
            if (JOptionPane.showConfirmDialog(null, msg, "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.NO_OPTION) {
                //return "Backup canceled by user (file exists)";
                return;
            }
        }
        
        String connStr = getConnectionURL(host, dbName);
        String dumpCommand = "pg_dump --format custom --disable-triggers --no-password -v -F c --file \"" + file + "\" \"" + connStr + "\"";
        
        showBusyDialog("Creating Backup");
        String msg = executeCommand(dumpCommand, true, new File("/opt/PostgreSQL/9.3/bin"));
        
        if (msg == null || msg.trim().isEmpty()) {
            if (f.exists()) {
                if (f.length() <= 50) {
                    CommandLineProcessor.showWarn("Error: Backup file exists but does not contain data", false);
                }
            } else {
                CommandLineProcessor.showError("Failed to create Backup file!", null, false);
            }
            CommandLineProcessor.showInfo("Backup was successful!", false);
        } else {
            CommandLineProcessor.showWarn(msg, false);
        }
        
    }
    
    
    public static void restorePgDatabase(String host, String dbName, String file) {
        
        File f = new File(file);
        if (!f.exists()) {
            CommandLineProcessor.showWarn("Backup file specified does not exist!\n" + file, false);
            return;
        }
        
        showBusyDialog("Restoring Data");
        java.sql.Connection conn = null;
        java.sql.Statement stmt = null;
        
        try {
            
            Properties props = new Properties();
            props.put("user", "postgres");
            props.put("password", "true");
            
            conn = java.sql.DriverManager.getConnection("jdbc:postgresql://" + host + ":5432/" + dbName, props);
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            
            ResultSet rs = stmt.executeQuery("SELECT schema_name FROM information_schema.schemata WHERE schema_name NOT LIKE 'pg_%' AND schema_name <> 'information_schema'");
            List<String> schemas = new ArrayList<>();
            
            while (rs.next()) {
                schemas.add(rs.getString(1));
            }
            
            rs.close();
            
            for (String s : schemas) {
                stmt.executeUpdate("DROP SCHEMA " + s + " CASCADE;");
            }
            
            // public schema must be recreated for restore to work
            stmt.executeUpdate("CREATE SCHEMA public AUTHORIZATION postgres;");
            stmt.executeUpdate("GRANT ALL ON SCHEMA public TO postgres;");
            stmt.executeUpdate("COMMENT ON SCHEMA public IS 'standard public schema';");
            
        } catch (Exception ex) {
            closeBusyDialog();
            CommandLineProcessor.showError("Error dropping old data!\n" + ex, ex, false);
            return;
        } finally {
            try {
                stmt.close();
                conn.close();
            } catch (Exception ex) {
                CommandLineProcessor.showError(ex.getLocalizedMessage(), ex, true);
            }
        }
        
        String connStr = getConnectionURL(host, dbName);
        String restore = "pg_restore --disable-triggers --no-password -v --dbname \"" + connStr + "\" \"" + file + "\"";
        
        
        String msg = executeCommand(restore, true, new File("/opt/PostgreSQL/9.3/bin"));
        
        if (msg.isEmpty()) {
            CommandLineProcessor.showInfo("Appears the Backup was successful!", false);
        } else {
            CommandLineProcessor.showError("Error doing database restore!\n" + msg, null, false);
        }
        
    }
    
    
    private static String getConnectionURL(String host, String dbName) {
        
        String connStr = "postgresql://" + host + ":5432/" + dbName + "?user=postgres&password=true";
        
        return connStr;
        
    }
    
    
    /**
     * This routine copied from Classic Accounting
     * @param command
     * @param waitFor
     * @param executeDir
     * @param variables
     * @return empty String if success, error message otherwise
     */
    private static String executeCommand(final String command, boolean waitFor, final File executeDir, final String... variables) {

        SwingWorker worker = new SwingWorker() {
            @Override
            protected String doInBackground() throws Exception {
                //define some variables

                //we have to add in current environment variables otherwise command may not execute properly
                List<String> variableList = getCurrentEnviromentVariables();
                for (String var : variables) {
                    variableList.add(var);
                }
                String[] envVariables = variableList.toArray(new String[variableList.size()]);

                String OS_NAME = System.getProperty("os.name").toLowerCase();

                try {
                    
                    final StringBuilder errorString = new StringBuilder();
                    final Process p;
                    if (OS_NAME.contains("windows")) {
                        String cm = "cmd.exe /C " + command;
                        p = Runtime.getRuntime().exec(cm, envVariables, executeDir);
                    } else if (OS_NAME.contains("linux")) {
                        String[] cm = {"sh", "-c", "./" + command};
                        p = Runtime.getRuntime().exec(cm, envVariables, executeDir);
                    } else if (OS_NAME.contains("mac")) {
                        String[] cm = {"sh", "-c", "./" + command};
                        p = Runtime.getRuntime().exec(cm, envVariables, executeDir);
                    } else {
                        closeBusyDialog();
                        return "Your operating system is not supported for this operation.";
                    }

                    /* 
                    This revised to handle and close all streams of the process,
                    Input, Error and Output.  Previously it was only handling
                    the Error stream and somehow this caused it to hang on
                    Windows when doing a restore.  Appeared to depend on the
                    version of the backup & database.  JAM - 2021-04-22, v2021.1.12
                     */
                    final InputStreamReader errStreamReader = new InputStreamReader(p.getErrorStream());
                    final OutputStreamWriter writer = new OutputStreamWriter(p.getOutputStream());
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                while (p.isAlive()) {
                                    String s = "";
                                    while (errStreamReader.ready()) {
                                        s += (char) errStreamReader.read();
                                    }
                                    if (!s.trim().isEmpty()) {
                                        logger.warn(s);
                                        errorString.append(s);
                                    }
                                }
                            } catch (Exception e) {
                                logger.error("Error in process ErrStream", e);
                                errorString.append(e.toString());
                            } finally {
                                try {
                                    errStreamReader.close();
                                } catch (Exception ex) {
                                }
                                try {
                                    writer.close();
                                } catch (Exception ex) {
                                }
                            }
                        }
                    }).start();

                    final InputStreamReader inStreamReader = new InputStreamReader(p.getInputStream());
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                while (p.isAlive()) {
                                    String s = "";
                                    while (inStreamReader.ready()) {
                                        s += (char) inStreamReader.read();
                                    }
                                    if (!s.trim().isEmpty()) {
                                        logger.debug(s);
                                        errorString.append(s);
                                    }
                                }
                            } catch (Exception e) {
                                logger.error("Error in process input stream", e);
                                errorString.append(e.toString());
                            } finally {
                                try {
                                    inStreamReader.close();
                                } catch (Exception ex) {
                                }
                            }
                        }
                    }).start();

                    int retVal = p.waitFor();
                    if (retVal == 0) {
                        // not an error, so return success
                        return "";
                    } else {
                        // pause a bit to try to collect the error messages
                        Thread.sleep(1500);
                        errorString.append("\nProcess return value is: ");
                        errorString.append(retVal);
                    }
                    
                    return errorString.toString().trim();
                    
                } catch (Exception e) {
                    return "An error has occurred: " + e.getMessage();
                } finally {
                    closeBusyDialog();
                }
            }
        };
        
        worker.execute();
        
        try {

            if(waitFor){
                
                while(!worker.isDone()){
                    Thread.sleep(1000);
                }

                return String.valueOf(worker.get());

            }else{
                if(worker.isDone()){
                    return String.valueOf(worker.get());
                }
            }
           
        } catch (InterruptedException ex) {
            logger.error("Error waiting for thread", ex);
        } catch (ExecutionException ex) {
            logger.error("Error executing command", ex);
        }
        return "";
    }

    private static List getCurrentEnviromentVariables(){
        Map<String,String> env = System.getenv();

        List<String> variables = new ArrayList<String>();
        for(String key : env.keySet()){
            String var = key + "=" + env.get(key);
            variables.add(var);
        }

        return variables;
    }
    
    
    
    
    private static void showBusyDialog(String text) {

        if (busyDialog == null) {

            busyDialog = new JFrame("Please Wait");
            busyDialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            GraphicsDevice screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().getDevice();
            Dimension screenSize = screen.getDefaultConfiguration().getBounds().getSize();

            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout(10, 10));
            busyDialog.add(panel);

            progress = new JProgressBar();
            progress.setPreferredSize(new Dimension(80, 20));
            progress.setStringPainted(true);
            progress.setString(text);
            progress.setIndeterminate(true);
            progress.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
            panel.add(new JLabel(" "), BorderLayout.NORTH);
            panel.add(new JLabel(" "), BorderLayout.SOUTH);
            panel.add(progress, BorderLayout.CENTER);

            int height = 110;
            int width = 250;

            java.awt.Dimension dimension = new java.awt.Dimension(width, height);
            busyDialog.setPreferredSize(dimension);

            // alter location upward to compensate for documents bar at bottom of screen
            busyDialog.setBounds(new java.awt.Rectangle(new java.awt.Point((screenSize.width - width) / 2, ((screenSize.height - height) / 2) - 20), dimension));
        } else {
            
            progress.setString(text);
            // should already be visible!
            if (busyDialog.isVisible()) {
                return;
            }
        }
        
        SwingUtilities.invokeLater(() -> {
            busyDialog.setVisible(true);
        });

    }
    
    
    
    private static void closeBusyDialog() {
        if (busyDialog != null) {
            SwingUtilities.invokeLater(() -> {
                busyDialog.setVisible(false);
                busyDialog.dispose();
                busyDialog = null;
                progress = null;
            });
        }
    }
    
}
