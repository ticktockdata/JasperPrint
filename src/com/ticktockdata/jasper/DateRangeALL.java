package com.ticktockdata.jasper;

/**
 *
 * An application can submit an implementation of this interface to
 * {@link DateService#setDateRangeAllImpl(DateRangeALL)} to have
 * {@link DateRange#ALL} return a more accurate start / end date.
 *
 * @author JAM
 * @since Oct 07, 2022
 */
public interface DateRangeALL {

    public java.util.Date getStartDate();

    public java.util.Date getEndDate();

}
