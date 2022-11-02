package com.ticktockdata.jasper;

import com.ticktockdata.jasper.PrintStatusEvent.StatusCode;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.export.SimpleCsvExporterConfiguration;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleWriterExporterOutput;

/**
 *
 * @author JAM
 * @since Apr 03, 2019
 */
public class ExportToCSVAction extends PrintExecutor {

    private boolean canceled = false;
    private static File lastFile;

    public ExportToCSVAction(JasperReportImpl report) {
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
                picker.setDialogTitle("Save CSV to");

                picker.setAcceptAllFileFilterUsed(true);

                FileFilter filter = new FileFilter() {
                    @Override
                    public boolean accept(File f) {
                        if (f.isDirectory()) {
                            return true;
                        }
                        try {
                            return f.getName().toLowerCase().endsWith(".csv");
                        } catch (Exception x) {
                            return false;
                        }
                    }

                    @Override
                    public String getDescription() {
                        return "CSV files (*.csv)";
                    }
                };

                picker.addChoosableFileFilter(filter);
                picker.setFileFilter(filter);
                if (lastFile != null) {
                    picker.setSelectedFile(lastFile);
                }
                System.out.println("Parent = " + getReport().getParent());
                if (canceled) {
                    return false;
                }
                int rslt = picker.showSaveDialog(getReport().getParent());
                if (canceled) {
                    return false;
                }

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
                if (!filePath.toLowerCase().endsWith(".csv")) {
                    filePath += ".csv";
                }

            }   // end of file selection routine

            File file = new File(filePath);
            if (canceled) {
                return false;
            }
            // confirm that it does not exist
            if (!getReport().isOverwriteExportFile() && file.exists()) {
                if (JOptionPane.showConfirmDialog(getReport().getParent(), "File already exists, do you want to overwrite?", "Confirm:", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
                    cancelExecute();
                    return false;
                }
            }
            JRCsvExporter exporter = new JRCsvExporter();

            exporter.setExporterInput(new SimpleExporterInput(print));
            exporter.setExporterOutput(new SimpleWriterExporterOutput(file));

            SimpleCsvExporterConfiguration config = new SimpleCsvExporterConfiguration();

            System.out.println("Configuraton: FieldDelimiter = $$" + config.getFieldDelimiter() + "$$, enclosure = $$" + config.getFieldEnclosure() + "$$, record delimiter = $$" + config.getRecordDelimiter());
            exporter.setConfiguration(config);

            if (canceled) {
                return false;
            }
            exporter.exportReport();

            // clear the report path after exporting
            getReport().setExportFilePath(null);

        } catch (Exception ex) {
            logger.error("Error while exporting to PDF: " + ex, ex);
            getReport().firePrintStatusChanged(StatusCode.ERROR);
            return false;
        } finally {
            if (canceled) {
                getReport().firePrintStatusChanged(StatusCode.CANCELED);
            }
        }

        return !canceled;
    }

    @Override
    public void cancelExecute() {
        canceled = true;
    }

}
