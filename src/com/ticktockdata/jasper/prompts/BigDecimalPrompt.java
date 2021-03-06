/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ticktockdata.jasper.prompts;

import java.math.BigDecimal;
import javax.swing.SwingUtilities;
import net.sf.jasperreports.engine.JRParameter;

/**
 *
 * @author default
 */
public class BigDecimalPrompt extends PromptComponent<BigDecimal> {

    /**
     * Creates new form BigDecimalPrompt
     */
    public BigDecimalPrompt() {
        initComponents();
    }

    @Override
    public void setParameter(JRParameter parameter) {
        super.setParameter(parameter);
        
        String format = null;
        for (String key : parameter.getPropertiesMap().getPropertyNames()) {
            if ("format".equalsIgnoreCase(key)) {
                format = parameter.getPropertiesMap().getProperty(key);
                if (format != null && !format.trim().isEmpty()) {
                    logger.info("Setting Format of BigDecimal to: " + format);
                    ffValue.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat(format))));
                }
                break;
            }
        }
        
    }
    
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblDescription = new javax.swing.JLabel();
        ffValue = new javax.swing.JFormattedTextField();

        lblDescription.setText("Number");

        ffValue.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#,##0.##"))));
        ffValue.setPreferredSize(new java.awt.Dimension(125, 23));
        ffValue.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                ffValueFocusGained(evt);
            }
        });
        ffValue.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                ffValuePropertyChange(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(ffValue, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblDescription, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblDescription)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ffValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void ffValuePropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_ffValuePropertyChange
        
        if ("value".equals(evt.getPropertyName())) {
            setPromptValue(toBigDecimal(evt.getNewValue()));
        }
        
    }//GEN-LAST:event_ffValuePropertyChange

    private void ffValueFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_ffValueFocusGained
        
        SwingUtilities.invokeLater(() -> {
            ffValue.selectAll();
        });
        
    }//GEN-LAST:event_ffValueFocusGained

    
    private BigDecimal toBigDecimal(Object arg0) {
        try {
            return new BigDecimal(arg0.toString());
        } catch (Exception e){
            logger.warn("ERROR: " + e.getLocalizedMessage(), e);
            return BigDecimal.ZERO;
        }
    }
    
    
    @Override
    public void setPromptValue(BigDecimal value) {
        promptValue = value;
        ffValue.setValue(value);
    }

    @Override
    public BigDecimal getPromptValue() {
        return promptValue;
    }
    
    
    @Override
    public void setDescription(String description) {
        super.setDescription(description);
        lblDescription.setText(description);
    }
    
    
    @Override
    public void refreshData() {
        
        System.out.println("refreshData() for BigDecimalPrompt - do nothing.");
        
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JFormattedTextField ffValue;
    private javax.swing.JLabel lblDescription;
    // End of variables declaration//GEN-END:variables
}
