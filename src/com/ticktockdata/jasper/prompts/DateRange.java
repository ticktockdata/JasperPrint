/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ticktockdata.jasper.prompts;

import com.ticktockdata.jasper.prompts.DateRangePrompt.RangeName;
import java.util.Date;

/**
 *
 * @author JAM {javajoe@programmer.net}
 * @since Sep 05, 2019
 */
public class DateRange {
    
    private Date startDate;
    private Date endDate;
    private RangeName rangeName;
    
    
    public DateRange(RangeName rangeName, Date startDate, Date endDate) {
        this.rangeName = rangeName;
        this.startDate = startDate;
        this.endDate = endDate;
    }
    
    /**
     * @return the startDate
     */
    public Date getStartDate() {
        return startDate;
    }

    /**
     * @param startDate the startDate to set
     */
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    /**
     * @return the endDate
     */
    public Date getEndDate() {
        return endDate;
    }

    /**
     * @param endDate the endDate to set
     */
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    /**
     * @return the rangeName
     */
    public RangeName getRangeName() {
        return rangeName;
    }

    /**
     * @param rangeName the rangeName to set
     */
    public void setRangeName(RangeName rangeName) {
        this.rangeName = rangeName;
    }
    
}
