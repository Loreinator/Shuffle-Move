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

package shuffle.fwk.service.textdisplay;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import shuffle.fwk.config.ConfigManager;
import shuffle.fwk.i18n.I18nUser;
import shuffle.fwk.service.BaseService;
import shuffle.fwk.service.DisposeAction;

/**
 * @author Andrew Meyers
 *
 */
public abstract class TextDisplayService extends BaseService<TextDisplayServiceUser> implements I18nUser {
   private static final Logger LOG = Logger.getLogger(TextDisplayService.class.getName());
   
   private static final String KEY_CLOSE_BUTTON = "button.close";
   private static final String KEY_BAD_LINK = "error.badlink";
   private static final String KEY_CLOSE_TOOLTIP = "tooltip.close";
   
   private JEditorPane textArea = null;
   private JScrollPane jsp = null;
   
   /*
    * (non-Javadoc)
    * @see shuffle.fwk.service.BaseService#getUserClass()
    */
   @Override
   protected Class<TextDisplayServiceUser> getUserClass() {
      return TextDisplayServiceUser.class;
   }
   
   /*
    * (non-Javadoc)
    * @see shuffle.fwk.service.BaseService#setupGUI()
    */
   @SuppressWarnings("serial")
   @Override
   public void onSetupGUI() {
      JDialog d = new JDialog(getOwner(), getTitle(getUser().getTitle()));
      d.setLayout(new BorderLayout());
      ConfigManager preferencesManager = getUser().getPreferencesManager();
      final int width = preferencesManager.getIntegerValue(KEY_POPUP_WIDTH, DEFAULT_POPUP_WIDTH);
      final int height = preferencesManager.getIntegerValue(KEY_POPUP_HEIGHT, DEFAULT_POPUP_HEIGHT);
      textArea = new JEditorPane() {
         @Override
         public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            d.width = width - 40;
            return d;
         }
      };
      textArea.setEditable(false);
      textArea.setContentType("text/html");
      textArea.addHyperlinkListener(new HyperlinkListener() {
         @Override
         public void hyperlinkUpdate(HyperlinkEvent event) {
            if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
               if (Desktop.isDesktopSupported()) {
                  try {
                     Desktop.getDesktop().browse(event.getURL().toURI());
                  } catch (IOException | URISyntaxException | NullPointerException exception) {
                     LOG.severe(getString(KEY_BAD_LINK, exception.getMessage()));
                  }
               }
            }
         }
      });
      jsp = new JScrollPane(textArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      d.add(jsp, BorderLayout.CENTER);
      jsp.getVerticalScrollBar().setUnitIncrement(30);
      
      JButton close = new JButton(new DisposeAction(getString(KEY_CLOSE_BUTTON), this));
      close.setToolTipText(getString(KEY_CLOSE_TOOLTIP));
      d.add(close, BorderLayout.SOUTH);
      d.pack();
      d.setSize(new Dimension(width, height));
      d.repaint();
      d.setLocationRelativeTo(null);
      setDialog(d);
   }
   
   /*
    * (non-Javadoc)
    * @see shuffle.fwk.service.BaseService#updateGUIFrom(java.lang.Object)
    */
   @Override
   protected void updateGUIFrom(TextDisplayServiceUser user) {
      ConfigManager pathManager = user.getPathManager();
      String text = pathManager.readEntireDocument(getFilePath(), getDefaultFileKey());
      textArea.setText(text);
      textArea.setSelectionStart(0);
      textArea.setSelectionEnd(0);
      textArea.repaint();
   }
   
   protected abstract String getFilePath();
   
   protected abstract String getDefaultFileKey();
   
   /**
    * @param title
    * @return
    */
   protected abstract String getTitle(String title);
   
}
