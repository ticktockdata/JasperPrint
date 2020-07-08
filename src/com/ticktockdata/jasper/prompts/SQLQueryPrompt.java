/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ticktockdata.jasper.prompts;

import com.ticktockdata.jasper.ConnectionManager;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import net.sf.jasperreports.engine.JRParameter;
import org.apache.log4j.Logger;

/**
 * This class is a Generic root class for creating a Combo Box control that
 * is populated via SQL command.  The first column of the SQL output is always
 * displayed, and must be a String.  The second column is the value returned
 * by the control, and must be of type T.
 * @see PromptComponentFactory
 * @author JAM {javajoe@programmer.net}
 * @param <T>
 * @since Sep 12, 2019
 */
public abstract class SQLQueryPrompt<T> extends PromptComponent<T> {
    
    
    @Override
    public void setDescription(String description) {
        super.setDescription(description);
        getDescriptionLabel().setText(description);
    }
    
    
    @Override
    public void setPromptValue(T value) {
        if (getComboBox().getModel() instanceof QueryComboBoxModel) {
            getComboBox().getModel().setSelectedItem(value);
            ((QueryComboBoxModel<String>)getComboBox().getModel()).setSelectedItem(value);
        } else {
            getComboBox().setSelectedItem(value);
        }
    }

    @Override
    public T getPromptValue() {
        if (getComboBox().getModel() instanceof QueryComboBoxModel) {
            return ((QueryComboBoxModel<T>)getComboBox().getModel()).getSelectedValue();
        } else {
            return (T) getComboBox().getSelectedItem();
        }
    }
    
    
    @Override
    public void setParameter(JRParameter parameter) {
        super.setParameter(parameter);
        String queryText = PromptComponentFactory.getProperty(parameter, "Query");
        groovy.lang.GroovyShell shell = new groovy.lang.GroovyShell();
        String defValExpression = parameter.getDefaultValueExpression().getText();
        T defVal = null;
        try {
            if (defValExpression != null && !defValExpression.trim().isEmpty()) {
                defVal = (T) shell.evaluate(defValExpression);
            }
        } catch (Exception ex) {
            Logger.getLogger(this.getClass()).error("Error evaluating default expression: " + defValExpression, ex);
            defVal = null;
        }
        parameter.getDefaultValueExpression();
        QueryComboBoxModel<T> model = new QueryComboBoxModel<>(ConnectionManager.DEFAULT_CONNECTION_NAME, getComboBox(), queryText, defVal);
        getComboBox().setModel(model);
        
    }
    
    
    @Override
    public void refreshData() {
        ((QueryComboBoxModel)getComboBox().getModel()).refreshData();
    }
    
    
    /**
     * Must be overwritten by implementing class, for access to the combo box.
     * @return may not be null
     */
    protected abstract JComboBox getComboBox();
    
    /**
     * Must be overwritten by implementing class, for access to the label
     * @return may not be null
     */
    protected abstract JLabel getDescriptionLabel();
    
    
}
