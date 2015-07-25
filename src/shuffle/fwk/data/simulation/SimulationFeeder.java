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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.UUID;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;

import shuffle.fwk.data.Board;
import shuffle.fwk.data.Species;
import shuffle.fwk.data.Stage;

/**
 * @author Andrew Meyers
 *
 */
public class SimulationFeeder {
   
   private static final Random RAND = new Random(System.nanoTime());
   
   private List<Queue<Species>> feederQueue;
   private int[] sizes = new int[] { 0, 0, 0, 0, 0, 0 };
   private final UUID feederID;
   
   public SimulationFeeder() {
      this(0);
   }
   
   public SimulationFeeder(int height) {
      init();
      feederID = UUID.randomUUID();
   }
   
   public SimulationFeeder(SimulationFeeder other) {
      if (other != null && other.feederQueue != null) {
         copyFrom(other);
      } else {
         init();
      }
      feederID = UUID.randomUUID();
   }
   
   public UUID getID() {
      return feederID;
   }
   
   /**
    * @param other
    */
   private void copyFrom(SimulationFeeder other) {
      if (other != null && other.feederQueue != null) {
         int size = other.feederQueue.size();
         feederQueue = new ArrayList<Queue<Species>>(size);
         int i = 0;
         for (Queue<Species> column : other.feederQueue) {
            feederQueue.add(new LinkedList<Species>(column));
            sizes[i++] = column.size();
         }
      }
   }
   
   /**
	 * 
	 */
   private void init() {
      feederQueue = new ArrayList<Queue<Species>>(6);
      for (int i = 1; i <= Board.NUM_COLS; i++) {
         feederQueue.add(new LinkedList<Species>());
         sizes[i - 1] = 0;
      }
   }
   
   /**
    * Adds the given collection of species, in sequence, to the given column's queue.
    * 
    * @param column
    * @param toAdd
    */
   private void addToQueue(int column, Collection<Species> toAdd) {
      for (Species s : toAdd) {
         addToQueue(column, s);
      }
   }
   
   private void addToQueue(int column, Species toAdd) {
      feederQueue.get(column - 1).add(toAdd);
      sizes[column - 1] += 1;
   }
   
   private int getQueueSize(int column) {
      return sizes[column - 1];
   }
   
   public boolean hasMore(int column) {
      return sizes[column - 1] > 0;
   }
   
   public Species pollColumn(int column) {
      return feederQueue.get(column - 1).poll();
   }
   
   /**
    * Gets the possible feeders that could arise from the given collection of queue options for the
    * specified column, for this SimulationFeeder.
    * 
    * @param column
    * @param possibles
    * @return
    */
   private Collection<SimulationFeeder> getPossibleFeeders(int column, Collection<Queue<Species>> possibles) {
      Collection<SimulationFeeder> ret = new HashSet<SimulationFeeder>();
      for (Queue<Species> possible : possibles) {
         SimulationFeeder possibleFeeder = new SimulationFeeder(this);
         possibleFeeder.addToQueue(column, possible);
         ret.add(possibleFeeder);
      }
      return ret;
   }
   
   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder();
      for (int i = 1; i <= Board.NUM_COLS; i++) {
         sb.append("\nFeederRow: ");
         sb.append(Integer.toString(i));
         for (Species s : feederQueue.get(i - 1)) {
            sb.append(" ");
            sb.append(s.getName());
            sb.append(", ");
         }
      }
      return sb.toString();
   }
   
   /**
    * Gets the feeders for the given minimum height, stage, possible blocks, and the preferred
    * number of feeders. Note: the number of feeders actually returned will be equal to: <code>
    * Math.max(1, product of preferredCount / stage column possibilities ) * stage column possibilities.
    * </code> This means that the number produced will be at minimum the number possible
    * combinations from the given stage. But it may be at maximum slightly less than double the
    * given preferred count.
    * 
    * @param minHeight
    * @param stage
    * @param possibleBlocks
    * @param preferredCount
    * @return
    */
   public static Collection<SimulationFeeder> getFeedersFor(int minHeight, Stage stage,
         Collection<Species> possibleBlocks, int preferredCount) {
      Collection<SimulationFeeder> ret = new HashSet<SimulationFeeder>(Arrays.asList(new SimulationFeeder(minHeight)));
      Collection<SimulationFeeder> temp = new HashSet<SimulationFeeder>();
      ArrayList<Species> possibleBlockList = new ArrayList<Species>(possibleBlocks);
      if (stage != null) {
         for (int i = 1; i <= Board.NUM_COLS; i++) {
            temp = new HashSet<SimulationFeeder>(ret.size() * 3);
            Collection<Queue<Species>> dropPatterns = stage.getDropPatterns(i);
            if (dropPatterns.size() > 0) {
               for (SimulationFeeder simFeeder : ret) {
                  temp.addAll(simFeeder.getPossibleFeeders(i, dropPatterns));
               }
            } else {
               temp.addAll(ret);
            }
            ret = temp;
         }
      }
      int numPermutations = Math.max(1, preferredCount / ret.size());
      temp = new HashSet<SimulationFeeder>(numPermutations * ret.size());
      for (SimulationFeeder simFeeder : ret) {
         for (int i = 0; i < numPermutations; i++) {
            temp.add(fillToLevel(simFeeder, minHeight, possibleBlockList));
         }
      }
      return temp;
   }
   
   /**
    * Fills each column for the specified simFeeder to a minimum height as specified. The blocks
    * used will be randomly chosen from the given ArrayList.
    * 
    * @param simFeeder
    * @param minHeight
    * @param possibleBlocks
    * @return
    */
   private static SimulationFeeder fillToLevel(SimulationFeeder simFeeder, int minHeight,
         ArrayList<Species> possibleBlocks) {
      SimulationFeeder ret = new SimulationFeeder(simFeeder);
      if (minHeight == 0 || possibleBlocks.isEmpty()) {
         return ret;
      }
      // Using IntStream from random to streamline the performance a little bit.
      int[] numToAdd = new int[Board.NUM_COLS];
      int sum = 0;
      for (int i = 1; i <= Board.NUM_COLS; i++) {
         numToAdd[i - 1] = Math.max(0, minHeight - ret.getQueueSize(i));
         sum += numToAdd[i - 1];
      }
      IntStream is = RAND.ints(sum, 0, possibleBlocks.size());
      is.forEachOrdered(new IntConsumer() {
         int curCol = 1;
         
         @Override
         public void accept(int arg0) {
            while (curCol <= Board.NUM_COLS && numToAdd[curCol - 1] <= 0) {
               curCol++;
            }
            if (curCol <= Board.NUM_COLS) {
               ret.addToQueue(curCol, possibleBlocks.get(arg0));
               numToAdd[curCol - 1]--;
            }
         }
      });
      return ret;
   }
   
}
