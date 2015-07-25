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

package shuffle.fwk.data.simulation.effects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import shuffle.fwk.data.Species;

/**
 * @author Andrew Meyers
 *
 */
public class ActivateMegaComboEffect extends ActivateComboEffect {
   
   private int value = 0;
   private final LinkedList<List<Integer>> plans;
   private Species targetSpecies = null;
   
   public ActivateMegaComboEffect(ActivateComboEffect other) {
      super(other);
      plans = new LinkedList<List<Integer>>();
      if (other instanceof ActivateMegaComboEffect) {
         ActivateMegaComboEffect otherMegaEffect = (ActivateMegaComboEffect) other;
         setTargetSpecies(otherMegaEffect.getTargetSpecies());
         setInt(otherMegaEffect.getInt());
         for (List<Integer> plan : otherMegaEffect.plans) {
            addPlannedOptions(plan);
         }
      }
   }
   
   public Species getTargetSpecies() {
      return targetSpecies;
   }
   
   public void setTargetSpecies(Species target) {
      targetSpecies = target;
   }
   
   public int getInt() {
      return value;
   }
   
   public void setInt(int v) {
      value = v;
   }
   
   public boolean hasPlan() {
      return !plans.isEmpty();
   }
   
   public List<Integer> getNextPlan() {
      return plans.poll();
   }
   
   public void addPlannedOptions(List<Integer> options) {
      plans.offer(new ArrayList<Integer>(options));
   }
   
   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder(super.toString());
      sb.append(" value: ");
      sb.append(String.valueOf(getInt()));
      sb.append(" plans: ");
      sb.append(Arrays.deepToString(plans.toArray()));
      return sb.toString();
   }
   
   @Override
   public boolean equals(Object obj) {
      boolean equal = super.equals(obj) && obj instanceof ActivateMegaComboEffect;
      if (equal) {
         ActivateMegaComboEffect other = (ActivateMegaComboEffect) obj;
         equal &= other.value == value;
         equal &= plans.equals(other.plans);
      }
      return equal;
   }
}
