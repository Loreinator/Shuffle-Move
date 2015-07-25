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

import shuffle.fwk.data.Effect;
import shuffle.fwk.i18n.I18nUser;

/**
 * @author Andrew Meyers
 *
 */
public class EffectChooser extends JComboBox<String> implements I18nUser {
   private static final long serialVersionUID = -403530675414799097L;
   // i18n keys
   private static final String KEY_NO_FILTER = "typechooser.nofilter";
   
   private boolean shouldRebuild = false;
   private boolean megaEffects;
   private final boolean includeNoFilter;
   
   public EffectChooser(boolean megas) {
      this(megas, false);
   }
   
   public EffectChooser(boolean megas, boolean includeNoFilter) {
      super();
      megaEffects = megas;
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
         setSelectedItem(convertToBox(Effect.NONE.toString()));
      }
   }
   
   /**
    * @param megaEffects
    */
   private void refill() {
      removeAllItems();
      if (includeNoFilter) {
         addItem(getString(KEY_NO_FILTER));
      }
      List<String> effects = new ArrayList<String>();
      for (Effect t : Effect.values()) {
         if (t.canLevel() && (megaEffects == t.isPersistent() || t.equals(Effect.NONE))) {
            effects.add(convertToBox(t.toString()));
         }
      }
      Collections.sort(effects, Collator.getInstance());
      for (String item : effects) {
         addItem(item);
      }
   }
   
   public void setSelectedEffect(Effect effect) {
      String toSelect = null;
      if (effect != null) {
         if (shouldRebuild) {
            refill();
         }
         toSelect = convertToBox(effect.toString());
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
   
   private String convertToBox(String s) {
      String cap = WordUtils.capitalizeFully(s);
      String temp = cap.replaceAll("_", " ");
      if (temp.endsWith(" p p")) {
         return temp.substring(0, temp.length() - 4) + "++";
      } else if (temp.endsWith(" p")) {
         return temp.substring(0, temp.length() - 2) + "+";
      } else {
         return temp;
      }
   }
   
   private String convertFromBox(String s) {
      String ret = s.toUpperCase();
      String temp = ret.replaceAll(" ", "_");
      if (temp.endsWith("++")) {
         return temp.substring(0, temp.length() - 2) + "_P_P";
      } else if (temp.endsWith("+")) {
         return temp.substring(0, temp.length() - 1) + "_P";
      } else {
         return temp;
      }
   }
   
   public Effect getSelectedEffect() {
      Effect ret = null;
      int selectedIndex = getSelectedIndex();
      if (selectedIndex >= 0) {
         String itemAt = convertFromBox(getItemAt(selectedIndex));
         try {
            ret = Enum.valueOf(Effect.class, itemAt);
         } catch (IllegalArgumentException iae) {
            // do nothing.
         }
      }
      return ret;
   }
}
