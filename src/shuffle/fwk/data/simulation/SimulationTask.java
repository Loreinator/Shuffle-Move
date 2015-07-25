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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.RecursiveTask;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;

import shuffle.fwk.data.Board;
import shuffle.fwk.data.Effect;
import shuffle.fwk.data.PkmType;
import shuffle.fwk.data.Species;
import shuffle.fwk.data.simulation.effects.ActivateComboEffect;
import shuffle.fwk.data.simulation.effects.ActivateMegaComboEffect;
import shuffle.fwk.data.simulation.effects.ComboEffect;
import shuffle.fwk.data.simulation.effects.EraseComboEffect;
import shuffle.fwk.data.simulation.util.TriFunction;

/**
 * @author Andrew Meyers
 *
 */
public class SimulationTask extends RecursiveTask<SimulationState> {
   private static final long serialVersionUID = -7639294565196247487L;
   private static final Logger LOG = Logger.getLogger(SimulationTask.class.getName());
   private static boolean logFiner = false;
   /**
    * All sims will terminate if their curTimeStamp reaches this frame count.
    */
   private static final int SIM_TIMEOUT = 1000;
   private int simCounter = 0;
   
   private static final double[] COMBO_MULTIPLIER = new double[] { 1.0, 1.1, 1.15, 1.2, 1.3, 1.4, 1.5, 2 };
   private static final int[] COMBO_THRESHOLD = new int[] { 1, 2, 5, 10, 25, 50, 75, 100 };
   
   private static final int COMBO_DELAY = 24;
   
   private Integer lastGravityTime = null;
   private Integer nextBumpTime = 0;
   
   private int lastComboTime = -COMBO_DELAY;
   
   private boolean boardChanged = true;
   
   /**
    * The current time for the simulation.
    */
   private int curTimeStamp = 0;
   
   /**
    * The unique identification for this simulation.
    */
   private final String id;
   
   /**
    * The map of timestamp to a Collection of all scheduled effects for that timestamp (happens
    * before gravity checks/etc.)
    */
   private Map<Integer, Collection<ComboEffect>> simulationEffects = new HashMap<Integer, Collection<ComboEffect>>();
   private PriorityQueue<Integer> simulationEffectTimes = new PriorityQueue<Integer>();
   
   private HashMap<List<Integer>, Collection<ActivateComboEffect>> effectClaims = new HashMap<List<Integer>, Collection<ActivateComboEffect>>();
   private HashMap<List<Integer>, Collection<ComboEffect>> activeEffects = new HashMap<List<Integer>, Collection<ComboEffect>>();
   
   private HashMap<PkmType, Double> typeMultipliers = new HashMap<PkmType, Double>();
   /**
    * The prospective combos that are available to activate.
    */
   private TreeSet<ActivateComboEffect> prospecticeCombosSet = new TreeSet<ActivateComboEffect>(
         (a, b) -> Integer.compare(a.getPriority(), b.getPriority()));
   
   private SimulationState state;
   
   public SimulationTask(SimulationCore simulationCore) {
      this(simulationCore, null, new SimulationFeeder());
   }
   
   public SimulationTask(SimulationCore simulationCore, SimulationFeeder feeder) {
      this(simulationCore, null, feeder);
   }
   
   public SimulationTask(SimulationCore simulationCore, List<Integer> move, SimulationFeeder feeder) {
      String moveString;
      if (move == null) {
         moveString = "null";
      } else {
         moveString = StringUtils.join(move.toArray(new Integer[0]));
      }
      id = moveString + " feeder:" + feeder.getID().toString();
      for (PkmType type : PkmType.values()) {
         typeMultipliers.put(type, 1.0);
      }
      createNewStateForMove(simulationCore, move, feeder);
   }
   
   public double getSpecialTypeMultiplier(PkmType type) {
      if (type == null) {
         return 1.0;
      }
      return typeMultipliers.get(type);
   }
   
   public void setSpecialTypeMultiplier(PkmType type, double multiplier) {
      typeMultipliers.put(type, multiplier);
   }
   
   public String getId() {
      return id;
   }
   
   public void logFinerWithId(String message, Object... args) {
      LOG.finer(String.format(id + ": " + message, args));
   }
   
   public static void setLogFiner(boolean enabled) {
      logFiner = enabled;
   }
   
   /**
    * Only used when starting a simulation task. This will initialize queues, processes, etc.
    * 
    * @param simulationCore
    * @param move
    * @param feeder
    */
   private void createNewStateForMove(SimulationCore simulationCore, List<Integer> move, SimulationFeeder feeder) {
      if (logFiner) {
         logFinerWithId("creating new state");
      }
      // Do the swap
      Board startBoard = simulationCore.getBoardCopy();
      if (move != null && move.size() >= 4) {
         Species pickedUp = startBoard.getSpeciesAt(move.get(0), move.get(1));
         Species droppedAt = startBoard.getSpeciesAt(move.get(2), move.get(3));
         startBoard.setSpeciesAt(move.get(0), move.get(1), droppedAt);
         startBoard.setSpeciesAt(move.get(2), move.get(3), pickedUp);
      }
      if (logFiner) {
         logFinerWithId("board created");
      }
      // Check for originality as non-air blocks.
      boolean[][] originality = new boolean[Board.NUM_ROWS][Board.NUM_COLS];
      for (int row = 1; row <= Board.NUM_ROWS; row++) {
         for (int col = 1; col <= Board.NUM_COLS; col++) {
            originality[row - 1][col - 1] = !startBoard.getSpeciesAt(row, col).equals(Species.AIR);
         }
      }
      if (logFiner) {
         logFinerWithId("originality set");
      }
      
      // Create the state
      state = new SimulationState(simulationCore, feeder, startBoard, 1.0f, 0, 0, originality);
      if (logFiner) {
         logFinerWithId("state made");
      }
      
      doComboCheck();
      if (logFiner) {
         logFinerWithId("combos checked, number of claims: "
 + prospecticeCombosSet.size());
      }
      if (move != null && move.size() >= 4) {
         ActivateComboEffect firstCombo = findBestComboFor(move.get(2), move.get(3));
         if (firstCombo == null) {
            firstCombo = findBestComboFor(move.get(0), move.get(1));
         }
         if (logFiner) {
            logFinerWithId("performing FIRST combo: " + StringUtils.join(firstCombo) + " with species: "
                  + getEffectSpecies(firstCombo.getCoords()));
         }
         doCombo(firstCombo);
      }
   }
   
   private ActivateComboEffect findBestComboFor(int row, int col) {
      ActivateComboEffect firstCombo = null;
      for (ActivateComboEffect effect : prospecticeCombosSet) {
         if (effect.containsCoords(row, col)) {
            firstCombo = effect;
            break;
         }
      }
      return firstCombo;
   }
   
   @Override
   protected SimulationState compute() {
      // ScheduledEffects should start out with exactly one effect on the queue.
      try {
         while (!doneSimulation() && simCounter < SIM_TIMEOUT) {
            if (logFiner) {
               logFinerWithId("simtime: %s, score: %s, comboQueue:%s", curTimeStamp, getState().getScore(),
                     prospecticeCombosSet.size());
            }
            doGravity();
            doAllCurrentEffects();
            doGravity();
            if (boardChanged) {
               doComboCheck();
               boardChanged = false;
            }
            doBestCombo();
            advanceTimeStamp();
            simCounter++; // Loop protection
         }
         return getState();
      } catch (Exception e) {
         LOG.severe("Something happened: " + e.getMessage() + " ");
         e.printStackTrace();
         return null;
      }
   }
   
   private void advanceTimeStamp() {
      Integer nextEffectTime = getNextEffectTime();
      Integer nextBumpTime = getNextBumpTime();
      Integer nextComboTime = getNextComboTime();
      
      Integer nextTime = getLowestOf(nextEffectTime, nextBumpTime, nextComboTime);
      if (nextTime != null) {
         curTimeStamp = nextTime.intValue();
      }
   }
   
   private Integer getLowestOf(Integer... values) {
      Integer lowest = null;
      for (Integer i : values) {
         if (lowest == null || i != null && i.intValue() < lowest.intValue()) {
            lowest = i;
         }
      }
      return lowest;
   }
   
   private boolean doneSimulation() {
      boolean ret = true;
      ret &= getNextEffectTime() == null;
      ret &= getNextBumpTime() == null;
      ret &= getNextComboTime() == null;
      return ret;
   }
   
   /**
    * @return
    */
   private Integer getNextEffectTime() {
      return simulationEffectTimes.peek();
   }
   
   public Integer getNextComboTime() {
      if (prospecticeCombosSet.isEmpty()) {
         return null;
      } else {
         return Math.max(lastComboTime + COMBO_DELAY, curTimeStamp);
      }
   }
   
   public Integer getNextBumpTime() {
      return nextBumpTime;
   }
   
   private void doAllCurrentEffects() {
      Integer nextTime = getNextEffectTime();
      if (nextTime != null && nextTime.intValue() <= curTimeStamp) {
         Collection<ComboEffect> currentEffects = popCurrentEffects();
         boardChanged |= !currentEffects.isEmpty();
         for (ComboEffect effect : currentEffects) {
            effect.doEffect(this);
         }
      }
   }
   
   private void doGravity() {
      int lastTime = lastGravityTime == null ? curTimeStamp : lastGravityTime.intValue();
      int increment = curTimeStamp - lastTime;
      
      Integer minHeightToBump = moveEverythingDownBy(increment);
      
      nextBumpTime = minHeightToBump == null ? null : minHeightToBump.intValue() + curTimeStamp;
      if (nextBumpTime == null) { // If gravity is done,
         lastGravityTime = null; // then we de-activate gravity
      } else {
         lastGravityTime = curTimeStamp; // otherwise we keep track of when it last ticked
      }
   }
   
   /**
    * Returns the minimum height until the next bump. this will also set the boardChanged condition
    * to true if something bumps, and this will ALSO use the feeder (if available and ready) to fill
    * in according to the increment.
    * 
    * @param increment
    * @return
    */
   private Integer moveEverythingDownBy(int increment) {
      Integer minHeight = null;
      SimulationFeeder feeder = getState().getFeeder();
      for (int col = 1; col <= Board.NUM_COLS; col++) { // each column
         int[] initialHeight = getHeights(col);
         for (int row = Board.NUM_ROWS; row >= 1; row--) { // going upwards
            int toLowerBy = Math.min(increment, initialHeight[row - 1]);
            if (toLowerBy > 0 && canMove(row, col)) { // not frozen, not claimed by a combo, and not
                                                      // air
               // find the distance to cover
               int positionDelta = toLowerBy % SimulationState.FALL_DISTANCE;
               int rowDelta = toLowerBy / SimulationState.FALL_DISTANCE;
               // find the destination position and row
               int startPosition = getState().getFallingPositionAt(row, col);
               int destPos = startPosition - positionDelta;
               int destRow = row + rowDelta;
               while (destPos < 0) {
                  destPos += SimulationState.FALL_DISTANCE;
                  destRow += 1;
               }
               if (destRow != row || destPos != startPosition) {
                  // make the actual move
                  getState().swapTiles(row, col, destRow, col);
                  // we DID move, so we are 'falling'
                  getState().setFallingAt(destRow, col, true);
                  // set the appropriate position
                  getState().setFallingPositionAt(destRow, col, destPos);
               }
            } else if (toLowerBy > 0 && row == 1 && feeder.hasMore(col) && getState().getBoard().isAir(row, col)) {
               // Feeder pushes into the very TOP row.
               // How much we can actually put in
               // how many rows we can actually add to - for every 16th additional unit of toLowerBy
               // we gain 1 more row.
               // Because this row is definitely free, we know we can add at least one, even if its
               // at position 15.
               // So we must go through and add as many as we can fit from the feeder.
               int rowSpace = 1 + toLowerBy / SimulationState.FALL_DISTANCE;
               int destPos = SimulationState.FALL_DISTANCE - toLowerBy % SimulationState.FALL_DISTANCE;
               
               int fedRow = rowSpace;
               while (fedRow >= 1 && feeder.hasMore(col)) {
                  Board b = getState().getBoard();
                  b.setSpeciesAt(fedRow, col, feeder.pollColumn(col));
                  b.setFrozenAt(fedRow, col, false);
                  // We fed something in, which is 'falling'
                  getState().setFallingAt(fedRow, col, true);
                  // set the appropriate position
                  getState().setFallingPositionAt(fedRow, col, destPos);
                  // go to the next row up
                  fedRow -= 1;
               }
            }
         }
         // Maintain states and check for heights
         int[] postMoveHeight = getHeights(col);
         for (int row = Board.NUM_ROWS; row >= 1; row--) { // going upwards
            if (canMove(row, col)) { // not frozen, not claimed by a combo, and not air
               int height = postMoveHeight[row - 1];
               if (getState().isFallingAt(row, col) && height == 0) {
                  getState().setFallingAt(row, col, false);
                  boardChanged = true;
               } else if (height > 0) {
                  getState().setFallingAt(row, col, true);
                  if (minHeight == null || minHeight.intValue() > height) {
                     minHeight = height;
                  }
               }
            } else if (row == 1 && feeder.hasMore(col) && getState().getBoard().isAir(row, col)) {
               int height = postMoveHeight[row - 1];
               if (minHeight == null || minHeight.intValue() > height) {
                  minHeight = height;
               }
            }
         }
      }
      return minHeight;
   }
   
   private int[] getHeights(int col) {
      Board b = getState().getBoard();
      int[] heightAt = new int[Board.NUM_ROWS];
      for (int row = Board.NUM_ROWS; row >= 1; row--) { // going upwards
         int belowPosition = 0;
         int belowHeight = 0;
         if (row < Board.NUM_ROWS && // this is above the bottom
               // And this block can conduct height (is inactive air or moving)
               (canMove(row + 1, col) || b.isAir(row + 1, col) && !isActive(row, col))) {
            belowPosition = getState().getFallingPositionAt(row + 1, col);
            belowHeight = heightAt[row];
         }
         
         // Finally, set the height for this row, to be used while finding how far
         // to move each block downwards (and where to put them)
         int myPosition = getState().getFallingPositionAt(row, col);
         if (b.isAir(row, col) && !isActive(row, col)) {
            // If this is inactive air, then it has a full fall distance to add.
            myPosition = SimulationState.FALL_DISTANCE;
            // Air has a height of this amount when inactive, but always a position of 0
         }
         heightAt[row - 1] = belowHeight + myPosition - belowPosition;
      }
      return heightAt;
   }
   
   /**
    * Can the given coordinates move? That is, they aren't claimed, active, frozen, or air.
    * 
    * @param row
    * @param col
    * @return
    */
   private boolean canMove(int row, int col) {
      return !isActive(row, col) && !isClaimed(row, col) && getState().getBoard().canMove(row, col);
   }
   
   private void doComboCheck() {
      Board b = getState().getBoard();
      boolean[][] hAvailable = new boolean[Board.NUM_ROWS][Board.NUM_COLS];
      boolean[][] vAvailable = new boolean[Board.NUM_ROWS][Board.NUM_COLS];
      boolean[][] isAvailable = new boolean[Board.NUM_ROWS][Board.NUM_COLS];
      // Find out what is available for combo
      for (int row = 1; row <= Board.NUM_ROWS; row++) {
         for (int col = 1; col <= Board.NUM_COLS; col++) {
            if (isFalling(row, col) || !isPickable(row, col)) {
               // No falling or Air block may combo
               continue;
            }
            if (isActive(row, col)) {
               // All active non-falling blocks might be available for either combo direction if...
               Collection<ActivateComboEffect> claims = getClaimsFor(row, col);
               for (ActivateComboEffect effect : claims) {
                  // there is an effect in that direction which has not been activated yet
                  hAvailable[row - 1][col - 1] |= effect.isHorizontal();
                  vAvailable[row - 1][col - 1] |= !effect.isHorizontal();
               }
            } else {
               // All inactive non-falling blocks are available for either vertical or horizontal
               // combos
               hAvailable[row - 1][col - 1] = true;
               vAvailable[row - 1][col - 1] = true;
            }
            isAvailable[row - 1][col - 1] = hAvailable[row - 1][col - 1] || vAvailable[row - 1][col - 1];
         }
      }
      // Then map out the exact lines
      int[][] hLines = new int[Board.NUM_ROWS][Board.NUM_COLS];
      int[][] vLines = new int[Board.NUM_ROWS][Board.NUM_COLS];
      // This will include the biggest current prospective combos, all of them
      // including possible extensions.
      for (int row = 1; row <= Board.NUM_ROWS; row++) {
         for (int col = 1; col <= Board.NUM_COLS; col++) {
            if (isAvailable[row - 1][col - 1]) {
               Species cur = b.getSpeciesAt(row, col);
               if (col > 1 && hAvailable[row - 1][col - 1] && hAvailable[row - 1][col - 2]) {
                  Species left = b.getSpeciesAt(row, col - 1);
                  if (cur.equals(left)) {
                     hLines[row - 1][col - 1] = hLines[row - 1][col - 2] + 1;
                     hLines[row - 1][col - 2] = 0;
                  }
               }
               if (row > 1 && vAvailable[row - 1][col - 1] && vAvailable[row - 2][col - 1]) {
                  Species above = b.getSpeciesAt(row - 1, col);
                  if (cur.equals(above)) {
                     vLines[row - 1][col - 1] = vLines[row - 2][col - 1] + 1;
                     vLines[row - 2][col - 1] = 0;
                  }
               }
            }
         }
      }
      effectClaims.clear();
      prospecticeCombosSet.clear();
      // Finally, wipe out the prospective combos and all combo claims.
      // Then, reconstruct them from the grids made above.
      for (int row = 1; row <= Board.NUM_ROWS; row++) {
         for (int col = 1; col <= Board.NUM_COLS; col++) {
            int vRun = vLines[row - 1][col - 1];
            if (vRun >= 2) {
               List<Integer> coords = new ArrayList<Integer>();
               while (vRun >= 0) {
                  coords.addAll(Arrays.asList(row - vRun, col));
                  vRun -= 1;
               }
               addProspectiveCombo(coords);
            }
            
            int hRun = hLines[row - 1][col - 1];
            if (hRun >= 2) {
               List<Integer> coords = new ArrayList<Integer>();
               while (hRun >= 0) {
                  coords.addAll(Arrays.asList(row, col - hRun));
                  hRun -= 1;
               }
               addProspectiveCombo(coords);
            }
         }
      }
      
   }
   
   private boolean isPickable(int row, int col) {
      Board board = getState().getBoard();
      Species cur = board.getSpeciesAt(row, col);
      return cur.getEffect().isPickable();
   }
   
   public boolean isFalling(int row, int col) {
      return getState().isFallingAt(row, col);
   }
   
   private void doBestCombo() {
      Integer nextComboTime = getNextComboTime();
      if (nextComboTime != null && nextComboTime.intValue() <= curTimeStamp) {
         ActivateComboEffect effect = prospecticeCombosSet.pollFirst();
         doCombo(effect);
      }
   }
   
   public void doCombo(ActivateComboEffect effect) {
      if (effect != null) {
         if (logFiner) {
            logFinerWithId("Performing combo effect: %s", StringUtils.join(effect));
         }
         effect.doEffect(this);
         lastComboTime = curTimeStamp;
         boardChanged = true;
      }
   }
   
   public boolean isActive(int row, int col) {
      return activeEffects.containsKey(Arrays.asList(row, col));
   }
   
   public boolean isActiveCombo(List<Integer> coords) {
      if (coords.size() >= 2) {
         Collection<ComboEffect> effects = activeEffects.get(Arrays.asList(coords.get(0), coords.get(1)));
         if (effects != null) {
            for (ComboEffect collision : effects) {
               if (collision.getCoords().equals(coords)) {
                  return true;
               }
            }
         }
      }
      return false;
   }
   
   public Collection<ComboEffect> getActiveEffectsFor(int row, int col) {
      Collection<ComboEffect> ret = Collections.emptyList();
      List<Integer> key = Arrays.asList(row, col);
      if (activeEffects.containsKey(key)) {
         ret = activeEffects.get(key);
      }
      return ret;
   }
   
   public void addActiveFor(ComboEffect effect) {
      List<Integer> coords = effect.getCoords();
      for (int i = 0; i * 2 + 1 < coords.size(); i++) {
         Integer row = coords.get(i * 2);
         Integer col = coords.get(i * 2 + 1);
         List<Integer> key = Arrays.asList(row, col);
         if (!activeEffects.containsKey(key)) {
            activeEffects.put(key, new HashSet<ComboEffect>());
         }
         activeEffects.get(key).add(effect);
      }
   }
   
   public void removeActive(ComboEffect effect) {
      List<Integer> coords = effect.getCoords();
      for (int i = 0; i * 2 + 1 < coords.size(); i++) {
         Integer row = coords.get(i * 2);
         Integer col = coords.get(i * 2 + 1);
         List<Integer> key = Arrays.asList(row, col);
         if (activeEffects.containsKey(key)) {
            activeEffects.get(key).remove(effect);
            if (activeEffects.get(key).isEmpty()) {
               activeEffects.remove(key);
            }
         }
      }
   }
   
   /**
    * Returns true if the given coordinates are claimed by some recognized possible combo.
    * 
    * @param row
    * @param col
    * @return
    */
   public boolean isClaimed(int row, int col) {
      return effectClaims.containsKey(Arrays.asList(row, col));
   }
   
   public Collection<ActivateComboEffect> getClaimsFor(int row, int col) {
      Collection<ActivateComboEffect> ret = Collections.emptyList();
      List<Integer> key = Arrays.asList(row, col);
      if (effectClaims.containsKey(key)) {
         ret = effectClaims.get(key);
      }
      return ret;
   }
   
   /**
    * @param coords
    */
   public void addClaimFor(ActivateComboEffect effect) {
      List<Integer> coords = effect.getCoords();
      for (int i = 0; i * 2 + 1 < coords.size(); i++) {
         Integer row = coords.get(i * 2);
         Integer col = coords.get(i * 2 + 1);
         List<Integer> key = Arrays.asList(row, col);
         if (!effectClaims.containsKey(key)) {
            effectClaims.put(key, new HashSet<ActivateComboEffect>());
         }
         effectClaims.get(key).add(effect);
      }
   }
   
   /**
    * Just removes all claims for the given row and column. Does not affect prospectiveCombos.
    * 
    * @param row
    * @param col
    */
   public void removeClaimsFor(int row, int col) {
      effectClaims.remove(Arrays.asList(row, col));
   }
   
   public void removeClaim(ActivateComboEffect effect) {
      List<Integer> coords = effect.getCoords();
      for (int i = 0; i * 2 + 1 < coords.size(); i++) {
         Integer row = coords.get(i * 2);
         Integer col = coords.get(i * 2 + 1);
         List<Integer> key = Arrays.asList(row, col);
         if (effectClaims.containsKey(key)) {
            effectClaims.get(key).remove(effect);
            if (effectClaims.get(key).isEmpty()) {
               effectClaims.remove(key);
            }
         }
      }
      prospecticeCombosSet.remove(effect);
   }
   
   public void completeComboFor(EraseComboEffect effect, boolean forceErase) {
      if (logFiner) {
         logFinerWithId("Completing combo: %s", effect.toString());
      }
      List<Integer> coords = effect.getCoords();
      removeActive(effect);
      Board b = getState().getBoard();
      for (int i = 0; i * 2 + 1 < coords.size(); i++) {
         int row = coords.get(i * 2);
         int col = coords.get(i * 2 + 1);
         if (!isActive(row, col) && effect.shouldErase(row, col)) {
            // Handle statistics
            
            // Continue on with replacement
            if (b.isFrozenAt(row, col) || b.getSpeciesAt(row, col).getEffect().isDisruption()) {
               getState().addDisruptionCleared(1);
            }
            getState().addBlockCleared(1);
            
            // Do changes
            getState().setOriginalAt(row, col, false);
            b.setSpeciesAt(row, col, Species.AIR);
            b.setFrozenAt(row, col, false);
            
            // Ensure that even if something was activated mid-fall (i.e. mega gengar's
            // silliness)
            // then it will still be set to a proper resting state when finally cleared, so it
            // won't
            // interfere with other gravity effects and height measurements
            // This might not be needed, but it is good to maintain the state properly
            getState().setFallingPositionAt(row, col, 0);
            
         }
      }
   }
   
   private void addProspectiveCombo(List<Integer> coords) {
      if (logFiner) {
         logFinerWithId("Recognized combo: %s", StringUtils.join(coords.toArray(new Integer[0])));
      }
      if (logFiner && isActiveCombo(coords)) {
         logFinerWithId("combo is already active: %s", StringUtils.join(coords.toArray(new Integer[0])));
      }
      if (coords.size() < 2 || isActiveCombo(coords)) {
         return;
      }
      Species effectSpecies = getEffectSpecies(coords);
      Effect effect = getEffectFor(effectSpecies);
      ActivateComboEffect activateEffect = new ActivateComboEffect(coords, effect.isPersistent());
      
      boolean horizontal = activateEffect.isHorizontal();
      Collection<ActivateComboEffect> toMerge = new HashSet<ActivateComboEffect>();
      for (int i = 0; i * 2 + 1 < coords.size(); i++) {
         int row = coords.get(i * 2);
         int col = coords.get(i * 2 + 1);
         Collection<ActivateComboEffect> claims = getClaimsFor(row, col);
         for (ActivateComboEffect claimEffect : claims) {
            if (claimEffect.isHorizontal() == horizontal) {
               toMerge.add(claimEffect);
            }
         }
      }
      if (!toMerge.isEmpty()) {
         List<Integer> collectiveCoords = new ArrayList<Integer>(coords);
         for (ActivateComboEffect conflictingEffect : toMerge) {
            removeClaim(conflictingEffect);
            prospecticeCombosSet.remove(conflictingEffect);
            collectiveCoords.addAll(conflictingEffect.getCoords());
         }
         
         List<Integer> limits = getLimits(collectiveCoords);
         List<Integer> finalCoords = getComboForLimits(limits);
         activateEffect = new ActivateComboEffect(finalCoords, effect.isPersistent());
      }
      if (logFiner) {
         logFinerWithId("Claiming for combo: %s", activateEffect);
      }
      prospecticeCombosSet.add(activateEffect);
      addClaimFor(activateEffect);
   }
   
   public static List<Integer> getComboForLimits(List<Integer> limits) {
      List<Integer> ret = new ArrayList<Integer>();
      int rowDir = Integer.signum(limits.get(2) - limits.get(0));
      int colDir = Integer.signum(limits.get(3) - limits.get(1));
      if (rowDir == 0 && colDir == 0) {
         ret.addAll(Arrays.asList(limits.get(0), limits.get(1)));
      } else {
         int row = limits.get(0);
         int col = limits.get(1);
         while (row <= limits.get(2) && col <= limits.get(3)) {
            ret.add(row);
            ret.add(col);
            row += rowDir;
            col += colDir;
         }
      }
      return ret;
   }
   
   public static List<Integer> getLimits(List<Integer> coords) {
      int minRow = coords.get(0);
      int minCol = coords.get(1);
      int maxRow = coords.get(0);
      int maxCol = coords.get(1);
      
      for (int i = 0; i * 2 + 1 < coords.size(); i++) {
         int row = coords.get(i * 2);
         int col = coords.get(i * 2 + 1);
         if (row < minRow) {
            minRow = row;
         }
         if (row > maxRow) {
            maxRow = row;
         }
         if (col < minCol) {
            minCol = col;
         }
         if (col > maxCol) {
            maxCol = col;
         }
      }
      
      return Arrays.asList(minRow, minCol, maxRow, maxCol);
   }
   
   public void removeCollisions(List<Integer> coords) {
      if (logFiner) {
         logFinerWithId("Removing collisions with: %s", StringUtils.join(coords.toArray(new Integer[0])));
      }
      Set<ActivateComboEffect> toRemove = new HashSet<ActivateComboEffect>();
      for (ActivateComboEffect combo : prospecticeCombosSet) {
         boolean shouldRemove = false;
         for (int i = 0; !shouldRemove && i * 2 + 1 < coords.size(); i++) {
            int row = coords.get(i * 2);
            int col = coords.get(i * 2 + 1);
            shouldRemove |= combo.containsCoords(row, col);
         }
         if (shouldRemove) {
            toRemove.add(combo);
         }
      }
      for (ActivateComboEffect comboEffect : toRemove) {
         prospecticeCombosSet.remove(comboEffect);
         removeClaim(comboEffect);
      }
   }
   
   public void scheduleEffect(ComboEffect effect, int delay) {
      if (logFiner) {
         logFinerWithId("Scheduling combo after %s frames: %s", delay, effect);
      }
      effect.init(this);
      addActiveFor(effect);
      int timeStamp = curTimeStamp + delay;
      if (!simulationEffects.containsKey(timeStamp)) {
         simulationEffects.put(timeStamp, new HashSet<ComboEffect>());
         simulationEffectTimes.offer(timeStamp);
      }
      simulationEffects.get(timeStamp).add(effect);
   }
   
   private Collection<ComboEffect> popCurrentEffects() {
      Collection<ComboEffect> ret = simulationEffects.remove(curTimeStamp);
      if (ret == null) {
         ret = Collections.emptyList();
      }
      simulationEffectTimes.remove(curTimeStamp);
      return ret;
   }
   
   public SimulationState getState() {
      return state;
   }
   
   public EraseComboEffect getWoodShatterEffect(ComboEffect comboEffect) {
      Set<List<Integer>> woodCoords = new HashSet<List<Integer>>();
      Board b = getState().getBoard();
      int[] nearby = new int[] { 0, -1, 0, 1, 1, 0, -1, 0 };
      List<Integer> coords = comboEffect.getCoords();
      
      for (int i = 0; i * 2 + 1 < coords.size(); i++) {
         for (int k = 0; k * 2 + 1 < nearby.length; k++) {
            int row = coords.get(i * 2) + nearby[k * 2];
            int col = coords.get(i * 2 + 1) + nearby[k * 2 + 1];
            if (!isClaimed(row, col)) {
               Species neighbour = b.getSpeciesAt(row, col);
               if (neighbour.getEffect().equals(Effect.WOOD)) {
                  woodCoords.add(Arrays.asList(row, col));
               }
            }
         }
      }
      EraseComboEffect ret = null;
      if (!woodCoords.isEmpty()) {
         List<Integer> retCoords = new ArrayList<Integer>(woodCoords.size() * 2);
         for (List<Integer> coord : woodCoords) {
            retCoords.addAll(coord);
         }
         ret = new EraseComboEffect(retCoords);
         ret.setForceErase(true);
      }
      return ret;
   }
   
   public Double getScoreFor(ActivateComboEffect comboEffect) {
      Species effectSpecies = getEffectSpecies(comboEffect.getCoords());
      double comboMultiplier = 1.0;
      int combos = comboEffect.getNumCombosOnActivate();
      for (int i = 0; i < COMBO_THRESHOLD.length && i < COMBO_MULTIPLIER.length; i++) {
         if (COMBO_THRESHOLD[i] <= combos + 1) {
            comboMultiplier = COMBO_MULTIPLIER[i];
         }
      }
      double basicScore = getBasicScoreFor(effectSpecies);
      double typeMod = getTypeModifier(effectSpecies);
      double numBlocksModifier = getNumBlocksMultiplier(comboEffect.getNumBlocks());
      Effect effect = getEffectFor(effectSpecies);
      double effectSpecial = effect.getScoreMultiplier(comboEffect, this);
      double finalScore = basicScore * typeMod * comboMultiplier * numBlocksModifier * effectSpecial;
      if (logFiner) {
         logFinerWithId("Calculated score as %s for combo %s", finalScore, comboEffect);
      }
      return finalScore;
   }
   
   /**
    * @param effectSpecies
    * @return
    */
   public double getBasicScoreFor(Species effectSpecies) {
      int level = getState().getCore().getLevel(effectSpecies);
      // gets the basic block score for this species in this stage
      return effectSpecies.getAttack(level);
   }
   
   /**
    * @param effectSpecies
    * @return
    */
   public double getTypeModifier(Species effectSpecies) {
      PkmType stageType = getState().getCore().getStage().getType();
      double typeMod = PkmType.getMultiplier(effectSpecies.getType(), stageType);
      return typeMod;
   }
   
   /**
    * @param numBlocks
    * @return
    */
   private double getNumBlocksMultiplier(int numBlocks) {
      double result;
      if (numBlocks == 6) {
         result = 3.0;
      } else {
         result = (numBlocks - 1) / 2.0;
      }
      return result;
   }
   
   public void addScore(int score) {
      if (logFiner) {
         logFinerWithId("Adding score: %s", score);
      }
      getState().addScore(score);
   }
   
   public void handleMainComboResult(ActivateComboEffect comboEffect, Effect effect) {
      int scoreToAdd = getScoreFor(comboEffect).intValue();
      if (logFiner) {
         logFinerWithId("Adding main score of %s for combo %s", scoreToAdd, comboEffect);
      }
      removeActive(comboEffect);
      List<Integer> coords = comboEffect.getCoords();
      
      handleMegaIncreases(coords);
      addScore(scoreToAdd);
      
      EraseComboEffect erasureEffect = new EraseComboEffect(coords);
      erasureEffect.setForceErase(comboEffect instanceof ActivateMegaComboEffect);
      scheduleEffect(erasureEffect, effect.getErasureDelay());
      
      EraseComboEffect woodShatter = getWoodShatterEffect(comboEffect);
      if (woodShatter != null) {
         scheduleEffect(woodShatter, Effect.WOOD.getErasureDelay());
      }
      if (logFiner) {
         logFinerWithId("Number of total blocks cleared is now: %s", getState().getBlocksCleared());
      }
   }
   
   /**
    * @param coords
    */
   protected void handleMegaIncreases(List<Integer> coords) {
      if (getState().getCore().isMegaAllowed()) {
         Species effectSpecies = getEffectSpecies(coords);
         Species megaSlot = getState().getCore().getMegaSlot();
         if (megaSlot != null && megaSlot.equals(effectSpecies)) {
            int megaIncrease = 0;
            for (int i = 0; i * 2 + 1 < coords.size(); i += 1) {
               int row = coords.get(i * 2);
               int col = coords.get(i * 2 + 1);
               if (!getState().getBoard().isFrozenAt(row, col)) {
                  megaIncrease += 1;
               }
            }
            getState().increaseMegaProgress(megaIncrease);
            if (logFiner) {
               logFinerWithId("Mega progress is now %s of %s", getState().getMegaProgress(), getState().getCore()
                     .getMegaThreshold());
            }
         }
      }
   }
   
   public Species getEffectSpecies(List<Integer> coords) {
      Board b = getState().getBoard();
      Species s = Species.AIR;
      for (int i = 0; !s.getEffect().isPickable() && i * 2 + 1 < coords.size(); i++) {
         int row = coords.get(i * 2);
         int col = coords.get(i * 2 + 1);
         s = b.getSpeciesAt(row, col);
      }
      return s;
   }
   
   public List<Integer> findMatches(int limit, boolean includeActive,
         TriFunction<Integer, Integer, Species, Boolean> function) {
      List<Integer> match = new ArrayList<Integer>();
      for (int row = 1; match.size() / 2 < limit && row <= Board.NUM_ROWS; row++) {
         for (int col = 1; match.size() / 2 < limit && col <= Board.NUM_COLS; col++) {
            if (!includeActive && isActive(row, col)) {
               continue;
            }
            Species thisSpecies = getState().getBoard().getSpeciesAt(row, col);
            if (function.apply(row, col, thisSpecies)) {
               match.addAll(Arrays.asList(row, col));
            }
         }
      }
      return match;
   }
   
   public Effect getEffectFor(Species s) {
      Effect effect = s.getEffect();
      Effect megaEffect = s.getMegaEffect();
      if (s.getMegaName() != null // Has a mega name
            && !megaEffect.equals(Effect.NONE) // Mega effect is well defined
            && getState().isMegaActive() // Mega is actually active right now
            && getState().getCore().getMegaSlot().equals(s)) { // and the mega slot IS this species.
         effect = megaEffect;
      }
      if (logFiner) {
         logFinerWithId("Effect Query for Species %s, Returned %s", s, effect);
      }
      return effect;
   }
   
   public void eraseBonusIn(List<Integer> toErase, int erasureDelay) {
      eraseBonusIn(toErase, erasureDelay, true);
   }

   public void eraseBonusIn(List<Integer> toErase, int erasureDelay, boolean forceErase) {
      if (logFiner) {
         logFinerWithId("Scheduling erasure after %s frames for %s", erasureDelay,
               StringUtils.join(toErase.toArray(new Integer[0])));
      }
      for (int i = 0; i * 2 + 1 < toErase.size(); i++) {
         int row = toErase.get(i * 2);
         int col = toErase.get(i * 2 + 1);
         removeClaimsFor(row, col);
      }
      boardChanged = true;
      EraseComboEffect eraseBonus = new EraseComboEffect(toErase);
      eraseBonus.setForceErase(forceErase);
      scheduleEffect(eraseBonus, erasureDelay);
   }
   
   public void unfreezeAt(List<Integer> coords) {
      if (coords == null || coords.size() < 2) {
         return;
      }
      Board b = getState().getBoard();
      for (int i = 0; i * 2 + 1 < coords.size(); i++) {
         int row = coords.get(i * 2);
         int col = coords.get(i * 2 + 1);
         if (b.isFrozenAt(row, col)) {
            getState().addDisruptionCleared(1);
         }
         b.setFrozenAt(row, col, false);
      }
   }
}
