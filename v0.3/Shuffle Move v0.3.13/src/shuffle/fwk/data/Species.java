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

import java.util.logging.Logger;

/**
 * This is an object which acts as the intermediary between the configuration loading modules and
 * the main program. They are created by species, and then can be used to obtain things like the
 * icon, name, type, power, etc. This object is immutable once created, to ensure consistency.
 * 
 * @author Andrew Meyers
 */
public class Species implements Comparable<Species> {
   @SuppressWarnings("unused")
   private static final Logger LOG = Logger.getLogger(Species.class.getName());
   public static final Species AIR = new Species("Air", 0, 0, PkmType.NONE, Effect.AIR, null, Effect.NONE);
   public static final Species WOOD = new Species("Wood", 1, 0, PkmType.WOOD, Effect.WOOD, null, Effect.NONE);
   public static final Species METAL = new Species("Metal", 2, 0, PkmType.NONE, Effect.METAL, null, Effect.NONE);
   public static final Species COIN = new Species("Coin", 3, 100, PkmType.NONE, Effect.COIN, null, Effect.NONE);
   
   private static final int[] LEVEL_BONUS = new int[] { 0, // level 0 has 0 bonus
         0, 3, 6, 8, 10, 12, 14, 16, 18, 20 };
   private static final int[] LEVEL_BONUS_30 = new int[] { 0, // level 0 has 0 bonus
         0, 5, 9, 12, 15, 17, 19, 21, 23, 25 };
   
   private final String name;
   private final int attack;
   private final PkmType type;
   private final Effect effect;
   private final String megaName;
   private final Effect megaEffect;
   private final int number;
   
   private final String toString;
   
   public Species(Species other) {
      this(other.name, other.number, other.attack, other.type, other.effect, other.megaName, other.megaEffect);
   }
   
   public Species(String name, int attack, PkmType type, Effect effect) {
      this(name, null, attack, type, effect, null, null);
   }
   
   public Species(String name, int attack, PkmType type, Effect effect, String megaName, Effect megaEffect) {
      this(name, null, attack, type, effect, megaName, megaEffect);
   }
   
   public Species(String name, Integer number, int attack, PkmType type, Effect effect, String megaName,
         Effect megaEffect) {
      if (name == null || type == null || effect == null) {
         throw new NullPointerException(String.format(
               "Cannot specify a null Species name (%s), type (%s), or effect (%s).", name, type, effect));
      }
      this.number = number == null ? -1 : Math.max(0, number.intValue());
      this.name = name;
      if (attack < 0) {
         this.attack = 0;
      } else if (attack >= 999) {
         this.attack = 999;
      } else {
         this.attack = attack;
      }
      this.type = type;
      this.effect = effect;
      this.megaName = megaName == null || megaName.trim().isEmpty() ? null : megaName.trim();
      this.megaEffect = megaEffect == null ? Effect.NONE : megaEffect;
      
      toString = getString();
   }
   
   public int getNumber() {
      return number;
   }
   
   public String getName() {
      return name;
   }
   
   public int getBaseAttack() {
      return attack;
   }
   
   public int getAttack(int level) {
      int lev = level;
      if (lev > 10) {
         lev = 10;
      } else if (lev < 0) {
         lev = 0;
      }
      int bonus;
      if (attack == 30) {
         bonus = LEVEL_BONUS_30[lev];
      } else {
         bonus = LEVEL_BONUS[lev];
      }
      return getBaseAttack() + bonus;
   }
   
   public PkmType getType() {
      return type;
   }
   
   public Effect getEffect() {
      return effect;
   }
   
   public String getMegaName() {
      return megaName;
   }
   
   public Effect getMegaEffect() {
      return megaEffect;
   }
   
   public boolean isFreezable() {
      return !getEffect().equals(Effect.AIR);
   }
   
   private String getString() {
      String s = String.format("%s %s %d %s %s", getName(), String.valueOf(number), getBaseAttack(), getType()
            .toString(), getEffect().toString());
      if (getMegaName() != null) {
         s = s + String.format(" %s %s", getMegaName(), getMegaEffect().toString());
      }
      return s;
   }
   
   @Override
   public String toString() {
      return toString;
   }
   
   @Override
   public int hashCode() {
      return 37 * toString().hashCode();
   }
   
   @Override
   public boolean equals(Object o) {
      if (o == null || !(o instanceof Species)) {
         return false;
      }
      Species s = (Species) o;
      return toString().equals(s.toString());
   }
   
   /*
    * (non-Javadoc)
    * @see java.lang.Comparable#compareTo(java.lang.Object)
    */
   @Override
   public int compareTo(Species o) {
      return o == null ? 1 : Integer.compare(number, o.number);
   }
}
