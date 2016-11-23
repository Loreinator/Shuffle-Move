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
package shuffle.fwk.service.gridprintconfig;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;

import shuffle.fwk.i18n.I18nUser;
import shuffle.fwk.service.BaseService;
import shuffle.fwk.service.DisposeAction;

/**
 * @author Andrew Meyers
 *
 */
public class GridPrintConfigService extends BaseService<GridPrintConfigServiceUser>implements I18nUser {
   
   private static final String KEY_GRID_PRINT = "button.print";
   private static final String KEY_CLOSE = "button.close";
   
   private static final String KEY_CLOSE_TOOLTIP = "tooltip.close";
   private static final String KEY_GRID_PRINT_TOOLTIP = "button.print.tooltip";
   
   private static final String KEY_ENABLE_GRID = "enable.grid";
   private static final String KEY_ENABLE_MOVE = "enable.move";
   private static final String KEY_ENABLE_CURSOR = "enable.cursor";
   
   private static final String KEY_ENABLE_GRID_TOOLTIP = "enable.grid.tooltip";
   private static final String KEY_ENABLE_MOVE_TOOLTIP = "enable.move.tooltip";
   private static final String KEY_ENABLE_CURSOR_TOOLTIP = "enable.cursor.tooltip";
   
   
   private JCheckBox enableGrid;
   private JCheckBox enableMove;
   private JCheckBox enableCursor;
   
   /* (non-Javadoc)
    * @see shuffle.fwk.service.BaseService#getUserClass()
    */
   @Override
   protected Class<GridPrintConfigServiceUser> getUserClass() {
      return GridPrintConfigServiceUser.class;
   }
   
   /* (non-Javadoc)
    * @see shuffle.fwk.service.BaseService#onSetupGUI()
    */
   @Override
   protected void onSetupGUI() {
      JDialog d = new JDialog(getOwner());
      
      enableGrid = new JCheckBox(getString(KEY_ENABLE_GRID));
      enableMove = new JCheckBox(getString(KEY_ENABLE_MOVE));
      enableCursor = new JCheckBox(getString(KEY_ENABLE_CURSOR));
      
      d.setLayout(new GridBagLayout());
      
      GridBagConstraints c = new GridBagConstraints();
      c.fill = GridBagConstraints.BOTH;
      c.anchor = GridBagConstraints.CENTER;
      c.weightx = 1.0;
      c.weighty = 1.0;
      
      int maxWidth = 2;
      
      c.gridx = 1;
      c.gridy++;
      c.gridwidth = maxWidth;
      enableGrid.setToolTipText(getString(KEY_ENABLE_GRID_TOOLTIP));
      enableGrid.setSelected(getUser().isGridPrintGridEnabled());
      enableGrid.addItemListener(new ItemListener() {
         @Override
         public void itemStateChanged(ItemEvent e) {
            getUser().setGridPrintGridEnabled(e.getStateChange() == ItemEvent.SELECTED);
         }
      });
      d.add(enableGrid, c);
      
      c.gridx = 1;
      c.gridy++;
      c.gridwidth = maxWidth;
      enableMove.setToolTipText(getString(KEY_ENABLE_MOVE_TOOLTIP));
      enableMove.setSelected(getUser().isGridPrintMoveEnabled());
      enableMove.addItemListener(new ItemListener() {
         @Override
         public void itemStateChanged(ItemEvent e) {
            getUser().setGridPrintMoveEnabled(e.getStateChange() == ItemEvent.SELECTED);
         }
      });
      d.add(enableMove, c);
      
      c.gridx = 1;
      c.gridy++;
      c.gridwidth = maxWidth;
      enableCursor.setToolTipText(getString(KEY_ENABLE_CURSOR_TOOLTIP));
      enableCursor.setSelected(getUser().isGridPrintCursorEnabled());
      enableCursor.addItemListener(new ItemListener() {
         @Override
         public void itemStateChanged(ItemEvent e) {
            getUser().setGridPrintCursorEnabled(e.getStateChange() == ItemEvent.SELECTED);
         }
      });
      d.add(enableCursor, c);
      
      c.gridx = 1;
      c.gridy++;
      c.gridwidth = 1;
      @SuppressWarnings("serial")
      JButton printButton = new JButton(new AbstractAction(getString(KEY_GRID_PRINT)) {
         @Override
         public void actionPerformed(ActionEvent arg0) {
            getUser().printGrid();
         }
      });
      printButton.setToolTipText(getString(KEY_GRID_PRINT_TOOLTIP));
      d.add(printButton, c);
      
      c.gridx++;
      JButton closeButton = new JButton(new DisposeAction(getString(KEY_CLOSE), this));
      closeButton.setToolTipText(getString(KEY_CLOSE_TOOLTIP));
      d.add(closeButton, c);
      
      d.pack();
      d.repaint();
      d.setLocationRelativeTo(null);
      
      setDialog(d);
   }
   
   /* (non-Javadoc)
    * @see shuffle.fwk.service.BaseService#updateGUIFrom(java.lang.Object)
    */
   @Override
   protected void updateGUIFrom(GridPrintConfigServiceUser user) {
      enableGrid.setSelected(user.isGridPrintGridEnabled());
      enableMove.setSelected(user.isGridPrintMoveEnabled());
      enableCursor.setSelected(user.isGridPrintMoveEnabled());
   }
   
}
