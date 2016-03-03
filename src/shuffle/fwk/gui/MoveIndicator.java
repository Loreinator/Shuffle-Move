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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JLabel;
import javax.swing.JPanel;

import shuffle.fwk.config.ConfigManager;
import shuffle.fwk.data.simulation.SimulationResult;
import shuffle.fwk.gui.user.MoveIndicatorUser;
import shuffle.fwk.i18n.I18nUser;

/**
 * @author Andrew Meyers
 *
 */
@SuppressWarnings("serial")
public class MoveIndicator extends JPanel implements I18nUser {
   private static final Logger LOG = Logger.getLogger(MoveIndicator.class.getName());
   static {
      LOG.setLevel(Level.FINE);
   }
   
   // Config keys
   public static final String KEY_SCORE_COLOR = "SCORE_COLOR";
   private static final String KEY_MOVE_FONT = "MOVE_FONT";
   // i18n keys
   private static final String KEY_SCORE_TO_MOVE = "separator.scoretomove";
   private static final String KEY_PICK_TO_DROP = "separator.picktodrop";
   private static final String KEY_TEXT_MOVE = "text.move";
   private static final String KEY_TEXT_NOMOVE = "text.nomove";
   private static final String KEY_TEXT_SETTLE = "text.settle";
   private static final String KEY_FORMAT_SCORE = "format.score";
   private static final String KEY_FORMAT_GOLDSCORE = "format.goldscore";
   private static final String KEY_FORMAT_COORDINATES = "format.coordinates";
   // Defaults
   private static final Font DEFAULT_FONT_USED = new Font("Arial", 0, 20);
   
   private final MoveIndicatorUser user;
   
   private JLabel messageLabel;
   private JLabel scoreLabel;
   private JLabel pickupLabel;
   private JLabel dropatLabel;
   
   private String prevMessage = null;
   private Integer score = null;
   private Integer gold = null;
   private List<Integer> prevPickup = new ArrayList<Integer>();
   private List<Integer> prevDropat = new ArrayList<Integer>();
   
   public MoveIndicator(MoveIndicatorUser user) {
      super();
      this.user = user;
      setup();
   }
   
   private MoveIndicatorUser getUser() {
      return user;
   }
   
   private void setup() {
      ConfigManager manager = getUser().getPreferencesManager();
      Color scoreColor = manager.getColorFor(KEY_SCORE_COLOR);
      Font fontToUse = manager.getFontValue(KEY_MOVE_FONT, DEFAULT_FONT_USED);
      fontToUse = new JLabel().getFont().deriveFont(fontToUse.getStyle(), fontToUse.getSize2D());
      fontToUse = getUser().scaleFont(fontToUse);
      messageLabel = new JLabel(getString(KEY_TEXT_NOMOVE));
      messageLabel.setFont(fontToUse);
      if (scoreColor != null) {
         messageLabel.setForeground(scoreColor);
      }
      
      String text = getTextForScore(0, 0);
      scoreLabel = new JLabel(text);
      scoreLabel.setFont(fontToUse);
      if (scoreColor != null) {
         scoreLabel.setForeground(scoreColor);
      }
      
      pickupLabel = new JLabel(getTextForCoord(null));
      pickupLabel.setFont(fontToUse);
      Color pickupColor = manager.getColorFor(GridPanel.KEY_PICKUP_COLOR);
      if (pickupColor != null) {
         pickupLabel.setForeground(pickupColor);
      }
      
      dropatLabel = new JLabel(getTextForCoord(null));
      dropatLabel.setFont(fontToUse);
      Color dropatColor = manager.getColorFor(GridPanel.KEY_DROPAT_COLOR);
      if (dropatColor != null) {
         dropatLabel.setForeground(dropatColor);
      }
      
      JLabel leftSpacer = new JLabel(getString(KEY_SCORE_TO_MOVE));
      leftSpacer.setFont(fontToUse);
      
      JLabel rightSpacer = new JLabel(getString(KEY_PICK_TO_DROP));
      rightSpacer.setFont(fontToUse);
      
      setLayout(new GridBagLayout());
      GridBagConstraints c = new GridBagConstraints();
      c.fill = GridBagConstraints.BOTH;
      c.anchor = GridBagConstraints.LINE_END;
      c.gridx = 1;
      c.gridy = 1;
      c.weightx = 0.0;
      c.weighty = 0.0;
      
      c.gridx++;
      c.weightx = 1.0;
      add(messageLabel, c);
      c.weightx = 0.0;
      
      c.gridx++;
      add(scoreLabel, c);
      
      c.gridx++;
      add(leftSpacer, c);
      
      c.gridx++;
      add(pickupLabel, c);
      
      c.gridx++;
      add(rightSpacer, c);
      
      c.gridx++;
      add(dropatLabel, c);
      
      repaint();
   }
   
   public boolean updateMove() {
      SimulationResult result = getUser().getSelectedResult();
      
      Integer score = 0;
      Integer gold = 0;
      if (result != null) {
         score = result.getNetScore().intValue();
         gold = result.getNetGold().intValue();
      }
      
      List<Integer> move = new ArrayList<Integer>();
      List<Integer> pickup = new ArrayList<Integer>();
      List<Integer> dropat = new ArrayList<Integer>();
      if (result != null) {
         move = result.getMove();
         if (move.size() >= 2) {
            pickup.addAll(move.subList(0, 2));
            if (move.size() >= 4) {
               dropat.addAll(move.subList(2, 4));
            }
         }
      }
      
      boolean changed = false;
      if (!pickup.equals(prevPickup)) {
         changed = true;
         pickupLabel.setText(getTextForCoord(pickup));
         prevPickup = pickup;
         pickupLabel.repaint();
      }
      
      if (!dropat.equals(prevDropat)) {
         changed = true;
         dropatLabel.setText(getTextForCoord(dropat));
         prevDropat = dropat;
         dropatLabel.repaint();
      }
      boolean resultChanged = false;
      if (!score.equals(this.score) || !gold.equals(this.gold)) {
         changed = true;
         String text = getTextForScore(gold, score);
         scoreLabel.setText(text);
         this.score = score;
         this.gold = gold;
         resultChanged = true;
         scoreLabel.repaint();
      }
      
      String newMessage;
      if (result == null) {
         newMessage = getString(KEY_TEXT_NOMOVE);
      } else if (move == null || move.isEmpty()) {
         newMessage = getString(KEY_TEXT_SETTLE);
      } else {
         newMessage = getString(KEY_TEXT_MOVE);
      }
      if (!newMessage.equals(prevMessage)) {
         resultChanged = true;
         prevMessage = newMessage;
         messageLabel.setText(newMessage);
         messageLabel.repaint();
      }
      if (resultChanged) {
         LOG.fine("Result set: " + String.valueOf(result));
         LOG.fine("New message is: " + newMessage);
      }
      
      return changed;
   }
   
   /**
    * @param score
    * @return
    */
   private String getTextForScore(Integer gold, Integer score) {
      String text = null;
      if (score == 0 && gold == 0) {
         text = "?";
      } else if (gold > 0) {
         text = getString(KEY_FORMAT_GOLDSCORE, gold, score);
      } else {
         text = getString(KEY_FORMAT_SCORE, score);
      }
      return text;
   }
   
   /**
    * @param coord
    * @return
    */
   private String getTextForCoord(List<Integer> coord) {
      String ret;
      if (coord == null || coord.size() < 2) {
         ret = getString(KEY_FORMAT_COORDINATES, "?", "?");
      } else {
         ret = getString(KEY_FORMAT_COORDINATES, coord.get(0), coord.get(1));
      }
      return ret;
   }
}
