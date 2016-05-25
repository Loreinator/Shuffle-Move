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
   private static final int DEFAULT_INT = 100;
   private static final double DEFAULT_DOUBLE = 1.0;
   private static final String ODDS_TYPE = "ODDS";
   private static final String MULT_TYPE = "MULT";
   private static final Pattern EFFECT_ODDS_MULT_PATTERN = Pattern
         .compile("\\s*(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(" + MULT_TYPE
               + "|" + ODDS_TYPE + ")\\s*");
   private EnumMap<Effect, double[][]> oddsMap;
   private EnumMap<Effect, double[]> multMap;
   
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
   
   @Override
   protected boolean shouldUpdate() {
      return true;
   }
   
   public EnumMap<Effect, double[][]> getOddsMap() {
      if (oddsMap == null) {
         oddsMap = new EnumMap<Effect, double[][]>(Effect.class);
      }
      return oddsMap;
   }
   
   public EnumMap<Effect, double[]> getMultMap() {
      if (multMap == null) {
         multMap = new EnumMap<Effect, double[]>(Effect.class);
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
            // Get the odds from the first three values (100 = 100%)
            int[] oddsValues = parseOdds(m.group(1), m.group(2), m.group(3));
            // Get the base multiplier from the fourth value (100 = 1.0x)
            int multiplier = getMultiplier(m.group(4));
            // Get the scaling skill up values
            int[] skillUpValues = parseSkillUpValues(m.group(5), m.group(6), m.group(7), m.group(8));
            // The skill type
            String skillUpType = m.group(9);
            
            // 5 skill levels by 3 odd rates, these are ADDATIVE
            double[][] oddsArray = new double[5][3];
            for (int i = 0; i < 5; i++) {
               int skillValue = 0;
               if (i > 0 && ODDS_TYPE.equals(skillUpType)) {
                  skillValue = skillUpValues[i - 1];
               }
               double[] resultOdds = new double[] { 1.0, 1.0, 1.0 };
               for (int oddIndex = 0; oddIndex < oddsValues.length; oddIndex++) {
                  int oddValue = oddsValues[oddIndex] + skillValue;
                  // Constrain oddValue between 0 and 100.
                  oddValue = Math.max(Math.min(oddValue, 100), 0);
                  // Assign to resultOdds array
                  resultOdds[oddIndex] = oddValue / 100.0;
               }
               oddsArray[i] = resultOdds;
            }
            
            // 5 skill levels of multipliers, these REPLACE the base
            double[] multArray = new double[5];
            for (int i = 0; i < 5; i++) {
               int multValue = multiplier;
               if (i > 0 && MULT_TYPE.equals(skillUpType)) {
                  multValue = skillUpValues[i - 1];
               }
               multArray[i] = multValue / 100.0;
            }
            
            // Set the arrays into the maps
            getMultMap().put(e, multArray);
            getOddsMap().put(e, oddsArray);
            
         } else {
            getOddsMap().remove(e);
            getMultMap().remove(e);
         }
      }
   }
   
   /**
    * Parses the skill values and returns an array of integers.
    * 
    * @param group
    * @param group2
    * @param group3
    * @param group4
    * @return
    */
   private int[] parseSkillUpValues(String... skills) {
      int[] ret = new int[] { DEFAULT_INT, DEFAULT_INT, DEFAULT_INT, DEFAULT_INT };
      try {
         for (int i = 0; i <= 3 && i < skills.length; i++) {
            String skill = skills[i];
            if (skill == null || skill.isEmpty()) {
               continue;
            } else {
               ret[i] = Integer.parseInt(skill.trim());
            }
         }
      } catch (NumberFormatException nfe) {
         nfe.printStackTrace();
      }
      return ret;
   }
   
   private int[] parseOdds(String... odds) {
      int[] ret = new int[] { DEFAULT_INT, DEFAULT_INT, DEFAULT_INT };
      try {
         for (int i = 0; i <= 3 && i < odds.length; i++) {
            String odd = odds[i];
            if (odd == null || odd.isEmpty()) {
               continue;
            } else {
               ret[i] = Integer.parseInt(odd.trim());
            }
         }
      } catch (NumberFormatException nfe) {
         nfe.printStackTrace();
      }
      return ret;
   }
   
   private int getMultiplier(String toParse) {
      int ret = DEFAULT_INT;
      if (toParse != null) {
         try {
            ret = Integer.parseInt(toParse.trim());
         } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
         }
      }
      return ret;
   }
   
   public double getMult(Effect effect, int skillLevel) {
      double ret = DEFAULT_DOUBLE;
      if (getMultMap() != null && getMultMap().containsKey(effect)) {
         double[] values = getMultMap().get(effect);
         if (values.length > 0) {
            int skillIndex = Math.min(Math.max(skillLevel - 1, 0), Math.min(4, values.length));
            ret = values[skillIndex];
         }
      }
      return ret;
   }
   
   public double getOdds(Effect effect, int num, int skillLevel) {
      int index = Math.max(Math.min(num, 5), 3) - 3;
      if (getOddsMap().containsKey(effect)) {
         int skillIndex = Math.min(Math.max(skillLevel - 1, 0), 4);
         return getOddsMap().get(effect)[skillIndex][index];
      } else {
         return DEFAULT_DOUBLE;
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
