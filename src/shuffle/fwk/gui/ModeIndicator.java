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
import java.awt.Component;
import java.awt.Container;
import java.awt.FocusTraversalPolicy;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import shuffle.fwk.EntryMode;
import shuffle.fwk.config.ConfigManager;
import shuffle.fwk.gui.user.FocusRequester;
import shuffle.fwk.gui.user.ModeIndicatorUser;
import shuffle.fwk.i18n.I18nFactory;
import shuffle.fwk.i18n.I18nUser;

/**
 * @author Andrew Meyers
 *
 */
@SuppressWarnings("serial")
public class ModeIndicator extends JPanel implements FocusRequester, I18nUser {
   
   // Config keys
   private static final String KEY_MODE_SELECT_COLOR = "MODE_SELECT_COLOR";
   private static final String KEY_LABEL_FONT = "LABEL_FONT";
   private static final String KEY_MODE_FONT = "MODE_FONT";
   private static final String KEY_SELECT_THICK = "MODE_SELECT_THICK";
   // I18n keys
   private static final String KEY_MODE_TEXT = "mode.text";
   // Defaults
   private static final Font DEFAULT_FONT = new Font("Arial", Font.BOLD, 16);
   private static final int DEFAULT_SELECT_THICK = 2;
   
   private ModeIndicatorUser user;
   private EntryMode oldMode = null;
   
   private JLabel modeLabel;
   private Map<EntryMode, JLabel> modeMap = new LinkedHashMap<EntryMode, JLabel>();
   
   public ModeIndicator(ModeIndicatorUser user) {
      super();
      this.user = user;
      setup();
   }
   
   private ModeIndicatorUser getUser() {
      return user;
   }
   
   private void setup() {
      setLayout(new GridBagLayout());
      GridBagConstraints c = new GridBagConstraints();
      c.fill = GridBagConstraints.NONE;
      c.anchor = GridBagConstraints.LINE_START;
      c.gridx = 1;
      c.gridy = 1;
      c.weightx = 0.0;
      c.weighty = 0.0;
      c.insets = new Insets(1, 2, 1, 2);
      modeLabel = new JLabel(getString(KEY_MODE_TEXT));
      ConfigManager manager = getUser().getPreferencesManager();
      Font labelFont = manager.getFontValue(KEY_LABEL_FONT, DEFAULT_FONT);
      labelFont = new JLabel().getFont().deriveFont(labelFont.getStyle(), labelFont.getSize2D());
      Font modeFont = manager.getFontValue(KEY_MODE_FONT, DEFAULT_FONT);
      modeFont = new JLabel().getFont().deriveFont(modeFont.getStyle(), modeFont.getSize2D());
      int selectThick = manager.getIntegerValue(KEY_SELECT_THICK, DEFAULT_SELECT_THICK);
      modeLabel.setFont(labelFont);
      modeLabel.setForeground(getModeSelectColor());
      add(modeLabel, c);
      oldMode = getUser().getCurrentMode();
      for (EntryMode mode : EntryMode.values()) {
         JLabel label = new JLabel(getString(mode.getI18nKey()));
         label.setFont(modeFont);
         label.setForeground(getColorFor(mode));
         String modeTooltipKey = "tooltip." + mode.getI18nKey();
         String modeTooltipText = getString(modeTooltipKey);
         if (!modeTooltipKey.equals(modeTooltipText)) {
            label.setToolTipText(modeTooltipText);
         }
         setBorderFor(label, mode.equals(oldMode), selectThick);
         modeMap.put(mode, label);
         c.gridx++;
         label.addFocusListener(new FocusAdapter() {
            private final ModeIndicatorUser user = getUser();
            
            @Override
            public void focusGained(FocusEvent e) {
               user.setCurMode(mode);
            }
         });
         add(label, c);
      }
   }
   
   /**
    * @param mode
    * @return
    */
   private String getTextFor(EntryMode mode) {
      String key = mode.getI18nKey();
      String text = I18nFactory.getString(ModeIndicator.class, key);
      return text;
   }
   
   private boolean setBorderFor(JLabel label, boolean equals, int thickness) {
      if (label == null) {
         return false;
      }
      Border b;
      if (equals) {
         b = new LineBorder(getModeSelectColor(), thickness, true);
      } else {
         b = new EmptyBorder(thickness, thickness, thickness, thickness);
      }
      label.setBorder(b);
      return true;
   }
   
   private Color getModeSelectColor() {
      return getUser().getPreferencesManager().getColorFor(KEY_MODE_SELECT_COLOR);
   }
   
   private Color getColorFor(EntryMode mode) {
      return getUser().getPreferencesManager().getColorFor(mode);
   }
   
   public boolean updateMode() {
      EntryMode newMode = getUser().getCurrentMode();
      boolean changed = newMode != null && !newMode.equals(oldMode);
      for (EntryMode mode : EntryMode.values()) {
         if (modeMap.containsKey(mode)) {
            String text = getTextFor(mode);
            JLabel label = modeMap.get(mode);
            if (!text.equals(label.getText())) {
               label.setText(text);
               changed = true;
            }
         }
      }
      String newModeLabel = getString(KEY_MODE_TEXT);
      if (!modeLabel.getText().equals(newModeLabel)) {
         changed = true;
         modeLabel.setText(newModeLabel);
      }
      if (changed) {
         JLabel oldSelection = modeMap.get(oldMode);
         JLabel newSelection = modeMap.get(newMode);
         ConfigManager manager = getUser().getPreferencesManager();
         int selectThick = manager.getIntegerValue(KEY_SELECT_THICK, DEFAULT_SELECT_THICK);
         setBorderFor(oldSelection, false, selectThick);
         setBorderFor(newSelection, true, selectThick);
         oldMode = newMode;
      }
      if (!modeMap.get(newMode).hasFocus()) {
         modeMap.get(newMode).requestFocusInWindow();
      }
      return changed;
   }
   
   public void addActionListeners() {
      for (EntryMode mode : EntryMode.values()) {
         modeMap.get(mode).addMouseListener(new PressOrClickMouseAdapter() {
            @Override
            protected void onLeft(MouseEvent e) {
               getUser().setCurMode(mode);
               updateFocus();
            }
            
            @Override
            protected void onRight(MouseEvent e) {
               getUser().setCurMode(mode);
               updateFocus();
            }
            
            @Override
            protected void onEnter() {
               updateFocus();
            }
         });
         modeMap.get(mode).addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
               mode.handleKeyPress(getUser(), e);
            }
            
            @Override
            public void keyPressed(KeyEvent e) {
               if (e.getKeyChar() == KeyEvent.CHAR_UNDEFINED) {
                  mode.handleKeyPress(getUser(), e);
               }
            }
         });
      }
   }
   
   public void updateFocus(EntryMode toMode) {
      for (EntryMode mode : EntryMode.values()) {
         if (mode.equals(toMode)) {
            modeMap.get(mode).requestFocusInWindow();
         }
      }
   }
   
   public FocusTraversalPolicy getModeTraversalPolicy() {
      return new FocusTraversalPolicy() {
         
         @Override
         public Component getLastComponent(Container aContainer) {
            return modeMap.get(EntryMode.values()[EntryMode.values().length - 1]);
         }
         
         @Override
         public Component getFirstComponent(Container aContainer) {
            return modeMap.get(EntryMode.values()[0]);
         }
         
         @Override
         public Component getDefaultComponent(Container aContainer) {
            return modeMap.get(EntryMode.values()[0]);
         }
         
         @Override
         public Component getComponentBefore(Container aContainer, Component aComponent) {
            return modeMap.get(EntryMode.values()[getIndexOf(aComponent, -1)]);
         }
         
         @Override
         public Component getComponentAfter(Container aContainer, Component aComponent) {
            return modeMap.get(EntryMode.values()[getIndexOf(aComponent, 1)]);
         }
         
         private int getIndexOf(Component aComponent, int offset) {
            int indexOf = -1;
            for (int i = 0; i < EntryMode.values().length; i++) {
               if (modeMap.get(EntryMode.values()[i]).equals(aComponent)) {
                  indexOf = i;
               }
            }
            indexOf += offset;
            while (indexOf < 0) {
               indexOf += EntryMode.values().length;
            }
            while (indexOf >= EntryMode.values().length) {
               indexOf -= EntryMode.values().length;
            }
            return indexOf;
         }
      };
   }
   
   /*
    * (non-Javadoc)
    * @see shuffle.fwk.gui.user.FocusRequester#updateFocus()
    */
   @Override
   public void updateFocus() {
      updateFocus(getUser().getCurrentMode());
   }
   
}
