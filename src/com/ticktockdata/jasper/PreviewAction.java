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

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.SwingUtilities;
import net.sf.jasperreports.engine.JasperPrint;

/**
 *
 * @author JAM {javajoe@programmer.net}
 * @since Aug 27, 2018
 */
public class PreviewAction extends PrintExecutor {

    private JasperViewer viewer;
    
    public PreviewAction(JasperReportImpl report) {
        super(report);
    }
    
    @Override
    public Action getAction() {
        return Action.PREVIEW;
    }

    @Override
    public boolean isValid() {
        return true;
    }
    
    
    WindowAdapter frameCloser = new WindowAdapter() {
        @Override
        public void windowClosing(WindowEvent e) {
            super.windowClosing(e);
            dispose();
        }
        
    };
    
    /**
     * This is called by the frameCloser when Preview window closes
     */
    public void dispose() {
        
        if (viewer != null) {
            System.out.println("PreviewAction called dispose");
            viewer.removeWindowListener(frameCloser);
            viewer = null;
        }
        frameCloser = null;
    }
    

    @Override
    public boolean execute(final JasperPrint print) {
        try {
            if (SwingUtilities.isEventDispatchThread()) {
                // no issue!
                viewer = JasperViewer.viewReport(print);
            } else {
                //not EDT, use Swing Utilities
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        viewer = JasperViewer.viewReport(print);
                    }
                });
                
            }
            
            /**
             * Add a WindowListener to find out when frame closes!
             */
            if (viewer != null) {
                // visible, add a dispose listener
                viewer.addWindowListener(frameCloser);
            } else {
                dispose();
            }
            
            return true;
            
        } catch (Exception ex) {
            ReportManager.LOGGER.error("Error viewing the report", ex);
            return false;
        }
        
    }
    
    
    @Override
    public void cancelExecute() {
        if (viewer != null) {
            viewer.exitForm();
        } else {
            System.out.println("cancelExecute called and viewer is not null - did not dispose!");
        }
    }
    
    
}
