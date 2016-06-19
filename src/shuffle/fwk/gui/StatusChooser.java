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

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

import shuffle.fwk.data.Board.Status;
import shuffle.fwk.i18n.I18nUser;

/**
 * @author Andrew Meyers
 *         
 */
public class StatusChooser extends JComboBox<String>implements I18nUser {
   private static final long serialVersionUID = 7503566089115860009L;
   
   private boolean shouldRebuild = false;
   
   public StatusChooser() {
      super();
      setup();
   }
   
   /**
    * Sets up the entries
    */
   private void setup() {
      refill();
      setSelectedItem(getString(Status.NONE.getKey()));
   }
   
   /**
    * @param megaEffects
    */
   private void refill() {
      removeAllItems();
      for (Status s : Status.values()) {
         String entry = getString(s.getKey());
         addItem(entry);
      }
   }
   
   public void setSelectedStatus(Status status) {
      String toSelect = null;
      if (status != null) {
         if (shouldRebuild) {
            refill();
         }
         toSelect = getString(status.getKey());
         DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) getModel();
         if (model.getIndexOf(toSelect) == -1) {
            shouldRebuild = true;
            addItem(toSelect);
         } else {
            shouldRebuild = false;
         }
      }
      setSelectedItem(toSelect);
   }
   
   public Status getSelectedStatus() {
      Status ret = Status.NONE;
      int selectedIndex = getSelectedIndex();
      if (selectedIndex >= 0 && selectedIndex < Status.values().length) {
         ret = Status.values()[selectedIndex];
      }
      return ret;
   }
}
