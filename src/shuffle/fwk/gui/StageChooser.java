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

import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;
import java.util.HashSet;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import shuffle.fwk.data.PkmType;
import shuffle.fwk.data.Stage;
import shuffle.fwk.gui.user.StageIndicatorUser;
import shuffle.fwk.i18n.I18nUser;

/**
 * @author Andrew Meyers
 *
 */
@SuppressWarnings("serial")
public class StageChooser extends JPanel implements I18nUser {
   
   public static final int TEXT_LIMIT = 15;
   
   private static final String KEY_FILTER_TOOLTIP = "tooltip.filter";
   private static final String KEY_STAGE_LIST_TOOLTIP = "tooltip.stagelist";
   
   private JComboBox<Stage> stageComboBox;
   private Indicator<PkmType> stageTypeIndicator;
   private ItemListener il = null;
   private Collection<Stage> curStages = new HashSet<Stage>();
   private Stage curStage = null;
   private StageIndicatorUser user;
   private JTextField textField;
   private JSpinner levelSpinner = null;
   
   private String lastFilter = "";
   
   public StageChooser(StageIndicatorUser user) {
      super();
      this.user = user;
      setup();
   }
   
   private StageIndicatorUser getUser() {
      return user;
   }
   
   private void setup() {
      
      stageComboBox = new JComboBox<Stage>();
      curStages = getUser().getAllStages();
      for (Stage stage : curStages) {
         stageComboBox.addItem(stage);
      }
      Stage currentStage = getUser().getCurrentStage();
      stageComboBox.setSelectedItem(currentStage.getName());
      il = new ItemListener() {
         @Override
         public void itemStateChanged(ItemEvent arg0) {
            getUser().setCurrentStage(stageComboBox.getItemAt(stageComboBox.getSelectedIndex()));
         }
      };
      stageComboBox.addItemListener(il);
      stageTypeIndicator = new Indicator<PkmType>(getUser());
      textField = new JTextField(lastFilter) {
         @Override
         public Dimension getMinimumSize() {
            Dimension d = super.getPreferredSize();
            d.width = Math.max(20, d.width);
            return d;
         }
         
         @Override
         public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            d.width = Math.max(20, d.width);
            return d;
         }
      };
      PlainDocument doc = new PlainDocument() {
         @Override
         public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
            if (str == null) {
               return;
            }
            int lenAllowed = TEXT_LIMIT - getLength();
            String toInsert = str == null ? "" : str.substring(0, Math.min(lenAllowed, str.length()));
            
            if (getLength() + toInsert.length() <= TEXT_LIMIT) {
               super.insertString(offset, toInsert, attr);
            }
         }
      };
      doc.addDocumentListener(new DocumentListener() {
         @Override
         public void removeUpdate(DocumentEvent e) {
            changedUpdate(e);
         }
         
         @Override
         public void insertUpdate(DocumentEvent e) {
            changedUpdate(e);
         }
         
         @Override
         public void changedUpdate(DocumentEvent e) {
            updateStage();
         }
      });
      textField.setDocument(doc);
      String tfTT = getString(KEY_FILTER_TOOLTIP);
      if (!tfTT.equals(textField.getToolTipText())) {
         textField.setToolTipText(tfTT);
      }
      String scbTT = getString(KEY_STAGE_LIST_TOOLTIP);
      if (!scbTT.equals(stageComboBox.getToolTipText())) {
         stageComboBox.setToolTipText(scbTT);
      }
      SpinnerNumberModel snm = new SpinnerNumberModel(getUser().getEscalationLevel().intValue(), 1,
            Stage.MAX_ESCALATION_LEVEL, 1);
      levelSpinner = new JSpinner(snm);
      levelSpinner.getModel().addChangeListener(new ChangeListener() {
         @Override
         public void stateChanged(ChangeEvent e) {
            getUser().setEscalationLevel((Integer) levelSpinner.getValue());
         }
      });
      boolean escalation = currentStage.isEscalation();
      levelSpinner.setVisible(escalation && getUser().canLevelEscalation());
      
      add(stageTypeIndicator);
      add(textField);
      add(stageComboBox);
      add(levelSpinner);
   }
   
   public boolean updateStage() {
      Stage stage = getUser().getCurrentStage();
      Collection<Stage> allStages = getUser().getAllStages();
      String newFilter = getFilterString().toLowerCase();
      boolean changing = !stage.equals(curStage) || !curStages.containsAll(allStages)
            || allStages.size() != curStages.size() || !lastFilter.equals(newFilter);
      if (changing) {
         lastFilter = newFilter;
         curStage = stage;
         curStages = allStages;
         stageComboBox.removeItemListener(il);
         stageComboBox.removeAllItems();
         for (Stage s : curStages) {
            if (s.equals(curStage) || s.toString().toLowerCase().contains(lastFilter)) {
               stageComboBox.addItem(s);
            }
         }
         stageComboBox.setSelectedItem(curStage);
         stageComboBox.addItemListener(il);
         stageTypeIndicator.setVisualized(stage.getType());
         levelSpinner.setVisible(curStage.isEscalation() && getUser().canLevelEscalation());
      }
      textField.setToolTipText(getString(KEY_FILTER_TOOLTIP));
      stageComboBox.setToolTipText(getString(KEY_STAGE_LIST_TOOLTIP));
      return true;
   }
   
   private String getFilterString() {
      String text = textField.getText();
      String valueOf = String.valueOf(text);
      return valueOf;
   }
   
   public void addActionListeners() {
      // unused hook for adding action listeners that might be triggered in setup
   }
   
}
