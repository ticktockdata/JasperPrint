/*
 * Copyright (C) 2018-2022 Joseph A Miller
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

import com.ticktockdata.jasper.PrintExecutor.Action;
import com.ticktockdata.jasper.PrintStatusEvent.StatusCode;
import com.ticktockdata.jasper.gui.PromptDialog;
import com.ticktockdata.jasperserver.ServerManager;
import java.awt.Component;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import net.sf.jasperreports.engine.JasperReport;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractButton;
import javax.swing.SwingUtilities;
import org.apache.log4j.Level;
import static com.ticktockdata.jasper.ReportManager.logger;
import javax.swing.JOptionPane;

/**
 * This is the primary class (wrapper) for running a JasperReport. You (the
 * programmer) should generally work with this rather than
 * {@link net.sf.jasperreports.engine.JasperReport}
 *
 * @author JAM
 * @since Aug 29, 2018
 */
public class JasperReportImpl implements Runnable {

//    private JasperReport jasperReport = null;
    private String reportName = null;
    private String reportPath = null;
    private Component parent = null;
    /**
     * Use AbstractButton rather then JButton, so it can be a JMenuItem as well
     */
    private AbstractButton printButton = null;
    /**
     * default action is preview
     */
    private Action printAction = Action.PREVIEW;
    private PrintExecutor printExecutor = new PreviewAction(this);

    private HashMap<String, Object> params = new HashMap<String, Object>();
    private boolean promptForParameters = true;
    private int progressDelay = 5;
    private String connectionID = ServerManager.DEFAULT_IDENTIFIER;

    /**
     * This is used to make JasperReport accessible, but is only valid during
     * filling of report
     */
    private JasperReport currentJasperReport = null;
    /**
     * fields required by Action
     */
    private String printer = null;
    private int copies = 1;
    private boolean collate = true;
    private PrintAction.Duplex duplex = PrintAction.Duplex.OFF;
    private boolean showDialog = true;
    private String exportFilePath = null;
    private boolean overwriteExportFile = false;

    /**
     * Each prompt filler in list is called after compiling, but before
     * executing. There is no guarantee in what order the fillers will be
     * called!
     */
    private List<PrintPromptFiller> promptFillers = new ArrayList<PrintPromptFiller>();
    /**
     * Status listeners get notified of events that occur during
     */
    private List<PrintStatusListener> statusListeners = new ArrayList<PrintStatusListener>();

    /**
     * fields required by ExportAction
     */
    /**
     * Used by executeReport so we know if the report successfully filled or
     * not.
     */
    private boolean processing = false;    // becomes true if execute() is called, false when finished = true
    private boolean error = false;      // becomes true if execution error
    private boolean canceled = false;   // becomes true if execution canceled
    private StatusCode status = StatusCode.UNDEFINED;
    private FillMonitor monitor = null;

    /**
     * Basic no-args constructor.
     */
    public JasperReportImpl() {
    }

    /**
     * @param reportPath full file path and name of .jrxml file.
     */
    public JasperReportImpl(String reportPath) {
        this();
        setReportPath(reportPath);
    }

    /**
     * The JasperReport object is stored and managed by the Report Manager. It
     * is not cached within this JasperReportImpl class, but this method can be
     * called whenever the JasperReport is needed.
     * <p>
     * This should be called only 1x per use (method), but should NOT be stored
     * (cached) by any other class.
     * <p>
     * In order for the PromptDialog to access the JasperReport it is
     * temporarily set to the jReport variable during running of the report.
     *
     * @return the jasperReport
     */
    public JasperReport getJasperReport() {
        if (currentJasperReport != null) {
            return currentJasperReport;
        } else {
            return ReportManager.getJasperReport(reportPath);
        }
    }

    /**
     * This execute method used by buttons and print menu items. It sets certain
     * parameters before executing.
     *
     * @param prefs
     */
    public void execute(PrintPreferences prefs) {

        logger.trace("Calling execute w/prefs = " + prefs);
        if (prefs != null) {
            this.setCopies(prefs.getCopies());
            this.setShowDialog(prefs.isShowPrintDialog());
            this.setPrinter(prefs.getPrinterName());
            this.setPrintAction(prefs.getPrintAction());
            logger.debug("execute, prefs set - PrintAction = " + this.getPrintAction());
        } else {
            logger.warn("Execute called with null prefs");
        }
        execute();
    }

    /**
     * Call this to execute the report using the currently set
     * {@link PrintExecutor} Must fill all parameters before calling this! (use
     * {@link execute(PrintPreferences)})
     * <p>
     * This is NOT the actual filling / executing of report method. This calls
     * {@link ReportManager}.executeReport(JasperReportImpl), which queues the
     * job to a {@link java.util.concurrent.ThreadPoolExecutor}
     * (Executors.newSingleThreadExecutor()) which in turn calls the
     * {@link run()} method of this class.
     * <p>
     */
    public void execute() {
        logger.debug("execute() called, the printer is: " + printer + ", action = " + this.getPrintAction());

        status = StatusCode.UNDEFINED;
        error = false;
        canceled = false;
        processing = true;

        setPrintButtonEnabled(false);
        ReportManager.executeReport(this);
        setPrintButtonEnabled(true);
    }

    /**
     * Calls {@link execute()} on a secondary thread and waits for it to
     * complete.
     * <p>
     * <b>Warning: </b> This should not be called from the EventDispatchThread!
     *
     * @return
     */
    public boolean executeAndWait() {

        boolean success = false;

        try {

            // ensure that execute doesn't happen on EDT
            new Thread(() -> {
                execute();
            }).start();

            Thread.sleep(50);

            while (isProcessing()) {
                Thread.sleep(25);
            }

            success = !(isCanceled() || isError());

        } catch (Throwable ex) {
            logger.error("Failed to convert to PDF: " + ex.getLocalizedMessage(), ex);
        }

        return success;

    }

    /**
     *
     * @return the reportPath
     */
    public String getReportPath() {
        return reportPath;
    }

    /**
     * This should be the full path to the report (.jrxml) file.
     *
     * @param reportPath the reportPath to set
     */
    public void setReportPath(String reportPath) {
        this.reportPath = reportPath;
    }

    /**
     * @return the parent
     */
    public Component getParent() {
        if (parent == null && printButton != null) {
            parent = printButton.getTopLevelAncestor();
        }
        return parent;
    }

    /**
     * Parent is the component used to center the Report Parameter and Progress
     * dialogs to. Defaults to null if not set.
     *
     * @param parent the parent to set
     */
    public void setParent(Component parent) {
        this.parent = parent;
    }

    /**
     * @return the printButton
     */
    public AbstractButton getPrintButton() {
        return printButton;
    }

    /**
     * If a printButton is set then it will set the printButton color and
     * disable printButton during compiling of the report.
     *
     * @param printButton the printButton to set
     */
    public void setPrintButton(AbstractButton printButton) {
        this.printButton = printButton;
    }

    private void setPrintButtonEnabled(boolean enabled) {

        if (printButton == null) {
            return;
        }

        if (printButton.isEnabled() && enabled == false) {
            // disable it!
            printButton.setEnabled(false);
        } else if (enabled == true) {
            // enable it!
            printButton.setEnabled(true);
        }

        logger.trace("set print button enabled: " + printButton.isEnabled());
    }


    /**
     * public access to executor required so outside classes can cancel the
     * execution if required.
     *
     * @return the printExecutor
     */
    public PrintExecutor getPrintExecutor() {
        return printExecutor;
    }

    /**
     * The PrintExecutor is the class that does the actual printing / preview /
     * export. The default is PREVIEW if not modified. Changing this will also
     * update the printAction. This method is private, and is set by the
     * setPrintAction class.
     *
     * @param printExecutor the printExecutor to set
     */
    private void setPrintExecutor(PrintExecutor printExecutor) {
        if (printExecutor == null || !printExecutor.isValid()) {
            throw new IllegalArgumentException("The PrintAction supplied is not valid!");
        }
        this.printExecutor = printExecutor;
        this.printAction = printExecutor.getAction();
    }

    public Action getPrintAction() {
        return printAction;
    }

    /**
     * This method sets the correct PrintExecutor based on the action entered
     *
     * @param action
     */
    public void setPrintAction(Action action) {

        if (printExecutor != null && printAction != null && printAction.equals(action)) {
            logger.info("The print action is already set, don't set a new one!");
            return;
        }
        logger.debug("Setting print action to " + action);
        // this method sets the correct PrintExecutor per the 
        switch (action) {
            case PRINT:
                setPrintExecutor(new PrintAction(this));
                break;
            case EXPORT_TO_CSV:
                setPrintExecutor(new ExportToCSVAction(this));
                break;
            case EXPORT_TO_PDF:
                setPrintExecutor(new ExportToPDFAction(this));
                break;
            case EXPORT_TO_XLS:
                setPrintExecutor(new ExportToXLSAction(this));
                break;
            default:
                // PREVIEW
                setPrintExecutor(new PreviewAction(this));
        }

    }

    /**
     * @return the params
     */
    public HashMap<String, Object> getParams() {
        return params;
    }

    /**
     * Clears all parameters
     */
    public void clearParams() {
        this.params.clear();
    }

    /**
     * This does NOT clear existing parameters, but overwrites them if the
     * parameter already exists.
     *
     * @param params the params to set
     */
    public void setParams(Map<String, Object> params) {
        if (params != null) {
            this.params.putAll(params);
        }

    }

    /**
     * Used to set a single parameter. This will silently replace an existing
     * parameter of the same name.
     *
     * @param name
     * @param value
     */
    public void setParameter(String name, Object value) {

        this.params.put(name, value);

    }

    /**
     * @return the promptForParameters
     */
    public boolean isPromptForParameters() {
        return promptForParameters;
    }

    /**
     * Default is true if not set. If set to false then it will never show the
     * parameter prompt dialog.. This is only set per Report, the PrintButton
     * does not control this!
     *
     * @param promptForParameters the promptForParameters to set
     */
    public void setPromptForParameters(boolean promptForParameters) {
        this.promptForParameters = promptForParameters;
    }

    /**
     * Is true from the time {@link execute()} is called until the report is
     * either complete, canceled or error.
     * <p>
     * If your code waits on report to complete by checking this property, then
     * ensure that execution was successful by checking that
     * {@link isCanceled()} and {@link isError()} both return false, or
     * {@link getStatus()} returns {@link StatusCode#COMPLETE}
     *
     * @return
     */
    public boolean isProcessing() {
        return processing;
    }

    /**
     *
     * @return true if the report execution was canceled for any reason
     */
    public boolean isCanceled() {
        return canceled;
    }

    /**
     *
     * @return true if executing the report invokes an error
     */
    public boolean isError() {
        return error;
    }

    /**
     * Returns the last fired status of execution, or UNDEFINED if called before
     * first execute() or between execute() and {@link StatusCode#QUEUED}
     *
     * @return
     */
    public StatusCode getStatus() {
        return status;
    }

    /**
     * This is called by the Cancel action on PromptDialog - will not cancel
     * report once in fill mode, only in set parameters stage.
     *
     * @param cancel
     */
    public void setCanceled(boolean cancel) {
        canceled = cancel;
        if (cancel == true) {
            firePrintStatusChanged(StatusCode.CANCELED);
            logger.debug("The report execution was cancelled by user.");
            if (getPrintButton() != null) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        // enable the button again!
                        getPrintButton().setEnabled(true);
                    }
                });
            }
        }
    }

    /**
     * @return the number of seconds to wait before showing a cancelable
     *         progress dialog.
     */
    public int getProgressDelay() {
        return progressDelay;
    }

    /**
     * Number of seconds to wait before showing a cancelable progress dialog.
     * Setting to 0 will show immediately, -1 will never show.
     *
     * @param progressDelay number of seconds to wait
     */
    public void setProgressDelay(int progressDelay) {
        this.progressDelay = progressDelay;
    }

    /**
     * The name of the report as specified by the <b>name</b> property of the
     * .jrxml file. This method will parse the file and extract name if needed.
     * <p>
     * The File is obtained from the reportPath parameter by calling
     * {@link ReportManager}.getReportFile(reportPath)
     *
     * @return the name of the report, or null if not exist
     */
    public String getReportName() {

        if (reportName == null) {
            long startTime = System.currentTimeMillis();
            if (reportPath != null) {

                File f = ReportManager.getReportFile(reportPath);
                if (f == null) {
                    return null;
                }
                //FileInputStream fis = null;
                logger.debug("Extracting Report Name by parsing text file");
                StringBuilder sb = new StringBuilder();
                try (FileInputStream fis = new FileInputStream(f)) {
                    while (fis.available() > 0) {
                        sb.append((char) fis.read());
                        if (sb.toString().toLowerCase().endsWith("name=\"")) {
                            sb = new StringBuilder();
                            while (true) {
                                char c = (char) fis.read();
                                if (c == '"') {
                                    reportName = sb.toString().trim().replaceAll("&apos;", "'");
                                    logger.trace("Name extracted in " + (System.currentTimeMillis() - startTime));
                                    return reportName;
                                } else {
                                    sb.append(c);
                                }
                            }
                        }
                    }

                } catch (Exception ex) {
                    logger.warn("Failed to extract report name: " + ex.getLocalizedMessage(), ex);
                }

                // if we can't extract the name from file then use file name
                reportName = f.getName();
                return reportName;
            }

        }

        return reportName;

    }

    /**
     * Each prompt filler in list is called after compiling, but before
     * executing. There is no guarantee in what order the fillers will be
     * called!
     * <p>
     * A filler can cancel the report execution by returning false, it must
     * return true in order for report to continue execution.
     *
     * @param filler
     */
    public void addPrintPromptFiller(PrintPromptFiller filler) {
        promptFillers.add(filler);
    }

    public void removePrintPromptFiller(PrintPromptFiller filler) {
        promptFillers.remove(filler);
    }

    /**
     * Adds a listener that notifies with a {@link PrintStatusEvent} when a
     * change of status occurs.
     *
     * @param listener
     * @see PrintStatusEvent
     */
    public void addPrintStatusListener(PrintStatusListener listener) {
        statusListeners.add(listener);
    }

    public void removePrintStatusListener(PrintStatusListener listener) {
        statusListeners.remove(listener);
    }

    public void firePrintStatusChanged(StatusCode status) {

        this.status = status;

        // change status of this report
        switch (status) {
            case ERROR:
                error = true;
                processing = false;
                break;
            case CANCELED:
                canceled = true;
                processing = false;
                break;
            case COMPLETE:
                processing = false;
                break;
        }

        // now fire listeners and give the listners a chance to cancel, etc.
        for (PrintStatusListener listener : statusListeners) {
            listener.statusChanged(new PrintStatusEvent(this, status));
        }
    }

    /**
     * This is called by the ReportManager's REPORT_EXECUTOR, which is a
     * SingleThreadExecutor. Invokes in the following sequence:
     * <ol>
     * <li>Gets the compiled JasperReport from ReportManager (which caches them)
     * <li>Invokes {@link PrintPromptFiller} to fill report with parameters
     * (including default ones)
     * <li>Invokes a {@link com.ticktockdata.jasper.gui.PromptDialog} if
     * isPromptForParameters() == true
     * <li>Starts a {@link FillMonitor} to fill the JasperReport
     * <li>The FillMonitor's reportFinished(JasperPrint) event calls this
     * report's reportExecutor.execute() to print / preview / export the report.
     * </ol>
     * At any point an error or cancel will (should) stop the process and
     * generate a PrintStatusEvent to that effect.
     */
    @Override
    public void run() {

        java.awt.Color bg = null;

        try {

            // fire property change that lets interested parties know execution has started
            firePrintStatusChanged(StatusCode.STARTED);

            // get jasperReport from ReportManager
            currentJasperReport = getJasperReport();

            if (currentJasperReport == null) {
                firePrintStatusChanged(StatusCode.ERROR);
                return;
            }

            firePrintStatusChanged(StatusCode.COMPILED);

            reportName = currentJasperReport.getName();
            logger.debug("The thread is filling on EDT? " + SwingUtilities.isEventDispatchThread());

            // Multiple PromptFillers can be registered but not guaranteed in what order they will be called.
            for (PrintPromptFiller filler : promptFillers) {
                logger.debug("calling fillPrompts on a PrintPromptFiller");
                if (!filler.fillPrompts(this)) {
                    firePrintStatusChanged(StatusCode.CANCELED);
                    return;
                }
            }

            // don't think it can be canceled at this point, but make sure
            if (canceled) {
                firePrintStatusChanged(StatusCode.CANCELED);
                return;
            }

            // if set then show the prompts dialog.
            if (isPromptForParameters()) {
                SwingUtilities.invokeAndWait(() -> {
                    logger.debug("calling showPromptDialog...");
                    PromptDialog.showPromptDialog(parent, JasperReportImpl.this);
                });
                logger.trace("did fill parameters");
                if (canceled) {
                    logger.warn("canceled when prompting for params!");
                    return; // canceled event fired via setCanceled(true)
                }
            }

            // set the button's background color to have visible indicator of action
            bg = (printButton == null ? null : printButton.getBackground());
            if (printButton != null) {
                printButton.setBackground(bg.darker().darker());
            }

            // at this point we should have a valid JasperReport
            // this is never the EDT, as run is started by a SingleThreadExecutor
            // NOTE: even though this is invokeAndWait it does not block while report is filling
            SwingUtilities.invokeAndWait(() -> {
                monitor = new FillMonitor(JasperReportImpl.this);
                logger.debug("started fill monitor");
            });

            logger.trace("Starting a Waiter thread and calling .join");
            Thread waiter = new Thread(() -> {
                monitor.startFill();
                while (monitor.isFilling()) {
                    try {
                        Thread.sleep(5);
                    } catch (Exception tx) {
                        logger.error("Error while waiting on fill monitor", tx);
                    }
                }
                // ToDo: Showing message should NOT be here - need to move outward
                if (monitor.getThrowable() != null) {
                    SwingUtilities.invokeLater(() -> {
                        Throwable cause = monitor.getThrowable();
                        String msg = "";
                        while (cause != null) {
                            msg += "\n" + cause.toString();
                            cause = cause.getCause();
                        }
                        JOptionPane.showMessageDialog(null, "An error occurred filling report:" + msg, "Report Failed", JOptionPane.ERROR_MESSAGE);
                    });
                }
            });

            // start the waiter thread
            waiter.start();
            // current thread waits until waiter dies.
            waiter.join();

            logger.trace("Waiter has joined!");            
            error = false;  // why?

        } catch (Exception err) {
            logger.error("Error while generating report: " + err.getLocalizedMessage(), err);
            firePrintStatusChanged(StatusCode.ERROR);
            // remove this report from cache on error, so it can be tried again.
            ReportManager.removeFromCache(reportPath);
        } finally {
            monitor = null; // free resource
            currentJasperReport = null;
            if (bg != null) {
                printButton.setBackground(bg);
            }
        }

    }

    /**
     * This returns the name of the {@link ConnectionInfo} that is to be used
     * for the database connection. This defaults to &quot;default&quot; and
     * only needs to be changed if one running instance of the JasperPrint (one
     * application or print server) is using more then one database.
     *
     * @return the connectionID
     */
    public String getConnectionID() {
        return connectionID;
    }

    /**
     * @param connectionID the connectionID to set
     * @see #getConnectionID()
     */
    public void setConnectionID(String connectionID) {
        this.connectionID = connectionID;
    }

    public PrintAction.Duplex getDuplex() {
        return duplex;
    }

    /**
     * @param printer the printer to set
     */
    public void setPrinter(String printer) {
        if (printer != null && printer.trim().isEmpty()) {
            // don't allow setting to empty string!
            this.printer = null;
        } else {
            this.printer = printer;
        }
    }

    public void setDuplex(PrintAction.Duplex duplex) {
        if (duplex == null) {
            this.duplex = PrintAction.Duplex.OFF;
        } else {
            this.duplex = duplex;
        }
    }

    /**
     * @return the collate
     */
    public boolean isCollate() {
        return collate;
    }

    public void setShowDialog(boolean showDialog) {
        this.showDialog = showDialog;
    }

    /**
     * If the Printing dialog should be displayed or not.
     *
     * @return
     */
    public boolean isShowDialog() {
        return showDialog;
    }

    /**
     * @return the copies
     */
    public int getCopies() {
        return copies;
    }

    /**
     * @param collate the collate to set
     */
    public void setCollate(boolean collate) {
        this.collate = collate;
    }

    /**
     * @param copies the copies to set
     */
    public void setCopies(int copies) {
        this.copies = copies;
    }

    /**
     * @return the printer
     */
    public String getPrinter() {
        return printer;
    }

    /**
     * This may be called by Print Button, etc.
     */
    public void dispose() {

        statusListeners.clear();
        promptFillers.clear();

    }

    /**
     * @return the exportFilePath
     */
    public String getExportFilePath() {
        return exportFilePath;
    }

    /**
     * Specifies the File to save to when using EXPORT_TO_XXX action. Must be
     * full file path, not just a directory.
     * <p>
     * This parameter is cleared after each execution. If no path is specified
     * then it will show a File Picker and allow you to choose the file to save
     * to.
     *
     * @param exportFilePath the exportFilePath to set
     */
    public void setExportFilePath(String exportFilePath) {
        this.exportFilePath = exportFilePath;
    }

    /**
     * @return the overwriteExportFile
     */
    public boolean isOverwriteExportFile() {
        return overwriteExportFile;
    }

    /**
     * Set this parameter to force an existing file to be overwritten w/o a
     * warning. Used for EXPORT_TO_XXX actions.
     *
     * @param overwriteExportFile the overwriteExportFile to set
     */
    public void setOverwriteExportFile(boolean overwriteExportFile) {
        this.overwriteExportFile = overwriteExportFile;
    }

}
