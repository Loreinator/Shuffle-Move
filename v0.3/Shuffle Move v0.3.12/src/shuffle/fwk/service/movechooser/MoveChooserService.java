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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import shuffle.fwk.config.ConfigManager;
import shuffle.fwk.config.EntryType;
import shuffle.fwk.data.simulation.SimulationResult;
import shuffle.fwk.data.simulation.util.NumberSpan;
import shuffle.fwk.gui.GradingModeIndicator;
import shuffle.fwk.i18n.I18nUser;
import shuffle.fwk.service.BaseService;
import shuffle.fwk.service.DisposeAction;

/**
 * @author Andrew Meyers
 *
 */
public class MoveChooserService extends BaseService<MoveChooserServiceUser> implements I18nUser, Observer {
   
   // i18n keys
   private static final String KEY_METRIC_LABEL = "text.metric.label";
   private static final String KEY_DO_NOW = "button.donow";
   private static final String KEY_CLOSE = "button.close";
   private static final String KEY_NO_MOVES = "text.nomoves";
   private static final String KEY_TITLE = "text.title";
   private static final String KEY_RESULT_FORMAT = "format.result";
   private static final String KEY_RESULT_FORMAT_MOVE = "format.result.move";
   private static final String KEY_RESULT_FORMAT_SETTLE = "format.result.settle";
   private static final String KEY_UNDO = "menuitem.undomove";
   private static final String KEY_REDO = "menuitem.redomove";
   private static final String KEY_DO = "menuitem.domove";
   private static final String KEY_MENU_MOVE = "menu.move";
   
   // config keys
   private static final String KEY_CHOOSER_X = "CHOOSER_X";
   private static final String KEY_CHOOSER_Y = "CHOOSER_Y";
   private static final String KEY_CHOOSER_WIDTH = "CHOOSER_WIDTH";
   private static final String KEY_CHOOSER_HEIGHT = "CHOOSER_HEIGHT";
   private static final String KEY_CHOOSER_RESIZE = "CHOOSER_RESIZE";
   public static final String KEY_CHOOSER_AUTOLAUNCH = "CHOOSER_AUTOLAUNCH";
   
   // defaults
   private static final String MOVE_FORMAT = "%d,%d -> %d,%d";
   private static final String SETTLE = "Settle";
   private static final int DEFAULT_CHOOSER_WIDTH = 400;
   private static final int DEFAULT_CHOOSER_HEIGHT = 400;
   private static final boolean DEFAULT_RESIZE = true;
   private static final Dimension MIN_SIZE = new Dimension(100, 100);
   
   // components
   private JDialog d = null;
   private GradingModeIndicator ind = null;
   private JList<String> list = null;
   private DefaultListModel<String> model = null;
   private Map<String, SimulationResult> map = null;
   private ListSelectionListener listener = null;
   
   /*
    * (non-Javadoc)
    * @see shuffle.fwk.service.BaseService#getUserClass()
    */
   @Override
   protected Class<MoveChooserServiceUser> getUserClass() {
      return MoveChooserServiceUser.class;
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
      
      addMenu();
      
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
      map = new HashMap<String, SimulationResult>();
      model = new DefaultListModel<String>();
      list = new JList<String>(model);
      list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      list.setLayoutOrientation(JList.VERTICAL);
      listener = new ListSelectionListener() {
         
         @Override
         public void valueChanged(ListSelectionEvent e) {
            pushSelectionToUser();
         }
      };
      list.addListSelectionListener(listener);
      JScrollPane pane = new JScrollPane(list);
      d.add(pane, c);
      
      // Control row
      c.weighty = 0.0;
      c.gridwidth = 1;
      c.gridx = 1;
      c.gridy++;
      
      c.gridx++;
      JLabel metricLabel = new JLabel(getString(KEY_METRIC_LABEL));
      metricLabel.setHorizontalAlignment(SwingConstants.CENTER);
      metricLabel.setHorizontalTextPosition(SwingConstants.CENTER);
      d.add(metricLabel, c);
      
      c.gridx++;
      ind = new GradingModeIndicator(getUser());
      d.add(ind, c);
      
      c.gridx++;
      JButton doMoveButton = new JButton(new AbstractAction(getString(KEY_DO_NOW)) {
         private static final long serialVersionUID = -8952138130413953491L;
         
         @Override
         public void actionPerformed(ActionEvent arg0) {
            doMove();
         }
      });
      d.add(doMoveButton, c);
      
      c.gridx++;
      JButton closeButton = new JButton(new DisposeAction(getString(KEY_CLOSE), this));
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
    * 
    */
   private void addMenu() {
      AbstractAction doAction = new AbstractAction(getString(KEY_DO)) {
         private static final long serialVersionUID = 5001922828829522595L;
         
         @Override
         public void actionPerformed(ActionEvent e) {
            getUser().doSelectedMove();
         }
      };
      doAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, ActionEvent.CTRL_MASK));
      
      AbstractAction undoAction = new AbstractAction(getString(KEY_UNDO)) {
         private static final long serialVersionUID = -3811519711082321686L;
         
         @Override
         public void actionPerformed(ActionEvent e) {
            getUser().undoMove();
         }
      };
      undoAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK));
      
      AbstractAction redoAction = new AbstractAction(getString(KEY_REDO)) {
         private static final long serialVersionUID = 3417068461078579687L;
         
         @Override
         public void actionPerformed(ActionEvent e) {
            getUser().redoMove();
         }
      };
      redoAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Y, ActionEvent.CTRL_MASK));
      
      JMenu menu = new JMenu(getString(KEY_MENU_MOVE));
      menu.add(undoAction);
      menu.add(redoAction);
      menu.addSeparator();
      menu.add(doAction);
      JMenuBar menuBar = new JMenuBar();
      menuBar.add(menu);
      d.setJMenuBar(menuBar);
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
         }
         
         @Override
         public void componentResized(ComponentEvent ev) {
            ConfigManager preferencesManager = getUser().getPreferencesManager();
            Dimension dim = d.getSize();
            preferencesManager.setEntry(EntryType.INTEGER, KEY_CHOOSER_WIDTH, dim.width);
            preferencesManager.setEntry(EntryType.INTEGER, KEY_CHOOSER_HEIGHT, dim.height);
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
   protected void updateGUIFrom(MoveChooserServiceUser user) {
      Collection<SimulationResult> results = user.getResults();
      SimulationResult selectResult = user.getSelectedResult();
      if (listener != null) {
         list.removeListSelectionListener(listener);
         model.clear();
         map.clear();
         if (results != null) {
            for (SimulationResult result : results) {
               String item = convertResult(result);
               map.put(item, result);
               model.addElement(item);
            }
         }
         if (model.isEmpty()) {
            model.addElement(getString(KEY_NO_MOVES));
         }
         if (selectResult == null) {
            list.setSelectedIndex(0);
         } else {
            list.setSelectedValue(convertResult(selectResult), true);
         }
         list.repaint();
         list.addListSelectionListener(listener);
      }
      ind.repaint();
   }
   
   private String convertResult(SimulationResult result) {
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
      
      String formatToUse = getString(KEY_RESULT_FORMAT, firstPart.toString(), gold.toString(), score.toString(),
            combos.toString(), blocks.toString(), disrupts.toString(), mega.toString());
      if (KEY_RESULT_FORMAT.equals(formatToUse)) {
         return result.toString();
      } else {
         return formatToUse;
      }
   }
   
   private void pushSelectionToUser() {
      String selected = list.getSelectedValue();
      if (selected != null && !selected.isEmpty() && map.containsKey(selected)) {
         SimulationResult result = map.get(selected);
         getUser().setSelectedResult(result);
      }
   }
   
   private void doMove() {
      getUser().doSelectedMove();
   }
   
   /*
    * (non-Javadoc)
    * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
    */
   @Override
   public void update(Observable arg0, Object arg1) {
      updateGUI();
   }
}
