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
import java.util.List;

import shuffle.fwk.data.simulation.SimulationTask;

/**
 * @author Andrew Meyers
 *
 */
public abstract class ComboEffect implements SimulationEffect {
   
   private List<Integer> toCombo;
   private int priority = 0;
   private boolean isHorizontal;
   
   public ComboEffect(ComboEffect effect) {
      toCombo = new ArrayList<Integer>(effect.toCombo);
      priority = effect.priority;
      isHorizontal = effect.isHorizontal;
   }
   
   public ComboEffect(List<Integer> combo, boolean isPersistentEffect) {
      toCombo = combo;
      int minRow = combo.get(0);
      int minCol = combo.get(1);
      int maxCol = combo.get(1);
      for (int i = 0; i * 2 + 1 < combo.size(); i++) {
         int row = combo.get(i * 2);
         int col = combo.get(i * 2 + 1);
         if (row < minRow) {
            minRow = row;
         }
         if (col < minCol) {
            minCol = col;
         } else if (col > maxCol) {
            maxCol = col;
         }
      }
      isHorizontal = minCol != maxCol;
      
      // priority - lower number = happens earlier, higher = happens later
      
      // if the effect is an active one, it will have a lower priority, even more important than
      // combo size
      // (This need to be confirmed in how important they are)
      priority += (isPersistentEffect ? 1 : 0) * 10000;
      // Size is very important
      priority += (6 - getNumBlocks()) * 1000;
      // Horizontal matches are next
      priority += isHorizontal ? 0 : 100;
      // Finally, the grid position of their upper left corner.
      if (isHorizontal) {
         priority += minCol - 1 + 6 * (minRow - 1);
      } else { // in vertical, the order is flipped for priority
         priority += minRow - 1 + 6 * (minCol - 1);
      }
   }
   
   public boolean containsCoords(int rowToFind, int colToFind) {
      for (int i = 0; i * 2 + 1 < toCombo.size(); i++) {
         int row = toCombo.get(i * 2);
         int col = toCombo.get(i * 2 + 1);
         if (row == rowToFind && col == colToFind) {
            return true;
         }
      }
      return false;
   }
   
   public List<Integer> getCoords() {
      return toCombo;
   }
   
   public int getPriority() {
      return priority;
   }
   
   public boolean isHorizontal() {
      return isHorizontal;
   }
   
   public int getNumBlocks() {
      return toCombo.size() / 2;
   }
   
   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("Combo Effect, Priority ");
      sb.append(String.valueOf(getPriority()));
      sb.append(", ");
      sb.append(Arrays.deepToString(toCombo.toArray()));
      return sb.toString();
   }
   
   /*
    * (non-Javadoc)
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + (isHorizontal ? 1231 : 1237);
      result = prime * result + priority;
      result = prime * result + (toCombo == null ? 0 : toCombo.hashCode());
      return result;
   }
   
   /*
    * (non-Javadoc)
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj) {
      boolean equal = obj != null && obj instanceof ComboEffect;
      if (equal) {
         ComboEffect other = (ComboEffect) obj;
         equal &= priority == other.priority;
         equal &= toCombo == other.toCombo || toCombo != null && toCombo.equals(other.toCombo);
      }
      return equal;
   }
   
   /**
    * @param simulationTask
    */
   public void init(SimulationTask simulationTask) {
      // Empty Base implementation, used by EraseComboEffect to set itself up
   }
}
