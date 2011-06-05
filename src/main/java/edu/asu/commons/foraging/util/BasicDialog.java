package edu.asu.commons.foraging.util;

import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

public class BasicDialog extends javax.swing.JFrame {

	public void bindUsableComponents(Container container)
    {
        Component[] c = container.getComponents();
        for(int j = 0; j < c.length; j++)
        {
            // isfocusable is new in j2se 1.4, may be ok without it
            if(c[j] instanceof JComponent && c[j].isFocusable() &&
                !(c[j] instanceof JPanel))
            {
                System.out.println(c[j].getClass().getName());
                addKeyBinding((JComponent)c[j]);
            }
            if(((Container)c[j]).getComponentCount() > 0)
                bindUsableComponents((Container)c[j]);
        }
    }
 
    private void addKeyBinding(JComponent c)
    {
        Object name = escape.getValue(Action.NAME);
        c.getInputMap().put(KeyStroke.getKeyStroke("ESCAPE"), name);
        c.getActionMap().put(name, escape);
    }
 
    private Action escape = new AbstractAction()
    {
        { putValue(NAME, "escape"); }
 
        public void actionPerformed(ActionEvent e)
        {
            JComponent source = (JComponent)e.getSource();
            Window window = SwingUtilities.getWindowAncestor(source);
            window.dispose();            
        }
    };
}
