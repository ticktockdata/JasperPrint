
package classicacctapp;

import java.util.UUID;
import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

/**
 *
 * @author TDY {timonyoder@gmail.com}
 */
public abstract class JasperKeyBinder extends AbstractAction {
    
    public JasperKeyBinder(JComponent component, KeyStroke ks) {
        this(component, ks, JComponent.WHEN_IN_FOCUSED_WINDOW);
    }
    
    public JasperKeyBinder(JComponent component, KeyStroke ks, int when) {
        String name = UUID.randomUUID().toString();
        
        InputMap inputMap = component.getInputMap(when);
        inputMap.put(ks, name);
        component.getActionMap().put(name, this);
        
    }
    
}
