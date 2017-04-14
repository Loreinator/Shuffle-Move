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

package shuffle.fwk.data;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * @author Andrew Meyers
 *
 */
public class Board {
   public static final Logger LOG = Logger.getLogger(Board.class.getName());
   public static final int NUM_ROWS = 6;
   public static final int NUM_COLS = 6;
   public static final int NUM_CELLS = NUM_ROWS * NUM_COLS;
   
   private final Species[][] species = new Species[NUM_ROWS][NUM_COLS];
   private final boolean[][] frozen = new boolean[NUM_ROWS][NUM_COLS];
   private final boolean[][] clouded = new boolean[NUM_ROWS][NUM_COLS];
   private int megaProgress;
   private Status status;
   /**
    * The number of turns required for the non-none status to revert to none.
    */
   private int statusDuration;
   
   public enum Status {
      NONE(1.0, "board.status.none"),
      DELAY(1.0, "board.status.delay"),
      BURN(1.5, "board.status.burn", PkmType.FIRE),
      SLEEP(1.2, "board.status.sleep", PkmType.values()),
      PARALYZE(1.0, "board.status.paralyze"),
      FEAR(1.5, "board.status.fear", PkmType.GHOST),
      FROZEN(1.2, "board.status.frozen", PkmType.ICE),
      POISON(1.5, "board.status.poison", PkmType.POISON);
      
      private final double mult;
      private final String key;
      private final Collection<PkmType> boostedTypes;
      
      private Status(double multiplier, String i18nKey, PkmType... types) {
         mult = multiplier;
         key = i18nKey;
         boostedTypes = Arrays.asList(types);
      }
      
      public double getBoostMultiplier() {
         return mult;
      }
      
      public boolean boostsType(PkmType type) {
         return boostedTypes.contains(type);
      }
      
      public String getKey() {
         return key;
      }
      
      public boolean isNone() {
         return NONE.equals(this);
      }
      
      public Number getMultiplier(PkmType type) {
         return boostsType(type) ? getBoostMultiplier() : 1;
      }
   }
   private String toString = null;
   
   public Board() {
      clear();
      megaProgress = 0;
      status = Status.NONE;
      statusDuration = 0;
   }
   
   public Board(Board b) {
      this();
      for (int row = 1; row <= NUM_ROWS; row++) {
         for (int col = 1; col <= NUM_COLS; col++) {
            setSpeciesAt(row, col, b.getSpeciesAt(row, col));
            setFrozenAt(row, col, b.isFrozenAt(row, col));
         }
      }
      megaProgress = b.getMegaProgress();
      status = b.getStatus();
      statusDuration = b.getStatusDuration();
   }
   
   public int getStatusDuration() {
      return statusDuration;
   }
   
   public boolean decreaseStatusDuration(int decreaseBy) {
      boolean changed = setStatusDuration(statusDuration - decreaseBy);
      if (changed && getStatusDuration() == 0) {
         status = Status.NONE;
      }
      return changed;
   }
   
   public boolean setStatusDuration(int newDuration) {
      newDuration = Math.max(0, newDuration);
      if (newDuration != statusDuration) {
         statusDuration = newDuration;
         if (toString != null) {
            toString = null;
         }
         return true;
      } else {
         return false;
      }
   }
   
   public int getMegaProgress() {
      return megaProgress;
   }
   
   public boolean increaseMegaProgress(int increaseBy) {
      return setMegaProgress(megaProgress + increaseBy);
   }
   
   public boolean setMegaProgress(int newProgress) {
      newProgress = Math.max(0, newProgress);
      if (newProgress != megaProgress) {
         megaProgress = newProgress;
         if (toString != null) {
            toString = null;
         }
         return true;
      } else {
         return false;
      }
   }
   
   public Status getStatus() {
      return status;
   }
   
   public boolean setStatus(Status s) {
      if (s == null || s.equals(status)) {
         return false;
      }
      status = s;
      if (Status.NONE.equals(s)) {
         statusDuration = 0;
      }
      return true;
   }
   
   /**
    * gets the species for the block at the specified co-odinates. Coordinates should be within
    * [1,6]x[1,6].
    * 
    * @param row
    * @param column
    * @return The species. Null if there is no species at the location. If the coordinate is a wood,
    *         metal, or air, it will return the 'wood' species, the 'metal' species, or the null,
    *         respectively.
    */
   public Species getSpeciesAt(int row, int column) {
      if (row < 1 || row > NUM_ROWS || column < 1 || column > NUM_COLS) {
         return Species.AIR;
      }
      return species[row - 1][column - 1];
   }
   
   public Set<Species> getSpeciesPresent() {
      Set<Species> ret = new HashSet<Species>();
      for (int row = 1; row <= NUM_ROWS; row++) {
         for (int col = 1; col <= NUM_COLS; col++) {
            ret.add(getSpeciesAt(row, col));
         }
      }
      return ret;
   }
   
   public boolean setSpeciesAt(int row, int column, Species s) {
      if (row < 1 || row > NUM_ROWS || column < 1 || column > NUM_COLS || s == null || s.equals(Species.FREEZE)) {
         return false;
      }
      boolean changed = !s.equals(getSpeciesAt(row, column));
      if (!s.isFreezable()) {
         changed |= setFrozenAt(row, column, false);
      }
      species[row - 1][column - 1] = s;
      if (changed && toString != null) {
         toString = null;
      }
      return changed;
   }
   
   public boolean isCloudedAt(int row, int column) {
      if (row < 1 || row > NUM_ROWS || column < 1 || column > NUM_COLS) {
         return false;
      }
      return clouded[row - 1][column - 1];
   }
   
   public boolean setClouded(int row, int column, Boolean encloud) {
      if (row < 1 || row > NUM_ROWS || column < 1 || column > NUM_COLS || encloud == null) {
         return false;
      }
      Species s = getSpeciesAt(row, column);
      boolean toSet = s != null && encloud;
      boolean changed = clouded[row - 1][column - 1] != encloud;
      clouded[row - 1][column - 1] = toSet;
      if (changed && toString != null) {
         toString = null;
      }
      return changed;
   }
   
   /**
    * gets the frozen state for the block at the specified coodinates. Coordinates should be within
    * [1,6]x[1,6].
    * 
    * @param row
    * @param column
    * @return The frozen state. false if there is no species at the location.
    */
   public boolean isFrozenAt(int row, int column) {
      if (row < 1 || row > NUM_ROWS || column < 1 || column > NUM_COLS) {
         return false;
      }
      return frozen[row - 1][column - 1];
   }
   
   public boolean setFrozenAt(int row, int column, Boolean freeze) {
      if (row < 1 || row > NUM_ROWS || column < 1 || column > NUM_COLS || freeze == null) {
         return false;
      }
      Species s = getSpeciesAt(row, column);
      boolean toSet = s != null && s.isFreezable() && freeze;
      boolean changed = frozen[row - 1][column - 1] != freeze;
      frozen[row - 1][column - 1] = toSet;
      if (changed && toString != null) {
         toString = null;
      }
      return changed;
   }
   
   public boolean setAllFrozen(Boolean frozen) {
      boolean changed = false;
      for (int i = 1; i <= NUM_ROWS; i++) {
         for (int j = 1; j <= NUM_COLS; j++) {
            changed |= setFrozenAt(i, j, frozen);
         }
      }
      return changed;
   }
   
   public boolean isAir(int row, int column) {
      if (row < 1 || row > NUM_ROWS || column < 1 || column > NUM_COLS) {
         return true;
      }
      return species[row - 1][column - 1].getDefaultEffect().equals(Effect.AIR);
   }
   
   /**
    * Returns true if the given coordinate tile is unable to move due to one of:
    * <ul>
    * <li>is at the bottom row</li>
    * <li>is waiting</li>
    * <li>is active</li>
    * <li>is frozen</li>
    * </ul>
    * 
    * @param row
    * @param column
    * @return True if the given coordinates have a tile which is not able to move at this time.
    */
   public boolean isFixed(int row, int column) {
      if (row < 1 || row > NUM_ROWS || column < 1 || column > NUM_COLS) {
         return false;
      }
      return Board.NUM_ROWS == row || frozen[row - 1][column - 1];
   }
   
   public boolean clear() {
      boolean changed = setAllFrozen(false);
      for (int i = 1; i <= NUM_ROWS; i++) {
         for (int j = 1; j <= NUM_COLS; j++) {
            changed |= setSpeciesAt(i, j, Species.AIR);
            changed |= setFrozenAt(i, j, false);
         }
      }
      if (changed && toString != null) {
         toString = null;
      }
      return changed;
   }
   
   @Override
   public String toString() {
      if (toString == null) {
         StringBuilder sb = new StringBuilder();
         sb.append("Mega_Progress:" + Integer.toString(this.megaProgress) + "\n");
         sb.append("Status:" + status.toString() + "\n");
         sb.append("Status_Duration:" + Integer.toString(this.statusDuration) + "\n");
         int maxlen = 0;
         for (Species[] row : species) {
            for (Species s : row) {
               if (maxlen < s.getName().length()) {
                  maxlen = s.getName().length();
               }
            }
         }
         for (int i = 1; i <= NUM_ROWS; i++) {
            for (int j = 1; j <= NUM_COLS; j++) {
               String name = species[i - 1][j - 1].getName();
               String bufr = new String(new char[maxlen - name.length()]).replace("\0", " ");
               sb.append(bufr);
               sb.append(name);
               if (j <= NUM_COLS - 1) {
                  sb.append(",");
               }
            }
            if (i <= NUM_ROWS - 1) {
               sb.append("\n");
            }
         }
         sb.append("\n");
         for (int i = 1; i <= NUM_ROWS; i++) {
            for (int j = 1; j <= NUM_COLS; j++) {
               sb.append(Boolean.toString(isFrozenAt(i, j)));
               if (j < NUM_COLS) {
                  sb.append(",");
               }
            }
            if (i < NUM_ROWS) {
               sb.append("\n");
            }
         }
         sb.append("\n");
         for (int i = 1; i <= NUM_ROWS; i++) {
            for (int j = 1; j <= NUM_COLS; j++) {
               sb.append(isCloudedAt(i, j) ? "clouded" : "clear");
               if (j < NUM_COLS) {
                  sb.append(",");
               }
            }
            if (i < NUM_ROWS) {
               sb.append("\n");
            }
         }
         toString = sb.toString();
      }
      return toString;
   }
   
   @Override
   public int hashCode() {
      return 17 * toString().hashCode();
   }
   
   @Override
   public boolean equals(Object o) {
      return o instanceof Board && o != null && o.toString().equals(toString());
   }

}
