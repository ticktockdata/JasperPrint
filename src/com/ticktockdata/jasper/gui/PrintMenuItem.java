/*
 * Copyright (C) 2019 Joseph A Miller
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
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.JOptionPane;

/**
 * A JMenuItem that is used by the PrintButton as the drop-down items.
 * Note that there are 2 different menu options for each report, one that
 * defaults to Print and the other that is always Preview.
 * <p>Note that a single JasperReportImpl object is shared between the Print
 and the Preview PrintMenuItem.  The Preview one always sets the report's 
 PrintExecutor.Action to PREVIEW.  The initial executor is stored in
 defaultExecutor, which is then restored by the Print item when called.
 Also note that the first two Items on a button's drop-down also share their
 JasperReportImpl with the Button itself.
 * @author JAM {javajoe@programmer.net}
 * @since Jan 16, 2019
 */
public class PrintMenuItem extends javax.swing.JMenuItem implements MouseListener, Serializable {
    
    private JasperReportImpl jasperReportImpl;
    private boolean preview = false;
    private PrintExecutor defaultExecutor;
    private PrintButton printButton;
    private PrintPreferences prefs;
    
    // added - TDY
//    private List<ActionListener> printListeners = new ArrayList<ActionListener>();
    
    /**
     * Must use this constructor to create a PrintMenuItem.  This class is used
     * by {@link PrintButton} and will probably not be used by programmer.
     * @param report the {@link com.ticktockdata.jasper.JasperReportImpl} to be printed
     * @param printButton normally a {@link javax.swing.JButton}, but can be any {@link java.awt.Component}
     * @param preview if this menu item shall be a preview only (true) or print (false) option
     */
    public PrintMenuItem(JasperReportImpl report, PrintButton printButton, boolean preview) {
        addMouseListener(this);
        
        this.printButton = printButton;
        this.preview = preview;
        jasperReportImpl = report;
        
        if (preview == false) {
            // get a different preferences from other
            prefs = new PrintPreferences(report.getReportName() + "_sub_" + printButton.getPreferenceID());
        }
        
        //defaultExecutor = jasperReportImpl.getPrintExecutor();
        
    }
    
    
    public JasperReportImpl getJasperReportImp() {
        return jasperReportImpl;
    }
    

    @Override
    public String getText() {
        if (super.getText() == null || super.getText().isEmpty()) {
            if (jasperReportImpl == null) {
                return "Err: Rpt Not Set";
            }
            super.setText(jasperReportImpl.getReportName() + (preview ? " PREVIEW" : ""));
        }
        return super.getText();
    }
    
    
    /**
     * Remove reference to resources so Garbage Collector can get rid of this object.
     * Called by PrintButton when popup list is cleared
     */
    public void dispose() {
        jasperReportImpl = null;
        removeMouseListener(this);
    }

    /**
     * Preview menu item gets a lighter background color then the print option.
     * @return 
     */
    @Override
    public Color getBackground() {
        if (preview) {
            return super.getBackground().brighter().brighter();
        } else {
            return super.getBackground();
        }
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
                || e.getX() > ((AbstractButton)e.getSource()).getWidth() 
                || e.getY() > ((AbstractButton)e.getSource()).getHeight()) {
            return;
        }
        
        switch (e.getButton()) {
            case MouseEvent.BUTTON3:
                if (prefs != null) {
                    PrintPreferencesDialog.editPrintPrefs(prefs, printButton.getAppFrame());
                }
                break;
            case MouseEvent.BUTTON1:
                System.out.println("Left mouse button clicked, print report");
                if (preview) {
                    jasperReportImpl.setPrintAction(PrintExecutor.Action.PREVIEW);
                    jasperReportImpl.execute();
                } else {
                    // jasperReportImpl.setPrintExecutor(defaultExecutor);
                    jasperReportImpl.execute(prefs);
                }
                
//                firePrintListeners("execute");
                
                break;
            case MouseEvent.BUTTON2:
                JOptionPane.showMessageDialog(printButton.getParent(),
                    "This is a pointless message to tell you\n" +
                    "that Middle-Click is useless", "Hi There!",
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

//    private void firePrintListeners(String command) {
//        ActionEvent e = new ActionEvent(this, 0, command);
//        
//        for (ActionListener l : printListeners) {
//            l.actionPerformed(e);
//        }
//    }
//    
//    public PrintMenuItem addPrintListener(ActionListener listener) {
//        printListeners.add(listener);
//        return this;
//    }
//    
//    public PrintMenuItem addAllPrintListeners(List<ActionListener> listeners) {
//        printListeners.addAll(listeners);
//        return this;
//    }
//    
//    public PrintMenuItem removePrintListener(ActionListener listener) {
//        printListeners.remove(listener);
//        return this;
//    }
    
}
