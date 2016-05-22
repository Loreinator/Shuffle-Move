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

import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.SwingUtilities;

/**
 * @author Andrew Meyers
 *
 */
public abstract class PressOrClickMouseAdapter extends MouseAdapter {
   
   // Static so this is maintained across all mouse adapters
   protected static boolean inside = false;
   
   protected abstract void onLeft(MouseEvent e);
   
   protected abstract void onRight(MouseEvent e);
   
   protected void onMiddle(MouseEvent e) {
      // do nothing by default.
   }
   
   protected abstract void onEnter();
   
   protected boolean ignoreClick() {
      return false;
   }

   @Override
   public void mouseClicked(MouseEvent e) {
      if (ignoreClick()) {
         return;
      }
      if (SwingUtilities.isRightMouseButton(e) | SwingUtilities.isLeftMouseButton(e) && e.isControlDown()) {
         onRight(e);
      } else if (SwingUtilities.isLeftMouseButton(e)) {
         onLeft(e);
      } else if (SwingUtilities.isMiddleMouseButton(e)) {
         onMiddle(e);
      }
   }
   
   @Override
   public void mouseEntered(MouseEvent e) {
      onEnter();
      if (inside) {
         if (b1Down(e) && e.isControlDown() || b3Down(e)) {
            onRight(e);
         } else if (b1Down(e)) {
            onLeft(e);
         } else if (b2Down(e)) {
            onMiddle(e);
         }
      }
   }
   
   @Override
   public void mouseReleased(MouseEvent e) {
      // System.out.println("Mouse Released");
      inside = false; // This seems to be enough to stop spill over from menu clicks
   }
   
   protected final boolean b1Down(MouseEvent e) {
      return (e.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK) != 0;
   }
   
   protected final boolean b2Down(MouseEvent e) {
      return (e.getModifiersEx() & InputEvent.BUTTON2_DOWN_MASK) != 0;
   }
   
   protected final boolean b3Down(MouseEvent e) {
      return (e.getModifiersEx() & InputEvent.BUTTON3_DOWN_MASK) != 0;
   }
   
   @Override
   public void mousePressed(MouseEvent e) {
      inside = true;
      System.out.println("Mouse Pressed, calling mouseEntered");
      mouseEntered(e);
   }
}
