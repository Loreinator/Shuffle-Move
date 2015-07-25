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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Andrew Meyers
 *
 */
public class ConfigParser<Y> {
   private static final Logger LOG = Logger.getLogger(ConfigParser.class.getName());
   
   public static <Y> LinkedHashMap<String, List<Y>> parseFile(String filePathName, Interpreter<Y> inter) {
      return parse(new File(filePathName), inter);
   }
   
   public static <Y> LinkedHashMap<String, List<Y>> parseResource(String resourceName, Interpreter<Y> inter) {
      LinkedHashMap<String, List<Y>> ret = new LinkedHashMap<String, List<Y>>();
      if (resourceName != null) {
         InputStream is = ClassLoader.getSystemResourceAsStream(resourceName);
         if (is != null) {
            ret = parse(is, inter);
            try {
               is.close();
            } catch (IOException ioe) {
               LOG.log(Level.WARNING, "Cannot close resource due to a IOException: ", ioe);
            }
         }
      }
      return ret;
   }
   
   public static <Y> LinkedHashMap<String, List<Y>> parse(File f, Interpreter<Y> inter) {
      LinkedHashMap<String, List<Y>> ret = new LinkedHashMap<String, List<Y>>();
      if (init(f, inter)) {
         InputStream is;
         try {
            is = new FileInputStream(f);
            ret = parse(is, inter);
         } catch (FileNotFoundException e) {
            LOG.log(Level.WARNING, "Cannot parse file due to a IOException: ", e);
         }
      }
      return ret;
   }
   
   public static <Y> LinkedHashMap<String, List<Y>> parse(InputStream is, Interpreter<Y> inter) {
      LinkedHashMap<String, List<Y>> ret = new LinkedHashMap<String, List<Y>>();
      try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
         String line;
         while ((line = br.readLine()) != null) {
            Map<String, List<Y>> result = inter.interpret(line);
            if (result != null) {
               DataLoader.mergeSafely(ret, result);
            }
         }
      } catch (IOException e) {
         LOG.log(Level.WARNING, "Cannot parse file due to a IOException: ", e);
      }
      return ret;
   }
   
   private static <Y> boolean init(File f, Interpreter<Y> inter) {
      boolean valid = true;
      if (f == null || !f.exists()) {
         LOG.warning("No such file available: " + f);
         valid = false;
      }
      if (inter == null) {
         LOG.warning("Interpreter is null, cannot parse data.");
         valid = false;
      }
      return valid;
   }
   
}
