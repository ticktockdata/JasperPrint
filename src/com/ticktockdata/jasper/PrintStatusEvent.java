
package com.ticktockdata.jasper;

/**
 *
 * @author JAM {javajoe@programmer.net}
 * @since Apr 03, 2019
 * @see getStatus()
 */
public class PrintStatusEvent extends java.util.EventObject {
    
    
//    /**
//     * Status of the printing process
//     */
//    public enum StatusCode {
//        EXECUTE_START,
//        EXECUTE_CANCELED,
//        EXECUTE_ERROR,
//        EXECUTE_COMPLETE
//    }
    
    public static final int STATUS_UNDEFINED = 0;
    public static final int EXECUTE_START = 2048;
    public static final int EXECUTE_CANCELED = 4096;
    public static final int EXECUTE_ERROR = 8192;
    public static final int EXECUTE_COMPLETE = 16384;
    
    
    private final int status;
    private final long when;
    
    
    /**
     * 
     * @param source
     * @param status {@link getStatus()}
     */
    public PrintStatusEvent(JasperReportImpl source, int status) {
        super(source);
        this.status = status;
        this.when = System.currentTimeMillis();
    }
    
    @Override
    public JasperReportImpl getSource() {
        return (JasperReportImpl)source;
    }
    
    /**
     * Get the status that occurred, which is one of the following:
     * <ul>
     * <li>EXECUTE_START - The report is added to the ExecutorService
     * <li>EXECUTE_CANCELED - User cancels report execution
     * <li> EXECUTE_ERROR - Error occurs during execution
     * <li>EXECUTE_COMPLETE - The report has completed execution successfully
     * </ul>
     * @return 
     */
    public int getStatus() {
        return status;
    }
    
    /**
     * The System.currentTimeMillis() when the event occurred.
     * @return 
     */
    public long getWhen() {
        return when;
    }
    
}
