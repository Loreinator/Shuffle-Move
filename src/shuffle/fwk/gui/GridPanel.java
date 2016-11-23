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
import java.awt.Desktop;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import shuffle.fwk.EntryMode;
import shuffle.fwk.ShuffleModel;
import shuffle.fwk.config.ConfigManager;
import shuffle.fwk.config.manager.ImageManager;
import shuffle.fwk.data.Board;
import shuffle.fwk.data.Species;
import shuffle.fwk.data.SpeciesPaint;
import shuffle.fwk.data.simulation.SimulationResult;
import shuffle.fwk.gui.user.FocusRequester;
import shuffle.fwk.gui.user.GridPanelUser;
import shuffle.fwk.service.gridprintconfig.GridPrintConfigServiceUser;

/**
 * @author Andrew Meyers
 *
 */
@SuppressWarnings("serial")
public class GridPanel extends JPanel {
   
   private static final Logger LOG = Logger.getLogger(GridPanel.class.getName());
   
   public static final String KEY_PICKUP_COLOR = "PICKUP_COLOR";
   public static final String KEY_DROPAT_COLOR = "DROPAT_COLOR";
   public static final String KEY_CUR_CURSOR_COLOR = "CUR_CURSOR_COLOR";
   public static final String KEY_PREV_CURSOR_COLOR = "PREV_CURSOR_COLOR";
   public static final String KEY_CELL_BORDER_THICK_INNER = "CELL_BORDER_THICK";
   public static final String KEY_GRID_BORDER_THICK = "GRID_BORDER_THICK";
   public static final String KEY_CELL_OUTLINE_THICK = "CELL_OUTLINE_THICK";
   public static final String KEY_PRINT_INCLUDE_CURSOR = "PRINT_INCLUDE_CURSOR";
   public static final String KEY_PRINT_INCLUDE_MOVE = "PRINT_INCLUDE_MOVE";
   public static final String KEY_PRINT_INCLUDE_GRID = "PRINT_INCLUDE_GRID";
   public static final String KEY_GRID_PRINT_PATH = "PRINT_GRID_PATH";
   
   public static final boolean DEFAULT_PRINT_INCLUDE_CURSOR = false;
   public static final boolean DEFAULT_PRINT_INCLUDE_MOVE = true;
   public static final boolean DEFAULT_PRINT_INCLUDE_GRID = true;
   
   private static final int DEFAULT_CELL_OUTLINE_THICK = 1;
   private static final int DEFAULT_CELL_BORDER_THICK = 2;
   private static final int DEFAULT_GRID_BORDER_THICK = 3;
   
   private static final String DEFAULT_GRID_PRINT_PATH = "output/%s.png";
   private static final SimpleDateFormat DT_FORMAT = new SimpleDateFormat("yyyyMMdd_HHmmss_zzz");
   
   private GridPanelUser user;
   private final Map<Integer, Indicator<SpeciesPaint>> cellMap = new HashMap<Integer, Indicator<SpeciesPaint>>();
   private final List<Integer> currentCursor = new ArrayList<Integer>();
   private final List<Integer> previousCursor = new ArrayList<Integer>();
   private final List<Integer> pickupCoords = new ArrayList<Integer>();
   private final List<Integer> dropatCoords = new ArrayList<Integer>();
   private EntryMode mode;
   private FocusRequester focusRequester;
   private JPanel content;
   
   private SpeciesPaint transferIcon = SpeciesPaint.AIR;
   
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
         ind.setBorder(getBorderFor(null));
         ind.setVisualized(null, "" + pos);
         content.add(ind, c);
         c.gridx++;
         while (c.gridx > Board.NUM_COLS) {
            c.gridy++;
            c.gridx -= Board.NUM_COLS;
         }
      }
      content.setBorder(getBorderFor(null));
      
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
      return getCellBorderInnerThickness(getUser());
   }
   
   /**
    * @return
    */
   private int getGridBorderThickness() {
      return getGridBorderThickness(getUser());
   }
   
   private int getCellOutlineThickness() {
      return getCellOutlineThickness(getUser());
   }
   
   private static int getCellBorderInnerThickness(GridPanelUser user) {
      ConfigManager preferencesManager = user.getPreferencesManager();
      Integer thick = preferencesManager.getIntegerValue(KEY_CELL_BORDER_THICK_INNER, DEFAULT_CELL_BORDER_THICK);
      return user.scaleBorderThickness(thick) * 2;
   }
   
   private static int getGridBorderThickness(GridPanelUser user) {
      ConfigManager preferencesManager = user.getPreferencesManager();
      Integer thick = preferencesManager.getIntegerValue(KEY_GRID_BORDER_THICK, DEFAULT_GRID_BORDER_THICK);
      return user.scaleBorderThickness(thick);
   }
   
   private static int getCellOutlineThickness(GridPanelUser user) {
      ConfigManager preferencesManager = user.getPreferencesManager();
      Integer thick = preferencesManager.getIntegerValue(KEY_CELL_OUTLINE_THICK, DEFAULT_CELL_OUTLINE_THICK);
      return user.scaleBorderThickness(thick);
   }
   
   private Border getBorderFor(Color inner) {
      int cellOutlineThickness = getCellOutlineThickness();
      int cellBorderInnerThickness = getCellBorderInnerThickness();
      Border innerBorder = new EmptyBorder(cellBorderInnerThickness, cellBorderInnerThickness, cellBorderInnerThickness,
            cellBorderInnerThickness);
      if (inner != null) {
         innerBorder = new LineBorder(inner, cellBorderInnerThickness, true);
      }
      Border greyOutline = new LineBorder(Color.gray, cellOutlineThickness);
      CompoundBorder finalBorder = BorderFactory.createCompoundBorder(greyOutline, innerBorder);
      return finalBorder;
   }
   
   public void addActionListeners() {
      for (int pos = 1; pos <= Board.NUM_CELLS; pos++) {
         List<Integer> coords = ShuffleModel.getCoordsFromPosition(pos);
         final int row = coords.get(0);
         final int col = coords.get(1);
         Indicator<SpeciesPaint> indicator = cellMap.get(pos);
         if (indicator != null) {
            indicator.addMouseWheelListener(new MouseWheelListener() {
               @Override
               public void mouseWheelMoved(MouseWheelEvent e) {
                  swapPaintAt(row, col);
               }
            });
            indicator.addMouseListener(new PressOrClickMouseAdapter() {
               private void handlePress(boolean erase) {
                  EntryMode currentMode = getUser().getCurrentEntryMode();
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
               protected void onMiddle(MouseEvent e) {
                  swapPaintAt(row, col);
               }
               
               @Override
               protected void onEnter() {
                  focusRequester.updateFocus();
               }
               
               @Override
               protected boolean ignoreClick() {
                  return true;
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
         currentCursor.clear();
         currentCursor.addAll(curCursor);
      }
      if (!prevCursor.equals(previousCursor)) {
         borderChangedSet.add(prevCursor);
         borderChangedSet.add(previousCursor);
         previousCursor.clear();
         previousCursor.addAll(prevCursor);
      }
      if (!pickup.equals(pickupCoords)) {
         borderChangedSet.add(pickup);
         borderChangedSet.add(pickupCoords);
         pickupCoords.clear();
         pickupCoords.addAll(pickup);
      }
      if (!dropat.equals(dropatCoords)) {
         borderChangedSet.add(dropat);
         borderChangedSet.add(dropatCoords);
         dropatCoords.clear();
         dropatCoords.addAll(dropat);
      }
      boolean changed = false;
      for (int pos = 1; pos <= Board.NUM_CELLS; pos++) {
         List<Integer> coords = ShuffleModel.getCoordsFromPosition(pos);
         SpeciesPaint s = getUser().getPaintAt(coords.get(0), coords.get(1));
         Color selectColor = getCursorColor(coords, curCursor, prevCursor, preferencesManager);
         Color moveColor = getMoveColorFor(coords, pickup, dropat, preferencesManager);
         cellMap.get(pos).setBorder(getBorderFor(selectColor));
         cellMap.get(pos).setOpaque(true);
         cellMap.get(pos).setBackground(moveColor);

         if (cellMap.get(pos).setVisualized(s) || borderChangedSet.contains(coords)) {
            changed = true;
         }
      }
      
      EntryMode curMode = getUser().getCurrentEntryMode();
      if (!curMode.equals(mode)) {
         mode = curMode;
         Color gridOutlineColor = preferencesManager.getColorFor(mode);
         content.setBorder(new LineBorder(gridOutlineColor, getGridBorderThickness(), true));
         changed = true;
      }
      if (changed) {
         content.repaint();
      }
      return changed;
   }

   /**
    * @param row
    * @param col
    */
   public void swapPaintAt(final int row, final int col) {
      SpeciesPaint newTransfer = getUser().getPaintAt(row, col);
      if (newTransfer.getSpecies().equals(Species.METAL)) {
         newTransfer = new SpeciesPaint(Species.METAL_5, newTransfer.isFrozen(), newTransfer.isMega());
      }
      getUser().paintAt(transferIcon, row, col);
      transferIcon = newTransfer;
   }
   
   public static Color getCursorColor(List<Integer> coords, List<Integer> curCursor, List<Integer> prevCursor,
         ConfigManager preferencesManager) {
      Color selectColor = null;
      if (coords.equals(curCursor)) {
         selectColor = preferencesManager.getColorFor(KEY_CUR_CURSOR_COLOR);
      } else if (coords.equals(prevCursor)) {
         selectColor = preferencesManager.getColorFor(KEY_PREV_CURSOR_COLOR);
      }
      return selectColor;
   }
   
   public static Color getMoveColorFor(List<Integer> coords, List<Integer> pickup, List<Integer> dropat,
         ConfigManager preferencesManager) {
      Color moveColor = null;
      if (coords.equals(pickup)) {
         moveColor = preferencesManager.getColorFor(KEY_PICKUP_COLOR);
      } else if (coords.equals(dropat)) {
         moveColor = preferencesManager.getColorFor(KEY_DROPAT_COLOR);
      }
      return moveColor;
   }
   
   /**
    * @param shuffleController
    */
   public static void printGrid(GridPrintConfigServiceUser user) {
      SimulationResult simResult = user.getSelectedResult();
      List<Integer> move = new ArrayList<Integer>();
      List<Integer> pickup = new ArrayList<Integer>();
      List<Integer> dropat = new ArrayList<Integer>();
      if (simResult != null) {
         move = simResult.getMove();
         if (move.size() >= 2) {
            pickup.addAll(move.subList(0, 2));
            if (move.size() >= 4) {
               dropat.addAll(move.subList(2, 4));
            }
         }
      }
      
      ConfigManager preferencesManager = user.getPreferencesManager();
      ImageManager imageManager = user.getImageManager();
      
      boolean includeCursor = preferencesManager.getBooleanValue(KEY_PRINT_INCLUDE_CURSOR,
            DEFAULT_PRINT_INCLUDE_CURSOR);
      boolean includeMove = preferencesManager.getBooleanValue(KEY_PRINT_INCLUDE_MOVE, DEFAULT_PRINT_INCLUDE_MOVE);
      boolean includeGrid = preferencesManager.getBooleanValue(KEY_PRINT_INCLUDE_GRID, DEFAULT_PRINT_INCLUDE_GRID);
      
      List<Integer> curCursor = user.getCurrentCursor();
      List<Integer> prevCursor = user.getPreviousCursor();
      
      int innerBorderThick = includeCursor ? getCellBorderInnerThickness(user) : 0;
      int outerBorderThick = includeGrid ? getCellOutlineThickness(user) : 0;
      int borderThick = innerBorderThick + outerBorderThick;
      int iconWidth = imageManager.getIconWidth();
      int iconHeight = imageManager.getIconHeight();
      int cellWidth = iconWidth + borderThick * 2;
      int cellHeight = iconHeight + borderThick * 2;
      
      BufferedImage result = new BufferedImage(Board.NUM_COLS * cellWidth, Board.NUM_ROWS * cellHeight,
            BufferedImage.TYPE_INT_ARGB);
      Graphics2D g = result.createGraphics();
      g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
      
      for (int pos = 1; pos <= Board.NUM_CELLS; pos++) {
         // Coordinates in the grid (row, column) where each starts at 1
         List<Integer> coords = ShuffleModel.getCoordsFromPosition(pos);
         // start point for this cell's pixel coordinates
         int cellPixelX = (coords.get(1) - 1) * cellWidth;
         int cellPixelY = (coords.get(0) - 1) * cellHeight;
         if (includeMove) {
            // The background fill for move selection
            Color moveColor = getMoveColorFor(coords, pickup, dropat, preferencesManager);
            if (moveColor != null) {
               g.setPaint(moveColor);
               g.fillRect(cellPixelX, cellPixelY, cellWidth, cellHeight);
            }
         }
         if (includeCursor) {
            // the selection cursor
            Color thisCursorColor = getCursorColor(coords, curCursor, prevCursor, preferencesManager);
            if (thisCursorColor != null) {
               g.setPaint(thisCursorColor);
               drawBorder(g, cellPixelX + outerBorderThick, cellPixelY + outerBorderThick,
                     iconWidth + 2 * innerBorderThick, iconHeight + 2 * innerBorderThick, innerBorderThick);
            }
         }
         // The icon itself
         SpeciesPaint s = user.getPaintAt(coords.get(0), coords.get(1));
         Image img = imageManager.getImageFor(s).getImage();
         int iconPixelX = cellPixelX + borderThick;
         int iconPixelY = cellPixelY + borderThick;
         g.drawImage(img, iconPixelX, iconPixelY, iconWidth, iconHeight, null);
         if (includeGrid) {
            // The grey grid outline
            g.setPaint(Color.gray);
            drawBorder(g, cellPixelX, cellPixelY, cellWidth, cellHeight, outerBorderThick);
         }
      }
      g.dispose();
      
      try {
         File outputFile = getGridConfigFile(user);
         ImageIO.write(result, "png", outputFile);
         Desktop desktop = Desktop.getDesktop();
         desktop.open(outputFile);
      } catch (Exception e) {
         LOG.log(Level.SEVERE, e.getMessage(), e);
      }
   }
   
   private static File getGridConfigFile(GridPanelUser user) {
      ConfigManager preferencesManager = user.getPreferencesManager();
      String stageName = user.getCurrentStage().getName();
      String timeStamp = DT_FORMAT.format(new Date());
      String filePath = preferencesManager.getStringValue(KEY_GRID_PRINT_PATH, DEFAULT_GRID_PRINT_PATH);
      String formattedPath = String.format(filePath, stageName + "_" + timeStamp);
      File outputFile = null;
      try {
         outputFile = new File(formattedPath);
         outputFile.getParentFile().mkdirs();
         outputFile.createNewFile();
      } catch (Exception e) {
         LOG.log(Level.SEVERE, e.getMessage(), e);
      }
      return outputFile;
   }
   
   private static void drawBorder(Graphics2D graphics, int x, int y, int width, int height, int thick) {
      int curX = x;
      int curY = y;
      int curWidth = width - 1;
      int curHeight = height - 1;
      for (int widthComplete = 0; widthComplete < thick; widthComplete++) {
         graphics.drawRect(curX, curY, curWidth, curHeight);
         curX++;
         curY++;
         curWidth -= 2;
         curHeight -= 2;
      }
   }
}
