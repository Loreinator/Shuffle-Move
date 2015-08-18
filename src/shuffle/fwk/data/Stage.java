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

package shuffle.fwk.data;

import java.util.Collection;
import java.util.HashSet;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Andrew Meyers
 *
 */
public class Stage implements Comparable<Stage> {
   
   public static final int DEFAULT_MOVES = 20;
   public static final int DEFAULT_HEALTH = 10000;
   
   private static final Pattern MAIN = Pattern.compile("^\\s*(\\d+)\\s*$");
   private static final Pattern SPECIAL = Pattern.compile("^\\s*SP[_ ](\\d+)?\\S*\\s*$");
   private static final Pattern EX = Pattern.compile("^\\s*EX(\\d+)\\s*$");
   private static final int EX_OFFSET = 10000;
   private static final int SPECIAL_OFFSET = 100000000;
   private static final int TYPE_OFFSET = Integer.MAX_VALUE - 1;

   private final String stageName;
   private final String targetName;
   private final PkmType targetType;
   private final int stageMoves;
   private final int stageHealth;
   private final String toString;
   
   private final int ordering;

   public Stage(Stage stage) {
      this(stage.stageName, stage.targetName, stage.targetType, stage.stageMoves, stage.stageHealth);
   }

   public Stage(String name, String target, PkmType type, int moves, int health) {
      stageName = name;
      targetName = target;
      targetType = type;
      stageMoves = moves;
      stageHealth = health;
      
      StringBuilder sb = new StringBuilder();
      if (targetType.toString().equals(targetName)) {
         sb.append(targetName.replaceAll("_", " "));
      } else {
         sb.append(stageName.replaceAll("_", " "));
         sb.append(": ");
         sb.append(targetName.replaceAll("_", " "));
      }
      toString = sb.toString();
      int orderValue = getOrderValue(this);
      ordering = orderValue;
   }
   
   public Stage(PkmType target) {
      this(target.toString(), target.toString(), target, DEFAULT_MOVES, DEFAULT_HEALTH);
   }
   
   // TODO implement stage-specific configuration for predictable stages.
   public Collection<Queue<Species>> getDropPatterns(int column) {
      return new HashSet<Queue<Species>>();
   }
   
   public String getName() {
      return stageName;
   }
   
   public String getTarget() {
      return targetName;
   }
   
   public PkmType getType() {
      return targetType;
   }
   
   public int getMoves() {
      return stageMoves;
   }
   
   public int getHealth() {
      return stageHealth;
   }
   
   public static int getOrderValue(Stage s) {
      String name = s.getName();
      Matcher main = MAIN.matcher(name);
      if (main.find()) {
         return Integer.parseInt(main.group(1));
      }
      Matcher special = SPECIAL.matcher(name);
      if (special.find()) {
         String value = special.group(1);
         if (value == null) {
            return SPECIAL_OFFSET;
         } else {
            return SPECIAL_OFFSET + Integer.parseInt(value);
         }
      }
      Matcher ex = EX.matcher(name);
      if (ex.find()) {
         String value = ex.group(1);
         if (value.matches("\\d+")) {
            return EX_OFFSET + Integer.parseInt(value);
         }
      }
      if (s.getType().toString().equals(name)) {
         return TYPE_OFFSET;
      }
      return Integer.MAX_VALUE;
   }

   @Override
   public String toString() {
      return toString;
   }
   
   @Override
   public int hashCode() {
      return toString().hashCode();
   }
   
   @Override
   public boolean equals(Object o) {
      return o != null && o.toString().equals(toString());
   }
   
   /*
    * (non-Javadoc)
    * @see java.lang.Comparable#compareTo(java.lang.Object)
    */
   @Override
   public int compareTo(Stage other) {
      int result = Integer.compare(ordering, other.ordering);
      if (result == 0) {
         result = getName().compareTo(other.getName());
      }
      if (result == 0) {
         result = toString().compareTo(other.toString());
      }
      return result;
   }
}
