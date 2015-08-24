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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.function.Predicate;

import shuffle.fwk.config.ConfigEntry;
import shuffle.fwk.config.ConfigFactory;
import shuffle.fwk.config.ConfigManager;
import shuffle.fwk.config.EntryType;
import shuffle.fwk.data.Species;

/**
 * @author Andrew Meyers
 *
 */
public class SpeciesManager extends ConfigManager {
   
   public SpeciesManager(List<String> loadPaths, List<String> writePaths, ConfigFactory factory) {
      super(loadPaths, writePaths, factory);
   }
   
   /**
    * @param manager
    */
   public SpeciesManager(ConfigManager manager) {
      super(manager);
   }
   
   @Override
   protected boolean shouldUpdate() {
      return true;
   }

   @Override
   public boolean loadFromConfig() {
      boolean changed = super.loadFromConfig();
      changed |= setDefaultSpecies();
      Map<Integer, List<Species>> dexToSpecies = new HashMap<Integer, List<Species>>();
      for (Species s : getSpeciesValues()) {
         if (dexToSpecies.containsKey(s.getNumber())) {
            dexToSpecies.get(s.getNumber()).add(s);
         } else {
            dexToSpecies.put(s.getNumber(), new ArrayList<Species>(Arrays.asList(s)));
         }
      }
      ImageManager im = getFactory().getImageManager();
      for (Integer dex : dexToSpecies.keySet()) {
         List<Species> list = dexToSpecies.get(dex);
         if (list.size() > 1) {
            Species keep = list.get(0);
            for (int i = 1; i < list.size() && im.getImageFor(keep) == null; i++) {
               keep = list.get(i);
            }
            if (im.getImageFor(keep) != null) {
               final String keepName = keep.getName();
               for (Species s : list) {
                  String name = s.getName();
                  if (!name.equals(keepName)) {
                     removeEntry(EntryType.SPECIES, name);
                  }
               }
            }
         }
      }
      return changed;
   };
   
   @Override
   public boolean copyFromManager(ConfigManager manager) {
      boolean changed = super.copyFromManager(manager);
      changed |= setDefaultSpecies();
      return changed;
   }
   
   private boolean setDefaultSpecies() {
      boolean changed = false;
      for (Species species : Species.FIXED_SPECIES) {
         changed |= setEntry(EntryType.SPECIES, species.getName(), species);
      }
      return changed;
   }
   
   @Override
   public boolean setEntry(EntryType type, String key, ConfigEntry entry, Integer index) {
      if (key == null || type == null) {
         return false;
      }
      boolean isReserved = false;
      if (EntryType.SPECIES.equals(type)) {
         for (Species species : Species.FIXED_SPECIES) {
            isReserved |= species.getName().equals(key) && !species.equals(entry.getValue());
         }
      }
      boolean changed = false;
      if (!isReserved) {
         changed |= super.setEntry(type, key, entry, index);
      }
      return changed;
   }
   
   public boolean setSpeciesByName(String name, Species s) {
      boolean changed = setEntry(EntryType.SPECIES, name, s);
      changed |= setDefaultSpecies();
      return changed;
   }
   
   public Species getSpeciesByName(String name) {
      return getSpeciesValue(name, Species.AIR);
   }
   
   public Collection<Species> getAllSpecies() {
      return getSpeciesByFilters(Collections.emptyList());
   }
   
   public Collection<Species> getSpeciesByFilters(List<Predicate<Species>> filters) {
      List<Predicate<Species>> filtersToCheck = new ArrayList<Predicate<Species>>(filters.size() + 1);
      filtersToCheck.add(species -> species != null);
      filtersToCheck.addAll(filters);
      List<String> keys = getKeys(EntryType.SPECIES);
      Collection<Species> speciesList = new TreeSet<Species>(getSpeciesComparator());
      for (String speciesName : keys) {
         Species species = getSpeciesValue(speciesName);
         boolean canAdd = species != null;
         Iterator<Predicate<Species>> itr = filtersToCheck.iterator();
         while (canAdd && itr.hasNext()) {
            canAdd &= itr.next().test(species);
         }
         if (canAdd) {
            speciesList.add(species);
         }
      }
      
      return speciesList;
   }
   
   /**
    * @return
    */
   public Comparator<Species> getSpeciesComparator() {
      return new Comparator<Species>() {
         @Override
         public int compare(Species o1, Species o2) {
            if (o1 == null && o2 != null) {
               return -1;
            } else if (o1 != null && o2 == null) {
               return 1;
            } else if (o1 == o2) {
               return 0;
            } else {
               return Integer.compare(o1.getNumber(), o2.getNumber());
            }
         }
      };
   }
   
   public int getNextNumber() {
      int nextNumber = 0;
      for (Species s : getAllSpecies()) {
         nextNumber = Math.max(nextNumber, s.getNumber() + 10);
      }
      return nextNumber;
   }
}
