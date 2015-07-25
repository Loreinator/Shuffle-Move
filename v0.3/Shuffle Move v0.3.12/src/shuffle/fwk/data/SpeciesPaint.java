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

/**
 * @author Andrew Meyers
 *
 */
public class SpeciesPaint {
   
   public static final SpeciesPaint AIR = new SpeciesPaint(Species.AIR);
   
   private final Species s;
   private final Boolean frozen;
   private final Boolean mega;
   
   public SpeciesPaint(Species paint) {
      this(paint, null, null);
   }
   
   public SpeciesPaint(Species paint, Boolean isFrozen) {
      this(paint, isFrozen, null);
   }
   
   public SpeciesPaint(Species paint, Boolean isFrozen, Boolean isMega) {
      if (paint == null) {
         throw new NullPointerException("Cannot specify a null species for a paint. Air would be acceptable.");
      }
      s = paint;
      frozen = isFrozen;
      mega = isMega;
   }
   
   public Species getSpecies() {
      return s;
   }
   
   public Boolean isFrozen() {
      return frozen == null ? false : frozen.booleanValue();
   }
   
   public Boolean isMega() {
      return mega == null ? false : mega.booleanValue();
   }
   
   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder();
      if (isFrozen()) {
         sb.append("Frozen_");
      }
      if (isMega()) {
         sb.append("Mega_");
      }
      sb.append(s.toString());
      return sb.toString();
   }
   
   @Override
   public int hashCode() {
      return toString().hashCode();
   }
   
   @Override
   public boolean equals(Object o) {
      return o != null && o instanceof SpeciesPaint && o.toString().equals(toString());
   }
}
