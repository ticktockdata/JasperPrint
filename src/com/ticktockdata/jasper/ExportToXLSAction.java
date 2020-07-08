/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ticktockdata.jasper;

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsReportConfiguration;

/**
 *
 * @author JAM {javajoe@programmer.net}
 * @since Apr 03, 2019
 */
public class ExportToXLSAction extends PrintExecutor {
    
    private boolean canceled = false;
    private File lastFile;
    
    public ExportToXLSAction(JasperReportImpl report) {
        super(report);
    }

    @Override
    public Action getAction() {
        return Action.EXPORT_TO_CSV;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public boolean execute(JasperPrint print) {
        
        try {
            
            String filePath = getReport().getExportFilePath();
            if (filePath == null) {
                // allow user to choose
                JFileChooser picker = new JFileChooser();
                picker.setDialogType(JFileChooser.SAVE_DIALOG);
                picker.setDialogTitle("Save Excel to");
                
                picker.setAcceptAllFileFilterUsed(true);
                
                FileFilter filter = new FileFilter() {
                    @Override
                    public boolean accept(File f) {
                        if (f.isDirectory()) return true;
                        try {
                            System.out.println(f.getName() + " = " + f.toURI().toURL().openConnection().getContentEncoding());
                            return f.getName().toLowerCase().endsWith(".xls");
                        } catch (Exception x) {
                            return false;
                        }
                    }

                    @Override
                    public String getDescription() {
                        return "Excel files (*.xls)";
                    }
                };
                
                picker.addChoosableFileFilter(filter);
                picker.setFileFilter(filter);
                if(lastFile != null) {
                    picker.setSelectedFile(lastFile);
                }
                System.out.println("Parent = " + getReport().getParent());
                if (canceled) return false;
                int rslt = picker.showSaveDialog(getReport().getParent());
                if (canceled) return false;
                
                if (rslt != JFileChooser.APPROVE_OPTION) {
                    logger.info("User canceled file selection");
                    return false;
                }
                
                
                File selectedFile = picker.getSelectedFile();
                
                if (selectedFile == null) {
                    logger.warn("Selected File is null (should not happen)");
                    return false;
                } else {
                    lastFile = selectedFile;
                }
                
                filePath = selectedFile.getAbsolutePath();
                if (!filePath.toLowerCase().endsWith(".xls")) {
                    filePath += ".xls";
                }
                
            }   // end of file selection routine
            
            File file = new File(filePath);
            if (canceled) return false;
            // confirm that it does not exist
            if (!getReport().isOverwriteExportFile() && file.exists()) {
                if (JOptionPane.showConfirmDialog(getReport().getParent(), "File already exists, do you want to overwrite?", "Confirm:", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
                    return false;
                }
            }
            
            JRXlsExporter exporter = new JRXlsExporter();
            
            exporter.setExporterInput(new SimpleExporterInput(print));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(file));

            SimpleXlsReportConfiguration config = new SimpleXlsReportConfiguration();
            config.setDetectCellType(true);
            
            exporter.setConfiguration(config);
            
            if (canceled) return false;
            exporter.exportReport();
            
            
            // clear the report path after exporting
            getReport().setExportFilePath(null);
            
        } catch (Exception ex) {
            logger.error("Error while exporting to PDF: " + ex, ex);
            return false;
        }
        
        return true;
    }

    @Override
    public void cancelExecute() {
        canceled = true;
    }

}
