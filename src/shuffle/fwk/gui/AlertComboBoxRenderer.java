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

import java.awt.Component;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 * @author Andrew Meyers
 *
 */
@SuppressWarnings("serial")
public abstract class AlertComboBoxRenderer extends JLabel implements ListCellRenderer<String> {
   
   private final ListCellRenderer<? super String> def;
   
   public AlertComboBoxRenderer() {
      def = new JComboBox<String>().getRenderer();
   }
   
   /*
    * (non-Javadoc)
    * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList,
    * java.lang.Object, int, boolean, boolean)
    */
   @Override
   public Component getListCellRendererComponent(JList<? extends String> list, String value, int index,
         boolean isSelected, boolean cellHasFocus) {
      Component ret = def.getListCellRendererComponent(list, index >= 0 ? getPreparedText(value) : value, index,
            isSelected, cellHasFocus);
      return ret;
   }
   
   public abstract int getPreferredWidth();
   
   private String getPreparedText(String text) {
      StringBuilder sb = new StringBuilder(100 + (text == null ? 0 : text.length()));
      sb.append("<html><p style=\"word-wrap: break-word;width: ");
      sb.append(getPreferredWidth());
      sb.append("px;\">");
      sb.append(text);
      sb.append("</p></html>");
      return sb.toString();
   }
}
