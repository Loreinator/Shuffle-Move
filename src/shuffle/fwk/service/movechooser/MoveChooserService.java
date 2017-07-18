/*  ShuffleMove - A program for identifying and simulating ideal moves in the game
 *  called Pokemon Shuffle.
 *  
 *  Copyright (C) 2015  Andrew Meyers
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package shuffle.fwk.service.movechooser;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import shuffle.fwk.config.ConfigManager;
import shuffle.fwk.config.EntryType;
import shuffle.fwk.data.simulation.SimulationResult;
import shuffle.fwk.data.simulation.util.NumberSpan;
import shuffle.fwk.gui.GradingModeIndicator;
import shuffle.fwk.gui.ShuffleMenuBar;
import shuffle.fwk.gui.user.ShuffleMenuUser;
import shuffle.fwk.i18n.I18nUser;
import shuffle.fwk.service.BaseService;
import shuffle.fwk.service.DisposeAction;

/**
 * @author Andrew Meyers
 *         
 */
public class MoveChooserService extends BaseService<ShuffleMenuUser>implements I18nUser, Observer {
   
   /** The logger for this service. */
   private static final Logger LOG = Logger.getLogger(MoveChooserService.class.getName());
   
   // i18n keys
   private static final String KEY_METRIC_LABEL = "text.metric.label";
   private static final String KEY_COMPUTE_NOW = "button.computenow";
   private static final String KEY_DO_NOW = "button.donow";
   private static final String KEY_CLOSE = "button.close";
   private static final String KEY_TITLE = "text.title";
   private static final String KEY_RESULT_FORMAT_MOVE = "format.result.move";
   private static final String KEY_RESULT_FORMAT_SETTLE = "format.result.settle";
   private static final String KEY_HEADER_RANK = "column.rank";
   private static final String KEY_HEADER_MOVE = "column.move";
   private static final String KEY_HEADER_GOLD = "column.gold";
   private static final String KEY_HEADER_POINTS = "column.points";
   private static final String KEY_HEADER_COMBOS = "column.combos";
   private static final String KEY_HEADER_BLOCKS = "column.blocks";
   private static final String KEY_HEADER_DISRUPTIONS = "column.disruptions";
   private static final String KEY_HEADER_MEGASTATE = "column.megastate";
   private static final String KEY_METRIC_TOOLTIP = "tooltip.metric";
   private static final String KEY_DO_TOOLTIP = "tooltip.donow";
   private static final String KEY_CLOSE_TOOLTIP = "tooltip.close";
   
   // config keys
   private static final String KEY_CHOOSER_X = "CHOOSER_X";
   private static final String KEY_CHOOSER_Y = "CHOOSER_Y";
   private static final String KEY_CHOOSER_WIDTH = "CHOOSER_WIDTH";
   private static final String KEY_CHOOSER_HEIGHT = "CHOOSER_HEIGHT";
   private static final String KEY_CHOOSER_RESIZE = "CHOOSER_RESIZE";
   public static final String KEY_CHOOSER_AUTOLAUNCH = "CHOOSER_AUTOLAUNCH";
   private static final String KEY_COLUMN_ORDER = "COLUMN_ORDER";
   
   // defaults
   private static final String MOVE_FORMAT = "%d,%d -> %d,%d";
   private static final String SETTLE = "Settle";
   private static final int DEFAULT_CHOOSER_WIDTH = 400;
   private static final int DEFAULT_CHOOSER_HEIGHT = 400;
   private static final boolean DEFAULT_RESIZE = true;
   private static final Dimension MIN_SIZE = new Dimension(100, 100);
   
   // components
   private ShuffleMenuBar shuffleMenuBar = null;
   private JDialog d = null;
   private JLabel metricLabel = null;
   private GradingModeIndicator ind = null;
   private JButton doMoveButton = null;
   private JButton closeButton = null;
   private JTable table = null;
   private DefaultTableModel model2 = null;
   private ListSelectionListener listener2 = null;
   private List<SimulationResult> results = null;
   private Map<SimulationResult, Integer> resultsMap = null;
   
   /*
    * (non-Javadoc)
    * @see shuffle.fwk.service.BaseService#getUserClass()
    */
   @Override
   protected Class<ShuffleMenuUser> getUserClass() {
      return ShuffleMenuUser.class;
   }
   
   /*
    * (non-Javadoc)
    * @see shuffle.fwk.service.BaseService#setupGUI()
    */
   @Override
   public void onSetupGUI() {
      d = new JDialog(getOwner());
      d.setLayout(new GridBagLayout());
      d.setTitle(getString(KEY_TITLE));
      shuffleMenuBar = new ShuffleMenuBar(getUser(), getOwner());
      d.setJMenuBar(shuffleMenuBar);
      
      GridBagConstraints c = new GridBagConstraints();
      c.fill = GridBagConstraints.BOTH;
      c.anchor = GridBagConstraints.CENTER;
      c.weightx = 1.0;
      c.weighty = 1.0;
      c.gridy = 1;
      
      // Display row
      c.weighty = 1.0;
      c.gridwidth = 4;
      c.gridx = 1;
      c.gridy++;
      
      c.gridx++;
      results = new ArrayList<SimulationResult>();
      resultsMap = new HashMap<SimulationResult, Integer>();
      model2 = new DefaultTableModel(getColumnNames(), 0) {
         private static final long serialVersionUID = 5180830497930828902L;
         
         @Override
         public boolean isCellEditable(int row, int col) {
            return false;
         }
      };
      table = new JTable(model2);
      loadOrderFor(table, getUser().getPreferencesManager().getStringValue(KEY_COLUMN_ORDER));
      resizeColumnWidth(table);
      table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      listener2 = new ListSelectionListener() {
         
         @Override
         public void valueChanged(ListSelectionEvent e) {
            pushSelectionToUser2();
         }
      };
      table.getSelectionModel().addListSelectionListener(listener2);
      JScrollPane jsp = new JScrollPane(table);
      d.add(jsp, c);
      
      // Control row
      c.weighty = 0.0;
      c.gridwidth = 1;
      c.gridx = 1;
      c.gridy++;
      
      c.gridx++;
      metricLabel = new JLabel(getString(KEY_METRIC_LABEL));
      metricLabel.setHorizontalAlignment(SwingConstants.CENTER);
      metricLabel.setHorizontalTextPosition(SwingConstants.CENTER);
      metricLabel.setToolTipText(getString(KEY_METRIC_TOOLTIP));
      d.add(metricLabel, c);
      
      c.gridx++;
      ind = new GradingModeIndicator(getUser());
      ind.setToolTipText(getString(KEY_METRIC_TOOLTIP));
      d.add(ind, c);
      
      c.gridx++;
      doMoveButton = new JButton(new AbstractAction(getString(KEY_DO_NOW)) {
         private static final long serialVersionUID = -8952138130413953491L;
         
         @Override
         public void actionPerformed(ActionEvent arg0) {
            doMovePressed();
         }
      });
      doMoveButton.setToolTipText(getString(KEY_DO_TOOLTIP));
      d.add(doMoveButton, c);
      setDefaultButton(doMoveButton);
      
      c.gridx++;
      closeButton = new JButton(new DisposeAction(getString(KEY_CLOSE), this));
      closeButton.setToolTipText(getString(KEY_CLOSE_TOOLTIP));
      d.add(closeButton, c);
      d.pack();
      
      ConfigManager preferencesManager = getUser().getPreferencesManager();
      final int width = preferencesManager.getIntegerValue(KEY_CHOOSER_WIDTH, DEFAULT_CHOOSER_WIDTH);
      final int height = preferencesManager.getIntegerValue(KEY_CHOOSER_HEIGHT, DEFAULT_CHOOSER_HEIGHT);
      d.setSize(width, height);
      d.repaint();
      Integer x = preferencesManager.getIntegerValue(KEY_CHOOSER_X);
      Integer y = preferencesManager.getIntegerValue(KEY_CHOOSER_Y);
      if (x != null && y != null) {
         d.setLocation(x, y);
      } else {
         d.setLocationRelativeTo(null);
      }
      d.setMinimumSize(MIN_SIZE);
      d.setResizable(preferencesManager.getBooleanValue(KEY_CHOOSER_RESIZE, DEFAULT_RESIZE));
      getUser().addObserver(this);
      setDialog(d);
   }
   
   /**
    * @param table2
    * @param stringValue
    */
   private void loadOrderFor(JTable table2, String stringValue) {
      if (stringValue == null) {
         return;
      }
      String[] tokens = stringValue.split(",");
      List<Integer> toIndexes = new ArrayList<Integer>();
      try {
         for (String token : tokens) {
            Integer toIndex = Integer.parseInt(token);
            toIndexes.add(toIndex);
         }
         sortColumnsBy(toIndexes);
      } catch (NumberFormatException nfe) {
         LOG.warning("Corrupted configuration for the table load order, ignoring...");
      }
   }
   
   /**
    * @param toIndexes
    */
   private void sortColumnsBy(List<Integer> toIndexes) {
      if (toIndexes == null || toIndexes.isEmpty()) {
         return;
      }
      List<TableColumn> columns = new ArrayList<TableColumn>();
      TableColumnModel tcm = table.getColumnModel();
      for (int index = 0; index < tcm.getColumnCount(); index++) {
         columns.add(tcm.getColumn(index));
      }
      columns.sort(new Comparator<TableColumn>() {
         @Override
         public int compare(TableColumn o1, TableColumn o2) {
            return Integer.compare(o1.getModelIndex(), o2.getModelIndex());
         }
      });
      
      while (tcm.getColumnCount() > 0) {
         tcm.removeColumn(tcm.getColumn(0));
      }
      
      for (Integer index : toIndexes) {
         tcm.addColumn(columns.get(index));
      }
   }
   
   /**
    * From Stackoverflow: http://stackoverflow.com/a/17627497
    * 
    * @param table
    *           The table to be resized
    */
   public void resizeColumnWidth(JTable table) {
      table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
      final TableColumnModel columnModel = table.getColumnModel();
      for (int column = 0; column < table.getColumnCount(); column++) {
         TableColumn tableColumn = columnModel.getColumn(column);
         TableCellRenderer r = tableColumn.getHeaderRenderer();
         if (r == null) {
            r = table.getTableHeader().getDefaultRenderer();
         }
         Component component = r.getTableCellRendererComponent(table, tableColumn.getHeaderValue(), false, false, 0,
               column);
         int width = component.getPreferredSize().width;
         for (int row = 0; row < table.getRowCount(); row++) {
            TableCellRenderer renderer = table.getCellRenderer(row, column);
            Component comp = table.prepareRenderer(renderer, row, column);
            width = Math.max(comp.getPreferredSize().width + 1, width);
         }
         tableColumn.setPreferredWidth(width);
      }
   }
   
   /**
    * @return
    */
   private Vector<String> getColumnNames() {
      Vector<String> ret = new Vector<String>();
      ret.add(getString(KEY_HEADER_RANK));
      ret.add(getString(KEY_HEADER_MOVE));
      ret.add(getString(KEY_HEADER_GOLD));
      ret.add(getString(KEY_HEADER_POINTS));
      ret.add(getString(KEY_HEADER_COMBOS));
      ret.add(getString(KEY_HEADER_BLOCKS));
      ret.add(getString(KEY_HEADER_DISRUPTIONS));
      ret.add(getString(KEY_HEADER_MEGASTATE));
      return ret;
   }
   
   @Override
   protected void onLaunch() {
      ConfigManager preferencesManager = getUser().getPreferencesManager();
      preferencesManager.setEntry(EntryType.BOOLEAN, KEY_CHOOSER_AUTOLAUNCH, true);
      d.addComponentListener(new ComponentAdapter() {
         @Override
         public void componentMoved(ComponentEvent ev) {
            ConfigManager preferencesManager = getUser().getPreferencesManager();
            Point p = d.getLocation();
            int x = new Double(p.getX()).intValue();
            int y = new Double(p.getY()).intValue();
            preferencesManager.setEntry(EntryType.INTEGER, KEY_CHOOSER_X, x);
            preferencesManager.setEntry(EntryType.INTEGER, KEY_CHOOSER_Y, y);
            correctPosition(d, x, y, preferencesManager);
         }
         
         @Override
         public void componentResized(ComponentEvent ev) {
            ConfigManager preferencesManager = getUser().getPreferencesManager();
            Dimension dim = d.getSize();
            preferencesManager.setEntry(EntryType.INTEGER, KEY_CHOOSER_WIDTH, dim.width);
            preferencesManager.setEntry(EntryType.INTEGER, KEY_CHOOSER_HEIGHT, dim.height);
         }
      });
      table.getColumnModel().addColumnModelListener(new TableColumnModelListener() {
         
         @Override
         public void columnSelectionChanged(ListSelectionEvent e) {
         }
         
         @Override
         public void columnRemoved(TableColumnModelEvent e) {
         }
         
         @Override
         public void columnMoved(TableColumnModelEvent e) {
            if (e.getFromIndex() != e.getToIndex()) {
               StringBuilder sb = new StringBuilder();
               Enumeration<TableColumn> columns = table.getColumnModel().getColumns();
               while (columns.hasMoreElements()) {
                  TableColumn nextElement = columns.nextElement();
                  sb.append(String.format("%s,", nextElement.getModelIndex()));
               }
               getUser().getPreferencesManager().setEntry(EntryType.STRING, KEY_COLUMN_ORDER, sb.toString());
            }
         }
         
         @Override
         public void columnMarginChanged(ChangeEvent e) {
         }
         
         @Override
         public void columnAdded(TableColumnModelEvent e) {
         }
      });
   }
   
   @Override
   protected void onDispose() {
      getUser().deleteObserver(this);
      ConfigManager preferencesManager = getUser().getPreferencesManager();
      preferencesManager.setEntry(EntryType.BOOLEAN, KEY_CHOOSER_AUTOLAUNCH, false);
   }
   
   /*
    * (non-Javadoc)
    * @see shuffle.fwk.service.BaseService#updateGUIFrom(java.lang.Object)
    */
   @Override
   protected void updateGUIFrom(ShuffleMenuUser user) {
      updateTooltips(user);
      updateComponentText(user);
      Collection<SimulationResult> userResults = user.getResults();
      SimulationResult selectResult = user.getSelectedResult();
      if (listener2 != null) {
         table.getSelectionModel().removeListSelectionListener(listener2);
         results.clear();
         resultsMap.clear();
         loadOrderFor(table, user.getPreferencesManager().getStringValue(KEY_COLUMN_ORDER));
         if (model2.getRowCount() > 0) {
            for (int i = model2.getRowCount() - 1; i >= 0; i--) {
               model2.removeRow(i);
            }
         }
         Vector<String> columNames = getColumnNames();
         Vector<String> curNames = new Vector<String>();
         for (int i = 0; i < model2.getColumnCount(); i++) {
            curNames.add(model2.getColumnName(i));
         }
         if (!curNames.equals(columNames)) {
            model2.setColumnIdentifiers(columNames);
         }
         if (userResults != null) {
            for (SimulationResult result : userResults) {
               resultsMap.put(result, results.size());
               model2.addRow(getVectorFor(result, results.size() + 1));
               results.add(result);
            }
         }
         if (selectResult == null) {
            table.clearSelection();
            
         } else {
            int row = resultsMap.get(selectResult);
            table.setRowSelectionInterval(row, row);
         }
         resizeColumnWidth(table);
         table.repaint();
         table.getSelectionModel().addListSelectionListener(listener2);
      }
      ind.repaint();
   }
   
   /**
    * Updates the tooltip text based on the current language.
    */
   private void updateTooltips(ShuffleMenuUser user) {
      String metricTT = getString(KEY_METRIC_TOOLTIP);
      String doMoveTT = getString(KEY_DO_TOOLTIP);
      String closeTT = getString(KEY_CLOSE_TOOLTIP);
      if (!metricTT.equals(metricLabel.getToolTipText())) {
         metricLabel.setToolTipText(getString(KEY_METRIC_TOOLTIP));
         ind.setToolTipText(getString(KEY_METRIC_TOOLTIP));
      }
      if (user.getSelectedResult() == null) {
         doMoveButton.setToolTipText(null);
      } else if (!doMoveTT.equals(doMoveButton.getToolTipText())) {
         doMoveButton.setToolTipText(getString(KEY_DO_TOOLTIP));
      }
      if (!closeTT.equals(closeButton.getToolTipText())) {
         closeButton.setToolTipText(getString(KEY_CLOSE_TOOLTIP));
      }
   }
   
   private void updateComponentText(ShuffleMenuUser user) {
      String metricLabelText = getString(KEY_METRIC_LABEL);
      String doMoveText = getString(KEY_DO_NOW);
      String computeNowText = getString(KEY_COMPUTE_NOW);
      String closeText = getString(KEY_CLOSE);
      String titleText = getString(KEY_TITLE);
      if (!metricLabelText.equals(metricLabel.getText())) {
         metricLabel.setText(metricLabelText);
      }
      if (user.getSelectedResult() == null) {
         if (!computeNowText.equals(doMoveButton.getText())) {
            doMoveButton.setText(computeNowText);
         }
      } else if (!doMoveText.equals(doMoveButton.getText())) {
         doMoveButton.setText(doMoveText);
      }
      if (!closeText.equals(closeButton.getText())) {
         closeButton.setText(closeText);
      }
      if (!titleText.equals(d.getTitle())) {
         d.setTitle(titleText);
      }
   }
   
   /**
    * @param result
    * @return
    */
   private Vector<String> getVectorFor(SimulationResult result, int rank) {
      if (result == null) {
         return null;
      }
      String settleText = getString(KEY_RESULT_FORMAT_SETTLE);
      if (settleText.equals(KEY_RESULT_FORMAT_SETTLE)) {
         settleText = SETTLE;
      }
      List<Integer> move = result.getMove();
      int row1 = 0;
      int row2 = 0;
      int column1 = 0;
      int column2 = 0;
      if (move != null && move.size() >= 2) {
         row1 = move.get(0);
         column1 = move.get(1);
         if (move.size() >= 4) {
            row2 = move.get(2);
            column2 = move.get(3);
         }
      }
      String moveText = getString(KEY_RESULT_FORMAT_MOVE, row1, column1, row2, column2);
      if (moveText.equals(KEY_RESULT_FORMAT_MOVE)) {
         moveText = String.format(MOVE_FORMAT, row1, column1, row2, column2);
      }
      String firstPart = settleText;
      if (row1 != 0 || column2 != 0 || row2 != 0 || column2 != 0) {
         firstPart = moveText;
      }
      
      NumberSpan gold = result.getNetGold();
      NumberSpan score = result.getNetScore();
      NumberSpan combos = result.getCombosCleared();
      NumberSpan blocks = result.getBlocksCleared();
      NumberSpan disrupts = result.getDisruptionsCleared();
      NumberSpan mega = result.getProgress();
      
      return new Vector<String>(Arrays.asList(Integer.toString(rank), firstPart.toString(), gold.toString(),
            score.toString(), combos.toString(), blocks.toString(), disrupts.toString(), mega.toString()));
   }
   
   private void pushSelectionToUser2() {
      int selectedRow = table.getSelectedRow();
      if (selectedRow >= 0 && results.size() > selectedRow) {
         SimulationResult result = results.get(selectedRow);
         getUser().setSelectedResult(result);
      }
   }
   
   private void doMovePressed() {
      if (getUser().getSelectedResult() == null) {
         getUser().computeNow();
      } else {
         getUser().doSelectedMove();
      }
   }
   
   /*
    * (non-Javadoc)
    * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
    */
   @Override
   public void update(Observable arg0, Object arg1) {
      shuffleMenuBar.updateAll();
      updateGUI();
   }
}
