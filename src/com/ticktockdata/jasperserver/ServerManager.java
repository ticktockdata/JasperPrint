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
import java.net.Socket;
import java.security.InvalidParameterException;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.apache.log4j.Logger;

/**
 * ServerManager contains static methods used to start / stop a PrintServer.
 * You should not use the PrintServer class directly, always use through methods
 in this class.
 <p>Note that ServerManager does not have actual access to a running
 PrintServer, all communication between processes is done via sockets
 * @author JAM {javajoe@programmer.net}
 * @since Oct 04, 2018
 */
public class ServerManager {
    
    public enum MessageType {
        INFO,
        WARN,
        ERROR;
        
        /** Similar to valueOf, except it ignores whitespace, capitalization */
        public static MessageType statusValue(String text) {
            if (text == null || text.trim().isEmpty()) {
                throw new InvalidParameterException("parameter may not be null or empty!");
            }
            if (text.trim().toLowerCase().startsWith("info")) {
                return INFO;
            } else if (text.trim().toLowerCase().startsWith("warn")) {
                return WARN;
            } else if (text.trim().toLowerCase().startsWith("err")) {
                return ERROR;
            } else {
                throw new InvalidParameterException(text + " is not a valid MsgStatus");
            }
        }

        @Override
        public String toString() {
            return super.toString() + " | ";
        }
        
        
    }
    
    /**
     * Unless otherwise specified the Print PrintServer will run on this port.
     */
    public static final int DEFAULT_SERVER_PORT = 35246;
    
    public static final String LOCALHOST = "localhost";
    
    public static final String DEFAULT_IDENTIFIER = "default";
    
    public static final Logger LOGGER = Logger.getLogger(ServerManager.class);
    
    
    private ServerManager() {
        // prevents initating this class.
    }
    
    
    /**
     * Starts a new ServerSocket on the given port - should use DEFAULT_SERVER_PORT
     * unless there is a reason to do otherwise.
     * <p>This method does not specify a host, as a ServerSocket can only
     * be started on localhost.
     * @param port the Port on which the ServerSocket runs.
     * Use DEFAULT_SERVER_PORT unless there is a reason to do otherwise.
     * @param connInfo a valid {@link ConnectionInfo} used to open the database 
     * connection.
     * @param silent if false then silently adds / replaces connection to 
     * existing server w/o asking user.
     * @return status message text, syntax: MessageType | message text
     */
    public static String startPrintServer(int port, ConnectionInfo connInfo, boolean silent) {
                
        if (connInfo == null || !connInfo.isValidInfo(false)) {
            // if not valid connection info then we just START a server, don't 
            // add a connection
            if (isPrintServerRunning(LOCALHOST, port)) {
                return MessageType.WARN + "There is already a PrintServer running on port " + port;
            }
            try {
                PrintServer server = new PrintServer(port, connInfo, silent);
                server.setSilent(silent);
                server.start();
                return MessageType.INFO + "Started Successfully with NO Connection registered: " + server.toString();
            } catch (Exception ex) {
                return MessageType.ERROR + "Failed to start a PrintServer on port " + port + " without a Connection";
            }
            
            //throw new InvalidParameterException("ConnectionInfo is not valid - cannot connect!");
        }
        
        // see if this connection already exists or not
        if (isPrintServerRunning(LOCALHOST, port)) {
            // existing already running, so just ADD a connection to it (or update)
            if (!silent) {
                if (isPrintServerRunning(LOCALHOST, port, connInfo.getIdentifier())) {
                    // confirm that we want to replace this 
                    if (!showConfirmationDalog(
                            "<html>There is already an existing connection named "
                            + "<br><b>" + connInfo.getIdentifier() + "</b> open on this Server port."
                            + "<p>Do you want to replace the existing connection?", 
                            "Confirm:")) {
                        LOGGER.debug("User canceled replacing the connection " + connInfo.getIdentifier() + " on this print server");
                        return "INFO | User canceled replacing connection";
                    }
                } else {
                    // confirm that we want to add a new connection
                    if (!showConfirmationDalog(
                            "<html>There is already a server running on this port."
                            + "<p>Do you want to add an additional connection named <b>" + connInfo.getIdentifier() + "</b>?"
                            , "Confirm:")) {
                        LOGGER.debug("User canceled adding an additional connection to this print server");
                        return MessageType.INFO + "User canceled adding connection";
                    }
                }
            }
            
            // confirmed to add / replace connection to server already running.
            LOGGER.info("There is already a print server running, trying to ADD!");
            
            try {
                final Client client = new Client(ServerManager.LOCALHOST, port);
                client.println("ADD");
                client.println("--identifier");
                client.println(connInfo.getIdentifier());
                for (java.io.File j : connInfo.getDriverJars()) {
                    client.println("--classpath");
                    client.println(j.getCanonicalPath());
                }
                client.println("--driver_class");
                client.println(connInfo.getDriverClass());
                client.println("--url");
                client.println(connInfo.getUrl());
                client.println("--user");
                client.println(connInfo.getUser());
                client.println("--password");
                client.println(connInfo.getPassword());
                if (silent) client.println("--silent");
                
                client.println(";");
                
                
                String statMsg = client.readLine();
                if (statMsg == null || statMsg.trim().isEmpty()) {
                    return MessageType.ERROR + "Status unknown, did not get a reply from Server";
                } else {
                    return statMsg;
                }
                
            } catch (Exception ex) {
                LOGGER.error("Was unable to write the ConnectionInfo object to print server!", ex);
                return MessageType.ERROR + "Was unable to write the ConnectionInfo object to Print Server: " + ex.toString();
                //showError("Was unable to write the ConnectionInfo object to print server!", ex, silent);
            }
            
        } else {
            // no server running, start new
            LOGGER.info("Starting a Print Server on port: " + port);
            try {
                PrintServer server = new PrintServer(port, connInfo, silent);
                server.setSilent(silent);
                server.start();
                return MessageType.INFO + "Started successfully: " + server.toString() + " with Connection " + connInfo.getIdentifier();
            } catch (Exception ex) {
                LOGGER.error("Failed to start a Print Server!", ex);
                return MessageType.ERROR + "Failed to start a Print Server: " + ex.toString();
            }
        }
        
        
        
        
        
        // check if this connection is already open
//        if (isPrintServerRunning(LOCALHOST, port, connInfo.getIdentifier())) {
//            if (JOptionPane.showConfirmDialog(null, 
//                    "There is already a connection named " + connInfo.getIdentifier() 
//                            + " established on port " + port 
//                            + ".\nDo you want to shutdown existing Connection and open a new one?"
//                    , "Warning:", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) 
//                    == JOptionPane.NO_OPTION) {
//                
//                return;
//            }
//        }
//        
//        System.out.println("Starting server....");
//        
//        try {
//            PrintServer server = new PrintServer(port, connInfo);
//            server.start();
//        } catch (Exception ex) {
//            LOGGER.error("Failed to start a server...", ex);
//        }
        
        
    }
    
    private static boolean confirmValue = false;
    /**
     * Method to show a Yes / No dialog that is guaranteed to be on the 
     * Event Dispatch Thread.
     * @param message
     * @param title
     * @return 
     */
    public static boolean showConfirmationDalog(final String message, final String title) {
        
        if (SwingUtilities.isEventDispatchThread()) {
            // then just do this
            return JOptionPane.showConfirmDialog(null, message, title, 
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
        } else {
            // not EDT, so must run in EDT
            try {
                confirmValue = false;
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        confirmValue = JOptionPane.showConfirmDialog(null, message, title, 
                            JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
                    }
                });
            } catch (Exception ex) {
                LOGGER.error("Error when showing confirmation dialog.", ex);
                confirmValue = false;
            }
            
            return confirmValue;
        }
        
    }
    
    
    /**
     * Completely STOPS the print server on this port, closes all database
     * connections.
     * @param port 
     */
    public static void stopPrintServer(int port) {
        
        Client c = connectClient(LOCALHOST, port);
        if (c == null) return;
        
        // tell server to stop
        c.println("STOP");
        
        try {
            c.close();
        } catch (Exception ex) {
            LOGGER.error("Error closing the socket connection!", ex);
        }
        
        c = null;
    }
    
    
    /**
     * This Closes the given Connection on a port, and STOPS the PrintServer ONLY
 if there are no other connections open on this server.
     * @param port
     * @param connectionName 
     */
    public static void closePrintServerConnection(int port, String connectionName) {
        
    }
    
    
    
    public static boolean isPrintServerRunning(String host, int port) {
        
        Client socket = null;
        
        try {
            socket = connectClient(host, port);
            // we don't actually want to communicate with socket, just see if it's there
            // !!! Due to implementation changes we need to send a command!
            if (socket != null && !socket.isClosed()) {
                socket.println("TEST");
//                socket.println(";");
                return true;
            } else {
                return false;
            }
            
        } catch (Exception ex) {
            
            return false;
        } finally {
            try {if (socket != null && !socket.isClosed()) socket.close();}
            catch(Exception ex) {}
        }
        
    }
    
    
    private static Client connectClient(String host, int port) {
        
        try {
            Client socket = new Client(host, port);
            return socket;
        } catch (Exception ex) {
            LOGGER.info("Unable to connect to server: " + ex.toString());
            return null;
        }
    }
    
    
    
    public static boolean isPrintServerRunning(String host, int port, String connectionName) {
        
        Client socket = connectClient(host, port);
        if (socket == null) return false;
        boolean found = false;
        
        // ask for names of available connections
        socket.println("CONNECTIONS");
        LOGGER.info("Searching for connection named " + connectionName);
        // read list of connections
        while (socket.hasInput()) {
            String text = socket.readLine();
            LOGGER.info("Found: " + text);
            if (text.equals(connectionName)) {
                LOGGER.warn("Match = true!");
                found = true;
            }
        }
        
        return found;
    }
    
    
    public static void main(String[] args) {
        System.out.println(isPrintServerRunning(LOCALHOST, DEFAULT_SERVER_PORT));
    }
    
    
    
}
