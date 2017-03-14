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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import shuffle.fwk.config.manager.RosterManager;
import shuffle.fwk.i18n.I18nUser;

/**
 * This is an object which acts as the intermediary between the configuration loading modules and
 * the main program. They are created by species, and then can be used to obtain things like the
 * icon, name, type, power, etc. This object is immutable once created, to ensure consistency.
 * 
 * @author Andrew Meyers
 */
public class Species implements Comparable<Species>, I18nUser {
   @SuppressWarnings("unused")
   private static final Logger LOG = Logger.getLogger(Species.class.getName());

   // Must be initialized before the static species
   private static final int[] BASE_ATTACK = new int[] { 30, 40, 50, 60, 70, 80, 90 };
   private static final int[][] LEVEL_BONUS = new int[][] {
         /* 30 */ { 0, 5, 9, 12, 15, 17, 19, 21, 23, 25, 31, 37, 43, 49, 55, 58, 61, 64, 67, 70 },
         /* 40 */ { 0, 3, 6, 8, 10, 12, 14, 16, 18, 20, 26, 32, 38, 44, 50, 53, 56, 59, 62, 65 },
         /* 50 */ { 0, 3, 6, 8, 10, 12, 14, 16, 18, 20, 25, 30, 35, 40, 50, 53, 56, 59, 62, 65 },
         /* 60 */ { 0, 3, 6, 8, 10, 12, 14, 16, 18, 20, 24, 28, 32, 36, 45, 48, 51, 54, 57, 60 },
         /* 70 */ { 0, 3, 6, 8, 10, 12, 14, 16, 18, 20, 23, 26, 29, 32, 40, 43, 46, 49, 52, 55 },
         /* 80 */ { 0, 3, 6, 8, 10, 12, 14, 16, 18, 20, 22, 24, 26, 28, 35, 38, 41, 44, 47, 50 },
         /* 90 */ { 0, 3, 6, 8, 10, 12, 14, 16, 18, 20, 22, 24, 26, 28, 30, 33, 36, 39, 42, 45 } };
   public static final int MAX_LEVEL = 20;
         
   private static final Map<String, Integer> STRING_TO_ID = new HashMap<String, Integer>();

   public static final Species AIR = new Species("Air", 0, 0, PkmType.NONE, Effect.AIR, null, Effect.NONE);
   public static final Species WOOD = new Species("Wood", 1, 0, PkmType.WOOD, Effect.WOOD, null, Effect.NONE);
   public static final Species METAL = new Species("Metal", 2, 0, PkmType.NONE, Effect.METAL, null, Effect.NONE);
   public static final Species COIN = new Species("Coin", 3, 100, PkmType.NONE, Effect.COIN, null, Effect.NONE);
   public static final Species METAL_1 = new Species("Metal_1", 4, 0, PkmType.NONE, Effect.METAL, null, Effect.NONE);
   public static final Species METAL_2 = new Species("Metal_2", 5, 0, PkmType.NONE, Effect.METAL, null, Effect.NONE);
   public static final Species METAL_3 = new Species("Metal_3", 6, 0, PkmType.NONE, Effect.METAL, null, Effect.NONE);
   public static final Species METAL_4 = new Species("Metal_4", 7, 0, PkmType.NONE, Effect.METAL, null, Effect.NONE);
   public static final Species METAL_5 = new Species("Metal_5", 8, 0, PkmType.NONE, Effect.METAL, null, Effect.NONE);
   public static final Species FREEZE = new Species("Freeze", 9, 0, PkmType.NONE, Effect.UNLISTED, null, Effect.NONE);
   
   public static final List<Species> FIXED_SPECIES = Collections.unmodifiableList(Arrays.asList(AIR, WOOD, METAL, COIN,
         METAL_5, METAL_4, METAL_3, METAL_2, METAL_1, FREEZE));
   public static final List<Species> EXTENDED_METAL = Collections
         .unmodifiableList(Arrays.asList(METAL, METAL_1, METAL_2, METAL_3, METAL_4, METAL_5));
   
   
   private final String name;
   private final int attack;
   private final PkmType type;
   private final List<Effect> effects;
   private final List<String> effectStrings;
   private final String megaName;
   private final Effect megaEffect;
   private final PkmType megaType;
   private final Double number;
   private final String toString;
   private final Integer ID;
   private final int hash;
   private final int[] levelBonus;
   
   public Species(Species other) {
      this(other.name, other.number, other.attack, other.type, other.effects, other.megaName, other.megaEffect,
            other.megaType);
   }
   
   public Species(String name, int attack, PkmType type, Effect effect) {
      this(name, null, attack, type, effect, null, null);
   }
   
   public Species(String name, int attack, PkmType type, Effect effect, String megaName, Effect megaEffect) {
      this(name, null, attack, type, effect, megaName, megaEffect);
   }
   
   public Species(String name, Double number, int attack, PkmType type, Effect effect, String megaName,
         Effect megaEffect) {
      this(name, number, attack, type, Arrays.asList(effect), megaName, megaEffect, type);
   }
   
   public Species(String name, int number, int attack, PkmType type, Effect effect, String megaName,
         Effect megaEffect) {
      this(name, (double) number, attack, type, Arrays.asList(effect), megaName, megaEffect, type);
   }
   
   public Species(String name, int number, int attack, PkmType type, List<Effect> effects, String megaName,
         Effect megaEffect, PkmType megaType) {
      this(name, (double) number, attack, type, effects, megaName, megaEffect, megaType);
   }
   
   public Species(String name, Double number, int attack, PkmType type, List<Effect> effects, String megaName,
         Effect megaEffect, PkmType megaType) {
      if (name == null || type == null || effects == null || effects.isEmpty()) {
         throw new NullPointerException(String.format(
"Cannot specify a null Species name (%s), type (%s), or effects (%s).", name, type, effects));
      }
      this.number = number == null ? -1 : Math.max(0.0, number.doubleValue());
      this.name = name;
      if (attack < 0) {
         this.attack = 0;
      } else if (attack >= 999) {
         this.attack = 999;
      } else {
         this.attack = attack;
      }
      int i = 0;
      while (i < BASE_ATTACK.length && BASE_ATTACK[i] < this.attack) {
         i++;
         // Seek to the smallest slot that is equal to or greater than this,
         // or the largest slot if none are sufficient.
      }
      if (i >= BASE_ATTACK.length) {
         // If beyond the bound, go to the highest bound.
         i = BASE_ATTACK.length - 1;
      }
      if (i < 0) {
         // if the highest bound is less than 0 (there are no bonuses defined), then use 0.
         this.levelBonus = new int[] { 0 };
      } else {
         // Otherwise, use the appropriate level bounds.
         this.levelBonus = LEVEL_BONUS[i];
      }
      this.type = type;
      this.effects = effects;
      this.effectStrings = new ArrayList<String>(effects.stream().map(e -> e.toString()).collect(Collectors.toList()));
      this.megaName = megaName == null || megaName.trim().isEmpty() ? null : megaName.trim();
      this.megaEffect = megaEffect == null ? Effect.NONE : megaEffect;
      this.megaType = megaType == null ? type : (megaType == PkmType.NONE ? type : megaType);
      toString = getString();
      ID = getId(toString);
      hash = 37 * toString.hashCode();
   }
   
   private static final Integer getId(String toString) {
      Integer ret = STRING_TO_ID.get(toString);
      if (ret == null) {
         ret = STRING_TO_ID.size();
         STRING_TO_ID.put(toString, ret);
      }
      return ret;
   }

   public double getNumber() {
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
      if (lev > levelBonus.length) {
         lev = levelBonus.length;
      } else if (lev < 1) {
         lev = 1;
      }
      int bonus = levelBonus[lev - 1];
      return getBaseAttack() + bonus;
   }
   
   public PkmType getType() {
      return type;
   }
   
   // public Effect getEffect() {
   // return getEffect(null);
   // }
   
   public List<Effect> getEffects() {
      return Collections.unmodifiableList(effects);
   }
   
   public String getEffectsString() {
      StringBuilder sb = new StringBuilder();
      Iterator<String> itr = effectStrings.iterator();
      while (itr.hasNext()) {
         sb.append(itr.next());
         if (itr.hasNext()) {
            sb.append(",");
         }
      }
      return sb.toString();
   }
   
   public boolean hasEffect(String effectName) {
      return effectStrings.contains(effectName);
   }
   
   public Effect getDefaultEffect() {
      return effects.get(0);
   }
   
   public Effect getEffect(RosterManager manager) {
      Effect ret = getDefaultEffect();
      if (manager != null) {
         ret = manager.getActiveEffect(this);
      }
      return ret;
   }
   
   public String getMegaName() {
      return megaName;
   }
   
   public Effect getMegaEffect() {
      return megaEffect;
   }
   
   public PkmType getMegaType() {
      return megaType;
   }
   
   public boolean isAir() {
      return equals(AIR);
   }
   
   public boolean isFreezable() {
      return !isAir() && !equals(FREEZE);
   }
   
   private String getString() {
      String s = String.format("%s %s %d %s %s", getName(), String.valueOf(number), getBaseAttack(),
            getType().toString(), getEffectsString());
      if (getMegaName() != null) {
         s = s + String.format(" %s %s", getMegaName(), getMegaEffect().toString());
         if (getMegaType() != getType()) {
            s = s + String.format(" %s", getMegaType());
         }
      }
      return s;
   }
   
   @Override
   public String toString() {
      return toString;
   }
   
   @Override
   public int hashCode() {
      return hash;
   }
   
   /**
    * Gets the localized name for this species.
    * 
    * @param asMega
    *           If true, and this species has a mega name, then the mega name will be returned
    *           instead (localized)
    * @return The localized name for this species, as a String.
    */
   public String getLocalizedName(boolean asMega) {
      if (asMega && getMegaName() != null) {
         return getString(getMegaName());
      } else {
         return getString(getName());
      }
   }
   
   /**
    * Equivalent to<br>
    * {@link Species#getLocalizedName(boolean)} called as getLocalizedName(false)
    * 
    * @return The localized name for this species, as a String.
    */
   public String getLocalizedName() {
      return getLocalizedName(false);
   }

   @Override
   public boolean equals(Object o) {
      if (o == null || !(o instanceof Species)) {
         return false;
      }
      Species s = (Species) o;
      return ID.equals(s.ID);
   }
   
   /*
    * (non-Javadoc)
    * @see java.lang.Comparable#compareTo(java.lang.Object)
    */
   @Override
   public int compareTo(Species o) {
      return o == null ? 1 : Double.compare(number, o.number);
   }
   
   public Species getNextMetal() {
      return getNextMetal(this);
   }
   
   public static Species getNextMetal(Species cur) {
      if (cur.equals(METAL) || cur.equals(METAL_5)) {
         return METAL_4;
      } else if (cur.equals(METAL_4)) {
         return METAL_3;
      } else if (cur.equals(METAL_3)) {
         return METAL_2;
      } else if (cur.equals(METAL_2)) {
         return METAL_1;
      } else if (cur.equals(METAL_1)) {
         return AIR;
      } else {
         return METAL;
      }
   }
}
