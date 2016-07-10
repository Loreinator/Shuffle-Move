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

import java.util.List;

import shuffle.fwk.data.Board;
import shuffle.fwk.data.simulation.SimulationState;
import shuffle.fwk.data.simulation.SimulationTask;

/**
 * @author Andrew Meyers
 *
 */
public class EraseComboEffect extends ComboEffect {
   
   private boolean forceErase;
   
   private boolean[] shouldErase;
   
   public EraseComboEffect(List<Integer> combo) {
      super(combo);
      shouldErase = new boolean[Board.NUM_CELLS];
   }
   
   public void setForceErase(boolean forceErase) {
      this.forceErase = forceErase;
   }
   
   public boolean isForceErase() {
      return forceErase;
   }
   
   /*
    * (non-Javadoc)
    * @see
    * shuffle.fwk.data.simulation.effects.SimulationEffect#doEffect(shuffle.fwk.data.simulation.
    * SimulationTask)
    */
   @Override
   public void doEffect(SimulationTask task) {
      task.completeComboFor(this);
   }
   
   @Override
   public String toString() {
      return "Erase " + super.toString();
   }
   
   @Override
   public void init(SimulationTask task) {
      List<Integer> coords = super.getCoords();
      SimulationState state = task.getState();
      Board b = state.getBoard();
      for (int i = 0; i * 2 + 1 < coords.size(); i++) {
         int row = coords.get(i * 2);
         int col = coords.get(i * 2 + 1);
         boolean isFrozen = b.isFrozenAt(row, col);
         shouldErase[getPosition(row, col)] = !isFrozen;
         if (isFrozen) {
            b.setFrozenAt(row, col, false);
            state.addDisruptionCleared(1);
         }
      }
   }
   
   public void inheritPersistenceFrom(ActivateComboEffect effect) {
      List<Integer> coords = super.getCoords();
      for (int i = 0; i * 2 + 1 < coords.size(); i++) {
         int row = coords.get(i * 2);
         int col = coords.get(i * 2 + 1);
         boolean shouldPersist = effect.isPersistent(row, col);
         int pos = getPosition(row, col);
         if (pos >= 0 && pos < Board.NUM_CELLS) {
            shouldErase[pos] = shouldErase[pos] && !shouldPersist;
         }
      }
   }

   /**
    * @param row
    * @param col
    * @return
    */
   private int getPosition(int row, int col) {
      return col + row * 6 - 7;
   }
   
   /**
    * @param row
    * @param col
    * @return
    */
   public boolean shouldErase(int row, int col) {
      return forceErase || shouldErase[getPosition(row, col)];
   }
}
