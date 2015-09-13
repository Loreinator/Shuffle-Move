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

package shuffle.fwk.data.simulation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.RecursiveTask;

import shuffle.fwk.data.Board;
import shuffle.fwk.data.simulation.util.NumberSpan;

/**
 * @author Andrew Meyers
 *
 */
@SuppressWarnings("serial")
public class SimulationResultsAssembler extends RecursiveTask<SimulationResult> {
   
   private final List<Integer> move;
   private final UUID processUUID;
   private final Collection<SimulationTask> results;
   private final long startTime;
   
   public SimulationResultsAssembler(List<Integer> move, UUID processUUID, Collection<SimulationTask> results,
         long startTime) {
      this.move = move == null ? null : new ArrayList<Integer>(move);
      this.processUUID = processUUID;
      this.results = new ArrayList<SimulationTask>(results);
      this.startTime = startTime;
   }
   
   /*
    * (non-Javadoc)
    * @see java.util.concurrent.RecursiveTask#compute()
    */
   @Override
   protected SimulationResult compute() {
      if (results == null || results.isEmpty()) {
         return null;
      }
      
      NumberSpan score = new NumberSpan();
      NumberSpan gold = new NumberSpan();
      NumberSpan blocks = new NumberSpan();
      NumberSpan disrupts = new NumberSpan();
      NumberSpan combos = new NumberSpan();
      NumberSpan progress = new NumberSpan();
      
      // keeps track of all board chances, and the best one.
      Map<Board, Float> boardChances = new HashMap<Board, Float>();
      Board likelyBoard = null;
      
      for (SimulationTask task : results) {
         // get the state
         SimulationState state = task.join();
         if (state == null) {
            continue;
         }
         float weight = state.getWeight();
         
         score = score.put(state.getScore());
         gold = gold.put(state.getGold(), weight);
         blocks = blocks.put(state.getBlocksCleared(), weight);
         disrupts = disrupts.put(state.getDisruptionsCleared(), weight);
         combos = combos.put(state.getCombosCleared(), weight);
         progress = progress.put(state.getMegaProgress(), weight);
         
         // Process the board chances
         Board b = state.getResultBoard();
         float boardChance = weight;
         if (boardChances.containsKey(b)) {
            boardChance += boardChances.get(b);
         }
         boardChances.put(b, boardChance);
         if (likelyBoard == null || boardChances.get(b) > boardChances.get(likelyBoard)) {
            likelyBoard = b;
         }
      }
      SimulationResult result = null;
      if (likelyBoard != null) {
         result = new SimulationResult(move, likelyBoard, score, gold, progress, processUUID, blocks, disrupts, combos,
               startTime);
      }
      return result;
   }
   
}
