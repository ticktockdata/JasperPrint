package com.ticktockdata.jasper;

import static com.ticktockdata.jasper.DateService.*;
import java.util.Date;

/**
 * The values of this enum have methods for getStartDate() and getEndDate() that
 * call the static methods in {@link DateService} class to obtain the correct
 * values.
 *
 * @author JAM
 * @since Oct 06, 2022
 * @see DateRange#ALL
 */
public enum DateRange {

    /**
     * The ALL range is subjective to the application. There is a default value
     * for this range, but it can be customized by setting an implementation of
     * the {@link DateRangeALL} interface in
     * {@link DateService#setDateRangeAllImpl(DateRangeALL)}
     * <p>
     */
    ALL("All") {
        @Override
        public Date getStartDate() {
            return getAllStart();
        }

        @Override
        public Date getEndDate() {
            return getAllEnd();
        }
    },
    TODAY("Today") {
        @Override
        public Date getStartDate() {
            return getToday();
        }

        @Override
        public Date getEndDate() {
            return getToday();
        }
    },
    THIS_WEEK("This Week") {
        @Override
        public Date getStartDate() {
            return getFirstDayOfThisWeek();
        }

        @Override
        public Date getEndDate() {
            return getLastDayOfThisWeek();
        }
    },
    THIS_WEEK_TO_DATE("This Week To Date") {
        @Override
        public Date getStartDate() {
            return getFirstDayOfThisWeek();
        }

        @Override
        public Date getEndDate() {
            return getToday();
        }
    },
    THIS_MONTH("This Month") {
        @Override
        public Date getStartDate() {
            return getFirstDayOfThisMonth();
        }

        @Override
        public Date getEndDate() {
            return getLastDayOfThisMonth();
        }
    },
    THIS_MONTH_TO_DATE("This Month To Date") {
        @Override
        public Date getStartDate() {
            return getFirstDayOfThisMonth();
        }

        @Override
        public Date getEndDate() {
            return getToday();
        }
    },
    THIS_QUARTER("This Quarter") {
        @Override
        public Date getStartDate() {
            return getFirstDayOfThisQuarter();
        }

        @Override
        public Date getEndDate() {
            return getLastDayOfThisQuarter();
        }
    },
    THIS_QUARTER_TO_DATE("This Quarter To Date") {
        @Override
        public Date getStartDate() {
            return getFirstDayOfThisQuarter();
        }

        @Override
        public Date getEndDate() {
            return getToday();
        }
    },
    THIS_YEAR("This Year") {
        @Override
        public Date getStartDate() {
            return getFirstDayOfThisYear();
        }

        @Override
        public Date getEndDate() {
            return getLastDayOfThisYear();
        }
    },
    THIS_YEAR_TO_DATE("This Year To Date") {
        @Override
        public Date getStartDate() {
            return getFirstDayOfThisYear();
        }

        @Override
        public Date getEndDate() {
            return getToday();
        }
    },
    LAST_WEEK("Last Week") {
        @Override
        public Date getStartDate() {
            return getFirstDayOfLastWeek();
        }

        @Override
        public Date getEndDate() {
            return getLastDayOfLastWeek();
        }
    },
    LAST_WEEK_MINUS1("Last Week -1") {
        @Override
        public Date getStartDate() {
            return getFirstDayOfLastWeekMinus1();
        }

        @Override
        public Date getEndDate() {
            return getLastDayOfLastWeekMinus1();
        }
    },
    LAST_WEEK_MINUS2("Last Week -2") {
        @Override
        public Date getStartDate() {
            return getFirstDayOfLastWeekMinus2();
        }

        @Override
        public Date getEndDate() {
            return getLastDayOfLastWeekMinus2();
        }
    },
    LAST_WEEK_MINUS3("Last Week -3") {
        @Override
        public Date getStartDate() {
            return getFirstDayOfLastWeekMinus3();
        }

        @Override
        public Date getEndDate() {
            return getLastDayOfLastWeekMinus3();
        }
    },
    LAST_WEEK_TO_DATE("Last Week To Date") {
        @Override
        public Date getStartDate() {
            return getFirstDayOfLastWeek();
        }

        @Override
        public Date getEndDate() {
            return getToday();
        }
    },
    LAST_MONTH("Last Month") {
        @Override
        public Date getStartDate() {
            return getFirstDayOfLastMonth();
        }

        @Override
        public Date getEndDate() {
            return getLastDayOfLastMonth();
        }
    },
    LAST_MONTH_TO_DATE("Last Month To Date") {
        @Override
        public Date getStartDate() {
            return getFirstDayOfLastMonth();
        }

        @Override
        public Date getEndDate() {
            return getToday();
        }
    },
    LAST_QUARTER("Last Quarter") {
        @Override
        public Date getStartDate() {
            return getFirstDayOfLastQuarter();
        }

        @Override
        public Date getEndDate() {
            return getLastDayOfLastQuarter();
        }
    },
    LAST_QUARTER_TO_DATE("Last Quarter To Date") {
        @Override
        public Date getStartDate() {
            return getFirstDayOfLastQuarter();
        }

        @Override
        public Date getEndDate() {
            return getToday();
        }
    },
    LAST_YEAR("Last Year") {
        @Override
        public Date getStartDate() {
            return getFirstDayOfLastYear();
        }

        @Override
        public Date getEndDate() {
            return getLastDayOfLastYear();
        }
    },
    LAST_YEAR_TO_DATE("Last Year To Date") {
        @Override
        public Date getStartDate() {
            return getFirstDayOfLastYear();
        }

        @Override
        public Date getEndDate() {
            return getToday();
        }
    },
    NEXT_WEEK("Next Week") {
        @Override
        public Date getStartDate() {
            return getFirstDayOfNextWeek();
        }

        @Override
        public Date getEndDate() {
            return getLastDayOfNextWeek();
        }
    },
    NEXT_WEEK_PLUS1("Next Week +1") {
        @Override
        public Date getStartDate() {
            return getFirstDayOfNextWeekPlus1();
        }

        @Override
        public Date getEndDate() {
            return getLastDayOfNextWeekPlus1();
        }
    },
    NEXT_WEEK_PLUS2("Next Week +2") {
        @Override
        public Date getStartDate() {
            return getFirstDayOfNextWeekPlus2();
        }

        @Override
        public Date getEndDate() {
            return getLastDayOfNextWeekPlus2();
        }
    },
    NEXT_WEEK_PLUS3("Next Week +3") {
        @Override
        public Date getStartDate() {
            return getFirstDayOfNextWeekPlus3();
        }

        @Override
        public Date getEndDate() {
            return getLastDayOfNextWeekPlus3();
        }
    },
    NEXT_MONTH("Next Month") {
        @Override
        public Date getStartDate() {
            return getFirstDayOfNextMonth();
        }

        @Override
        public Date getEndDate() {
            return getLastDayOfNextMonth();
        }

    },
    NEXT_QUARTER("Next Quarter") {
        @Override
        public Date getStartDate() {
            return getFirstDayOfNextQuarter();
        }

        @Override
        public Date getEndDate() {
            return getLastDayOfNextQuarter();
        }
    },
    NEXT_YEAR("Next Year") {
        @Override
        public Date getStartDate() {
            return getFirstDayOfNextYear();
        }

        @Override
        public Date getEndDate() {
            return getLastDayOfNextYear();
        }
    },
    TOMORROW("Tomorrow") {
        @Override
        public Date getStartDate() {
            return getTomorrow();
        }

        @Override
        public Date getEndDate() {
            return getTomorrow();
        }
    },
    YESTERDAY("Yesterday") {
        @Override
        public Date getStartDate() {
            return getYesterday();
        }

        @Override
        public Date getEndDate() {
            return getYesterday();
        }
    };

    private final String prettyName;

    DateRange(String prettyName) {
        this.prettyName = prettyName;
    }

    public abstract Date getStartDate();

    public abstract Date getEndDate();

    @Override
    public String toString() {
        return prettyName;
    }

    /**
     * This is just a tester method that prints all values to Standard.out.
     */
    public static void printValues() {
        for (DateRange dr : values()) {
            System.out.println((dr.toString() + "                 ").substring(0, 20) + "  :  " + dr.getStartDate() + "  -  " + dr.getEndDate());
        }
    }

}
