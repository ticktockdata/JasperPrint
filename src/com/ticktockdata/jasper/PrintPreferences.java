

package com.ticktockdata.jasper;

import com.ticktockdata.jasper.PrintExecutor.Action;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;
import javax.swing.JOptionPane;

/**
 * Each report has settings, and has separate settings for each button + menu
 * option, so a standard button has 3 different settings for the same report.
 * This is accomplished by having each button / menu provide a name for the 
 * setting.
 * @author JAM {javajoe@programmer.net}
 * @since Feb 27, 2019
 */
public class PrintPreferences {
    
    /** Container for ALL the preferences, not just for a single instance */
    private static final Preferences allPrefs = Preferences.userNodeForPackage(PrintPreferences.class);
    
    private String id;
    
    private Map<String, String> prefs = new HashMap<String, String>();
    
    private int copies = 1;
    private String printerName = "Default";
    private boolean showPrintDialog = true;
    private Action printAction = Action.PRINT;
    
    private final String COPIES = "copies";
    private final String PRINTER_NAME = "printerName";
    private final String SHOW_PRINT_DIALOG = "showPrintDialog";
    private final String PRINT_ACTION = "printAction";
    /**
     * Prevents creating instance w/o an Identifier
     */
    private PrintPreferences() {}
    
    
    /**
     * Create a preferences for a give report - ID must uniquely identify a
     * report + button / action
     * @param id 
     */
    public PrintPreferences(String id) {
        
        // replace spaces with - and add an underscore after id
        this.id = id.replace(" ", "-") + "_";
        reload();
        
    }
    
    
    public boolean isShowPrintDialog() {
        return showPrintDialog;
    }
    
    public void setShowPrintDialog(boolean showPrintDialog) {
        this.showPrintDialog = showPrintDialog;
    }
    
    public int getCopies() {
        return copies;
    }
    
    public void setCopies(int copies) {
        this.copies = copies;
    }
    
    public String getPrinterName() {
        return printerName;
    }
    
    
    public void setPrinterName(String printerName) {
        this.printerName = printerName;
    }
    
    
    public Action getPrintAction() {
        return printAction;
    }
    
    
    public void setPrintAction(Action printAction) {
        this.printAction = printAction;
    }
    
    
    /**
     * Loads all the variable for this class. Called by constructor
     */
    public void reload() {
        try {
            
            allPrefs.sync();
            
            for (String k : allPrefs.keys()) {
                if (k.startsWith(id)) {
                    // default value is empty string for all
                    String key = k.replace(id, "");
                    String val = allPrefs.get(k, "");
                    if (val.isEmpty()) continue;    // move on to next
                    if (COPIES.equals(key)) {
                        try {
                            copies = Integer.valueOf(val);
                        } catch (Exception ex) {
                            System.out.println("Error: " + ex.toString());
                            ex.printStackTrace();
                        }
                    } else if (PRINTER_NAME.equals(key)) {
                        setPrinterName(val);
                    } else if (SHOW_PRINT_DIALOG.equals(key)) {
                        showPrintDialog = Boolean.valueOf(val);
                    } else if (PRINT_ACTION.equals(key)) {
                        printAction = Action.fromString(val);
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println("Error loading the Prefs: " + ex.toString());
            ex.printStackTrace();
        }
    }
    
    
    /**
     * Call this method to save changes to local machine (not to a database).
     */
    public void save() {
        
        try {
            // remove all instances of this prefs from allPrefs
            for (String key : allPrefs.keys()) {
                if (key.startsWith(id)) {
                    allPrefs.remove(key);
                }
            }
            
            // now add all the prefs of this class
            allPrefs.put(id+PRINTER_NAME, getPrinterName());
            allPrefs.put(id+SHOW_PRINT_DIALOG, Boolean.valueOf(showPrintDialog).toString());
            allPrefs.put(id+COPIES, Integer.valueOf(copies).toString());
            allPrefs.put(id+PRINT_ACTION, printAction.name());
            
            // save
            allPrefs.flush();
            
        } catch (Exception ex) {
            System.out.println("Error: " + ex.toString());
        }
    }
    
    
    
    @Override
    public String toString() {
        return "PrintPreferences(" + id + ": " 
                + COPIES + "=" + copies + ", " 
                + PRINTER_NAME + "=" + printerName + ", " 
                + SHOW_PRINT_DIALOG + "=" + showPrintDialog + ", "
                + PRINT_ACTION + "=" + printAction.name()
                + ")";
    }
    
    
    
    
    
    
    public static void main(String[] args) {
        long t = System.currentTimeMillis();
        PrintPreferences p = new PrintPreferences("Test Report.jrxml");
        System.out.println(p);
        System.out.println("Time = " + ((System.currentTimeMillis() - t)) + " ms");
        
        p.save();
        
        p.setCopies(3);
        p.setShowPrintDialog(false);
        p.setPrintAction(PrintExecutor.Action.PRINT);
        System.out.println(p);
        
        p.reload();
        
        System.out.println(p);
        System.out.println("Time = " + ((System.currentTimeMillis() - t)) + " ms");
        
        
    }
    
}
