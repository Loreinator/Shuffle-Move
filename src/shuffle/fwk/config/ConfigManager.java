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

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;

import shuffle.fwk.EntryMode;
import shuffle.fwk.ShuffleVersion;
import shuffle.fwk.config.writer.PreferencesWriter;
import shuffle.fwk.data.Species;
import shuffle.fwk.data.SpeciesPaint;
import shuffle.fwk.data.Stage;
import shuffle.fwk.data.Team;
import shuffle.fwk.update.UpdateCheck;

/**
 * @author Andrew Meyers
 *
 */
public class ConfigManager {
   private static final Logger LOG = Logger.getLogger(ConfigManager.class.getName());
   static {
      LOG.setLevel(Level.FINE);
   }
   
   private static final String KEY_VERSION = "VERSION";
   private static final String KEY_BUILD_DATE = "BUILD_DATE";

   private final List<String> filePaths;
   private final ConfigLoader loader;
   private final ConfigFactory factory;
   private final LinkedHashMap<EntryType, LinkedHashMap<String, ConfigEntry>> data = new LinkedHashMap<EntryType, LinkedHashMap<String, ConfigEntry>>();
   private final Map<String, List<String>> savedDataStrings = new LinkedHashMap<String, List<String>>();
   
   public ConfigManager(List<String> resources, List<String> files, ConfigFactory factory) {
      this.factory = factory;
      loader = new ConfigLoader(resources, files);
      filePaths = files;
      loadFromConfig();
      savedDataStrings.clear();
      savedDataStrings.putAll(getDataStrings());
   }
   
   public ConfigManager(ConfigManager copyFrom) {
      factory = copyFrom.factory;
      filePaths = copyFrom.filePaths;
      loader = copyFrom.loader.getNewCopy();
      copyFromManager(copyFrom);
   }
   
   public ConfigFactory getFactory() {
      return factory;
   }
   
   public <T extends ConfigManager> boolean copyFromManager(T manager) {
      boolean changed = false;
      synchronized (data) {
         LinkedHashMap<String, List<String>> oldData = getDataStrings();
         data.clear();
         for (EntryType type : EntryType.values()) {
            LinkedHashMap<String, ConfigEntry> mappings = new LinkedHashMap<String, ConfigEntry>();
            List<String> keyOrder = new ArrayList<String>();
            for (String key : manager.getKeys(type)) {
               ConfigEntry entry = manager.getEntry(type, key);
               keyOrder.add(key);
               mappings.put(key, entry);
            }
            data.put(type, mappings);
         }
         LinkedHashMap<String, List<String>> newData = getDataStrings();
         changed |= !oldData.equals(newData);
         onCopyFrom(manager);
      }
      return changed;
   }
   
   protected <T extends ConfigManager> void onCopyFrom(T manager) {
      // Empty default implementation
   }
   
   protected void clearMappingsOfType(EntryType type) {
      synchronized (data) {
         data.get(type).clear();
      }
   }
   
   protected void putAllOfType(EntryType type, Map<String, ? extends Object> map) {
      synchronized (data) {
         LinkedHashMap<String, ConfigEntry> mappings = data.get(type);
         Class<?> classForCast = type.getDataClass();
         for (String key : map.keySet()) {
            Object obj = map.get(key);
            if (obj != null && classForCast.isInstance(obj)) {
               ConfigEntry entry = new ConfigEntry(type, obj);
               mappings.put(key, entry);
            }
         }
      }
   }
   
   public boolean loadFromConfig() {
      loader.setForceReload(true);
      boolean changed = false;
      synchronized (data) {
         LinkedHashMap<String, List<String>> oldData = getDataStrings();
         data.clear();
         for (EntryType type : EntryType.values()) {
            LinkedHashMap<String, ConfigEntry> mappings = loader.getMappings(type);
            data.put(type, mappings);
         }
         maintainVersion();
         LinkedHashMap<String, List<String>> newData = getDataStrings();
         changed |= !oldData.equals(newData);
         if (changed) {
            savedDataStrings.clear();
            savedDataStrings.putAll(newData);
         }
      }
      return changed;
   }
   
   protected boolean shouldUpdate() {
      return false;
   }

   /**
    * 
    */
   protected void maintainVersion() {
      if (shouldUpdate()) {
         if (isOutOfDate()) {
            // we need to update from the default, overriding any out-dated keys
            loader.updateFromResource();
            for (EntryType type : EntryType.values()) {
               LinkedHashMap<String, ConfigEntry> mappings = loader.getMappings(type);
               data.put(type, mappings);
            }
         }
      }
      setDateStamp();
   }
   
   protected final void setDateStamp() {
      setEntry(EntryType.STRING, KEY_VERSION, ShuffleVersion.VERSION_FULL);
      setEntry(EntryType.LONG, KEY_BUILD_DATE, ShuffleVersion.BUILD_DATE);
   }
   
   public final boolean isOutOfDate() {
      try {
         String fileVersionString = getStringValue(KEY_VERSION, UpdateCheck.NO_VERSION);
         int[] fileVersionNumbers = new UpdateCheck().getVersionNumbers(fileVersionString);
         int[] curVersionNumbers = ShuffleVersion.VERSION_ARRAY;
         boolean uptodate = true;
         for (int i = 0; i < 3; i++) {
            if (fileVersionNumbers[i] > curVersionNumbers[i]) {
               break;
            } else if (fileVersionNumbers[i] < curVersionNumbers[i]) {
               uptodate = false;
               break;
            }
         }
         Long fileBuildDate = getLongValue(KEY_BUILD_DATE, 0l);
         uptodate &= fileBuildDate >= ShuffleVersion.BUILD_DATE;
         return !uptodate;
      } catch (Exception e) {
         // then we assume it has no saved version.
         return true;
      }
   }

   public void saveDataToConfig() {
      PreferencesWriter pw = new PreferencesWriter(filePaths);
      LinkedHashMap<String, List<String>> dataToWrite;
      synchronized (data) {
         dataToWrite = getDataStrings();
         savedDataStrings.clear();
         savedDataStrings.putAll(dataToWrite);
      }
      pw.writePreferences(dataToWrite);
   }
   
   /**
    * @return
    */
   private LinkedHashMap<String, List<String>> getDataStrings() {
      LinkedHashMap<String, List<String>> dataToWrite = new LinkedHashMap<String, List<String>>();
      for (EntryType type : EntryType.values()) {
         if (!data.containsKey(type)) {
            continue;
         }
         List<String> linesToWrite = new ArrayList<String>();
         LinkedHashMap<String, ConfigEntry> typeMappings = data.get(type);
         for (String key : typeMappings.keySet()) {
            ConfigEntry entry = typeMappings.get(key);
            String entryString = safelyGetSaveString(entry);
            linesToWrite.add(String.format("%s %s", key, entryString));
         }
         dataToWrite.put(type.toString(), linesToWrite);
      }
      return dataToWrite;
   }
   
   /**
    * @param entry
    * @return
    * @throws Exception
    */
   private String safelyGetSaveString(ConfigEntry entry) {
      String saveString = null;
      try {
         saveString = entry.getSaveString();
      } catch (Exception e) {
         LOG.log(Level.WARNING, "Cannot get the save string for: " + entry, e);
      }
      return saveString;
   }
   
   public ConfigEntry getEntry(EntryType type, String key) {
      ConfigEntry value = null;
      if (key != null && type != null) {
         synchronized (data) {
            if (data.containsKey(type) && data.get(type).containsKey(key)) {
               value = data.get(type).get(key);
            }
         }
      }
      return value;
   }
   
   /**
    * Sets the value for the given key to the given newValue.
    * 
    * @param key
    * @param newValue
    * @param index
    *           TODO
    * 
    * @return True if anything changed. False otherwise.
    */
   public boolean setEntry(EntryType type, String key, ConfigEntry newValue) {
      if (key == null || type == null) {
         return false;
      }
      boolean changed = false;
      synchronized (data) {
         if (data.containsKey(type)) {
            LinkedHashMap<String, ConfigEntry> mappings = data.get(type);
            ConfigEntry oldValue = mappings.get(key);
            changed |= !(newValue == oldValue || newValue != null && newValue.equals(oldValue));
            if (newValue == null) {
               mappings.remove(key);
            } else {
               mappings.put(key, newValue);
            }
         }
      }
      return changed;
   }
   
   public boolean setEntry(String key, ConfigEntry newValue) {
      if (key == null || newValue == null) {
         return false;
      }
      return setEntry(newValue.getEntryType(), key, newValue);
   }
   
   public boolean setEntry(EntryType type, String key, Object newValue) {
      boolean result = false;
      try {
         ConfigEntry entry = newValue == null ? null : new ConfigEntry(type, newValue);
         result = setEntry(type, key, entry);
      } catch (Exception e) {
         LOG.log(Level.FINE,
               String.format("Cannot parse config entry, key: %s type: %s value: %s ", key, type, newValue), e);
      }
      return result;
   }
   
   public boolean removeEntry(EntryType type, String key) {
      return setEntry(type, key, null);
   }
   
   public boolean hasKey(EntryType type, String key) {
      boolean ret = false;
      synchronized (data) {
         if (type != null && key != null && data.containsKey(type)) {
            ret = data.get(type).containsKey(key);
         }
      }
      return ret;
   }
   
   public List<String> getKeys(EntryType type) {
      List<String> ret = new ArrayList<String>();
      synchronized (data) {
         ret = new ArrayList<String>(data.get(type).keySet());
      }
      return ret;
   }
   
   public <T extends Object> List<T> getValues(EntryType type, Class<T> c) {
      List<T> ret = new ArrayList<T>();
      synchronized (data) {
         LinkedHashMap<String, ConfigEntry> mappings = data.get(type);
         for (String key : mappings.keySet()) {
            ConfigEntry entry = mappings.get(key);
            Object value = entry.getValue();
            if (c.isInstance(value)) {
               T casted = c.cast(value);
               ret.add(casted);
            }
         }
      }
      return ret;
   }
   
   /**
    * Gets the value for the given key.
    * 
    * @param key
    * @param c
    * @return The value, null if not found.
    */
   public <T extends Object> T getValue(EntryType type, String key, Class<T> c) {
      T ret = null;
      synchronized (data) {
         ConfigEntry entry = data.get(type).get(key);
         if (entry != null) {
            Object value = entry.getValue();
            if (c.isInstance(value)) {
               ret = c.cast(value);
            }
         }
      }
      return ret;
   }
   
   public <T extends Object> T getValue(EntryType type, String key, Class<T> c, T def) {
      T value = getValue(type, key, c);
      if (value == null) {
         value = def;
      }
      return value;
   }
   
   // Boolean
   public Boolean getBooleanValue(String key) {
      return getValue(EntryType.BOOLEAN, key, Boolean.class);
   }
   
   public Boolean getBooleanValue(String key, Boolean bak) {
      return getValue(EntryType.BOOLEAN, key, Boolean.class, bak);
   }
   
   public List<Boolean> getBooleanValues() {
      return getValues(EntryType.BOOLEAN, Boolean.class);
   }
   
   // Integer
   public Integer getIntegerValue(String key) {
      return getValue(EntryType.INTEGER, key, Integer.class);
   }
   
   public Integer getIntegerValue(String key, Integer def) {
      return getValue(EntryType.INTEGER, key, Integer.class, def);
   }
   
   public List<Integer> getIntegerValues() {
      return getValues(EntryType.INTEGER, Integer.class);
   }
   
   // Long
   public Long getLongValue(String key) {
      return getValue(EntryType.LONG, key, Long.class);
   }
   
   public Long getLongValue(String key, Long def) {
      return getValue(EntryType.LONG, key, Long.class, def);
   }
   
   public List<Long> getLongValues() {
      return getValues(EntryType.LONG, Long.class);
   }

   // String
   public String getStringValue(String key) {
      return getValue(EntryType.STRING, key, String.class);
   }
   
   public String getStringValue(String key, String def) {
      return getValue(EntryType.STRING, key, String.class, def);
   }
   
   public List<String> getStringValues() {
      return getValues(EntryType.STRING, String.class);
   }
   
   // Color
   public Color getColorValue(String key) {
      return getValue(EntryType.COLOR, key, Color.class);
   }
   
   public Color getColorValue(String key, Color def) {
      return getValue(EntryType.COLOR, key, Color.class, def);
   }
   
   public List<Color> getColorValues() {
      return getValues(EntryType.COLOR, Color.class);
   }
   
   public Color getColorFor(Object o) {
      Color ret = null;
      if (o != null) {
         Species s = null;
         boolean mega = false;
         if (o instanceof Species) {
            s = (Species) o;
         }
         if (o instanceof SpeciesPaint) {
            s = ((SpeciesPaint) o).getSpecies();
            mega = ((SpeciesPaint) o).isMega();
         }
         if (s != null) {
            if (mega) {
               ret = getColorValue(s.getMegaName());
               ret = getColorValue(s.getMegaName());
            }
            if (ret == null) {
               ret = getColorValue(s.getName());
            }
         }
         if (ret == null && o instanceof EntryMode) {
            ret = getColorValue(((EntryMode) o).getColorKey());
         }
         if (ret == null) { // Otherwise, everything is by its 'toString()' representation.
            ret = getColorValue(o.toString());
         }
      }
      return ret;
   }
   
   // Species
   public Species getSpeciesValue(String key) {
      return getValue(EntryType.SPECIES, key, Species.class);
   }
   
   public Species getSpeciesValue(String key, Species def) {
      return getValue(EntryType.SPECIES, key, Species.class, def);
   }
   
   public List<Species> getSpeciesValues() {
      return getValues(EntryType.SPECIES, Species.class);
   }
   
   // Stage
   public Stage getStageValue(String key) {
      return getValue(EntryType.STAGE, key, Stage.class);
   }
   
   public Stage getStageValue(String key, Stage def) {
      return getValue(EntryType.STAGE, key, Stage.class, def);
   }
   
   public List<Stage> getStageValues() {
      return getValues(EntryType.STAGE, Stage.class);
   }
   
   // File
   public File getFileValue(String key) {
      return getValue(EntryType.FILE, key, File.class);
   }
   
   public File getFileValue(String key, File def) {
      return getValue(EntryType.FILE, key, File.class, def);
   }
   
   public List<File> getFileValues() {
      return getValues(EntryType.FILE, File.class);
   }
   
   /**
    * Reads the entire file for the given key into a String. If the key doesn't map to a file which
    * can be read this will return an empty String.
    * 
    * @param key
    * @return
    */
   public String readEntireFile(String key) {
      File f = getFileValue(key);
      String ret = null;
      if (f != null && f.exists() && f.canRead()) {
         Path path = Paths.get(f.toURI());
         try {
            ret = new String(Files.readAllBytes(path), Charset.defaultCharset());
         } catch (IOException e) {
            LOG.log(Level.FINE, "Cannot parse file due to an IOException: ", e);
         }
      }
      return ret == null ? "" : ret;
   }
   
   // Team
   public Team getTeamValue(String key) {
      return getValue(EntryType.TEAM, key, Team.class);
   }
   
   public Team getTeamValue(String key, Team def) {
      return getValue(EntryType.TEAM, key, Team.class, def);
   }
   
   public List<Team> getTeamValues() {
      return getValues(EntryType.TEAM, Team.class);
   }
   
   // Font
   public Font getFontValue(String key) {
      return getValue(EntryType.FONT, key, Font.class);
   }
   
   public Font getFontValue(String key, Font def) {
      return getValue(EntryType.FONT, key, Font.class, def);
   }
   
   public List<Font> getFontValues() {
      return getValues(EntryType.FONT, Font.class);
   }
   
   // Resource
   public String getResourceValue(String key) {
      return getValue(EntryType.RESOURCE, key, String.class);
   }
   
   public String getResourceValue(String key, String def) {
      return getValue(EntryType.RESOURCE, key, String.class, def);
   }
   
   public List<String> getResourceValues() {
      return getValues(EntryType.RESOURCE, String.class);
   }
   
   public InputStream getResourceAsInputStream(String key) {
      String path = getResourceValue(key);
      return getResourceInputStream(path);
   }
   
   public InputStream getResourceInputStream(String path) {
      InputStream result = null;
      if (path != null) {
         result = ClassLoader.getSystemResourceAsStream(path);
      }
      return result;
   }
   
   /**
    * Reads the entire resource for the given key into a String. If the key doesn't refer to a path
    * for a resource that can be read, this will return "". Otherwise, it will return the string
    * representation of the byte data for the resource.
    * 
    * @param key
    * @return
    */
   public String readEntireResource(String key) {
      InputStream is = getResourceAsInputStream(key);
      String result = null;
      try {
         result = IOUtils.toString(is);
      } catch (IOException e) {
         LOG.log(Level.FINE, "Cannot parse file due to an IOException: ", e);
      } finally {
         try {
            is.close();
         } catch (IOException e) {
            LOG.log(Level.FINE, "Cannot parse file due to an IOException: ", e);
         }
      }
      return result == null ? "" : result;
   }
   
   public String readEntireFileOrResource(String key) {
      String ret = readEntireFile(key);
      if (ret.isEmpty()) {
         ret = readEntireResource(key);
      }
      return ret;
   }
   
   /*
    * (non-Javadoc)
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + (data == null ? 0 : data.hashCode());
      result = prime * result + (factory == null ? 0 : factory.hashCode());
      result = prime * result + (filePaths == null ? 0 : filePaths.hashCode());
      result = prime * result + (loader == null ? 0 : loader.hashCode());
      return result;
   }
   
   /*
    * (non-Javadoc)
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj) {
      if (obj != null && this.getClass().isInstance(obj)) {
         ConfigManager manager = (ConfigManager) obj;
         boolean equal = true;
         equal |= data == manager.data || data != null && data.equals(manager.data);
         equal |= factory == manager.factory || factory != null && factory.equals(manager.factory);
         equal |= filePaths == manager.filePaths || filePaths != null && filePaths.equals(manager.filePaths);
         equal |= loader == manager.loader || loader != null && loader.equals(manager.loader);
         return equal;
      }
      return false;
   }
   
   /**
    * @return
    */
   public boolean dataChanged() {
      LinkedHashMap<String, List<String>> dataStrings = getDataStrings();
      return !savedDataStrings.equals(dataStrings);
   }
   
}
