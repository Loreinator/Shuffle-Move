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

import java.util.Arrays;
import java.util.List;

import shuffle.fwk.data.Effect;
import shuffle.fwk.data.PkmType;
import shuffle.fwk.data.Species;
import shuffle.fwk.data.Stage;
import shuffle.fwk.data.simulation.SimulationFeeder;

/**
 * @author Andrew Meyers
 *
 */
public class SimulationBenchmarks {
   
   private static final int N = 100;
   private static final int MAX = 20000;
   
   public static void main(String[] args) {
      testFeederProduction();
      
   }
   
   /**
	 * 
	 */
   private static void testFeederProduction() {
      Stage stage = null;
      List<Species> blocks = Arrays.asList(Species.AIR, new Species("Wood", 0, PkmType.WOOD, Effect.NONE));
      for (int height = 0; height <= MAX; height += 200) {
         long t1 = System.nanoTime();
         SimulationFeeder.getFeedersFor(height, stage, blocks, N);
         long t2 = System.nanoTime();
         System.out.println(String.format("%d,%d,%d", height * N, t2 - t1, (t2 - t1) / Math.max(1, height * N)));
      }
   }
   
}
