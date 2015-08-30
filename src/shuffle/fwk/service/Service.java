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

package shuffle.fwk.service;

import java.awt.Frame;

import javax.swing.JDialog;

/**
 * The Service interface. Please extend {@link BaseService} instead. to ensure proper cleanup and
 * creation.
 * 
 * @author Andrew Meyers
 */
public interface Service<Y extends Object> {
   
   /**
    * Launches the service's GUI.
    */
   public void launch();
   
   /**
    * Pulls to the top level window if it has launched.
    */
   public void pullToFront();
   
   /**
    * Hides the service's GUI.
    */
   public void hide();
   
   /**
    * Performs setup for the service's GUI.
    * 
    * @param owner
    *           TODO
    */
   public void setupGUI(Frame owner);
   
   /**
    * Updates the service's GUI.
    */
   public void updateGUI();
   
   /**
    * Returns the service's unique ID.
    * 
    * @return The unique identifier.
    */
   public long getId();
   
   /**
    * Returns the dialog for this service.
    * 
    * @return The JDialog for this service, or null if there is none.
    */
   public JDialog getDialog();

}
