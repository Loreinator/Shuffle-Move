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

package shuffle.fwk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import shuffle.fwk.data.simulation.SimulationResult;
import shuffle.fwk.i18n.I18nUser;

/**
 * @author Andrew Meyers
 *         
 */
public class GradingMode implements I18nUser {
   
   private static final List<String> DESC_KEYS = Arrays.asList("GOLD", "SCORE", "COMBOS", "DISRUPTIONS", "BLOCKS",
         "PROGRESS", "MOVE", "NOCOIN", "GOLD_THRESHOLD", "COMBOS_THRESHOLD");
   private static final List<Function<String, BiFunction<SimulationResult, SimulationResult, Integer>>> DESC_COMP = Arrays
         .asList((a) -> getGoldCompare(a), (a) -> getScoreCompare(a), (a) -> getCombosCompare(a),
               (a) -> getDisruptionsCompare(a), (a) -> getBlocksCompare(a), (a) -> getProgressCompare(a),
               (a) -> getMoveCompare(a), (a) -> getNoCoinCompare(a), (a) -> getThresholdGold(a),
               (a) -> getThresholdCombos(a));
   private static final String DEFAULT_DESC = "GOLD,SCORE,COMBOS,DISRUPTIONS,BLOCKS,PROGRESS,MOVE";
   private static final Pattern DESC_KEY_PATTERN = Pattern.compile("^([+-]?)([\\d]*)([A-Z_]+)$");
   
   public static int getThreshold(String args, int def) {
      final int threshold;
      if (args != null && !args.isEmpty() && args.matches("^[\\d]+$")) {
         threshold = Integer.parseInt(args);
      } else {
         threshold = def;
      }
      return threshold;
   }
   
   protected static final BiFunction<SimulationResult, SimulationResult, Integer> getConditionalCompare(
         String thresholdArg, Function<SimulationResult, Double> mapper) {
      final int threshold = getThreshold(thresholdArg, 0);
      return (arg0, arg1) -> {
         Double val0 = mapper.apply(arg0);
         Double val1 = mapper.apply(arg1);
         if (val0 >= threshold && val1 >= threshold) {
            return Double.compare(val1, val0);
         } else if (val0 >= threshold && val1 < threshold) {
            return -1;
         } else if (val0 < threshold && val1 >= threshold) {
            return 1;
         } else {
            return 0;
         }
      };
   }
   
   protected static BiFunction<SimulationResult, SimulationResult, Integer> getScoreCompare(String args) {
      return getConditionalCompare(args, (r) -> r.getNetScore().getAverage());
   }
   
   protected static BiFunction<SimulationResult, SimulationResult, Integer> getGoldCompare(String args) {
      return getConditionalCompare(args, (r) -> r.getNetGold().getAverage());
   }
   
   protected static BiFunction<SimulationResult, SimulationResult, Integer> getCombosCompare(String args) {
      return getConditionalCompare(args, (r) -> r.getCombosCleared().getAverage());
   }
   
   protected static BiFunction<SimulationResult, SimulationResult, Integer> getBlocksCompare(String args) {
      return getConditionalCompare(args, (r) -> r.getBlocksCleared().getAverage());
   }
   
   protected static BiFunction<SimulationResult, SimulationResult, Integer> getDisruptionsCompare(String args) {
      return getConditionalCompare(args, (r) -> r.getDisruptionsCleared().getAverage());
   }
   
   protected static BiFunction<SimulationResult, SimulationResult, Integer> getProgressCompare(String args) {
      return getConditionalCompare(args, (r) -> r.getProgress().getAverage());
   }
   
   // Sorts by coordinate
   protected static BiFunction<SimulationResult, SimulationResult, Integer> getMoveCompare(String args) {
      return (arg0, arg1) -> {
         List<Integer> move0 = arg0.getMove();
         List<Integer> move1 = arg1.getMove();
         if (move0 == move1) {
            return 0;
         } else {
            int ret = 0;
            if (move0 == null) {
               ret = 1;
            } else if (move1 == null) {
               ret = -1;
            } else {
               int min = Math.min(move0.size(), move1.size());
               for (int i = 0; ret == 0 && i < min; i++) {
                  ret = Integer.compare(move0.get(i), move1.get(i));
               }
            }
            return ret;
         }
      };
   }
   
   // Special - sorts by gold priority - avoid it or have lots of it.
   protected static BiFunction<SimulationResult, SimulationResult, Integer> getNoCoinCompare(String args) {
      return (arg0, arg1) -> {
         double gold0 = arg0.getNetGold().doubleValue();
         double gold1 = arg1.getNetGold().doubleValue();
         if (gold0 > 0 == gold1 > 0) {
            // then compare descending.
            return Double.compare(gold1, gold0);
         } else {
            return Boolean.compare(gold0 > 0, gold1 > 0);
         }
      };
   }
   
   protected static BiFunction<SimulationResult, SimulationResult, Integer> getThresholdGold(String args) {
      final int threshold = getThreshold(args, 0);
      return (arg0, arg1) -> {
         double gold0 = arg0.getNetGold().doubleValue();
         double gold1 = arg1.getNetGold().doubleValue();
         if (gold0 >= threshold && gold1 < threshold) {
            return -1;
         } else if (gold0 < threshold && gold1 >= threshold) {
            return 1;
         } else {
            return 0;
         }
      };
   }
   
   protected static BiFunction<SimulationResult, SimulationResult, Integer> getThresholdCombos(String args) {
      final int threshold = getThreshold(args, 0);
      return (arg0, arg1) -> {
         int cmb0 = (int) arg0.getCombosCleared().getMinimum();
         int cmb1 = (int) arg1.getCombosCleared().getMinimum();
         if (cmb0 >= threshold && cmb1 < threshold) {
            return -1;
         } else if (cmb0 < threshold && cmb1 >= threshold) {
            return 1;
         } else {
            return 0;
         }
      };
   }
   
   public static Comparator<SimulationResult> getGradingMetric(String description) {
      if (description == null) {
         description = "";
      }
      description = description + "," + DEFAULT_DESC;
      String[] tokens = description.split("[,\\s]");
      List<BiFunction<SimulationResult, SimulationResult, Integer>> comparrators = new ArrayList<BiFunction<SimulationResult, SimulationResult, Integer>>();
      for (String token : tokens) {
         Matcher m = DESC_KEY_PATTERN.matcher(token);
         if (m.find()) {
            String metric = m.group(3);
            if (DESC_KEYS.contains(metric)) {
               int index = DESC_KEYS.indexOf(metric);
               String comparatorArg = m.group(2);
               BiFunction<SimulationResult, SimulationResult, Integer> func = DESC_COMP.get(index).apply(comparatorArg);
               String argSign = m.group(1);
               if (argSign.equals("-")) {
                  // Reverses the ordering
                  func = func.andThen((value) -> value * (-1));
               }
               comparrators.add(func);
            }
         }
      }
      return new Comparator<SimulationResult>() {
         @Override
         public int compare(SimulationResult arg0, SimulationResult arg1) {
            int ret = 0;
            Iterator<BiFunction<SimulationResult, SimulationResult, Integer>> itr = comparrators.iterator();
            while (ret == 0 && itr.hasNext()) {
               ret = itr.next().apply(arg0, arg1);
            }
            return ret;
         }
      };
   }
   
   private final Comparator<SimulationResult> metric;
   private final String key;
   private final String desc;
   private final boolean custom;
   
   public GradingMode(String name, String description, boolean isCustom) {
      desc = description;
      metric = getGradingMetric(desc);
      key = name;
      custom = isCustom;
   }
   
   public boolean isCustom() {
      return custom;
   }
   
   public String getKey() {
      return key;
   }
   
   public String geti18nString() {
      if (isCustom()) {
         return getKey();
      } else {
         return getString(getKey());
      }
   }
   
   public Comparator<SimulationResult> getGradingMetric() {
      return metric;
   }
   
   public String getDescription() {
      return desc;
   }
}
