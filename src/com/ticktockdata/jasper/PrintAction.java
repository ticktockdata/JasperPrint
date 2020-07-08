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

import static com.ticktockdata.jasper.PrintAction.Duplex.LONG_EDGE;
import static com.ticktockdata.jasper.PrintAction.Duplex.SHORT_EDGE;
import java.awt.Window;
import javax.print.PrintService;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.MultipleDocumentHandling;
import javax.print.attribute.standard.OrientationRequested;
import javax.print.attribute.standard.SheetCollate;
import javax.print.attribute.standard.Sides;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.type.OrientationEnum;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimplePrintServiceExporterConfiguration;

/**
 *
 * @author JAM {javajoe@programmer.net}
 * @since Aug 27, 2018
 */
public class PrintAction extends PrintExecutor {
    
    private boolean interrupted = false;
    private JRPrintServiceExporter printExporter;
    
    @Override
    public void cancelExecute() {
        interrupted = true;
        if (printExporter != null) {
            printExporter.cancelExport();
            logger.info("canceled the print export!");
        }
    }

    

    /**
     * Duplex settings do not appear to work.
     */
    public enum Duplex {
        OFF,
        LONG_EDGE,
        SHORT_EDGE;
        
        /**
         * This is a more forgiving version of valueOf(String name), it trims
         * leading / trailing spaces and is not case-sensitive.  Spelling must
         * be exact, though.
         * @param name 
         * @return null if invalid, instead of throwing error.
         */
        public static Duplex fromString(String name) {
            try {
                return valueOf(name.trim().toUpperCase());
            } catch (Exception ex) {
                return null;
            }
        }
    }
    
    
    
    public PrintAction(JasperReportImpl report) {
        super(report);
    }
    
    
    @Override
    public PrintExecutor.Action getAction() {
        return PrintExecutor.Action.PRINT;
    }


    @Override
    public boolean isValid() {
        // printing is always valid, uses print dialog if not printer set.
        return true;
    }

    @Override
    public boolean execute(JasperPrint jasperPrint) {

        try {
            //return JasperPrintManager.printReport(print, showDialog);
            logger.debug(this.getClass().getName() + ".execute() called, Thread is EDT? " 
                    + SwingUtilities.isEventDispatchThread());
            // This sets the specific printer,
            PrintService printService = null;
            
            if (getReport().getPrinter() != null) {
                printService = ReportManager.getPrintService(getReport().getPrinter());
            }
            if (printService == null) {
                printService = ReportManager.getDefaultPrintService();
            }
            if (printService == null) {
                ReportManager.focusToDialog("Warning:", "OK");
                JOptionPane.showMessageDialog(getReport().getParent(), 
                        "Could not find any available printer"
                        , "Warning:", JOptionPane.WARNING_MESSAGE);
                return false;
            }
            
            
            PrintRequestAttributeSet requestSet = new HashPrintRequestAttributeSet();
            
            
//            // Set copy count (moved to bottom)
//            requestSet.add(new javax.print.attribute.standard.Copies(getCopies()));

            // set collate in case certain printers / system need it
            requestSet.add(getReport().isCollate() ? javax.print.attribute.standard.SheetCollate.COLLATED
                    : javax.print.attribute.standard.SheetCollate.UNCOLLATED);

            // if this is a letter size paper, then set to letter
            int pgWd = jasperPrint.getPageWidth();
            int pgHt = jasperPrint.getPageHeight();

            // if it is letter size then set - maybe should have a routine.
            if (((pgWd == 612) && pgHt == 792)
                    || (pgWd == 792 && pgHt == 612)) {
                // Letter size
                requestSet.add(MediaSizeName.NA_LETTER);

            }

            // extract the Orientation from jasperPrint
            if (jasperPrint.getOrientationValue().equals(OrientationEnum.LANDSCAPE)) {
                requestSet.add(OrientationRequested.LANDSCAPE);
            } else {
                requestSet.add(OrientationRequested.PORTRAIT);
            }

            // attempt to set duplex attribute - may not work! v2.8, 2017-09-26, JAM
            switch (getReport().getDuplex()) {
                case LONG_EDGE:
                    requestSet.add(Sides.TWO_SIDED_LONG_EDGE);
                    break;
                case SHORT_EDGE:
                    requestSet.add(Sides.TWO_SIDED_SHORT_EDGE);
                    break;
                default:
                    requestSet.add(Sides.ONE_SIDED);

            }
            
            // attempt to set collate info, plus multi-document-handling
            requestSet.add(getReport().isCollate() ? SheetCollate.COLLATED : SheetCollate.UNCOLLATED);
            if (getReport().isCollate()) {
                requestSet.add(MultipleDocumentHandling.SEPARATE_DOCUMENTS_COLLATED_COPIES);
            } else {
                requestSet.add(MultipleDocumentHandling.SEPARATE_DOCUMENTS_UNCOLLATED_COPIES);
            }
//            requestSet.add(MultipleDocumentHandling.SINGLE_DOCUMENT_NEW_SHEET);
            
//            requestSet.add(new javax.print.attribute.standard.NumberUp(4));
            
            
            
            
            // results of PrintService
//            printer-info = class javax.print.attribute.standard.PrinterInfo
//            queued-job-count = class javax.print.attribute.standard.QueuedJobCount
//            printer-is-accepting-jobs = class javax.print.attribute.standard.PrinterIsAcceptingJobs
//            printer-name = class javax.print.attribute.standard.PrinterName
//            color-supported = class javax.print.attribute.standard.ColorSupported
//            pdl-override-supported = class javax.print.attribute.standard.PDLOverrideSupported
            
            
            // doesn't work?
             requestSet.add(javax.print.attribute.standard.DialogTypeSelection.NATIVE);
            

//            System.out.println(jasperPrint.getName() + " Page Size = " + jasperPrint.getPageHeight() + "x" + jasperPrint.getPageWidth());
//            System.out.println("Margins are: T " + jasperPrint.getTopMargin() + ", R " + jasperPrint.getRightMargin() + ", B " + jasperPrint.getBottomMargin() + ", L " + jasperPrint.getLeftMargin());
//            //attempt to set "printer margins" (doesn't work, printService is always null)
//            if(config.getPrintService() != null){
//                MediaPrintableArea area = (MediaPrintableArea) printExporter.getPrintService()
//                        .getSupportedAttributeValues(MediaPrintableArea.class, null, requestSet);
//                System.out.println("MediaPrintableArea = " + area);
//                if(area != null){
//                    requestSet.add(area);
//                }  
//            }
//            else System.out.println("printExporter.getPrintService() == null");

        
            // Set copy count
            requestSet.add(new javax.print.attribute.standard.Copies(getReport().getCopies()));
            
            
            // Create PrintServiceExporter
            SimplePrintServiceExporterConfiguration config = new SimplePrintServiceExporterConfiguration();
            
            //net.sf.jasperreports.export.SimpleCommonExportConfiguration xconfig = new net.sf.jasperreports.export.SimpleCommonExportConfiguration();
            
            
            
            // do actual setting of print service
            config.setPrintService(printService);
            // set if we want to use the print dialog or not
            config.setDisplayPrintDialog(getReport().isShowDialog());
            // add request set to configuration
            config.setPrintRequestAttributeSet(requestSet);
            // attempt to fix copies issue, doesn't make a difference
            config.setOverrideHints(Boolean.FALSE);
            
            // create Print Exporter
            printExporter = new JRPrintServiceExporter();
            
            // add the printing data (doc) to the exporter
             printExporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            // set configuration to printExporter
            printExporter.setConfiguration(config);
           
            
//            // print out some stuff
//            for (String key : printExporter.getJasperReportsContext().getProperties().keySet()) {
//                System.out.println(key + " : " + printExporter.getJasperReportsContext().getProperty(key));
//            }
            
            if (interrupted == true) {
                logger.debug("print execute was interrupted, exiting...");
                return false;
            }
            
            
            if (getReport().isShowDialog()) {
                //hack to center the print dialog on classic systems
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        // wait for dialog to become visible
                        try {
                            Thread.sleep(80);
                        } catch (Exception ex) {
                        }
                        for (Window w : JDialog.getWindows()) {
                            if (w != null && w.getClass().getCanonicalName().equals("sun.print.ServiceDialog")) {
                                w.setLocationRelativeTo(getReport().getParent());
                            }
                        }
                    }
                });
            }

            // This blocks until complete
            printExporter.exportReport();
            
            return true;

        } catch (JRException ex) {
            ReportManager.LOGGER.error("Failed to print report!", ex);
            return false;
        }

    }

}
