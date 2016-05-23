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

//							Nrm	Fir	Wat	Gra	Ele	Ice	Fig	Poi	Gro	Fly	Psy	Bug	Roc	Gho	Drg	Drk	Ste	Fry	None Wood
public enum PkmType {// 0 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18
   NORMAL(0, new int[] { 2, 2, 2, 2, 2, 2, 4, 2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 0, 0 }),
   FIRE(1, new int[] { 2, 1, 4, 1, 2, 1, 2, 2, 4, 2, 2, 1, 4, 2, 2, 2, 1, 1, 0, 0 }),
   WATER(2, new int[] { 2, 1, 1, 4, 4, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 2, 0, 0 }),
   GRASS(3, new int[] { 2, 4, 1, 1, 1, 4, 2, 4, 1, 4, 2, 4, 2, 2, 2, 2, 2, 2, 0, 0 }),
   // Nrm Fir Wat Gra Ele Ice Fig Poi Gro Fly Psy Bug Roc Gho Drg Drk Ste Fry None
   ELECTRIC(4, new int[] { 2, 2, 2, 2, 1, 2, 2, 2, 4, 1, 2, 2, 2, 2, 2, 2, 1, 2, 0, 0 }),
   ICE(5, new int[] { 2, 4, 2, 2, 2, 1, 4, 2, 2, 2, 2, 2, 4, 2, 2, 2, 4, 2, 0, 0 }),
   FIGHTING(6, new int[] { 2, 2, 2, 2, 2, 2, 2, 2, 2, 4, 4, 1, 1, 2, 2, 1, 2, 4, 0, 0 }),
   POISON(7, new int[] { 2, 2, 2, 1, 2, 2, 1, 1, 4, 2, 4, 1, 2, 2, 2, 2, 2, 1, 0, 0 }),
   // Nrm Fir Wat Gra Ele Ice Fig Poi Gro Fly Psy Bug Roc Gho Drg Drk Ste Fry None
   GROUND(8, new int[] { 2, 2, 4, 4, 1, 4, 2, 1, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 0, 0 }),
   FLYING(9, new int[] { 2, 2, 2, 1, 4, 4, 1, 2, 1, 2, 2, 1, 4, 2, 2, 2, 2, 2, 0, 0 }),
   PSYCHIC(10, new int[] { 2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 1, 4, 2, 4, 2, 4, 2, 2, 0, 0 }),
   BUG(11, new int[] { 2, 4, 2, 1, 2, 2, 1, 2, 1, 4, 2, 2, 4, 2, 2, 2, 2, 2, 0, 0 }),
   // Nrm Fir Wat Gra Ele Ice Fig Poi Gro Fly Psy Bug Roc Gho Drg Drk Ste Fry None
   ROCK(12, new int[] { 1, 1, 4, 4, 2, 2, 4, 1, 4, 1, 2, 2, 2, 2, 2, 2, 4, 2, 0, 0 }),
   GHOST(13, new int[] { 1, 2, 2, 2, 2, 2, 1, 1, 2, 2, 2, 1, 2, 4, 2, 4, 2, 2, 0, 0 }),
   DRAGON(14, new int[] { 2, 1, 1, 1, 1, 4, 2, 2, 2, 2, 2, 2, 2, 2, 4, 2, 2, 4, 0, 0 }),
   DARK(15, new int[] { 2, 2, 2, 2, 2, 2, 4, 2, 2, 2, 1, 4, 2, 1, 2, 1, 2, 4, 0, 0 }),
   // Nrm Fir Wat Gra Ele Ice Fig Poi Gro Fly Psy Bug Roc Gho Drg Drk Ste Fry None
   STEEL(16, new int[] { 1, 4, 2, 1, 2, 1, 4, 1, 4, 1, 1, 1, 1, 2, 1, 2, 1, 1, 0, 0 }),
   FAIRY(17, new int[] { 2, 2, 2, 2, 2, 2, 1, 4, 2, 2, 2, 1, 2, 2, 1, 1, 4, 2, 0, 0 }),
   NONE(18, new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }),
   WOOD(19, new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });
   // Nrm Fir Wat Gra Ele Ice Fig Poi Gro Fly Psy Bug Roc Gho Drg Drk Ste Fry None
   // Along the left is the target type. Along the top is the attacking type.
   // The power given is the modifier when this type is attacked by a type by this index.
   
   private int index;
   private int[] power;
   
   private PkmType(int index, int[] power) {
      this.index = index;
      this.power = power;
   }
   
   public static int numTypes = 20;
   
   public static PkmType getType(String type) {
      PkmType retType = NONE;
      for (PkmType tempType : PkmType.values()) {
         if (tempType.toString().equalsIgnoreCase(type)) {
            return tempType;
         }
      }
      return retType;
   }
   
   public static double getMultiplier(PkmType cleared, PkmType target) {
      return target.power[cleared.index] / 2.0;
   }
   
   public boolean isSpecial() {
      return equals(WOOD) || equals(NONE);
   }
   
   public static boolean hasType(String type) {
      for (PkmType tempType : PkmType.values()) {
         if (tempType.toString().equalsIgnoreCase(type)) {
            return true;
         }
      }
      return false;
   }
}
