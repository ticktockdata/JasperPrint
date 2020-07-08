/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ticktockdata.jasper.prompts;

import net.sf.jasperreports.engine.JRExpression;
import net.sf.jasperreports.engine.JRParameter;
import org.apache.log4j.Logger;

/**
 *
 * @author JAM {javajoe@programmer.net}
 * @param <T>
 * @since Jan 31, 2019
 */
public abstract class PromptComponent<T> extends javax.swing.JPanel {
    
    protected static final Logger logger = Logger.getLogger(PromptComponent.class);
    private JRParameter parameter;
    protected T promptValue;
    private String description = "";
    

    /**
     * Set the parameter for this PromptComponent
     *
     * @param parameter
     */
    public void setParameter(JRParameter parameter) {

        this.parameter = parameter;
        //System.out.println("Parameter = " + parameter + ", desc = " + (parameter == null ? "(null)" : parameter.getDescription()));
        setDescription(parameter.getDescription());
        JRExpression expr = parameter.getDefaultValueExpression();
        String defVal = (expr == null ? null : expr.getText());
        if (defVal != null && !defVal.trim().isEmpty()) {
//            org.codehaus.groovy.tools.shell.Shell shell = new org.codehaus.groovy.tools.shell.Shell();
            groovy.lang.GroovyShell shell = new groovy.lang.GroovyShell();
    //        sh.execute(description)
            try {
                setPromptValue((T) shell.evaluate(defVal));
            } catch (Exception ex) {
                logger.error("Failed to evaluate the default value", ex);
            }
        }
    }

    /**
     *
     * @return the JRParameter
     */
    public JRParameter getParameter() {
        return parameter;
    }

    /**
     * Returns the name of the Report's PromptComponent
     *
     * @return
     */
    public String getPromptName() {
        return (parameter == null ? "null" : parameter.getName());
    }

    /**
     * Must be implemented by all PromptComponent. This is called when
     *
     * @param value
     */
    abstract public void setPromptValue(T value);

    /**
     * Must be implemented by all PromptComponents
     *
     * @return the value of this PromptComponent
     */
    abstract public T getPromptValue();

    /**
     * This is called by {@link getPromptComponent(JRParameter, Class)}. In
     * order to have the extending prompt display the description text user
     * should override this method like this example (substituting
     * <b>lblDescription</b> for your own component):
     * <p>
     * <code>
     * {@literal @Override}<br>
     * public void setDescription(String description) {<br>
     * &nbsp;&nbsp;super.setDescription(description);<br>
     * &nbsp;&nbsp;lblDescription.setText(description);<br>
     * }</code>
     *
     * @param description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * This is called before the component is displayed. Used to load queries,
     * etc. Override if needed, otherwise does nothing.
     */
    public void refreshData() {

    }

}
