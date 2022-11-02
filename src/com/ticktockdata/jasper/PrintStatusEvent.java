package com.ticktockdata.jasper;

/**
 *
 * @author JAM
 * @since Apr 03, 2019
 * @see getStatus()
 */
public class PrintStatusEvent extends java.util.EventObject {

    /**
     * Status of the printing process. A printing event always terminates with
     * one of COMPLETE, CANCELED or ERROR, the remaining statuses are progress
     * markers.
     * <p>
     * UNDEFINED is never fired as a status update, but {@link JasperReportImpl#getStatus()} returns
     * UNDEFINED during these periods:
     * <ol>
     * <li>The report has never been executed
     * <li>Between the time execute is called and the QUEUED event is fired
     * </ol>
     *
     * @see getStatus()
     */
    public enum StatusCode {
        UNDEFINED,
        QUEUED,
        STARTED,
        COMPILED,
        FILLED,
        CANCELED,
        ERROR,
        COMPLETE
    }

    private final StatusCode status;
    private final long when;

    /**
     *
     * @param source
     * @param status {@link getStatus()}
     */
    public PrintStatusEvent(JasperReportImpl source, StatusCode status) {
        super(source);
        this.status = status;
        this.when = System.currentTimeMillis();
    }

    @Override
    public JasperReportImpl getSource() {
        return (JasperReportImpl) source;
    }

    /**
     * Get the {@link StatusCode} that occurred, which is one of the following:
     * <ul>
     * <li>QUEUED - The report is added to the ExecutorService
     * <li>STARTED - The report.run() method was called (started execution)
     * <li>COMPILED - The report file was fetched and compiled successfully
     * <li>FILLED - The parameters were injected and the report filled
     * successfully
     * <li>CANCELED - User canceled report execution
     * <li>ERROR - Error occurred during execution
     * <li>COMPLETE - The report has completed execution successfully
     * </ul>
     *
     * @return
     */
    public StatusCode getStatus() {
        return status;
    }

    /**
     * The System.currentTimeMillis() when the event occurred.
     *
     * @return
     */
    public long getWhen() {
        return when;
    }

}
