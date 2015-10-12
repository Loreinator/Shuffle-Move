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

package shuffle.fwk.service.update;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import shuffle.fwk.config.ConfigManager;
import shuffle.fwk.i18n.I18nUser;
import shuffle.fwk.service.BaseService;
import shuffle.fwk.service.DisposeAction;
import shuffle.fwk.update.UpdateCheck;

public class UpdateService extends BaseService<UpdateServiceUser> implements I18nUser {
   
   private static final Logger LOG = Logger.getLogger(UpdateService.class.getName());
   
   private static final String KEY_UPDATE_FILE = "UPDATE_FILE";
   private static final String KEY_UPDATE_MIN_WIDTH = "UPDATE_MIN_WIDTH";
   private static final String KEY_UPDATE_MIN_HEIGHT = "UPDATE_MIN_HEIGHT";
   private static final int DEFAULT_UPDATE_WIDTH = 300;
   private static final int DEFAULT_UPDATE_HEIGHT = 200;
   
   private static final String KEY_GETNEWEST = "button.getnewest";
   private static final String KEY_FORCE = "button.force";
   private static final String KEY_CLOSE = "button.close";
   private static final String KEY_BAD_LINK = "error.badlink";
   private static final String KEY_TITLE = "text.title";
   private static final String KEY_GETNEWEST_TOOLTIP = "tooltip.getnewest";
   private static final String KEY_FORCE_TOOLTIP = "tooltip.force";
   private static final String KEY_CLOSE_TOOLTIP = "tooltip.close";
   public static final String KEY_DOWNLOADING = "text.downloading";
   public static final String KEY_PLEASE_UNPACK = "text.pleaseunpack";
   public static final String KEY_UPTODATE = "text.uptodate";
   
   private JDialog d = null;
   private JPanel progressPanel = null;
   private JProgressBar bar = null;
   
   private boolean updating = false;
   private String currentMessage = null;
   
   private Collection<Consumer<Boolean>> inProgressConsumers = new ArrayList<Consumer<Boolean>>();
   
   @Override
   protected Class<UpdateServiceUser> getUserClass() {
      return UpdateServiceUser.class;
   }
   
   @Override
   public void onSetupGUI() {
      JButton getNewestButton = new JButton(new UpdateNowAction(getString(KEY_GETNEWEST), this, false));
      getNewestButton.setToolTipText(getString(KEY_GETNEWEST_TOOLTIP));
      JButton forceNewestButton = new JButton(new UpdateNowAction(getString(KEY_FORCE), this, true));
      forceNewestButton.setToolTipText(getString(KEY_FORCE_TOOLTIP));
      JButton closeButton = new JButton(new DisposeAction(getString(KEY_CLOSE), this));
      closeButton.setToolTipText(getString(KEY_CLOSE_TOOLTIP));
      inProgressConsumers.addAll(Arrays.asList(b -> getNewestButton.setEnabled(!b),
            b -> forceNewestButton.setEnabled(!b), b -> closeButton.setEnabled(!b)));
      
      JEditorPane textArea = new JEditorPane();
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
      String text = getUser().getPathManager().readEntireFileOrResource(KEY_UPDATE_FILE);
      textArea.setText(text);
      textArea.setSelectionStart(0);
      textArea.setSelectionEnd(0);
      textArea.repaint();
      JScrollPane jsp = new JScrollPane(textArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      jsp.getVerticalScrollBar().setUnitIncrement(30);
      JPanel scrollPanel = new JPanel(new BorderLayout()) {
         private static final long serialVersionUID = -560615667007328181L;
         
         @Override
         public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            d.width = getNewestButton.getMinimumSize().width + forceNewestButton.getMinimumSize().width
                  + closeButton.getMinimumSize().width;
            d.height = 100;
            return d;
         }
         
         @Override
         public Dimension getMaximumSize() {
            Dimension d = super.getMaximumSize();
            d.width = getNewestButton.getMinimumSize().width + forceNewestButton.getMinimumSize().width
                  + closeButton.getMinimumSize().width;
            return d;
         }
      };
      scrollPanel.add(jsp, BorderLayout.CENTER);
      
      progressPanel = new JPanel(new BorderLayout());
      bar = new JProgressBar();
      setInProgress(false, true);
      progressPanel.add(bar, BorderLayout.CENTER);
      
      d = new JDialog(getOwner(), getString(KEY_TITLE, getUser().getTitle()));
      d.setLayout(new GridBagLayout());
      GridBagConstraints c = new GridBagConstraints();
      c.fill = GridBagConstraints.BOTH;
      c.anchor = GridBagConstraints.CENTER;
      
      // Row 1
      c.gridy = 1;
      c.weighty = 1.0;
      c.weightx = 1.0;
      // Scroll pane
      c.gridx = 1;
      c.gridwidth = 3;
      d.add(scrollPanel, c);
      
      // Row 2
      c.gridy = 2;
      c.weighty = 0.0;
      c.weightx = 1.0;
      // Progress bar
      c.gridx = 1;
      c.gridwidth = 3;
      d.add(progressPanel, c);
      
      // Row 3
      c.gridy = 3;
      c.weighty = 0.0;
      c.weightx = 1.0;
      // Get newest
      c.gridx = 1;
      c.gridwidth = 1;
      d.add(getNewestButton, c);
      // Force update
      c.gridx = 2;
      c.gridwidth = 1;
      d.add(forceNewestButton, c);
      // Close
      c.gridx = 3;
      c.gridwidth = 1;
      d.add(closeButton, c);
      
      ConfigManager preferencesManager = getUser().getPreferencesManager();
      final int width = preferencesManager.getIntegerValue(KEY_UPDATE_MIN_WIDTH, DEFAULT_UPDATE_WIDTH);
      final int height = preferencesManager.getIntegerValue(KEY_UPDATE_MIN_HEIGHT, DEFAULT_UPDATE_HEIGHT);
      Dimension minSize = new Dimension(width, height);
      d.pack();
      d.repaint();
      d.setMinimumSize(minSize);
      d.setResizable(true);
      d.setLocationRelativeTo(null);
      setDialog(d);
   }
   
   @Override
   protected void onDispose() {
      progressPanel = null;
      bar = null;
      d = null;
   }
   
   private void setInProgress(boolean inProgress) {
      setInProgress(inProgress, false, null);
   }
   
   private void setInProgress(boolean inProgress, boolean force) {
      setInProgress(inProgress, force, null);
   }
   
   private void setInProgress(boolean inProgress, boolean force, String i18nKey) {
      if (bar != null && (updating != inProgress || force)) {
         updating = inProgress;
         setDisposeEnabled(!inProgress);
         inProgressConsumers.forEach(c -> c.accept(inProgress));
         bar.setIndeterminate(inProgress);
         if (i18nKey != null) {
            currentMessage = i18nKey;
         } else if (inProgress) {
            currentMessage = KEY_DOWNLOADING;
         } else {
            UpdateCheck updateCheck = new UpdateCheck();
            Map<String, String> availableVersions = updateCheck.getAvailableVersions();
            if (availableVersions.isEmpty()) {
               currentMessage = null;
            } else {
               String newest = updateCheck.getNewestVersion(availableVersions);
               currentMessage = UpdateCheck.getZipName(newest);
            }
         }
         setMessage(currentMessage);
         bar.setStringPainted(inProgress || currentMessage != null);
         bar.repaint();
      }
   }
   
   private void setMessage(String i18nkey) {
      if (bar != null) {
         bar.setString(getString(i18nkey));
      }
   }
   
   private boolean isInProgress() {
      return updating;
   }
   
   @Override
   protected void updateGUIFrom(UpdateServiceUser user) {
      // Do nothing, we did our update from the user in setup (never changes)
   }
   
   private static class UpdateNowAction extends AbstractAction implements PropertyChangeListener {
      private static final long serialVersionUID = 1207120189320345749L;
      
      private final UpdateService service;
      private boolean force;
      
      public UpdateNowAction(String name, UpdateService service, boolean force) {
         super(name);
         this.service = service;
         this.force = force;
      }
      
      @Override
      public void actionPerformed(ActionEvent e) {
         if (!service.isInProgress()) {
            service.setInProgress(true);
            UpdateCheck updateCheck = new UpdateCheck();
            String curVersion = service.getUser().getCurrentVersion();
            updateCheck.doUpdate(curVersion, force, UpdateNowAction.this);
         }
      }
      
      /*
       * (non-Javadoc)
       * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
       */
      @Override
      public void propertyChange(PropertyChangeEvent arg0) {
         if (arg0.getPropertyName().equals(UpdateCheck.PROPERTY_DONE)) {
            if (SwingUtilities.isEventDispatchThread()) {
               service.setInProgress(false);
            } else {
               SwingUtilities.invokeLater(new Runnable() {
                  @Override
                  public void run() {
                     service.setInProgress(false);
                  }
               });
            }
         } else if (arg0.getPropertyName().equals(UpdateCheck.PROPERTY_MESSAGE)) {
            Object newValue = arg0.getNewValue();
            if (newValue instanceof String) {
               String text = (String) newValue;
               if (SwingUtilities.isEventDispatchThread()) {
                  service.setInProgress(false, false, text);
               } else {
                  SwingUtilities.invokeLater(new Runnable() {
                     @Override
                     public void run() {
                        service.setInProgress(false, false, text);
                     }
                  });
               }
            }
         }
      }
   }
}
