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

import shuffle.fwk.EntryMode;
import shuffle.fwk.config.ConfigFactory;
import shuffle.fwk.config.ConfigManager;
import shuffle.fwk.config.EntryType;

/**
 * @author Andrew Meyers
 *
 */
public class EntryModeManager extends ConfigManager {
   
   public static final String CURRENT_ENTRY_MODE = "CURRENT_ENTRY_MODE";
   public static final EntryMode DEFAULT_ENTRY_MODE = EntryMode.PAINT;
   
   public EntryModeManager(List<String> loadPaths, List<String> writePaths, ConfigFactory factory) {
      super(loadPaths, writePaths, factory);
   }
   
   public EntryModeManager(ConfigManager manager) {
      super(manager);
   }
   
   @Override
   protected boolean shouldUpdate() {
      return false;
   }
   
   public EntryMode getCurrentEntryMode() {
      return getEntryModeValue(CURRENT_ENTRY_MODE, DEFAULT_ENTRY_MODE);
   }
   
   public boolean setCurrentEntryMode(EntryMode mode) {
      return setEntry(EntryType.ENTRY_MODE, CURRENT_ENTRY_MODE, mode);
   }
   
   public EntryMode getDefaultEntryMode() {
      return DEFAULT_ENTRY_MODE;
   }
}
