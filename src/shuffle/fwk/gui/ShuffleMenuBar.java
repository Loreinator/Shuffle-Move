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

import java.awt.Desktop;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;

import shuffle.fwk.GradingMode;
import shuffle.fwk.gui.user.ShuffleMenuUser;
import shuffle.fwk.i18n.I18nUser;
import shuffle.fwk.service.BaseServiceManager;
import shuffle.fwk.service.about.AboutService;
import shuffle.fwk.service.bugreport.BugReportService;
import shuffle.fwk.service.editspecies.EditSpeciesService;
import shuffle.fwk.service.gridprintconfig.GridPrintConfigService;
import shuffle.fwk.service.help.HelpService;
import shuffle.fwk.service.migration.MigrateService;
import shuffle.fwk.service.movechooser.MoveChooserService;
import shuffle.fwk.service.movepreferences.MovePreferencesService;
import shuffle.fwk.service.roster.EditRosterService;
import shuffle.fwk.service.teams.EditTeamService;
import shuffle.fwk.service.update.UpdateService;

/**
 * @author Andrew Meyers
 *
 */
@SuppressWarnings("serial")
public class ShuffleMenuBar extends JMenuBar implements I18nUser {
   private static final Logger LOG = Logger.getLogger(ShuffleMenuBar.class.getName());
   
   // i18n Keys... a lot of them!
   private static final String KEY_BAD_LINK = "error.badlink";
   private static final String KEY_FILE = "menuitem.file";
   private static final String KEY_LOAD = "menuitem.load";
   private static final String KEY_SAVE = "menuitem.save";
   private static final String KEY_EDIT = "menuitem.edit";
   private static final String KEY_PREFERENCES = "menuitem.preferences";
   private static final String KEY_IMPORT = "menuitem.import";
   private static final String KEY_EXIT = "menuitem.exit";
   private static final String KEY_ROSTER = "menuitem.roster";
   private static final String KEY_EDITSPECIES = "menuitem.editspecies";
   private static final String KEY_TEAM = "menuitem.team";
   private static final String KEY_TOGGLE_MEGA = "menuitem.togglemega";
   private static final String KEY_GRID = "menuitem.grid";
   private static final String KEY_GRID_PRINT = "menuitem.grid.print";
   private static final String KEY_GRID_PRINT_CONFIG = "menuitem.grid.print.config";
   private static final String KEY_CLEAR = "menuitem.clear";
   private static final String KEY_FILL = "menuitem.fill";
   private static final String KEY_LOAD_DEFAULT = "menuitem.loaddefault";
   private static final String KEY_CHANGE_MODE = "menuitem.changemode";
   private static final String KEY_MOVE = "menuitem.move";
   private static final String KEY_UNDO_MOVE = "menuitem.undomove";
   private static final String KEY_REDO_MOVE = "menuitem.redomove";
   private static final String KEY_DO_MOVE = "menuitem.domove";
   private static final String KEY_COMPUTE_NOW = "menuitem.computenow";
   private static final String KEY_AUTO_COMPUTE = "menuitem.autocompute";
   private static final String KEY_HELP = "menuitem.help";
   private static final String KEY_ABOUT = "menuitem.about";
   private static final String KEY_BUG = "menuitem.bug";
   private static final String KEY_BUG_FORCE = "menuitem.bug.force";
   private static final String KEY_UPDATE = "menuitem.update";
   private static final String KEY_GRADING_MENU = "menuitem.grading";
   private static final String KEY_CHOOSE_MOVE = "menuitem.choosemove";
   private static final String KEY_SURVIVAL_MODE = "menuitem.survival";
   private static final String KEY_LINKS = "menuitem.links";
   private static final String KEY_LATEST_LINK = "menuitem.latest";
   private static final String KEY_SUBREDDIT_LINK = "menuitem.subreddit";
   private static final String KEY_GUIDE_LINK = "menuitem.guide";
   
   // Hard-coded to avoid tampering
   public static final String LATEST_LINK = "https://github.com/Loreinator/Shuffle-Move/releases/latest";
   public static final String SUBREDDIT_LINK = "https://www.reddit.com/r/ShuffleMove/";
   public static final String GUIDE_LINK = "https://docs.google.com/spreadsheets/d/1hF-TquHrYSY4dP8K_LUsd13HXBx9ck_klASiNKrGVck/htmlview#";
   public static final List<Locale> AVAILABLE_LOCALES = Collections
         .unmodifiableList(Arrays.asList("de en fi fr it ja ko es zh pt".split("\\s+")).stream()
               .map(s -> Locale.forLanguageTag(s)).collect(Collectors.toList()));
   
   private ShuffleMenuUser user;
   private JCheckBoxMenuItem autoComputeItem;
   private JCheckBoxMenuItem survivalItem;
   private Map<GradingMode, AbstractButton> modeMap;
   private Frame owner;
   private Map<AbstractButton, Supplier<String>> buttonToi18nKeyMap = null;
   private Locale prevLocale = null;
   
   public ShuffleMenuBar(ShuffleMenuUser user, Frame owner) {
      super();
      this.user = user;
      this.owner = owner;
      doSetup();
   }
   
   private Frame getOwner() {
      return owner;
   }
   
   private ShuffleMenuUser getUser() {
      return user;
   }
   
   private void addMenuAction(JMenu menu, MenuAction action) {
      registerAbstractButton(menu.add(action), () -> action.getNewText());
   }
   
   private void registerAbstractButton(AbstractButton button, Supplier<String> getText) {
      if (buttonToi18nKeyMap == null) {
         buttonToi18nKeyMap = new HashMap<AbstractButton, Supplier<String>>();
      }
      if (button != null) {
         buttonToi18nKeyMap.put(button, getText);
      }
   }
   
   private void repaintAllButtons() {
      for (AbstractButton button : buttonToi18nKeyMap.keySet()) {
         Supplier<String> supplier = buttonToi18nKeyMap.get(button);
         String text = supplier.get();
         button.setText(text);
      }
   }
   
   public void updateAutoCompute(boolean selected) {
      autoComputeItem.setSelected(selected);
   }
   
   public void updateSurvival(boolean selected) {
      survivalItem.setSelected(selected);
   }
   
   private void doSetup() {
      setupFileMenu();
      setupRosterMenu();
      setupTeamMenu();
      setupGridMenu();
      setupMoveMenu();
      setupHelpMenu();
      setupLinksMenu();
   }
   
   private void setupFileMenu() {
      JMenu menu = new JMenu(getString(KEY_FILE));
      menu.setMnemonic(KeyEvent.VK_F);
      registerAbstractButton(menu, () -> getString(KEY_FILE));
      
      MenuAction load = new MenuAction(() -> getString(KEY_LOAD), e -> getUser().loadAll());
      load.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK));
      addMenuAction(menu, load);
      
      MenuAction saveAction = new MenuAction(() -> getString(KEY_SAVE), e -> getUser().saveAll());
      saveAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
      addMenuAction(menu, saveAction);
      
      menu.addSeparator();
      
      MenuAction importAction = new MenuAction(() -> getString(KEY_IMPORT),
            e -> BaseServiceManager.launchServiceByClass(MigrateService.class, getUser(), getOwner()));
      addMenuAction(menu, importAction);
      
      menu.addSeparator();
      
      MenuAction exitAction = new MenuAction(() -> getString(KEY_EXIT), e -> getOwner().dispatchEvent(
            new WindowEvent(getOwner(), WindowEvent.WINDOW_CLOSING)));
      addMenuAction(menu, exitAction);
      
      add(menu);
   }
   
   private void setupRosterMenu() {
      JMenu menu = new JMenu(getString(KEY_ROSTER));
      menu.setMnemonic(KeyEvent.VK_R);
      registerAbstractButton(menu, () -> getString(KEY_ROSTER));
      
      MenuAction editAction = new MenuAction(() -> getString(KEY_EDIT), e -> BaseServiceManager.launchServiceByClass(
            EditRosterService.class, getUser(), getOwner()));
      editAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK));
      addMenuAction(menu, editAction);
      
      MenuAction loadAction = new MenuAction(() -> getString(KEY_LOAD), e -> getUser().loadRoster());
      addMenuAction(menu, loadAction);
      
      MenuAction saveAction = new MenuAction(() -> getString(KEY_SAVE), e -> getUser().saveRoster());
      addMenuAction(menu, saveAction);
      
      menu.addSeparator();
      
      MenuAction editSpecies = new MenuAction(() -> getString(KEY_EDITSPECIES),
            e -> BaseServiceManager.launchServiceByClass(EditSpeciesService.class, getUser(), getOwner()));
      addMenuAction(menu, editSpecies);
      
      add(menu);
   }
   
   private void setupTeamMenu() {
      JMenu menu = new JMenu(getString(KEY_TEAM));
      menu.setMnemonic(KeyEvent.VK_T);
      buttonToi18nKeyMap.put(menu, () -> getString(KEY_TEAM));
      
      MenuAction editAction = new MenuAction(() -> getString(KEY_EDIT), e -> BaseServiceManager.launchServiceByClass(
            EditTeamService.class, getUser(), getOwner()));
      editAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.CTRL_MASK));
      addMenuAction(menu, editAction);
      
      MenuAction activateMega = new MenuAction(() -> getString(KEY_TOGGLE_MEGA), e -> getUser().toggleActiveMega());
      addMenuAction(menu, activateMega);
      
      survivalItem = new JCheckBoxMenuItem(getString(KEY_SURVIVAL_MODE));
      if (getUser().isSurvival()) {
         survivalItem.setSelected(true);
      }
      survivalItem.addItemListener(new ItemListener() {
         @Override
         public void itemStateChanged(ItemEvent e) {
            getUser().setSurvival(e.getStateChange() == ItemEvent.SELECTED);
         }
      });
      buttonToi18nKeyMap.put(survivalItem, () -> getString(KEY_SURVIVAL_MODE));
      menu.add(survivalItem);
      
      menu.addSeparator();
      
      MenuAction loadAction = new MenuAction(() -> getString(KEY_LOAD), e -> getUser().loadTeams());
      addMenuAction(menu, loadAction);
      
      MenuAction saveAction = new MenuAction(() -> getString(KEY_SAVE), e -> getUser().saveTeams());
      addMenuAction(menu, saveAction);
      
      add(menu);
   }
   
   private void setupGridMenu() {
      JMenu menu = new JMenu(getString(KEY_GRID));
      menu.setMnemonic(KeyEvent.VK_G);
      buttonToi18nKeyMap.put(menu, () -> getString(KEY_GRID));
      
      MenuAction clearAction = new MenuAction(() -> getString(KEY_CLEAR), e -> getUser().clearGrid());
      clearAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, ActionEvent.CTRL_MASK));
      addMenuAction(menu, clearAction);
      
      MenuAction fillAction = new MenuAction(() -> getString(KEY_FILL), e -> getUser().fillGrid());
      fillAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F, ActionEvent.CTRL_MASK));
      addMenuAction(menu, fillAction);

      menu.addSeparator();
      
      MenuAction loadDefaultAction = new MenuAction(() -> getString(KEY_LOAD_DEFAULT), e -> getUser().loadDefaultGrid());
      addMenuAction(menu, loadDefaultAction);
      
      MenuAction loadAction = new MenuAction(() -> getString(KEY_LOAD), e -> getUser().loadGrid());
      addMenuAction(menu, loadAction);
      
      MenuAction saveAction = new MenuAction(() -> getString(KEY_SAVE), e -> getUser().saveGrid());
      addMenuAction(menu, saveAction);
      
      menu.addSeparator();
      
      MenuAction printAction = new MenuAction(() -> getString(KEY_GRID_PRINT), e -> getUser().printGrid());
      printAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));
      addMenuAction(menu, printAction);
      
      MenuAction printConfigAction = new MenuAction(() -> getString(KEY_GRID_PRINT_CONFIG),
            e -> BaseServiceManager.launchServiceByClass(GridPrintConfigService.class, getUser(), getOwner()));
      addMenuAction(menu, printConfigAction);
      
      menu.addSeparator();
      
      MenuAction changeModeAction = new MenuAction(() -> getString(KEY_CHANGE_MODE), e -> getUser().changeMode());
      changeModeAction.putValue(Action.ACCELERATOR_KEY,
            KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, ActionEvent.CTRL_MASK));
      addMenuAction(menu, changeModeAction);
      
      add(menu);
   }
   
   private void setupMoveMenu() {
      JMenu menu = new JMenu(getString(KEY_MOVE));
      menu.setMnemonic(KeyEvent.VK_M);
      buttonToi18nKeyMap.put(menu, () -> getString(KEY_MOVE));
      
      MenuAction undoMoveAction = new MenuAction(() -> getString(KEY_UNDO_MOVE), e -> getUser().undoMove());
      undoMoveAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK));
      addMenuAction(menu, undoMoveAction);
      
      MenuAction redoMoveAction = new MenuAction(() -> getString(KEY_REDO_MOVE), e -> getUser().redoMove());
      redoMoveAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Y, ActionEvent.CTRL_MASK));
      addMenuAction(menu, redoMoveAction);
      
      menu.addSeparator();
      
      menu.add(makeGradingMenu());
      
      MenuAction computeNowAction = new MenuAction(() -> getString(KEY_COMPUTE_NOW), e -> getUser().computeNow());
      computeNowAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("F5"));
      addMenuAction(menu, computeNowAction);
      
      MenuAction chooserAction = new MenuAction(() -> getString(KEY_CHOOSE_MOVE),
            e -> BaseServiceManager.launchServiceByClass(MoveChooserService.class, getUser(), getOwner()));
      chooserAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionEvent.CTRL_MASK));
      addMenuAction(menu, chooserAction);
      
      MenuAction doMoveAction = new MenuAction(() -> getString(KEY_DO_MOVE), e -> getUser().doSelectedMove());
      doMoveAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, ActionEvent.CTRL_MASK));
      addMenuAction(menu, doMoveAction);
      
      menu.addSeparator();
      
      autoComputeItem = new JCheckBoxMenuItem(getString(KEY_AUTO_COMPUTE));
      autoComputeItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, ActionEvent.CTRL_MASK));
      if (getUser().isAutoCompute()) {
         autoComputeItem.setSelected(true);
      }
      autoComputeItem.addItemListener(new ItemListener() {
         @Override
         public void itemStateChanged(ItemEvent e) {
            getUser().setAutoCompute(e.getStateChange() == ItemEvent.SELECTED);
         }
      });
      buttonToi18nKeyMap.put(autoComputeItem, () -> getString(KEY_AUTO_COMPUTE));
      menu.add(autoComputeItem);
      
      MenuAction preferencesItem = new MenuAction(() -> getString(KEY_PREFERENCES),
            e -> BaseServiceManager.launchServiceByClass(MovePreferencesService.class, getUser(), getOwner()));
      addMenuAction(menu, preferencesItem);
      
      add(menu);
   }
   
   private JMenu makeGradingMenu() {
      JMenu menu = new JMenu(getString(KEY_GRADING_MENU));
      buttonToi18nKeyMap.put(menu, () -> getString(KEY_GRADING_MENU));
      GradingMode selectedMode = getUser().getCurrentGradingMode();
      ButtonGroup group = new ButtonGroup();
      modeMap = new HashMap<GradingMode, AbstractButton>();
      for (GradingMode mode : getUser().getGradingModeManager().getGradingModeValues()) {
         String text = mode.geti18nString();
         JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(new AbstractAction(text) {
            @Override
            public void actionPerformed(ActionEvent e) {
               getUser().setGradingMode(mode);
            }
         });
         if (mode.equals(selectedMode)) {
            menuItem.setSelected(true);
         }
         group.add(menuItem);
         menu.add(menuItem);
         modeMap.put(mode, menuItem);
         buttonToi18nKeyMap.put(menuItem, () -> mode.geti18nString());
      }
      return menu;
   }
   
   public void updateGradingMode(GradingMode mode) {
      if (mode != null && modeMap != null && modeMap.containsKey(mode)) {
         modeMap.get(mode).setSelected(true);
      }
   }
   
   private void setupHelpMenu() {
      JMenu menu = new JMenu(getString(KEY_HELP));
      buttonToi18nKeyMap.put(menu, () -> getString(KEY_HELP));
      menu.setMnemonic(KeyEvent.VK_H);
      
      MenuAction helpAction = new MenuAction(() -> getString(KEY_HELP), e -> BaseServiceManager.launchServiceByClass(
            HelpService.class, getUser(), getOwner()));
      addMenuAction(menu, helpAction);
      
      menu.add(getLanguageSelectionMenu());
      
      menu.addSeparator();
      
      MenuAction reportAction = new MenuAction(() -> getString(KEY_BUG), e -> BaseServiceManager.launchServiceByClass(
            BugReportService.class, getUser(), getOwner()));
      addMenuAction(menu, reportAction);
      
      MenuAction dumpReportAction = new MenuAction(() -> getString(KEY_BUG_FORCE), e -> getUser().reportBug(
            "Forced by command."));
      dumpReportAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.CTRL_MASK));
      addMenuAction(menu, dumpReportAction);

      menu.addSeparator();
      
      MenuAction updateAction = new MenuAction(() -> getString(KEY_UPDATE),
            e -> BaseServiceManager.launchServiceByClass(UpdateService.class, getUser(), getOwner()));
      addMenuAction(menu, updateAction);
      
      MenuAction aboutAction = new MenuAction(() -> getString(KEY_ABOUT),
            e -> BaseServiceManager.launchServiceByClass(AboutService.class, getUser(), getOwner()));
      addMenuAction(menu, aboutAction);
      
      add(menu);
   }
   
   private void setupLinksMenu() {
      JMenu menu = new JMenu(getString(KEY_LINKS));
      buttonToi18nKeyMap.put(menu, () -> getString(KEY_LINKS));
      menu.setMnemonic(KeyEvent.VK_L);
      
      MenuAction latestAction = new MenuAction(() -> getString(KEY_LATEST_LINK), e -> openLink(LATEST_LINK));
      addMenuAction(menu, latestAction);
      
      MenuAction subredditAction = new MenuAction(() -> getString(KEY_SUBREDDIT_LINK), e -> openLink(SUBREDDIT_LINK));
      addMenuAction(menu, subredditAction);
      
      MenuAction guideAction = new MenuAction(() -> getString(KEY_GUIDE_LINK), e -> openLink(GUIDE_LINK));
      addMenuAction(menu, guideAction);
      
      add(menu);
   }
   
   private JMenu getLanguageSelectionMenu() {
      JMenu menu = new JMenu(Locale.getDefault().getDisplayName());
      registerAbstractButton(menu, () -> Locale.getDefault().getDisplayName());
      
      for (Locale loc : AVAILABLE_LOCALES) {
         MenuAction action = new MenuAction(() -> loc.getDisplayName(), e -> setLocaleTo(loc));
         addMenuAction(menu, action);
      }
      return menu;
   }
   
   private void setLocaleTo(Locale loc) {
      getUser().setLocaleTo(loc);
   }
   
   /**
    * 
    */
   public void updateAll() {
      updateAutoCompute(getUser().isAutoCompute());
      updateSurvival(getUser().isSurvival());
      updateGradingMode(getUser().getCurrentGradingMode());
      Locale defLocale = Locale.getDefault();
      if (prevLocale == null || !prevLocale.equals(defLocale)) {
         prevLocale = defLocale;
         repaintAllButtons();
      }
   }
   
   public static class MenuAction extends AbstractAction {
      
      private final Consumer<ActionEvent> consumer;
      private final Supplier<String> getText;
      
      public MenuAction(Supplier<String> getText, Consumer<ActionEvent> useEvent) {
         super(getText.get());
         this.getText = getText;
         consumer = useEvent;
      }
      
      /*
       * (non-Javadoc)
       * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
       */
      @Override
      public void actionPerformed(ActionEvent e) {
         consumer.accept(e);
      }
      
      public String getNewText() {
         return getText.get();
      }
   }
   
   private void openLink(String url) {
      if (Desktop.isDesktopSupported()) {
         try {
            Desktop.getDesktop().browse(new URL(url).toURI());
         } catch (IOException | URISyntaxException | NullPointerException exception) {
            LOG.severe(getString(KEY_BAD_LINK, exception.getMessage()));
         }
      }
   }
}
