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


import com.ticktockdata.jasper.AboutDialog;
import com.ticktockdata.jasper.ConnectionInfo;
import com.ticktockdata.jasper.ConnectionManager;
import com.ticktockdata.jasper.ReportManager;
import static com.ticktockdata.jasperserver.ServerManager.LOGGER;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidParameterException;
import java.util.Enumeration;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;


/**
 * This class is a wrapper for the actual print server.  Creates a 
 * {@link java.net.ServerSocket} on a provided port.
 * port (default = 35246).
 * <p>Commands are received via text line inputs, following is a list of the
 * available commands and the output that is generated.
 * <ul>
 * <li>STATUS<ul><li>Outputs user-readable string of connections, etc. (multi-line)</ul>
 * <li>CONNECTIONS<ul><li>Outputs the names of all registered connections, 1 per line.</ul>
 * <li>STOP<ul><li>Stops the server - closes all database connections first.
 *      <li>Optional --force {true | false} parameter to force shut-down</ul>
 * </ul>
 * This process just sits and waits for incoming connections on the server port,
 * then spawns a new ServerProcess when a connection is made.
 * @author JAM {javajoe@programmer.net}
 * @since Oct 04, 2018
 */
public class PrintServer extends Thread {
    
    
    public enum Command {
        START,
        STOP,
        CLOSE,
        ADD,
        PRINT,
        CLEAR,
        STATUS,
        CONNECTIONS,
        HELP,
        PROMPTS // used for help page
    }
    
    private String hostName = null; // used by toString()
    private int port;
    
    private ServerSocket server;
    private boolean silent = false;
    
    private TrayIcon trayIcon = null;
    
    /**
     * Constructor that creates a server without any database connections
     * @param port 
     */
    public PrintServer(int port) {
        this.port = port;
    }
    
    
    /**
     * Constructor that creates a server with a database connection
     * @param port
     * @param connInfo 
     */
    public PrintServer(int port, ConnectionInfo connInfo, boolean silent) throws IOException {
        this(port);
        // allow starting a server w/o a connection.
        if (connInfo != null && connInfo.isValidInfo(false)) {
            System.out.println("addConnection called from PrintServer");
            addConnection(connInfo, silent);
        }
    }
    
    
    /**
     * Shows icon in system tray.  This is called from run(), not from 
     * constructor.
     */
    private boolean addTrayIcon() {
        
        try {
            
            Dimension size = SystemTray.getSystemTray().getTrayIconSize();
            final ImageIcon icon = new ImageIcon(getClass().getResource("tray_icon.png"));
            
            // create the pop-up menu
            PopupMenu popup = new PopupMenu();
            
            MenuItem close = new MenuItem("Shutdown");
            close.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    shutdown();
                }
            });
            popup.add(close);
            
            
            MenuItem status = new MenuItem("Status");
            status.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JOptionPane.showMessageDialog(null, getStatusMessage(), 
                        "Server Status", JOptionPane.INFORMATION_MESSAGE, icon);
                }
            });
            popup.add(status);
            
            
            MenuItem about = new MenuItem("About");
            about.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    AboutDialog.showAbout();
                }
            });
            popup.add(about);
            
            
            trayIcon = new TrayIcon(icon.getImage()
                    .getScaledInstance(size.width, size.height, Image.SCALE_SMOOTH)
                    , this.toString(), popup);
            trayIcon.addActionListener(new ActionListener(){
                @Override public void actionPerformed(ActionEvent e) {
                    JOptionPane.showMessageDialog(null, getStatusMessage(), 
                            "Server Status", JOptionPane.INFORMATION_MESSAGE, icon);
                }
            });
            
            trayIcon.setToolTip("JasperPrint server.  Double-Click for status, Right-Click to stop.");
            SystemTray.getSystemTray().add(trayIcon);
            
        } catch (Exception ex) {
            LOGGER.error("Failed to add the tray icon!", ex);
            trayIcon = null;
            return false;
        }
        
        return true;
        
    }
    
    /**
     * Starting of ServerSocket occurs on a new thread
     */
    @Override
    public void run() {
        
        try {
            
            server = new ServerSocket(port);
            
            // display the tray icon
            addTrayIcon();
            
            
            while (true) {
                
                try {
                    
                    // blocks / waits for a connection
                    Socket socket = server.accept();
                    
                    // process this connection on a new thread and wait another connection
                    new ServerProcess(this, socket).start();
                    
                } catch (Exception ex) {
                    if (server.isClosed()) return;
                    LOGGER.error("Error while waiting for or accepting socket connection.", ex);
                    if (!silent) {
                        JOptionPane.showMessageDialog(null, 
                                "Error while waiting for or accepting socket connection:\n" 
                                        + ex.toString(), "Error:", JOptionPane.ERROR_MESSAGE);
                    }
                }
                
            }
            
        } catch (Exception ex) {
            LOGGER.error("Error creating a new server socket on port " 
                    + port + ", exiting now.", ex);
            if (!silent) {
                JOptionPane.showMessageDialog(null, 
                        "Error creating a new server socket on port " 
                        + port + ", exiting now.\n" + ex.toString()
                        , "Fatal Error:", JOptionPane.ERROR_MESSAGE);
            }
            System.exit(1); // kill everything if socket creating fails
        }
    }
    
    
    /**
     * Returns a status message that can be retrieved by the STATUS command
     * @return 
     */
    public String getStatusMessage() {
        
        StringBuilder sb = new StringBuilder();
        
        sb.append(this.toString());
        sb.append("\n");
        
        if (server == null || server.isClosed()) {
            sb.append("Status = ERROR: The Server is not running!\n");
        } else {
            sb.append("Status = OK: Server is alive and well.\n");
        }
        if (ConnectionManager.getAllConnections().size() > 0) {
            sb.append("The following Database Connections are registered:\n");
            for (String key : ConnectionManager.getAllConnections().keySet()) {
                sb.append("CONNECTION: ");
                sb.append(key);
                sb.append(" @ URL: ");
                sb.append(ConnectionManager.getAllConnections().get(key).getUrl());
                sb.append("\n");
            }
        } else {
            sb.append("There are no Database Connections registered.");
        }
        
        return sb.toString().trim();
        
    }
    
    
    public boolean hasConnection(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) {
            throw new InvalidParameterException("identifier may not be null or empty!");
        }
        for (String id:  ConnectionManager.getAllConnections().keySet()) {
            if (identifier.equals(id)) {
                return true;
            }
        }
        return false;
    }
    
    
    /**
     * Method to add a connection - this should be the only way to add a
     * connection, as it registers the connection with the ConnectionManager.
     * <p>If a connection with the same name already exists it will silently
     * unregister and replace that connection.
     * @param connInfo 
     */
    public final void addConnection(ConnectionInfo connInfo, boolean silent) {
        
        System.out.println("Add Connection: " + silent);
        if (connInfo == null || !connInfo.isValidInfo(true)) {
            throw new InvalidParameterException("Connection Info is not valid!");
        }
        
        // check for existing connection of same name
        ConnectionInfo oldInfo = 
                ConnectionManager.getAllConnections().get(connInfo.getIdentifier());
        
        // if already exists, remove and unregister
        if (oldInfo != null) {
            ConnectionManager.unregisterConnection(oldInfo.getIdentifier());
        }
        
        // register new connection and add it to list
        ConnectionManager.registerConnection(connInfo, !silent);
        
    }
    
    
    
    public void shutdown() {
        shutdown(false);
    }
    
    
    
    
    
    /**
     * This is the hard line line exit - calls System.exit().  Unless force =
     * true it will wait until jobs are complete.
     */
    public void shutdown(boolean force) {
        
        try {
            // shutdown the server right away to prevent additional connections.
            if (server != null && !server.isClosed()) {
                LOGGER.info("Closing the Server Socket");
                server.close();
            }
            
            if (!force) {
                // wait for current processes to finish
                try {
                    // TODO: check for processing jobs!
                    
                } catch (Exception ex) {
                }
            }
            // shutdown the printing service - this also closes database connections.
            ReportManager.shutdown(force);
            
            
            if (trayIcon != null) {
                SystemTray.getSystemTray().remove(trayIcon);
            }
            
            // exit system - not needed, it shuts down perfectly fine w/o this
            //System.exit(0);
        } catch (Exception ex) {
            LOGGER.fatal("HOW TERRIBLE - Failed to stop the Server!", ex);
            if (!silent) {
                JOptionPane.showMessageDialog(null, "HOW TERRIBLE - "
                        + "Failed to Stop the Sever!", "Bad Error"
                        , JOptionPane.ERROR_MESSAGE);
            }
        }
        
    }
    
    
    /**
     * Close / Unregister a specified Database Connection from this Server.
     * Calls <code>close(identifier, false);</code>
     * @param identifier 
     */
    public void close(String identifier) {
        close(identifier, false);
    }
    
    
    /**
     * This closes a specific connection, and does NOT shut down the server.
     * @param identifier
     * @param force 
     */
    public void close(String identifier, boolean force) {
        
        LOGGER.info("Closing the connection for " + identifier);
        ConnectionManager.unregisterConnection(identifier);
        
    }

    /**
     * @return the silent
     */
    public boolean isSilent() {
        return silent;
    }

    /**
     * @param silent the silent to set
     */
    public void setSilent(boolean silent) {
        this.silent = silent;
    }
    
    public String getHostName() {
        if (hostName == null) {
            try {
                Enumeration<InetAddress> addrs = java.net.NetworkInterface.getByIndex(2).getInetAddresses();
                while (addrs.hasMoreElements()) {
                    InetAddress a = addrs.nextElement();
                    if (!a.getHostAddress().contains(":")) {
                        // don't show the ipv6 / imac address
                        hostName = a.getHostAddress();
                        break;
                    }
                }
            } catch (Exception ex) {
                LOGGER.error("Unable to determine hostName", ex);
            }
        }
        
        return hostName == null ? "N/A" : hostName;
    }
    
    @Override
    public String toString() {
        return "PrintServer@" + getHostName() + ":" + port;
    }

    
}
