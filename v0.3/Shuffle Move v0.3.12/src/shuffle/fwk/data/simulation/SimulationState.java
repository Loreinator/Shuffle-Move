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

import shuffle.fwk.data.Board;
import shuffle.fwk.data.Species;

/**
 * @author Andrew Meyers
 *
 */
public class SimulationState {
   
   public static final int MEGA_PROGRESS_LIMIT = 10;
   public static final int MAX_FALL_POSITION = 15;
   public static final int FALL_DISTANCE = MAX_FALL_POSITION + 1;
   
   private final SimulationCore core;
   private final SimulationFeeder simFeeder;
   private final Board board;
   private float curWeight;
   private int score;
   private int blocksCleared = 0;
   private int disruptionsCleared = 0;
   private int numCombos = 0;
   private int gold;
   
   private boolean[][] falling = new boolean[Board.NUM_ROWS][Board.NUM_COLS];
   private int[][] fallPosition = new int[Board.NUM_ROWS][Board.NUM_COLS];
   private boolean[][] original = new boolean[Board.NUM_ROWS][Board.NUM_COLS];
   
   private int fallingCount = 0;
   
   /**
    * Creates a new SimulationState.
    * 
    * @param simCore
    *           The core
    * @param feeder
    *           The feeder, a copy is used.
    * @param b
    *           The Board, a copy is used.
    * @param weight
    *           The weight of this simulation
    * @param progress
    *           The progress towards a Mega Evolution
    * @param curScore
    *           The score up until now
    * @param originality
    *           The grid of booleans of [1, Board.NUM_ROWS]x[1,Board.NUM_COLS] dimensions defining
    *           the originality of each block (should it be included in the result board)
    */
   public SimulationState(SimulationCore simCore, SimulationFeeder feeder, Board b, float weight, int curScore,
         int curGold, boolean[][] originality) {
      core = simCore;
      simFeeder = new SimulationFeeder(feeder);
      board = new Board(b);
      curWeight = weight;
      score = curScore;
      gold = curGold;
      for (int row = 1; row <= Board.NUM_ROWS; row++) {
         for (int col = 1; col <= Board.NUM_COLS; col++) {
            original[row - 1][col - 1] = originality[row - 1][col - 1];
         }
      }
   }
   
   /**
    * Creates a new SimulationState using the given object's values, according to
    * {@link #SimulationState(SimulationCore, SimulationFeeder, Board, float, int, int, boolean[][])}
    * .
    * 
    * @param other
    *           The other simulation state to copy from
    */
   public SimulationState(SimulationState other) {
      this(other.getCore(), other.getFeeder(), other.getBoard(), other.getWeight(), other.getScore(), other.getGold(),
            other.original);
      for (int row = 0; row < Board.NUM_ROWS; row++) {
         for (int col = 0; col < Board.NUM_COLS; col++) {
            falling[row][col] = other.falling[row][col];
            fallPosition[row][col] = other.fallPosition[row][col];
         }
      }
      fallingCount = other.fallingCount;
      blocksCleared = other.blocksCleared;
      disruptionsCleared = other.disruptionsCleared;
      numCombos = other.numCombos;
   }
   
   public SimulationCore getCore() {
      return core;
   }
   
   public float getWeight() {
      return curWeight;
   }
   
   public void setWeight(float newWeight) {
      curWeight = newWeight;
   }
   
   public int getScore() {
      return score;
   }
   
   public int getGold() {
      return gold;
   }
   
   public int getDisruptionsCleared() {
      return disruptionsCleared;
   }
   
   public int getBlocksCleared() {
      return blocksCleared;
   }
   
   public int getCombosCleared() {
      return numCombos;
   }
   
   /**
    * @return the board
    */
   public Board getBoard() {
      return board;
   }
   
   public Board getResultBoard() {
      Board ret = new Board(board);
      for (int row = 1; row <= Board.NUM_ROWS; row++) {
         for (int col = 1; col <= Board.NUM_COLS; col++) {
            if (!isOriginalAt(row, col)) {
               ret.setSpeciesAt(row, col, Species.AIR);
            }
         }
      }
      return ret;
   }
   
   public boolean isMegaActive() {
      return getCore().isMegaAllowed() && getCore().getMegaThreshold() <= getMegaProgress();
   }
   
   /**
    * @return the megaProgress
    */
   public int getMegaProgress() {
      return board.getMegaProgress();
   }
   
   public SimulationFeeder getFeeder() {
      return simFeeder;
   }
   
   public void addScore(int add) {
      score += Math.max(0, add);
   }
   
   public void addGold(int add) {
      gold += Math.max(0, add);
   }
   
   public void addBlockCleared(int add) {
      blocksCleared += Math.max(0, add);
   }
   
   public void addDisruptionCleared(int add) {
      disruptionsCleared += Math.max(0, add);
   }
   
   public void addCombosCleared(int add) {
      numCombos += Math.max(0, add);
   }
   
   /**
    * @param megaProgress
    *           the megaProgress to set
    */
   public void setMegaProgress(int megaProgress) {
      board.setMegaProgress(megaProgress);
   }
   
   public void increaseMegaProgress(int increaseBy) {
      setMegaProgress(Math.min(getCore().getMegaThreshold(), getMegaProgress() + increaseBy));
   }
   
   /**
    * Sets the given coordinates to the specified state of falling. If the state changed then this
    * will return true. This will return false otherwise.
    * 
    * @param row
    * @param column
    * @param fall
    * @return
    */
   public boolean setFallingAt(int row, int column, boolean fall) {
      if (row < 1 || row > Board.NUM_ROWS || column < 1 || column > Board.NUM_COLS) {
         return false; // Invalid coordinates
      }
      boolean changed = falling[row - 1][column - 1] != fall;
      if (changed) {
         falling[row - 1][column - 1] = fall;
         fallingCount += fall ? 1 : -1;
      }
      return changed;
   }
   
   public int getFallingPositionAt(int row, int column) {
      if (row < 1 || row > Board.NUM_ROWS || column < 1 || column > Board.NUM_COLS) {
         return 0; // Invalid coordinates
      }
      return fallPosition[row - 1][column - 1];
   }
   
   public boolean setFallingPositionAt(int row, int column, int position) {
      if (row < 1 || row > Board.NUM_ROWS || column < 1 || column > Board.NUM_COLS) {
         return false; // Invalid coordinates
      }
      if (position < 0) {
         position = 0;
      } else if (position > MAX_FALL_POSITION) {
         position = MAX_FALL_POSITION;
      }
      boolean changed = fallPosition[row - 1][column - 1] != position;
      if (changed) {
         fallPosition[row - 1][column - 1] = position;
      }
      return changed;
   }
   
   /**
    * Returns true if the given coordinates are in a state of falling.
    * 
    * @param row
    * @param column
    * @return
    */
   public boolean isFallingAt(int row, int column) {
      if (row < 1 || row > Board.NUM_ROWS || column < 1 || column > Board.NUM_COLS) {
         return false; // Invalid coordinates
      }
      return falling[row - 1][column - 1];
   }
   
   /**
    * Sets the given coordinates to the specified state of mid falling. If the state changed then
    * this will return true. This will return false otherwise.
    * 
    * @param row
    * @param column
    * @param fall
    * @return
    */
   public boolean decreaseFallAt(int row, int column) {
      boolean changed = isMidFallAt(row, column);
      if (changed) {
         fallPosition[row - 1][column - 1] -= 1;
      }
      return changed;
   }
   
   public boolean decreaseFallAt(int row, int column, int repeat) {
      boolean changed = isMidFallAt(row, column);
      if (changed) {
         int prevPosition = fallPosition[row - 1][column - 1];
         int newPosition = Math.max(0, prevPosition - repeat);
         fallPosition[row - 1][column - 1] = newPosition;
      }
      return changed;
   }
   
   /**
    * Returns true if the given coordinates are in a state of mid-fall.
    * 
    * @param row
    * @param column
    * @return
    */
   public boolean isMidFallAt(int row, int column) {
      if (row < 1 || row > Board.NUM_ROWS || column < 1 || column > Board.NUM_COLS) {
         return false; // Invalid coordinates
      }
      return fallPosition[row - 1][column - 1] > 0;
   }
   
   public int getFallingCount() {
      return fallingCount;
   }
   
   /**
    * Sets the given coordinates to the specified state of originality - that is, was this block
    * included in the initial board. If the state changed then this will return true. This will
    * return false otherwise.
    * 
    * @param row
    * @param column
    * @param fall
    * @return
    */
   public boolean setOriginalAt(int row, int column, boolean fall) {
      if (row < 1 || row > Board.NUM_ROWS || column < 1 || column > Board.NUM_COLS) {
         return false; // Invalid coordinates
      }
      boolean prev = original[row - 1][column - 1];
      original[row - 1][column - 1] = fall;
      return prev != fall;
   }
   
   /**
    * Returns true if the given coordinates contain a block that originally was in the board.
    * 
    * @param row
    * @param column
    * @return
    */
   public boolean isOriginalAt(int row, int column) {
      if (row < 1 || row > Board.NUM_ROWS || column < 1 || column > Board.NUM_COLS) {
         return false; // Invalid coordinates
      }
      return original[row - 1][column - 1];
   }
   
   public boolean swapTiles(int row1, int column1, int row2, int column2) {
      if (row1 == row2 && column1 == column2) {
         return false;
      }
      
      // First, get all the data that defines them
      Species s1 = getBoard().getSpeciesAt(row1, column1);
      Species s2 = getBoard().getSpeciesAt(row2, column2);
      
      boolean frozen1 = getBoard().isFrozenAt(row1, column1);
      boolean frozen2 = getBoard().isFrozenAt(row2, column2);
      
      boolean f1 = isFallingAt(row1, column1);
      boolean f2 = isFallingAt(row2, column2);
      
      int fp1 = getFallingPositionAt(row1, column1);
      int fp2 = getFallingPositionAt(row2, column2);
      
      boolean o1 = isOriginalAt(row1, column1);
      boolean o2 = isOriginalAt(row2, column2);
      
      boolean changed = false;
      // Then swap the data
      changed |= getBoard().setSpeciesAt(row1, column1, s2);
      changed |= getBoard().setSpeciesAt(row2, column2, s1);
      
      changed |= getBoard().setFrozenAt(row1, column1, frozen2);
      changed |= getBoard().setFrozenAt(row2, column2, frozen1);
      
      changed |= setFallingAt(row1, column1, f2);
      changed |= setFallingAt(row2, column2, f1);
      
      changed |= setFallingPositionAt(row1, column1, fp2);
      changed |= setFallingPositionAt(row2, column2, fp1);
      
      changed |= setOriginalAt(row1, column1, o2);
      changed |= setOriginalAt(row2, column2, o1);
      
      return changed;
      
   }
}
