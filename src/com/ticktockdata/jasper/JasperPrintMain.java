package com.ticktockdata.jasper;

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


import com.ticktockdata.jasperserver.CommandLineProcessor;
import com.ticktockdata.jasperserver.PrintServer;
import com.ticktockdata.jasperserver.ServerManager;
import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import org.apache.log4j.Logger;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * This class is the entry point when running as JasperServer and is 
 * not used when using JasperPrint as an embedded printing solution.
 * @author JAM {javajoe@programmer.net}
 * @since Aug 23, 2018
 */
public class JasperPrintMain {

    public static Logger LOGGER = Logger.getLogger(JasperPrintMain.class);
    public static final String VERSION = "BETA_1";
        
    
    public JasperPrintMain() {
        

        String compName = "My Business\n124 N Main St\nBigCity, US 22184";
        
        
        
        Map<String, Object> defParams = new HashMap<String, Object>();
        defParams.put("?CompNameAddr", compName);
        ReportManager.setDefaultReportParameters(defParams);
        
        JasperReportImpl rpt = new JasperReportImpl("/home/default/FurnitureLogic/penwood/ClassicUtils/print/Reports/Expense/CheckDispersementsDetail.jrxml");
        rpt.setPrintAction(PrintExecutor.Action.PREVIEW);
        rpt.setPrinter("PDF-Printer");    //"PDF-Printer");    //"LaserJet");   // "HP_8610");
        rpt.setCollate(true);
        rpt.setCopies(2);
        rpt.setShowDialog(false);
        rpt.setDuplex(PrintAction.Duplex.LONG_EDGE);
        
        rpt.setProgressDelay(0);
        
        
        //rpt.setParameter("START_DATE", new Date(117, 5, 30));
        rpt.setParameter("START_DATE", new Date(117, 0, 1));
        
        //rpt.setParameter("END_DATE", new Date(117, 5, 30));
        rpt.setParameter("END_DATE", new Date(117, 11, 31));
        rpt.setParameter("BANK_ACCOUNT", "1010 - Checking Penwood CSB");

        rpt.execute();
        
        
        if (false) {
        
            JasperReportImpl rpt2 = new JasperReportImpl("/home/default/share/Clients/Penwood/2018-08-08_Panels_Update/PanelShopOrder.jrxml");

    //        rpt2.setParameter("REPORT_CONNECTION", ConnectionManager.getReportConnection());
            rpt2.setParameter("P_DATE_PRODUCTION", new Date(118, 5, 13));
    //        Date reportDate = new Date(118, 5, 13);
    //        System.out.println("ReportDate = " + reportDate);
    //        params.put("P_DATE_PRODUCTION", reportDate);

            rpt2.setPrintAction(PrintExecutor.Action.PREVIEW);
            rpt2.setConnectionID("panels");

            rpt2.execute();
        
        }
        
//        ReportManager.executeReport(rpt2);
        
        
        // wait for reports to print
        ReportManager.shutdown(false);
        System.out.println("shut down the report manager");
        
        
    }
    
    
    /**
     * Use this to add jars and classes to the system classpath. Public access
     * so it can be used outside of this class. This code copied from another
     * application written by JAM.
     * <p>
     * Does not throw exception if invalid, but records warning in log.
     * @since v2021.1, JAM
     * @param _path
     */
    public static void addToClassPath(String _path) {
        try {
            // get the java.class.path property
            String cp = System.getProperty("java.class.path");
            // if this jar is already on class-path then don't add again
            java.nio.file.Path path = Paths.get(_path);
            if (cp.contains(path.getFileName().toString())) {
                LOGGER.debug("Resource already exists on Classpath: " + _path);
                return;
            }
            if (!Files.exists(path)) {
                LOGGER.error("Not a valid file to add to Classpath: " + _path);
            }
            // compatibile with Java > 8 does not use URLClassLoader like my earlier attempts.
            ClassLoader cl = ClassLoader.getSystemClassLoader();
            // NOTE:  for compatibility with BeanShell we need to use Array for varargs param type
            Method method = cl.getClass().getDeclaredMethod("appendToClassPathForInstrumentation", new Class[]{String.class});
            method.setAccessible(true);
            method.invoke(cl, new Object[]{_path});
            LOGGER.debug("ADDED resource to ClassPath: " + _path);
            // update the java.class.path property
            System.setProperty("java.class.path", System.getProperty("java.class.path") + File.pathSeparator + _path);
            LOGGER.debug("Successfully added to classpath: " + _path);
        } catch (Throwable ex) {
            LOGGER.warn("Failed to add to classpath:", ex);
        }
    }
    
    
    /**
     * This is strictly a test routine, not used in live runtime.
     */
    public static boolean initConnection() {
        
        try {
//            SwingUtilities.invokeLater(new Runnable() {
//                @Override
//                public void run() {
//                    JOptionPane.showMessageDialog(null, "Initializing connection...");
//                }
//            });
            ConnectionInfo connInfo = new ConnectionInfo();
            connInfo.setDriverJars("/home/default/postgresql-9.3-1100.jdbc4.jar");
            connInfo.setDriverClass("org.postgresql.Driver");
            connInfo.setUrl("jdbc:postgresql://localhost:5432/penwood");
            connInfo.setUser("postgres");
            connInfo.setPassword("true");
            
            return ConnectionManager.registerConnection(connInfo, false);
            
        } catch (final Exception ex) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    JOptionPane.showMessageDialog(null, "Error init Conn: " + ex.toString());
                }
            });
        }
        
        return false;
    }
    
    
    /**
     * 
     * @param args the command line arguments
     */
    public static void main(final String[] args) {
        
        
        //"org.jvnet.substance.skin.SubstanceDustCoffeeLookAndFeel"
        String lookClass = "net.sf.nimrod.NimRODLookAndFeel";
        
        /* Set the look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* installs a custom L&F, or tries Nimubs if that N/A
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            
            // if a parameter --lookandfeel is specified in args, then
            // use the supplied class!
            boolean found = false;
            for (String s : args) {
                if (found) {
                    lookClass = s;
                    break;
                } else if (s.toLowerCase().equals("--lookandfeel")) {
                    found = true;
                }
            }
            //org.jvnet.substance.skin.SubstanceBusinessBlackSteelLookAndFeel
            javax.swing.UIManager.setLookAndFeel(lookClass);
            //System.out.println("Set the Look & Feel to " + lookClass);
        } catch (Exception ex) {
            
            //System.out.println(lookClass + " L&F not installed!");
            
            try {
                // if nothing else then try to set Nimbus
                for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                    //System.out.println("L&F: " + info.getName());
                    if ("Nimbus".equals(info.getName())) {
                    //if ("GTK+".equals(info.getName())) {
                    //if ("Metal".equals(info.getName())) {
                    //if ("CDE/Motif".equals(info.getName())) {
                        javax.swing.UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception x) {
            }
        }
        //</editor-fold>

        
//        // this was testing code!
//        if (!initConnection()) {return;}
////        TestPrint.main(args);
////        if (true) return;
//        
//        new JasperPrintMain();
//        if (true) return;

    
        /**
         * The following is the entry point for Command-Line usage of JasperPrint
         */
        if (args.length > 0) {
            //System.out.println(" >>> JasperPrintMain, arg count = " + args.length);
            Thread launcherThread = new Thread() {
                @Override public void run() {
                    try {
                    // this adds the ; character @ end of input if not present
                    int len = args.length;
                    if (!";".equals(args[len-1])) {
                        len ++;
                        String[] newArgs = new String[len];
                        newArgs[len-1] = ";";
                        for (len = 0; len < args.length; len++) {
                            newArgs[len] = args[len];
                        }
                        new CommandLineProcessor(newArgs);
                    } else {
                        new CommandLineProcessor(args);
                    }
                    
                    } catch (Exception ex) {
                        System.out.println("Error: " + ex.toString());
                        ex.printStackTrace();
                    }
                    
                    
                }
            };
            launcherThread.start();
            
        } else {
            System.out.println(getHelpAbout());
        }

    }
    
    
    
    
    private static String getHelpHeader() {
        return "\n>>> -----------------------------------\n"
            + "JasperPrint " + JasperPrintMain.VERSION + "\n"
            + "Copyright 2018 - 2021, Joseph A Miller / Tick Tock Data\n"
            + "email: support@ticktockdata.com\n\n";
    }
    
    
    public static String getHelpAbout() {
        // ToDo: help header / footer
        return 
                getHelpHeader()
                + "JasperPrint: A java program for printing with JasperReports.\n"
                + "This program can be embedded within a Java program, or it\n"
                + "can be run as a stand-alone program that opens a TCP\n"
                + "Socket which accepts commands to print JasperReport (.jrxml)\n"
                + "files.  This help is for use as a Server.\n"
                + "\n"
                + "The Server must be started using the START command via Java\n"
                + "and the JasperPrint.jar file, usually in the form of:\n"
                + "java -jar <path_to>/JasperPrint.jar START <required_args_here>\n"
                + "After a server is running further Commands may be send\n"
                + "via the same java -jar JasperPrint.jar <Command> method, or\n"
                + "via any programming method which you can use to connect to\n"
                + "the server socket port, including telnet, nc, etc.\n"
                + "\n"
                + "When using any method other then java -jar, you must end\n"
                + "each command list with a ';' character as the last parameter.\n"
                + "\n"
                + "Following is a list of valid commands:\n"
                + PrintServer.Command.START + "   (starts a server process for printing via command line)\n"
                + PrintServer.Command.STOP + "    (stops a server)\n"
                + PrintServer.Command.ADD + "     (add a database connection to a running server)\n"
                + PrintServer.Command.CLOSE + "   (Close a specified database connection on a server)\n"
                + PrintServer.Command.CLEAR + "   (dump report cache to force reload of reports)\n"
                + PrintServer.Command.PRINT + "   (prints a report using a specified server)\n"
                + "\n"
                + "To get additional info on any of the above commands use " + PrintServer.Command.HELP + "\n"
                + "    Example:  " + PrintServer.Command.HELP + " " + PrintServer.Command.START + "\n"
                + "\n"
                + ">> Commands to obtain status / info of a server:\n"
                + PrintServer.Command.STATUS + "  (prints a status message about a running server)\n"
                + PrintServer.Command.CONNECTIONS + " (lists all registered database connections for a server)\n"
                + "\n"
                + ">> All usage of JasperPrint is always one of above commands,\n"
                + "   usually followed by additional arguments.\n"
                + "\n"
                + "Some Standard OPTIONAL arguments (each with [default value]): \n"
                + "--host  [" + ServerManager.LOCALHOST + "] (which host to connect to, specify to print to remote server\n"
                + "--port [" + ServerManager.DEFAULT_SERVER_PORT + "] (the port used by the Print Server)\n"
                + "--identifier [" + ConnectionManager.DEFAULT_CONNECTION_NAME + "] (also -id, the 'Name' of the database connection)\n"
                + "--fonts (path of directory containing JasperReport font extensions (must be .jar files))\n"
                + "--silent OR -si (suppresses visible Message Boxes)\n"
                + "--verbose OR -v (increases terminal output)\n"
                + getHelpFooter();
    }
    
    
    
    
    public static String getHelpStart() {
        return
                getHelpHeader()
                + "Help with the START command:  You must START a \n"
                + "JasperPrint server socket before you can PRINT\n"
                + "\n"
                + "The START command is usually followed by arguments that\n"
                + "allow JasperPrint to make a JDBC database connection.\n"
                + "The following arguments are available, to register (open)\n"
                + "a Database Connection, all arguments are required\n"
                + "except the identifier and port.\n"
                + "Identifier is used to specify a connection with a\n"
                + "different name then the default, which is '" + ConnectionManager.DEFAULT_CONNECTION_NAME + "'.\n"
                + "Port may be specified if you wish to run multiple\n"
                + "Servers on a single host, or to use a port other than " + ServerManager.DEFAULT_SERVER_PORT+ "\n"
                + "\n"
                + "--identifier <connection_name>\n"
                + "--port <port_number>\n"
                + "--classpath <jdbc_driver_jar_path>\n"
                + "--driver <driver_class>\n"
                + "--url <jdbc:url>\n"
                + "--user <user_name>\n"
                + "--password <password>\n"
                + "\n"
                + "JasperPrint does not contain any database drivers,\n"
                + "you will need to add them via --classpath.\n"
                + "\n"
                + "The default ServerSocket port is " + ServerManager.DEFAULT_SERVER_PORT + ".\n"
                + "\n"
                + "Can add custom fonts via JasperReport Font Extensions (.jar files)\n"
                + "by specifying the directory they're in with --fonts <path_to_dir>\n"
                + "\n"
                + "You can host multiple database connections on a\n"
                + "single Server (port) by providing an --identifier for\n"
                + "each connection, or you can create multiple servers\n"
                + "by specifying a different --port for each server.\n"
                + "\n"
                + "Here is a sample connection to PostgreSQL (Linux syntax):\n"
                + "\n"
                + "START --classpath '/home/joe/libs/postgresql-9.3-1100.jdbc4.jar' --driver 'org.postgresql.Driver' --url 'jdbc:postgresql://localhost:5432/my_db' --user 'joe' --password 'secret'\n"
                + getHelpFooter();
    }
    
    
    
    public static String getHelpAdd() {
        return
                getHelpHeader()
                + PrintServer.Command.ADD + " command help:\n"
                + "Used to add an additional Database Connection to an\n"
                + "existing Print Server.\n"
                + "This uses the same Database Connection arguments as\n"
                + "the " + PrintServer.Command.START + " command, but instead of\n"
                + "starting a new server it adds (registers) another\n"
                + "Database Connection to that server.\n"
                + "Note that you need to specify the --identifier (-id)\n"
                + "argument in order to have more than one connection\n"
                + "on a single Print Server. (Give each one a name)\n"
                + "\n"
                + "When using the " + PrintServer.Command.PRINT + " command you then specify\n"
                + "which Database Connection to use with --identifier (-id)\n"
                + "\n"
                + "\n Note that ADD cannot be used when communicating to the\n"
                + "Server Socket directly via TCP socket,\n"
                + "only via command: java -jar ADD <args>.\n"
                + getHelpFooter();
    }
    
    
    
    
    public static String getHelpPrint() {
        return
                getHelpHeader()
                + "Help with PRINT: Used to Print, Preview or Export\n"
                + "a Jasper Report.  This command is followed by\n"
                + "arguments specifying the path of the .jrxml file,\n"
                + "the report's required parameters and optional \n"
                + "specifications for printing / previewing / exporting.\n"
                + "\n"
                + "Here is a list of the available arguments:\n"
                + "\n"
                + "--identifier <name> (optional, for multi-connection use)\n"
                + "--progress_delay or -delay <seconds> (default = 5)\n"
                + "--file (or --report) <full_path_to_.jrxml_file> (required)\n"
                + "--action <PRINT | PREVIEW | EXPORT_TO_xxx>\n"
                + "       (default action is PREVIEW if none specified)\n"
                + "\n"
                + "When using the PRINT action:\n"
                + "--printer <printer_name> (if no printer specified\n"
                + "                  then the print dialog will show)\n"
                + "--copies <copy_count> (default is 1 if not specified)\n"
                + "--collate <true | false> (default is true, \n"
                + "                          might not have any effect)\n"
                + "--duplex <OFF | LONG_EDGE | SHORT_EDGE> (default is OFF, \n"
                + "                          might not have any effect)\n"
                + "--show_dialog <true | false> (default is true)\n"
                + "\n"
                + "When using an EXPORT_TO_xxx action:\n"
                + "xxx must be one of: PDF | CSV | XLS | HTML\n"
                + "--xfile <file_to_save> (shows file picker if missing)\n"
                + "--overwrite <true | false> (asks if false, default = false)\n"
                + "\n"
                + "Regardless of the print action, you must always\n"
                + "specify the required report parameters unless the\n"
                + "report is set up with self-prompts and you specify\n"
                + "--prompt TRUE \n"
                + "This will show a dialog that contains the prompt\n"
                + "controls specified by the report.  Run HELP PROMPTS\n"
                + "for further help on self-prompts.\n"
                + "\n"
                + "The Progress Delay command specifies how many seconds\n"
                + "to wait before a Progress Dialog with a Cancel\n"
                + "button will appear, allowing the report execution to\n"
                + "be canceled.  0 = immediate, -1 = never.\n"
                + "Default value is 5 seconds, do not need to specify.\n"
                + "\n"
                + "To send a report parameter via the command line use:\n"
                + "--parameter <type> <name> <value>\n"
                + "where <type> is one of: STRING | LONG | INTEGER \n"
                + "| BIGDECIMAL | BOOLEAN | DATE | TIMESTAMP\n"
                + "<name> is the name of the parameter, and \n"
                + "<value> is the value to be set.\n"
                + "\n"
                + "For DATE value the required format is 'YYYY-MM-DD'\n"
                + "For TIMESTAMP the format is:  'YYYY-MM-DD HH:MM:SS.MILS'\n"
                + "     Note that the hours are required to be in 24 hr value\n"
                + "     and Seconds and Milliseconds are optional.\n"
                + "     Also note that timestamp needs to be quoted, as it contains spaces.\n"
                + getHelpFooter();
        
    }
    
    
    
    public static String getHelpPrompts() {
        
        return
                getHelpHeader()
                + "Help with Report self-prompts.\n" +
                "JasperPrint allows Jasper Reports (.jrxml files) to have\n" +
                "parameter prompts embedded, where a dialog appears\n" +
                "and allows the user to select values from the prompt controls.\n" +
                "The parameters must have 'Use as a prompt' option checked.\n" +
                "The Parameter's Description text is used as the prompt's label.\n" +
                "An attempt is made to extract the Default Value using a Groovy parser.\n" +
                "Properties are utilized as described below.\n" +
                "\n" +
                "Dates\n" +
                "If a Parameter's Class is java.util.Date then it will show a date\n" +
                "picker control.  If a Date parameter with a Name that containing\n" +
                "'start' and another containing 'end' are both present then the 2\n" +
                "are combined in a 'Date Range' control.\n" +
                "\n" +
                "Boolean\n" +
                "Parameters of type java.lang.Boolean display a checkbox control.\n" +
                "\n" +
                "Text\n" +
                "For a free-fill type in box use type java.lang.String.  If you want\n" +
                "the end results to be anything other then a String you need to parse\n" +
                "the results in your report query.\n" +
                "\n" +
                "BigDecimal\n" +
                "A parameter with class java.math.BigDecimal will show a\n" +
                "Formatted Field control to enter numbers.\n" +
                "You can specify a format pattern by adding a Property named\n" +
                "'Format' to the parameter, and enter a format pattern to use.\n" +
                "It is best to not use % or currency symbols, if you do the user\n" +
                "will need to type those characters for the value to be valid.\n" +
                "\n" +
                "Query Parameters\n" +
                "By adding a Property named 'Query' to a Parameter and setting\n" +
                "its value to a SQL query text you can create a prompt with a list\n" +
                "of values to choose from.\n" +
                "This supports String and Long parameter classes.\n" +
                "The query's first column is always the displayed value, used as a String.\n" +
                "If the query returns 2 or more columns the first column is displayed\n" +
                "and the second column is used as the parameter value. \n" +
                "A Long parameter must have 2 columns, and the second column\n" +
                "must be of type Long (BIGINT)."
                + getHelpFooter();
    }
    
    public static String getHelpClear() {
        return
                getHelpHeader()
                + "Help with the CLEAR command:\n"
                + "This is used to clear the report cache.\n"
                + " Used to force reloading of the .jrxml files\n"
                + "after editing, etc.\n"
                + "This command is usually not needed because the server\n"
                + "automatically detects and reloads modified reports.\n"
                + "\n"
                + "The only arguments available is the host / port to use\n"
                + "and the --silent false (or -v / --verbose) parameter\n"
                + "\n"
                + "Sample usage:\n"
                + "\n"
                + "CLEAR\n"
                + "CLEAR --host localhost --port 12345\n"
                + getHelpFooter();
    }
    
    
    
    public static String getHelpHelp() {
        return 
                getHelpHeader()
                + "Help with HELP (Oh my, you are really lost!)\n"
                + "To see help on a particular subject use one of the following:\n"
                + "HELP " + PrintServer.Command.START + "\n"
                + "HELP " + PrintServer.Command.STOP + "\n"
                + "HELP " + PrintServer.Command.ADD + "\n"
                + "HELP " + PrintServer.Command.PRINT + "\n"
                + "HELP " + PrintServer.Command.CLEAR + "\n"
                + getHelpFooter();
//        System.out.println("HELP " + PrintServer.Command.STATUS);
//        System.out.println("HELP " + PrintServer.Command.CONNECTIONS);

    }
    
    public static String getHelpClose() {
        return
                getHelpHeader()
                + PrintServer.Command.CLOSE + " command help:\n"
                + "Use to close a specific Database Connection on an \n"
                + "existing Print Server.\n"
                + "Must specify the --identifier (-id) of the Database\n"
                + "Connection that you want to close.\n"
                + "Optionally specify --port if you have more than one\n"
                + "Print Server running.  Available only for " + ServerManager.LOCALHOST + "\n"
                + "If the --identifier (or -id) parameter is not supplied\n"
                + "it will attempt to close a connection named " + ConnectionManager.DEFAULT_CONNECTION_NAME + "\n"
                + "\n"
                + "Sample of usage (close connection named MyConn):\n"
                + "CLOSE --identifier MyConn\n"
                + getHelpFooter();
    }
    
    
    public static String getHelpStop() {
        return
                getHelpHeader()
                + "STOP command help:\n"
                + "You should always STOP the JasperPrint server when\n"
                + "the application using it is shut down.\n"
                + "This closes all connections registered in the Server.\n"
                + "\n"
                + "It is not permitted to " + PrintServer.Command.START  + " or " 
                + PrintServer.Command.STOP + " a Server on a remote\n"
                + "host, attempting to do so will return an error message.\n"
                + "\n"
                + "The only valid arguments to STOP are:\n"
                + "--port (if not supplied the default port " + ServerManager.DEFAULT_SERVER_PORT + " is used\n"
                + "--force (force termination of running jobs and shut down server)\n"
                + getHelpFooter();
    }
    
    
    
    
    
    
    
    private static String getHelpFooter() {
        return "\n----------------------------------- <<<\n";
    }

}
