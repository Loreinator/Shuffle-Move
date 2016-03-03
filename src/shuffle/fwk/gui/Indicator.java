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

import java.awt.Color;
import java.awt.Font;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import shuffle.fwk.config.ConfigManager;
import shuffle.fwk.config.manager.ImageManager;
import shuffle.fwk.gui.user.IndicatorUser;

/**
 * @author Andrew Meyers
 *
 */
@SuppressWarnings("serial")
public class Indicator<Y extends Object> extends JLabel {
   
   private static String defaultDisplayText = "NYS";
   
   private static final Font DEFAULT_FONT = new Font("Arial", Font.BOLD, 20);
   private static final String KEY_INDICATOR_FONT = "INDICATOR_FONT";
   
   private IndicatorUser<Object> user;
   private ImageIcon curIcon = null;
   private String curText = null;
   private Color curIndicatorColor = null;
   private Integer maxTextLength = null;
   private Y visualizedValue = null;
   
   public Indicator(IndicatorUser<Object> user) {
      this(user, null, null);
   }
   
   public Indicator(IndicatorUser<Object> user, Integer maxTextLength) {
      this(user, null, maxTextLength);
   }
   
   public Indicator(IndicatorUser<Object> user, String defaultText) {
      this(user, defaultText, null);
   }
   
   public Indicator(IndicatorUser<Object> user, String defaultText, Integer maxTextLength) {
      super();
      this.user = user;
      if (defaultText != null) { // cannot have a null fallback.
         defaultDisplayText = defaultText;
      }
      this.maxTextLength = maxTextLength;
      ConfigManager manager = user.getPreferencesManager();
      Font fontToUse = manager.getFontValue(KEY_INDICATOR_FONT, DEFAULT_FONT);
      fontToUse = this.user.scaleFont(fontToUse);
      setupIndicator(fontToUse);
   }
   
   public Y getValue() {
      return visualizedValue;
   }
   
   private final void setupIndicator(Font f) {
      setAlignmentX(CENTER_ALIGNMENT);
      setAlignmentY(CENTER_ALIGNMENT);
      setFont(f);
      setHorizontalAlignment(SwingConstants.CENTER);
      setHorizontalTextPosition(SwingConstants.CENTER);
      setVerticalAlignment(SwingConstants.CENTER);
      setVerticalTextPosition(SwingConstants.BOTTOM);
      setVisualized(null);
   }
   
   private final String trimText(String s) {
      String ret = s;
      if (ret != null && maxTextLength != null && ret.length() > maxTextLength) {
         ret = ret.substring(0, maxTextLength);
      }
      return ret;
   }
   
   /**
    * Sets the indicator to visualize the selected value only.
    * 
    * @param value
    * @return
    */
   public final boolean setVisualized(Y value) {
      return setVisualized(value, null);
   }
   
   /**
    * Sets the indicator to visualize the selected value as an image with the given text as the
    * label below. If there is no such image, then the given text will be the only used text.
    * 
    * @param value
    * @param text
    * @return
    */
   public final boolean setVisualized(Y value, String text) {
      ImageIcon icon = getImageManager().getImageFor(value);
      String textToUse = trimText(text);
      if (textToUse == null && icon == null) { // null image and can't find the icon
         textToUse = trimText(getTextForValue(value)); // we fall back to visualization text
         if (textToUse == null) { // and if that fallback fails,
            textToUse = defaultDisplayText; // we then use defaultDisplayText
         }
      }
      Color indicatorColor = null;
      if (icon == null) { // no icon, we need to get a background color
         indicatorColor = getPreferencesManager().getColorFor(value);
      }
      boolean changing = setIndicatorIcon(icon);
      changing |= setIndicatorText(textToUse);
      changing |= setIndicatorColor(indicatorColor);
      visualizedValue = value;
      return changing;
   }
   
   private final boolean setIndicatorIcon(ImageIcon icon) {
      boolean changed = areDifferent(icon, curIcon);
      if (changed) {
         curIcon = icon;
         setIcon(curIcon);
      }
      return changed;
   }
   
   private final boolean setIndicatorText(String text) {
      boolean changed = areDifferent(text, curText);
      if (changed) {
         curText = text;
         setText(curText);
      }
      return changed;
   }
   
   private final boolean setIndicatorColor(Color indicatorColor) {
      boolean changed = areDifferent(indicatorColor, curIndicatorColor);
      if (changed) {
         curIndicatorColor = indicatorColor;
         setBackground(curIndicatorColor);
      }
      return changed;
   }
   
   private boolean areDifferent(Object o1, Object o2) {
      return !(o1 == o2 || o1 != null && o1.equals(o2));
   }
   
   /**
    * @param value
    * @return
    */
   private String getTextForValue(Y value) {
      return user.getTextFor(value);
   }
   
   private final ConfigManager getPreferencesManager() {
      return user.getPreferencesManager();
   }
   
   private final ImageManager getImageManager() {
      return user.getImageManager();
   }
   
   public final void dispose() {
      user = null;
      curIcon = null;
      onDispose();
      removeAll();
   }
   
   protected void onDispose() {
      // nothing to do, default implementation.
   }
}
