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

import static com.ticktockdata.jasper.ReportConnectionManager.LOGGER;
import java.io.File;
import java.io.Serializable;
import java.security.InvalidParameterException;
import java.sql.Connection;
import java.sql.Driver;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * Class that contains the required information to make a database connection
 * and a method that returns a connection from that information.
 * @author JAM {javajoe@programmer.net}
 * @since Aug 23, 2018
 */
public class ConnectionInfo implements Serializable {

    /** Identifier can only be set via constructor - 
     * equals() is implemented solely on the Identifier */
    private String identifier;
    /** Driver path is static so we don't add the same resource 2x */
    private static final List<File> driverJars = new ArrayList<File>();
    private String driverClass = null;
    private String url = null;
    private String user = null;
    private String password = null;
    private Connection conn = null;
    
    private boolean initialized = false;

    
    /**
     * Default constructor with no params.  This is just a forward call:
     * {@code this(ReportConnectionManager.DEFAULT_CONNECTION_NAME);}
     */
    public ConnectionInfo() {
        this(ReportConnectionManager.DEFAULT_CONNECTION_NAME);
    }
    
    
    /**
     * Constructor with identifier parameter.
     * @param identifier a name for this connection.
     */
    public ConnectionInfo(String identifier) {
        super();
        this.identifier = identifier;
    }
    
    /**
     * The Identifier uniquely identifies this connection.  If using the
     * JasperServer with multiple database connections you must give each one
     * an identifier (name / string).  <b>Case sensitive!</b>
     * <p>Identifier is not settable except through constructor, may not change
     * Identifier on existing instance.
     * @return the identifier
     */
    public String getIdentifier() {
        return identifier;
    }

//    /**
//     * @param identifier the identifier to set
//     */
//    public void setIdentifier(String identifier) {
//        if (identifier == null) {
//            throw new InvalidParameterException("Identifier may not be null!");
//        }
//        this.identifier = identifier;
//    }
    
    /**
     * @return the driverClass
     */
    public String getDriverClass() {
        return driverClass;
    }

    /**
     * @param driverClass the driverClass to set
     */
    public void setDriverClass(String driverClass) {
        if (driverClass == null || driverClass.trim().isEmpty()) {
            throw new InvalidParameterException("Driver Class may not be null or empty!");
        }
        this.driverClass = driverClass;
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url the url to set
     */
    public void setUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            throw new InvalidParameterException("URL may not be null or empty!");
        }
        this.url = url;
    }

    /**
     * @return the user
     */
    public String getUser() {
        return user;
    }

    /**
     * @param user the user to set
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }
    
    
    /**
     * This is called to get the database connection.
     * Should only be called internally and by ReportConnectionManager.
 Synchronized for thread safety.
 <p>Shows an error message (JOptionPane) if no connection can be made.
     * @return null if no connection can be made
     */
    protected synchronized Connection getConnection() {
        
        if (conn != null) return conn;
        
        loadDriverIfNeeded();
        
        try {
            // create properties - allows creating if User and/or Password is null
            Properties props = new Properties();
            if (user != null) {
                props.put("user", user);
            }
            if (password != null) {
                props.put("password", password);
            }
            
            conn = java.sql.DriverManager.getConnection(url, props);
            
        } catch (Exception ex) {
            LOGGER.error("Failed to make a database connection with the information provided!", ex);
            JOptionPane.showMessageDialog(null, 
                    "Failed to make a database connection\n"
                            + "with the information provided!\n"
                            + "Driver = " + driverClass + "\nURL = " + url + "\n"
                            + "User = " + user + ", Password = " + password + "\n"
                            + "Error: " + ex.toString(), 
                    "Database Error:", JOptionPane.ERROR_MESSAGE);
            conn = null;
        }
        
        return conn;
    }

    /**
     * This closes the database connection, if it is not already closed.
     * Should only be called internally and by ReportConnectionManager.
 Synchronized for thread safety.
     */
    protected synchronized void closeConnection() {
        
        if (conn == null) {
            LOGGER.warn("Database connection for " + getIdentifier() + " is null, already closed");
            return;
        }
        try {
            conn.close();
            LOGGER.info("Successfully closed database connection for " + getIdentifier());
        } catch (Exception ex) {
            LOGGER.error("Failed to close database connection for " + getIdentifier(), ex);
        }
        
    }
    
//    private boolean validVar;
    /**
     * Returns true if the supplied information can be used to make a 
     * database connection.  Opens and closes a connection.
     * This does the same thing as getConnection except it does not display
     * an error.
     * @return true if the supplied info can make a database connection
     */
    public boolean isValidInfo(final boolean showMessages) {
        
        
        if (url == null || url.trim().isEmpty()) {
            if (showMessages) {
                JOptionPane.showMessageDialog(null, 
                        "The URL may not be null or empty!", "Error:"
                        , JOptionPane.WARNING_MESSAGE);
            }
            return false;
        }
        
        Connection conn = null;
        try {
            if (url == null || url.isEmpty()) {
                LOGGER.error("The url is empty or null: " + url);
                return false;
            }
            
            loadDriverIfNeeded();
            
            conn = java.sql.DriverManager.getConnection(url, user, password);
            
            if (conn == null) {
                LOGGER.warn("DriverManager.getConnection() "
                        + "returned null!\nurl: " + url + ", user: " + user 
                        + ", pwd: " + password);
                return false;
            }
            
            return true;
        } catch (Exception ex) {
            LOGGER.warn("Failed to make a connection!", ex);
            if (showMessages) {
                JOptionPane.showMessageDialog(null, "Insufficient or invalid "
                    + "parameters to make a database connection!"
                    + "\nDriver Class = " + getDriverClass() 
                    + "\nurl = " + getUrl() 
                    + "\nUser = " + getUser() + ", Password = " + getPassword() 
                    + "\nError: " + ex.toString(), 
                    "Connection Error:", JOptionPane.ERROR_MESSAGE);
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                if (!conn.isClosed()) conn.close();
                } catch (Exception x) {}
            }
        }
    }
    
    
    private void loadDriverIfNeeded() {
        
        if (!initialized) {
            if (driverClass != null) {
                try {
                    java.sql.DriverManager.registerDriver((Driver) Class.forName(driverClass).newInstance());
                } catch (Exception ex) {
                    LOGGER.error("Failed to register the jdbc Driver", ex);
                }
            }
            
            initialized = true;
        }
        
    }
    
    
    @Override
    public String toString() {
        return getIdentifier();
    }

    @Override
    public boolean equals(Object obj) {
        
        if (obj == null || !(obj instanceof ConnectionInfo)) return false;
        
        return (((ConnectionInfo)obj).getIdentifier().equals(getIdentifier()));
        
    }

    @Override
    public int hashCode() {
        return 31 + (getIdentifier().hashCode() * 7);
    }

    /**
     * @return the driverPath
     */
    public List<File> getDriverJars() {
        return driverJars;
    }

    /**
     * Convenience method, converts path string to File and calls
     * {@link setDriverJars(File...)}
     * @param paths the driverPath to set
     */
    public void setDriverJars(String... paths) {
        for (String jarPath : paths) {
            if (jarPath != null) {
                File jar = new File(jarPath);
                setDriverJars(jar);
            }
        }
        
    }
        
    /**
     * This adds the required driver jars, other other files, to the classpath,
     * so the database connection can be established.
     * @param jars
     */
    public void setDriverJars(File... jars) {
        for (File jar : jars) {
            if (jar != null) {
                if (driverJars.contains(jar)) {
                    LOGGER.info("The driver jar <" + jar.getAbsolutePath() + "> is already registered!");
                    continue;
                }
                if (jar.exists()) {
                    ReportConnectionManager.addToClasspath(jar);
                    driverJars.add(jar);
                } else {
                    JOptionPane.showMessageDialog(null, "The specified Driver Jar does not exist!\n" + jar.getAbsolutePath(), "Invalid Parameter!", JOptionPane.WARNING_MESSAGE);
                    LOGGER.warn("The specified Driver Jar does not exist:\n" + jar.getAbsolutePath());
                }
                
            }
        }

    }

    
}
