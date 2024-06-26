package com.ticktockdata.jasper;

import java.util.Date;

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
/**
 *
 * @author default
 */
public class TestPrint extends javax.swing.JFrame {

    /**
     * Creates new form TestPrint
     */
    public TestPrint() {
        initComponents();

        setupConnection();
        
        JasperReportImpl rpt1 = new JasperReportImpl("/home/default/FurnitureLogic/penwood/ClassicUtils/print/Reports/Expense/CheckDispersementsDetail.jrxml");
        JasperReportImpl rpt2 = new JasperReportImpl("/home/default/NetBeans/classic/reports/CustomerListing.jrxml");
        JasperReportImpl rpt3 = new JasperReportImpl("/home/default/JoeWatch/ClassicUtils/A_TimeTrack/TT_TasksToDo.jrxml");
        JasperReportImpl rpt4 = new JasperReportImpl("/home/default/NetBeans/JKennel/reports/ContactInfo.jrxml");
        
        PrintPromptFiller filler = new PrintPromptFiller() {
            @Override
            public boolean fillPrompts(JasperReportImpl report) {
                System.out.println("Filling prompts on " + report.getReportName());
                report.setParameter("START_DATE", new Date(117, 5, 30));
                //report.setParameter("START_DATE", new Date(117, 0, 1));

                report.setParameter("END_DATE", new Date(117, 5, 30));
                //report.setParameter("END_DATE", new Date(117, 11, 31));

                report.setParameter("BANK_ACCOUNT", "1010 - Checking Penwood CSB");
                return true;
            }
        };

        rpt1.addPrintPromptFiller(filler);
        //rpt2.addPrintPromptFiller(filler);

        printButton2.setAdditionalReports(rpt1, rpt2, rpt3, rpt4);

        //printButton2.getJasperReportImp().addPrintPromptFiller(filler);
    }

    private void setupConnection() {

        ConnectionInfo cInfo = new ConnectionInfo();
        cInfo.setDriverJars("/home/default/NetBeans/JasperPrint/lib/postgresql-9.3-1100.jdbc4.jar");
        cInfo.setDriverClass("org.postgresql.Driver");
        cInfo.setUrl("jdbc:postgresql://localhost:5432/joeswcr");
        cInfo.setUser("postgres");
        cInfo.setPassword("true");
        ReportConnectionManager.registerConnection(cInfo, false);

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        printButton2 = new com.ticktockdata.jasper.gui.PrintButton();
        printButton3 = new com.ticktockdata.jasper.gui.PrintButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Print Button Tester");

        printButton2.setReport("/home/default/NetBeans/classic/reports/CustomerListing.jrxml");

        printButton3.setReport("/home/default/NetBeans/classic/reports/CustomerListing.jrxml");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(51, 51, 51)
                .addComponent(printButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(244, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(printButton3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(101, 101, 101))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(37, 37, 37)
                .addComponent(printButton2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(71, 71, 71)
                .addComponent(printButton3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(132, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
//        try {
//            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
//                if ("Nimbus".equals(info.getName())) {
//                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
//                    break;
//                }
//            }
//        } catch (ClassNotFoundException ex) {
//            java.util.logging.Logger.getLogger(TestPrint.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (InstantiationException ex) {
//            java.util.logging.Logger.getLogger(TestPrint.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (IllegalAccessException ex) {
//            java.util.logging.Logger.getLogger(TestPrint.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
//            java.util.logging.Logger.getLogger(TestPrint.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new TestPrint().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.ticktockdata.jasper.gui.PrintButton printButton2;
    private com.ticktockdata.jasper.gui.PrintButton printButton3;
    // End of variables declaration//GEN-END:variables
}
