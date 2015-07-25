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

package shuffle.fwk.config.writer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import shuffle.fwk.config.EntryType;
import shuffle.fwk.i18n.I18nUser;

/**
 * @author Andrew Meyers Writes to the specified file with the specified mapping of key to value
 *         pairs.
 */
public class PreferencesWriter implements I18nUser {
   private static final Logger LOG = Logger.getLogger(PreferencesWriter.class.getName());
   private static final String SAVE_PATTERN = "%s %s%n";
   
   private List<File> files = new ArrayList<File>();
   
   public PreferencesWriter(Collection<String> fileNames) {
      this(fileNames.toArray(new String[0]));
   }
   
   public PreferencesWriter(String... fileNames) {
      for (String fileName : fileNames) {
         try {
            File file = (File) EntryType.FILE.parseValue(null, fileName);
            files.add(file);
         } catch (Exception e) {
            LOG.warning("Could not create preferences file " + fileName + " because: " + e.getLocalizedMessage());
         }
      }
   }
   
   public void writePreferences(Map<String, List<String>> data) {
      boolean dataEmpty = true;
      for (List<String> dataLines : data.values()) {
         dataEmpty &= dataLines.isEmpty();
      }
      for (File file : files) {
         try {
            if (dataEmpty) {
               if (file.exists()) {
                  file.delete();
               }
               continue;
            } else {
               if (!file.exists()) {
                  try {
                     file.createNewFile();
                  } catch (IOException ioe) {
                     LOG.log(Level.WARNING, ioe.getLocalizedMessage(), ioe);
                     continue;
                  }
               }
            }
            // data is not empty, and file exists.
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
               for (String key : data.keySet()) {
                  for (String value : data.get(key)) {
                     bw.write(String.format(SAVE_PATTERN, String.valueOf(key), String.valueOf(value)));
                  }
               }
            } catch (IOException e) {
               LOG.log(Level.WARNING, e.getLocalizedMessage() + ": " + file.getAbsolutePath(), e);
            }
         } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getLocalizedMessage(), e);
         }
      }
      
   }
}
