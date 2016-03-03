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

package shuffle.fwk.service.migration;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileFilter;

import shuffle.fwk.config.manager.RosterManager;
import shuffle.fwk.config.manager.TeamManager;
import shuffle.fwk.i18n.I18nUser;
import shuffle.fwk.service.BaseService;
import shuffle.fwk.service.DisposeAction;

/**
 * @author Andrew Meyers
 *
 */
public class MigrateService extends BaseService<MigrateServiceUser> implements I18nUser {
   private static final Logger LOG = Logger.getLogger(MigrateService.class.getName());
   
   private static final String KEY_OK = "button.ok";
   private static final String KEY_CANCEL = "button.cancel";
   private static final String KEY_IMPORT_TEAMS = "button.import.teams";
   private static final String KEY_IMPORT_ROSTER = "button.import.roster";
   private static final String KEY_FILTER_DESCRIPTION = "text.filterdesc";
   private static final String KEY_IMPORT_TEAMS_TITLE = "title.import.teams";
   private static final String KEY_IMPORT_ROSTER_TITLE = "title.import.roster";
   private static final String KEY_IMPORT_SUCCESS = "text.import.success";
   private static final String KEY_IMPORT_FAIL = "text.import.fail";
   private static final String KEY_STATUS_CHOOSE = "text.status.choose";
   private static final String KEY_STATUS_DONE = "text.status.done";
   private static final String KEY_IMPORT_TEAMS_TOOLTIP = "tooltip.import.teams";
   private static final String KEY_IMPORT_ROSTER_TOOLTIP = "tooltip.import.roster";
   private static final String KEY_OK_TOOLTIP = "tooltip.ok";
   private static final String KEY_CANCEL_TOOLTIP = "tooltip.cancel";
   
   private JDialog d = null;
   private JLabel teamStatus = null;
   private JLabel rosterStatus = null;
   
   private TeamManager teamData = null;
   private RosterManager rosterData = null;
   
   private File prevLocation = null;
   
   /*
    * (non-Javadoc)
    * @see shuffle.fwk.service.BaseService#getUserClass()
    */
   @Override
   protected Class<MigrateServiceUser> getUserClass() {
      return MigrateServiceUser.class;
   }
   
   /*
    * (non-Javadoc)
    * @see shuffle.fwk.service.BaseService#setupGUI()
    */
   @Override
   public void onSetupGUI() {
      
      JDialog d = new JDialog(getOwner());
      
      d.setLayout(new GridBagLayout());
      
      GridBagConstraints c = new GridBagConstraints();
      c.fill = GridBagConstraints.BOTH;
      c.anchor = GridBagConstraints.CENTER;
      c.weightx = 1.0;
      c.weighty = 1.0;
      c.gridy = 1;
      
      c.gridx = 1;
      c.gridy++;
      
      c.gridx++;
      teamStatus = new JLabel(getString(KEY_STATUS_CHOOSE));
      teamStatus.setHorizontalAlignment(SwingConstants.CENTER);
      teamStatus.setHorizontalTextPosition(SwingConstants.CENTER);
      d.add(teamStatus, c);
      
      c.gridx++;
      rosterStatus = new JLabel(getString(KEY_STATUS_CHOOSE));
      rosterStatus.setHorizontalAlignment(SwingConstants.CENTER);
      rosterStatus.setHorizontalTextPosition(SwingConstants.CENTER);
      d.add(rosterStatus, c);
      
      // chooser row
      c.gridx = 1;
      c.gridy++;
      
      c.gridx++;
      JButton importTeamsButton = new JButton(new AbstractAction(getString(KEY_IMPORT_TEAMS)) {
         private static final long serialVersionUID = 7210442243906308193L;
         
         @Override
         public void actionPerformed(ActionEvent arg0) {
            importTeams();
         }
      });
      importTeamsButton.setToolTipText(getString(KEY_IMPORT_TEAMS_TOOLTIP));
      d.add(importTeamsButton, c);
      
      c.gridx++;
      JButton importRosterButton = new JButton(new AbstractAction(getString(KEY_IMPORT_ROSTER)) {
         private static final long serialVersionUID = -8952138130413953491L;
         
         @Override
         public void actionPerformed(ActionEvent e) {
            importRoster();
         }
      });
      importRosterButton.setToolTipText(getString(KEY_IMPORT_ROSTER_TOOLTIP));
      d.add(importRosterButton, c);
      
      // ok/cancel row
      c.gridx = 1;
      c.gridy++;
      
      c.gridx++;
      JButton okButton = new JButton(new AbstractAction(getString(KEY_OK)) {
         private static final long serialVersionUID = -8952138130413953491L;
         
         @Override
         public void actionPerformed(ActionEvent arg0) {
            onOk();
         }
      });
      okButton.setToolTipText(getString(KEY_OK_TOOLTIP));
      d.add(okButton, c);
      setDefaultButton(okButton);
      
      c.gridx++;
      JButton cancelButton = new JButton(new DisposeAction(getString(KEY_CANCEL), this));
      cancelButton.setToolTipText(getString(KEY_CANCEL_TOOLTIP));
      d.add(cancelButton, c);
      d.pack();
      d.repaint();
      d.setLocationRelativeTo(null);
      
      setDialog(d);
   }
   
   /*
    * (non-Javadoc)
    * @see shuffle.fwk.service.BaseService#updateGUIFrom(java.lang.Object)
    */
   @Override
   protected void updateGUIFrom(MigrateServiceUser user) {
      teamData = new TeamManager(user.getTeamManager());
      rosterData = new RosterManager(user.getRosterManager());
      teamStatus.setText(getString(KEY_STATUS_CHOOSE));
      rosterStatus.setText(getString(KEY_STATUS_CHOOSE));
   }
   
   private void onOk() {
      getUser().loadFromTeamManager(teamData);
      getUser().loadFromRosterManager(rosterData);
      dispose();
   }
   
   private void importTeams() {
      File f = getFile(KEY_IMPORT_TEAMS_TITLE);
      if (f == null) {
         LOG.log(Level.WARNING, getString(KEY_IMPORT_FAIL));
      } else {
         try {
            TeamManager.migrateUsing(f.getAbsolutePath(), teamData);
            LOG.info(getString(KEY_IMPORT_SUCCESS));
            teamStatus.setText(getString(KEY_STATUS_DONE));
         } catch (Exception e) {
            LOG.log(Level.WARNING, getString(KEY_IMPORT_FAIL), e);
         }
      }
   }
   
   private void importRoster() {
      File f = getFile(KEY_IMPORT_ROSTER_TITLE);
      if (f == null) {
         LOG.log(Level.WARNING, getString(KEY_IMPORT_FAIL));
      } else {
         try {
            RosterManager.migrateUsing(f.getAbsolutePath(), rosterData);
            LOG.info(getString(KEY_IMPORT_SUCCESS));
            rosterStatus.setText(getString(KEY_STATUS_DONE));
         } catch (Exception e) {
            LOG.log(Level.WARNING, getString(KEY_IMPORT_FAIL), e);
         }
      }
   }
   
   private File getFile(String titleKey) {
      if (prevLocation == null) {
         prevLocation = new File(System.getProperty("user.dir"));
      }
      JFileChooser chooser = new JFileChooser();
      chooser.setDialogTitle(getString(titleKey));
      chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
      chooser.setAcceptAllFileFilterUsed(false);
      chooser.setCurrentDirectory(prevLocation);
      chooser.setFileFilter(new FileFilter() {
         @Override
         public String getDescription() {
            return getString(KEY_FILTER_DESCRIPTION);
         }
         
         @Override
         public boolean accept(File f) {
            return f.isDirectory() || f.getPath().endsWith(".txt");
         }
      });
      File ret;
      if (chooser.showOpenDialog(d) == JFileChooser.APPROVE_OPTION) {
         ret = chooser.getSelectedFile();
      } else {
         ret = null;
      }
      prevLocation = chooser.getCurrentDirectory();
      return ret;
   }
   
}
