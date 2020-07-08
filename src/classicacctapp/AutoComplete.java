/*
 * AutoCompletion.java
 *
 * Created on July 17, 2006, 11:23 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package classicacctapp;

/**
 * This lifted from Classic Accounting!
 * @author daniel
 */
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.*;
import javax.swing.text.*;
import org.apache.log4j.Logger;

/* This work is hereby released into the Public Domain.
 * To view a copy of the public domain dedication, visit
 * http://creativecommons.org/licenses/publicdomain/
 */
public class AutoComplete extends PlainDocument {
    private static Logger logger = Logger.getLogger(AutoComplete.class);

    JComboBox comboBox;
    ComboBoxModel model;
    JTextComponent editor;
    // flag to indicate if setSelectedItem has been called
    // subsequent calls to remove/insertString should be ignored
    protected boolean selecting=false;
    boolean hidePopupOnFocusLoss;
    boolean hitBackspace=false;
    boolean hitBackspaceOnSelection;
    
    KeyListener editorKeyListener;
    FocusListener editorFocusListener;
    FocusListener customEditFocusListener;
    
    public AutoComplete(final JComboBox comboBox, FocusListener customEditFocusListener) {
        this.comboBox = comboBox;
        model = comboBox.getModel();
        this.customEditFocusListener = customEditFocusListener;
        comboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!selecting) highlightCompletedText(0);
            }
        });
        comboBox.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {                
                if (e.getPropertyName().equals("editor")) configureEditor((ComboBoxEditor) e.getNewValue());
                if (e.getPropertyName().equals("model")) model = (ComboBoxModel) e.getNewValue();
            }
        });
        
        editorKeyListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {              
                //if (comboBox.isDisplayable()) comboBox.setPopupVisible(true);
                hitBackspace=false;
                switch (e.getKeyCode()) {
                    // determine if the pressed key is backspace (needed by the remove method)
                    case KeyEvent.VK_BACK_SPACE :
                        //hitBackspace=true;
                        //hitBackspaceOnSelection=editor.getSelectionStart()!=editor.getSelectionEnd();
                        comboBox.setSelectedIndex(-1);
                        //comboBox.updateUI();
                        break;
                    // ignore delete key
                    case KeyEvent.VK_DELETE :                        
                        comboBox.setSelectedIndex(-1);
                        //comboBox.updateUI();
                    break;                    
                }              
            }
        };
        // Bug 5100422 on Java 1.5: Editable JComboBox won't hide popup when tabbing out
        hidePopupOnFocusLoss=System.getProperty("java.version").startsWith("1.5");
        // Highlight whole text when gaining focus
        editorFocusListener = new FocusAdapter() {            
            @Override
            public void focusGained(FocusEvent e) {
                highlightCompletedText(0);
            }
            @Override
            public void focusLost(FocusEvent e) {
                // Workaround for Bug 5100422 - Hide Popup on focus loss
                if (hidePopupOnFocusLoss) comboBox.setPopupVisible(false);
            }
        };
        configureEditor(comboBox.getEditor());
        // Handle initially selected object
        Object selected = comboBox.getSelectedItem();
        if (selected!=null) setText(selected.toString());
        highlightCompletedText(0);
    }
    
    public static void enable(JComboBox comboBox) {
        // has to be editable
        comboBox.setEditable(true);
        // change the editor's document
        new AutoComplete(comboBox, null);
    }

    public static void enable(JComboBox comboBox, FocusListener customFocusListener) {
        // has to be editable
        comboBox.setEditable(true);
        // change the editor's document
        new AutoComplete(comboBox, customFocusListener);
    }
    
    public static void loadListBox(javax.swing.JComboBox _c, java.util.Vector _v){
        try{
            if(_v == null){
                _c.setEnabled(false);
                return;
            }else{
                _c.setEnabled(true);
            }
            for(int i = 0; i < _v.size(); i++){
                _c.addItem(_v.elementAt(i));
            }
        }catch(Exception e){
            logger.error("", e);
        }
    }
    
    public static void loadListBox(javax.swing.JComboBox _c, java.util.List list){
        try{
            if(list == null){
                _c.setEnabled(false);
                return;
            }else{
                _c.setEnabled(true);
            }
            
            for(int i = 0; i < list.size(); i++){
                _c.addItem(list.get(i));
            }
        }catch(Exception e){
            logger.error("", e);
        }
    }
    
    void configureEditor(ComboBoxEditor newEditor) {
        if (editor != null) {
            editor.removeKeyListener(editorKeyListener);
            editor.removeFocusListener(editorFocusListener);
            if(customEditFocusListener != null){
                editor.removeFocusListener(customEditFocusListener);
            }
        }
        
        if (newEditor != null) {
            editor = (JTextComponent) newEditor.getEditorComponent();
            editor.addKeyListener(editorKeyListener);
            editor.addFocusListener(editorFocusListener);
            if(customEditFocusListener != null){
                editor.addFocusListener(customEditFocusListener);
            }
            editor.setDocument(this);
        }
    }
    
    @Override
    public void remove(int offs, int len) throws BadLocationException {
        // return immediately when selecting an item
        if (selecting) return;
        if (hitBackspace) {
            // user hit backspace => move the selection backwards
            // old item keeps being selected
            if (offs>0) {
                if (hitBackspaceOnSelection) offs--;
            } else {
                // User hit backspace with the cursor positioned on the start => beep
                comboBox.getToolkit().beep(); // when available use: UIManager.getLookAndFeel().provideErrorFeedback(comboBox);
            }
            highlightCompletedText(offs);
        } else {
            super.remove(offs, len);
        }
    }
    
    @Override
    public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
        // return immediately when selecting an item
        if (selecting) return;
        // insert the string into the document
        super.insertString(offs, str, a);
        // lookup and select a matching item
        Object item = lookupItem(getText(0, getLength()));

        if(item == null){
            //try to find a match by inserting space before last character
            str = new StringBuilder(getText(0, getLength())).insert(getLength() - 1, " ").toString();            
            item = lookupItem(str);
            if(item != null){
                offs--;
            }
        }
        
        if (item != null) {
            setSelectedItem(item);           
        } else {
            // keep old item selected if there is no match
            item = comboBox.getSelectedItem();
            
            // imitate no insert (later on offs will be incremented by str.length(): selection won't move forward)
            offs = offs-str.length();
            // provide feedback to the user that his input has been received but can not be accepted
            comboBox.getToolkit().beep(); // when available use: UIManager.getLookAndFeel().provideErrorFeedback(comboBox);            
        }        
        
        if(item != null){
            setText(item.toString());
        }else{
            setText("");
        }
        
        // select the completed part
        highlightCompletedText(offs+str.length());
    }
    
    private void setText(String text) {
        try {
            // remove all text and insert the completed string
            super.remove(0, getLength());
            super.insertString(0, text, null);
        } catch (BadLocationException e) {
            throw new RuntimeException(e.toString());
        }
    }
    
    private void highlightCompletedText(int start) {
        try {                     
            editor.setCaretPosition(getLength());
            editor.moveCaretPosition(start);
        } catch(IllegalArgumentException ex) {
        }
    }
    
    protected void setSelectedItem(Object item) {        
        selecting = true;
        model.setSelectedItem(item);        
        selecting = false;        
    }
    
    protected Object lookupItem(String pattern) {
        Object selectedItem = model.getSelectedItem();
        // only search for a different item if the currently selected does not match
        if (selectedItem != null && startsWithIgnoreCase(selectedItem.toString(), pattern) && !(selectedItem instanceof String)) {             
            return selectedItem;
        } else {       
            // iterate over all items
            for (int i=0, n=model.getSize(); i < n; i++) {
                Object currentItem = model.getElementAt(i);
                //current item starts with the pattern?
                if (currentItem != null && startsWithIgnoreCase(currentItem.toString(), pattern)) {
                    return currentItem;
                }
            }
        }
        
        //no item starts with the pattern => return null
        return null;
    }
    
    // checks if str1 starts with str2 - ignores case
    protected boolean startsWithIgnoreCase(String str1, String str2) {
        return str1.toUpperCase().startsWith(str2.toUpperCase());
    }
}

