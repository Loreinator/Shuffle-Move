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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import shuffle.fwk.config.ConfigEntry;
import shuffle.fwk.config.ConfigFactory;
import shuffle.fwk.config.ConfigManager;
import shuffle.fwk.config.ConfigParser;
import shuffle.fwk.config.EntryType;
import shuffle.fwk.config.interpreter.TeamConfigInterpreter;
import shuffle.fwk.data.PkmType;
import shuffle.fwk.data.Species;
import shuffle.fwk.data.Stage;
import shuffle.fwk.data.Team;
import shuffle.fwk.data.TeamImpl;

/**
 * @author Andrew Meyers
 *
 */
public class TeamManager extends ConfigManager {
   private static final Logger LOG = Logger.getLogger(TeamManager.class.getName());
   
   private static final String KEY_FROZEN_KEYBIND = "FROZEN_KEYBIND";
   private static final String KEY_VALID_KEYBINDS = "VALID_KEYBINDS";
   private static final String KEY_PREF_KEYBINDS = "PREF_KEYBINDS";
   /** The key used to identify the team configuration path entry in a preferences file. */
   public static final String KEY_TEAMS_CONFIG = "TEAMS_CONFIG";
   public static final String KEY_OTHER_CONFIG = "OTHER_CONFIG";
   public static final String DEFAULT_KEYBINDS = "abcdeghijklmnopqrstuvwxyzABCDEGHIJKLMNOPQRSTUVWXYZ0123456789";
   public static final String DEFAULT_FROZEN = "fF";
   
   public TeamManager(List<String> resources, List<String> files, ConfigFactory factory) {
      super(resources, files, factory);
   }
   
   /**
    * @param manager
    */
   public TeamManager(ConfigManager manager) {
      super(manager);
   }
   
   @Override
   public boolean loadFromConfig() {
      boolean changed = super.loadFromConfig();
      // Verify data sanity by checking every team is composed of registered species
      SpeciesManager sm = getFactory().getSpeciesManager();
      for (String key : getKeys(EntryType.TEAM)) {
         Team team = getTeamValue(key);
         if (team != null) {
            List<String> names = team.getNames();
            List<Species> species = team.getSpecies(sm);
            if (names.size() != species.size()) {
               List<String> goodNames = species.stream().map(s -> s.getName()).collect(Collectors.toList());
               List<String> toRemove = new ArrayList<String>(names);
               toRemove.removeAll(goodNames);
               TeamImpl newTeam = new TeamImpl(team);
               toRemove.stream().forEach(s -> newTeam.removeName(s));
               setEntry(EntryType.TEAM, key, newTeam);
            }
         }
      }
      return changed;
   };
   
   /**
    * Returns the team of species for the given stage. If there is no such team for the specified
    * stage then it will attempt to use the configured stage for that type. If that is also empty or
    * not configured it will then return an empty set. The returned set is unmodifiable. See
    * {@link Collections#unmodifiableSet(Set)}. If the given stage is null this returns an empty
    * set.
    * 
    * @param stage
    * @return
    */
   public Team getTeamForStage(Stage stage) {
      return getTeamForStage(stage, true);
   }
   
   public Team getTeamForStage(Stage stage, boolean useFallbacks) {
      Team ret = null;
      if (stage != null) {
         ret = getTeamValue(stage.getName());
         if (ret == null && useFallbacks) {
            /*
             * If the stage's team does not exist, we will instead check for firstly the default
             * type stage's team. Then we will fall back to the largest stage's team for which the
             * stage's type matches the queried stage.
             */
            PkmType type = stage.getType();
            Stage typeStage = new Stage(type);
            ret = getTeamValue(typeStage.getName());
            if (ret == null) {
               ConfigManager stagesManager = getFactory().getStageManager();
               for (Stage s : stagesManager.getStageValues()) {
                  if (s.getType().equals(stage.getType())) {
                     Team team = getTeamValue(s.getName());
                     if (ret == null || team != null && team.getNames().size() > ret.getNames().size()) {
                        ret = team;
                     }
                  }
               }
            }
         }
      }
      if (ret != null || useFallbacks) {
         ret = new TeamImpl(ret);
      }
      return ret; // deep copy to avoid unwanted damage
   }
   
   public boolean setTeamForStage(Team team, Stage stage) {
      if (stage == null) {
         return false;
      }
      boolean changed = false;
      String key = stage.getName();
      if (team == null) {
         changed |= removeEntry(EntryType.TEAM, key);
      } else {
         try {
            ConfigEntry entry = new ConfigEntry(EntryType.TEAM, team);
            changed |= setEntry(key, entry);
         } catch (Exception e) {
            LOG.log(Level.WARNING, "Cannot set team for stage: ", e);
         }
      }
      return changed;
   }
   
   public static boolean setTeamForStageInMap(Team team, Stage stage, Map<Stage, TeamImpl> map) {
      if (stage == null) {
         return false;
      }
      Team prev = map.get(stage);
      boolean changing = !(prev == team || prev != null && prev.equals(team));
      if (changing) {
         map.remove(stage);
         if (team != null) { // if team is null, then we just simply remove the stage from the map.
            map.put(stage, new TeamImpl(team));
         }
      }
      return changing;
   }
   
   /**
    * Clears the stage's team for the specified stage, if it exists. This also removes the entry
    * entirely, so it can benefit from the fallback of {@link #getTeamForStage(Stage)}. If the given
    * stage is null this will do nothing.
    * 
    * @param stage
    * @return
    */
   public boolean removeTeamForStage(Stage stage) {
      return setTeamForStage(null, stage);
   }
   
   /**
    * Return the best binding for the given species.
    * 
    * @param s
    * @param stage
    * @return
    */
   public Character getBindingFor(Species s, Stage stage) {
      Team t = getTeamForStage(stage);
      return t.getBinding(s);
   }
   
   public boolean setBindingForSpecies(Stage stage, Species species, Character binding) {
      Team t = getTeamForStage(stage);
      boolean changed = isAvailable(binding, t);
      if (changed) {
         TeamImpl newTeam = new TeamImpl(t);
         newTeam.setBinding(species, binding);
         changed &= setTeamForStage(newTeam, stage);
      }
      return changed;
   }
   
   /**
    * Fills the bindings for the specified stage, according to the configured and default weightings
    * of relative suitability for each character.
    * 
    * @param stage
    *           the stage
    * @return True if this made a difference, false if otherwise.
    */
   public boolean fillBindingsForStage(Stage stage) {
      if (stage == null) {
         return false;
      }
      boolean changed = false;
      if (hasKey(EntryType.TEAM, stage.getName())) {
         Team oldTeam = getTeamValue(stage.getName());
         Team newTeam = fillBindingsForTeam(oldTeam);
         changed |= !oldTeam.equals(newTeam);
      } else {
         Team newTeam = new TeamImpl();
         changed |= setEntry(EntryType.TEAM, stage.getName(), newTeam);
      }
      return changed;
   }
   
   /**
    * @param oldTeam
    * @return
    */
   private Team fillBindingsForTeam(Team oldTeam) {
      TeamImpl newTeam = new TeamImpl(oldTeam);
      List<String> namesList = newTeam.getNames();
      for (String name : namesList) {
         LinkedHashSet<Character> prefs = getAllAvailableBindingsFor(name, newTeam);
         Iterator<Character> itr = prefs.iterator();
         while (newTeam.getBinding(name) == null && itr.hasNext()) {
            newTeam.setBinding(name, itr.next());
         }
         if (newTeam.getBinding(name) == null) {
            LOG.severe("Cannot find an appropriate binding for a species: " + String.valueOf(name));
         }
      }
      return newTeam;
   }
   
   private boolean isAvailable(Character c, Team t) {
      // Checks that the character is valid by all accounts, and not spoken for
      boolean available = c != null // Not null
            && !isFrozenBind(c) // not a 'frozen' hotkey
            && isValidBind(c); // is a valid key
      Iterator<String> itr = t.getNames().iterator(); // and for all of the team's species
      while (itr.hasNext() && available) { // none of their keys are equal to this one
         available &= !c.equals(t.getBinding(itr.next()));
      }
      return available;
   }
   
   public LinkedHashSet<Character> getAllAvailableBindingsFor(String name, Team team) {
      LinkedHashSet<Character> set = getAllBindingsFor(name);
      LinkedHashSet<Character> finalSet = new LinkedHashSet<Character>();
      for (Character c : set) {
         if (isAvailable(c, team) || c.equals(team.getBinding(name))) {
            finalSet.add(c);
         }
      }
      return finalSet;
   }
   
   /**
    * Lists all bindings possible by order of occurrence, then by
    * 
    * @param name
    *           The name of the species to check bindings for.
    * @return An ordered list without duplicates, by descending rate of occurrence.
    */
   public LinkedHashSet<Character> getAllBindingsFor(String name) {
      List<Character> secondChoices = new ArrayList<Character>(); // prefered list, (by
                                                                  // configuration)
      List<Character> firstChoices = getSpeciesKeybinds(name); // ranked list of user preferences
                                                               // (actively changes)
      ConfigManager cm = getFactory().getPreferencesManager();
      String preferredKeys = cm.getStringValue(KEY_PREF_KEYBINDS);
      if (preferredKeys != null) {
         for (char c : preferredKeys.toCharArray()) {
            secondChoices.add(new Character(c));
         }
      }
      List<Character> lastChoices = new ArrayList<Character>(); // valid list, (by configuration)
      String validKeys = cm.getStringValue(KEY_VALID_KEYBINDS, DEFAULT_KEYBINDS);
      if (validKeys != null) {
         for (char c : validKeys.toCharArray()) {
            lastChoices.add(new Character(c));
         }
      }
      // Only one entry per choice. Each entry is added by order of preference.
      LinkedHashSet<Character> allChoices = new LinkedHashSet<Character>(firstChoices);
      allChoices.addAll(secondChoices);
      allChoices.addAll(lastChoices);
      return allChoices;
   }
   
   private List<Character> getSpeciesKeybinds(String speciesName) {
      Map<Character, Integer> charMap = new HashMap<Character, Integer>();
      for (Team team : getTeamValues()) {
         for (String name : team.getNames()) {
            if (!name.equals(speciesName)) {
               continue;
            }
            Character c = team.getBinding(name);
            if (c != null) {
               if (charMap.containsKey(c)) {
                  charMap.put(c, charMap.get(c) + 1);
               } else if (isValidBind(c)) {
                  charMap.put(c, 1);
               }
            }
         }
      }
      List<Map.Entry<Character, Integer>> entries = new ArrayList<Map.Entry<Character, Integer>>(charMap.entrySet());
      Collections.sort(entries, new Comparator<Map.Entry<Character, Integer>>() {
         @Override
         public int compare(Map.Entry<Character, Integer> a, Map.Entry<Character, Integer> b) {
            return a.getValue().compareTo(b.getValue());
         }
      });
      List<Character> sortedFirstChoices = new ArrayList<Character>();
      for (Map.Entry<Character, Integer> entry : entries) {
         sortedFirstChoices.add(entry.getKey());
      }
      return sortedFirstChoices;
   }
   
   public Species getSpeciesFor(Character c, Stage stage) {
      Team team = getTeamForStage(stage);
      SpeciesManager manager = getFactory().getSpeciesManager();
      return team.getSpecies(c, manager);
   }
   
   public boolean isFrozenBind(char c) {
      ConfigManager cm = getFactory().getPreferencesManager();
      String frozenBinds = cm.getStringValue(KEY_FROZEN_KEYBIND, DEFAULT_FROZEN);
      return frozenBinds.contains(Character.toString(c));
   }
   
   public boolean isValidBind(char c) {
      ConfigManager cm = getFactory().getPreferencesManager();
      String validBinds = cm.getStringValue(KEY_VALID_KEYBINDS, DEFAULT_KEYBINDS);
      return validBinds.contains(Character.toString(c));
   }
   
   public static void main(String[] args) {
      // migrateFrom("config/teams.txt");
      ConfigManager manager = new ConfigFactory().getTeamManager();
      manager.saveDataToConfig();
      manager.loadFromConfig();
   }
   
   // For use to migrate configurations from the previous system
   public static void migrateFrom(String filePath, ConfigFactory factory) {
      ConfigManager manager = factory.getTeamManager();
      migrateUsing(filePath, manager);
      manager.saveDataToConfig();
   }
   
   public static void migrateUsing(String filePath, ConfigManager manager) {
      if (isLegacyFile(filePath)) {
         Map<String, List<TeamImpl>> parsedResults = ConfigParser.parseFile(filePath, new TeamConfigInterpreter());
         for (String key : parsedResults.keySet()) {
            for (TeamImpl team : parsedResults.get(key)) {
               if (team != null) {
                  ConfigEntry entry = new ConfigEntry(EntryType.TEAM, team);
                  manager.setEntry(EntryType.TEAM, key, entry);
                  break;
               }
            }
         }
      } else {
         TeamManager tm = new TeamManager(null, Arrays.asList(filePath), manager.getFactory());
         tm.loadFromConfig();
         manager.copyFromManager(tm);
      }
   }
   
   /**
    * @param filePath
    * @return
    */
   private static boolean isLegacyFile(String filePath) {
      return filePath.endsWith("teams.txt");
   }
   
   public boolean setMetalInTeam(TeamImpl team, boolean include, boolean extended) {
      TeamImpl before = new TeamImpl(team);
      String metalName = Species.METAL.getName();
      if (!include) {
         // Remove all metal species
         for (Species metalSpecies : Species.EXTENDED_METAL) {
            String name = metalSpecies.getName();
            team.removeName(name);
         }
         team.removeName(metalName);
      } else if (include) {
         // Add metal species
         if (extended) {
            // add ALL metal species.
            for (Species metalSpecies : Species.EXTENDED_METAL) {
               String name = metalSpecies.getName();
               if (!team.getNames().contains(name)) {
                  team.addName(name, getNextBindingFor(name, team));
               }
            }
            // Ensure the basic metal species is included
            if (!team.getNames().contains(metalName)) {
               team.addName(metalName, getNextBindingFor(metalName, team));
            }
         } else {
            // Ensure ONLY the base "METAL" species is there
            for (Species metalSpecies : Species.EXTENDED_METAL) {
               if (!metalSpecies.equals(Species.METAL)) {
                  // Don't remove the base metal species, to be kind to key-binds
                  String name = metalSpecies.getName();
                  team.removeName(name);
               }
            }
            // Ensure the basic metal species is included
            if (!team.getNames().contains(metalName)) {
               team.addName(metalName, getNextBindingFor(metalName, team));
            }
         }
      }
      return !before.equals(team);
   }
   
   public char getNextBindingFor(String name, Team team) {
      return getAllAvailableBindingsFor(name, team).iterator().next();
   }
}
