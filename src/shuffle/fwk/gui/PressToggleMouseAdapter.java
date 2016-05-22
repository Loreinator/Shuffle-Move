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
package shuffle.fwk.gui;

import java.awt.event.MouseEvent;

/**
 * @author Andrew Meyers
 *
 */
public abstract class PressToggleMouseAdapter extends PressOrClickMouseAdapter {
   
   @Override
   protected boolean ignoreClick() {
      return true;
   }
   
   @Override
   public void mouseEntered(MouseEvent e) {
      onEnter();
      // Unlike the super type, we ignore all mouse entered events
   }
   
   @Override
   protected void onEnter() {
      // Do nothing
   }
   
   @Override
   public void mouseReleased(MouseEvent e) {
      // System.out.println("Mouse Released");
      PressOrClickMouseAdapter.inside = false;
      // This seems to be enough to stop spill over from menu clicks
   }
   
   @Override
   public void mousePressed(MouseEvent e) {
      PressOrClickMouseAdapter.inside = true;
      if (b1Down(e) && e.isControlDown() || b3Down(e)) {
         onRight(e);
      } else if (b1Down(e)) {
         onLeft(e);
      } else if (b2Down(e)) {
         onMiddle(e);
      }
   }
   
}
