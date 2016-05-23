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

package shuffle.fwk.config.manager;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import shuffle.fwk.config.ConfigFactory;
import shuffle.fwk.config.EntryType;
import shuffle.fwk.config.loader.BoardConfigLoader;
import shuffle.fwk.config.writer.PreferencesWriter;
import shuffle.fwk.data.Board;
import shuffle.fwk.data.Species;
import shuffle.fwk.data.Stage;

/**
 * @author Andrew Meyers
 *
 */
public class BoardManager {
   public static final String KEY_BOARD = "BOARD";
   public static final String KEY_BOARD_STAGE_CONFIG = "BOARD_STAGE_CONFIG";
   public static final String KEY_DEFAULT_BOARD_STAGE_CONFIG = "DEFAULT_BOARD_STAGE_CONFIG";
   
   private final BoardConfigLoader boardLoader;
   private Stage curStage = StageManager.DEFAULT_STAGE;
   private Board curBoard = new Board();
   private ConfigFactory factory = null;
   
   public BoardManager(ConfigFactory factory) {
      this.factory = factory;
      File boardPath = factory.getPathManager().getFileValue(KEY_BOARD);
      boardLoader = new BoardConfigLoader(null, Arrays.asList(boardPath.getPath()));
      loadFromConfig();
   }
   
   public boolean loadFromConfig() {
      boardLoader.setForceReload(true);
      boolean changed = loadBoardFromConfig(boardLoader);
      loadStageFromConfig();
      return changed;
   }
   
   public boolean loadBoardForStage(Stage stage, boolean forceDefault) {
      List<String> toLoadFrom = new ArrayList<String>();
      if (!forceDefault) {
         addBoardStagePath(stage, toLoadFrom);
      }
      addDefaultBoardStagePath(stage, toLoadFrom);
      List<String> validPaths = new ArrayList<String>();
      for (String path : toLoadFrom) {
         try {
            File file = (File) EntryType.FILE.parseValue(null, path);
            if (file.exists() && file.canRead()) {
               validPaths.add(path);
            }
         } catch (Exception e) {
            // Ignore this
         }
      }
      List<String> defaultBoardResources = getDefaultBoardResources(stage);
      boolean changed = false;
      if (!validPaths.isEmpty() || !defaultBoardResources.isEmpty()) {
         BoardConfigLoader stageBoardLoader = new BoardConfigLoader(defaultBoardResources, validPaths);
         changed |= loadBoardFromConfig(stageBoardLoader);
      }
      return changed;
   }
   
   /**
    * @param stage
    * @return
    */
   private List<String> getDefaultBoardResources(Stage stage) {
      String pathFormat = factory.getPathManager().getStringValue(KEY_DEFAULT_BOARD_STAGE_CONFIG);
      if (pathFormat != null) {
         String resourceName = String.format(pathFormat, stage.getName());
         URL url = ClassLoader.getSystemResource(resourceName);
         if (url != null) {
            return Arrays.asList(resourceName);
         }
      }
      return Collections.emptyList();
   }
   
   /**
    * @param stage
    * @param toLoadFrom
    */
   private void addDefaultBoardStagePath(Stage stage, List<String> toLoadFrom) {
      String pathFormat = factory.getPreferencesManager().getStringValue(KEY_DEFAULT_BOARD_STAGE_CONFIG);
      if (pathFormat != null) {
         toLoadFrom.add(String.format(pathFormat, stage.getName()));
      }
   }
   
   /**
    * @param stage
    * @param toLoadFrom
    */
   private void addBoardStagePath(Stage stage, List<String> toLoadFrom) {
      String pathFormat = factory.getPreferencesManager().getStringValue(KEY_BOARD_STAGE_CONFIG);
      if (pathFormat != null) {
         toLoadFrom.add(String.format(pathFormat, stage.getName()));
      }
   }
   
   private boolean loadBoardFromConfig(BoardConfigLoader boardLoader) {
      String before = curBoard.toString();
      curBoard.clear();
      List<List<String>> rows = boardLoader.getConfiguredRows();
      List<List<Boolean>> fRows = boardLoader.getConfiguredFrozenRows();
      List<List<Boolean>> cRows = boardLoader.getConfiguredCloudedRows();
      SpeciesManager manager = factory.getSpeciesManager();
      for (int r = 1; r <= 6; r++) {
         for (int c = 1; c <= 6; c++) {
            String speciesName = rows.get(r - 1).get(c - 1);
            Species s = manager.getSpeciesByName(speciesName);
            curBoard.setSpeciesAt(r, c, s);
            Boolean frozen = fRows.get(r - 1).get(c - 1);
            curBoard.setFrozenAt(r, c, frozen == null ? false : frozen);
            Boolean clouded = cRows.get(r - 1).get(c - 1);
            curBoard.setClouded(r, c, clouded == null ? false : clouded);
         }
      }
      for (String progress : boardLoader.getConfiguredMegaProgress()) {
         try {
            curBoard.setMegaProgress(Integer.parseInt(progress));
         } catch (NullPointerException | NumberFormatException nfe) {
            curBoard.setMegaProgress(0);
         }
      }
      String after = curBoard.toString();
      return !before.equals(after);
   }
   
   private boolean loadStageFromConfig() {
      List<String> stages = boardLoader.getConfiguredStage();
      Stage oldStage = curStage;
      curStage = factory.getStageManager().getStageMatch(stages);
      return !(curStage == oldStage || curStage != null && curStage.equals(oldStage));
   }
   
   public void saveBoard() {
      Map<String, List<String>> data = new LinkedHashMap<String, List<String>>();
      // The stage data
      data.put(BoardConfigLoader.STAGE, Arrays.asList(getCurrentStage().getName()));
      // The progress data
      data.put(BoardConfigLoader.KEY_MEGA_PROGRESS, Arrays.asList(String.valueOf(getBoard().getMegaProgress())));
      // The row data
      for (int i = 1; i <= 6; i++) {
         String rowKey = BoardConfigLoader.getRowKey(i);
         String rowfKey = BoardConfigLoader.getFrozenRowKey(i);
         String rowcKey = BoardConfigLoader.getCloudedRowKey(i);
         List<String> rowSpeciesNames = new ArrayList<String>();
         List<String> rowFrozenStates = new ArrayList<String>();
         List<String> rowCloudedStates = new ArrayList<String>();
         for (int j = 1; j <= 6; j++) {
            rowSpeciesNames.add(getBoard().getSpeciesAt(i, j).getName());
            rowFrozenStates.add(Boolean.toString(getBoard().isFrozenAt(i, j)));
            rowCloudedStates.add(Boolean.toString(getBoard().isCloudedAt(i, j)));
         }
         StringBuilder rowBuilder = new StringBuilder();
         Iterator<String> itr = rowSpeciesNames.iterator();
         while (itr.hasNext()) {
            rowBuilder.append(itr.next());
            if (itr.hasNext()) {
               rowBuilder.append(",");
            }
         }
         data.put(rowKey, Arrays.asList(rowBuilder.toString()));
         StringBuilder fRowBuilder = new StringBuilder();
         Iterator<String> fitr = rowFrozenStates.iterator();
         while (fitr.hasNext()) {
            fRowBuilder.append(fitr.next());
            if (fitr.hasNext()) {
               fRowBuilder.append(",");
            }
         }
         data.put(rowfKey, Arrays.asList(fRowBuilder.toString()));
         StringBuilder cRowBuilder = new StringBuilder();
         Iterator<String> citr = rowCloudedStates.iterator();
         while (citr.hasNext()) {
            cRowBuilder.append(citr.next());
            if (citr.hasNext()) {
               cRowBuilder.append(",");
            }
         }
         data.put(rowcKey, Arrays.asList(cRowBuilder.toString()));
      }
      File boardPath = factory.getPathManager().getFileValue(KEY_BOARD);
      List<String> paths = new ArrayList<String>(Arrays.asList(boardPath.getPath()));
      addBoardStagePath(getCurrentStage(), paths);
      PreferencesWriter pw = new PreferencesWriter(paths);
      pw.writePreferences(data);
      boardLoader.setForceReload(true);
   }
   
   public Board getBoard() {
      return getBoard(false);
   }
   
   public Board getBoard(boolean reloadFromConfig) {
      if (reloadFromConfig) {
         boardLoader.setForceReload(true);
         loadBoardFromConfig(boardLoader);
      }
      return curBoard;
   }
   
   public boolean setBoard(Board b) {
      String before = curBoard.toString();
      curBoard = new Board(b);
      String after = curBoard.toString();
      return !before.equals(after);
   }
   
   public Stage getCurrentStage() {
      if (curStage == null) {
         loadStageFromConfig();
      }
      return curStage;
   }
   
   public boolean setStage(Stage stage) {
      boolean changed = stage != null && !stage.equals(curStage);
      if (changed) {
         curStage = stage;
      }
      return changed;
   }
}
