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

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import shuffle.fwk.gui.user.MultiListenerUser;

/**
 * @author Andrew Meyers
 *
 */
public class MultiListener implements DocumentListener, ChangeListener, ItemListener {
   private final MultiListenerUser service;
   
   public MultiListener(MultiListenerUser service) {
      this.service = service;
   }
   
   private void updateService() {
      service.update();
   }
   
   @Override
   public void removeUpdate(DocumentEvent e) {
      changedUpdate(e);
   }
   
   @Override
   public void insertUpdate(DocumentEvent e) {
      changedUpdate(e);
   }
   
   @Override
   public void changedUpdate(DocumentEvent e) {
      updateService();
   }
   
   @Override
   public void itemStateChanged(ItemEvent e) {
      updateService();
   }
   
   @Override
   public void stateChanged(ChangeEvent e) {
      updateService();
   }
}
