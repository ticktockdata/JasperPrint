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

package com.ticktockdata.jasper;

import static com.ticktockdata.jasper.ReportConnectionManager.DEFAULT_CONNECTION_NAME;
import java.io.File;
import java.lang.reflect.Method;
import java.security.InvalidParameterException;
import java.sql.Connection;
import java.util.*;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.apache.log4j.Logger;

/**
 * This class manages one or more database Connections used by JasperPrint.
 * Before using JasperPrint you must register a ConnectionInfo with this 
 * manager.  This Manager takes care of caching / closing connections during
 * runtime, but you should call <code>ReportConnectionManager.unregisterAllConnections()
 </code> before terminating your application to ensure that all database
 * connections are closed properly.
 * <p>The ReportConnectionManager does not save any preferences or remember the 
 connection parameters from one session to the next, that is the
 responsibility of your application.
 * @author JAM
 * @since Aug 23, 2018
 */
public class ReportConnectionManager {
    
    
    public static final Logger LOGGER = Logger.getLogger(ReportConnectionManager.class);
    
    /**
     * Name (identifier) of the connection, unless specified otherwise 
     */
    public static final String DEFAULT_CONNECTION_NAME = "default";
    
    private static Map<String, ConnectionInfo> allConnections = new HashMap<String, ConnectionInfo>();
    
    /**
     * This gets a Connection from the ConnectionInfo named {@link DEFAULT_CONNECTION_NAME}.
     * <p>This call is just forwarded:
     * <code>ReportConnectionManager.getReportConnection(DEFUALT_CONNECTION_NAME)</code>
     * @return a java.sql.Connection
     */
    public static Connection getReportConnection() {
        
        return getReportConnection(DEFAULT_CONNECTION_NAME);
    }
    
    /**
     * Gets a Connection from the ConnectionInfo specified by identifier (name).
     * <p><b>Warning:</b>  You should NOT attempt to use this connection in 
     * your application.  This connection is used by JasperPrint and will close
     * itself after a certain time length since the last call to this method.  
     * It opens a new Connection when called again after auto-closing, but in
     * application usage it may leave you hanging with a null Connection.
     * @param id the Identifier (name) for the desired connection
     * @return a java.sql.Connection
     */
    public static Connection getReportConnection(String id) {
        
        ConnectionInfo info = allConnections.get(id);
        if (info == null) {
            throw new InvalidParameterException("No Connection named " + id + " was found!");
        }
        return info.getConnection();
    }
    
    
    
    public static Map<String, ConnectionInfo> getAllConnections() {
        return allConnections;
    }
    
    private static boolean regSuccess = false;
    
    /**
     * This is used to register a ConnectionInfo, which supplies the database
     * connection used by JasperPrint.  Multiple connections can be registered
     * by providing them with different names (identifier).
     * <p>This method is guaranteed to run on the EventDispatchThread, it does
     * a check and uses SwingUtilities.InvokeAndWait to run method on EDT if 
     * current thread is not the EDT.  This is required for the JOptionPane 
     * messages to display correctly.
     * @throws InvalidParameterException if no database connection can be made
     * with <b>info</b>.
     * @param info a <code>com.ticktockdata.jasper.ConnectionInfo</code> (provides database connection)
     * @return true if successfully registered, false otherwise
     */
    public static boolean registerConnection(final ConnectionInfo info, final boolean showMessages) {
        
        // becuase of JOptionPane messages that might be displayed
        // we want to be sure this is run on EDT.
        if (!SwingUtilities.isEventDispatchThread()) {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        regSuccess = registerConnection(info, showMessages);
                    }
                });
                return regSuccess;
            } catch (Exception ex) {
                LOGGER.error("Error registering connection!", ex);
                return false;
            }
        }
        
        
        // make sure it is valid before we register
        if (!info.isValidInfo(showMessages)) {
            throw new InvalidParameterException("ConnectionInfo data failed "
                    + "to establish a database connection!");
        }
        
        // check if a connection of this name already exists
        if (allConnections.containsKey(info.getIdentifier())) {
            if (showMessages) {
                int rsp = 
                JOptionPane.showConfirmDialog(null, "A connection of this name already exists.\n"
                        + "Do you want to replace the existing connection?", "Confirm:", JOptionPane.YES_NO_OPTION);
                if (rsp == JOptionPane.NO_OPTION) {
                    return false;
                }
            }
            LOGGER.info("Removing existing connection: " + info.getIdentifier());
            unregisterConnection(info.getIdentifier());
        }
        
        allConnections.put(info.getIdentifier(), info);
        
        LOGGER.info("Registerd a connection as " + info.getIdentifier() + 
                " with Driver " + info.getDriverClass() + " and URL " + 
                info.getUrl());
        return true;
    }
    
    
    /**
     * Used to Un-Register a connection based on the Identifier.
     * Logs a warning but does not crash if id not found.
     * @param id Identifier of the connection to unregister.
     */
    public static void unregisterConnection(String id) {
        
        if (!allConnections.containsKey(id)) {
            LOGGER.debug("No connection named " + id + " found, did not unregister");
            return;
        }

        allConnections.get(id).closeConnection();
        allConnections.remove(id);
        LOGGER.info("Unregistered Connection " + id);

    }
    
    /**
     * Helper method for shutdown.  Closes all connections.  This is called by 
     * {@link com.ticktockdata.jasper.ReportManager#shutdown(boolean)}, so programmer should call that instead.
     */
    protected static void unregisterAllConnections() {
        
        String[] keys = new String[allConnections.size()];
        int i = 0;
        for (String k : allConnections.keySet()) {
            keys[i] = k;
            i++;
            LOGGER.trace("key = " + k);
        }
        for (i = keys.length-1; i >= 0; i--) {
            unregisterConnection(keys[i]);
        }
    }
    
    
    public static void addToClasspath(File jarFile) {

        try {
            if (jarFile == null || !jarFile.exists()) {
                throw new InvalidParameterException("File " + jarFile + " does not exist!");
            }
            
            // original method, works
//            URL url = jarFile.toURI().toURL();
//            Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
//            method.setAccessible(true);
//            method.invoke(ClassLoader.getSystemClassLoader(), url);
//            LOGGER.info("Added to classpath: " + url);

            
            // This is another method that works.
            // Should work on java 11, except it complains about accissiblity
            // This should be preferrable to previous method, as it does not 
            // depend on the system classloader being a URLClassLoader, but is not just a plain ClassLoader either
            ClassLoader classLoader = ClassLoader.getSystemClassLoader();
            
            Method method = classLoader.getClass()
            .getDeclaredMethod("appendToClassPathForInstrumentation", String.class);
            method.setAccessible(true);
            method.invoke(classLoader, jarFile.getAbsolutePath());

            
        } catch (Throwable ex) {
            LOGGER.error("Failed to add <" + jarFile + "> to the classpath\n" +ex.toString(), ex);
        }
    }
    
}
