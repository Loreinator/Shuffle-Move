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

package shuffle.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import shuffle.test.fwk.config.manager.DataIntegrityTest;
import shuffle.test.fwk.data.BoardTest;
import shuffle.test.fwk.data.PkmTypeTest;
import shuffle.test.fwk.data.SpeciesTest;
import shuffle.test.fwk.data.simulation.SimulationTaskTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({ // Make sure this includes all test classes
      BoardTest.class, PkmTypeTest.class, SpeciesTest.class, SimulationTaskTest.class, DataIntegrityTest.class })
public class TestAll {
   
}
