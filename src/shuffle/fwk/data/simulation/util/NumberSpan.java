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

import java.text.DecimalFormat;
import java.util.Optional;

/**
 * @author Andrew Meyers
 *         
 */
public class NumberSpan extends Number implements Cloneable, Comparable<NumberSpan> {
   private static final long serialVersionUID = -6911146707992934793L;
   private static final DecimalFormat FORMAT = new DecimalFormat("##.##");
   
   private final double min;
   private final double max;
   private final double total;
   private final int n;
   
   public NumberSpan() {
      min = 0;
      max = 0;
      total = 0;
      n = 0;
   }
   
   public NumberSpan(Number value) {
      if (value instanceof NumberSpan) {
         NumberSpan other = (NumberSpan) value;
         min = other.min;
         max = other.max;
         total = other.total;
         n = other.n;
      } else {
         double doubleValue = value.doubleValue();
         min = Math.max(0, doubleValue);
         max = Math.max(0, doubleValue);
         total = doubleValue;
         n = doubleValue <= 0 ? 0 : 1;
      }
   }
   
   public NumberSpan(Number base, Number bonus, double chance) {
      double baseValue = base.doubleValue();
      if (chance <= 0.0) {
         min = baseValue;
         max = min;
         total = min;
      } else {
         double bonusValue = bonus.doubleValue();
         if (chance >= 1.0) {
            max = baseValue + bonusValue;
            min = max;
            total = max;
         } else {
            min = baseValue;
            max = baseValue + bonusValue;
            total = baseValue + bonusValue * chance;
         }
      }
      n = 1;
   }
   
   public NumberSpan(Number min, Number max, Number total, Number n) {
      this.min = min.doubleValue();
      this.max = max.doubleValue();
      this.total = total.doubleValue();
      this.n = n.intValue();
   }
   
   @Override
   public NumberSpan clone() {
      return new NumberSpan(min, max, total, n);
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
   
   public double getMinimum() {
      return min;
   }
   
   public double getMaximum() {
      return max;
   }
   
   public NumberSpan add(Number num) {
      NumberSpan ret;
      if (num instanceof NumberSpan) {
         NumberSpan other = (NumberSpan) num;
         if (n == 0) {
            ret = new NumberSpan(other.min, other.max, other.total, other.n);
         } else if (other.n != 0) {
            ret = new NumberSpan(other.min + min, other.max + max, other.total + total, Math.max(n, other.n));
         } else {
            ret = clone();
         }
      } else {
         double val = num.doubleValue();
         ret = new NumberSpan(min + val, max + val, total + val, Math.max(1, n));
      }
      return ret;
   }
   
   /**
    * @param effectSpecial
    * @return
    */
   public NumberSpan multiplyBy(Number num) {
      NumberSpan ret;
      if (num instanceof NumberSpan) {
         NumberSpan span = (NumberSpan) num;
         ret = new NumberSpan(min * span.min, max * span.max, total * span.total, Math.max(n, span.n));
      } else {
         double numVal = num.doubleValue();
         ret = new NumberSpan(min * numVal, max * numVal, total * numVal, Math.max(n, 1));
      }
      return ret;
   }
   
   public NumberSpan put(NumberSpan other) {
      NumberSpan ret;
      if (n == 0) {
         ret = new NumberSpan(other);
      } else if (other.n != 0) {
         ret = new NumberSpan(Math.min(min, other.min), Math.max(max, other.max), total + other.total, n + other.n);
      } else {
         ret = clone();
      }
      return ret;
   }
   
   public NumberSpan put(int value, float likelihood) {
      if (likelihood < 0f) {
         throw new IllegalArgumentException("Likelihood cannot be negative.");
      }
      NumberSpan ret;
      if (n == 0) {
         ret = new NumberSpan(value, value, value * likelihood, 1);
      } else {
         ret = new NumberSpan(Math.min(min, value), Math.max(max, value), total + value * likelihood, n + 1);
      }
      return ret;
   }
   
   @Override
   public final String toString() {
      String ret;
      double average = getAverage();
      String avgString;
      if (average == (long) average) {
         avgString = String.format("%d", (long) average);
      } else {
         avgString = String.format("%s", FORMAT.format(average));
      }
      if (min == max) {
         ret = avgString;
      } else {
         ret = String.format("[%s-%s] (%s)", FORMAT.format(getMinimum()), FORMAT.format(getMaximum()), avgString);
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
      long temp;
      temp = Double.doubleToLongBits(max);
      result = prime * result + (int) (temp ^ temp >>> 32);
      temp = Double.doubleToLongBits(min);
      result = prime * result + (int) (temp ^ temp >>> 32);
      result = prime * result + n;
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
         ret = Double.compare(max, o.max);
         if (ret == 0) {
            ret = Double.compare(min, o.min);
         }
      }
      return ret;
   }
}
