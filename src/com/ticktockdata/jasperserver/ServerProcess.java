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

import com.ticktockdata.jasper.ConnectionInfo;
import com.ticktockdata.jasper.ReportConnectionManager;
import com.ticktockdata.jasper.JasperPrintMain;
import com.ticktockdata.jasper.JasperReportImpl;
import com.ticktockdata.jasper.PrintAction;
import com.ticktockdata.jasper.PrintExecutor.Action;
import com.ticktockdata.jasper.ReportManager;
import com.ticktockdata.jasperserver.PrintServer.Command;
import static com.ticktockdata.jasperserver.ServerManager.LOGGER;
import com.ticktockdata.jasperserver.ServerManager.MessageType;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * This class handles actual work for PrintServer - spawned from
 * server.connect()
 * <p>
 * For some commands the server process returns a status message to the caller
 * (socket connection) which is always one of {INFO | WARN | ERROR}, followed by
 * a pipe (|) then then status message text, like:
 * <b>INFO | Stopped the print server</b>
 *
 * @author JAM {javajoe@programmer.net}
 * @since Oct 04, 2018
 */
public class ServerProcess extends Thread {

    private PrintServer server;
    private Socket socket;
    private BufferedReader input = null;
    private PrintWriter output = null;

    public ServerProcess(PrintServer server, Socket socket) {

        this.server = server;
        this.socket = socket;

    }

    @Override
    public void run() {

        try {
            // get input / output streams
            input = new BufferedReader(new InputStreamReader(new BufferedInputStream(socket.getInputStream())));
            output = new PrintWriter(new BufferedOutputStream(socket.getOutputStream()));

            String serverName = server.toString();

            // now check what this process wants to do - 1st word is always the key
            // but discard empty entries (in case direct communication via socket)
            String command = readLine();
            int readCt = 0;
            while (input != null && !socket.isClosed() && (command == null || command.trim().isEmpty())) {
                command = readLine();
                readCt++;
                if (readCt >= 20) {
                    println(MessageType.ERROR + "Could not get Command to run, input was null");
                    close();
                    return;
                }
            }

            // check for TEST command
            if ("test".equalsIgnoreCase(command)) {
                println("Hi there, Java Geek!");
                close();
                return;
            }

            Command cmd = null;
            try {
                cmd = Command.valueOf(command.toUpperCase());
                if (cmd == null) {
                    this.println(MessageType.ERROR + serverName + " - This Command is invalid or not implemented yet: " + command);
                    this.close();
                    return;
                }
            } catch (Exception ex) {
                this.println(MessageType.ERROR + serverName + " - Command is invalid or not implemented yet: " + command + ", Error: " + ex.toString());
                this.close();
                return;
            }

            switch (cmd) {
                case STATUS:

                    this.println(server.getStatusMessage());
                    this.close();    // this will exit this process

                    break;
                case CONNECTIONS:

                    for (String id : ReportConnectionManager.getAllConnections().keySet()) {
                        this.println(id);
                    }
                    this.close();    // this will exit this process

                    break;
                case STOP:
                    
                    // this stops the whole server.
                    // to remove a registered connection use CLOSE instead
                    processStop(serverName);
                    close();

                    break;
                case ADD:

                    processAdd(serverName);
                    close();

                    break;
                case CLOSE:

                    processClose(serverName);
                    close();

                    break;
                case CLEAR:

                    // clear the report cache
                    int ct = ReportManager.clearReportCache();
                    this.println(MessageType.INFO + "Cleared " + ct + " reports from cache.");
                    this.close();

                    break;
                case PRINT:

                    processPrintJob();

                    break;
                case HELP:

                    //String[] args = readCommands();
                    processHelp(readCommands());
                    close();

                    break;
                default:

                    this.close();
            }

        } catch (Exception ex) {
            LOGGER.error("Error during socket startup", ex);
            this.print(MessageType.ERROR + "Error during socket startup: " + ex.toString());
        }

    }

    private void processHelp(String[] args) {

        Command cmd = null;
System.out.println("Processing Help");
        if (args.length > 0) {
            // print specific help message, if it can be parsed into a Command
            try {
                cmd = Command.valueOf(args[0].toUpperCase());
                System.out.println("COMMAND = " + cmd);
            } catch (Exception ex) {
                LOGGER.warn("Error extracting Command from " + args[0], ex);
                this.println(MessageType.WARN + "Error extracting Command from " + args[0] + ex.toString());
                cmd = null;
            }
        }

        if (cmd == null) {
            print(JasperPrintMain.getHelpAbout());
            output.flush();
            return;
        }
        
        switch (cmd) {
            case START:
                print(JasperPrintMain.getHelpStart());
                break;
            case ADD:
                print(JasperPrintMain.getHelpAdd());
                break;
            case STOP:
                print(JasperPrintMain.getHelpStop());
                break;
            case CLEAR:
                print(JasperPrintMain.getHelpClear());
                break;
            case CLOSE:
                print(JasperPrintMain.getHelpClose());
                break;
            case PRINT:
                print(JasperPrintMain.getHelpPrint());
                break;
            case HELP:
                print(JasperPrintMain.getHelpHelp());
                break;
            case PROMPTS:
                print(JasperPrintMain.getHelpPrompts());
                break;
            default:
                print(JasperPrintMain.getHelpAbout());
        }

        output.flush();
    }

    /**
     * Registers (adds) a connection to an existing (running) PrintServer.
     *
     * @param serverName
     */
    private void processAdd(String serverName) {

        try {
            
            String[] args = readCommands();
            boolean silent = CommandLineProcessor.getSilentFromArgs(args);
            
            ConnectionInfo info = CommandLineProcessor.getConnectionInfoFromArgs(args, silent);
            
            if (info == null || !info.isValidInfo(!silent)) {
                println(MessageType.ERROR + "Insufficient or invalid args found to make a database connection.");
                close();
                return;
            }
            
            boolean hasID = server.hasConnection(info.getIdentifier());
            
            // attempt to 
            server.addConnection(info, silent);

            if (this.isAlive()) {
                if (server.hasConnection(info.getIdentifier())) {
                    if (hasID) {
                        this.println(MessageType.INFO + serverName + " - Replaced an existing connection named " + info.getIdentifier());
                    } else {
                        this.println(MessageType.INFO + serverName + " - Added a new connection named " + info.getIdentifier());
                    }
                } else {
                    this.println(MessageType.ERROR + serverName + " - Failed to add a connection named " + info.getIdentifier());
                }
            }

        } catch (Exception ex) {
            println(MessageType.ERROR + "Error processing ADD command: " + ex.toString());
        } finally {
            this.close();
        }

    }

    /**
     * Routine that processes the commands for printing a document.
     */
    private void processPrintJob() {

        String errs = "";
        String[] args = readCommands();

        try {
            System.out.println("----------- listing args");
            for (String axe : args) {
                System.out.println(axe);
            }
            System.out.println("----------- end of args");

            // get the identifier, if any
            String id = CommandLineProcessor.getArgumentValue(args, "-id", "-n");
            if (id == null || id.trim().isEmpty()) {
                id = ServerManager.DEFAULT_IDENTIFIER;
            }
            // make sure this id is registered
            if (!ReportConnectionManager.getAllConnections().containsKey(id)) {
                errs += ("\nThere is no database connection named " + id + " registered with this server.");
            }

            // get the report file
            String rptFile = CommandLineProcessor.getArgumentValue(args, "-f", "--report");
            if (rptFile == null) {
                errs += ("\n--file parameter is required (.jrxml file).");
            } else if (!new java.io.File(rptFile).exists()) {
                errs += ("\nFile Not Found: " + rptFile);
            }

            // determine the print action, if specified (PREVIEW if none)
            Action action = Action.PREVIEW;
            String actionText = CommandLineProcessor.getArgumentValue(args, "-ac");
            if (actionText != null) {
                action = Action.fromString(actionText);
                if (action == null) {
                    errs += ("\nInvalid --action specified: " + actionText);
                }
            }

            // if there are any errors at this point then we need to exit
            if (!errs.isEmpty()) {
                println(MessageType.ERROR + "The following error(s) were found:\n" + errs);
                return;
            }

            String arg;

            // *****************************************************************
            // create a new JasperReportImpl
            JasperReportImpl report = new JasperReportImpl(rptFile);

            // *****************************************************************
            // general commands for all action types
            // make sure it uses the correct connection
            report.setConnectionID(id);

            // set the print action
            report.setPrintAction(action);

            // check for progress_delay (do not accept -de, might be --debug
            arg = CommandLineProcessor.getArgumentValue(args, "-pd", "-progress");
            if (arg != null) {
                report.setProgressDelay(Integer.valueOf(arg));
            }

            // check for prompt for parameters
            arg = CommandLineProcessor.getArgumentValue(args, "-prompt");
            if (arg != null) {
                report.setPromptForParameters(Boolean.valueOf(arg));
            }

            // *****************************************************************
            // if the action is for printing then check for printer, copies, etc.
            if (action.equals(Action.PRINT)) {

                // printer
                arg = CommandLineProcessor.getArgumentValue(args, "-pri");
                if (arg != null) {
                    report.setPrinter(arg);
                } else {
                    // set to show dialog
                    report.setShowDialog(true);
                }

                // copy count
                arg = CommandLineProcessor.getArgumentValue(args, "-cop");
                if (arg != null) {
                    report.setCopies(Integer.valueOf(arg));
                }

                // Collate
                arg = CommandLineProcessor.getArgumentValue(args, "-col");
                if (arg != null) {
                    report.setCollate(Boolean.valueOf(arg));
                }

                // duplex
                arg = CommandLineProcessor.getArgumentValue(args, "-du");
                if (arg != null) {
                    report.setDuplex(PrintAction.Duplex.fromString(arg));
                }

                // show dialog
                arg = CommandLineProcessor.getArgumentValue(args, "-sh");
                if (arg != null) {
                    report.setShowDialog(Boolean.valueOf(arg));
                }

            }   // *************************************************************

            // *****************************************************************
            // if this is an export action then look for additional prarameters
            if (action.toString().contains("EXPORT")) {
                arg = CommandLineProcessor.getArgumentValue(args, "-xf");
                if (arg != null) {
                    report.setExportFilePath(arg);
                }
                arg = CommandLineProcessor.getArgumentValue(args, "-ov");
                if (arg != null) {
                    report.setOverwriteExportFile(Boolean.valueOf(arg));
                }
            }   // *************************************************************

            // *****************************************************************
            // Now check for and add all supplied parameters
            for (int i = 0; i < args.length; i++) {
                arg = args[i];
                if (arg.toLowerCase().contains("-pa")) {
                    LOGGER.trace("Found arg: " + arg);
                    if ((i + 3) > args.length) {
                        println(MessageType.ERROR + "Invalid parameter, found -pa / --parameter argument at end of input");
                        return;
                    }
                    String type = args[i + 1].trim().toUpperCase();
                    String name = args[i + 2];
                    String val = args[i + 3];
                    if ("STRING".equals(type)) {
                        report.setParameter(name, val);
                    } else if ("LONG".equals(type)) {
                        report.setParameter(name, Long.valueOf(val));
                    } else if ("INTEGER".equals(type)) {
                        report.setParameter(name, Integer.valueOf(val));
                    } else if ("BIGDECIMAL".equals(type)) {
                        report.setParameter(name, new BigDecimal(val));
                    } else if ("BOOLEAN".equals(type)) {
                        report.setParameter(name, Boolean.valueOf(val));
                    } else if ("DATE".equals(type)) {
                        String[] dt = val.split("[\\-]");
                        Calendar cal = GregorianCalendar.getInstance();
                        cal.clear();
                        cal.set(Integer.valueOf(dt[0]), Integer.valueOf(dt[1]), Integer.valueOf(dt[2]));
                        report.setParameter(name, cal.getTime());
                    } else if ("TIMESTAMP".equals(type)) {
                        String[] s1 = val.split(" ");
                        String[] dt = s1[0].split("[\\-]");
                        String[] tm = s1[1].split("[\\:]");
                        Calendar cal = GregorianCalendar.getInstance();
                        cal.clear();
                        cal.set(Integer.valueOf(dt[0]), Integer.valueOf(dt[1]), Integer.valueOf(dt[2]), Integer.valueOf(tm[0]), Integer.valueOf(tm[1]));
                        // check for seconds
                        if (dt.length > 2) {
                            String[] sec = tm[2].split("[\\.]");
                            cal.set(Calendar.SECOND, Integer.valueOf(sec[0]));
                            if (sec.length > 1) {
                                cal.set(Calendar.MILLISECOND, Integer.valueOf(sec[1]));
                            }
                        }
                        //new java.sql.Timestamp(cal.getTime().getTime());
                        report.setParameter(name, cal.getTime());
                    } else {
                        println(MessageType.ERROR + "Invalid Parameter Type, don't know how to process " + type);
                        return;
                    }

                    // increment counter
                    i += 3;
                }
            }

            report.execute();

            println(MessageType.INFO + "Processed the print job and added to Executor queue.");

        } catch (Exception ex) {
            println(MessageType.ERROR + "Error while processing " + PrintServer.Command.PRINT + " command: " + ex.toString());
            ex.printStackTrace();
        } finally {
            this.close();
        }

    }

    /**
     * Close (un-register) a specified database connection.
     *
     * @param serverName
     */
    private void processClose(String serverName) {

        try {
            // close a specific connection, but do not shut down server
            if (!socket.getInetAddress().isLoopbackAddress()) {
                this.println(MessageType.WARN + serverName + " - This operation is not permitted from a remote connection!");
                this.close();
                return;
            }

            //List<String> argList = readCommands();
            String[] args = readCommands();
            String id = CommandLineProcessor.getArgumentValue(args, "-id", "-n");     // identifier

            if (id != null) {
                // just shutdown one connection
                if (server.hasConnection(id)) {
                    
                    // shutdown this connection
                    server.close(id);

                    if (server.hasConnection(id)) {
                        println(MessageType.ERROR + serverName + " - Failed to close the connection named " + id);
                    } else {
                        println(MessageType.INFO + serverName + " - Closed the connection named " + id);
                    }

                } else {
                    println(MessageType.WARN + serverName + " - No connection named " + id + " is registered.");
                }
            } else {
                println(MessageType.ERROR + serverName + " - Must specify the Database Connection to close!");
            }
        } catch (Exception ex) {
            LOGGER.error("Error while processing CLOSE command", ex);
            println("Error while processing CLOSE: " + ex.toString());
        } finally {
            this.close();   // shutdown this socket connection
        }

    }

    /**
     * Stops the PrintServer. To remove an individual connection use the CLOSE
     * command instead.
     *
     * @param serverName
     */
    private void processStop(String serverName) {

        try {
            
            if (!socket.getInetAddress().isLoopbackAddress()) {
                this.println(MessageType.ERROR + serverName + " - It is not permitted to stop a Print Server running on annother host!");
                return;
            }

            //List<String> argList = readCommands();
            String[] args = readCommands(); // argList.toArray(new String[argList.size()]);
            boolean force = false;
            for (String a : args) {
                // if called from BeanShell script a can be a null element :(
                if (a != null && a.toLowerCase().contains("-f")) {
                    force = true;
                    break;
                }
            }
            
            // shutdown the database server
            server.shutdown(force);
            if (server != null && server.isAlive()) {
                println(MessageType.ERROR + serverName + " - PrintServer Shutdown Failed!");
            } else {
                println(MessageType.INFO + serverName + " - PrintServer Shutdown Successful.");
            }
        } catch (Exception ex) {
            this.println(MessageType.ERROR + serverName + " - Error occurred during shutdown attempt.\n" + ex);
            LOGGER.error("Error in processStop: " + ex.getLocalizedMessage(), ex);
        } finally {
            this.close();   // shutdown this socket connection
        }
    }

    /**
     * Reads one line at a time as a command from the server process (socket)
     * until it gets a ; character as a 'data complete' signal.
     *
     * @return list of commands (read lines)
     */
    private String[] readCommands() {

        List<String> commands = new ArrayList<String>();
        String cmd = "";

        while (cmd != null && !cmd.equals(";")) {
            cmd = readLine();
            // exit only if the ; character is found
            if (";".equals(cmd)) {
                break;
            }
            LOGGER.debug("readCommands: Found Command \"" + cmd + "\"");
            commands.add(cmd);
        }

        return commands.toArray(new String[commands.size()]);

    }

//    /**
//     * Retrieves the command value for a given argument
//     * @param args
//     * @param key
//     * @return null if not found
//     */
//    private String getCommandValue(List<String> args, String... key) {
//        for (int i = 0; i < args.size(); i++) {
//            String arg = args.get(i);
//            for (String k : key) {
//                if (arg.contains(k)) {
//                    i++;
//                    return args.get(i);
//                }
//            }
//        }
//        return null;
//    }
    /**
     * Use this to test if there is input available to be read from the Input
     * Stream. If not input.ready() then it waits 50 milliseconds and checks
     * again, as input is slower when done via Shell command in LibreOffice.
     *
     * @return
     */
    public boolean hasInput() {
        try {
            if (input.ready()) {
                return true;
            }

            try {
                // if not iput available wait 1/20th of a second and check again
                // this was a bug fix - 2019-04-08, JAM
                Thread.sleep(50);
            } catch (Exception ex) {
            }
            return input.ready();
        } catch (Exception ex) {
            LOGGER.error("Error checking input.ready()", ex);
            return false;
        }
    }

    public int read() throws IOException {
        return input.read();
    }

    /**
     * Reads line from input stream. Note that this blocks until input
     * available! Use hasInput() to check if input is available before calling
     * this.
     *
     * @return String that was read, or null if error.
     */
    public String readLine() {
        try {
            return input.readLine();
        } catch (Exception ex) {
            LOGGER.error("Failed to read line from InputStream", ex);
            return null;
        }
    }

    /**
     * Writes text to output stream, but does not terminate or flush
     *
     * @param text
     */
    public void print(String text) {
        output.print(text);
    }

    /**
     * Writes a string to the output stream. Terminates with line ending and
     * flushes!
     *
     * @param text
     */
    public void println(String text) {
        output.println(text);
        output.flush();
    }

    public void println() {
        output.println();
        output.flush();
    }

    public void write(int c) {
        output.write(c);
        output.flush();
    }

    public synchronized void close() {

        if (socket == null || socket.isClosed()) {
            LOGGER.debug("ServerProcess Socket already closed.");
            return;
        }
        try {
            socket.close();
            // set variables null
            input = null;
            output = null;
            socket = null;
            LOGGER.debug("Closed the ServerProcess socket.");
        } catch (Exception ex) {
            LOGGER.error("Error while closing ServerProcess", ex);
        }
    }

    /**
     * For the purpose of adding additional connections to a server the
     * ConnectionInfo object is written to stream by ServerManager
     *
     * @param input
     * @return
     */
    private ConnectionInfo readConnectionInfo(InputStream input) {

        try {
            ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(input));
            return (ConnectionInfo) in.readObject();
        } catch (Exception ex) {
            LOGGER.error("Failed to read the ConnectionInfo object from InputStream");
            return null;
        }
    }

}
