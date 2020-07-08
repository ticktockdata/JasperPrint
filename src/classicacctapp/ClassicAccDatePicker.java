package classicacctapp;

import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.swing.SwingUtilities;
import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXDatePicker;

/**
 * Adds a custom listener to deal with date formatting where input is 1/1/15 and
 * you want date to be 1/1/2015
 *
 * @author jaz
 */
public class ClassicAccDatePicker extends JXDatePicker {

    private static final Logger logger = Logger.getLogger(ClassicAccDatePicker.class);

    public ClassicAccDatePicker() {

        this.addPropertyChangeListener((PropertyChangeEvent evt) -> {
            fixDate();
        });

        // KeyListener added to add year to abbreviated text (2016-04-21, JAM)
        getEditor().addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent ke) {
                // Only run when Enter pressed //or Tab, ITH Modifications on 2020-02-27 for build 2020.2
                if (ke.getKeyChar() == KeyEvent.VK_ENTER || ke.getKeyChar() == KeyEvent.VK_TAB) {
                    addYear();
                }
            }
        });

        // FocusListener added to add year to abbreviated text (2016-04-21, JAM)
        getEditor().addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent fe) {
                addYear();
            }

            @Override
            public void focusGained(FocusEvent fe) {
                // Select text when entered, added 2016-08-17, JAM
                SwingUtilities.invokeLater(() -> {
                    getEditor().setCaretPosition(0);
                    int len = getEditor().getText().length();
                    if (len > 0) {
                        getEditor().moveCaretPosition(len);
                    }
                });

            }
        });
    }

    /**
     * This method does sets year to current if date typed w/o year.<br>
     * Also parses date if entered with period or dash instead of slash.<br>
     * Should not be any need for this method to be public.<br>
     * Needed to trap this before the PropertyChange event, couldn't accomplish
     * it with single event, used keyPressed and focusLost.<br>
     *
     * @author JAM
     * @since 2016-04-21, version 2.4.2.1
     */
    private void addYear() {

        // Get the control's text
        String sText = getEditor().getText();

        if (sText == null || sText.equals("")) {
            setDate(null);
            return;
        }
        if (sText.matches(".*/.*/..*")) {
            return;
        } // exit ASAP if valid!

        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yy");
        String current = format.format(Calendar.getInstance().getTime());
        String a1[] = null;

        // Use regexp to replace all '.' and '-' characters with a '/'
        sText = sText.replaceAll("[.-]", "/");

        // Try splitting string by /
        if (sText.contains("/")) {
            a1 = sText.split("/");
        }

        // If split done, then generate to date
        if (a1 != null) {   // if split, then parse to date
            switch (a1.length) {
                case 3:
                    sText = a1[0] + "/" + a1[1] + "/" + a1[2];
                    break;
                case 2:
                    sText = a1[0] + "/" + a1[1] + "/" + current.split("/")[2];
                    break;
                default:
                    return;  // parse error, do nothing
            }
            getEditor().setText(sText);
        } else if (sText.toLowerCase().contains("today")
                || sText.toLowerCase().contains("now")) {
            // If not splittable, then check if now or today
            sText = current;
            getEditor().setText(current);
        }

    }

    private void fixDate() {
        Date current = getDate();
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 1970);
        Date cutoff = cal.getTime();
        if (current != null && current.compareTo(cutoff) < 0) {
            SimpleDateFormat format = new SimpleDateFormat("MM/dd/yy");
            String currentFormat = format.format(current);
            try {
                Date good = format.parse(currentFormat);
                setDate(good);
            } catch (ParseException ex) {
                logger.error("", ex);
            }
        }
    }

}
