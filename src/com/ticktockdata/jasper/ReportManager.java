/*
 * Copyright (C) 2018, Joseph A Miller
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

import com.ticktockdata.jasper.prompts.PromptComponentFactory;
import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.io.File;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import org.apache.log4j.Logger;

/**
 * This is a static class for obtaining and running reports.
 * <p>
 * A cache of compiled reports are kept, which can be dumped or individual
 * reports removed using the clearReportCache() and removeFromCache(String
 * reportPath) methods.  It should, however, not be necessary to clear cache
 * manually as the file's lastModified is stored and checked on each call, so
 * the Manager will automatically detect modified files and reload them.
 * <p>
 * To do actual execution (view / print / preview) of reports use the
 * executeReport(JasperReportImpl report) method, which passes the job to a
 * SingleThreadExecutor and returns immediately.
 * <p>
 * The Jasper Report object that the programmer will work with is a
 * {@link JasperReportImpl}, which is a Runnable implementation of a
 * {@link net.sf.jasperreports.engine.JasperReport}. Get an instance of a
 * JasperReportImpl by calling ReportManager.getReport(String full_path_to_file)
 * <p>
 * Before a report can be executed a Database Connection must be established,
 * use {@link ReportConnectionManager} to add connection(s).
 *
 * @author JAM {javajoe@programmer.net}
 * @since v0.0, Aug 24, 2018
 */
public class ReportManager {

    /**
     * log4j logger
     */
    public static final Logger logger = Logger.getLogger(ReportManager.class);

    /**
     * SingleThreadExecutorService that runs the reports
     */
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();
    
    /**
     * Cache of the compiled reports, prevents need to re-compile each run
     */
    private static final Map<String, JasperReport> REPORT_CACHE = new HashMap<String, JasperReport>();
    
    /**
     * Cache of the mod date of reports - used to auto-reload reports on modification
     */
    private static final Map<String, Long> REPORT_MODIFIED = new HashMap<String, Long>();
    
    /**
     * Cache of default parameters that are to be loaded to every report before
     * running
     */
    private static final Map<String, Map<String, Object>> DEFAULT_PARAMS = new HashMap<String, Map<String, Object>>();

    /**
     * Returns a compiled JasperReport. Returns cached value if available,
     * otherwise compiles, caches and returns report.
     * <p>
     * This should probably not be called by implementing program. Create a new
     * {@link JasperReportImpl} by calling {@link getReport(String)} instead,
     * which calls this method to load the report.
     *
     * @param reportPath string with full path and name of desired report.
     * @return a {@link net.sf.jasperreports.engine.JasperReport}, or null if
     * report doesn't exist or can't be compiled
     * @throws InvalidParameterException if specified reportPath doesn't exist
     */
    protected static JasperReport getJasperReport(String reportPath) {

        logger.trace("getReport called for file: " + reportPath);

        if (reportPath == null || reportPath.trim().isEmpty()) {
            logger.warn("A Null or empty reportPath was given to getJasperReport: " + reportPath);
            throw new InvalidParameterException("The specified reportFile is null or empty!");
        }
        
        // this throws InvalidParameterException if not valid
        File file = getReportFile(reportPath);

        // if report is cached then return it
        if (REPORT_CACHE.containsKey(reportPath)) {

            // check that it hasn't been modified!
            if (file.lastModified() != REPORT_MODIFIED.get(reportPath)) {
                // remove from cache and continue to reload
                logger.info("Report file has been modified, reloading!");
                // remove the parameters for this report
                PromptComponentFactory.clearPromptComponentCache(REPORT_CACHE.get(reportPath).getParameters());
                removeFromCache(reportPath);
            } else {
                logger.debug("Returning cached report: " + reportPath);
                return REPORT_CACHE.get(reportPath);
            }
        }

        // file exists but is not compiled, so compile it!
        try {
            JasperReport report = JasperCompileManager.compileReport(file.getAbsolutePath());
            REPORT_CACHE.put(reportPath, report);
            REPORT_MODIFIED.put(reportPath, file.lastModified());
            logger.debug("Compiled report and added to cache!  " + report.getName());
            return report;
        } catch (JRException ex) {
            logger.error("Error while compiling report: " + reportPath, ex);
            return null;
        }

    }

    /**
     * This is a convenience 'getter' that returns a new JasperReportImpl.
     * <p>
     * It is recommended to always use this to create a new JasperReportImpl
     * object, future updates may have path parsing, etc. done in here.
     *
     * @since v0.0
     * @param reportPath must be valid when called as: new
     * java.io.File(reportPath)
     * @return
     */
    public static JasperReportImpl getReport(String reportPath) {
        if (getReportFile(reportPath) != null) {
            return new JasperReportImpl(reportPath);
        }
        return null;
    }
    
    
    /**
     * Gets the Report File, uses ClassLoader to find.
     * @param reportPath needs to be a valid .jrxml file
     * @return a File object, or null if report does not exist
     */
    public static File getReportFile(String reportPath) {
        
        if (reportPath == null || !reportPath.toLowerCase().trim().endsWith(".jrxml")) {
            return null;
        }
        
        java.io.File file;
        
        try {
            // get file as resource
            java.net.URL url = ClassLoader.getSystemResource(reportPath);
            logger.trace("getReportFile() called, URL = " + url); // null
            
            if (url == null) {
                logger.debug("Creating file from report path: " + reportPath);
                file = new java.io.File(reportPath);    //url.toURI());    //reportPath);
                if (!file.exists()) {
                    logger.info("Attempting to find report in reports folder");
                    file = new java.io.File("reports" + File.separator + reportPath);
                }
            } else {
                logger.debug("Creating file from url: " + url.toURI());
                file = new java.io.File(url.toURI());
            }
        } catch (Exception ex) {
            file = null;
//            logger.error("The report file does not exist! " + reportPath);
//            removeFromCache(reportPath);
        }
        

        // if file doesn't exist then throw exception
        if (file == null || !file.exists()) {
            logger.warn("The specified reportFile does not exist: " + reportPath);
            // remove from cache if it is in there
            removeFromCache(reportPath);
            return null;
//            throw new InvalidParameterException("The file specified: <"
//                    + reportPath + "> does not exist!");
        }
        
        // return the report file
        return file;
        
    }
    
    
    public static List<File> listReportsInDir(String reportDir) {

        List<File> reports = new ArrayList<>();
        File dir = new File(reportDir);
        if (dir.exists() && dir.isDirectory()) {
            for (File f : dir.listFiles()) {
                if (f.getName().toLowerCase().endsWith(".jrxml")) {
                    reports.add(f);
                }
            }
        }
        return reports;
    }

    
    /**
     * Method that returns list of available printer names, sorted.
     *
     * @return
     */
    public static List<String> getPrinterList() {

        PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
        List<String> printers = new ArrayList<String>(services.length);
        for (PrintService ps : services) {
            printers.add(ps.getName());

        }

        Collections.sort(printers, new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                return o1.toString().toLowerCase().compareTo(o2.toString().toLowerCase());
            }
        });

        return printers;
    }

    /**
     * Method to retrieve a PrintService by the printer name.
     *
     * @param printerName
     * @return
     */
    public static PrintService getPrintService(String printerName) {

        for (PrintService ps : PrintServiceLookup.lookupPrintServices(null, null)) {
            if (ps.getName().equals(printerName)) {
                return ps;
            }
        }
        return null;
    }

    /**
     * Returns the system's default print service, or 1st in list alphabetically
     * if no default set.
     *
     * @return null if no print service available
     */
    public static PrintService getDefaultPrintService() {

        PrintService ps = PrintServiceLookup.lookupDefaultPrintService();

        if (ps != null) {
            return ps;
        }

        // if default print service not found, search for ANY print service
        List<String> printers = getPrinterList();
        if (printers.size() > 0) {
            return getPrintService(printers.get(0));
        }

        return null;    // no print service to be found

    }
    
    
    /**
     * This should always be used to run (print / preview / export) a report. A
     * SingleThreadExecutorService is used to run the reports, so they will
     * always be executed in sequence, one at a time.
     * <p>
     * This method loads the default parameters if they are set.
     *
     * @param report
     */
    public static void executeReport(JasperReportImpl report) {

        try {
            // load the default parameters!
            Map<String, Object> defaultParams = DEFAULT_PARAMS.get(report.getConnectionID());
            logger.debug("defaultParams = " + defaultParams);
            // if failed for this id and id is not default, then try default
            if (defaultParams == null && !ReportConnectionManager.DEFAULT_CONNECTION_NAME.equals(report.getConnectionID())) {
                defaultParams = DEFAULT_PARAMS.get(ReportConnectionManager.DEFAULT_CONNECTION_NAME);
            }
            if (defaultParams != null) {
                report.setParams(defaultParams);
            }
            
            try {
                // set the dir containing the report
                report.setParameter("SUBREPORT_DIR", new java.io.File(report.getReportPath()).getParentFile().getAbsolutePath() + File.separator);
            } catch (Exception ex) {
                logger.error("Failed to set SUBREPORT_DIR parameter!");
            }
            
            logger.debug("The Report's print action is: " + report.getPrintAction());
            EXECUTOR_SERVICE.execute(report);
            report.firePrintStatusChanged(PrintStatusEvent.StatusCode.QUEUED);
//            EXECUTOR_SERVICE.awaitTermination(10, TimeUnit.SECONDS);
            logger.debug("Added report " + report.getReportName() + " to SingleThreadExecutorService");
        } catch (Exception ex) {
            logger.error("Failed to execute the report: " + report.getReportName(), ex);
            removeFromCache(report.getReportPath());
        }

    }

    /**
     * Clears cached (compiled) reports from memory so they will be reloaded
     * from file on next call.  Any error handling code that catches a fault
     * with a report should remove that report from the cache via a call to
     * {@link removeFromCache(String)}, but it should not be necessary to call
     * this method otherwise.  When loaded the report's lastModified value
     * is cached as well and is always checked when getJasperReport(String reportPath)
     * is called.  If file has been modified it is cleared and reloaded.
     * @return count of reports cleared from cache
     */
    public static int clearReportCache() {
        int count = REPORT_CACHE.size();
        if (count <= 0) {
            logger.info("clearReportCache called, there were no cached reports to clear");
        } else {
            REPORT_CACHE.clear();
            logger.info("Cleared cache, there were " + count + " reports cached.");
        }
        // have the PromptComponentFactory clear it's cached prompts
        PromptComponentFactory.clearPromptComponentCache();
        return count;
    }

    /**
     * Removes a single report from the Report Cache.  This is called by 
     * clearReportCache(), and should be the only way to remove a report
     * from the cache.
     * @param reportPath
     */
    public static void removeFromCache(String reportPath) {

        REPORT_CACHE.remove(reportPath);
        REPORT_MODIFIED.remove(reportPath);
        logger.info("Removed from report cache: " + reportPath);

    }

    /**
     * This is just a forward call to
     * {@link setDefaultReportParameters(String, Map)} with String (Identifier)
     * set as &quot;default&quot;
     *
     * @param params
     */
    public static void setDefaultReportParameters(Map<String, Object> params) {
        setDefaultReportParameters(ReportConnectionManager.DEFAULT_CONNECTION_NAME, params);
    }

    /**
     * Sets the default parameters for a given report connection. Each report
     * will have these parameters loaded before being executed, typically used
     * for adding standardized parameters like Company Name & Address,
     * Application Name, Company Logo, etc.
     * <p>
     * If parameters for this identifier already exist, it will replace them as
     * a whole, not individually.
     * <p>
     * When applied to a report it first checks for parameters that match the
     * identifier (the database identifier), and if not found and the identifier
     * is not "default", then it will search for the "default" parameters and
     * load those if they exist.
     *
     * @param identifier should match with connection's identifier
     * @param params a map of parameters
     */
    public static void setDefaultReportParameters(String identifier, Map<String, Object> params) {

        if (DEFAULT_PARAMS.containsKey(identifier)) {
            logger.info("Replacing the existing parameters for connection \"" + identifier + "\"");
        } else {
            logger.debug("Adding default parameters for connection \"" + identifier + "\"");
        }

        DEFAULT_PARAMS.put(identifier, params);
        
        for (String s : params.keySet()) {
            logger.trace("Set a Default Parameter for \"" + identifier + "\": " + s + "=" + params.get(s));
        }
        
    }
    
    
    /**
     * Adds a parameter for the connection named "default"
     * 
     * @param key
     * @param value 
     */
    public static void addDefaultReportParameter(String key, Object value) {
        addDefaultReportParameter(ReportConnectionManager.DEFAULT_CONNECTION_NAME, key, value);
    }
    
    
    
    public static void addDefaultReportParameter(String identifier, String key, Object value) {
        
        Map<String, Object> map = DEFAULT_PARAMS.getOrDefault(identifier, new HashMap<>());
        map.put(key, value);
        DEFAULT_PARAMS.put(identifier, map);
        
    }
    
    
    /**
     * Another fancy threading method to set focus to desired component
     * (dialog).
     * <br>Requires that JDialog has an <b>OK</b> button.
     * <p>This copied from DesktopApp2 com.ticktockdata.utils.SwingUtils
     * @param title the title of the JOptionPane, or other Window, that we want
     * to search for a button
     * @param nameOrText regexp match text to specify the button (text) or control (name) to focus to
     */
    public static void focusToDialog(final String title, String nameOrText) {
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    int ct = 0;
                    // give dialog time to appear
                    Thread.sleep(100);
                    while (++ct < 25) {
                        Window[] wins = JFrame.getWindows();
                        // enumerate all windows
                        for (final Window w : wins) {
                            // and check if it is a visible JDialog
                            if (w instanceof JDialog && w.isVisible()) {
                                // if title matches, then this is the one!
                                if (((JDialog) w).getTitle().equals(title)) {
                                    // get the OK button
                                    for (Component c : ((JDialog) w).getComponents()) {
                                        // if (c instanceof JButton && (((JButton) c).getText().equalsIgnoreCase("ok") || ((JButton) c).getText().equalsIgnoreCase("yes"))) {
                                        final JComponent ok = findSubComponent(((JDialog) w).getRootPane(), nameOrText);
                                        // if OK button found and it does not have focus
                                        if (ok != null && !ok.hasFocus()) {
                                            // do focus request on EDT
                                            SwingUtilities.invokeLater(() -> {
                                                if (!ok.requestFocusInWindow()) {
                                                    ok.requestFocus();
                                                }
                                            });
                                        }
                                        // }
                                    }

                                    // dialog found, so return
                                    return;
                                }
                            }
                        }
                        // if not found, wait 50 ms and try again
                        Thread.sleep(50);
                    }
                } catch (InterruptedException ex) {
                }

            }
        };
        // start the thread that looks for dialog
        t.start();
    }

    /**
     * Helper method to find a button (by button text) or JComponent (by Name) 
     * in a container.
     * <br>This is a recursive method.
     *
     * @param d Container, like JRootPane
     * @param nameOrText regexp text match, like "exit" or "ok|yes", either button text or component name
     * @return JComponent, if correct one found, or null otherwise.
     */
    private static JComponent findSubComponent(Container d, String nameOrText) {
        
        // prevent null pointer exception
        if (nameOrText == null || nameOrText.trim().isEmpty()) return null;
        
        for (Component c : d.getComponents()) {
            if (c instanceof JButton && ((JButton)c).getText() != null && ((JButton) c).getText().toLowerCase().matches(nameOrText.toLowerCase())) {
                return (JButton)c;
            } else if (c instanceof JComponent && c.getName() != null && c.getName().toLowerCase().matches(nameOrText.toLowerCase())) {
                return (JComponent)c;
            } else if (c instanceof Container) {
                JComponent x = findSubComponent((Container) c, nameOrText);
                if (x != null) {
                    return x;
                }
            }
        }

        return null;

    }
    
    
    /**
     * Programmer should always call this before exiting, preferably with force
     * = false, which causes this to wait until all reports have been executed.
     * Shuts down the SingleThreadExecutorService that actually executes the
     * reports.
     * <p>
     * This also calls {@link ConnectionManager.unregisterAllConnections()} to
     * shut down the database connections.
     *
     * @param force
     */
    public static void shutdown(boolean force) {

        logger.info("ReportManager.shutdown(force=" + force + ") was called");
        try {
            if (force) {
                EXECUTOR_SERVICE.shutdownNow();
            } else {
                EXECUTOR_SERVICE.shutdown();
                EXECUTOR_SERVICE.awaitTermination(10, TimeUnit.MINUTES);
            }

            ReportConnectionManager.unregisterAllConnections();

        } catch (Exception ex) {
            logger.error("Error in ReportManager.shutdown()", ex);
        }

    }

}
