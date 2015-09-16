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
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import shuffle.fwk.data.simulation.SimulationResult;

/**
 * @author Andrew Meyers
 *         
 */
public enum GradingMode {
   SCORE("grading.score") {
      @Override
      public Comparator<SimulationResult> getGradingMetric() {
         return getGradingMetric("GOLD,SCORE,COMBOS,DISRUPTIONS,BLOCKS,PROGRESS,MOVE");
      }
      
   },
   TOTAL_BLOCKS("grading.totalblocks") {
      @Override
      public Comparator<SimulationResult> getGradingMetric() {
         return new Comparator<SimulationResult>() {
            @Override
            public int compare(SimulationResult arg0, SimulationResult arg1) {
               return getComparrison(arg0, arg1,
                     Arrays.asList(getBlocksCompare(), getCombosCompare(), getDisruptionsCompare(), getGoldCompare(),
                           getScoreCompare(), getProgressCompare(), getMoveCompare()));
            }
         };
      }
      
   },
   DISRUPTIONS("grading.disruptions") {
      @Override
      public Comparator<SimulationResult> getGradingMetric() {
         return new Comparator<SimulationResult>() {
            @Override
            public int compare(SimulationResult arg0, SimulationResult arg1) {
               return getComparrison(arg0, arg1, Arrays.asList(getDisruptionsCompare(), getBlocksCompare(),
                     getCombosCompare(), getGoldCompare(), getScoreCompare(), getProgressCompare(), getMoveCompare()));
            }
         };
      }
   },
   COMBOS("grading.combos") {
      @Override
      public Comparator<SimulationResult> getGradingMetric() {
         return new Comparator<SimulationResult>() {
            @Override
            public int compare(SimulationResult arg0, SimulationResult arg1) {
               return getComparrison(arg0, arg1, Arrays.asList(getCombosCompare(), getBlocksCompare(), getGoldCompare(),
                     getScoreCompare(), getDisruptionsCompare(), getProgressCompare(), getMoveCompare()));
            }
         };
      }
      
   },
   MIN_DISRUPTIONS("grading.mindisruptions") {
      
      @Override
      public Comparator<SimulationResult> getGradingMetric() {
         return new Comparator<SimulationResult>() {
            @Override
            public int compare(SimulationResult arg0, SimulationResult arg1) {
               return getComparrison(arg0, arg1,
                     Arrays.asList(getDisruptionsCompare().andThen(x -> -x), getBlocksCompare(), getCombosCompare(),
                           getGoldCompare(), getScoreCompare(), getProgressCompare(), getMoveCompare()));
            }
         };
      }
      
   },
   NONE_OR_ALL("grading.noneorall") {
      @Override
      public Comparator<SimulationResult> getGradingMetric() {
         return new Comparator<SimulationResult>() {
            @Override
            public int compare(SimulationResult arg0, SimulationResult arg1) {
               return getComparrison(arg0, arg1, Arrays.asList(getNoCoinCompare(), getBlocksCompare(),
                     getCombosCompare(), getGoldCompare(), getScoreCompare(), getProgressCompare(), getMoveCompare()));
            }
         };
      }
   },
   MEGA_PROGRESS("grading.megaprogress") {
      @Override
      public Comparator<SimulationResult> getGradingMetric() {
         return new Comparator<SimulationResult>() {
            @Override
            public int compare(SimulationResult arg0, SimulationResult arg1) {
               return getComparrison(arg0, arg1,
                     Arrays.asList(getProgressCompare(), getGoldCompare(), getScoreCompare(), getCombosCompare(),
                           getDisruptionsCompare(), getBlocksCompare(), getMoveCompare()));
            }
         };
      }
   },
   COORDINATE("grading.coordinate") {
      @Override
      public Comparator<SimulationResult> getGradingMetric() {
         return new Comparator<SimulationResult>() {
            @Override
            public int compare(SimulationResult arg0, SimulationResult arg1) {
               return getComparrison(arg0, arg1, Arrays.asList(getMoveCompare(), getGoldCompare(), getScoreCompare(),
                     getCombosCompare(), getDisruptionsCompare(), getBlocksCompare(), getProgressCompare()));
            }
         };
      }
   };
   
   protected static int getComparrison(SimulationResult arg0, SimulationResult arg1,
         Collection<BiFunction<SimulationResult, SimulationResult, Integer>> fn) {
      int ret = 0;
      Iterator<BiFunction<SimulationResult, SimulationResult, Integer>> itr = fn.iterator();
      while (ret == 0 && itr.hasNext()) {
         ret = itr.next().apply(arg0, arg1);
      }
      return ret;
   }
   
   protected static BiFunction<SimulationResult, SimulationResult, Integer> getProgressCompare() {
      return (arg0, arg1) -> Double.compare(arg1.getProgress().getAverage(), arg0.getProgress().getAverage());
   }
   
   // Sorts by coordinate
   protected static BiFunction<SimulationResult, SimulationResult, Integer> getMoveCompare() {
      return new BiFunction<SimulationResult, SimulationResult, Integer>() {
         @Override
         public Integer apply(SimulationResult arg0, SimulationResult arg1) {
            List<Integer> move0 = arg0.getMove();
            List<Integer> move1 = arg1.getMove();
            int ret = 0;
            if (move0 != move1) {
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
            }
            return ret;
         }
      };
   }
   
   // Special - sorts by gold priority - avoid it or have lots of it.
   protected static BiFunction<SimulationResult, SimulationResult, Integer> getNoCoinCompare() {
      return new BiFunction<SimulationResult, SimulationResult, Integer>() {
         @Override
         public Integer apply(SimulationResult arg0, SimulationResult arg1) {
            double gold0 = arg0.getNetGold().doubleValue();
            double gold1 = arg1.getNetGold().doubleValue();
            boolean hasGold0 = gold0 > 0;
            boolean hasGold1 = gold1 > 0;
            if (hasGold0 == hasGold1) {
               // then compare descending.
               return Double.compare(gold1, gold0);
            } else {
               return Boolean.compare(hasGold0, hasGold1);
            }
         }
      };
   }
   
   protected static BiFunction<SimulationResult, SimulationResult, Integer> getScoreCompare() {
      return (arg0, arg1) -> Double.compare(arg1.getNetScore().getAverage(), arg0.getNetScore().getAverage());
   }
   
   protected static BiFunction<SimulationResult, SimulationResult, Integer> getGoldCompare() {
      return (arg0, arg1) -> Double.compare(arg1.getNetGold().getAverage(), arg0.getNetGold().getAverage());
   }
   
   protected static BiFunction<SimulationResult, SimulationResult, Integer> getCombosCompare() {
      return (arg0, arg1) -> Double.compare(arg1.getCombosCleared().getAverage(), arg0.getCombosCleared().getAverage());
   }
   
   protected static BiFunction<SimulationResult, SimulationResult, Integer> getBlocksCompare() {
      return (arg0, arg1) -> Double.compare(arg1.getBlocksCleared().getAverage(), arg0.getBlocksCleared().getAverage());
   }
   
   protected static BiFunction<SimulationResult, SimulationResult, Integer> getDisruptionsCompare() {
      return (arg0, arg1) -> Double.compare(arg1.getDisruptionsCleared().getAverage(),
            arg0.getDisruptionsCleared().getAverage());
   }
   
   private static final List<String> DESC_KEYS = Arrays.asList("PROGRESS", "MOVE", "NOCOIN", "SCORE", "GOLD", "COMBOS",
         "BLOCKS", "DISRUPTIONS");
   private static final List<BiFunction<SimulationResult, SimulationResult, Integer>> DESC_COMP = Arrays.asList(
         getProgressCompare(), getMoveCompare(), getNoCoinCompare(), getScoreCompare(), getGoldCompare(),
         getCombosCompare(),
         getBlocksCompare(), getDisruptionsCompare());
   private final String i18nKey;
   private final Pattern DESC_KEY_PATTERN = Pattern.compile("^([+-]?)([A-Z]+)");
   
   private GradingMode(String i18nKey) {
      this.i18nKey = i18nKey;
   }
   
   public String getI18nKey() {
      return i18nKey;
   }
   
   public Comparator<SimulationResult> getGradingMetric(String description) {
      if (description == null || description.isEmpty()) {
         return SCORE.getGradingMetric();
      }
      System.out.printf("%s, %s%n", DESC_KEYS.size(), DESC_COMP.size());
      String[] tokens = description.split("[,\\s]");
      TreeSet<String> used = new TreeSet<String>();
      List<BiFunction<SimulationResult, SimulationResult, Integer>> comparrators = new ArrayList<BiFunction<SimulationResult, SimulationResult, Integer>>();
      for (String token : tokens) {
         Matcher m = DESC_KEY_PATTERN.matcher(token);
         if (m.find()) {
            String metric = m.group(2);
            if (DESC_KEYS.contains(metric)) {
               int index = DESC_KEYS.indexOf(metric);
               BiFunction<SimulationResult, SimulationResult, Integer> func = DESC_COMP.get(index);
               if (m.group(1).equals("-")) {
                  // Reverses the ordering
                  func = func.andThen((value) -> value * (-1));
               }
               comparrators.add(func);
            }
         }
      }
      for (int i = 0; i < DESC_KEYS.size(); i++) {
         String key = DESC_KEYS.get(i);
         if (!used.contains(key)) {
            comparrators.add(DESC_COMP.get(i));
         }
      }
      return new Comparator<SimulationResult>() {
         @Override
         public int compare(SimulationResult arg0, SimulationResult arg1) {
            return getComparrison(arg0, arg1, comparrators);
         }
      };
   }
   
   public abstract Comparator<SimulationResult> getGradingMetric();
}
