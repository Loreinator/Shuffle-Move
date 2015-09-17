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
import java.util.Locale;

import javax.swing.JComboBox;

import shuffle.fwk.GradingMode;
import shuffle.fwk.gui.user.GradingModeUser;
import shuffle.fwk.i18n.I18nUser;

/**
 * @author Andrew Meyers
 *
 */
public class GradingModeIndicator extends JComboBox<String> implements I18nUser {
   private static final long serialVersionUID = 357455163731560210L;
   
   private final GradingModeUser user;
   private final ItemListener listener;
   private boolean ready = false;
   private Locale lastLocale = Locale.getDefault();
   
   public GradingModeIndicator(GradingModeUser user) {
      super();
      this.user = user;
      listener = new ItemListener() {
         @Override
         public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
               pushSelectedMode();
            }
         }
      };
      setup();
   }
   
   private GradingModeUser getUser() {
      return user;
   }
   
   private void setup() {
      rebuildItems();
      addItemListener(listener);
      updateSelection();
      ready = true;
      repaint();
   }
   
   /**
    * 
    */
   private void rebuildItems() {
      removeAllItems();
      for (GradingMode mode : GradingMode.values()) {
         addItem(mode.geti18nString());
      }
   }
   
   private void pushSelectedMode() {
      String curSelection = getItemAt(getSelectedIndex());
      if (curSelection != null) {
         GradingMode toSet = null;
         for (GradingMode mode : GradingMode.values()) {
            if (mode.geti18nString().equals(curSelection)) {
               toSet = mode;
            }
         }
         if (toSet != null) {
            getUser().setGradingMode(toSet);
         }
      }
   }
   
   /**
    * 
    */
   private void updateSelection() {
      if (ready && getUser() != null && listener != null) {
         removeItemListener(listener);
         if (lastLocale == null || !lastLocale.equals(Locale.getDefault())) {
            lastLocale = Locale.getDefault();
            rebuildItems();
         }
         GradingMode curMode = getUser().getCurrentGradingMode();
         if (curMode == null) {
            setSelectedItem(null);
         } else {
            setSelectedItem(curMode.geti18nString());
         }
         addItemListener(listener);
      }
   }
   
   @Override
   public void repaint() {
      updateSelection();
      super.repaint();
   }
   
}
