/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ticktockdata.jasper.gui;

import com.ticktockdata.jasper.JasperReportImpl;
import com.ticktockdata.jasper.ReportManager;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.SwingUtilities;

/**
 *
 * @author JAM {javajoe@programmer.net}
 * @since Aug 28, 2019
 */
public class ReportButton extends javax.swing.JButton implements ActionListener {
    
    JasperReportImpl report;
    
    public ReportButton(File reportFile) {
        
        // call construction routine
        initButton(reportFile);
        
    }
    
    
    private void initButton(File reportFile) {
        
        // set the button's text to file name for now
        this.setText(reportFile.getName());
        this.setToolTipText(reportFile.getAbsolutePath());
        
        // set size
        this.setMinimumSize(new Dimension(150, 20));
        this.setPreferredSize(new Dimension(225, 25));
        this.setMaximumSize(new Dimension(350, 35));
        
        // load the report on a separate thread
        new Thread(() -> {
            report = ReportManager.getReport(reportFile.getAbsolutePath());
            String reportName = report.getReportName();
            
            // once loaded then set text and new tool tip text
            // but must do on EDT
            SwingUtilities.invokeLater(() -> {
                
                ReportButton.this.setText(reportName);
                setToolTipText("<html><b>" + reportName + "</b><br>" + getToolTipText());
                
                // and add an action listener
                addActionListener(ReportButton.this);
            });
        }).start();
        
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        report.execute();
    }

}
