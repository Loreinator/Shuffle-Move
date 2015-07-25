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
import java.util.List;
import java.util.concurrent.RecursiveTask;

/**
 * @author Andrew Meyers
 *
 */
@SuppressWarnings("serial")
public class SimulationCreationTask extends RecursiveTask<Collection<SimulationTask>> {
   
   private final Collection<SimulationFeeder> feeders;
   private final List<Integer> move;
   private final SimulationCore simulationCore;
   
   public SimulationCreationTask(SimulationCore simulationCore, List<Integer> move, Collection<SimulationFeeder> feeders) {
      this.simulationCore = simulationCore;
      this.move = move;
      this.feeders = feeders;
   }
   
   @Override
   protected Collection<SimulationTask> compute() {
      Collection<SimulationTask> ret = new ArrayList<SimulationTask>(feeders.size());
      for (SimulationFeeder feeder : feeders) {
         SimulationTask task = new SimulationTask(simulationCore, move, feeder);
         task.fork();
         ret.add(task);
      }
      return ret;
   }
   
}
