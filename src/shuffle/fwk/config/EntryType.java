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
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import shuffle.fwk.GradingMode;
import shuffle.fwk.data.Effect;
import shuffle.fwk.data.PkmType;
import shuffle.fwk.data.Species;
import shuffle.fwk.data.Stage;
import shuffle.fwk.data.Team;
import shuffle.fwk.data.TeamImpl;

/**
 * @author Andrew Meyers
 *
 */
public enum EntryType {
   BOOLEAN {
      
      @Override
      public Boolean parseValue(String key, String value) throws Exception {
         return Boolean.valueOf(value);
      }
      
      @Override
      public String getDataString(Object obj) throws Exception {
         Boolean value = (Boolean) obj;
         return value.toString();
      }
      
      @Override
      public Class<Boolean> getDataClass() {
         return Boolean.class;
      }
      
   },
   INTEGER {
      
      @Override
      public Integer parseValue(String key, String value) throws Exception {
         return Integer.parseInt(value);
      }
      
      @Override
      public Class<? extends Object> getDataClass() {
         return Integer.class;
      }
      
      @Override
      public String getDataString(Object obj) throws Exception {
         Integer value = (Integer) obj;
         String ret = String.valueOf(value);
         return ret;
      }
      
   },
   LONG {
      
      @Override
      public Long parseValue(String key, String value) throws Exception {
         return Long.parseLong(value);
      }
      
      @Override
      public Class<? extends Object> getDataClass() {
         return Long.class;
      }
      
      @Override
      public String getDataString(Object obj) throws Exception {
         Long value = (Long) obj;
         String ret = Long.toString(value);
         return ret;
      }
      
   },
   STRING {
      
      @Override
      public String parseValue(String key, String value) throws Exception {
         return value;
      }
      
      @Override
      public Class<? extends Object> getDataClass() {
         return String.class;
      }
      
      @Override
      public String getDataString(Object obj) throws Exception {
         String value = (String) obj;
         value = value.trim();
         return value;
      }
      
   },
   COLOR {
      
      @Override
      public Color parseValue(String key, String value) throws Exception {
         Matcher m = COLOR_PATTERN.matcher(value);
         if (!m.find()) {
            throw new IllegalArgumentException("Value is not a color." + value);
         }
         int r = Integer.parseInt(m.group(1));
         int g = Integer.parseInt(m.group(2));
         int b = Integer.parseInt(m.group(3));
         int a = 255;
         if (m.group(4) != null) {
            a = Integer.parseInt(m.group(4));
         }
         if (r < 0 || r > 255) {
            throw new NumberFormatException("Red value invalid: " + r);
         } else if (g < 0 || g > 255) {
            throw new NumberFormatException("Green value invalid: " + g);
         } else if (b < 0 || b > 255) {
            throw new NumberFormatException("Blue value invalid: " + b);
         } else if (a < 0 || a > 255) {
            throw new NumberFormatException("Alpha value invalid: " + a);
         }
         // Guaranteed that all four values are valid integers between 0 and 255 inclusive.
         return new Color(r, g, b, a);
      }
      
      @Override
      public Class<? extends Object> getDataClass() {
         return Color.class;
      }
      
      @Override
      public String getDataString(Object obj) throws Exception {
         Color value = (Color) obj;
         int red = value.getRed();
         int green = value.getGreen();
         int blue = value.getBlue();
         int alpha = value.getAlpha();
         String ret = String.format("%d %d %d", red, green, blue, alpha);
         return ret;
      }
      
   },
   SPECIES {
      
      @Override
      public Species parseValue(String key, String value) throws Exception {
         Matcher m = SPECIES_PATTERN.matcher(value);
         if (!m.find()) {
            throw new IllegalArgumentException("Value is not a species." + value);
         }
         String name = key;
         int number = Integer.parseInt(m.group(1));
         int attack = Integer.parseInt(m.group(2));
         PkmType type = PkmType.getType(m.group(3));
         List<Effect> effects = Effect.getEffects(m.group(4));
         String megaName = m.group(5);
         Effect megaEffect = Effect.getEffect(m.group(6));
         PkmType megaType = PkmType.getType(m.group(7));
         Species ret = new Species(name, number, attack, type, effects, megaName, megaEffect, megaType);
         return ret;
      }
      
      @Override
      public Class<? extends Object> getDataClass() {
         return Species.class;
      }
      
      @Override
      public String getDataString(Object obj) throws Exception {
         Species species = (Species) obj;
         int number = species.getNumber();
         int attack = species.getBaseAttack();
         PkmType type = species.getType();
         String effectsString = species.getEffectsString();
         String ret = String.format("%d %d %s %s", number, attack, type, effectsString);
         
         String megaName = species.getMegaName();
         Effect megaEffect = species.getMegaEffect();
         PkmType megaType = species.getMegaType();
         if (megaName != null) {
            ret = String.format("%s %s %s", ret, String.valueOf(megaName), String.valueOf(megaEffect));
            if (megaType != null && !megaType.equals(type)) {
               ret = String.format("%s %s", ret, String.valueOf(megaType));
            }
         }
         return ret;
      }
      
   },
   STAGE {
      
      @Override
      public Stage parseValue(String key, String value) throws Exception {
         String stageName = key;
         Matcher m = STAGE_PATTERN.matcher(value);
         String targetName = key;
         PkmType targetType = PkmType.getType(key);
         int moves = Stage.DEFAULT_MOVES;
         int health = Stage.DEFAULT_HEALTH;
         String escalationData = null;
         if (m.find()) {
            targetName = m.group(1);
            targetType = PkmType.getType(m.group(2));
            if (m.group(3) != null) {
               moves = Integer.parseInt(m.group(3));
               if (m.group(4) != null) {
                  health = Integer.parseInt(m.group(4));
                  escalationData = m.group(5);
               }
            }
         }
         return new Stage(stageName, targetName, targetType, moves, health, escalationData);
      }
      
      @Override
      public String getDataString(Object obj) throws Exception {
         Stage stage = (Stage) obj;
         // Stage name is the key
         String stageName = stage.getName();
         String targetName = stage.getTarget();
         String targetType = stage.getType().toString();
         String stageMoves = Integer.toString(stage.getMoves());
         String stageHealth = Integer.toString(stage.getHealth());
         String escalationString = stage.getEscalationString();
         
         if (stageName.equals(targetName) && stageName.equalsIgnoreCase(targetType)) {
            // All our data IS our key, so the data part is blank.
            return "";
         } else {
            String stageData = String.format("%s %s %s %s", targetName, targetType, stageMoves, stageHealth);
            if (stage.isEscalation()) {
               stageData = String.format("%s %s", stageData, escalationString);
            }
            return stageData;
         }
      }
      
      @Override
      public Class<? extends Object> getDataClass() {
         return Stage.class;
      }
      
   },
   FILE {
      
      @Override
      public Object parseValue(String key, String value) throws Exception {
         File f = new File(value).getAbsoluteFile();
         if (!f.exists()) {
            String parentPath = f.getParent();
            if (parentPath != null) {
               new File(parentPath).mkdirs();
            }
            f.createNewFile();
            if (f.exists()) {
               f.delete();
            } else {
               throw new FileNotFoundException(value);
            }
         }
         return f;
      }
      
      @Override
      public String getDataString(Object obj) throws Exception {
         File ret = (File) obj;
         String userdir = System.getProperty("user.dir").replaceAll("\\\\", "/");
         String filePath = ret.getAbsolutePath().replaceAll("\\\\", "/");
         if (filePath.startsWith(userdir)) {
            filePath = filePath.substring(userdir.length() + 1);
         }
         return filePath;
      }
      
      @Override
      public Class<File> getDataClass() {
         return File.class;
      }
      
   },
   RESOURCE {
      
      @Override
      public String parseValue(String key, String value) throws Exception {
         return value;
      }
      
      @Override
      public Class<? extends Object> getDataClass() {
         return String.class;
      }
      
      @Override
      public String getDataString(Object obj) throws Exception {
         String value = (String) obj;
         value = value.trim();
         return value;
      }
      
   },
   TEAM {
      
      @Override
      public Team parseValue(String key, String value) throws Exception {
         Matcher m = TEAM_PATTERN.matcher(value);
         if (!m.find()) {
            throw new IllegalArgumentException("Value is not a team." + value);
         }
         TeamImpl team = new TeamImpl();
         
         String names = m.group(1);
         String binds = m.group(2);
         
         List<String> nameList = Arrays.asList(names.split("[,]"));
         List<String> bindsList = Arrays.asList(binds.split("[,]", -1));
         
         for (int i = 0; i < nameList.size(); i++) {
            String name = nameList.get(i);
            Character binding = null;
            if (i < bindsList.size() && bindsList.get(i).length() > 0) {
               binding = bindsList.get(i).charAt(0);
            }
            team.addName(name, binding);
         }
         
         // Handles nulls fine on its own
         String megaName = m.group(3);
         team.setMegaSlot(megaName);
         
         String nonSupports = m.group(4);
         if (nonSupports != null) {
            List<String> nonSupportList = Arrays.asList(nonSupports.split("[,]"));
            for (int i = 0; i < nonSupportList.size(); i++) {
               String nonSupport = nonSupportList.get(i);
               team.setNonSupport(nonSupport, true);
            }
         }
         return team;
      }
      
      @Override
      public String getDataString(Object obj) throws Exception {
         Team team = (Team) obj;
         String data = team.toString();
         return data;
      }
      
      @Override
      public Class<Team> getDataClass() {
         return Team.class;
      }
      
   },
   FONT {
      
      @Override
      public Font parseValue(String key, String value) throws Exception {
         Matcher m = FONT_PATTERN.matcher(value);
         if (!m.find()) {
            throw new IllegalArgumentException("Value is not a font. " + value);
         }
         String name = m.group(1);
         String stylesString = m.group(2);
         String sizeString = m.group(3);
         
         int style = 0;
         String[] styles = stylesString.split(",");
         if (styles.length > 0) {
            for (String s : styles) {
               if (s.equalsIgnoreCase("ITALIC")) {
                  style |= Font.ITALIC;
               } else if (s.equalsIgnoreCase("BOLD")) {
                  style |= Font.BOLD;
               } else {
                  s = s.trim();
                  if (s.matches("\\d+")) {
                     try {
                        int val = Integer.parseInt(s);
                        style |= val % 4;
                        // only include the bits that could mean BOLD or ITALIC
                     } catch (NumberFormatException nfe) {
                        // then we don't do anything
                     }
                  }
               }
            }
         }
         
         int size = Integer.parseInt(sizeString);
         
         return new Font(name, style, size);
      }
      
      @Override
      public String getDataString(Object obj) throws Exception {
         Font value = (Font) obj;
         String name = value.getFamily();
         int style = value.getStyle();
         int size = value.getSize();
         return String.format("%s %d %d", name, style, size);
      }
      
      @Override
      public Class<Font> getDataClass() {
         return Font.class;
      }
      
   },
   GRADING_MODE {
      
      @Override
      public GradingMode parseValue(String key, String value) throws Exception {
         Matcher m = GRADING_PATTERN.matcher(value);
         m.find();
         String description = m.group(1);
         boolean isCustom = Boolean.parseBoolean(m.group(2));
         GradingMode ret = new GradingMode(key, description, isCustom);
         return ret;
      }
      
      @Override
      public String getDataString(Object obj) throws Exception {
         GradingMode mode = (GradingMode) obj;
         return String.format("%s %s", mode.getDescription(), mode.isCustom());
      }
      
      @Override
      public Class<GradingMode> getDataClass() {
         return GradingMode.class;
      }
      
   };
   
   private static final Pattern COLOR_PATTERN = Pattern.compile("^\\s*(\\d+)\\s+(\\d+)\\s+(\\d+)(?:\\s+(\\d+))?\\s*$");
   private static final Pattern SPECIES_PATTERN = Pattern
         .compile("^\\s*(-?\\d+)\\s+(\\d{1,3})\\s+(\\S+)\\s+(\\S+)(?:\\s+(\\S+)\\s+(\\S+)(?:\\s+(\\S+))?)?\\s*$");
   // __________________numberId ______attack______type ____effect __(o) MegaName MegaEffect
   // (o)MegaType
   private static final Pattern STAGE_PATTERN = Pattern
         .compile("^\\s*(\\S+)\\s+(\\S+)(?:\\s+(\\d+)(?:\\s+(\\d+)(?:\\s+(\\S+))?)?)?\\s*$");
   private static final Pattern TEAM_PATTERN = Pattern
.compile(
         "^\\s*(\\S+)(?:\\s+((?:[^,\\s]+[,])*[^,\\s]+)(?:\\s+((?:[^,\\s]+[,])*[^,\\s]+)(?:\\s+([^\\s]+))?(?:\\s+((?:[^,\\s]+[,])*[^,\\s]+))?)?)?\\s*$");
   // _______________ListofSpecies __________ListofKeybinds _______________MegaName _______,
   // ________ListOfSupportStatus
   private static final Pattern FONT_PATTERN = Pattern
         .compile("^\\s*(\\S+)\\s+([^,\\s]+(?:,[^,\\s]+)?)\\s+(\\d+)\\s*$");
   private static final Pattern GRADING_PATTERN = Pattern.compile("^\\s*(\\S+)\\s+(\\S+)\\s*$");
   
   /**
    * Parses the given string into the value for this entry type. Exceptions will be thrown if the
    * value is not valid in some way.
    * 
    * @param key
    *           The string key for the entry
    * @param value
    *           The value to be parsed
    * @return The parsed value
    * @throws Exception
    *            If anything goes wrong with the parse, i.e. invalid data.
    */
   public abstract Object parseValue(String key, String value) throws Exception;
   
   public abstract String getDataString(Object obj) throws Exception;
   
   public static String getSaveString(Object obj, EntryType type) throws Exception {
      if (obj == null) {
         throw new NullPointerException("Cannot convert a null object.");
      }
      if (!type.getDataClass().isInstance(obj)) {
         throw new IllegalArgumentException("Invalid type. The given object " + obj.toString() + " is not of type: "
               + type.toString());
      }
      return type.getDataString(obj);
   }
   
   public abstract Class<? extends Object> getDataClass();
}
