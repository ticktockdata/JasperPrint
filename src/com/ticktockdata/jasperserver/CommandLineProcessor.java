/*
 * Copyright (C) 2018 Joseph A Miller
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
package com.ticktockdata.jasperserver;

import com.ticktockdata.db.PgUtils;
import com.ticktockdata.jasper.ConnectionInfo;
import com.ticktockdata.jasper.ReportConnectionManager;
import com.ticktockdata.jasper.JasperPrintMain;
import static com.ticktockdata.jasper.JasperPrintMain.LOGGER;
import static com.ticktockdata.jasper.JasperPrintMain.addToClassPath;
import com.ticktockdata.jasperserver.PrintServer.Command;
import java.io.File;
import java.net.InetAddress;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * This is the entry point for all incoming commands to the jar file - it
 processes the command line arguments and forwards them to a running 
 instance of the Jasper Print PrintServer, or starts a new server, whatever the
 case may be.
 * <p>Each instance of this class runs on a new thread for
 * 
 *
 * @author JAM {javajoe@programmer.net}
 * @since Aug 23, 2018
 */
public class CommandLineProcessor {

    public CommandLineProcessor(String[] args) {
        
        if (args == null || args.length < 1) {
            processHelp(new String[] {"HELP"});
            return;
        }
        if (getVerboseFromArgs(args)) {
            System.out.println("\nNew Job Received for CommandLineProcessor\n");
            for (String s : args) {
                System.out.println("Received arg: " + s);
            }
            System.out.println(">> End of args received\n");
        }
        
        String a1 = args[0].toUpperCase().trim();
        if (a1.isEmpty()) {
            processHelp(new String[] {"HELP"});
            return;
        }
        
        PrintServer.Command cmd = null;
        try {
            cmd = PrintServer.Command.valueOf(a1);
            if (cmd == null) {
                throw new InvalidParameterException("Cannot convert '" + a1 + "' to a vaid PrintAction.Command");
            }
        } catch (Exception ex) {
            CommandLineProcessor.showError("ERROR: " + ex.toString(), ex, false);
            processHelp(new String[] {"HELP"});
            return;
        }

        
        switch  (cmd) {
            case START:
                processStart(args);
                break;
            case STOP:
                processStop(args);
                break;
            case ADD:
                processAdd(args);
                break;
            case CLOSE:
                processClose(args);
                break;
            case PRINT:
                System.out.println("got a print job, sending to processPrint");
                processPrint(args);
                break;
            case BACKUP:
                processBackup(args);
                break;
            case RESTORE:
                processRestore(args);
                break;
            case CLEAR:
                processClear(args);
                break;
            case HELP:
                processHelp(args);
                break;
            case STATUS:
                processStatus(args);
                break;
            case CONNECTIONS:
                processConnections(args);
                break;
            default:
                processHelp(new String[] {"HELP"});
        }

    }

    
    /**
     * Processes the START Command.  This starts the server process which
     * will only stop when STOP is sent.
     * @param args 
     */
    private void processStart(String[] args) {
        
        boolean silent = getSilentFromArgs(args);
        
        if (args.length < 1) {
            showError("Insufficient arguments to start a server!", null, silent);
            return;
        }
        
        int port = getPortFromArgs(args);
        String host = getHostFromArgs(args);
        
//        if (ServerManager.isPrintServerRunning(host, port)) {
//            showError("A print server is already running on " + host + ":" + port + ", use ADD to add a new connection.", null, silent);
//            return;
//        }
        
        ConnectionInfo connInfo = getConnectionInfoFromArgs(args, silent);
        
//        if (connInfo == null || !connInfo.isValidInfo(false)) {
//            LOGGER.info("Not able to create a connection, starting server w/o connection!");
////            return;  // do NOT return, start an empty server (w/o connection)
//        }   // if not creatable then exit
        
        String status = ServerManager.startPrintServer(port, connInfo, silent);
        
        // added 2021-07-07, JAM
        String fontDir = getArgumentValue(args, "-font");
        if (fontDir != null && !fontDir.isEmpty()) {
            loadFontExtensions(fontDir);
        }
        
        showStatus(status, silent);
        
    }
    
    
    private void loadFontExtensions(String fontDir) {
            // try to load font extension for JasperReports (added v2021.1, JAM)
        try {
            File d = new File(fontDir);
            if (!d.exists()) {
                LOGGER.info("Fonts directory does not exist!");
            } else {
                for (File f : d.listFiles()) {
                    if (!f.isDirectory() && f.getName().toLowerCase().endsWith(".jar")) {
                        addToClassPath(f.getAbsolutePath());
                        LOGGER.info("Added font extension to classpath: " + f.getName());
                    }
                }
            }
        } catch (Exception ex) {
            LOGGER.error("Error finding fonts directory", ex);
        }
}
    
    /** 
     * this routine is used by both START and ADD routines
     * @return null if no valid ConnectionInfo could be created
     */
    public static ConnectionInfo getConnectionInfoFromArgs(String[] args, boolean silent) {
        
        
        //boolean silent = getSilentFromArgs(args);
        
        String identifier = getArgumentValue(args, "-id", "-n");
        if (identifier == null) identifier = ReportConnectionManager.DEFAULT_CONNECTION_NAME;
        ConnectionInfo info = new ConnectionInfo(identifier);
        
        
        // required
        String driverClass = getArgumentValue(args, "-d");
        String url = getArgumentValue(args, "-ur");
        // if the required params are N/A then return null
        if (driverClass == null || url == null) {
            showWarn("Cannot create Connection without Driver Class and URL", silent);
            return null;
        }
        info.setDriverClass(driverClass);
        info.setUrl(url);
        
        // get list of classpath elements then convert to File objects
        List<String> cpElements = getArgumentValueList(args, "-cl");
        // add driver jars - needed
        info.setDriverJars(cpElements.toArray(new String[cpElements.size()]));
        
        
        // optional
        info.setUser(getArgumentValue(args, "-us"));
        info.setPassword(getArgumentValue(args, "-pa"));
        
        try {
            if (info.isValidInfo(!silent)) {
                return info;
            }
        } catch (Exception ex) {
            showError("Failed to create a valid ConnectionInfo", ex, silent);
        }
        
        return null;
        
    }
    
    
    /**
     * Add is allowed to run on remote host.
     * @param args 
     */
    private void processAdd(String[] args) {
        
        boolean silent = getSilentFromArgs(args);
        
        if (args.length < 1) {
            showError("Insufficient arguments to start a server!", null, silent);
            return;
        }
        
        int port = getPortFromArgs(args);
        String host = getHostFromArgs(args);
        String id = getArgumentValue(args, "-id", "-n");
        if (id == null || id.isEmpty()) {
            id = ServerManager.DEFAULT_IDENTIFIER;
        }
        
        
        // see if this connection already exists or not
        if (ServerManager.isPrintServerRunning(host, port)) {
            if (ServerManager.isPrintServerRunning(ServerManager.LOCALHOST, port, id)) {
                if (!silent) {  // only ask if not silent, 
                    if (JOptionPane.showConfirmDialog(null, 
                            "<html>There is already an existing connection named "
                            + "<br><b>" + id + "</b> open on this Server port."
                            + "<p>Do you want to replace the existing connection?", 
                            "Confirm:", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
                        return;
                    }
                }   // if silent, then just replace w/o asking.
            } else {
                // add, no warn
            }
            
            try {
                Client c = new Client(ServerManager.LOCALHOST, port);
                c.println("ADD");
                // just pass ALL args along
                for (String a : args) {
                    c.println(a);
                }
                c.print(";");
                //c.writeObject(connInfo);
                
                readStatusMessage(c, silent);
                
            } catch (Exception ex) {
                showError("Was unable to write the ConnectionInfo object to print server!", ex, silent);
            }
            
        } else {
            showError("There is no Print Server running on port " + port, null, silent);
        }
        
    }
    
    
    
    private void processStatus(String[] args) {
        
        int port = getPortFromArgs(args);
        String host = getHostFromArgs(args);
        boolean silent = getSilentFromArgs(args);
        
        try {
            Client c = new Client(host, port);
            c.println("STATUS");
            
            readStatusMessage(c, silent);
            
        } catch (Exception ex) {
            showWarn("There is no PrintServer running at " + host + ":" + port, silent);
            //showError("Failed to get status message:\n" + ex.toString(), ex, silent);
        }
        
        
    }
    
    
    
    private void processClear(String[] args) {
        
        int port = getPortFromArgs(args);
        String host = getHostFromArgs(args);
        boolean silent = getSilentFromArgs(args);
        
        try {
            Client c = new Client(host, port);
            c.println(Command.CLEAR.toString());
            readStatusMessage(c, silent);
            c.close();
        } catch (Exception ex) {
            showWarn("There is no PrintServer running at " + host + ":" + port, silent);
            //showError("Failed to get status message:\n" + ex.toString(), ex, silent);
        }
        
    }
    
    
    
    private void processConnections(String[] args) {
        
        int port = getPortFromArgs(args);
        String host = getHostFromArgs(args);
        boolean silent = getSilentFromArgs(args);
        
        try {
            
            Client c = new Client(host, port);
            c.println(PrintServer.Command.CONNECTIONS.toString());
            
            readStatusMessage(c, silent);
            
            
        } catch (Exception ex) {
            LOGGER.error("Error getting connections", ex);
            showError("Error trying to list connections @" 
                        + host + ":" + port, ex, silent);
        }
    }
    
    
    /**
     * Create and execute a print job.  This just opens a client connection and
     * forwards all the commands to the specified server.
     * @param args 
     */
    private void processPrint(String[] args) {
        
        int port = getPortFromArgs(args);
        String host = getHostFromArgs(args);
        boolean silent = getSilentFromArgs(args);
        
        // we will just forward the entire argument list to the specified server
        try {
            Client c = new Client(host, port);
            for (String s : args) {
                c.println(s);
            }
            
            readStatusMessage(c, silent);
            
        } catch (Exception ex) {
            showError("Error while printing!", ex, silent);
        }
    }
    
    
    
    /**
     * This is called directly, does not go through PrintServer
     * @param args 
     */
    private void processBackup(String[] args) {
                
        // information needed = Database, Host and OutputFile
        String dbName = getArgumentValue(args, "--database", "-db");
        String host = getArgumentValue(args, "-h");
        String file = getArgumentValue(args, "-f");
        
        if (dbName == null || file == null) {
            showWarn(" - Must supply -db (database) and -f (output_file) parameters", false);
            //this.println(MessageType.ERROR + serverName + " - Must supply -db (database) and -f (output_file) parameters");
            return;
        }
        
        PgUtils.backupPgDatabase(host, dbName, file);
        
    }
    
    
    /**
     * This is called directly, does not go through PrintServer
     * @param args 
     */
    private void processRestore(String[] args) {
                
        // information needed = Database, Host and OutputFile
        String dbName = getArgumentValue(args, "--database", "-db");
        String host = getArgumentValue(args, "-h");
        String file = getArgumentValue(args, "-f");
        String driver = getArgumentValue(args, "-c");
        
        if (dbName == null || file == null) {
            showWarn(" - Must supply -db (database), -c (driver_jar) and -f (input_file) parameters", false);
            //this.println(MessageType.ERROR + serverName + " - Must supply -db (database) and -f (output_file) parameters");
            return;
        }
        
        ReportConnectionManager.addToClasspath(new File(driver));
        PgUtils.restorePgDatabase(host, dbName, file);
        
    }
    
    
    /**
     * This reads status message returned from the Client (ServerProcess).
     * Reads multi-line messages, and displays in JOptionPane if silent is false.
     * <p>This closes the client!
     * @param c Client to read message from.
     * @param silent suppresses visible pop-ups if true
     */
    private void readStatusMessage(Client c, boolean silent) {
        
        try {
        // wait for output from service (success, etc)
            int waitCt = 0;
            while (!c.hasInput()) {
                Thread.sleep(20);
                waitCt++;
                if (waitCt >= 100) {
                    break;
                }
            }
            
            String status = "";
            // now read input until N/A
            while (c.hasInput()) {
                if (!status.isEmpty()) status += "\n";
                status += c.readLine();
                
                try {   // must wait a bit, so server has time to respond
                    Thread.sleep(100);
                } catch (Exception ex) {
                }
            }
            
            
            if (status == null) {
                showError("Unknown result, the return status was null!", null, silent);
            } else {
                // stop command returns a status message of ERROR or INFO 
                showStatus(status, silent);
            }
            
        } catch (Exception ex) {
            showError("Exception while waiting for status message", ex, silent);
        } finally {
            try {
                if (c != null && !c.isClosed()) {
                    c.close();
                }
            } catch (Exception ex) {
                LOGGER.error("Error closing the Client c!", ex);
            }
        }
        
    }
    
    
    /**
     * Stops the Print Server.  This always closes ALL connections.  If a
     * print job is processing it will complete first, unless --force true
     * is specified as argument.  To close a connection w/o stopping the server
     * use the CLOSE command.
     * <p>Like START, STOP can only be performed on localhost.
     * @param args 
     */
    private void processStop(String[] args) {
        
        int port = getPortFromArgs(args);
        String host = getHostFromArgs(args);
        boolean force = false;
        for (String a : args) {
            if (a.toLowerCase().contains("-f")) {
                force = true;
                break;
            }
        }
        
        boolean silent = getSilentFromArgs(args);
        
        
        try {
            // will return error message if host is not == to loopbackAddress
            Client c = new Client(host, port);
            c.println("STOP");
            if (force) {
                c.println("--force");
            }
            c.println(";"); // terminate
            
            // wait for output from service (success, etc)
            readStatusMessage(c, silent);
            
        } catch (Exception ex) {
            showError("Error while trying to stop server @ localhost:" + port + "\n" + ex.toString(), ex, silent);
        }
        
    }
    
    
    /**
     * CLOSE is a variation of STOP that only closes a specified connection, 
     * and does not stop the server.  It is permitted to ADD and CLOSE 
     * connections on a remote host, but be aware that all paths (classpath, etc)
     * are going to be relative to the host, not the local computer.
     * @param args 
     */
    private void processClose(String[] args) {
        
        int port = getPortFromArgs(args);
        String host = getHostFromArgs(args);
        String id = getArgumentValue(args, "-id", "-n");
        boolean silent = getSilentFromArgs(args);
        
        if (id == null) {
            id = ReportConnectionManager.DEFAULT_CONNECTION_NAME;
        }
        
        try {
            Client c = new Client(host, port);
            c.println("CLOSE");
            c.println("--identifier");
            c.println(id);
            c.println(";"); // end of args signal
            
            // wait for output from service (success, etc)
            readStatusMessage(c, silent);
            
        } catch (Exception ex) {
            showError("Error while trying to close connection " + id + " on PrintServer @ " + host + ":" + port + "\n" + ex.toString(), ex, silent);
        }
        
    }
    
    /**
     * Extracts the --port parameter from list of args, if there is on.
     * If not found or error, then returns ServerManager.DEFAULT_SERVER_PORT
     * @param args
     * @return 
     */
    public static int getPortFromArgs(String[] args) {
        
        int port = ServerManager.DEFAULT_SERVER_PORT;
        String val = getArgumentValue(args, "-po");
        if (val != null) {
            try {
                port = Integer.valueOf(val);
            } catch (Exception ex) {
                LOGGER.error("Invalid parameter value for port: " + val, ex);
                port = ServerManager.DEFAULT_SERVER_PORT;
            }
        }
        
        return port;
    }
    
    
    public static String getHostFromArgs(String[] args) {

        
        String val = getArgumentValue(args, "-h");
        String host = (val == null ? ServerManager.LOCALHOST : val);
        boolean silent = getSilentFromArgs(args);
        
        if (val != null) {
            // test if host reachable
            try {
                InetAddress addr = InetAddress.getByName(host);
                if (!addr.isReachable(1500)) {
                    // this will probably never be reached - throws error if not reachable
                    showError("The Host: " + host + " is not reachable!", null, silent);
                    throw new IllegalArgumentException("The specified host is not reachable: " + host);
                }
            } catch (Exception ex) {
                showError("The Host: " + host + " is not reachable!", ex, silent);
                // throw error to cause this halt process
                throw new IllegalArgumentException("The Host: " + host 
                            + " is not reachagble!", ex);
            }
        }
        return host;
    }
    
    
    /**
     * Silent has no arguments, false if missing
     * @param args
     * @return
     */
    public static boolean getSilentFromArgs(String[] args) {
        
        for (String s : args) {
            if (s.toLowerCase().contains("-si")) {
                return true;
            }
        }
        return false;
        
    }
    
    
    /**
     * If --verbose or -v is in arg list then it ups the terminal output
     * @param args
     * @return true if an arg containing -v is found, false otherwise
     */
    public static boolean getVerboseFromArgs(String[] args) {
        
        for (int i = 0; i < args.length; i++) {
            if (args[i].toLowerCase().contains("-v")) {
                // found the --verbose or -v option
                return true;
            }
        }
        
        // default is NOT verbose
        return false;
    }
    
    
    /**
     * Extract an argument from the list
     * @param args
     * @param key one or more keys to search for
     * @return String value of key if found, null if not found, or empty string if key exists w/o a value.
     */
    public static String getArgumentValue(String[] args, String... key) {


        for (int i = 0; i < args.length; i++) {
            boolean found = false;
            for (String k : key) {
                if (args[i].toLowerCase().contains(k.toLowerCase())) {
                    found = true;
                    break;
                }
            }
            if (found) {
                i++;
                if ((i+1) > args.length || args[i].startsWith("-") || args[i].startsWith(";")) {
                    return "";    // empty string - key exists but no value
//                    throw new IllegalArgumentException("Found " + args[i-1] + " param at end of parameters list");
                }
                return args[i];
            }
        }
        return null;
    }
    
    
    /**
     * Gets all matching arguments from the args list
     * @param args
     * @param key
     * @return list of values, or empty list if none found
     */
    public static List<String> getArgumentValueList(String[] args, String key) {
        
        List<String> vals = new ArrayList<String>();
        for (int i = 0; i < args.length; i++) {
            if (args[i].contains(key)) {
                i++;
                if ((i+1) > args.length) {
                    throw new IllegalArgumentException("Found " + key + " param at end of parameters list");
                }
                vals.add(args[i]);
            }
        }
        return vals;
    }
    
    
    /**
     * Use this to log / display the messages returned by PrintServer.
     * @param message
     * @param silent 
     */
    private static void showStatus(String message, boolean silent) {
        // show status to user
        String[] split = message.split("\\|");
        
        try {
            switch (ServerManager.MessageType.statusValue(split[0])) {
                case INFO:
                    showInfo(split[1], silent);
                    break;
                case WARN:
                    showWarn(split[1], silent);
                    break;
                case ERROR:
                    showError(split[1], null, silent);
                    break;
                default:
                    showInfo(message, silent);
            }
        } catch (Exception ex) {
            // not a valid status message, just show info:
            showInfo(message, silent);
        }

    }

    
    public static void showError(final String msg, Throwable err, boolean silent) {
        
        if (err != null) {
            LOGGER.error(msg, err);
        } else {
            LOGGER.error(msg);
        }
        
        if (!silent) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    JOptionPane.showMessageDialog(null, msg, "Error:", JOptionPane.ERROR_MESSAGE);
                }
            });
        }
        
    }
    
    
    public static void showInfo(final String msg, boolean silent) {
        LOGGER.info(msg);
        // we do NOT want to show message dialogs for INFO! (is very annoying)
        if (!silent) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    JOptionPane.showMessageDialog(null, msg, "Jasper Print Server", JOptionPane.INFORMATION_MESSAGE);
                }
            });
        }
    }
    
    
    public static void showWarn(final String msg, boolean silent) {
        LOGGER.warn(msg);
        if (!silent) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    JOptionPane.showMessageDialog(null, msg, "Jasper Print Server", JOptionPane.WARNING_MESSAGE);
                }
            });
        }
    }
    
    
    private void processHelp(String[] args) {
        
        // we always assume that the 1st arg is HELP
        if (args.length <= 1 || (args.length == 2 && ";".equals(args[1]))) {
            System.out.print(JasperPrintMain.getHelpAbout());
            return;
        }
        
        String type = args[1].toUpperCase().trim();
        System.out.println("TYPE = " + type);
        PrintServer.Command cmd = null;
        try {
            cmd = PrintServer.Command.valueOf(type);
            if (cmd == null) {
                System.out.println("WARN: Invalid parameter: " + cmd);
                System.out.print(JasperPrintMain.getHelpAbout());
                return;
            }
        } catch (Exception ex) {
            System.out.println("ERROR: " + ex.toString());
            System.out.print(JasperPrintMain.getHelpAbout());
            return;
        }
        
        switch  (cmd) {
            case START:
                System.out.print(JasperPrintMain.getHelpStart());
                break;
            case STOP:
                System.out.print(JasperPrintMain.getHelpStop());
                break;
            case ADD:
                System.out.print(JasperPrintMain.getHelpAdd());
                break;
            case CLOSE:
                System.out.print(JasperPrintMain.getHelpClose());
                break;
            case PRINT:
                System.out.print(JasperPrintMain.getHelpPrint());
                break;
            case CLEAR:
                System.out.print(JasperPrintMain.getHelpClear());
                break;
            case HELP:
                System.out.print(JasperPrintMain.getHelpHelp());
                break;
            case PROMPTS:
                System.out.println(JasperPrintMain.getHelpPrompts());
                break;
            default:
                System.out.println("Inavalid: no help page for : " + cmd);
                System.out.print(JasperPrintMain.getHelpAbout());
        }
        
    }
    
    

}