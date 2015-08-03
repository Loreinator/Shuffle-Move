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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Andrew Meyers
 *
 */
public class ConfigLoader {
   private static final Logger LOG = Logger.getLogger(ConfigLoader.class.getName());
   static {
      LOG.setLevel(Level.FINE);
   }
   private static final Pattern ENTRY_PATTERN = Pattern.compile("^\\s*(\\S+)\\s+(\\S+)\\s+(\\S+.*)(?=\\s*$)(?<=\\S)");
   private final List<String> resources;
   private final List<String> files;
   private boolean shouldReload = true;
   private LinkedHashMap<EntryType, LinkedHashMap<String, ConfigEntry>> myMap = new LinkedHashMap<EntryType, LinkedHashMap<String, ConfigEntry>>();
   
   /**
    * Default constructor. Include load locations to ensure proper functionality. Otherwise, this is
    * a useless object.
    * 
    * @param loadLocations
    */
   public ConfigLoader(List<String> resourceLocations, List<String> fileLocations) {
      if (resourceLocations == null) {
         resourceLocations = Collections.emptyList();
      }
      if (fileLocations == null) {
         fileLocations = Collections.emptyList();
      }
      if (resourceLocations.isEmpty()) {
         resources = Collections.emptyList();
      } else {
         resources = new ArrayList<String>();
         for (String resource : resourceLocations) {
            URL url = ClassLoader.getSystemResource(resource);
            if (url != null) {
               resources.add(resource);
            }
         }
      }
      
      if (fileLocations.isEmpty()) {
         files = Collections.emptyList();
      } else {
         files = new ArrayList<String>();
         for (String file : fileLocations) {
            files.add(file);
         }
      }
   }
   
   public ConfigLoader getNewCopy() {
      ConfigLoader ret = new ConfigLoader(resources, files);
      for (EntryType type : myMap.keySet()) {
         LinkedHashMap<String, ConfigEntry> entryMap = new LinkedHashMap<String, ConfigEntry>();
         if (!myMap.containsKey(type)) {
            ret.myMap.put(type, entryMap);
            continue;
         }
         LinkedHashMap<String, ConfigEntry> myEntryMap = myMap.get(type);
         for (String key : myEntryMap.keySet()) {
            entryMap.put(key, myEntryMap.get(key));
         }
         ret.myMap.put(type, entryMap);
      }
      ret.setForceReload(false);
      return ret;
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
   public boolean containsKey(EntryType type, String key) {
      manageReload();
      return key != null && type != null && myMap.containsKey(type) && myMap.get(type).containsKey(key);
   }
   
   public LinkedHashMap<String, ConfigEntry> getMappings(EntryType type) {
      manageReload();
      LinkedHashMap<String, ConfigEntry> mappings = myMap.get(type);
      if (mappings == null) {
         mappings = new LinkedHashMap<String, ConfigEntry>();
      }
      LinkedHashMap<String, ConfigEntry> ret = new LinkedHashMap<String, ConfigEntry>(mappings);
      return ret;
   }
   
   public ConfigEntry getEntryFor(EntryType type, String key) {
      LinkedHashMap<String, ConfigEntry> typeMappings = myMap.get(type);
      return typeMappings != null ? typeMappings.get(key) : null;
   }
   
   private void manageReload() {
      if (shouldReload) {
         myMap.clear();
         for (EntryType type : EntryType.values()) {
            myMap.put(type, new LinkedHashMap<String, ConfigEntry>());
         }
         for (String file : files) {
            try {
               InputStream is = new FileInputStream(file);
               addMissingTo(myMap, parse(is));
            } catch (FileNotFoundException e) {
               LOG.log(Level.FINE, e.getMessage());
            }
         }
         for (String resource : resources) {
            InputStream is = ClassLoader.getSystemResourceAsStream(resource);
            addMissingTo(myMap, parse(is));
         }
         shouldReload = false;
      }
   }
   
   /**
    * Maintains order of all keys, but overwrites all data keys with values from resources.
    */
   public void updateFromResource() {
      for (String resource : resources) {
         InputStream is = ClassLoader.getSystemResourceAsStream(resource);
         LinkedHashMap<EntryType, LinkedHashMap<String, ConfigEntry>> parseResult = parse(is);
         for (EntryType type : myMap.keySet()) {
            LinkedHashMap<String, ConfigEntry> dataMappings = myMap.get(type);
            LinkedHashMap<String, ConfigEntry> resourceMappings = parseResult.get(type);
            LinkedHashMap<String, ConfigEntry> tempMappings = new LinkedHashMap<String, ConfigEntry>();
            LinkedHashSet<String> keys = new LinkedHashSet<String>();
            keys.addAll(dataMappings.keySet());
            keys.addAll(resourceMappings.keySet());
            for (String key : keys) {
               if (resourceMappings.containsKey(key)) {
                  tempMappings.put(key, resourceMappings.get(key));
               } else {
                  tempMappings.put(key, dataMappings.get(key));
               }
            }
            myMap.put(type, tempMappings);
         }
      }
   }
   
   /**
    * Merges entries from the secondary into the primary if there is no such key in the primary.
    * 
    * @param myMap2
    * @param parse
    */
   public static void addMissingTo(LinkedHashMap<EntryType, LinkedHashMap<String, ConfigEntry>> primary,
         LinkedHashMap<EntryType, LinkedHashMap<String, ConfigEntry>> secondary) {
      for (EntryType type : secondary.keySet()) {
         LinkedHashMap<String, ConfigEntry> primaryMappings = primary.get(type);
         LinkedHashMap<String, ConfigEntry> secondaryMappings = secondary.get(type);
         for (String key : secondaryMappings.keySet()) {
            if (!primaryMappings.containsKey(key)) {
               primaryMappings.put(key, secondaryMappings.get(key));
            }
         }
      }
   }
   
   public static LinkedHashMap<EntryType, LinkedHashMap<String, ConfigEntry>> parse(InputStream is) {
      LinkedHashMap<EntryType, LinkedHashMap<String, ConfigEntry>> ret = new LinkedHashMap<EntryType, LinkedHashMap<String, ConfigEntry>>();
      for (EntryType type : EntryType.values()) {
         ret.put(type, new LinkedHashMap<String, ConfigEntry>());
      }
      try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
         String line;
         while ((line = br.readLine()) != null) {
            Matcher m = ENTRY_PATTERN.matcher(line);
            ConfigEntry entry = null;
            if (m.find()) {
               String type = m.group(1);
               String key = m.group(2);
               String value = m.group(3);
               try {
                  entry = ConfigEntry.getEntryFor(type, key, value);
               } catch (Exception e) {
                  LOG.log(Level.FINE,
                        String.format("Cannot parse config entry, key: %s type: %s value: %s ", key, type, value), e);
               }
               if (entry != null) {
                  ret.get(entry.getEntryType()).put(key, entry);
               }
            }
         }
      } catch (IOException e) {
         LOG.log(Level.WARNING, "Cannot parse file due to a IOException: ", e);
      }
      return ret;
   }
}
