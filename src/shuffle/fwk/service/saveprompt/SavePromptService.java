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

package shuffle.fwk.service.saveprompt;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;

import shuffle.fwk.i18n.I18nUser;
import shuffle.fwk.service.BaseService;

/**
 * @author Andrew Meyers
 *
 */
public class SavePromptService extends BaseService<SavePromptServiceUser> implements I18nUser {
   
   private static final String KEY_TITLE = "text.title";
   private static final String KEY_SAVE = "button.saveandexit";
   private static final String KEY_EXIT = "button.exit";
   private static final String KEY_CANCEL = "button.cancel";
   private static final String KEY_SAVE_MESSAGE = "message.savechanges";
   
   /*
    * (non-Javadoc)
    * @see shuffle.fwk.service.BaseService#getUserClass()
    */
   @Override
   protected Class<SavePromptServiceUser> getUserClass() {
      return SavePromptServiceUser.class;
   }
   
   /*
    * (non-Javadoc)
    * @see shuffle.fwk.service.BaseService#onSetupGUI()
    */
   @Override
   protected void onSetupGUI() {
      JButton saveButton = new JButton(new AbstractAction(getString(KEY_SAVE)) {
         private static final long serialVersionUID = 7003381946839386811L;

         @Override
         public void actionPerformed(ActionEvent e) {
            SavePromptService.this.getUser().saveAll();
            closeProgram();
         }
      });
      JButton closeButton = new JButton(new AbstractAction(getString(KEY_EXIT)) {
         private static final long serialVersionUID = -128415154672789095L;

         @Override
         public void actionPerformed(ActionEvent e) {
            closeProgram();
         }
      });
      JButton cancelButton = new JButton(new AbstractAction(getString(KEY_CANCEL)) {
         private static final long serialVersionUID = -1652264212207135433L;

         @Override
         public void actionPerformed(ActionEvent e) {
            SavePromptService.this.dispose();
         }
      });
      
      JDialog d = new JDialog(getOwner(), getString(KEY_TITLE));
      d.setLayout(new GridBagLayout());
      GridBagConstraints c = new GridBagConstraints();
      c.fill = GridBagConstraints.BOTH;
      c.anchor = GridBagConstraints.CENTER;
      
      c.gridy++;
      c.weighty = 1.0;
      c.weightx = 1.0;
      c.gridx = 1;
      c.gridwidth = 3;
      JLabel label = new JLabel(getString(KEY_SAVE_MESSAGE));
      label.setHorizontalAlignment(JLabel.CENTER);
      d.add(label, c);
      
      c.gridy++;
      c.weighty = 0.0;
      c.weightx = 1.0;
      c.gridx = 0;
      c.gridwidth = 1;
      
      c.gridx++;
      d.add(saveButton, c);
      
      c.gridx++;
      d.add(closeButton, c);
      
      c.gridx++;
      d.add(cancelButton, c);
      
      d.pack();
      d.repaint();
      d.setResizable(false);
      d.setLocationRelativeTo(null);
      setDialog(d);
   }
   
   private void closeProgram() {
      System.exit(0);
   }

   /*
    * (non-Javadoc)
    * @see shuffle.fwk.service.BaseService#updateGUIFrom(java.lang.Object)
    */
   @Override
   protected void updateGUIFrom(SavePromptServiceUser user) {
      // Nothing to do
   }
   
}
