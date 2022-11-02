
package com.ticktockdata.jasper.prompts;

import com.ticktockdata.jasper.JasperReportImpl;
import com.ticktockdata.jasper.ReportManager;
import com.ticktockdata.jasper.TestPrint;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.sf.jasperreports.engine.JRParameter;
import org.apache.log4j.Logger;

/**
 * This is a final Factory class that provides the {@link PromptDialog} with
 * the correct components.  This class will cache the {@link PromptComponent}
 * Components, but will clear the cache when {@code ReportManager.clearReportCache()} is called.
 * @author JAM {javajoe@programmer.net}
 * @since Sep 11, 2019
 */
public final class PromptComponentFactory {
    
    private static final Map<String, PromptComponent> promptMap = new WeakHashMap<String, PromptComponent>();
    
    /**
     * Private constructor prevents creating an instance of this class
     */
    private PromptComponentFactory() {
        
    }
    
    
    /**
     * This private method handles caching / creating PromptComponent
     * @param parameter
     * @return 
     */
    private static PromptComponent getPromptComponent(JRParameter param, Class<? extends PromptComponent> componentClass) {
        
        String paramName = (param == null ? "P_DATE_RANGE" : param.getName());
        PromptComponent panel = promptMap.get(paramName);

        if (panel == null) {
            try {
                panel = (PromptComponent) componentClass.newInstance();
            } catch (Exception ex) {
                Logger.getLogger(componentClass).error("Error creating PromptComponent", ex);
                return null;
            }
            if (param != null) {
                // set the parameter, which updates description, default value, etc.
                panel.setParameter(param);
            }
            // add to cache
            promptMap.put(paramName, panel);
        }
        
        return panel;
    }
    
    /**
     * Matches property name case-insensitive.
     * @param param
     * @param propertyName
     * @return 
     */
    protected static boolean hasProperty(JRParameter param, String propertyName) {
        if (param.getPropertiesMap().containsProperty(propertyName)) {
            return true;
        } else {
            for (String s : param.getPropertiesMap().getPropertyNames()) {
                if (propertyName.equalsIgnoreCase(s)) return true;
            }
        }
        // if not found then doesn't exist
        return false;
    }
    
    protected static String getProperty(JRParameter param, String propertyName) {
        // check for name exact match
        String prop = param.getPropertiesMap().getProperty(propertyName);
        if (prop != null) return prop;
        // if not found then check for ignore case match
        for (String s : param.getPropertiesMap().getPropertyNames()) {
            if (propertyName.equalsIgnoreCase(s)) {
                return param.getPropertiesMap().getProperty(s);
            }
        }
        return null;
    }
    
    /**
     * Gets all parameters for a report.  This method provides the logic for 
     * determining the proper type of prompt to use for each parameter
     * @param report
     * @return 
     */
    public static List<PromptComponent> getPromptsForReport(JasperReportImpl report) {
        
        // list to contain prompts as we fetch them
        List<PromptComponent> prompts = new ArrayList<>();
        
        // =====================================================================
        // do a pre-check for start / end date parameters
        // There is some kind-of complicated logic involved in determining
        // if there is a Date Range, and to make so it adds only the 
        // date range control and not the individual dates
        String startDateParameterName = null;
        String endDateParameterName = null;
        
        // loop though all parameters to see if we have a Start Date AND End Date
        for (JRParameter param : report.getJasperReport().getParameters()) {
            if  (param.getValueClass().equals(java.util.Date.class)) {
                if (param.getName().toUpperCase().contains("START") || param.getName().toUpperCase().contains("BEGIN")) {
                    if (startDateParameterName == null) {
                        startDateParameterName = param.getName();
                    } else {
                        startDateParameterName = ""; // empty string is flag for error
                    }
                } else if (param.getName().toUpperCase().contains("END")) {
                    if (endDateParameterName == null) {
                        endDateParameterName = param.getName();
                    } else {
                        endDateParameterName = ""; // empty string is flag for error
                    }
                }
            }
        }
        
        // if both startDate and endDate found then hasDateRange = True
        boolean hasDateRange = false;
        if ((startDateParameterName != null && !startDateParameterName.isEmpty())) {
            if (endDateParameterName != null && !endDateParameterName.isEmpty()) {
                hasDateRange = true;
            }
        }
        
        if (hasDateRange) {
            // add the Date Range control as the 1st prompt
//            System.out.println("Has Date Range!");
            DateRangePrompt dateRange = (DateRangePrompt)getPromptComponent(null, DateRangePrompt.class);
            prompts.add(dateRange);
            dateRange.setStartDateParameterName(startDateParameterName);
            dateRange.setEndDateParameterName(endDateParameterName);
        } else {
            // clear the names of start / end date params
            startDateParameterName = "";
            endDateParameterName = "";
        }
        // end of the date range control check
        // =====================================================================
        
        // Not enough - we need to get parameters now!
        //ToDo: get the individual parameters from the jasperReportImpl
        for (JRParameter param : report.getJasperReport().getParameters()) {
            // if not a user-defined Prompt For Value then continue to next
//            System.out.println("found param: " + param);
            if (param.isSystemDefined() || !param.isForPrompting()) {
                continue;
            }
            Class pClass = param.getValueClass();
            String pName = param.getName().toUpperCase();
            String query = getProperty(param, "Query");
//            System.out.println("class = " + pClass + ", name = " + pName);
            // Need to select proper Prompt Component based on various criteria
            if (pClass.equals(String.class)) {
                
                if (query != null && !query.isEmpty()) {
                    // is a query prompt
                    prompts.add(getPromptComponent(param, StringQueryPrompt.class));
                } else {    //if (pName.startsWith("P_TEXT") || pName.startsWith("P_FREE_TEXT")) {
                    prompts.add(getPromptComponent(param, FreeTextPrompt.class));
                }
                
            } else if (pClass.equals(java.util.Date.class)) {
                // Check that it is not part of the date parameters
                if (!pName.equals(startDateParameterName) && !pName.equals(endDateParameterName)) {
                    prompts.add(getPromptComponent(param, DatePrompt.class));
                }
                
            } else if (pClass.equals(Boolean.class)) {
                prompts.add(getPromptComponent(param, BooleanPrompt.class));
            } else if (pClass.equals(Long.class)) {
                
                if (query != null && !query.trim().isEmpty()) {
                    prompts.add(getPromptComponent(param, LongQueryPrompt.class));
                } else {
                    ReportManager.logger.warn("SHOUTING: WE FOUND A LONG AND DON'T KNOW WHAT TO DO WITH IT!  name = "  + param.getName() + ", desc = " + param.getDescription());
                }
            } else if (pClass.equals(java.math.BigDecimal.class)) {
                
                prompts.add(getPromptComponent(param, BigDecimalPrompt.class));
                
            } else {
                ReportManager.logger.warn("SHOUTING: WE FOUND A " + pClass + " PARAMETER AND DON'T KNOW WHAT TO DO WITH IT!");
            }
        }
        
        return prompts;
        
    }
    
    public static void clearPromptComponentCache() {
        ReportManager.logger.info("Clearing " + promptMap.size() + " Prompt Components from PromptComponentFactory");
        promptMap.clear();
    }
    
    public static void clearPromptComponentCache(JRParameter[] params) {
        
        for (JRParameter p : params) {
            if (promptMap.containsKey(p.getName())) {
                System.out.println("Removing parameter " + p.getName());
                promptMap.remove(p.getName());
            }
        }
    }
    
    public static void main(String[] args) {
        TestPrint.main(new String[] {});
    }
    
}
