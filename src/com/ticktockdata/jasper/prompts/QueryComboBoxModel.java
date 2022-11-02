
package com.ticktockdata.jasper.prompts;

import com.ticktockdata.jasper.ReportConnectionManager;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import org.apache.log4j.Logger;

/**
 * This is a data model for a Combo Box that uses a SQL query to obtain the 
 * data.  The Display value for the combo box is always the 1st column of the
 * query results.  If the SQL query returns only one column then the return
 * value of the model is the same as the display, otherwise the return value
 * is the 2nd column.  This model dynamically types the query results, so 
 * return value is the correct type for the data.
 * @author JAM {javajoe@programmer.net}
 * @since Apr 30, 2019
 */
public class QueryComboBoxModel<T> extends DefaultComboBoxModel<String> {
    
    private final String connectionID;
    private JComboBox dataComponent;
    private final String SQL;
    private final T defaultValue;
    private final List<T> values = new ArrayList<>();
    
    private int valueType = 0;   // Types.VARCHAR, etc.
    private int valueColumn = -1;    // column containing the data value
    private Object lastValue = null;    // value displayed last time shown
    
    private final Logger logger = Logger.getLogger(QueryComboBoxModel.class);
    
    
    public QueryComboBoxModel(String connectionID, JComboBox dataComponent, String SQL, T defaultValue) {
        super();
        this.connectionID = connectionID;
        this.dataComponent = dataComponent;
        this.SQL = SQL;
        this.defaultValue = defaultValue;
    }
    
    
//    @Override
    public void setDataComponent(JComboBox dataComponent) {
        this.dataComponent = dataComponent;
    }

//    @Override
    public JComboBox getDataComponent() {
        return this.dataComponent;
    }
    

//    @Override
//    public Object getElementAt(int index) {
//        return values.get(index);
//    }
    
    
    public T getSelectedValue() {
        int index = getIndexOf(((JComboBox)dataComponent).getSelectedItem());
        return (index < 0 ? (T)null : values.get(index));
    }
    
    public void setSelectedValue(T value) {
        getDataComponent().setSelectedIndex(values.indexOf(value));
    }

//    @Override
    public void refreshData() {
        
        T oldVal = getSelectedValue();
        
        this.removeAllElements();
        values.clear();
        
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = ReportConnectionManager.getReportConnection(connectionID);
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs = stmt.executeQuery(SQL);
            
            // check if we need to initialize some variables
            if (valueColumn <= 0) {
                rs.first();
                if (rs.getMetaData().getColumnCount() == 1) {
                    valueColumn = 1;
                } else {
                    valueColumn = 2;
                }
                valueType = rs.getMetaData().getColumnType(valueColumn);
                rs.beforeFirst();
            }
            logger.debug("Value Column is: " + valueColumn);
            while (rs.next()) {
                logger.trace("adding element: " + rs.getString(1) + " : " + rs.getString(valueColumn));
                switch (valueType) {
                    case Types.VARCHAR:
                        addElement(rs.getString(1));
                        values.add((T) rs.getString(valueColumn));
                        break;
                    case Types.BIGINT:
                        addElement(rs.getString(1));
                        values.add((T)(Long) rs.getLong(valueColumn));
                        break;
                    case Types.INTEGER:
                        addElement(rs.getString(1));
                        values.add((T)(Integer) rs.getInt(valueColumn));
                    default:
                        logger.warn("IS USING DEFAULT!!!");
                        addElement(rs.getString(1));
                        values.add((T) rs.getString(valueColumn));
                }
            }
            
            if (oldVal != null) {
                int index = values.indexOf(oldVal);
                if (index >= 0) setSelectedValue(values.get(index));
            } else if (defaultValue != null) {
                int index = values.indexOf(defaultValue);
                if (index >= 0) setSelectedValue(values.get(index));
            }
            
        } catch (Exception ex) {
            logger.info("SQL causing error is: =========\n" + SQL + "\n========== End SQL");
            logger.error("Error refreshing data! " + ex.toString(), ex);
        } finally {
            try {
                if (rs != null && !rs.isClosed()) {
                    rs.close();
                }
                rs = null;
                if (stmt != null && !stmt.isClosed()) {
                    stmt.close();
                }
                stmt = null;
            } catch (Exception ex) {
                logger.error("Error closing connection: " + ex.toString(), ex);
            }
        }
    }
    
    
    
}
