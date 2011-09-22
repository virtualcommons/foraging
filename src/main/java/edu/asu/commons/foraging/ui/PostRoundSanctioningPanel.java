package edu.asu.commons.foraging.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import javax.swing.text.html.HTMLEditorKit;

import edu.asu.commons.foraging.client.ForagingClient;
import edu.asu.commons.foraging.conf.RoundConfiguration;
import edu.asu.commons.foraging.event.EndRoundEvent;
import edu.asu.commons.foraging.event.PostRoundSanctionRequest;
import edu.asu.commons.foraging.model.ClientData;
import edu.asu.commons.net.Identifier;

/**
 * $ Id: Exp $
 *  
 * Customized JPanel that enables participants to engage in post-round sanctioning.
 *
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision: 384 $
 */

@SuppressWarnings("serial")
public class PostRoundSanctioningPanel extends JPanel {

	private JTable sanctionTable = null;
	private JButton submitButton;
	private SanctionTableModel sanctionTableModel;
    
    private Map<Identifier, ClientData> clientDataMap;
    private RoundConfiguration configuration;
    private Identifier id;
    
    private ForagingClient client;
    
    public PostRoundSanctioningPanel(EndRoundEvent event, RoundConfiguration configuration, ForagingClient client) {
        this.client = client;
        this.clientDataMap = event.getClientDataMap();
        this.id = event.getId();
        this.configuration = configuration;
        initGuiComponents();
    }
	
	
	private void initGuiComponents() {
		setLayout(new BorderLayout(4, 4));
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.PAGE_AXIS));
        JEditorPane textPane = new JEditorPane();
        textPane.setContentType("text/html");
        textPane.setEditorKit(new HTMLEditorKit());
        textPane.setEditable(false);
        textPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        textPane.setFont(new Font("Arial", Font.TRUETYPE_FONT, 14));
        textPane.setText(configuration.getSanctionInstructions());
        centerPanel.add(textPane);
        JPanel tablePanel = new JPanel();
        tablePanel.setPreferredSize(new Dimension(400, 80));
        tablePanel.setMaximumSize(new Dimension(400, 80));        
        tablePanel.setLayout(new BorderLayout());
        tablePanel.add(getSanctionTable().getTableHeader(), BorderLayout.NORTH);
        tablePanel.add(getSanctionTable(), BorderLayout.CENTER);
        centerPanel.add(tablePanel);
//        centerPanel.add(getSanctionTableScrollPane());
		add(centerPanel, BorderLayout.CENTER);
		add(getSubmitButton(), BorderLayout.SOUTH);
		setName("Post round sanctioning panel");
	}
	
	private JButton getSubmitButton() {
		if (submitButton == null) {
			submitButton = new JButton("Submit");
			submitButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					// generate a PostRoundSanctionEvent and send it on through.
					Map<Identifier, Integer> sanctionTally = sanctionTableModel.getSanctionTally();
					PostRoundSanctionRequest sanctionRequest = new PostRoundSanctionRequest(id, sanctionTally);
					client.transmit(sanctionRequest);
				}
			});
		}
		return submitButton;
	}

	/**
	 * This method initializes sanctionTable	
	 * 	
	 * @return javax.swing.JTable	
	 */
	private JTable getSanctionTable() {
		if (sanctionTable == null) {
			sanctionTableModel = new SanctionTableModel();
			sanctionTable = new SanctionTable(sanctionTableModel);
		}
		return sanctionTable;
	}
	
	
	private final static String[] COLUMN_NAMES = 
	{ "Participant", "Tokens Collected", "Your Adjustments", "Decrease [0-25]" };
	
	private class SanctionTable extends JTable {
		public SanctionTable(AbstractTableModel tableModel) {
			super(tableModel);			
			setDefaultEditor(Integer.class, new IntegerEditor(0, 25));
        	//Set column width equal to the length of the header text
            setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            for (int columnIndex = 0; columnIndex < COLUMN_NAMES.length; columnIndex++) {
            	getColumn(COLUMN_NAMES[columnIndex]).sizeWidthToFit();          	
            }
		}
		
		@Override
		public TableCellRenderer getCellRenderer(int row, int column) {
			DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer();
			cellRenderer.setHorizontalAlignment(JLabel.CENTER);
			if (column < 3 || toIdentifier(row).equals(id)) {
				cellRenderer.setBackground(Color.lightGray);
				cellRenderer.setForeground(Color.white);
			}
			return cellRenderer;
		}
		
		@Override
		public int getSelectedRow() {
			return -1;
		}
	}
    
    private List<ClientData> clientDataList;
    private List<ClientData> getOrderedClientDataList() {
        if (clientDataList == null) {
            clientDataList = new ArrayList<ClientData>(clientDataMap.values());
            Collections.sort(clientDataList, new Comparator<ClientData>() {
                public int compare(ClientData a, ClientData b) {
                    return a.getAssignedNumber() - b.getAssignedNumber();
                }
            });
        }
        return clientDataList;
    }
    
    private Identifier toIdentifier(int index) {
        return getOrderedClientDataList().get(index).getId();
    }
	
	private class SanctionTableModel extends AbstractTableModel {
		private int[] adjustments = new int[getRowCount()];
		private int[] increasingCosts = new int[getRowCount()];
		private int[] decreasingCosts = new int[getRowCount()];
		
		public int getColumnCount() {
			return COLUMN_NAMES.length;
		}

		public Map<Identifier, Integer> getSanctionTally() {
			Map<Identifier, Integer> sanctionTally = new HashMap<Identifier, Integer>();
//            sanctionTally.put(key, value)
            for (int index = 0; index < adjustments.length; index++) {
                int adjustment = adjustments[index];
                sanctionTally.put(toIdentifier(index), adjustment);
            }
			return sanctionTally;
		}

		public int getRowCount() {
			return clientDataMap.size();
		}
        
        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex <= 1) {
                return String.class;
            }
            return Integer.class;
        }
		
		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return (columnIndex > 2) && rowIndex != clientDataMap.get(id).getAssignedNumber() - 1;
		}
		
		@Override
		public String getColumnName(int columnIndex) {
			return COLUMN_NAMES[columnIndex];
		}




        
		public Object getValueAt(int rowIndex, int columnIndex) {
            int assignedNumber = rowIndex + 1;
            Identifier targetId = toIdentifier(rowIndex);
			switch (columnIndex) {
			case 0:
                if (targetId.equals(id)) {
                    return assignedNumber + " (You)";
                }
				return assignedNumber;
			case 1:
				return clientDataMap.get(targetId).getCurrentTokens();
			case 2:
				return formatInteger(adjustments[rowIndex]);
			case 3:
				return decreasingCosts[rowIndex];
			default:
				throw new IllegalArgumentException("Should not have more than 5 columns...");
			}
		}
		
		public void setValueAt(Object value, int rowIndex, int columnIndex) {
			int intValue = ((Integer) value).intValue();
			switch (columnIndex) {
            // FIXME: add logic to prevent people from spending more tokens than they can.
			case 3:
//				increasingCosts[rowIndex] = intValue;
//                decreasingCosts[rowIndex] = 0;
//				adjustments[rowIndex] = intValue * configuration.getSanctionMultiplier();
//                adjustSelfCost();
//				break;
//			case 4:
				decreasingCosts[rowIndex] = intValue;
                increasingCosts[rowIndex] = 0;
				adjustments[rowIndex] = -(intValue * configuration.getSanctionMultiplier());
                adjustSelfCost();
				break;
			default:
				throw new RuntimeException("Should not fallthrough here with column index: " + columnIndex + " and value: " + value);
			}
            fireTableDataChanged();
		}
        
        private void adjustSelfCost() {
            int selfIndex = getOrderedClientDataList().indexOf(clientDataMap.get(id));
            int value = 0;
            for (int i = 0; i < adjustments.length; i++) {
                value -= decreasingCosts[i];
                value -= increasingCosts[i];
            }
            adjustments[selfIndex] = value;
        }
		
		
		private String formatInteger(int value) {
			if (value > 0) {
				return "+" + value;
			}
			else {
				return "" + value;
			}
		}
	}
    
//    public static void main(String[] args) {
//        JFrame frame = new JFrame();
//        
//        Identifier id = new Identifier.Base();
//        ClientData clientData = new ClientData(id);
//        RoundConfiguration configuration = new RoundConfiguration("conf/round4.xml");
//        ServerDataModel serverGameState = new ServerDataModel(configuration);
//        Group group = new Group(serverGameState);
//        clientData.setGroup(group);
//        clientData.setTotalTokens(237);
//        group.addClient(clientData);
//        for (int i = 1; i < 4; i++) {
//            Identifier tempId = new Identifier.Base();
//            ClientData data = new ClientData(tempId);
//            data.setTotalTokens(100 * i);
//            group.addClient(data);
//            data.setGroup(group);
//        }
//        PostRoundSanctioningPanel panel = new PostRoundSanctioningPanel(new EndRoundEvent(id, clientData, false), configuration);
//        frame.add(panel);
//        frame.setSize(new Dimension(600, 400));
//        frame.setVisible(true);
//
//    }
    /**
     * Implements a cell editor that uses a formatted text field
     * to edit Integer values.  From http://java.sun.com/docs/books/tutorial/uiswing/components/table.html
     */
    public static class IntegerEditor extends DefaultCellEditor { //implements FocusListener {
        JFormattedTextField ftf;
        NumberFormat integerFormat;
        private Integer minimum, maximum;
        private boolean DEBUG = false;

        public IntegerEditor(int min, int max) {
            super(new JFormattedTextField());
            ftf = (JFormattedTextField)getComponent();
            minimum = new Integer(min);
            maximum = new Integer(max);

            //Set up the editor for the integer cells.
            integerFormat = NumberFormat.getIntegerInstance();
            NumberFormatter intFormatter = new NumberFormatter(integerFormat);
            intFormatter.setFormat(integerFormat);
            intFormatter.setMinimum(minimum);
            intFormatter.setMaximum(maximum);

            ftf.setFormatterFactory(new DefaultFormatterFactory(intFormatter));
            ftf.setValue(minimum);
            ftf.setHorizontalAlignment(JTextField.CENTER);
            ftf.setFocusLostBehavior(JFormattedTextField.PERSIST);
//            ftf.addFocusListener(this);
            
            //React when the user presses Enter while the editor is
            //active.  (Tab is handled as specified by
            //JFormattedTextField's focusLostBehavior property.)
            ftf.getInputMap().put(KeyStroke.getKeyStroke(
                                            KeyEvent.VK_ENTER, 0),
                                            "check");
            ftf.getActionMap().put("check", new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
            if (!ftf.isEditValid()) { //The text is invalid.
                        if (userSaysRevert()) { //reverted
                    ftf.postActionEvent(); //inform the editor
                }
                    } else try {              //The text is valid,
                        ftf.commitEdit();     //so use it.
                        ftf.postActionEvent(); //stop editing
                    } catch (java.text.ParseException exc) { }
                }
            });           
        }
        
//        public void focusGained(FocusEvent e) {
//        	System.out.println("Focus gained event");
//        	JFormattedTextField ftf = (JFormattedTextField)getComponent();
//            System.out.println("JFormattedTextField " + ftf);
//            ftf.select(0, 1);
//            ftf.selectAll();
//        }
//        
//        //Needed for FocusListener interface.
//        public void focusLost(FocusEvent e) { stopCellEditing(); } //ignore

        //Override to invoke setValue on the formatted text field.
        public Component getTableCellEditorComponent(JTable table,
                Object value, boolean isSelected,
                int row, int column) {
            JFormattedTextField ftf =
                (JFormattedTextField)super.getTableCellEditorComponent(
                    table, value, isSelected, row, column);
            ftf.setValue(value);
            return ftf;
        }

        //Override to ensure that the value remains an Integer.
        public Object getCellEditorValue() {
            JFormattedTextField ftf = (JFormattedTextField)getComponent();
            Object o = ftf.getValue();
            if (o instanceof Integer) {
                return o;
            } else if (o instanceof Number) {
                return new Integer(((Number)o).intValue());
            } else {
                if (DEBUG) {
                    System.out.println("getCellEditorValue: o isn't a Number");
                }
                try {
                    return integerFormat.parseObject(o.toString());
                } catch (ParseException exc) {
                    System.err.println("getCellEditorValue: can't parse o: " + o);
                    return null;
                }
            }
        }

        //Override to check whether the edit is valid,
        //setting the value if it is and complaining if
        //it isn't.  If it's OK for the editor to go
        //away, we need to invoke the superclass's version 
        //of this method so that everything gets cleaned up.
        public boolean stopCellEditing() {
            JFormattedTextField ftf = (JFormattedTextField)getComponent();
            if (ftf.isEditValid()) {
                try {
                    ftf.commitEdit();
                } catch (java.text.ParseException exc) { }
            
            } else { //text is invalid
                if (!userSaysRevert()) { //user wants to edit
                return false; //don't let the editor go away
            } 
            }
            return super.stopCellEditing();
        }

        /** 
         * Lets the user know that the text they entered is 
         * bad. Returns true if the user elects to revert to
         * the last good value.  Otherwise, returns false, 
         * indicating that the user wants to continue editing.
         */
        protected boolean userSaysRevert() {
            Toolkit.getDefaultToolkit().beep();
            ftf.selectAll();
            Object[] options = {"Edit",
                                "Revert"};
            int answer = JOptionPane.showOptionDialog(
                SwingUtilities.getWindowAncestor(ftf),
                "The value must be an integer between "
                + minimum + " and "
                + maximum + ".\n"
                + "You can either continue editing "
                + "or revert to the last valid value.",
                "Invalid Text Entered",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.ERROR_MESSAGE,
                null,
                options,
                options[1]);
            
            if (answer == 1) { //Revert!
                ftf.setValue(ftf.getValue());
            return true;
            }
        return false;
        }
    }
}
