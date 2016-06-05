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

import java.util.Collection;
import java.util.List;

import shuffle.fwk.data.Board;
import shuffle.fwk.data.Effect;
import shuffle.fwk.data.Species;
import shuffle.fwk.data.simulation.SimulationState;
import shuffle.fwk.data.simulation.SimulationTask;

public class ActivateComboEffect extends ComboEffect {
   
   /**
    * The number of combos activated before this one in the current chain.
    */
   private Integer numClearedOnActivation = null;
   
   private boolean[] shouldPersist;
   
   public ActivateComboEffect(ActivateComboEffect activateComboEffect) {
      super(activateComboEffect);
      numClearedOnActivation = activateComboEffect.numClearedOnActivation;
      shouldPersist = new boolean[Board.NUM_CELLS];
   }
   
   public ActivateComboEffect(List<Integer> combo, boolean isPersistentEffect) {
      super(combo, isPersistentEffect);
      shouldPersist = new boolean[Board.NUM_CELLS];
   }
   
   protected final void setNumClearedFrom(SimulationTask task) {
      if (numClearedOnActivation == null) {
         numClearedOnActivation = task.getState().getCurrentChainCount();
      }
   }
   
   public int getNumCombosOnActivate() {
      return numClearedOnActivation == null ? 0 : numClearedOnActivation;
   }
   
   @Override
   public final void doEffect(SimulationTask task) {
      setNumClearedFrom(task);
      Species s = task.getEffectSpecies(getCoords());
      Effect effect = task.getEffectFor(s);
      if (isClaimedIn(task)) { // only happens on the very FIRST activation
         task.removeClaim(this);
         task.addActiveFor(this);
         task.getState().addCombosCleared(1);
         if (effect.isPersistent()) { // If the effect is active, then it needs to remove colliding
                                      // effects
            // For example: mega gengar will not combo in a plus pattern, ever. As soon as the combo
            // is recognized it will
            // immediately remove the colliding combo of a lesser rank.
            task.removeCollisions(getCoords());
         }
      }
      if (isAllFrozen(task)) {
         task.removeActive(this);
         task.unfreezeAt(getCoords());
      } else {
         setPersistence(task);
         effect.handleCombo(this, task);
      }
   }
   
   private void setPersistence(SimulationTask task) {
      List<Integer> coords = super.getCoords();
      SimulationState state = task.getState();
      Board b = state.getBoard();
      for (int i = 0; i * 2 + 1 < coords.size(); i++) {
         int row = coords.get(i * 2);
         int col = coords.get(i * 2 + 1);
         boolean isFrozen = b.isFrozenAt(row, col);
         boolean isClaimed = task.isClaimed(row, col);
         shouldPersist[getPosition(row, col)] = isFrozen || isClaimed;
         if (isFrozen) {
            b.setFrozenAt(row, col, false);
            state.addDisruptionCleared(1);
         }
      }
   }
   
   public boolean isPersistent(int row, int col) {
      int pos = getPosition(row, col);
      if (pos >= 0 && pos < Board.NUM_CELLS) {
         return shouldPersist[getPosition(row, col)];
      }
      return false;
   }
   
   /**
    * @param row
    * @param col
    * @return
    */
   private int getPosition(int row, int col) {
      return col + row * 6 - 7;
   }
   
   private boolean isClaimedIn(SimulationTask task) {
      boolean claimed = false;
      List<Integer> coords = getCoords();
      for (int i = 0; !claimed && i * 2 + 1 <= coords.size(); i++) {
         int row = coords.get(i * 2);
         int col = coords.get(i * 2 + 1);
         Collection<ActivateComboEffect> claims = task.getClaimsFor(row, col);
         claimed |= claims.contains(this);
      }
      return claimed;
   }
   
   @Override
   public String toString() {
      return "Activate " + super.toString();
   }
   
   @Override
   public boolean equals(Object obj) {
      boolean equal = super.equals(obj) && obj instanceof ActivateComboEffect;
      if (equal) {
         ActivateComboEffect other = (ActivateComboEffect) obj;
         equal &= other.getNumCombosOnActivate() == getNumCombosOnActivate();
      }
      return equal;
   }
}