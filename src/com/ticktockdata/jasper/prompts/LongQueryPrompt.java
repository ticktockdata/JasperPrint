/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ticktockdata.jasper.prompts;

import classicacctapp.AutoComplete;
import javax.swing.JComboBox;
import javax.swing.JLabel;

/**
 * This prompt provides a Combo Box control with a QueryComboBoxModel.  It 
 * requires that the supplied SQL text returns a Long value as the 2nd column.
 * @see SQLQueryPrompt
 * @author JAM {javajoe@programmer.net}
 */
public class LongQueryPrompt extends SQLQueryPrompt<Long> {

    /**
     * Creates new form QueryPrompt
     */
    public LongQueryPrompt() {
        initComponents();
        AutoComplete.enable(comboBox);
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
        comboBox = new javax.swing.JComboBox<>();

        lblDescription.setText("Select Value");
        lblDescription.setPreferredSize(new java.awt.Dimension(250, 15));

        comboBox.setToolTipText("");
        comboBox.setPreferredSize(new java.awt.Dimension(250, 25));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblDescription, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(comboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblDescription, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(comboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    

    @Override
    protected JComboBox getComboBox() {
        return comboBox;
    }

    @Override
    protected JLabel getDescriptionLabel() {
        return lblDescription;
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> comboBox;
    private javax.swing.JLabel lblDescription;
    // End of variables declaration//GEN-END:variables
}
