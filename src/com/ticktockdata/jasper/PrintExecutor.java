/*
 * Copyright (C) 2018-2022 Joseph A Miller
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
package com.ticktockdata.jasper;

import net.sf.jasperreports.engine.JasperPrint;
import org.apache.log4j.Logger;

/**
 * A PrintExecutor must fire {@link com.ticktockdata.jasper.PrintStatusEvent}
 * for CANCELED and ERORR, but not for COMPLETE. If the return value of
 * {@link execute(JasperPrint)} is true then {@link FillMonitor} will fire the
 * {@link com.ticktockdata.jasper.PrintStatusEvent.StatusCode#EXECUTE_COMPLETE}
 * event
 *
 * @author JAM
 * @since Aug 27, 2018
 */
public abstract class PrintExecutor {

    protected Logger logger = Logger.getLogger(this.getClass());

    public static enum Action {
        PRINT,
        PREVIEW,
        EXPORT_TO_PDF,
        EXPORT_TO_XLS,
        EXPORT_TO_CSV;

        /**
         * This is a more forgiving version of valueOf(String name), it trims
         * leading / trailing spaces and is not case-sensitive. Spelling must be
         * exact, though.
         *
         * @param text
         * @return null if invalid, instead of throwing error.
         */
        public static Action fromString(String text) {

            text = text.trim().toUpperCase();
            try {
                return valueOf(text);
            } catch (Exception ex) {
                return null;
            }

        }
    }

    private JasperReportImpl report;

    /**
     * Not allowed to instantiate a no-args version
     */
    private PrintExecutor() {
    }

    /**
     * Constructor requires a JasperReportImpl.
     *
     * @param report
     */
    public PrintExecutor(JasperReportImpl report) {
        this.report = report;
    }

    public JasperReportImpl getReport() {
        return report;
    }

    /**
     *
     * @return the type of PrintExecutor.Action for this implementation.
     */
    public abstract Action getAction();

    /**
     * Each implementation must provide its own validation.
     *
     * @return true if the PrintExecutor provides sufficient information to do
     *         its thing.
     */
    public abstract boolean isValid();

    /**
     * Each PrintExecutor must be able to do the execution using a
     * {@link JasperPrint}. This method should return true only if the execute
     * action was successful.
     *
     * @param print
     * @return
     */
    public abstract boolean execute(JasperPrint print);

    /**
     * Allows canceling of a job.
     */
    public abstract void cancelExecute();

}
