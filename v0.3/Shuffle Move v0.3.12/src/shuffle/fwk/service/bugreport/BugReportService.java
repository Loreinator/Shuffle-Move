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

package shuffle.fwk.service.bugreport;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import shuffle.fwk.config.ConfigManager;
import shuffle.fwk.i18n.I18nUser;
import shuffle.fwk.service.BaseService;

/**
 * @author Andrew Meyers
 *
 */
public class BugReportService extends BaseService<BugReportServiceUser> implements I18nUser {
   
   private static final Logger LOG = Logger.getLogger(BugReportService.class.getName());
   private static final String KEY_BUGS_FILE = "BUGS_FILE";
   private static final String KEY_TITLE = "text.title";
   private static final String KEY_CREATE = "button.create";
   private static final String KEY_CANCEL = "button.cancel";
   private static final String KEY_BAD_LINK = "error.badlink";
   
   private JTextArea textArea;
   
   @Override
   protected Class<BugReportServiceUser> getUserClass() {
      return BugReportServiceUser.class;
   }
   
   @Override
   public void onSetupGUI() {
      JDialog d = new JDialog(getOwner(), getString(KEY_TITLE));
      d.setLayout(new GridBagLayout());
      GridBagConstraints c = new GridBagConstraints();
      c.fill = GridBagConstraints.BOTH;
      c.weightx = 1.0;
      c.weighty = 0.0;
      c.gridwidth = 5;
      
      c.gridy = 1;
      c.gridx = 1;
      JComponent instruction = getInstructionPanel();
      d.add(instruction, c);
      
      c.gridy = 2;
      c.gridx = 1;
      c.weighty = 1.0;
      textArea = new JTextArea();
      textArea.setLineWrap(true);
      textArea.setWrapStyleWord(true);
      JScrollPane jsp = new JScrollPane(textArea);
      jsp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
      jsp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      d.add(jsp, c);
      c.weighty = 0.0;
      
      c.gridy = 3;
      c.gridwidth = 1;
      
      c.gridx = 1;
      d.add(new JLabel(), c);
      
      c.gridx += 1;
      JButton submitButton = new JButton(getString(KEY_CREATE));
      submitButton.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            doSubmit();
            dispose();
         }
      });
      d.add(submitButton, c);
      
      c.gridx += 1;
      d.add(new JLabel(), c);
      
      c.gridx += 1;
      JButton cancelButton = new JButton(getString(KEY_CANCEL));
      cancelButton.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            dispose();
         }
      });
      d.add(cancelButton, c);
      
      c.gridx += 1;
      d.add(new JLabel(), c);
      
      ConfigManager preferencesManager = getUser().getPreferencesManager();
      final int width = preferencesManager.getIntegerValue(KEY_POPUP_WIDTH, DEFAULT_POPUP_WIDTH);
      final int height = preferencesManager.getIntegerValue(KEY_POPUP_HEIGHT, DEFAULT_POPUP_HEIGHT);
      d.pack();
      d.setSize(new Dimension(width, height));
      d.repaint();
      d.setLocationRelativeTo(null);
      setDialog(d);
   }
   
   /**
    * @return
    */
   @SuppressWarnings("serial")
   private JComponent getInstructionPanel() {
      ConfigManager preferencesManager = getUser().getPreferencesManager();
      final int width = preferencesManager.getIntegerValue(KEY_POPUP_WIDTH, DEFAULT_POPUP_WIDTH);
      JEditorPane textPane = new JEditorPane() {
         @Override
         public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            d.width = width - 40;
            return d;
         }
      };
      textPane.setEditable(false);
      textPane.setContentType("text/html");
      textPane.addHyperlinkListener(new HyperlinkListener() {
         @Override
         public void hyperlinkUpdate(HyperlinkEvent e) {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
               if (Desktop.isDesktopSupported()) {
                  try {
                     Desktop.getDesktop().browse(e.getURL().toURI());
                  } catch (IOException | URISyntaxException | NullPointerException e1) {
                     LOG.severe(getString(KEY_BAD_LINK, e1.getMessage()));
                  }
               }
            }
         }
      });
      JScrollPane jsp = new JScrollPane(textPane, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      jsp.getVerticalScrollBar().setUnitIncrement(30);
      String text = getUser().getPathManager().readEntireFileOrResource(KEY_BUGS_FILE);
      textPane.setText(text);
      textPane.setSelectionStart(0);
      textPane.setSelectionEnd(0);
      textPane.repaint();
      jsp.validate();
      return jsp;
   }
   
   private void doSubmit() {
      getUser().reportBug(textArea.getText());
   }
   
   @Override
   protected void updateGUIFrom(BugReportServiceUser user) {
      // Nothing to do
   }
}
