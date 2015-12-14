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

import java.util.List;

import shuffle.fwk.GradingMode;
import shuffle.fwk.config.ConfigFactory;
import shuffle.fwk.config.ConfigManager;
import shuffle.fwk.config.EntryType;

/**
 * @author Andrew Meyers
 *
 */
public class GradingModeManager extends ConfigManager {
   
   private static final String SCORE_KEY = "grading.score";
   private static final String CURRENT_MODE = "CURRENT_MODE";
   private static final GradingMode DEFAULT_MODE = new GradingMode(SCORE_KEY, "", false);
   
   public GradingModeManager(List<String> loadPaths, List<String> writePaths, ConfigFactory factory) {
      super(loadPaths, writePaths, factory);
   }
   
   public GradingModeManager(ConfigManager manager) {
      super(manager);
   }
   
   @Override
   protected boolean shouldUpdate() {
      return true;
   }
   
   public GradingMode getCurrentGradingMode() {
      return getGradingModeValue(getStringValue(CURRENT_MODE), DEFAULT_MODE);
   }
   
   public boolean setCurrentGradingMode(GradingMode mode) {
      return setEntry(EntryType.STRING, CURRENT_MODE, mode.getKey());
   }
   
   public GradingMode getDefaultGradingMode() {
      return DEFAULT_MODE;
   }
}
