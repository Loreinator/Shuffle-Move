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

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import shuffle.fwk.config.ConfigFactory;
import shuffle.fwk.config.ConfigManager;
import shuffle.fwk.data.Effect;
import shuffle.fwk.data.Species;

/**
 * @author Andrew Meyers
 *         
 */
public class EffectManager extends ConfigManager {
   
   private static final String FORMAT_MEGA_SPEEDUP_CAP = "MEGA_SPEEDUPS_%s";
   private static final String FORMAT_MEGA_THRESHOLD = "MEGA_THRESHOLD_%s";
   private static final double[] DEFAULT_ODDS = new double[] { 1.0, 1.0, 1.0, 1.0 };
   private static final double DEFAULT_MULT = 1.0;
   private static final Pattern EFFECT_ODDS_MULT_PATTERN = Pattern
         .compile("(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+([0-9]{1,13}(?:\\.[0-9]*)?)?");
   private EnumMap<Effect, double[]> oddsMap;
   private EnumMap<Effect, Double> multMap;
   
   /**
    * Creates an EffectManager which manages configurable settings for Effects.
    * 
    * @param loadPaths
    * @param writePaths
    */
   public EffectManager(List<String> loadPaths, List<String> writePaths, ConfigFactory factory) {
      super(loadPaths, writePaths, factory);
   }
   
   public EffectManager(ConfigManager manager) {
      super(manager);
   }
   
   public EnumMap<Effect, double[]> getOddsMap() {
      if (oddsMap == null) {
         oddsMap = new EnumMap<Effect, double[]>(Effect.class);
      }
      return oddsMap;
   }
   
   public EnumMap<Effect, Double> getMultMap() {
      if (multMap == null) {
         multMap = new EnumMap<Effect, Double>(Effect.class);
      }
      return multMap;
   }
   
   @Override
   protected <T extends ConfigManager> void onCopyFrom(T manager) {
      synchronized (this) {
         reloadMaps();
      }
   }
   
   @Override
   public boolean loadFromConfig() {
      boolean changed = super.loadFromConfig();
      if (changed) {
         synchronized (this) {
            reloadMaps();
         }
      }
      return changed;
   }
   
   /**
    * 
    */
   private void reloadMaps() {
      getOddsMap().clear();
      getMultMap().clear();
      for (Effect e : Effect.values()) {
         String dataString = getStringValue(e.toString());
         if (dataString == null || dataString.isEmpty()) {
            getOddsMap().remove(e);
            getMultMap().remove(e);
            continue;
         }
         Matcher m = EFFECT_ODDS_MULT_PATTERN.matcher(dataString);
         if (m.find()) {
            double[] oddsValues = parseOdds(m.group(1), m.group(2), m.group(3), m.group(4));
            getOddsMap().put(e, oddsValues);
            double multiplier = getMultiplier(m.group(5));
            if (multiplier == 1.0) {
               getMultMap().remove(e);
            } else {
               getMultMap().put(e, multiplier);
            }
         } else {
            getOddsMap().remove(e);
            getMultMap().remove(e);
         }
      }
   }
   
   private double getMultiplier(String toParse) {
      double ret = 1.0;
      if (toParse != null) {
         try {
            ret = Double.parseDouble(toParse);
         } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
         }
      }
      return ret;
   }
   
   private double[] parseOdds(String... odds) {
      double[] ret = Arrays.copyOf(DEFAULT_ODDS, DEFAULT_ODDS.length);
      try {
         for (int i = 0; i <= 3 && i < odds.length; i++) {
            String odd = odds[i];
            if (odd == null || odd.isEmpty()) {
               continue;
            } else {
               ret[i] = Integer.parseInt(odd.trim()) / 100.0;
            }
         }
      } catch (NumberFormatException nfe) {
         nfe.printStackTrace();
      }
      return ret;
   }
   
   public double getMult(Effect effect) {
      if (getMultMap().containsKey(effect)) {
         return getMultMap().get(effect);
      } else {
         return DEFAULT_MULT;
      }
   }
   
   public double getOdds(Effect effect, int num) {
      int index = Math.max(Math.min(num, 6), 3) - 3;
      if (getOddsMap().containsKey(effect)) {
         return getOddsMap().get(effect)[index];
      } else {
         return DEFAULT_ODDS[index];
      }
   }
   
   public int getMegaSpeedupCap(Species species) {
      return getIntegerValue(getMegaSpeedupKey(species), 0);
   }
   
   public int getMegaThreshold(Species species) {
      return getIntegerValue(getMegaThresholdKey(species), Integer.MAX_VALUE);
   }
   
   /**
    * @param species
    *           The Species to obtain the mega speedups key for.
    * @return The mega speedups key
    */
   public String getMegaSpeedupKey(Species species) {
      String megaName = species.getMegaName();
      if (megaName == null) {
         return null;
      } else {
         return String.format(FORMAT_MEGA_SPEEDUP_CAP, megaName);
      }
   }
   
   /**
    * @param species
    *           The Species to obtain the mega threshold key for.
    * @return The mega threshold key
    */
   public String getMegaThresholdKey(Species species) {
      String megaName = species.getMegaName();
      if (megaName == null) {
         return null;
      } else {
         return String.format(FORMAT_MEGA_THRESHOLD, megaName);
      }
   }
}
