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
package shuffle.test.fwk.data.simulation;

import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.UUID;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import shuffle.fwk.ShuffleController;
import shuffle.fwk.config.EntryType;
import shuffle.fwk.data.Stage;
import shuffle.fwk.data.simulation.SimulationCore;
import shuffle.fwk.data.simulation.SimulationResult;

/**
 * @author Andrew Meyers
 *
 */
public class SimulationCoreTest {
   
   private static final String KEY_NUM_FEEDERS = "NUM_FEEDERS";
   private static final String KEY_AUTO_COMPUTE = "AUTO_COMPUTE";
   private static ShuffleController controller;
   

   /**
    * @throws java.lang.Exception
    */
   @BeforeClass
   public static void setUpBeforeClass() throws Exception {
      controller = new ShuffleController();
      controller.getPreferencesManager().setEntry(EntryType.BOOLEAN, KEY_AUTO_COMPUTE, false);
   }
   
   @Before
   public void setUpBefore() throws Exception {
      controller.getPreferencesManager().setEntry(EntryType.INTEGER, KEY_NUM_FEEDERS, 1);
   }
   
   /**
    * Test method for {@link shuffle.fwk.data.simulation.SimulationCore#computeWithoutMove()}.
    */
   @Test
   public final void testComputeWithoutMove() {
      Stage stage = controller.getModel().getStageManager().getStageValue("001");
      testComputeWithoutMoveForStage(stage);
   }
   
   public final void testComputeWithoutMoveForStage(Stage stage) {
      assertTrue("Stage 001 is missing.", stage != null);
      controller.setCurrentStage(stage);
      controller.loadDefaultGrid();
      for (int i = 1; i < 5000; i++) {
         controller.getPreferencesManager().setEntry(EntryType.INTEGER, KEY_NUM_FEEDERS, i);
         SimulationCore core = new SimulationCore(controller, UUID.randomUUID());
         Collection<SimulationResult> results = core.computeWithoutMove();
         assertTrue("For Value of " + i + ", Stage 001 in the default configuration needs to always be settled.",
               results == null);
         if (i % 100 == 0) {
            System.out.println("all ok for " + i);
         }
      }
   }
   
}
