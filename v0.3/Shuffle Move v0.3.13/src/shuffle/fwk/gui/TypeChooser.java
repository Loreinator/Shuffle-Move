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

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

import org.apache.commons.lang3.text.WordUtils;

import shuffle.fwk.data.PkmType;
import shuffle.fwk.i18n.I18nUser;

/**
 * @author Andrew Meyers
 *
 */
public class TypeChooser extends JComboBox<String> implements I18nUser {
   private static final long serialVersionUID = -4886287658334768490L;
   // i18n keys
   private static final String KEY_NO_FILTER = "typechooser.nofilter";
   
   private boolean shouldRebuild = false;
   private final boolean includeNoFilter;
   
   public TypeChooser(boolean includeNoFilter) {
      super();
      this.includeNoFilter = includeNoFilter;
      setup();
   }
   
   /**
    * @param includeNoFilter
    *           TODO
    * 
    */
   private void setup() {
      refill();
      if (includeNoFilter) {
         setSelectedItem(getString(KEY_NO_FILTER));
      } else if (getItemCount() > 0) {
         setSelectedIndex(0);
      }
   }
   
   private void refill() {
      removeAllItems();
      if (includeNoFilter) {
         addItem(getString(KEY_NO_FILTER));
      }
      List<String> types = new ArrayList<String>();
      for (PkmType t : PkmType.values()) {
         if (PkmType.getMultiplier(PkmType.NORMAL, t) > 0) {
            types.add(WordUtils.capitalizeFully(t.toString()));
         }
      }
      Collections.sort(types, Collator.getInstance());
      for (String item : types) {
         addItem(item);
      }
   }
   
   public void setSelectedType(PkmType type) {
      String toSelect = null;
      if (type != null) {
         if (shouldRebuild) {
            refill();
         }
         toSelect = WordUtils.capitalizeFully(type.toString());
         DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) getModel();
         if (model.getIndexOf(toSelect) == -1) {
            shouldRebuild = true;
            insertItemAt(toSelect, 0);
         } else {
            shouldRebuild = false;
         }
      }
      setSelectedItem(toSelect);
   }
   
   public PkmType getSelectedType() {
      PkmType ret = null;
      int selectedIndex = getSelectedIndex();
      if (selectedIndex >= 0) {
         String itemAt = getItemAt(selectedIndex);
         if (PkmType.hasType(itemAt.toUpperCase())) {
            ret = PkmType.getType(itemAt);
         }
      }
      return ret;
   }
   
}
