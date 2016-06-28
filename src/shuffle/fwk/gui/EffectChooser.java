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
import shuffle.fwk.data.Species;
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
   private final DefaultEntry defaultEntry;
   private Species curSpecies = null;
   
   public static enum DefaultEntry {
      /**
       * Indicates that the "no filter" entry should be used.
       */
      NO_FILTER,
      /**
       * Indicates that the "none" entry should be used.
       */
      NONE,
      /**
       * Indicates that NO entry should be used.
       */
      EMPTY, /**
              * Indicates that this will only display the possible effects for a given species.
              */
      SPECIES;
   };

   public EffectChooser() {
      this(false, DefaultEntry.EMPTY);
   }

   public EffectChooser(boolean megas) {
      this(megas, DefaultEntry.EMPTY);
   }
   
   public EffectChooser(boolean megas, DefaultEntry defaultEntry) {
      super();
      megaEffects = megas;
      this.defaultEntry = defaultEntry;
      setup();
   }
   
   /**
    * Sets up the entries
    */
   private void setup() {
      refill();
      if (defaultEntry.equals(DefaultEntry.NO_FILTER)) {
         setSelectedItem(getString(KEY_NO_FILTER));
      } else if (getItemCount() > 0 && defaultEntry.equals(DefaultEntry.NONE)) {
         setSelectedItem(convertToBox(Effect.NONE.toString()));
      }
   }
   
   /**
    * @param megaEffects
    */
   private void refill() {
      removeAllItems();
      if (defaultEntry.equals(DefaultEntry.NO_FILTER)) {
         addItem(getString(KEY_NO_FILTER));
      }
      if (defaultEntry.equals(DefaultEntry.SPECIES)) {
         if (curSpecies != null) {
            List<String> effects = new ArrayList<String>();
            for (Effect t : curSpecies.getEffects()) {
               if (t.canLevel() && (!t.isPersistent() || t.equals(Effect.NONE))) {
                  effects.add(convertToBox(t.toString()));
               }
            }
            Collections.sort(effects, Collator.getInstance());
            for (String item : effects) {
               addItem(item);
            }
         }
      } else {
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
   }
   
   public void setSpecies(Species s) {
      curSpecies = s;
      refill();
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
   
   /**
    * Converts a given string, s, from the Enumerator name to an nicely legible name. Any occurrence
    * of "_P_" becomes a "+_". Then, any trailing "_P" become "+". Finally, all "_" are replaced by
    * spaces and the string is fully capitalized.
    * 
    * @param s
    * @return
    */
   public static String convertToBox(String s) {
      String temp = s.replaceAll("_P_", "+_");
      if (temp.endsWith("_P")) {
         temp = temp.substring(0, temp.length() - 2) + "+";
      }
      temp = temp.replaceAll("_", " ");
      return WordUtils.capitalizeFully(temp);
   }
   
   /**
    * Reverses the effect of {@link #convertToBox(String)}. Replaces all "+" with "_P", and all
    * spaces with "_".
    * 
    * @param s
    * @return
    */
   public static String convertFromBox(String s) {
      StringBuilder sb = new StringBuilder();
      for (char c : s.toCharArray()) {
         if (c == '+') {
            sb.append("_P");
         } else if (c == ' ') {
            sb.append('_');
         } else {
            sb.append(Character.toUpperCase(c));
         }
      }
      return sb.toString();
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
