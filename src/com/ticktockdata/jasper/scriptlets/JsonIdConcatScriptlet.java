/*
 * Copyright (C) 2016 Joseph A Miller
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

package com.ticktockdata.jasper.scriptlets;

import java.util.ArrayList;
import net.sf.jasperreports.engine.JRDefaultScriptlet;
import net.sf.jasperreports.engine.JRScriptletException;

/**
 * This scriptlet concatenates the json_ids field into a single json_agg.
 * <p>
 * Package incorrect for this project, but required to maintain compatibility
 * with reports created for a prior project.
 * 
 * @author JAM
 */
public class JsonIdConcatScriptlet extends JRDefaultScriptlet {

    ArrayList<String> ids = new ArrayList<String>();
    
    @Override
    public void beforeGroupInit(String groupName) throws JRScriptletException {
        // If the group restarts, then clear array
        if (groupName.equals("json_reset")) {
            if (ids.size() > 0) ids.clear();
        }
    }

    
    @Override
    public void afterDetailEval() throws JRScriptletException {

        String val = this.getFieldValue("json_ids").toString();

        val = val.substring(1, val.length()-1);
        String[] aVals = val.split(", ");
        for (int i = 0; i < aVals.length; i++) {
            if (!ids.contains(aVals[i])) ids.add(aVals[i]);
        }
    }
    
    /**
      * @return the time past from the last page init
      */
    public String getIDs() {

        StringBuilder sb = new StringBuilder();
        
        for (String s : ids) {
            if (sb.length() == 0) 
                sb.append("[");
            else
                sb.append(", ");
            sb.append(s);
        }
        sb.append("]");
        
        return sb.toString();
    }
    
}
