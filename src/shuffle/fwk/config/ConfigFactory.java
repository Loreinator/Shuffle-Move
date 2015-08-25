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

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import shuffle.fwk.config.manager.ImageManager;
import shuffle.fwk.config.manager.RosterManager;
import shuffle.fwk.config.manager.SpeciesManager;
import shuffle.fwk.config.manager.StageManager;
import shuffle.fwk.config.manager.TeamManager;

/**
 * @author Andrew Meyers
 *
 */
public class ConfigFactory {
   private static final Logger LOG = Logger.getLogger(ConfigFactory.class.getName());
   
   private static final String KEY_R_PATHS = "config/paths.txt";
   
   private static final String KEY_RESOURCE_PREFERENCES = "PREFERENCES";
   private static final String KEY_RESOURCE_SPECIES = "SPECIES";
   private static final String KEY_RESOURCE_STAGES = "STAGES";
   private static final String KEY_RESOURCE_ICONS = "ICONS";
   private static final String KEY_FILE_PREFERENCES = "PREFERENCES";
   private static final String KEY_FILE_SPECIES = "SPECIES";
   private static final String KEY_FILE_STAGES = "STAGES";
   private static final String KEY_FILE_ICONS = "ICONS";
   private static final String KEY_FILE_TEAMS = "TEAM";
   private static final String KEY_FILE_ROSTER = "ROSTER";
   
   private Map<Object, ConfigManager> managers = new HashMap<Object, ConfigManager>();
   
   private ConfigManager pathsConfigManager;
   private final String pathConfigPath;
   
   public ConfigFactory() {
      this(null);
   }
   
   public ConfigFactory(String pathsPath) {
      if (pathsPath == null) {
         pathConfigPath = KEY_R_PATHS;
      } else {
         pathConfigPath = pathsPath;
      }
   }
   
   public boolean isDataChanged() {
      for (ConfigManager manager : managers.values()) {
         if (manager.dataChanged()) {
            return true;
         }
      }
      return false;
   }
   
   /**
    * Reloads all ConfigManagers which have been used since the runtime began.
    * 
    * @return True if anything changed.
    */
   public boolean loadAllFromConfig() {
      boolean changed = false;
      Set<ConfigManager> managersToLoad = new HashSet<ConfigManager>(managers.values());
      for (ConfigManager manager : managersToLoad) {
         changed |= manager.loadFromConfig();
      }
      return changed;
   }
   
   /**
    * Saves all managers to file.
    */
   public void saveAllToConfig() {
      Set<ConfigManager> managersToSave = new HashSet<ConfigManager>(managers.values());
      for (ConfigManager manager : managersToSave) {
         manager.saveDataToConfig();
      }
   }
   
   /**
    * @param resource
    * @return
    */
   private List<String> getFiltered(String resource) {
      return Arrays.asList(resource).stream().filter(f -> f != null).collect(Collectors.toList());
   }
   
   public List<String> getFilePaths(File... files) {
      return new ArrayList<String>(Arrays.asList(files).stream().filter(f -> f != null).map(File::getPath)
            .collect(Collectors.toList()));
   }
   
   /**
    * Gets an instance of ConfigManager which will load from the specified resources and files. The
    * manager may or may not save to the given files.
    * 
    * @param reources
    * @param files
    * @return The ConfigManager that matches the specified paths.
    */
   public <T extends ConfigManager> T getManager(String resourceKey, String fileKey, Class<T> c) {
      ConfigManager pathManager = getPathManager();
      String resource = pathManager.getResourceValue(resourceKey);
      File file = pathManager.getFileValue(fileKey);
      return getManager(getFiltered(resource), getFilePaths(file), c);
   }
   
   public <T extends ConfigManager> T getManager(List<String> resources, List<String> files, Class<T> c) {
      Object key = getManagerKey(resources, files, c);
      T ret = null;
      if (managers.containsKey(key)) {
         ConfigManager value = managers.get(key);
         if (c.isInstance(value)) {
            ret = c.cast(value);
         } else {
            managers.remove(key);
         }
      }
      if (!managers.containsKey(key)) {
         if (resources.isEmpty() && files.isEmpty()) {
            throw new IllegalArgumentException("Cannot get an instance without at least one path.");
         } else {
            try {
               Constructor<T> con = c.getConstructor(List.class, List.class, ConfigFactory.class);
               ret = con.newInstance(resources, files, this);
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                  | InvocationTargetException | NoSuchMethodException | SecurityException e) {
               LOG.log(Level.SEVERE, "Cannot create config manager of class " + c.toString(), e);
               if (e.getCause() != null) {
                  LOG.log(Level.SEVERE, "The cause was: ", e.getCause());
               }
            }
         }
         if (ret != null) {
            managers.put(key, ret);
         }
      }
      return ret;
   }
   
   /**
    * Gets the key from the given paths.
    * 
    * @param resources
    * @param files
    * @return
    */
   private Object getManagerKey(List<String> resources, List<String> files, Class<? extends ConfigManager> c) {
      if (resources == null) {
         resources = Collections.emptyList();
      }
      if (files == null) {
         files = Collections.emptyList();
      }
      List<Object> key = Arrays.asList(resources, files, c.toString());
      return key;
   }
   
   public ConfigManager getPathManager() {
      if (pathsConfigManager == null) {
         List<String> paths = Arrays.asList(pathConfigPath);
         pathsConfigManager = getManager(paths, paths, ConfigManager.class);
      }
      return pathsConfigManager;
   }
   
   public ConfigManager getPreferencesManager() {
      return getManager(KEY_RESOURCE_PREFERENCES, KEY_FILE_PREFERENCES, ConfigManager.class);
   }
   
   public SpeciesManager getSpeciesManager() {
      return getManager(KEY_RESOURCE_SPECIES, KEY_FILE_SPECIES, SpeciesManager.class);
   }
   
   public StageManager getStageManager() {
      return getManager(KEY_RESOURCE_STAGES, KEY_FILE_STAGES, StageManager.class);
   }
   
   public ImageManager getImageManager() {
      return getManager(KEY_RESOURCE_ICONS, KEY_FILE_ICONS, ImageManager.class);
   }
   
   public TeamManager getTeamManager() {
      return getManager(null, KEY_FILE_TEAMS, TeamManager.class);
   }
   
   public RosterManager getRosterManager() {
      return getManager(null, KEY_FILE_ROSTER, RosterManager.class);
   }
   
}
