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

import java.util.List;

import shuffle.fwk.config.provider.SpeciesManagerProvider;
import shuffle.fwk.config.provider.TeamManagerProvider;
import shuffle.fwk.data.Species;
import shuffle.fwk.data.SpeciesPaint;
import shuffle.fwk.data.Team;

/**
 * @author Andrew Meyers
 *
 */
public interface EntryModeUser extends SpeciesManagerProvider, TeamManagerProvider {
   
   Team getCurrentTeam();
   
   /**
    * @param i
    */
   void advanceCursorBy(int i);
   
   /**
	 * 
	 */
   void changeMode();
   
   /**
    * @return
    */
   List<Integer> getCurrentCursor();
   
   /**
    * @return
    */
   SpeciesPaint getCurrentSpeciesPaint();
   
   /**
    * @return
    */
   List<Integer> getPreviousCursor();
   
   /**
    * @param curPaint
    * @param row
    * @param col
    */
   void paintAt(SpeciesPaint curPaint, int row, int col);
   
   /**
    * @param row
    * @param col
    */
   void setCursorTo(int row, int col);
   
   /**
    * @param species
    */
   void setSelectedSpecies(Species species);
   
   /**
	 * 
	 */
   void toggleFrozen();
   
   /**
    * @param integer
    * @param integer2
    */
   void toggleFrozenAt(Integer integer, Integer integer2);
   
}
