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

package shuffle.fwk.service.movepreferences;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.text.ParseException;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import shuffle.fwk.i18n.I18nUser;
import shuffle.fwk.service.BaseService;
import shuffle.fwk.service.DisposeAction;

/**
 * @author Andrew Meyers
 *
 */
public class MovePreferencesService extends BaseService<MovePreferencesServiceUser> implements I18nUser {
   
   private static final Logger LOG = Logger.getLogger(MovePreferencesService.class.getName());
   
   private static final String KEY_AUTOCOMPUTE = "text.autocompute";
   private static final String KEY_NUMBER_FEEDERS = "text.numberfeeders";
   private static final String KEY_HEIGHT_FEEDERS = "text.heightfeeders";
   private static final String KEY_OK = "button.ok";
   private static final String KEY_APPLY = "button.apply";
   private static final String KEY_CANCEL = "button.cancel";
   private static final String KEY_BAD_NUM = "error.numberfeeders";
   private static final String KEY_BAD_HEIGHT = "error.heightfeeders";
   
   private JSpinner numFeederSpinner;
   private JSpinner feederHeightSpinner;
   private JCheckBox autoComputeCheckBox;
   
   /*
    * (non-Javadoc)
    * @see shuffle.fwk.service.BaseService#getUserClass()
    */
   @Override
   protected Class<MovePreferencesServiceUser> getUserClass() {
      return MovePreferencesServiceUser.class;
   }
   
   /*
    * (non-Javadoc)
    * @see shuffle.fwk.service.BaseService#setupGUI()
    */
   @Override
   public void onSetupGUI() {
      JDialog d = new JDialog(getOwner());
      numFeederSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 5000, 1));
      feederHeightSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 36, 1));
      autoComputeCheckBox = new JCheckBox(getString(KEY_AUTOCOMPUTE));
      
      d.setLayout(new GridBagLayout());
      
      GridBagConstraints c = new GridBagConstraints();
      c.fill = GridBagConstraints.BOTH;
      c.anchor = GridBagConstraints.CENTER;
      c.weightx = 1.0;
      c.weighty = 1.0;
      
      int maxWidth = 3;
      
      c.gridx = 1;
      c.gridy = 1;
      c.gridwidth = maxWidth;
      JPanel numPanel = new JPanel(new BorderLayout());
      numPanel.add(new JLabel(getString(KEY_NUMBER_FEEDERS)), BorderLayout.WEST);
      numPanel.add(numFeederSpinner, BorderLayout.EAST);
      d.add(numPanel, c);
      
      c.gridx = 1;
      c.gridy = 2;
      c.gridwidth = maxWidth;
      JPanel heightPanel = new JPanel(new BorderLayout());
      heightPanel.add(new JLabel(getString(KEY_HEIGHT_FEEDERS)), BorderLayout.WEST);
      heightPanel.add(feederHeightSpinner, BorderLayout.EAST);
      d.add(heightPanel, c);
      
      c.gridx = 1;
      c.gridy = 3;
      c.gridwidth = maxWidth;
      d.add(autoComputeCheckBox, c);
      
      c.gridx = 1;
      c.gridy = 4;
      c.gridwidth = 1;
      @SuppressWarnings("serial")
      JButton okButton = new JButton(new AbstractAction(getString(KEY_OK)) {
         @Override
         public void actionPerformed(ActionEvent arg0) {
            onApply();
            dispose();
         }
      });
      d.add(okButton, c);
      
      c.gridx += 1;
      @SuppressWarnings("serial")
      JButton applyButton = new JButton(new AbstractAction(getString(KEY_APPLY)) {
         
         @Override
         public void actionPerformed(ActionEvent arg0) {
            onApply();
         }
      });
      d.add(applyButton, c);
      
      c.gridx += 1;
      JButton cancelButton = new JButton(new DisposeAction(getString(KEY_CANCEL), this));
      d.add(cancelButton, c);
      d.pack();
      d.repaint();
      d.setLocationRelativeTo(null);
      
      setDialog(d);
   }
   
   private void onApply() {
      try {
         numFeederSpinner.commitEdit();
      } catch (ParseException e) {
         LOG.info(getString(KEY_BAD_NUM));
      }
      int numFeeders = (Integer) numFeederSpinner.getValue();
      
      try {
         feederHeightSpinner.commitEdit();
      } catch (ParseException e) {
         LOG.info(getString(KEY_BAD_HEIGHT));
      }
      int feederHeight = (Integer) feederHeightSpinner.getValue();
      
      boolean autoCompute = autoComputeCheckBox.isSelected();
      
      getUser().setFeederPreferences(numFeeders, feederHeight, autoCompute);
   }
   
   /*
    * (non-Javadoc)
    * @see shuffle.fwk.service.BaseService#updateGUIFrom(java.lang.Object)
    */
   @Override
   protected void updateGUIFrom(MovePreferencesServiceUser user) {
      numFeederSpinner.setValue(user.getPreferredNumFeeders());
      feederHeightSpinner.setValue(user.getPreferredFeederHeight());
      autoComputeCheckBox.setSelected(user.isAutoCompute());
   }
   
}
