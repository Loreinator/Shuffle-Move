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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

import shuffle.fwk.config.ConfigFactory;
import shuffle.fwk.config.ConfigManager;
import shuffle.fwk.config.EntryType;
import shuffle.fwk.data.PkmType;
import shuffle.fwk.data.Stage;

/**
 * This is used to ONLY load the stages, not for saving. If you want to change the stages, please
 * edit the configuration file before loading the program.
 * 
 * @author Andrew Meyers
 */
public class StageManager extends ConfigManager {
   public static final Stage DEFAULT_STAGE = new Stage(PkmType.NORMAL);
   private static List<Stage> typeStages = null;
   
   public StageManager(List<String> loadPaths, List<String> writePaths, ConfigFactory factory) {
      super(loadPaths, writePaths, factory);
   }
   
   public StageManager(ConfigManager manager) {
      super(manager);
   }
   
   @Override
   public boolean loadFromConfig() {
      boolean changed = super.loadFromConfig();
      if (changed) {
         addDefaultStages();
      }
      return changed;
   }
   
   private void addDefaultStages() {
      for (Stage s : getDefaultStages()) {
         setEntry(EntryType.STAGE, s.getName(), s);
      }
   }
   
   public List<Stage> getDefaultStages() {
      if (typeStages == null) {
         PkmType[] types = PkmType.values();
         typeStages = new ArrayList<Stage>(types.length);
         for (PkmType type : types) {
            if (!type.isSpecial()) {
               typeStages.add(new Stage(type));
            }
         }
      }
      return typeStages;
   }
   
   public List<Stage> getAllStages() {
      return Collections.unmodifiableList(getStageValues());
   }
   
   /**
    * Gets a filtered view of all stages.
    * 
    * @param t
    *           If not null, filters to only allow stages with this type.
    * @param contains
    *           If not null, filters to only allow stages whose name (ignoring case) contains this
    *           String.
    * @return The filtered view as an unmodifiable list. Note: this list has no duplicates.
    */
   public List<Stage> getAllStagesFilteredBy(PkmType t, String contains) {
      LinkedHashSet<Stage> retStages = new LinkedHashSet<Stage>();
      String containsUpper = contains == null ? null : contains.toUpperCase();
      for (Stage s : getAllStages()) {
         PkmType thisType = s.getType();
         String thisNameUpper = s.getName().toUpperCase();
         if ((t == null || thisType.equals(t)) && (containsUpper == null || thisNameUpper.contains(containsUpper))) {
            retStages.add(s);
         }
      }
      return Collections.unmodifiableList(new ArrayList<Stage>(retStages));
   }
   
   /**
    * Gets the first valid stage object from the list of strings to interpret. If none of them are
    * valid, then the DEFAULT_STAGE is returned.
    * 
    * @param stages
    * @return
    */
   public Stage getStageMatch(List<String> stages) {
      Stage curStage = null;
      Iterator<String> itr = stages.iterator();
      while (itr.hasNext() && curStage == null) {
         curStage = getStageValue(itr.next());
      }
      return curStage == null ? DEFAULT_STAGE : curStage;
   }
}
