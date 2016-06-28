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

package shuffle.fwk.config.loader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import shuffle.fwk.config.DataLoader;
import shuffle.fwk.config.Interpreter;
import shuffle.fwk.config.interpreter.BoardConfigInterpreter;
import shuffle.fwk.data.Species;

/**
 * @author Andrew Meyers
 *
 */
public class BoardConfigLoader extends DataLoader<String> {
   public static final String STAGE = "STAGE";
   public static final String FROW_KEY_FORMAT = "FROW_%d";
   public static final String CROW_KEY_FORMAT = "CROW_%d";
   public static final String ROW_KEY_FORMAT = "ROW_%d";
   public static final String KEY_MEGA_PROGRESS = "MEGA_PROGRESS";
   public static final String KEY_STATUS = "STATUS";
   public static final String KEY_STATUS_DURATION = "STATUS_DURATION";
   private Interpreter<String> inter = null;
   
   public BoardConfigLoader(List<String> resources, List<String> files) {
      super(resources, files);
   }
   
   @Override
   public Interpreter<String> getInterpreter() {
      if (inter == null) {
         inter = new BoardConfigInterpreter();
      }
      return inter;
   }
   
   /**
    * Returns a list of all configured stage values.
    * 
    * @return
    */
   public List<String> getConfiguredStage() {
      List<String> ret = new ArrayList<String>();
      if (getMap().containsKey(STAGE)) {
         ret.addAll(getValues(STAGE));
      }
      return Collections.unmodifiableList(ret);
   }
   
   public List<String> getConfiguredMegaProgress() {
      List<String> ret = new ArrayList<String>();
      if (getMap().containsKey(KEY_MEGA_PROGRESS)) {
         ret.addAll(getValues(KEY_MEGA_PROGRESS));
      }
      return ret;
   }
   
   public List<String> getConfiguredStatus() {
      List<String> ret = new ArrayList<String>();
      if (getMap().containsKey(KEY_STATUS)) {
         ret.addAll(getValues(KEY_STATUS));
      }
      return ret;
   }
   
   public List<String> getConfiguredStatusDuration() {
      List<String> ret = new ArrayList<String>();
      if (getMap().containsKey(KEY_STATUS_DURATION)) {
         ret.addAll(getValues(KEY_STATUS_DURATION));
      }
      return ret;
   }
   
   /**
    * Returns a list of lists of strings to denote the state of all rows.
    */
   public List<List<String>> getConfiguredRows() {
      List<List<String>> ret = new ArrayList<List<String>>();
      for (int i = 1; i <= 6; i++) {
         ret.add(getRow(i));
      }
      return Collections.unmodifiableList(ret);
   }
   
   /**
    * Returns the row n as specified by the configuration.
    */
   private List<String> getRow(int n) {
      List<String> ret = new ArrayList<String>(6);
      for (int i = 0; i < 6; i++) {
         ret.add(Species.AIR.getName());
      }
      if (n > 6) {
         n = 6;
      }
      if (n < 1) {
         n = 1;
      }
      String key = getRowKey(n);
      if (containsKey(key)) {
         for (String row : getValues(key)) {
            String[] rowTokens = row.split("[,|]");
            if (rowTokens.length == 6) {
               ret = Arrays.asList(rowTokens);
               break;
            }
         }
      }
      return Collections.unmodifiableList(ret);
   }
   
   public List<List<Boolean>> getConfiguredFrozenRows() {
      List<List<Boolean>> ret = new ArrayList<List<Boolean>>();
      for (int i = 1; i < 7; i++) {
         ret.add(getFrozenRow(i));
      }
      return Collections.unmodifiableList(ret);
   }
   
   private List<Boolean> getFrozenRow(int n) {
      List<Boolean> ret = Arrays.asList(false, false, false, false, false, false);
      if (n > 6) {
         n = 6;
      }
      if (n < 1) {
         n = 1;
      }
      String key = getFrozenRowKey(n);
      if (containsKey(key)) {
         for (String row : getValues(key)) {
            String[] rowTokens = row.split("[,|]");
            if (rowTokens.length == 6) {
               for (int i = 0; i < 6; i++) {
                  ret.set(i, Boolean.parseBoolean(rowTokens[i]));
               }
            }
         }
      }
      return Collections.unmodifiableList(ret);
   }
   
   public List<List<Boolean>> getConfiguredCloudedRows() {
      List<List<Boolean>> ret = new ArrayList<List<Boolean>>();
      for (int i = 1; i < 7; i++) {
         ret.add(getCloudedRow(i));
      }
      return Collections.unmodifiableList(ret);
   }
   
   private List<Boolean> getCloudedRow(int n) {
      List<Boolean> ret = Arrays.asList(false, false, false, false, false, false);
      if (n > 6) {
         n = 6;
      }
      if (n < 1) {
         n = 1;
      }
      String key = getCloudedRowKey(n);
      if (containsKey(key)) {
         for (String row : getValues(key)) {
            String[] rowTokens = row.split("[,|]");
            if (rowTokens.length == 6) {
               for (int i = 0; i < 6; i++) {
                  ret.set(i, Boolean.parseBoolean(rowTokens[i]));
               }
            }
         }
      }
      return Collections.unmodifiableList(ret);
   }
   
   public static String getRowKey(int n) {
      return String.format(ROW_KEY_FORMAT, n);
   }
   
   public static String getFrozenRowKey(int n) {
      return String.format(FROW_KEY_FORMAT, n);
   }
   
   public static String getCloudedRowKey(int n) {
      return String.format(CROW_KEY_FORMAT, n);
   }
}
