package com.ticktockdata.jasper;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * All the methods in this class return a Date that has been stripped of the
 * Time portion (time = 00:00:00).
 *
 * @author JAM
 * @since Oct 06, 2022
 * @see DateService#setDateRangeAllImpl(DateRangeALL) 
 */
public class DateService {
    
    
    private static DateRangeALL allRange = new DateRangeALL() {
        @Override
        public Date getStartDate() {
            Calendar cal = GregorianCalendar.getInstance();
            cal.set(1970, Calendar.JANUARY, 1);
            return stripTime(cal);
        }

        @Override
        public Date getEndDate() {
            Calendar cal = calFirstDayThisYear();
            cal.add(Calendar.YEAR, 11);
            cal.add(Calendar.DAY_OF_MONTH, -1);
            return stripTime(cal);
        }
    };
    
    
    // prevent instating this class
    private DateService() {
    }
    
    
    /**
     * The values for {@link DateRange#ALL} are pretty broad, if desired a
     * custom implementation of the {@link DateRangeALL} interface can be 
     * set here to supply desired values
     * 
     * @param impl 
     */
    public static void setDateRangeAllImpl(DateRangeALL impl) {
        allRange = impl;
    }
    
    
    /**
     * The default implementation returns Jan 1, 1970.
     * 
     * @return 
     * @see setDateRangeAllImpl(DateRangeALL)
     */
    public static Date getAllStart() {
        return allRange.getStartDate();
    }
    
    
    /**
     * The default implementation returns Dec 31 of current Year+10.
     * 
     * @return 
     * @see setDateRangeAllImpl(DateRangeALL)
     */
    public static Date getAllEnd() {
        return allRange.getEndDate();
    }
    
    public static Date getYesterday() {
        Calendar cal = GregorianCalendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -1);
        return stripTime(cal);
    }

    public static Date getToday() {
        return stripTime(GregorianCalendar.getInstance());
    }

    public static Date getTomorrow() {
        Calendar cal = GregorianCalendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        return stripTime(cal);
    }

    public static Date getFirstDayOfThisQuarter() {
        Calendar cal = calLastDayThisQuarter();
        cal.add(Calendar.MONTH, -2);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        return stripTime(cal);
    }

    public static Date getLastDayOfThisQuarter() {
        return stripTime(calLastDayThisQuarter());  // 1K calls = 27 ms
    }

    public static Date getFirstDayOfThisWeek() {
        return stripTime(calFirstDayThisWeek());
    }

    public static Date getLastDayOfThisWeek() {
        Calendar cal = calFirstDayThisWeek();
        cal.add(Calendar.DAY_OF_MONTH, 6);
        return stripTime(cal);
    }

    public static Date getFirstDayOfThisMonth() {
        Calendar cal = calLastDayThisMonth();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        return stripTime(cal);
    }

    public static Date getLastDayOfThisMonth() {
        return stripTime(calLastDayThisMonth());
    }

    public static Date getFirstDayOfThisYear() {
        return stripTime(calFirstDayThisYear());
    }

    public static Date getLastDayOfThisYear() {
        Calendar cal = GregorianCalendar.getInstance();
        cal.set(Calendar.MONTH, Calendar.DECEMBER);
        cal.set(Calendar.DAY_OF_MONTH, 31);
        return stripTime(cal);
    }

    public static Date getFirstDayOfLastWeek() {
        Calendar cal = calFirstDayThisWeek();
        cal.add(Calendar.DAY_OF_MONTH, -7);
        return stripTime(cal);
    }

    public static Date getLastDayOfLastWeek() {
        Calendar cal = calFirstDayThisWeek();
        cal.add(Calendar.DAY_OF_MONTH, -1);
        return stripTime(cal);
    }
    
    public static Date getFirstDayOfLastWeekMinus1() {
        Calendar cal = calFirstDayThisWeek();
        cal.add(Calendar.DAY_OF_MONTH, -14);
        return stripTime(cal);
    }

    public static Date getLastDayOfLastWeekMinus1() {
        Calendar cal = calFirstDayThisWeek();
        cal.add(Calendar.DAY_OF_MONTH, -8);
        return stripTime(cal);
    }
    
    public static Date getFirstDayOfLastWeekMinus2() {
        Calendar cal = calFirstDayThisWeek();
        cal.add(Calendar.DAY_OF_MONTH, -21);
        return stripTime(cal);
    }

    public static Date getLastDayOfLastWeekMinus2() {
        Calendar cal = calFirstDayThisWeek();
        cal.add(Calendar.DAY_OF_MONTH, -15);
        return stripTime(cal);
    }
    
    public static Date getFirstDayOfLastWeekMinus3() {
        Calendar cal = calFirstDayThisWeek();
        cal.add(Calendar.DAY_OF_MONTH, -28);
        return stripTime(cal);
    }

    public static Date getLastDayOfLastWeekMinus3() {
        Calendar cal = calFirstDayThisWeek();
        cal.add(Calendar.DAY_OF_MONTH, -22);
        return stripTime(cal);
    }
    
    public static Date getFirstDayOfLastMonth() {
        Calendar cal = calFirstDayThisMonth();
        cal.add(Calendar.MONTH, -1);
        return stripTime(cal);
    }

    public static Date getLastDayOfLastMonth() {
        Calendar cal = calFirstDayThisMonth();
        cal.add(Calendar.DAY_OF_MONTH, -1);
        return stripTime(cal);
    }

    public static Date getFirstDayOfLastQuarter() {
        Calendar cal = calLastDayThisQuarter();
        cal.add(Calendar.MONTH, -5);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        return stripTime(cal);
    }

    public static Date getLastDayOfLastQuarter() {
        Calendar cal = calLastDayThisQuarter();
        cal.add(Calendar.MONTH, -3);
        return stripTime(cal);
    }

    public static Date getFirstDayOfLastYear() {
        Calendar cal = calFirstDayThisYear();
        cal.add(Calendar.YEAR, -1);
        return stripTime(cal);
    }

    public static Date getLastDayOfLastYear() {
        Calendar cal = calFirstDayThisYear();
        cal.add(Calendar.DAY_OF_MONTH, -1);
        return stripTime(cal);
    }

    public static Date getFirstDayOfNextWeek() {
        Calendar cal = calFirstDayThisWeek();
        cal.add(Calendar.DAY_OF_MONTH, 7);
        return stripTime(cal);
    }

    public static Date getLastDayOfNextWeek() {
        Calendar cal = calFirstDayThisWeek();
        cal.add(Calendar.DAY_OF_MONTH, 13);
        return stripTime(cal);
    }
    
    public static Date getFirstDayOfNextWeekPlus1() {
        Calendar cal = calFirstDayThisWeek();
        cal.add(Calendar.DAY_OF_MONTH, 14);
        return stripTime(cal);
    }

    public static Date getLastDayOfNextWeekPlus1() {
        Calendar cal = calFirstDayThisWeek();
        cal.add(Calendar.DAY_OF_MONTH, 20);
        return stripTime(cal);
    }
    
    public static Date getFirstDayOfNextWeekPlus2() {
        Calendar cal = calFirstDayThisWeek();
        cal.add(Calendar.DAY_OF_MONTH, 21);
        return stripTime(cal);
    }

    public static Date getLastDayOfNextWeekPlus2() {
        Calendar cal = calFirstDayThisWeek();
        cal.add(Calendar.DAY_OF_MONTH, 27);
        return stripTime(cal);
    }
    
    public static Date getFirstDayOfNextWeekPlus3() {
        Calendar cal = calFirstDayThisWeek();
        cal.add(Calendar.DAY_OF_MONTH, 28);
        return stripTime(cal);
    }

    public static Date getLastDayOfNextWeekPlus3() {
        Calendar cal = calFirstDayThisWeek();
        cal.add(Calendar.DAY_OF_MONTH, 34);
        return stripTime(cal);
    }
    
    public static Date getFirstDayOfNextMonth() {
        Calendar cal = calFirstDayThisMonth();
        cal.add(Calendar.MONTH, 1);
        return stripTime(cal);
    }

    public static Date getLastDayOfNextMonth() {
        Calendar cal = calFirstDayThisMonth();
        cal.add(Calendar.MONTH, 2);
        cal.add(Calendar.DAY_OF_MONTH, -1);
        return stripTime(cal);
    }

    public static Date getFirstDayOfNextQuarter() {
        Calendar cal = calLastDayThisQuarter();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        return stripTime(cal);
    }

    public static Date getLastDayOfNextQuarter() {
        Calendar cal = calLastDayThisQuarter();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        cal.add(Calendar.MONTH, 3);
        cal.add(Calendar.DAY_OF_MONTH, -1);
        return stripTime(cal);
    }

    public static Date getFirstDayOfNextYear() {
        Calendar cal = calFirstDayThisYear();
        cal.add(Calendar.YEAR, 1);
        return stripTime(cal);
    }

    public static Date getLastDayOfNextYear() {
        Calendar cal = calFirstDayThisYear();
        cal.add(Calendar.YEAR, 2);
        cal.add(Calendar.DAY_OF_MONTH, -1);
        return stripTime(cal);
    }

    /* Private Helper Methods */
    private static Calendar calFirstDayThisWeek() {
        Calendar cal = GregorianCalendar.getInstance();
        int day = cal.get(Calendar.DAY_OF_WEEK);
        while (day != cal.getFirstDayOfWeek()) {
            cal.add(Calendar.DAY_OF_WEEK, -1);
            day--;
        }
        return cal;
    }

    private static Calendar calFirstDayThisMonth() {
        Calendar cal = GregorianCalendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        return cal;
    }

    private static Calendar calLastDayThisMonth() {
        Calendar cal = calFirstDayThisMonth();
        cal.add(Calendar.MONTH, 1);
        cal.add(Calendar.DAY_OF_MONTH, -1);
        return cal;
    }

    private static Calendar calLastDayThisQuarter() {

        Calendar cal = calFirstDayThisMonth();
        int m = cal.get(Calendar.MONTH) + 1;
        // add quarter offset amount + 1 to go to first month after this period
        cal.add(Calendar.MONTH, ((12 - m) % 3 + 1));
        // then subtract 1 day to get last day of this period
        cal.add(Calendar.DAY_OF_MONTH, -1);
        return cal;
    }

    private static Calendar calFirstDayThisYear() {
        Calendar cal = GregorianCalendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        return cal;
    }

    /**
     * All get*DayOf*() functions call this at end to strip the time elements
     *
     * @param cal
     * @return
     */
    private static Date stripTime(Calendar cal) {

        return new GregorianCalendar(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).getTime();
    }

    public static void main(String[] args) {

        long start = System.currentTimeMillis();
        int ct = 0;

        for (int i = 0; i < 1000; i++) {

//            if (getToday() != null) {
//                ct++;
//            }
//            
//            if (getYesterday() != null) {
//                ct++;
//            }
            if (getFirstDayOfThisQuarter() != null) {
                ct++;
            }

//            System.out.println("Today = " + getToday());
//            //System.out.println("Yesterday = " + getYesterday() + "   " + System.currentTimeMillis());
//            ct += 1;
        }
        
        
        DateRange.printValues();
        
        
    }

}
