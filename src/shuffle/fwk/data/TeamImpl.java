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

package shuffle.fwk.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;

import shuffle.fwk.config.manager.EffectManager;
import shuffle.fwk.config.manager.RosterManager;
import shuffle.fwk.config.manager.SpeciesManager;

/**
 * @author Andrew Meyers
 *
 */
public class TeamImpl implements Team {
   
   private static final String NO_MEGA = "-";
   
   private Set<String> teamNames;
   private Map<String, Character> nameToKey;
   private Map<Character, String> keyToName;
   private Set<String> nonSupport;
   private String megaSlotName = null;
   
   public TeamImpl() {
      teamNames = new LinkedHashSet<String>();
      nameToKey = new HashMap<String, Character>();
      keyToName = new HashMap<Character, String>();
      nonSupport = new TreeSet<String>();
   }
   
   public TeamImpl(Team ret) {
      this();
      if (ret != null && !ret.getNames().isEmpty()) {
         for (String s : ret.getNames()) {
            addName(s, ret.getBinding(s));
            if (ret.isMegaSlot(s)) {
               megaSlotName = s;
            }
            if (ret.isNonSupport(s)) {
               setNonSupport(s, true);
            }
         }
      }
   }
   
   /*
    * (non-Javadoc)
    * @see shuffle.fwk.data.Team#getSpecies(int, shuffle.fwk.data.manager.SpeciesManager)
    */
   @Override
   public Species getSpecies(int keyCode, SpeciesManager manager) {
      String name = getName(keyCode);
      Species ret = null;
      if (manager != null) {
         ret = manager.getSpeciesByName(name);
      }
      return ret;
   }
   
   /*
    * (non-Javadoc)
    * @see shuffle.fwk.data.Team#getSpecies(java.lang.Character,
    * shuffle.fwk.data.manager.SpeciesManager)
    */
   @Override
   public Species getSpecies(Character binding, Function<String, Species> nameToSpecies) {
      String name = getName(binding);
      Species ret = null;
      if (nameToSpecies != null) {
         ret = nameToSpecies.apply(name);
      }
      return ret;
   }
   
   /*
    * (non-Javadoc)
    * @see shuffle.fwk.data.Team#getName(int)
    */
   @Override
   public String getName(int keyCode) {
      return getName(getCharacter(keyCode));
   }
   
   /*
    * (non-Javadoc)
    * @see shuffle.fwk.data.Team#getName(java.lang.Character)
    */
   @Override
   public String getName(Character binding) {
      if (binding == null) {
         return null;
      }
      return keyToName.get(binding);
   }
   
   private Character getCharacter(int keyCode) {
      Character ret = null;
      ret = Character.valueOf((char) keyCode);
      return ret;
   }
   
   /*
    * (non-Javadoc)
    * @see shuffle.fwk.data.Team#getBinding(java.lang.String)
    */
   @Override
   public Character getBinding(String s) {
      if (s == null) {
         return null;
      }
      return nameToKey.get(s);
   }
   
   /*
    * (non-Javadoc)
    * @see shuffle.fwk.data.Team#getBinding(shuffle.fwk.data.Species)
    */
   @Override
   public Character getBinding(Species s) {
      if (s == null) {
         return null;
      }
      return nameToKey.get(s.getName());
   }
   
   /*
    * (non-Javadoc)
    * @see shuffle.fwk.data.Team#getNames()
    */
   @Override
   public List<String> getNames() {
      return Collections.unmodifiableList(new ArrayList<String>(teamNames));
   }
   
   /*
    * (non-Javadoc)
    * @see shuffle.fwk.data.Team#getSpecies(SpeciesManager manager)
    */
   @Override
   public List<Species> getSpecies(SpeciesManager manager) {
      List<Species> ret = new ArrayList<Species>();
      if (manager != null) {
         for (String name : teamNames) {
            Species result = manager.getSpeciesByName(name);
            if (!result.equals(Species.AIR)) { // AIR can't be on a team.
               ret.add(result);
            }
         }
      }
      return ret;
   }
   
   /*
    * (non-Javadoc)
    * @see shuffle.fwk.data.Team#getNonSupportNames()
    */
   @Override
   public List<String> getNonSupportNames() {
      return Collections.unmodifiableList(new ArrayList<String>(nonSupport));
   }
   
   /*
    * (non-Javadoc)
    * @see shuffle.fwk.data.Team#getNonSupportSpecies()
    */
   @Override
   public List<Species> getNonSupportSpecies(SpeciesManager manager) {
      List<Species> ret = new ArrayList<Species>();
      if (manager != null) {
         for (String name : nonSupport) {
            Species result = manager.getSpeciesByName(name);
            if (!result.equals(Species.AIR)) { // AIR can't be on a team.
               ret.add(result);
            }
         }
      }
      return ret;
   }
   
   /**
    * Sets the binding for the specified species to the given character. If the species does not
    * exist, this does nothing. If the binding is null this will clear the binding. If the species
    * is null this will do nothing.
    * 
    * @param s
    *           The species to change the binding for
    * @param binding
    *           the binding to set. If null this clears the binding.
    * @return True if this action changed the team in any way.
    */
   public boolean setBinding(String s, Character binding) {
      if (s == null || !teamNames.contains(s)) {
         return false;
      }
      
      Character prevKey;
      if (binding == null) { // binding removal
         prevKey = nameToKey.remove(s);
         if (prevKey != null) { // previous binding was set
            keyToName.remove(prevKey); // so we remove that too
         }
      } else { // Binding not null
         prevKey = nameToKey.put(s, binding); // add in the binding for this species
         keyToName.remove(prevKey); // remove the previous binding
         String prevSpecies = keyToName.put(binding, s); // add the binding registered to this
                                                         // species
         if (prevSpecies != null) { // if there used to be a species assigned to this binding
            nameToKey.remove(prevSpecies); // now they have nothing!
         }
      }
      // So we've either removed the binding, in which case prevKey is the old
      // binding or we've set this binding for the species, which would mean
      // prevKey is the old binding. If we've overwritten someone else's
      // binding then they no longer have a key binding, and their binding
      // doesn't map to them either. The prevKey is still the previous binding
      // for this given species.
      return prevKey != binding && (prevKey == null || !prevKey.equals(binding));
   }
   
   public boolean setBinding(Species s, Character c) {
      return setBinding(s.getName(), c);
   }
   
   /**
    * Removes the given species, and its mapping.
    * 
    * @param s
    *           the Species
    * @return True if this affected the team.
    */
   public boolean removeName(String s) {
      if (s == null) {
         return false;
      }
      Character oldKey = nameToKey.remove(s);
      if (oldKey != null) {
         keyToName.remove(oldKey);
      }
      if (s.equals(megaSlotName)) {
         megaSlotName = null;
      }
      nonSupport.remove(s);
      return teamNames.remove(s);
   }
   
   /**
    * Registers the specified species with the given binding. If the binding is null it will have no
    * specified binding.
    * 
    * @param s
    *           the Species
    * @param binding
    *           The binding.
    * @return true if this changed either the team species or the bindings
    */
   public boolean addName(String s, Character binding) {
      if (s == null) {
         return false;
      }
      boolean changed = teamNames.add(s);
      if (changed) {
         changed |= setBinding(s, binding);
      }
      if (Species.FIXED_SPECIES_NAMES.contains(s)) {
         setNonSupport(s, true);
      }
      return changed;
   }
   
   public boolean setNonSupport(Species species, boolean isNonSupport) {
      return setNonSupport(species.getName(), isNonSupport);
   }
   
   public boolean setNonSupport(String name, boolean isNonSupport) {
      if (name == null) {
         return false;
      }
      boolean changed = false;
      if (isNonSupport) {
         changed = nonSupport.add(name);
         if (name.equals(megaSlotName)) {
            megaSlotName = null;
         }
      } else {
         changed = nonSupport.remove(name);
      }
      return changed;
   }
   
   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder();
      Iterator<String> itr = teamNames.iterator();
      while (itr.hasNext()) {
         sb.append(itr.next());
         if (itr.hasNext()) {
            sb.append(",");
         }
      }
      if (!keyToName.isEmpty()) {
         sb.append(" ");
         itr = teamNames.iterator();
         while (itr.hasNext()) {
            String name = itr.next();
            if (nameToKey.containsKey(name)) {
               sb.append(nameToKey.get(name).toString());
            }
            if (itr.hasNext()) {
               sb.append(",");
            }
         }
         sb.append(" ");
         if (megaSlotName == null || megaSlotName.isEmpty()) {
            sb.append(NO_MEGA);
         } else {
            sb.append(megaSlotName);
         }
         if (!nonSupport.isEmpty()) {
            sb.append(" ");
            itr = nonSupport.iterator();
            while (itr.hasNext()) {
               sb.append(itr.next());
               if (itr.hasNext()) {
                  sb.append(",");
               }
            }
         }
      }
      return sb.toString();
   }
   
   @Override
   public boolean equals(Object obj) {
      boolean equal = obj != null && obj instanceof TeamImpl;
      if (equal) {
         TeamImpl other = (TeamImpl) obj;
         equal &= teamNames.equals(other.teamNames);
         equal &= nameToKey.equals(other.nameToKey);
         equal &= keyToName.equals(other.keyToName);
         equal &= megaSlotName == other.megaSlotName || megaSlotName != null && megaSlotName.equals(other.megaSlotName);
         equal &= nonSupport.equals(other.nonSupport);
      }
      return equal;
   }
   
   @Override
   public boolean isMegaSlot(String name) {
      return name != null && name.equals(megaSlotName);
   }
   
   @Override
   public int getMegaThreshold(SpeciesManager speciesManager, RosterManager rosterManager,
         EffectManager effectManager) {
      if (speciesManager == null || megaSlotName == null) {
         return Integer.MAX_VALUE;
      } else {
         final Species megaSpecies = speciesManager.getSpeciesByName(megaSlotName);
         return rosterManager.getMegaThresholdFor(megaSpecies, effectManager);
      }
   }
   
   @Override
   public String getMegaSlotName() {
      return megaSlotName;
   }
   
   public boolean setMegaSlot(String name) {
      String prev = megaSlotName;
      megaSlotName = NO_MEGA.equals(name) ? null : name;
      return !(prev == megaSlotName || prev != null && prev.equals(megaSlotName));
   }
   
   /*
    * (non-Javadoc)
    * @see shuffle.fwk.data.Team#isNonSupport(java.lang.String)
    */
   @Override
   public boolean isNonSupport(String name) {
      return nonSupport.contains(name);
   }
   
   /*
    * (non-Javadoc)
    * @see shuffle.fwk.data.Team#isNonSupport(shuffle.fwk.data.Species)
    */
   @Override
   public boolean isNonSupport(Species species) {
      return nonSupport.contains(species.getName());
   }
}
