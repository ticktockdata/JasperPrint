/*
 * Copyright (C) 2019 Joseph A Miller
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

/**
 * This FunctionalInterface is used to fill the prompts of a {@link JasperReportImpl}
 * during compiling / filling of the report.  Add the prompt filler via
 * JasperReportImpl.addPrintPromptFiller(PrintPromptFiller).
 * <p>Not tagged w/@FunctionalInterface to remain Java 6/7 compatible.
 * @author JAM &lt;javajoe@programmer.net&gt;
 * @since Jan 16, 2019
 */
@FunctionalInterface
public interface PrintPromptFiller {
    
    /**
     * The filler can cancel report execution by returning False.
     * @param report
     * @return true to display / print the report, or false to cancel
     */
    boolean fillPrompts(JasperReportImpl report);

}
