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

package shuffle.fwk;

import java.awt.event.KeyEvent;
import java.util.List;

import shuffle.fwk.config.manager.TeamManager;
import shuffle.fwk.data.Board;
import shuffle.fwk.data.Species;
import shuffle.fwk.data.SpeciesPaint;
import shuffle.fwk.data.Stage;

public enum EntryMode {
   PAINT("paint") {
      @Override
      public String getColorKey() {
         return "PAINT_MODE_COLOR";
      }
      
      @Override
      public void handleKeyPress(EntryModeUser user, KeyEvent evt) {
         TeamManager tm = user.getTeamManager();
         if (tm.isFrozenBind(evt.getKeyChar())) {
            user.toggleFrozen();
         } else if (evt.getKeyCode() == KeyEvent.VK_TAB) {
            user.changeMode();
         } else {
            Species species = getSpeciesFor(user, evt);
            if (!species.equals(Species.AIR)) {
               user.setSelectedSpecies(species);
            }
         }
      }
      
      @Override
      public void handleGridEvent(EntryModeUser user, int row, int col, boolean isErase) {
         SpeciesPaint curPaint;
         if (isErase) {
            curPaint = SpeciesPaint.AIR;
         } else {
            curPaint = user.getCurrentSpeciesPaint();
         }
         user.paintAt(curPaint, row, col);
      }
      
      @Override
      public EntryMode getNextMode() {
         return EXPRESS;
      }
      
   },
   EXPRESS("express") {
      @Override
      public String getColorKey() {
         return "EXPRESS_MODE_COLOR";
      }
      
      @Override
      public void handleKeyPress(EntryModeUser user, KeyEvent evt) {
         TeamManager tm = user.getTeamManager();
         if (tm.isFrozenBind(evt.getKeyChar())) {
            List<Integer> prevCursor = user.getPreviousCursor();
            if (prevCursor != null && prevCursor.size() >= 2) {
               user.toggleFrozenAt(prevCursor.get(0), prevCursor.get(1));
            }
         } else if (evt.getKeyChar() == KeyEvent.VK_TAB) {
            user.changeMode();
         } else if (evt.getKeyChar() == KeyEvent.VK_SPACE) {
            user.advanceCursorBy(1);
         } else if (evt.getKeyChar() == KeyEvent.VK_BACK_SPACE) {
            user.advanceCursorBy(-1);
         } else if (evt.getKeyChar() == KeyEvent.VK_DELETE) {
            paintWith(user, Species.AIR);
         } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
            user.advanceCursorBy(-Board.NUM_COLS);
         } else if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
            user.advanceCursorBy(+Board.NUM_COLS);
         } else if (evt.getKeyCode() == KeyEvent.VK_LEFT) {
            user.advanceCursorBy(-1);
         } else if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
            user.advanceCursorBy(+1);
         } else {
            Species species = getSpeciesFor(user, evt);
            if (!species.equals(Species.AIR)) {
               user.setSelectedSpecies(species);
               paintWith(user, species);
            }
         }
      }
      
      @Override
      public void handleGridEvent(EntryModeUser user, int row, int col, boolean isErase) {
         user.setCursorTo(row, col);
      }
      
      @Override
      public EntryMode getNextMode() {
         return PAINT;
      }
   };
   
   /**
    * @param user
    * @param species
    */
   protected static void paintWith(EntryModeUser user, Species species) {
      List<Integer> coords = user.getCurrentCursor();
      user.paintAt(new SpeciesPaint(species, false), coords.get(0), coords.get(1));
      user.advanceCursorBy(1);
   }
   
   private String i18nKey;
   
   private EntryMode(String text) {
      i18nKey = text;
   }
   
   /**
    * @return
    */
   public String getI18nKey() {
      return i18nKey;
   }
   
   protected Species getSpeciesFor(EntryModeUser user, KeyEvent evt) {
      TeamManager teamManager = user.getTeamManager();
      char c = evt.getKeyChar();
      Stage currentStage = user.getCurrentStage();
      return teamManager.getSpeciesFor(c, currentStage);
   }
   
   public abstract String getColorKey();
   
   /**
    * Handles the specified KeyEvent, as specified by {@link java.awt.event.KeyEvent}
    * 
    * @param user
    *           The EntryModeUser to use.
    * @param evt
    *           The KeyEvent to handle
    * @return true if this changed anything
    */
   public abstract void handleKeyPress(EntryModeUser user, KeyEvent evt);
   
   /**
    * Handles the specified grid paint action, by row and column.
    * 
    * @param user
    *           The EntryModeUser to use.
    * @param row
    *           an int within [1, 6]
    * @param col
    *           an int within [1, 6]
    * @param isErase
    *           if true, this will try to erase the specified coordinate. Otherwise ignored.
    */
   public abstract void handleGridEvent(EntryModeUser user, int row, int col, boolean isErase);
   
   /**
    * Returns the mode that comes after this one.
    * 
    * @return An EntryMode, never null.
    */
   public abstract EntryMode getNextMode();
   
}