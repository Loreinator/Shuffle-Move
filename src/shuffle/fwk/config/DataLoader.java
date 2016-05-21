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

package shuffle.fwk.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import shuffle.fwk.config.interpreter.DefaultInterpreter;

/**
 * @author Andrew Meyers
 *
 */
public class DataLoader<Y> {
   private final List<String> fileLocations;
   private final List<String> resourceLocations;
   private boolean shouldReload = true;
   private Map<String, List<Y>> myMap = new LinkedHashMap<String, List<Y>>();
   
   /**
    * Default constructor. Include load locations to ensure proper functionality. Otherwise, this is
    * a useless object.
    * 
    * @param resources
    * @param files
    */
   public DataLoader(List<String> resources, List<String> files) {
      if (resources == null) {
         resourceLocations = Collections.emptyList();
      } else {
         resourceLocations = new ArrayList<String>(resources);
      }
      if (files == null) {
         fileLocations = Collections.emptyList();
      } else {
         fileLocations = new ArrayList<String>(files);
      }
      if (resourceLocations.isEmpty() && fileLocations.isEmpty()) {
         throw new IllegalArgumentException("Not enough load locations. Please recheck.");
      }
   }
   
   public void setForceReload(boolean reload) {
      shouldReload = reload;
   }
   
   /**
    * If true, this guarantees that unless a reload occurs there MUST be some non-null value
    * assigned to this key.
    * 
    * @param key
    * @return
    */
   public boolean containsKey(String key) {
      manageReload();
      return key != null && myMap.containsKey(key) && myMap.get(key).iterator().next() != null;
   }
   
   public Set<String> getKeys() {
      manageReload();
      return Collections.unmodifiableSet(myMap.keySet());
   }
   
   public List<Y> getValues(String key) {
      manageReload();
      List<Y> ret = new ArrayList<Y>();
      if (key != null && myMap.containsKey(key)) {
         ret = myMap.get(key);
      }
      return Collections.unmodifiableList(ret);
   }
   
   public Y getFirstValue(String key) {
      manageReload();
      for (Y v : getValues(key)) {
         if (v != null) {
            return v;
         }
      }
      return null;
   }
   
   private void manageReload() {
      if (shouldReload) {
         myMap.clear();
         for (String loc : fileLocations) {
            Map<String, List<Y>> resultMap = ConfigParser.parseFile(loc, getInterpreter());
            mergeSafely(myMap, resultMap);
         }
         for (String resource : resourceLocations) {
            Map<String, List<Y>> resultMap = ConfigParser.parseResource(resource, getInterpreter());
            mergeSafely(myMap, resultMap);
         }
         shouldReload = false;
      }
   }
   
   public Map<String, List<Y>> getMap() {
      manageReload();
      Map<String, List<Y>> ret = new LinkedHashMap<String, List<Y>>();
      for (String key : myMap.keySet()) {
         ret.put(key, getValues(key));
      }
      return Collections.unmodifiableMap(ret);
   }
   
   /**
    * Folds all results from the secondary into the primary map. The primary is altered, the
    * secondary is not. Does not return anything.
    */
   public static <X> void mergeSafely(Map<String, List<X>> primary, Map<String, List<X>> secondary) {
      if (primary == null || secondary == null) {
         return;
      }
      for (String key : secondary.keySet()) {
         if (primary.containsKey(key)) {
            primary.get(key).addAll(secondary.get(key));
         } else {
            primary.put(key, secondary.get(key));
         }
      }
   }
   
   public Interpreter<Y> getInterpreter() {
      return new DefaultInterpreter<Y>();
   }
}
