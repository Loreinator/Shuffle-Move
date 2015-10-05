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

package shuffle.test.fwk.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

import org.junit.Before;
import org.junit.Test;

import shuffle.fwk.config.ConfigFactory;
import shuffle.fwk.config.manager.SpeciesManager;
import shuffle.fwk.data.Board;
import shuffle.fwk.data.PkmType;
import shuffle.fwk.data.Species;

/**
 * @author Andrew Meyers
 *
 */
public class BoardTest {
   private static Random rand = new Random();
   private static SpeciesManager mngr = new ConfigFactory().getSpeciesManager();
   
   /**
    * @throws java.lang.Exception
    */
   @Before
   public void setUp() throws Exception {
   }
   
   /**
    * Test method for {@link shuffle.fwk.data.Board#Board()}.
    */
   @Test
   public final void testBoard() {
      Board b = new Board();
      for (int row = 1; row <= Board.NUM_ROWS; row++) {
         for (int col = 1; col <= Board.NUM_COLS; col++) {
            assertFalse("checking freeze state for " + row + "," + col, b.isFrozenAt(row, col));
            assertEquals("checking species for " + row + "," + col, Species.AIR, b.getSpeciesAt(row, col));
         }
      }
   }
   
   /**
    * Test method for {@link shuffle.fwk.data.Board#Board(shuffle.fwk.data.Board)}.
    */
   @Test
   public final void testBoardBoard() {
      Board b = getRandomBoard();
      assertEquals(b, new Board(b));
   }
   
   public static Board getRandomBoard() {
      Board ret = new Board();
      for (int row = 1; row <= Board.NUM_ROWS; row++) {
         for (int col = 1; col <= Board.NUM_COLS; col++) {
            if (rand.nextFloat() > 0.2) {
               ret.setSpeciesAt(row, col, getRandomSpecies());
               if (rand.nextFloat() > 0.5) {
                  ret.setFrozenAt(row, col, true);
               }
            }
         }
      }
      return ret;
   }
   
   public static Species getRandomSpecies() {
      PkmType t = PkmType.values()[rand.nextInt(PkmType.values().length)];
      Predicate<Species> filter = species -> species.getType().equals(t);
      List<Species> options = new ArrayList<Species>(mngr.getSpeciesByFilters(Arrays.asList(filter)));
      if (options.isEmpty()) {
         options = Arrays.asList(Species.AIR);
      }
      return options.get(rand.nextInt(options.size()));
   }
   
   public int[] getRandomCoords() {
      return new int[] { rand.nextInt(Board.NUM_ROWS) + 1, rand.nextInt(Board.NUM_COLS) + 1 };
   }
   
   /**
    * Test method for {@link shuffle.fwk.data.Board#getSpeciesAt(int, int)}.
    */
   @Test
   public final void testGetSpeciesAt() {
      Board b = new Board();
      for (int row = 1; row <= Board.NUM_ROWS; row++) {
         for (int col = 1; col <= Board.NUM_COLS; col++) {
            assertEquals("Needs to default to all air.", Species.AIR, b.getSpeciesAt(row, col));
         }
      }
      b.setAllFrozen(true);
      for (int i = 0; i < 1000; i++) { // Repeat 1000 times
         int[] coord = getRandomCoords();
         assertFalse(String.format("Failed with coords of [%d,%d] and board: %s", coord[0], coord[1], b.toString()),
               b.isFrozenAt(coord[0], coord[1]));
      }
   }
   
   /**
    * Test method for
    * {@link shuffle.fwk.data.Board#setSpeciesAt(int, int, shuffle.fwk.data.Species)}.
    */
   @Test
   public final void testSetSpeciesAt() {
      Board b = getRandomBoard();
      for (int i = 0; i < 1000; i++) { // Repeat 1000 times
         int[] coord = getRandomCoords();
         Species s = getRandomSpecies();
         b.setSpeciesAt(coord[0], coord[1], s);
         assertEquals(s, b.getSpeciesAt(coord[0], coord[1]));
      }
      b.setAllFrozen(true);
      for (int i = 0; i < 1000; i++) { // Repeat 1000 times
         int[] coord = getRandomCoords();
         b.setSpeciesAt(coord[0], coord[1], Species.AIR);
         assertFalse(String.format("Failed with coords of [%d,%d] and board: %s", coord[0], coord[1], b.toString()),
               b.isFrozenAt(coord[0], coord[1]));
      }
   }
   
   /**
    * Test method for {@link shuffle.fwk.data.Board#isFrozenAt(int, int)}.
    */
   @Test
   public final void testIsFrozenAt() {
      Board b = new Board();
      for (int row = 1; row <= Board.NUM_ROWS; row++) {
         for (int col = 1; col <= Board.NUM_COLS; col++) {
            assertFalse("incorrectly frozen by default.", b.isFrozenAt(row, col));
            b.setSpeciesAt(row, col, mngr.getSpeciesByName("Wood"));
            b.setFrozenAt(row, col, true);
            assertTrue("incorrectly thawed after intsertion and freeze.", b.isFrozenAt(row, col));
         }
      }
      
   }
   
   /**
    * Test method for {@link shuffle.fwk.data.Board#setFrozenAt(int, int, Boolean)}.
    */
   @Test
   public final void testSetFrozenAt() {
      Board b = getRandomBoard();
      for (int row = 1; row <= Board.NUM_ROWS; row++) {
         for (int col = 1; col <= Board.NUM_COLS; col++) {
            if (b.getSpeciesAt(row, col).equals(Species.AIR)) {
               continue;
            }
            boolean before = b.isFrozenAt(row, col);
            b.setFrozenAt(row, col, !before);
            assertFalse(String.format("Failed with coords of [%d,%d] and board: %s", row, col, b.toString()),
                  before == b.isFrozenAt(row, col));
         }
      }
   }
   
   /**
    * Test method for {@link shuffle.fwk.data.Board#setAllFrozen(Boolean)}.
    */
   @Test
   public final void testSetAllFrozen() {
      Board b = getRandomBoard();
      boolean[][] beforeFrozen = new boolean[Board.NUM_ROWS][Board.NUM_COLS];
      for (int row = 1; row <= Board.NUM_ROWS; row++) {
         for (int col = 1; col <= Board.NUM_COLS; col++) {
            beforeFrozen[row - 1][col - 1] = b.isFrozenAt(row, col);
         }
      } // initial state obtained.
      b.setAllFrozen(false);
      for (int row = 1; row <= Board.NUM_ROWS; row++) {
         for (int col = 1; col <= Board.NUM_COLS; col++) {
            assertFalse(b.isFrozenAt(row, col));
         }
      }
      b.setAllFrozen(true);
      for (int row = 1; row <= Board.NUM_ROWS; row++) {
         for (int col = 1; col <= Board.NUM_COLS; col++) {
            if (b.getSpeciesAt(row, col).equals(Species.AIR)) {
               continue;
            }
            assertTrue(b.isFrozenAt(row, col));
         }
      }
      b.setAllFrozen(false);
      for (int row = 1; row <= Board.NUM_ROWS; row++) {
         for (int col = 1; col <= Board.NUM_COLS; col++) {
            assertFalse(b.isFrozenAt(row, col));
         }
      }
   }
   
   /**
    * Test method for {@link shuffle.fwk.data.Board#clear()}.
    */
   @Test
   public final void testClear() {
      Board b = new Board();
      for (int row = 1; row <= Board.NUM_ROWS; row++) {
         for (int col = 1; col <= Board.NUM_COLS; col++) {
            assertFalse(b.isFrozenAt(row, col));
            assertEquals(Species.AIR, b.getSpeciesAt(row, col));
         }
      }
   }
   
}
