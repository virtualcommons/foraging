package edu.asu.commons.foraging.client;

import java.awt.Component;
import java.util.Arrays;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import edu.asu.commons.foraging.conf.RoundConfiguration;
import javax.sound.midi.SysexMessage;
import javax.swing.ButtonModel;
import javax.swing.table.TableColumn;

/**
 * $Id:$
 * 
 * @author alllee
 */
public class TrustGamePanel extends JPanel {

    private static final long serialVersionUID = 4780102066737046088L;
    private RoundConfiguration roundConfiguration;
    private ForagingClient client;

    private class PlayerTwoTableModel extends AbstractTableModel {

        private static final long serialVersionUID = 8044821545875471685L;
        private String[] columnNames = { "Amount sent by P1", "Total amount received", "Amount to keep", "Amount to return to P1" };
        private Object[][] columnDataTable = {

                { "0 cents", "(3 x 0) = 0 cents", 0.0d, 0.0d },
                { "25 cents", "(3 x 0.25) = 75 cents", "", "" },
                { "50 cents", "(3 x 0.5) = 1.5 dollars", "", "" },
                { "75 cents", "(3 x 0.75) = 2.25 dollars", "", "" },
                { "1 dollar", "(3 x 1) = 3 dollars", "", "" }
        };

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public String getColumnName(int col) {
            return columnNames[col];
        }

        @Override
        public int getRowCount() {
            return columnDataTable.length;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return columnDataTable[rowIndex][columnIndex];
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            // Note that the data/cell address is constant,
            // no matter where the cell appears onscreen.
            return (col == 2) && (row != 0);
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            if (value == null) {
                return;
            }
            try {
                Double amount = Double.parseDouble(value.toString());
                columnDataTable[row][col] = amount;
                Double totalAmount = row * 3 * roundConfiguration.getTrustGamePayoffIncrement();
                columnDataTable[row][col + 1] = totalAmount - amount;
                fireTableCellUpdated(row, col + 1);
            } catch (NumberFormatException exception) {
                JOptionPane.showMessageDialog(TrustGamePanel.this, "Please enter a valid number.");
                return;
            }
        }
    }

    private class PlayerTwoInputColumnCellEditor extends DefaultCellEditor {
        private static final long serialVersionUID = -981239232309467766L;

        public PlayerTwoInputColumnCellEditor() {
            super(new JComboBox());
        }

        public Component getTableCellEditorComponent(JTable table, Object value, boolean selected, int row, int column) {
            JComboBox combo = (JComboBox) super.getTableCellEditorComponent(table, value, selected, row, column);
            combo.removeAllItems();
            int numberOfQuarters = row * 3;
            for (int i = 0; i <= numberOfQuarters; i++) {
                combo.addItem(Double.valueOf(i * roundConfiguration.getTrustGamePayoffIncrement()));
            }
            return combo;
        }
    }

    private TableModel playerTwoTableModel;

    private TableModel getPlayerTwoTableModel() {
        if (playerTwoTableModel == null) {
            playerTwoTableModel = new PlayerTwoTableModel();
        }
        return playerTwoTableModel;
    }

    /** Creates new form TrustGamePanel */
    public TrustGamePanel() {
        initComponents();
    }

    public TrustGamePanel(ForagingClient client) {
        this();
        this.client = client;
        setRoundConfiguration(client.getCurrentRoundConfiguration());
        TableColumn column = playerTwoTable.getColumnModel().getColumn(2);
        column.setCellEditor(new DefaultCellEditor(new JComboBox()) {          
            public Component getTableCellEditorComponent(JTable table, Object value, boolean selected, int row, int column) {
                JComboBox combo = (JComboBox) super.getTableCellEditorComponent(table, value, selected, row, column);
                combo.removeAllItems();
                int numberOfQuarters = row * 3;
                for (int i = 0; i <= numberOfQuarters; i++) {
                    combo.addItem(Double.valueOf(i * roundConfiguration.getTrustGamePayoffIncrement()));
                }
                return combo;
            }
        });
    }

    public void setRoundConfiguration(RoundConfiguration configuration) {
        this.roundConfiguration = configuration;
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        playerOneActionButtonGroup = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        playerOneAmountKeptLabel = new javax.swing.JLabel();
        playerOneAmountSentLabel = new javax.swing.JLabel();
        playerOneAmountReceived = new javax.swing.JLabel();
        playerOneAction = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        playerOneRadioButton1 = new javax.swing.JRadioButton();
        playerOneRadioButton2 = new javax.swing.JRadioButton();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        playerOneRadioButton3 = new javax.swing.JRadioButton();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        playerOneRadioButton4 = new javax.swing.JRadioButton();
        playerOneRadioButton5 = new javax.swing.JRadioButton();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        playerTwoLabel = new javax.swing.JLabel();
        submitButton = new javax.swing.JButton();
        jLabel16 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        playerTwoTable = new javax.swing.JTable();
        playerOneTableLabel = new javax.swing.JLabel();

        playerOneAmountKeptLabel.setFont(new java.awt.Font("Trebuchet MS", 1, 15)); // NOI18N
        playerOneAmountKeptLabel.setText("Amount to keep");
        playerOneAmountKeptLabel.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        playerOneAmountSentLabel.setFont(new java.awt.Font("Trebuchet MS", 1, 15)); // NOI18N
        playerOneAmountSentLabel.setText("Amount to send to P2");
        playerOneAmountSentLabel.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        playerOneAmountReceived.setFont(new java.awt.Font("Trebuchet MS", 1, 15)); // NOI18N
        playerOneAmountReceived.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        playerOneAmountReceived.setText("Amount received by P2   ");
        playerOneAmountReceived.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        playerOneAction.setFont(new java.awt.Font("Trebuchet MS", 1, 15)); // NOI18N
        playerOneAction.setText("Select");
        playerOneAction.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("0 cents");
        jLabel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel2.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("1 dollar");
        jLabel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel4.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setText("(3 x 1) = 3 dollars");
        jLabel4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jLabel4.setFocusable(false);

        jLabel5.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel5.setText("(3 x 0.75) = 2.25 dollars");
        jLabel5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jLabel5.setFocusable(false);

        jLabel3.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("75 cents");
        jLabel3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel6.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel6.setText("25 cents");
        jLabel6.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        playerOneActionButtonGroup.add(playerOneRadioButton1);
        playerOneRadioButton1.setActionCommand("0");

        playerOneActionButtonGroup.add(playerOneRadioButton2);
        playerOneRadioButton2.setActionCommand("1");

        jLabel7.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel7.setText("50 cents");
        jLabel7.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel8.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel8.setText("50 cents");
        jLabel8.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel9.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel9.setText("(3 x 0.5) = 1.5 dollars");
        jLabel9.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jLabel9.setFocusable(false);

        playerOneActionButtonGroup.add(playerOneRadioButton3);
        playerOneRadioButton3.setActionCommand("2");

        jLabel10.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel10.setText("75 cents");
        jLabel10.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel11.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel11.setText("25 cents");
        jLabel11.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel12.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel12.setText("(3 x 0.25) = 0.75 cents");
        jLabel12.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jLabel12.setFocusable(false);

        playerOneActionButtonGroup.add(playerOneRadioButton4);
        playerOneRadioButton4.setActionCommand("3");

        playerOneActionButtonGroup.add(playerOneRadioButton5);
        playerOneRadioButton5.setActionCommand("4");

        jLabel13.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel13.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel13.setText("1 dollar");
        jLabel13.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel14.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel14.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel14.setText("0 cents");
        jLabel14.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel15.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel15.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel15.setText("(3 x 0) = 0 cents");
        jLabel15.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jLabel15.setFocusable(false);

        playerTwoLabel.setFont(new java.awt.Font("Trebuchet MS", 1, 15)); // NOI18N
        playerTwoLabel.setText("Player 2: Please enter data for ALL of the following allocations.");

        submitButton.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        submitButton.setText("Submit");
        submitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                submitButtonActionPerformed(evt);
            }
        });

        jLabel16.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel16.setText("Click in the \"Amount to keep\" column to select how much to keep if you are selected as player 2.");

        playerTwoTable.setFont(new java.awt.Font("Trebuchet MS", 0, 15)); // NOI18N
        playerTwoTable.setModel(getPlayerTwoTableModel());
        jScrollPane1.setViewportView(playerTwoTable);

        playerOneTableLabel.setFont(new java.awt.Font("Trebuchet MS", 1, 15)); // NOI18N
        playerOneTableLabel.setText("Player 1: Please select one of the following allocations.");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel16)
                        .addContainerGap(103, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(playerOneTableLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 483, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(playerOneAmountKeptLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(playerOneAmountSentLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(playerOneAmountReceived, javax.swing.GroupLayout.DEFAULT_SIZE, 191, Short.MAX_VALUE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(playerOneAction)
                        .addGap(302, 302, 302))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(playerTwoLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 831, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(submitButton)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 672, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(22, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, 191, Short.MAX_VALUE)
                                .addGap(6, 6, 6))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel11, javax.swing.GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE)
                                        .addGap(197, 197, 197))
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                                        .addGap(292, 292, 292)
                                        .addComponent(jLabel12, javax.swing.GroupLayout.DEFAULT_SIZE, 191, Short.MAX_VALUE)))
                                .addGap(6, 6, 6))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel14, javax.swing.GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel15, javax.swing.GroupLayout.DEFAULT_SIZE, 191, Short.MAX_VALUE)
                                .addGap(6, 6, 6))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE)
                                    .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE)
                                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, 191, Short.MAX_VALUE)
                                        .addGap(6, 6, 6))
                                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, 191, Short.MAX_VALUE))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(playerOneRadioButton5)
                            .addComponent(playerOneRadioButton4)
                            .addComponent(playerOneRadioButton3)
                            .addComponent(playerOneRadioButton2)
                            .addComponent(playerOneRadioButton1))
                        .addGap(331, 331, 331))))
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jLabel10, jLabel13, jLabel6, jLabel7, playerOneAmountKeptLabel});

        jPanel1Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jLabel11, jLabel14, jLabel2, jLabel3, jLabel8});

        jPanel1Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jLabel12, jLabel15, jLabel4, jLabel5, jLabel9});

        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(playerOneTableLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(playerOneAmountKeptLabel)
                    .addComponent(playerOneAmountSentLabel)
                    .addComponent(playerOneAmountReceived)
                    .addComponent(playerOneAction))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel1)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel4)
                        .addComponent(jLabel2))
                    .addComponent(playerOneRadioButton1))
                .addGap(6, 6, 6)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(playerOneRadioButton2, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.TRAILING))
                .addGap(9, 9, 9)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel9, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel8, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(playerOneRadioButton3, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel10)
                    .addComponent(jLabel11)
                    .addComponent(jLabel12)
                    .addComponent(playerOneRadioButton4))
                .addGap(10, 10, 10)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(playerOneRadioButton5)
                    .addComponent(jLabel13)
                    .addComponent(jLabel14)
                    .addComponent(jLabel15))
                .addGap(29, 29, 29)
                .addComponent(playerTwoLabel)
                .addGap(12, 12, 12)
                .addComponent(jLabel16)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(submitButton)
                .addContainerGap(23, Short.MAX_VALUE))
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {jLabel1, jLabel10, jLabel13, jLabel6, jLabel7});

        jPanel1Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {jLabel12, jLabel15, jLabel4, jLabel5, jLabel9});

        jPanel1Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {jLabel11, jLabel14, jLabel2, jLabel3, jLabel8});

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 704, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(22, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(21, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void submitButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_submitButtonActionPerformed
    // TODO add your handling code here:
        ButtonModel model = playerOneActionButtonGroup.getSelection();
        // default player action is to keep everything
        
        if (model == null) {
            JOptionPane.showMessageDialog(this, "Please select the amount you would like to keep as player 1.");
            return;
        }
        String selectedPlayerOneAction = model.getActionCommand();
        System.err.println("player one action: " + selectedPlayerOneAction);               
        double playerOneAmountToKeep = (Integer.parseInt(selectedPlayerOneAction) * roundConfiguration.getTrustGamePayoffIncrement());
        double[] playerTwoAmountsToKeep = new double[4];        
        for (int rowIndex = 1; rowIndex <= 4; rowIndex++) {
            Object value = playerTwoTable.getValueAt(rowIndex, 2);
            System.err.println("value is: " + value);
            if (value == null || "".equals(value)) {
                JOptionPane.showMessageDialog(this, "Please enter the amount you would like to keep as player 2.");
                playerTwoTable.setColumnSelectionAllowed(true);
                playerTwoTable.setColumnSelectionInterval(2, 2);
                return;
            }
            playerTwoAmountsToKeep[rowIndex - 1] = (Double) value;            
        }        
        client.sendTrustGameSubmissionRequest(playerOneAmountToKeep, playerTwoAmountsToKeep);
        client.getGameWindow2D().switchInstructionsPane();
        
        
        

    }// GEN-LAST:event_submitButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel playerOneAction;
    private javax.swing.ButtonGroup playerOneActionButtonGroup;
    private javax.swing.JLabel playerOneAmountKeptLabel;
    private javax.swing.JLabel playerOneAmountReceived;
    private javax.swing.JLabel playerOneAmountSentLabel;
    private javax.swing.JRadioButton playerOneRadioButton1;
    private javax.swing.JRadioButton playerOneRadioButton2;
    private javax.swing.JRadioButton playerOneRadioButton3;
    private javax.swing.JRadioButton playerOneRadioButton4;
    private javax.swing.JRadioButton playerOneRadioButton5;
    private javax.swing.JLabel playerOneTableLabel;
    private javax.swing.JLabel playerTwoLabel;
    private javax.swing.JTable playerTwoTable;
    private javax.swing.JButton submitButton;
    // End of variables declaration//GEN-END:variables
}
