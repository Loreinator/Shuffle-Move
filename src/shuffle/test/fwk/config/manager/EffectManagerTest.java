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
package shuffle.test.fwk.config.manager;

import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import shuffle.fwk.config.ConfigFactory;
import shuffle.fwk.config.manager.EffectManager;
import shuffle.fwk.data.Effect;

/**
 * @author Andrew Meyers
 *
 */
public class EffectManagerTest {
   
   /**
    * @throws java.lang.Exception
    */
   @BeforeClass
   public static void setUpBeforeClass() throws Exception {
   }
   
   /**
    * @throws java.lang.Exception
    */
   @AfterClass
   public static void tearDownAfterClass() throws Exception {
   }
   
   /**
    * Test method for {@link shuffle.fwk.config.manager.EffectManager#getOdds(shuffle.fwk.data.Effect, int)}.
    */
   @Test
   public final void testGetOdds() {
      EffectManager manager = new ConfigFactory().getEffectManager();
      for (Effect e : Effect.values()) {
         List<Double> odds = new ArrayList<Double>();
         for (int i = 0; i <= 3; i++) {
            manager.getOddsMap();
            odds.add(manager.getOdds(e, i + 3, 1));
         }
         System.out.printf("Effect %s has a string value of: %s. The odds are registered as: %s%n", e,
               manager.getStringValue(e.toString()), odds.toString());
      }
   }
   
}
