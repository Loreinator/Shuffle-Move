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
import shuffle.fwk.data.simulation.util.NumberSpanImpl;

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
      
      NumberSpanImpl score = new NumberSpanImpl();
      NumberSpanImpl gold = new NumberSpanImpl();
      NumberSpanImpl blocks = new NumberSpanImpl();
      NumberSpanImpl disrupts = new NumberSpanImpl();
      NumberSpanImpl combos = new NumberSpanImpl();
      NumberSpanImpl progress = new NumberSpanImpl();
      
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
         
         score.put(state.getScore(), weight);
         gold.put(state.getGold(), weight);
         blocks.put(state.getBlocksCleared(), weight);
         disrupts.put(state.getDisruptionsCleared(), weight);
         combos.put(state.getCombosCleared(), weight);
         progress.put(state.getMegaProgress(), weight);
         
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
