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

import com.ticktockdata.jasper.PrintStatusEvent.StatusCode;
import java.awt.event.ActionEvent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.fill.AsynchronousFillHandle;
import net.sf.jasperreports.engine.fill.AsynchronousFilllListener;
import net.sf.jasperreports.engine.fill.FillListener;
import static com.ticktockdata.jasper.ReportManager.logger;

/**
 * This class takes care of both filling and viewing / printing / executing a
 * {@link JasperReportImpl} by starting the Fill process, then calling the
 * report's execute method after fill is complete.
 * <p>
 * This uses an {@link net.sf.jasperreports.engine.fill.AsynchronousFillHandle}
 * to allow canceling the process and to help avoid tying up the
 * {@link java.awt.EventDispatchThread} Thread, but this class itself must be
 * run from EDT in order to prevent issues with Fill Parameter and Processing
 * dialogs.
 * <p>
 * This class will fire {@link PrintStatusEvent} for FILLED, COMPLETE,
 CANCELED, ERROR
 *
 * @author JAM
 * @since Aug 29, 2018
 */
public class FillMonitor extends javax.swing.JDialog implements AsynchronousFilllListener, FillListener {

    private int pageCount = 0;
    private AsynchronousFillHandle reportHandle;
    private JasperReportImpl report;
    private Timer timer;
    private boolean canceled = true;
    private boolean error = true;
    private Throwable throwable = null;
    private boolean stopped = false;


    /**
     * Creates new form FillMonitor
     *
     * @param report
     */
    public FillMonitor(JasperReportImpl report) {

        super((JFrame) null, false);

        this.report = report;

        initComponents();
        this.pack();

        try {
            reportHandle = AsynchronousFillHandle.createHandle(
                    report.getJasperReport(),
                    report.getParams(),
                    ReportConnectionManager.getReportConnection(report.getConnectionID()));
        } catch (Exception ex) {
            logger.error("Error creating AsynchronousFillHandle for report!", ex);
            throwable = ex;
            error = true;
            stopMonitor();
            return;
        }

        // it always allows interrupt, but if false then waits x seconds before showing.
        this.cmdCancel.setEnabled(true);    //report.isAllowInterrupt());

        // add listeners for fill report
        this.reportHandle.addFillListener(this);
        this.reportHandle.addListener(this);

        if (report.getParent() != null) {
            this.setLocationRelativeTo(report.getParent());
        } else {
            java.awt.Point center = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
            int dHt = this.getHeight();
            int dWd = this.getWidth();
            this.setLocation(center.x - (dWd / 2), center.y - (dHt / 2));
        }

        this.progressBar.setIndeterminate(true);

        // check when to show the progress dialog
        if (report.getProgressDelay() == 0) {
            // show immediately
            this.setVisible(true);
        } else if (report.getProgressDelay() < 0) {
            // never show!
        } else {
            // this needs to delay and only show if report is too slow in completing.
            timer = new Timer((report.getProgressDelay() * 1000), (ActionEvent e) -> {
                FillMonitor.this.setVisible(true);
            });
            timer.setRepeats(false);
            timer.start();
        }
    }

    /**
     * This must be called to terminate monitor. Is called from the
     * reportCanceled / reportFillError / reportFinished methods
     */
    private void stopMonitor() {

        if (timer != null) {
            timer.stop();
            timer = null;
        }
        
        this.setVisible(false);
        stopped = true; // must be set to true or isFilling will never return false!
        dispose();

    }

    /**
     * /**
     * Starts the actual report filling process.
     */
    public void startFill() {
        reportHandle.startFill();
    }

    /**
     * Allows checking if the filling process is complete or not.
     *
     * @return
     */
    public boolean isFilling() {
        return !stopped;    // && (status == PrintStatusEvent.STATUS_UNDEFINED || status == PrintStatusEvent.STARTED);
    }

    /**
     *
     * @return true if there was an error, false otherwise
     */
    public boolean isError() {
        return error;
    }

    public boolean isCanceled() {
        return canceled;
    }
    
    /**
     * If a {@link Throwable} error occurs it can be accessed here, otherwise
     * this returns null;
     *
     * @return
     */
    public Throwable getThrowable() {
        return throwable;
    }

    /**
     * After the report is filled this can be used to check how many pages it
     * contains.
     *
     * @return
     */
    public int getPageCount() {
        return pageCount;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();
        cmdCancel = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Busy");

        jLabel1.setText("Generating Report, please wait...");

        progressBar.setPreferredSize(new java.awt.Dimension(148, 25));
        progressBar.setStringPainted(true);

        cmdCancel.setText("Cancel");
        cmdCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdCancelActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(progressBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 99, Short.MAX_VALUE)
                        .addComponent(cmdCancel)
                        .addGap(0, 100, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(cmdCancel)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cmdCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdCancelActionPerformed

        try {
            reportHandle.cancellFill();
            report.getPrintExecutor().cancelExecute();
            logger.info("User Canceled Report execution!");
        } catch (Exception ex) {
            logger.error("Error canceling the report execution!", ex);
            throwable = ex;
        }

    }//GEN-LAST:event_cmdCancelActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cmdCancel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JProgressBar progressBar;
    // End of variables declaration//GEN-END:variables

    @Override
    public void reportFinished(JasperPrint jasperPrint) {

        report.firePrintStatusChanged(StatusCode.FILLED);

        if (this.report.getPrintExecutor().execute(jasperPrint)) {
            report.firePrintStatusChanged(StatusCode.COMPLETE);
        } else {
            logger.error("Failed to execute the report!");
        }

        stopMonitor();
    }

    @Override
    public void reportCancelled() {
        canceled = true;
        report.firePrintStatusChanged(StatusCode.CANCELED);
        stopMonitor();
    }

    @Override
    public void reportFillError(Throwable t) {
        error = true;
        logger.error("Error occurred during fillReport: " + t.getLocalizedMessage(), t);
        throwable = t;
        report.firePrintStatusChanged(StatusCode.ERROR);
        stopMonitor();
    }

    @Override
    public void pageGenerated(JasperPrint jasperPrint, final int pageIndex) {
        logger.debug("Filled page " + pageIndex + " of report " + report.getReportName());
        pageCount = pageIndex;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                progressBar.setString("Page " + pageIndex);
            }
        });
    }

    @Override
    public void pageUpdated(JasperPrint jasperPrint, final int pageIndex) {
        //if (!isVisible()) return;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (pageIndex == 0) {
                    progressBar.setMaximum(pageCount);
                    progressBar.setMinimum(0);
                    progressBar.setIndeterminate(false);
                    progressBar.setString(null);
                    logger.debug("Now updating the pages for view, report " + report.getReportName());
                }
                progressBar.setValue(pageIndex);
                logger.trace("Updated page " + pageIndex + " of report " + report.getReportName());

                if (pageIndex == getPageCount()) {
                    progressBar.setIndeterminate(true);
                    progressBar.setString("Creating Print Job");
                }
            }
        });

    }
}
