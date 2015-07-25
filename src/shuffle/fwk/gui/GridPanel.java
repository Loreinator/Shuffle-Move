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

package shuffle.fwk.gui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import shuffle.fwk.EntryMode;
import shuffle.fwk.ShuffleModel;
import shuffle.fwk.config.ConfigManager;
import shuffle.fwk.data.Board;
import shuffle.fwk.data.SpeciesPaint;
import shuffle.fwk.data.simulation.SimulationResult;
import shuffle.fwk.gui.user.FocusRequester;
import shuffle.fwk.gui.user.GridPanelUser;

/**
 * @author Andrew Meyers
 *
 */
@SuppressWarnings("serial")
public class GridPanel extends JPanel {
   
   public static final String KEY_PICKUP_COLOR = "PICKUP_COLOR";
   public static final String KEY_DROPAT_COLOR = "DROPAT_COLOR";
   public static final String KEY_CUR_CURSOR_COLOR = "CUR_CURSOR_COLOR";
   public static final String KEY_PREV_CURSOR_COLOR = "PREV_CURSOR_COLOR";
   public static final String KEY_CELL_BORDER_THICK_INNER = "CELL_BORDER_THICK";
   public static final String KEY_CELL_BORDER_THICK_OUTER = "CELL_BORDER_THICK_OUTER";
   public static final String KEY_GRID_BORDER_THICK = "GRID_BORDER_THICK";
   public static final String KEY_CELL_OUTLINE_THICK = "CELL_OUTLINE_THICK";
   
   private static final int DEFAULT_CELL_OUTLINE_THICK = 1;
   private static final int DEFAULT_CELL_BORDER_THICK = 2;
   private static final int DEFAULT_GRID_BORDER_THICK = 3;
   
   private GridPanelUser user;
   private final Map<Integer, Indicator<SpeciesPaint>> cellMap = new HashMap<Integer, Indicator<SpeciesPaint>>();
   private final List<Integer> currentCursor = new ArrayList<Integer>();
   private final List<Integer> previousCursor = new ArrayList<Integer>();
   private final List<Integer> pickupCoords = new ArrayList<Integer>();
   private final List<Integer> dropatCoords = new ArrayList<Integer>();
   private EntryMode mode;
   private FocusRequester focusRequester;
   private JPanel content;
   
   public GridPanel(GridPanelUser user, FocusRequester focusRequester) {
      super(new GridBagLayout());
      this.user = user;
      this.focusRequester = focusRequester;
      setup();
   }
   
   private GridPanelUser getUser() {
      return user;
   }
   
   private void setup() {
      content = new JPanel(new GridBagLayout());
      GridBagConstraints c = new GridBagConstraints();
      c.fill = GridBagConstraints.BOTH;
      c.gridx = 1;
      c.gridy = 1;
      c.weightx = 0.0;
      c.weighty = 0.0;
      
      for (int pos = 1; pos <= Board.NUM_CELLS; pos++) {
         Indicator<SpeciesPaint> ind = new Indicator<SpeciesPaint>(getUser());
         cellMap.put(pos, ind);
         ind.setBorder(getBorderFor(null, null));
         ind.setVisualized(null, "" + pos);
         content.add(ind, c);
         c.gridx++;
         while (c.gridx > Board.NUM_COLS) {
            c.gridy++;
            c.gridx -= Board.NUM_COLS;
         }
      }
      content.setBorder(getBorderFor(null, null));
      
      c.fill = GridBagConstraints.NONE;
      c.gridx = 1;
      c.gridy = 1;
      c.weightx = 1.0;
      c.weighty = 1.0;
      c.anchor = GridBagConstraints.CENTER;
      add(content, c);
   }
   
   /**
    * @return
    */
   private int getCellBorderInnerThickness() {
      return getUser().getPreferencesManager().getIntegerValue(KEY_CELL_BORDER_THICK_INNER, DEFAULT_CELL_BORDER_THICK);
   }
   
   /**
    * @return
    */
   private int getCellBorderOuterThickness() {
      return getUser().getPreferencesManager().getIntegerValue(KEY_CELL_BORDER_THICK_OUTER, DEFAULT_CELL_BORDER_THICK);
   }
   
   /**
    * @return
    */
   private int getGridBorderThickness() {
      return getUser().getPreferencesManager().getIntegerValue(KEY_GRID_BORDER_THICK, DEFAULT_GRID_BORDER_THICK);
   }
   
   private int getCellOutlineThickness() {
      return getUser().getPreferencesManager().getIntegerValue(KEY_CELL_OUTLINE_THICK, DEFAULT_CELL_OUTLINE_THICK);
   }
   
   private Border getBorderFor(Color inner, Color outer) {
      Border innerBorder = new EmptyBorder(getCellBorderInnerThickness(), getCellBorderInnerThickness(),
            getCellBorderInnerThickness(), getCellBorderInnerThickness());
      Border outerBorder = new EmptyBorder(getCellBorderOuterThickness(), getCellBorderOuterThickness(),
            getCellBorderOuterThickness(), getCellBorderOuterThickness());
      if (outer != null) {
         outerBorder = new LineBorder(outer, getCellBorderOuterThickness(), true);
      }
      if (inner != null) {
         innerBorder = new LineBorder(inner, getCellBorderInnerThickness(), true);
      }
      CompoundBorder coloredBorder = BorderFactory.createCompoundBorder(outerBorder, innerBorder);
      Border greyOutline = new LineBorder(Color.gray, getCellOutlineThickness());
      CompoundBorder finalBorder = BorderFactory.createCompoundBorder(greyOutline, coloredBorder);
      return finalBorder;
   }
   
   public void addActionListeners() {
      for (int pos = 1; pos <= Board.NUM_CELLS; pos++) {
         List<Integer> coords = ShuffleModel.getCoordsFromPosition(pos);
         final int row = coords.get(0);
         final int col = coords.get(1);
         Indicator<SpeciesPaint> indicator = cellMap.get(pos);
         if (indicator != null) {
            indicator.addMouseListener(new PressOrClickMouseAdapter() {
               private void handlePress(boolean erase) {
                  EntryMode currentMode = getUser().getCurrentMode();
                  currentMode.handleGridEvent(getUser(), row, col, erase);
               }
               
               @Override
               protected void onLeft(MouseEvent e) {
                  handlePress(false);
               }
               
               @Override
               protected void onRight(MouseEvent e) {
                  handlePress(true);
               }
               
               @Override
               protected void onEnter() {
                  focusRequester.updateFocus();
               }
            });
         }
      }
      MouseAdapter ml = new MouseAdapter() {
         @Override
         public void mouseEntered(MouseEvent e) {
            focusRequester.updateFocus();
         }
      };
      content.addMouseListener(ml);
      addMouseListener(ml);
   }
   
   public boolean updateGrid() {
      SimulationResult result = getUser().getSelectedResult();
      List<Integer> move = new ArrayList<Integer>();
      List<Integer> pickup = new ArrayList<Integer>();
      List<Integer> dropat = new ArrayList<Integer>();
      if (result != null) {
         move = result.getMove();
         if (move.size() >= 2) {
            pickup.addAll(move.subList(0, 2));
            if (move.size() >= 4) {
               dropat.addAll(move.subList(2, 4));
            }
         }
      }
      ConfigManager preferencesManager = getUser().getPreferencesManager();
      
      List<Integer> curCursor = getUser().getCurrentCursor();
      List<Integer> prevCursor = getUser().getPreviousCursor();
      
      Set<List<Integer>> borderChangedSet = new HashSet<List<Integer>>();
      if (!curCursor.equals(currentCursor)) {
         borderChangedSet.add(curCursor);
         borderChangedSet.add(currentCursor);
      }
      if (!prevCursor.equals(previousCursor)) {
         borderChangedSet.add(prevCursor);
         borderChangedSet.add(previousCursor);
      }
      if (!pickup.equals(pickupCoords)) {
         borderChangedSet.add(pickup);
         borderChangedSet.add(pickupCoords);
      }
      if (!dropat.equals(dropatCoords)) {
         borderChangedSet.add(dropat);
         borderChangedSet.add(dropatCoords);
      }
      boolean changed = false;
      for (int pos = 1; pos <= Board.NUM_CELLS; pos++) {
         List<Integer> coords = ShuffleModel.getCoordsFromPosition(pos);
         SpeciesPaint s = getUser().getPaintAt(coords.get(0), coords.get(1));
         Color selectColor = null;
         if (coords.equals(curCursor)) {
            selectColor = preferencesManager.getColorFor(KEY_CUR_CURSOR_COLOR);
         } else if (coords.equals(prevCursor)) {
            selectColor = preferencesManager.getColorFor(KEY_PREV_CURSOR_COLOR);
         }
         Color moveColor = null;
         if (coords.equals(pickup)) {
            moveColor = preferencesManager.getColorFor(KEY_PICKUP_COLOR);
         } else if (coords.equals(dropat)) {
            moveColor = preferencesManager.getColorFor(KEY_DROPAT_COLOR);
         }
         cellMap.get(pos).setBorder(getBorderFor(selectColor, moveColor));
         if (cellMap.get(pos).setVisualized(s) || borderChangedSet.contains(coords)) {
            changed = true;
         }
      }
      
      EntryMode curMode = getUser().getCurrentMode();
      if (!curMode.equals(mode)) {
         mode = curMode;
         Color gridOutline = preferencesManager.getColorFor(mode);
         content.setBorder(new LineBorder(gridOutline, getGridBorderThickness(), true));
         changed = true;
      }
      if (changed) {
         content.repaint();
      }
      return changed;
   }
   
}
