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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import shuffle.fwk.ShuffleVersion;
import shuffle.fwk.config.ConfigManager;
import shuffle.fwk.gui.user.ShuffleFrameUser;
import shuffle.fwk.i18n.I18nUser;
import shuffle.fwk.log.AlertLogHandler;
import shuffle.fwk.log.AlertReciever;
import shuffle.fwk.service.BaseServiceManager;
import shuffle.fwk.service.help.HelpService;
import shuffle.fwk.service.movechooser.MoveChooserService;
import shuffle.fwk.service.saveprompt.SavePromptService;
import shuffle.fwk.service.update.UpdateService;
import shuffle.fwk.update.UpdateCheck;

/**
 * @author Andrew Meyers
 *
 */
@SuppressWarnings("serial")
public class ShuffleFrame extends JFrame implements I18nUser {
   private static final Logger LOG = Logger.getLogger(ShuffleFrame.class.getName());
   // i18n keys
   private static final String KEY_TITLE = "shuffleframe.title";
   private static final String KEY_WELCOME = "shuffleframe.welcome";
   
   private static final List<String> iconPaths = Arrays.asList("img/icon_16.png", "img/icon_32.png", "img/icon_64.png",
         "img/icon.png");
   
   private ShuffleFrameUser user;
   
   private ShuffleMenuBar shuffleMenuBar;
   private StageChooser stageChooser;
   private MoveIndicator moveIndicator;
   private GridPanel gridPanel;
   private ModeIndicator modeIndicator;
   private PaintPalletPanel paintPalletPanel;
   private JComboBox<String> alertsLog;
   
   public ShuffleFrame(ShuffleFrameUser user) {
      super();
      setIconFromResources();
      setTitle(getString(KEY_TITLE, ShuffleVersion.VERSION_FULL));
      setUser(user);
      setResizable(true);
      setupGUI();
      addComponentListener(new ComponentAdapter() {
         @Override
         public void componentResized(ComponentEvent e) {
            updateMinimumSize();
         }
      });
      setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
      addWindowListener(new WindowAdapter() {
         
         @Override
         public void windowClosing(WindowEvent e) {
            if (getUser().shouldPromptSave()) {
               BaseServiceManager.launchServiceByClass(SavePromptService.class, getUser(), ShuffleFrame.this);
            } else {
               System.exit(0);
            }
         }
         
      });
      updateMinimumSize();
      repaint();
      pack();
      setLocationRelativeTo(null);
   }
   
   public void updateMinimumSize() {
      Dimension d = getMinimumSize();
      
      d.width = gridPanel.getPreferredSize().width + modeIndicator.getPreferredSize().width;
      d.height = gridPanel.getPreferredSize().height + alertsLog.getPreferredSize().height
            + moveIndicator.getPreferredSize().height + stageChooser.getPreferredSize().height;
      
      Insets insets = getInsets();
      if (insets != null) {
         d.width += getInsets().left + getInsets().right;
         d.height += getInsets().top + getInsets().bottom;
      }
      
      JMenuBar menu = getJMenuBar();
      if (menu != null) {
         d.height += getJMenuBar().getPreferredSize().height;
      }
      
      setMinimumSize(d);
   }
   
   private void setIconFromResources() {
      ClassLoader cl = this.getClass().getClassLoader();
      List<Image> images = new ArrayList<Image>();
      for (String resourcePath : iconPaths) {
         URL resource = cl.getResource(resourcePath);
         if (resource != null) {
            ImageIcon imageIcon = new ImageIcon(resource);
            images.add(imageIcon.getImage());
         }
      }
      if (!images.isEmpty()) {
         setIconImages(images);
      }
   }
   
   public ShuffleFrameUser getUser() {
      return user;
   }
   
   public void setUser(ShuffleFrameUser user) {
      this.user = user;
   }
   
   public void launch() {
      if (SwingUtilities.isEventDispatchThread()) {
         setVisible(true);
         handleLaunch();
      } else {
         SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
               setVisible(true);
               handleLaunch();
            }
         });
      }
   }
   
   private void handleLaunch() {
      ConfigManager manager = getUser().getPreferencesManager();
      if (manager.getBooleanValue(MoveChooserService.KEY_CHOOSER_AUTOLAUNCH, false)) {
         BaseServiceManager.launchServiceByClass(MoveChooserService.class, getUser(), this);
      }
      if (manager.getBooleanValue(HelpService.KEY_AUTOLAUNCH_HELP, true)) {
         BaseServiceManager.launchServiceByClass(HelpService.class, getUser(), this);
      }
      UpdateCheck.EXECUTOR.execute(new Runnable() {
         @Override
         public void run() {
            UpdateCheck updater = new UpdateCheck();
            Map<String, String> availableVersions = updater.getAvailableVersions();
            String curVersion = getUser().getCurrentVersion();
            String updateLink = updater.versionCheck(curVersion, availableVersions);
            if (updateLink != null) {
               BaseServiceManager.launchServiceByClass(UpdateService.class, getUser(), ShuffleFrame.this);
            }
         }
      });
   }
   
   @Override
   public void dispose() {
      super.dispose();
      setUser(null);
   }
   
   public void setupGUI() {
      shuffleMenuBar = new ShuffleMenuBar(getUser(), this);
      setJMenuBar(shuffleMenuBar);
      JPanel mainPanel = new JPanel(new GridBagLayout());
      
      modeIndicator = new ModeIndicator(getUser());
      
      GridBagConstraints c = new GridBagConstraints();
      c.fill = GridBagConstraints.NONE;
      c.weightx = 1.0;
      c.weighty = 1.0;
      c.gridx = 1;
      c.gridy = 1;
      c.anchor = GridBagConstraints.CENTER;
      c.gridheight = 2;
      c.gridwidth = 1;
      c.fill = GridBagConstraints.BOTH;
      // At 1,1 to 1,2
      gridPanel = new GridPanel(getUser(), modeIndicator);
      mainPanel.add(gridPanel, c);
      
      c.fill = GridBagConstraints.NONE;
      c.weighty = 0.0;
      c.gridwidth = 1;
      c.gridheight = 1;
      
      // Goes at 3,1
      c.gridy = 3;
      c.gridx = 1;
      c.fill = GridBagConstraints.NONE;
      c.anchor = GridBagConstraints.CENTER;
      moveIndicator = new MoveIndicator(getUser());
      mainPanel.add(moveIndicator, c);
      
      // Goes at 4,1
      c.gridy = 4;
      c.gridx = 1;
      c.fill = GridBagConstraints.HORIZONTAL;
      c.anchor = GridBagConstraints.CENTER;
      stageChooser = new StageChooser(getUser());
      mainPanel.add(stageChooser, c);
      
      // Goes at 1,3
      c.gridy = 1;
      c.gridx = 3;
      c.fill = GridBagConstraints.NONE;
      c.anchor = GridBagConstraints.CENTER;
      mainPanel.add(modeIndicator, c);
      
      // Goes at 2,3 to 4,3
      c.gridy++;
      c.gridheight = 3;
      c.weighty = 1.0;
      c.weightx = 0.0;
      c.fill = GridBagConstraints.BOTH;
      c.anchor = GridBagConstraints.CENTER;
      paintPalletPanel = new PaintPalletPanel(getUser(), new Function<Dimension, Dimension>() {
         @Override
         public Dimension apply(Dimension d) {
            d.width = modeIndicator.getPreferredSize().width - 20;
            d.height = Math.max(gridPanel.getPreferredSize().height, gridPanel.getSize().height);
            d.height += Math.max(moveIndicator.getPreferredSize().height, moveIndicator.getSize().height);
            d.height += Math.max(stageChooser.getPreferredSize().height, stageChooser.getSize().height);
            d.height -= Math.max(modeIndicator.getPreferredSize().height, modeIndicator.getSize().height);
            return d;
         }
      });
      mainPanel.add(paintPalletPanel, c);
      
      final int heightForLog = new JComboBox<String>().getPreferredSize().height;
      // Set up the alerts combobox to automatically maintain itself
      alertsLog = new JComboBox<String>() {
         @Override
         public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            d.width = mainPanel.getPreferredSize().width;
            d.height = Math.min(heightForLog, d.height);
            return d;
         }
         
         @Override
         public Dimension getMinimumSize() {
            Dimension d = super.getMinimumSize();
            d.height = getPreferredSize().height;
            return d;
         }
      };
      alertsLog.setRenderer(new AlertComboBoxRenderer() {
         @Override
         public int getPreferredWidth() {
            int width = alertsLog.getPreferredSize().width - 20;
            return Math.min(width, 400); // cross platform compromise so the text is always readable
         }
      });
      AlertLogHandler.registerReciever(new AlertReciever() {
         @Override
         public void processLogMessage(String message) {
            doSafely(new Runnable() {
               @Override
               public void run() {
                  alertsLog.insertItemAt(message, 0);
                  alertsLog.setSelectedIndex(0);
                  while (alertsLog.getItemCount() > 100) {
                     int last = alertsLog.getItemCount() - 1;
                     alertsLog.removeItemAt(last);
                  }
                  alertsLog.repaint();
               }
            });
         }
         
         private void doSafely(Runnable run) {
            if (SwingUtilities.isEventDispatchThread()) {
               run.run();
            } else {
               SwingUtilities.invokeLater(run);
            }
         }
      });
      JPanel contentPanel = new JPanel(new GridBagLayout());
      GridBagConstraints cc = new GridBagConstraints();
      cc.weightx = 1.0;
      cc.weighty = 1.0;
      cc.gridy = 1;
      cc.gridx = 1;
      cc.fill = GridBagConstraints.BOTH;
      setContentPane(contentPanel);
      contentPanel.add(mainPanel, cc);
      cc.weighty = 0.0;
      cc.gridy = 2;
      cc.gridx = 1;
      contentPanel.add(alertsLog, cc);
      
      setFocusTraversalPolicy(modeIndicator.getModeTraversalPolicy());
      stageChooser.addActionListeners();
      gridPanel.addActionListeners();
      modeIndicator.addActionListeners();
      paintPalletPanel.addIndicatorListeners();
      LOG.info(getString(KEY_WELCOME, ShuffleVersion.VERSION_FULL));
   }
   
   @Override
   public void repaint() {
      stageChooser.updateStage();
      moveIndicator.updateMove();
      gridPanel.updateGrid();
      modeIndicator.updateMode();
      paintPalletPanel.updateAll();
      shuffleMenuBar.updateAll();
      // super.pack();
      super.repaint();
   }
   
}
