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

package shuffle.fwk.data.simulation.util;

import java.util.Optional;

/**
 * @author Andrew Meyers
 *
 */
public class NumberSpan extends Number implements Cloneable, Comparable<NumberSpan> {
   private static final long serialVersionUID = -6911146707992934793L;
   
   private int min;
   private int max;
   private double total;
   private int n;
   
   public NumberSpan() {
      min = 0;
      max = 0;
      total = 0;
      n = 0;
   }
   
   public NumberSpan(int min, int max, double total, int n) {
      this.min = min;
      this.max = max;
      this.total = total;
      this.n = n;
   }
   
   @Override
   public NumberSpan clone() {
      NumberSpan ns = new NumberSpan();
      ns.min = min;
      ns.max = max;
      ns.total = total;
      ns.n = n;
      return ns;
   }
   
   /*
    * (non-Javadoc)
    * @see java.lang.Number#intValue()
    */
   @Override
   public int intValue() {
      return getSafeAverage().intValue();
   }
   
   /*
    * (non-Javadoc)
    * @see java.lang.Number#longValue()
    */
   @Override
   public long longValue() {
      return getSafeAverage().longValue();
   }
   
   /*
    * (non-Javadoc)
    * @see java.lang.Number#floatValue()
    */
   @Override
   public float floatValue() {
      return getSafeAverage().floatValue();
   }
   
   /*
    * (non-Javadoc)
    * @see java.lang.Number#doubleValue()
    */
   @Override
   public double doubleValue() {
      return getSafeAverage().doubleValue();
   }
   
   private Double getSafeAverage() {
      return Optional.of(getAverage()).orElse(0.0);
   }
   
   public double getAverage() {
      return n == 0 ? 0 : total / n;
   }
   
   public int getMinimum() {
      return min;
   }
   
   public int getMaximum() {
      return max;
   }
   
   protected final void putValue(int value, float likelihood) {
      if (likelihood < 0f) {
         throw new IllegalArgumentException("Likelihood cannot be negative.");
      }
      if (n == 0) {
         min = value;
         max = value;
      } else {
         min = Math.min(min, value);
         max = Math.max(max, value);
      }
      total += value * likelihood;
      n++;
   }
   
   @Override
   public final String toString() {
      String ret;
      double average = getAverage();
      String avgString;
      if (average == (long) average) {
         avgString = String.format("%d", (long) average);
      } else {
         avgString = String.format("%s", average);
      }
      if (min == max) {
         ret = avgString;
      } else {
         ret = String.format("[%d-%d] (%s)", getMinimum(), getMaximum(), avgString);
      }
      return ret;
   }
   
   /*
    * (non-Javadoc)
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + max;
      result = prime * result + min;
      result = prime * result + n;
      long temp;
      temp = Double.doubleToLongBits(total);
      result = prime * result + (int) (temp ^ temp >>> 32);
      return result;
   }
   
   /*
    * (non-Javadoc)
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj) {
      boolean equal = obj != null && obj instanceof NumberSpan;
      if (equal) {
         NumberSpan other = (NumberSpan) obj;
         equal &= toString().equals(other.toString());
         equal &= n == other.n;
      }
      return equal;
   }
   
   /*
    * (non-Javadoc)
    * @see java.lang.Comparable#compareTo(java.lang.Object)
    */
   @Override
   public int compareTo(NumberSpan o) {
      int ret = Double.compare(total, o.total);
      if (ret == 0) {
         ret = Integer.compare(max, o.max);
         if (ret == 0) {
            ret = Integer.compare(min, o.min);
         }
      }
      return ret;
   }
   
}
