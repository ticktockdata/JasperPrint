/*
 * JasperReports - Free Java Reporting Library.
 * Copyright (C) 2001 - 2016 TIBCO Software Inc. All rights reserved.
 * http://www.jaspersoft.com
 *
 * Unless you have purchased a commercial license agreement from Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of JasperReports.
 *
 * JasperReports is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JasperReports is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JasperReports. If not, see <http://www.gnu.org/licenses/>.
 */

 /*
 * Contributors:
 * Ryan Johnson - delscovich@users.sourceforge.net
 * Carlton Moore - cmoore79@users.sourceforge.net
 */
package com.ticktockdata.jasper;

import classicacctapp.JasperKeyBinder;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.InputStream;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;
import net.sf.jasperreports.engine.DefaultJasperReportsContext;
import net.sf.jasperreports.engine.JRConstants;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.engine.type.OrientationEnum;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimplePrintServiceExporterConfiguration;
import net.sf.jasperreports.swing.JRViewerController;
import net.sf.jasperreports.swing.JRViewerToolbar;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 */
/**
 * This class uses the {@link net.sf.jasperreports.swing.JRViewer} component to
 * display reports. It represents a simple Java Swing application that can load
 * and display reports. It is used in almost all of the supplied samples to
 * display the generated documents.
 * <p>
 * This class copied and modified by JAM {javajoe@programmer.net} on Aug 29,
 * 2018. Primary purpose is to get rid of the annoying fact that Viewer window
 * would display even if there were no pages. Also removed the isExitOnClose
 * option - we NEVER want to terminate the application if the viewer window is
 * closed! Further revised so viewReport returns the JasperViewer so we have a
 * 'handle' on it to close / cancel report by calling exitForm()
 *
 * @author Teodor Danciu (teodord@users.sourceforge.net)
 */
public class JasperViewer extends javax.swing.JFrame {

    private Log log = LogFactory.getLog(JasperViewer.class);

    private static final long serialVersionUID = JRConstants.SERIAL_VERSION_UID;

    /**
     *
     */
    protected net.sf.jasperreports.swing.JRViewer viewer;

    /**
     * We must un-register the window listener and set disposeLister to null on
     * closing, or it will keep the JasperViewer from Garbage Collection.
     */
    private java.awt.event.WindowAdapter disposeListener = new java.awt.event.WindowAdapter() {
        @Override
        public void windowClosing(java.awt.event.WindowEvent evt) {
            System.out.println("Calling exitForm() on JasperViewer");
            exitForm();
        }
    };

    /**
     * @param sourceFile
     * @param isXMLFile
     * @throws net.sf.jasperreports.engine.JRException
     * @see #JasperViewer(JasperReportsContext, String, boolean, boolean,
     * Locale, ResourceBundle)
     */
    public JasperViewer(
            String sourceFile,
            boolean isXMLFile
    ) throws JRException {
        this(sourceFile, isXMLFile, null);
    }

    /**
     * @param is
     * @param isXMLFile
     * @throws net.sf.jasperreports.engine.JRException
     * @see #JasperViewer(JasperReportsContext, InputStream, boolean, boolean,
     * Locale, ResourceBundle)
     */
    public JasperViewer(
            InputStream is,
            boolean isXMLFile
    ) throws JRException {
        this(is, isXMLFile, null);
    }

    /**
     * @see #JasperViewer(JasperReportsContext, JasperPrint, boolean, Locale,
     * ResourceBundle)
     */
    public JasperViewer(
            JasperPrint jasperPrint
    ) {
        this(jasperPrint, null);
    }

    /**
     * @see #JasperViewer(JasperReportsContext, String, boolean, boolean,
     * Locale, ResourceBundle)
     */
    public JasperViewer(
            String sourceFile,
            boolean isXMLFile,
            Locale locale
    ) throws JRException {
        this(
                DefaultJasperReportsContext.getInstance(),
                sourceFile,
                isXMLFile,
                locale,
                null
        );
    }

    /**
     * @see #JasperViewer(JasperReportsContext, InputStream, boolean, boolean,
     * Locale, ResourceBundle)
     */
    public JasperViewer(
            InputStream is,
            boolean isXMLFile,
            Locale locale
    ) throws JRException {
        this(
                DefaultJasperReportsContext.getInstance(),
                is,
                isXMLFile,
                locale,
                null
        );
    }

    /**
     * @see #JasperViewer(JasperReportsContext, JasperPrint, boolean, Locale,
     * ResourceBundle)
     */
    public JasperViewer(
            JasperPrint jasperPrint,
            Locale locale
    ) {
        this(
                DefaultJasperReportsContext.getInstance(),
                jasperPrint,
                locale,
                null
        );
    }

    /**
     *
     */
    public JasperViewer(
            JasperReportsContext jasperReportsContext,
            String sourceFile,
            boolean isXMLFile,
            Locale locale,
            ResourceBundle resBundle
    ) throws JRException {
        if (locale != null) {
            setLocale(locale);
        }

        initComponents();

        this.viewer = new net.sf.jasperreports.swing.JRViewer(jasperReportsContext, sourceFile, isXMLFile, locale, resBundle);
        this.pnlMain.add(this.viewer, BorderLayout.CENTER);

        addKeyBinders();

    }

    /**
     *
     */
    public JasperViewer(
            JasperReportsContext jasperReportsContext,
            InputStream is,
            boolean isXMLFile,
            Locale locale,
            ResourceBundle resBundle
    ) throws JRException {
        if (locale != null) {
            setLocale(locale);
        }

        initComponents();

        this.viewer = new net.sf.jasperreports.swing.JRViewer(jasperReportsContext, is, isXMLFile, locale, resBundle);
        this.pnlMain.add(this.viewer, BorderLayout.CENTER);

        addKeyBinders();

    }

    /**
     *
     */
    public JasperViewer(
            JasperReportsContext jasperReportsContext,
            JasperPrint jasperPrint,
            Locale locale,
            ResourceBundle resBundle
    ) {
        if (locale != null) {
            setLocale(locale);
        }

        initComponents();

        this.viewer = new net.sf.jasperreports.swing.JRViewer(jasperReportsContext, jasperPrint, locale, resBundle);

        this.jasperPrint = jasperPrint;
        this.pnlMain.add(this.viewer, BorderLayout.CENTER);

        addKeyBinders();
    }

    /**
     * JasperPrint needs to be accessible from the printActionListener
     */
    private JasperPrint jasperPrint = null;

    /**
     * Need access to the button, so we can remove the listener
     */
    javax.swing.JButton btnPrint = null;

    /**
     * This is a 'hack' to force the Print button's action to occur on the event
     * dispatch thread, otherwise it may be 0 size on some Look & Feel. Also
     * added other key bindings to
     *
     * @param jasperPrint
     */
    private void addKeyBinders() {

        try {
            btnPrint = (javax.swing.JButton) getToolBarComponent("btnPrint");

            // only one listener attached, so we shouldn't get a concurrent access error
            for (ActionListener listener : btnPrint.getActionListeners()) {
                btnPrint.removeActionListener(listener);
            }

            btnPrint.addActionListener(printActionListener);
            ((JButton) getToolBarComponent("btnSave")).addActionListener(saveActionListener);

            // Add Escape listener - close with escape (JAM, 9/12/19)
            new JasperKeyBinder(viewer, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0)) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    exitForm();
                }
            };

            // Add Ctrl+W listener - close window (JAM, 9/12/19)
            new JasperKeyBinder(viewer, KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyEvent.CTRL_DOWN_MASK)) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    exitForm();
                }
            };

            // Add Print listener - trigger print action on Ctrl+P (JAM, 9/12/19)
            new JasperKeyBinder(viewer, KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_DOWN_MASK)) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    ((JButton) getToolBarComponent("btnPrint")).doClick();
                }
            };

            // Add Save listener - trigger save action on Ctrl+S (JAM, 9/12/19)
            new JasperKeyBinder(viewer, KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK)) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    ((JButton) getToolBarComponent("btnSave")).doClick();
                }
            };

            // This needs to be done after components initialized
            // Added 2021-07-07, JAM
            setSizeAndLocation();

        } catch (Exception ex) {
            log.error("Unable to hack!", ex);
        }

    }

    /**
     * Added 2021-07-07, centers the Save dialog to frame
     */
    private ActionListener saveActionListener = new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
            
            //hack to center the print dialog on classic systems
            new Thread(() -> {
                try {
                    log.trace("Starting thread for saveActionListener ...");
                    // wait for dialog to become visible
                    int ct = 0;
                    sleeper:
                    while (true) {
                        Thread.sleep(20);
                        for (Window w : JDialog.getWindows()) {
                            if (w != null && w instanceof JDialog && w.isVisible() && "Save".equals(((JDialog) w).getTitle())) {
                                break sleeper;
                            }
                        }
                        if (++ct > 50) {
                            log.debug("count exceeded, breaking...");
                            break;
                        }
                    }
                    
                } catch (Exception ex) {
                    log.warn("Error while waiting for dialog!", ex);
                }
                SwingUtilities.invokeLater(() -> {
                    try {
                        for (Window w : JDialog.getWindows()) {
                            if (w != null && w instanceof JDialog && w.isVisible() && "Save".equals(((JDialog) w).getTitle())) {
                                w.setLocationRelativeTo(JasperViewer.this);
                                break;
                            }
                        }
                    } catch (Exception ex) {
                        log.warn("Failed to center the Save dialog!", ex);
                    }
                });
            }).start();

        }
    };

    /**
     * This listener is attached to the print button and invokes the print
     * dialog if triggered
     */
    private ActionListener printActionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {

                // Create PrintServiceExporter
                SimplePrintServiceExporterConfiguration config = new SimplePrintServiceExporterConfiguration();

                // set if we want to use the print dialog or not
                config.setDisplayPrintDialog(true);

                // create Print Exporter
                JRPrintServiceExporter printExporter = new JRPrintServiceExporter();

                // add the printing data (doc) to the exporter
                printExporter.setExporterInput(new SimpleExporterInput(jasperPrint));
                // set configuration to printExporter
                printExporter.setConfiguration(config);

                //hack to center the print dialog on classic systems
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        // wait for dialog to become visible
                        try {
                            Thread.sleep(80);
                        } catch (Exception ex) {
                        }
                        for (Window w : JDialog.getWindows()) {
                            if (w != null && w.getClass().getCanonicalName().equals("sun.print.ServiceDialog")) {
                                w.setLocationRelativeTo(JasperViewer.this);
                            }
                        }
                    }
                });

                // This blocks until complete
                printExporter.exportReport();

            } catch (Exception ex) {
                log.error("Error while printing...", ex);
            }
        }
    };

    /**
     * Enables this class to access some otherwise protected controls
     *
     * @since 2019-09-12, JAM
     * @param componentName
     * @return
     */
    private JComponent getToolBarComponent(String componentName) {
        try {

            // get access to toolbar
            java.lang.reflect.Field toolBarField = viewer.getClass().getDeclaredField("tlbToolBar");
            toolBarField.setAccessible(true);
            JRViewerToolbar toolBar = (JRViewerToolbar) toolBarField.get(viewer);

            // get access to the component
            java.lang.reflect.Field field = toolBar.getClass().getDeclaredField(componentName);
            field.setAccessible(true);
            return (javax.swing.JComponent) field.get(toolBar);

        } catch (Exception ex) {
            log.error("Failed to access toolbar component " + componentName, ex);
            return null;
        }
    }

    /**
     *
     */
    public JasperViewer(
            JasperReportsContext jasperReportsContext,
            String sourceFile,
            boolean isXMLFile
    ) throws JRException {
        this(jasperReportsContext, sourceFile, isXMLFile, null, null);
    }

    /**
     *
     */
    public JasperViewer(
            JasperReportsContext jasperReportsContext,
            InputStream is,
            boolean isXMLFile
    ) throws JRException {
        this(jasperReportsContext, is, isXMLFile, null, null);
    }

    /**
     *
     */
    public JasperViewer(
            JasperReportsContext jasperReportsContext,
            JasperPrint jasperPrint
    ) {
        this(jasperReportsContext, jasperPrint, null, null);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    private void initComponents() {
        pnlMain = new javax.swing.JPanel();

        setTitle("JasperViewer");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setIconImage(new javax.swing.ImageIcon(getClass().getResource("/net/sf/jasperreports/view/images/jricon.GIF")).getImage());
        addWindowListener(disposeListener);

        pnlMain.setLayout(new java.awt.BorderLayout());

        getContentPane().add(pnlMain, java.awt.BorderLayout.CENTER);

        pack();

    }

    private void setSizeAndLocation() {

        GraphicsDevice screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().getDevice();
        Dimension screenSize = screen.getDefaultConfiguration().getBounds().getSize();

        int height = 0;
        int width = 0;

        // default viewer size is wd = 1000, ht = 733
        if (OrientationEnum.LANDSCAPE.equals(jasperPrint.getOrientationValue())) {
            // landscape format - viewer ht of 733 is a bit short for this, and should also be wider
            log.trace("Landscape format, setting width and height, was " + width + "x" + height);
            width = (int) ((float) Math.min(1200, screenSize.width - 60));
            height = (int) ((float) Math.min(950, screenSize.height - 80));
        } else {
            // portrait format - viewer wd of 1000 is good for this, but should be taller 
            log.trace("Portrait format, setting Height, was " + height);
            width = (int) ((float) Math.min(920, screenSize.width - 60));
            height = (int) ((float) Math.min(1200, screenSize.height - 80));
        }

        java.awt.Dimension dimension = new java.awt.Dimension(width, height);
        setSize(dimension);
        // alter location upward to compensate for documents bar at bottom of screen
        setLocation((screenSize.width - width) / 2, ((screenSize.height - height) / 2) - 20);

        setFocusToComponent((JComponent) viewer.getRootPane().getContentPane(), (JComponent) rootPane.getContentPane(), 50);

    }

    private long focusCount = 1;

    /**
     * Method to attempt focusing a component on opening a screen
     * <p>
     * This method revised to allow a delay before trying to focus.
     *
     * @param comp
     * @param secondary  alternate component - may be null
     * @param waitMillis number of Milliseconds to wait before trying to set
     *                   focus. v2019.1 - 2019-04-08, JAM
     * @since v2018.1 - 2018-03-06, JAM
     * @see setFocusToComponent(JComponent, JComponent)
     */
    public void setFocusToComponent(final JComponent comp, final JComponent secondary, final int waitMillis) {

        // prevent this number from getting too high
        if (focusCount >= Integer.MAX_VALUE) {
            focusCount = 1;
        }

        Thread focusThread = new Thread() {
            @Override
            public void run() {

                // added wait option, 
                if (waitMillis > 0) {
                    try {
                        Thread.sleep(waitMillis);
                    } catch (Exception ex) {
                    }
                }

                try {
                    // find panel
                    Container temp = comp;
                    while (!(temp instanceof JPanel || temp instanceof JRootPane) && temp.getParent() != null) {
                        temp = temp.getParent();
                    }
                    final JComponent panel = (JComponent) temp;

                    long loopCount = 0;
                    while (!panel.isVisible() && (++loopCount) < 100) {
                        Thread.sleep(50);
                    }

                    // exit if it never became visible
                    if (!panel.isVisible()) {
                        return;
                    }
                    // after visible wait another 1/10 second to focus
                    Thread.sleep(100);

                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            if (!comp.equals(panel)) {
                                if (!panel.requestFocusInWindow()) {
                                    panel.requestFocus();
                                }
                            }

                            if (!comp.requestFocusInWindow()) {
                                if (secondary != null) {
                                    if (!secondary.requestFocusInWindow()) {
                                        secondary.requestFocus();
                                    }
                                } else {
                                    comp.requestFocus();
                                }
                            }
                        }
                    });
                } catch (Exception ex) {
                    log.warn("Error in focusing component", ex);
                    ex.printStackTrace();
                }
            }
        };

        focusThread.setName("FocusThread-" + String.valueOf(focusCount++));
        focusThread.start();
    }

    /**
     * Exit the Application. Need to remove listeners and null properties so
     * JasperViewer can be garbage collected. Doesn't quite work, for some
     * reason the last copy hangs and doesn't dispose.
     */
    void exitForm() {

        this.setVisible(false);
        this.removeWindowListener(disposeListener);
        disposeListener = null;

        if (btnPrint != null) {
            btnPrint.removeActionListener(printActionListener);
            printActionListener = null;
            btnPrint = null;
        }

        this.jasperPrint = null;

        this.getContentPane().removeAll();
        this.viewer.clear();
        this.viewer = null;
        this.log = null;
        this.pnlMain = null;

        this.dispose();

        System.gc();    // run the Garbage Collector
    }

    /**
     *
     */
    public void setZoomRatio(float zoomRatio) {
        viewer.setZoomRatio(zoomRatio);
    }

    /**
     *
     */
    public void setFitWidthZoomRatio() {
        viewer.setFitWidthZoomRatio();
    }

    /**
     *
     */
    public void setFitPageZoomRatio() {
        viewer.setFitPageZoomRatio();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        String fileName = null;
        boolean isXMLFile = false;

        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("-XML")) {
                isXMLFile = true;
            } else if (args[i].startsWith("-F")) {
                fileName = args[i].substring(2);
            } else {
                fileName = args[i];
            }
        }

        if (fileName == null) {
            usage();
            return;
        }

        if (!isXMLFile && fileName.endsWith(".jrpxml")) {
            isXMLFile = true;
        }

        try {
            viewReport(fileName, isXMLFile);
        } catch (JRException e) {
            if (LogFactory.getLog(JasperViewer.class).isErrorEnabled()) {
                LogFactory.getLog(JasperViewer.class).error("Error viewing report.", e);
            }
            System.exit(1);
        }
    }

    /**
     *
     */
    private static void usage() {
        System.out.println("JasperViewer usage:");
        System.out.println("\tjava JasperViewer [-XML] file");
    }

    /**
     * @see #viewReport(JasperReportsContext, String, boolean, boolean, Locale,
     * ResourceBundle)
     */
    public static void viewReport(
            String sourceFile,
            boolean isXMLFile
    ) throws JRException {
        viewReport(sourceFile, isXMLFile, null);
    }

    /**
     * @see #viewReport(JasperReportsContext, InputStream, boolean, boolean,
     * Locale, ResourceBundle)
     */
    public static void viewReport(
            InputStream is,
            boolean isXMLFile
    ) throws JRException {
        viewReport(is, isXMLFile, null);
    }

    /**
     * @see #viewReport(JasperReportsContext, JasperPrint, boolean, Locale,
     * ResourceBundle)
     */
    public static JasperViewer viewReport(
            JasperPrint jasperPrint
    ) {
        return viewReport(jasperPrint, null);
    }

    /**
     * @see #viewReport(JasperReportsContext, String, boolean, boolean, Locale,
     * ResourceBundle)
     */
    public static void viewReport(
            String sourceFile,
            boolean isXMLFile,
            Locale locale
    ) throws JRException {
        viewReport(
                DefaultJasperReportsContext.getInstance(),
                sourceFile,
                isXMLFile,
                locale,
                null
        );
    }

    /**
     * @see #viewReport(JasperReportsContext, InputStream, boolean, boolean,
     * Locale, ResourceBundle)
     */
    public static void viewReport(
            InputStream is,
            boolean isXMLFile,
            Locale locale
    ) throws JRException {
        viewReport(
                DefaultJasperReportsContext.getInstance(),
                is,
                isXMLFile,
                locale,
                null
        );
    }

    /**
     * @see viewReport(JasperReportsContext, JasperPrint, boolean, Locale,
     * ResourceBundle)
     */
    public static JasperViewer viewReport(
            JasperPrint jasperPrint,
            Locale locale
    ) {
        return viewReport(
                DefaultJasperReportsContext.getInstance(),
                jasperPrint,
                locale,
                null
        );
    }

    /**
     *
     */
    public static void viewReport(
            JasperReportsContext jasperReportsContext,
            String sourceFile,
            boolean isXMLFile,
            Locale locale,
            ResourceBundle resBundle
    ) throws JRException {
        JasperViewer jasperViewer
                = new JasperViewer(
                        jasperReportsContext,
                        sourceFile,
                        isXMLFile,
                        locale,
                        resBundle
                );
        jasperViewer.setVisibleIfHasPages();
    }

    /**
     *
     */
    public static void viewReport(
            JasperReportsContext jasperReportsContext,
            InputStream is,
            boolean isXMLFile,
            Locale locale,
            ResourceBundle resBundle
    ) throws JRException {
        JasperViewer jasperViewer
                = new JasperViewer(
                        jasperReportsContext,
                        is,
                        isXMLFile,
                        locale,
                        resBundle
                );
        jasperViewer.setVisibleIfHasPages();
    }

    /**
     *
     */
    public static JasperViewer viewReport(
            JasperReportsContext jasperReportsContext,
            JasperPrint jasperPrint,
            Locale locale,
            ResourceBundle resBundle
    ) {
        // another 'hack' - sets focus to "Has No Pages" message box
        // added 2019-08-26, JAM
        if (jasperPrint == null || jasperPrint.getPages().isEmpty()) {
            System.out.println("CALLED FOCUS TO DIALOG");
            ReportManager.focusToDialog("Message", "OK");
        }

        JasperViewer jasperViewer
                = new JasperViewer(
                        jasperReportsContext,
                        jasperPrint,
                        locale,
                        resBundle
                );
        if (jasperViewer.setVisibleIfHasPages()) {
            return jasperViewer;    // set visible
        } else {
            return null;    // no pages
        }
    }

    /**
     *
     */
    public static void viewReport(
            JasperReportsContext jasperReportsContext,
            String sourceFile,
            boolean isXMLFile
    ) throws JRException {
        viewReport(
                jasperReportsContext,
                sourceFile,
                isXMLFile,
                null,
                null
        );
    }

    /**
     *
     */
    public static void viewReport(
            JasperReportsContext jasperReportsContext,
            InputStream is,
            boolean isXMLFile
    ) throws JRException {
        viewReport(
                jasperReportsContext,
                is,
                isXMLFile,
                null,
                null
        );
    }

    /**
     *
     */
    public static void viewReport(
            JasperReportsContext jasperReportsContext,
            JasperPrint jasperPrint
    ) {
        viewReport(
                jasperReportsContext,
                jasperPrint,
                null,
                null
        );
    }

    /**
     * Allows access to the controller, so we can get Page Count, etc.
     *
     * @param jasperViewer
     * @return
     */
    public JRViewerController getController() {

        try {
            // we need access to the viewer.viewerContext
            java.lang.reflect.Field context = viewer.getClass().getDeclaredField("viewerContext");
            context.setAccessible(true);
            return (JRViewerController) context.get(viewer);

        } catch (Exception ex) {
            log.error("Error occurred in getController(): " + ex);
            ex.printStackTrace();
            return null;
        }

    }

    /**
     * This is called to show the viewer, but disposes instead if there are no
     * pages available.
     */
    private boolean setVisibleIfHasPages() {

        JRViewerController controller = getController();
        try {
            if (controller != null && controller.getPageCount() > 0) {
                // set title of this frame to report name
                this.setTitle(controller.getJasperPrint().getName());
                setFocus(); // force component to have focus
                this.setVisible(true);
                return true;
            } else {
                exitForm();
                return false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            this.setVisible(true);
            return true;
        }

    }

    /**
     * Sets the focus to a specified component WHEN component is visible.<br>
     * Uses separate thread to 'wait' on visibility, then selects via EDT.
     *
     * @since 2018-04-26, JAM
     * @param c
     */
    public void setFocus() {

        final Component c = this;

        Thread t = new Thread() {
            @Override
            public void run() {
                int ct = 0;
                try {
                    while (!c.isVisible()) {
                        Thread.sleep(50);
                        ct++;
                        if (ct > 200) {
                            return;   // exit after 10 seconds!
                        }
                    }
                    // see if we can find this component's parent
                    RootPaneContainer root = null;
                    if (!(c instanceof RootPaneContainer)) {
                        Container p = c.getParent();
                        while (p != null && !(p instanceof RootPaneContainer)) {
                            p = p.getParent();
                        }
                        if (p != null && p instanceof RootPaneContainer) {
                            root = (RootPaneContainer) p;
                        }
                    } else {
                        root = (RootPaneContainer) c;
                    }
                    Thread.sleep(50);   // wait another 50 ms
                    final Frame win = (root != null && root instanceof Frame) ? (Frame) root : null;

                    // Invoke focus Event Dispatch Thread
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            if (win != null) {
                                if (!win.requestFocusInWindow()) {
                                    win.requestFocus();
                                }
                            }
                            if (!c.requestFocusInWindow()) {
                                c.requestFocus();
                            }
                        }
                    });
                } catch (Exception ex) {
                }
            }

        };
        t.start();
    }

    // Variables declaration - do not modify                     
    private javax.swing.JPanel pnlMain;
    // End of variables declaration        

}
