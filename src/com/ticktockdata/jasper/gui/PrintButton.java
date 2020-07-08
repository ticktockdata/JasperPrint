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
package com.ticktockdata.jasper.gui;

import com.ticktockdata.jasper.JasperReportImpl;
import com.ticktockdata.jasper.PrintExecutor;
import com.ticktockdata.jasper.PrintPreferences;
import com.ticktockdata.jasper.PrintPromptFiller;
import com.ticktockdata.jasper.PrintStatusListener;
import com.ticktockdata.jasper.ReportManager;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import org.apache.log4j.Logger;

/**
 * This is a JButton GUI component with features for implementing
 * {@link com.ticktockdata.jasper.JasperReportImpl}. The most basic usage is to
 * set a single report to the button using the {@link setReport(String)} method.
 * Additional reports can be added, to be accessed via a Pop-Up menu that
 * appears when Right-Clicked, using the method
 * {@link setAdditionalReports(JasperReportImpl...)}.
 *
 * @author JAM {javajoe@programmer.net}
 * @since Dec 13, 2018
 */
public class PrintButton extends javax.swing.JButton implements MouseListener, Serializable {

    private final List<JasperReportImpl> additionalReports = new ArrayList<>();
    /**
     * This must exist for JavaBeans purpose, allows setting report in GUI via
     * string
     */
    private String report;
//    private boolean showPromptDialog = true;
    private JPopupMenu popup = null;
    private int popupCount = 0;
    private JasperReportImpl jasperReportImpl;
    private String preferenceID = "";
    private java.awt.Frame appFrame;

    // added - JAM (revised from PrintListeners (ActionListener) by TDY)
    private final List<PrintStatusListener> printStatusListeners = new ArrayList<>();
    private final List<PrintPromptFiller> printPromptFillers = new ArrayList<>();

    /**
     * the prefs are set in the setJasperReportImpl routine.  Do not use this
     * variable directly, use {@link getPrefs()} instead.
     */
    private PrintPreferences prefs;

    public PrintButton() {

        setText("Print");
        setIconTextGap(10);
        setPreferredSize(new java.awt.Dimension(105, 30));
        setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/ticktockdata/jasper/resources/printer16.png")));
        setToolTipText("Click to Print, Right-Click for Options.");
        addMouseListener(this);
        setHideActionText(false);

    }

    /**
     * Access to the primary report for this PrintButton
     *
     * @return the primary report, as a JasperReportImpl
     * @see #setReport
     */
    public JasperReportImpl getJasperReportImp() {
        return jasperReportImpl;
    }

    /**
     * When done programmatically rather then via the GUI Editor you can set the
     * button's primary report via this method instead of
     * {@link setReport(String)}
     *
     * @param jri
     */
    public void setJasperReportImpl(JasperReportImpl jri) {
        
        if (this.jasperReportImpl != null) {
            // remove the existing listeners and fillers from old report
            for (PrintStatusListener listener : printStatusListeners)
                this.jasperReportImpl.removePrintStatusListener(listener);
            
            for (PrintPromptFiller filler : printPromptFillers)
                this.jasperReportImpl.removePrintPromptFiller(filler);
            // dispose old report
            this.jasperReportImpl.dispose();
        }
        
        this.jasperReportImpl = jri;
        this.report = jri.getReportPath();
        //this.setPrefs(new PrintPreferences(jri.getReportName() + getPreferenceID()));
        System.out.println("Set the report, path  = " + report);
        
        // if listeners registerd, then add them
        for (PrintStatusListener sl : printStatusListeners) {
            this.jasperReportImpl.addPrintStatusListener(sl);
        }
        for (PrintPromptFiller filler : printPromptFillers) {
            this.jasperReportImpl.addPrintPromptFiller(filler);
        }
    }

    /**
     * Sets the primary report for this PrintButton
     *
     * @param report the path of primary report to set
     * @see #getReport
     * @beaninfo bound: true preferred: true attribute: visualUpdate true
     * description: primary report
     */
    public void setReport(String report) {
//        this.report = report;
        setJasperReportImpl(ReportManager.getReport(report));
        if (jasperReportImpl != null) {
            jasperReportImpl.setPrintButton(this);
        }
    }

    public String getReport() {
        return report;
    }

    /**
     * Calling this will clear the reports list <u>except for the main
     * report</u>
     * and add the entered reports to the list. Resets the popup menu
     *
     * @param reports list of reports to set for this button
     * @see #setJasperReportImpl(JasperReportImpl)
     */
    public void setAdditionalReports(JasperReportImpl... reports) {

        // Dispose the existing additional reports
        for (JasperReportImpl r : additionalReports) {
            for (PrintStatusListener listener : printStatusListeners)
                r.removePrintStatusListener(listener);
            
            for (PrintPromptFiller filler : printPromptFillers)
                r.removePrintPromptFiller(filler);
            
            r.dispose();
        }
        // then clear list
        this.additionalReports.clear();

        for (JasperReportImpl rpt : reports) {
            rpt.setPrintButton(this);
            for (PrintStatusListener sl : printStatusListeners) 
                rpt.addPrintStatusListener(sl);
            for (PrintPromptFiller filler : printPromptFillers)
                rpt.addPrintPromptFiller(filler);
            this.additionalReports.add(rpt);
        }
        // reset popup count so it forces new popup
        popupCount = 0;
    }

    /**
     * @see setAdditionalReports(JasperReportImpl...)
     * @return list of additional reports, outside of main report, that belong
     * to this button
     */
    public List<JasperReportImpl> getAdditionalReports() {
        return additionalReports;
    }
    
    
    // we need this for centering the preferences dialog
    public Frame getAppFrame() {
        if (appFrame == null) {
            try {
                appFrame = (Frame)this.getTopLevelAncestor();
            } catch (Exception x) {
                appFrame = null;
                System.out.println("Failed to get Frame as top level ancenstor; " + x.toString());
            }
        }
        return appFrame;
    }

//    /**
//     * 
//     * @return if the Prompt filling dialog shall show or not
//     * @beaninfo
//     *        bound: true
//     *    preferred: true
//     *    attribute: visualUpdate true
//     *  description: show prompt dialog
//     */
//    public boolean isShowPromptDialog() {
//        return showPromptDialog;
//    }
//    
//    
//    /**
//     * This is True by default, if user sets it false then any required report 
//     * parameters must be set via a {@link com.ticktockdata.jasper.PrintPromptFiller}.
//     * @param showPromtDialog 
//     * @beaninfo
//     *        bound: true
//     *    preferred: true
//     *    attribute: visualUpdate true
//     *  description: show prompt dialog
//     */
//    public void setShowPromptDialog(boolean showPromtDialog) {
//        this.showPromptDialog = showPromtDialog;
//    }
    /**
     * Internal method that shows the pop-up menu on right-click
     */
    private void showPopup() {
        
        if (popupCount == 0) {
            // create a new popup menu
            if (popup != null) {
                // destroy old
                System.out.println("We need to destroy the old popup!");
                for (java.awt.Component c : popup.getComponents()) {
                    if (c instanceof PrintMenuItem) {
                        ((PrintMenuItem) c).dispose();
                    }
                }
                popup = null;
            }
            popup = new JPopupMenu();

            // add print and preview options for the primary report
            if (getJasperReportImp() != null) {
                popup.add(new PrintMenuItem(getJasperReportImp(), this, false));
                popupCount++;
                popup.add(new PrintMenuItem(getJasperReportImp(), this, true));
                popupCount++;
            }

            for (JasperReportImpl s : additionalReports) {
                s.setPrintAction(PrintExecutor.Action.PRINT);
                popup.add(new PrintMenuItem(s, this, false));
                popupCount++;
                popup.add(new PrintMenuItem(s, this, true));
                popupCount++;
            }

            // now add the settings one
            JMenuItem settings = new JMenuItem("<Edit Settings>");
            settings.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    PrintPreferencesDialog.editPrintPrefs(PrintButton.this.getPrefs(), getAppFrame());
                }
            });

            popup.add(settings);
            popupCount++;
        }

        popup.show(this, 0, this.getHeight());

    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getX() < 0
                || e.getY() < 0
                || e.getX() > ((AbstractButton) e.getSource()).getWidth()
                || e.getY() > ((AbstractButton) e.getSource()).getHeight()) {
            System.out.println("Invalid click, exiting...");
            return;
        }

        System.out.println("Mouse was released!");
        switch (e.getButton()) {
            case MouseEvent.BUTTON3:
                showPopup();
                break;
            case MouseEvent.BUTTON1:
                if (getJasperReportImp() == null) {
                    JOptionPane.showMessageDialog(getParent(), "No report set!", "Invalid:", JOptionPane.WARNING_MESSAGE);
                } else {
                    jasperReportImpl.execute(getPrefs());
                }
                break;
            case MouseEvent.BUTTON2:
                JOptionPane.showMessageDialog(this.getParent(),
                        "This is a pointless message to tell you\n"
                        + "that Middle-Click is useless", "Hi There!",
                        JOptionPane.INFORMATION_MESSAGE);
                break;
            default:
                break;
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
    
    
    public void addPrintStatusListener(PrintStatusListener lisentner) {
        printStatusListeners.add(lisentner);
        if (jasperReportImpl != null) jasperReportImpl.addPrintStatusListener(lisentner);
        for (JasperReportImpl jri : additionalReports) {
            jri.addPrintStatusListener(lisentner);
        }
    }
    
    
    public void removePrintStatusListener(PrintStatusListener listener) {
        printStatusListeners.remove(listener);
        if (jasperReportImpl != null) jasperReportImpl.removePrintStatusListener(listener);
        for (JasperReportImpl jri : additionalReports) {
            jri.removePrintStatusListener(listener);
        }
    }
    
    
    /**
     * This forward-calls addPromptFiller to all of this button's reports
     * @param filler 
     */
    public void addPrintPromptFiller(PrintPromptFiller filler) {
        if (printPromptFillers.contains(filler)) return;
        printPromptFillers.add(filler);
        if (jasperReportImpl != null) jasperReportImpl.addPrintPromptFiller(filler);
        for (JasperReportImpl jri : additionalReports) {
            jri.addPrintPromptFiller(filler);
        }
    }
    
    
    public void removePrintPromptFiller(PrintPromptFiller filler) {
        if (!printPromptFillers.contains(filler)) return;
        printPromptFillers.remove(filler);
        if (jasperReportImpl != null) jasperReportImpl.removePrintPromptFiller(filler);
        for (JasperReportImpl jri : additionalReports) {
            jri.removePrintPromptFiller(filler);
        }
    }
    
//    public List<PrintStatusListener> getPrintStatusListeners() {
//        return printStatusListeners;
//    }
    
    
    /**
     * This is normally not set and defaults to an Empty String.  Each button 
     * looks up the Preferences by it's parent (JPanel) and the report name.
     * By setting a preferenceID you can force multiple settings for the same
     * panel
     * @return the preferenceID
     */
    public String getPreferenceID() {
        return preferenceID;
    }

    /**
     * @param preferenceID the preferenceID to set
     */
    public void setPreferenceID(String preferenceID) {
        this.preferenceID = preferenceID;
    }

    /**
     * This loads the preferences if it is null
     * @return the prefs
     */
    public PrintPreferences getPrefs() {
        
        if (prefs == null) {
            if (jasperReportImpl == null) {
                throw new IllegalStateException("Cannot get Prefs if JasperReportImpl is not set!");
            }
            prefs = new PrintPreferences(jasperReportImpl.getReportName() + "_" + getPreferenceID());
        }
        return prefs;
    }
    

}
