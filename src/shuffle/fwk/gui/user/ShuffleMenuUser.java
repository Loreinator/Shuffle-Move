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

package shuffle.fwk.gui.user;

import java.util.Locale;

import shuffle.fwk.service.bugreport.BugReportServiceUser;
import shuffle.fwk.service.editspecies.EditSpeciesServiceUser;
import shuffle.fwk.service.gridprintconfig.GridPrintConfigServiceUser;
import shuffle.fwk.service.migration.MigrateServiceUser;
import shuffle.fwk.service.movechooser.MoveChooserServiceUser;
import shuffle.fwk.service.movepreferences.MovePreferencesServiceUser;
import shuffle.fwk.service.roster.EditRosterServiceUser;
import shuffle.fwk.service.teams.EditTeamServiceUser;
import shuffle.fwk.service.textdisplay.TextDisplayServiceUser;
import shuffle.fwk.service.update.UpdateServiceUser;

/**
 * @author Andrew Meyers
 *
 */
public interface ShuffleMenuUser extends EditRosterServiceUser, MovePreferencesServiceUser, TextDisplayServiceUser,
      BugReportServiceUser, UpdateServiceUser, EditTeamServiceUser, EditSpeciesServiceUser, GradingModeUser,
      MigrateServiceUser, MoveChooserServiceUser, GridPrintConfigServiceUser {
   
   /**
	 * 
	 */
   void loadAll();
   
   /**
	 * 
	 */
   void saveAll();
   
   /**
	 * 
	 */
   void loadRoster();
   
   /**
	 * 
	 */
   void saveRoster();
   
   /**
	 * 
	 */
   void loadTeams();
   
   /**
	 * 
	 */
   void saveTeams();
   
   /**
	 * 
	 */
   void toggleActiveMega();
   
   /**
	 * 
	 */
   void clearGrid();
   
   /**
	 * 
	 */
   void loadDefaultGrid();
   
   /**
	 * 
	 */
   void loadGrid();
   
   /**
	 * 
	 */
   void saveGrid();
   
   /**
	 * 
	 */
   @Override
   void changeMode();
   
   /**
	 * 
	 */
   void computeNow();
   
   /**
    * @param b
    */
   void setAutoCompute(boolean b);
   
   void repaint();
   
   /**
    * @param loc
    */
   void setLocaleTo(Locale loc);
   
   /**
    * Fills the grid with the currently selected species paint, for every empty tile.
    */
   void fillGrid();
   
   /**
    * Clears all tiles in the Grid that match the currently selected paint. Has no effect if the
    * selected paint is air or null.
    */
   void clearSelectedTiles();
   
}
