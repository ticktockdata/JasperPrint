
package com.ticktockdata.jasper;

/**
 *
 * @author JAM {javajoe@programmer.net}
 * @since Apr 03, 2019
 * 
 */
@FunctionalInterface
public interface PrintStatusListener {
    
    public void statusChanged(PrintStatusEvent evt);
    
    
}
