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

package shuffle.fwk;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Observable;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.commons.lang3.StringUtils;

import shuffle.fwk.config.ConfigFactory;
import shuffle.fwk.config.ConfigManager;
import shuffle.fwk.config.manager.BoardManager;
import shuffle.fwk.config.manager.ImageManager;
import shuffle.fwk.config.manager.RosterManager;
import shuffle.fwk.config.manager.SpeciesManager;
import shuffle.fwk.config.manager.TeamManager;
import shuffle.fwk.data.Species;
import shuffle.fwk.data.SpeciesPaint;
import shuffle.fwk.data.Stage;
import shuffle.fwk.data.Team;
import shuffle.fwk.data.simulation.SimulationResult;
import shuffle.fwk.data.simulation.SimulationTask;
import shuffle.fwk.data.simulation.SimulationUser;
import shuffle.fwk.gui.ShuffleFrame;
import shuffle.fwk.gui.user.ShuffleFrameUser;
import shuffle.fwk.i18n.I18nUser;

/**
 * @author Andrew Meyers
 *
 */
public class ShuffleController extends Observable implements ShuffleViewUser, ShuffleModelUser, ShuffleFrameUser,
      SimulationUser, I18nUser {
   /** The log properties file path */
   private static final String LOG_CONFIG_FILE = "config/logger.properties";
   static { // Ensures that the log managers are loaded from the config file.
      String userHome = System.getProperty("user.home") + File.separator + "Shuffle-Move";
      try {
         new File(userHome).getAbsoluteFile().mkdir();
         System.setProperty("user.dir", userHome);
         try (InputStream is = ClassLoader.getSystemResourceAsStream(LOG_CONFIG_FILE)) {
            File logDir = new File("log").getAbsoluteFile();
            if (!logDir.exists() && !logDir.mkdirs()) {
               throw new IOException("Cannot create log directory.");
            }
            LogManager.getLogManager().readConfiguration(is);
         } catch (IOException e) {
            e.printStackTrace();
         }
      } catch (SecurityException e) {
         e.printStackTrace();
      }
   }
   /** The logger for this controller. */
   private static final Logger LOG = Logger.getLogger(ShuffleController.class.getName());
   
   static { // Sets look and feel to a nicer version than the default
      try {
         UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
      } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
            | UnsupportedLookAndFeelException e) {
         LOG.warning("Cannot load NimbusLookAndFeel because: " + e.getMessage());
      }
   }
   
   /** The Major version number. 0 = unfinished, 1 = first release, etc. */
   public static final int VERSION_MAJOR = 0;
   /** The Minor version number. Each increment is a new significant overhaul. */
   public static final int VERSION_MINOR = 3;
   /** The SubMinor version number. Each increment is a minor batch of tweaks and fixes. */
   public static final int VERSION_SUBMINOR = 17;
   /** The full version String which identifies the program's actual version. */
   public static final String VERSION_FULL = String.format("v%d.%d.%d", VERSION_MAJOR, VERSION_MINOR, VERSION_SUBMINOR);
   
   public static final String KEY_I18N_LOCATION = "i18n/";
   
   // i18n keys
   private static final String KEY_LOAD_ALL = "log.all.load";
   private static final String KEY_SAVE_ALL = "log.all.save";
   private static final String KEY_SAVE_ALL_SUCCESS = "log.all.save.success";
   private static final String KEY_LOAD_ROSTER = "log.roster.load";
   private static final String KEY_SAVE_ROSTER = "log.roster.save";
   private static final String KEY_SAVE_ROSTER_SUCCESS = "log.roster.save.success";
   private static final String KEY_LOAD_TEAM = "log.team.load";
   private static final String KEY_SAVE_TEAM = "log.team.save";
   private static final String KEY_SAVE_TEAM_SUCCESS = "log.team.save.success";
   private static final String KEY_CLEAR_GRID = "log.grid.cleared";
   private static final String KEY_LOAD_GRID = "log.grid.load";
   private static final String KEY_LOAD_DEFAULT_GRID = "log.grid.load.default";
   private static final String KEY_SAVE_GRID = "log.grid.save";
   private static final String KEY_SAVE_GRID_SUCCESS = "log.grid.save.success";
   private static final String KEY_DO_MOVE = "log.move.do";
   private static final String KEY_REDO_MOVE = "log.move.redo";
   private static final String KEY_UNDO_MOVE = "log.move.undo";
   private static final String KEY_COMPUTE_NOW = "log.compute.now";
   private static final String KEY_COMPUTE_AUTO_TRUE = "log.compute.auto.on";
   private static final String KEY_COMPUTE_AUTO_FALSE = "log.compute.auto.off";
   private static final String KEY_ROSTER_CHANGED = "log.roster.changed";
   private static final String KEY_TEAM_CHANGED = "log.team.changed";
   private static final String KEY_IMAGES_CHANGED = "log.images.changed";
   private static final String KEY_SPECIES_CHANGED = "log.species.changed";
   private static final String KEY_GRADING_CHANGED = "log.grading.changed";
   
   /** The model for this controller. */
   private ShuffleModel model;
   /** The view for this controller. */
   private ShuffleView view;
   
   /** The ShuffleFrame which is the primary Window for the program. */
   private ShuffleFrame frame = null;
   /** The ConfigFactory used for manager creation */
   private ConfigFactory factory = null;
   
   /**
    * The main which starts the program.
    * 
    * @param args
    *           unused.
    */
   public static void main(String... args) {
      if (args != null && args.length > 0) {
         try {
            Level levelToSet = Level.parse(args[0]);
            Logger.getLogger(SimulationTask.class.getName()).setLevel(levelToSet);
            SimulationTask.setLogFiner(levelToSet.intValue() <= Level.FINER.intValue());
            Logger.getLogger(ShuffleModel.class.getName()).setLevel(levelToSet);
         } catch (Exception e) {
            LOG.fine("Cannot set simulation logging to that level: " + StringUtils.join(args));
         }
      }
      ShuffleController ctrl = new ShuffleController();
      ctrl.getFrame().launch();
   }
   
   /**
    * Creates a ShuffleController with the given configuration paths for the primary configuration
    * (which tells other managers where to get their configurations). If there are none passed, then
    * "config/main.txt" is assumed.
    * 
    * @param configPaths
    *           The paths as Strings
    */
   public ShuffleController(String... configPaths) {
      if (configPaths.length > 0 && configPaths[0] != null) {
         factory = new ConfigFactory(configPaths[0]);
      } else {
         factory = new ConfigFactory();
      }
      try {
         setModel(new ShuffleModel(this));
         setView(new ShuffleView(this));
         getModel().checkLocaleConfig();
         loadFrame();
      } catch (Exception e) {
         StringWriter sw = new StringWriter();
         PrintWriter pw = new PrintWriter(sw);
         e.printStackTrace(pw);
         LOG.log(Level.SEVERE, "Failure on start:", e);
      }
   }
   
   /**
    * Gets the ConfigFactory associated with this ShuffleController.
    * 
    * @return
    */
   @Override
   public ConfigFactory getConfigFactory() {
      return factory;
   }
   
   /**
    * Gets the ShuffleModel for this ShuffleController.
    * 
    * @return The ShuffleModel
    */
   public ShuffleModel getModel() {
      return model;
   }
   
   /**
    * Sets the ShuffleModel for this ShuffleController
    * 
    * @param model
    *           The ShuffleModel
    */
   public void setModel(ShuffleModel model) {
      this.model = model;
   }
   
   /**
    * Gets the ShuffleView for this ShuffleController.
    * 
    * @return The ShuffleView
    */
   public ShuffleView getView() {
      return view;
   }
   
   /**
    * Sets the ShuffleView for this ShuffleController.
    * 
    * @param view
    *           The ShuffleView.
    */
   public void setView(ShuffleView view) {
      this.view = view;
   }
   
   // Load/Save menu options
   /**
    * Loads all data from configurations.
    */
   @Override
   public void loadAll() {
      LOG.info(getString(KEY_LOAD_ALL));
      if (getModel().loadAllData()) {
         getModel().setDataChanged();
         repaint();
      }
   }
   
   @Override
   public void saveAll() {
      LOG.info(getString(KEY_SAVE_ALL));
      getModel().saveAllData();
      LOG.info(getString(KEY_SAVE_ALL_SUCCESS));
   }
   
   /**
    * Loads all Roster data from configuration.
    */
   @Override
   public void loadRoster() {
      RosterManager manager = getModel().getRosterManager();
      if (manager.loadFromConfig()) {
         LOG.info(getString(KEY_LOAD_ROSTER));
         getModel().setDataChanged();
         repaint();
      }
   }
   
   /**
    * Saves all Roster data to configuration.
    */
   @Override
   public void saveRoster() {
      RosterManager manager = getModel().getRosterManager();
      LOG.info(getString(KEY_SAVE_ROSTER));
      manager.saveDataToConfig();
      LOG.info(getString(KEY_SAVE_ROSTER_SUCCESS));
   }
   
   /**
    * Loads all Teams data from configuration.
    */
   @Override
   public void loadTeams() {
      TeamManager manager = getModel().getTeamManager();
      if (manager.loadFromConfig()) {
         LOG.info(getString(KEY_LOAD_TEAM));
         getModel().setDataChanged();
         repaint();
      }
   }
   
   /**
    * Saves all Teams data to configuration.
    */
   @Override
   public void saveTeams() {
      TeamManager manager = getModel().getTeamManager();
      LOG.info(getString(KEY_SAVE_TEAM));
      manager.saveDataToConfig();
      LOG.info(getString(KEY_SAVE_TEAM_SUCCESS));
   }
   
   /**
    * Clears the current board.
    */
   @Override
   public void clearGrid() {
      if (getModel().clearBoard()) {
         LOG.info(getString(KEY_CLEAR_GRID));
         getModel().setDataChanged();
         repaint();
      }
   }
   
   /**
    * Loads the board from configuration.
    */
   @Override
   public void loadGrid() {
      if (getModel().loadBoard()) {
         LOG.info(getString(KEY_LOAD_GRID));
         getModel().setDataChanged();
         repaint();
      }
   }
   
   /**
    * Loads the default board for the current stage, from configuration.
    */
   @Override
   public void loadDefaultGrid() {
      if (getModel().loadDefaultBoard()) {
         LOG.info(getString(KEY_LOAD_DEFAULT_GRID));
         getModel().setDataChanged();
         repaint();
      }
   }
   
   /**
    * Saves the board to configuration.
    */
   @Override
   public void saveGrid() {
      LOG.info(getString(KEY_SAVE_GRID));
      getModel().saveBoard();
      LOG.info(getString(KEY_SAVE_GRID_SUCCESS));
   }
   
   @Override
   public void doSelectedMove() {
      if (getModel().doSelectedMove()) {
         LOG.info(getString(KEY_DO_MOVE));
         getModel().setCurrentMode(EntryMode.PAINT);
         getModel().setCursorTo(1, 1);
         repaint();
      }
   }
   
   @Override
   public void undoMove() {
      if (getModel().undoMove()) {
         LOG.info(getString(KEY_UNDO_MOVE));
         repaint();
      }
   }
   
   @Override
   public void redoMove() {
      if (getModel().redoMove()) {
         LOG.info(getString(KEY_REDO_MOVE));
         repaint();
      }
   }
   
   /**
    * Returns the ShuffleFrame for this view.
    * 
    * @return The ShuffleFrame
    */
   public ShuffleFrame getFrame() {
      if (frame == null) {
         frame = new ShuffleFrame(this);
      }
      return frame;
   }
   
   public void loadFrame() {
      if (frame == null) {
         frame = new ShuffleFrame(this);
      }
   }
   
   @Override
   public void changeMode() {
      EntryMode next = getModel().getCurrentMode().getNextMode();
      if (getModel().setCurrentMode(next)) {
         repaint();
      }
   }
   
   @Override
   public ImageManager getImageManager() {
      return getView().getImageManager();
   }
   
   @Override
   public void toggleFrozen() {
      if (getModel().toggleFrozenPaints()) {
         repaint();
      }
   }
   
   @Override
   public void setSelectedSpecies(Species toPaint) {
      if (getModel().setSelectedSpecies(toPaint)) {
         repaint();
      }
   }
   
   @Override
   public void setCursorTo(int row, int col) {
      if (getModel().setCursorTo(row, col)) {
         repaint();
      }
   }
   
   @Override
   public void paintAt(SpeciesPaint paint, int row, int col) {
      if (getModel().paintAt(row, col, paint)) {
         getModel().setDataChanged();
         repaint();
      }
   }
   
   @Override
   public void toggleFrozenAt(Integer row, Integer column) {
      boolean prevFrozen = getModel().getBoard().isFrozenAt(row, column);
      if (getModel().paintAt(row, column, null, !prevFrozen)) {
         getModel().setDataChanged();
         repaint();
      }
   }
   
   @Override
   public void advanceCursorBy(int i) {
      if (getModel().advanceCursorBy(i)) {
         repaint();
      }
   }
   
   @Override
   public void setCurMode(EntryMode mode) {
      if (getModel().setCurrentMode(mode)) {
         repaint();
      }
   }
   
   @Override
   public void setCurrentStage(Stage newStage) {
      if (getModel().setCurrentStage(newStage)) {
         getModel().setDataChanged();
         repaint();
      }
   }
   
   @Override
   public String getTitle() {
      return VERSION_FULL;
   }
   
   @Override
   public String getTextFor(Object value) {
      return getView().getTextFor(value);
   }
   
   @Override
   public Stage getCurrentStage() {
      return getModel().getCurrentStage();
   }
   
   @Override
   public SpeciesPaint getSelectedSpeciesPaint() {
      return getModel().getCurrentSpeciesPaint();
   }
   
   @Override
   public List<SpeciesPaint> getCurrentPaints() {
      return getModel().getCurrentPaints();
   }
   
   // Simulation User methods
   
   @Override
   public int getMegaProgress() {
      return getModel().getMegaProgress();
   }
   
   @Override
   public void setMegaProgress(int progress) {
      if (getModel().setMegaProgress(progress)) {
         getModel().setDataChanged();
         repaint();
      }
   }
   
   @Override
   public boolean isMegaAllowed() {
      return getModel().isMegaAllowed();
   }
   
   /*
    * (non-Javadoc)
    * @see shuffle.fwk.data.simulation.util.SimulationAcceptor#acceptResults(java.util.Collection)
    */
   @Override
   public void acceptResults(Collection<SimulationResult> results) {
      if (getModel().setBestResults(results)) {
         repaint();
      }
   }
   
   @Override
   public UUID getAcceptedId() {
      return getModel().getAcceptedId();
   }
   
   @Override
   public int getPreferredNumFeeders() {
      return getModel().getNumFeeders();
   }
   
   @Override
   public int getPreferredFeederHeight() {
      return getModel().getFeederHeight();
   }
   
   @Override
   public void computeNow() {
      LOG.info(getString(KEY_COMPUTE_NOW));
      getModel().computeNow();
      repaint();
   }
   
   @Override
   public boolean isAutoCompute() {
      return getModel().getAutoCompute();
   }
   
   @Override
   public void setAutoCompute(boolean autoCompute) {
      if (getModel().setAutoCompute(autoCompute)) {
         if (autoCompute) {
            LOG.info(getString(KEY_COMPUTE_AUTO_TRUE));
         } else {
            LOG.info(getString(KEY_COMPUTE_AUTO_FALSE));
         }
         getModel().setDataChanged();
         repaint();
      }
   }
   
   @Override
   public void reportBug(String message) {
      SwingUtilities.invokeLater(new Runnable() {
         @Override
         public void run() {
            getModel().saveAllData();
            getModel().reportBug(message);
         }
      });
   }
   
   @Override
   public void repaint() {
      if (SwingUtilities.isEventDispatchThread()) {
         setChanged();
         notifyObservers();
         getFrame().repaint();
      } else {
         SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
               setChanged();
               notifyObservers();
               getFrame().repaint();
            }
         });
      }
   }
   
   /*
    * (non-Javadoc)
    * @see shuffle.fwk.service.update.UpdateServiceUser#getCurrentVersion()
    */
   @Override
   public String getCurrentVersion() {
      return VERSION_FULL;
   }
   
   /*
    * (non-Javadoc)
    * @see shuffle.fwk.service.movepreferences.MovePreferencesServiceUser#setFeederPreferences(int,
    * int)
    */
   @Override
   public void setFeederPreferences(int numFeeders, int feederHeight, boolean autoCompute) {
      if (getModel().setFeederPreferences(numFeeders, feederHeight, autoCompute)) {
         getModel().setDataChanged();
         repaint();
      }
   }
   
   public void setMegaActive(boolean active) {
      if (getModel().setMegaActive(active)) {
         getModel().setDataChanged();
         repaint();
      }
   }
   
   /**
	 * 
	 */
   @Override
   public void toggleActiveMega() {
      Team team = getTeamManager().getTeamForStage(getCurrentStage());
      String megaSlotName = team.getMegaSlotName();
      if (megaSlotName != null) {
         boolean wasActive = getModel().isMegaSlotActive();
         setMegaActive(!wasActive);
      }
   }
   
   /*
    * (non-Javadoc)
    * @see shuffle.fwk.data.simulation.SimulationUser#getSpeciesManager()
    */
   @Override
   public SpeciesManager getSpeciesManager() {
      return getModel().getSpeciesManager();
   }
   
   /*
    * (non-Javadoc)
    * @see shuffle.fwk.gui.user.StageIndicatorUser#getAllStages()
    */
   @Override
   public Collection<Stage> getAllStages() {
      return getModel().getStageManager().getAllStages();
   }
   
   /*
    * (non-Javadoc)
    * @see shuffle.fwk.config.provider.RosterManagerProvider#getRosterManager()
    */
   @Override
   public RosterManager getRosterManager() {
      return getModel().getRosterManager();
   }
   
   /*
    * (non-Javadoc)
    * @see
    * shuffle.fwk.service.roster.EditRosterServiceUser#loadFromRosterManager(shuffle.fwk.config.
    * manager.RosterManager)
    */
   @Override
   public void loadFromRosterManager(RosterManager manager) {
      RosterManager curManager = getModel().getRosterManager();
      if (curManager.copyFromManager(manager)) {
         LOG.info(getString(KEY_ROSTER_CHANGED));
         getModel().setDataChanged();
         repaint();
      }
   }
   
   /*
    * (non-Javadoc)
    * @see shuffle.fwk.config.provider.TeamManagerProvider#getTeamManager()
    */
   @Override
   public TeamManager getTeamManager() {
      return getModel().getTeamManager();
   }
   
   /*
    * (non-Javadoc)
    * @see shuffle.fwk.config.provider.BoardManagerProvider#getBoardManager()
    */
   @Override
   public BoardManager getBoardManager() {
      return getModel().getBoardManager();
   }
   
   /*
    * (non-Javadoc)
    * @see shuffle.fwk.gui.user.ModeIndicatorUser#getCurrentMode()
    */
   @Override
   public EntryMode getCurrentMode() {
      return getModel().getCurrentMode();
   }
   
   /*
    * (non-Javadoc)
    * @see shuffle.fwk.EntryModeUser#getCurrentCursor()
    */
   @Override
   public List<Integer> getCurrentCursor() {
      return getModel().getCurrentCursor();
   }
   
   /*
    * (non-Javadoc)
    * @see shuffle.fwk.EntryModeUser#getCurrentSpeciesPaint()
    */
   @Override
   public SpeciesPaint getCurrentSpeciesPaint() {
      return getModel().getCurrentSpeciesPaint();
   }
   
   /*
    * (non-Javadoc)
    * @see shuffle.fwk.EntryModeUser#getPreviousCursor()
    */
   @Override
   public List<Integer> getPreviousCursor() {
      return getModel().getPreviousCursor();
   }
   
   /*
    * (non-Javadoc)
    * @see shuffle.fwk.data.simulation.util.SimulationResultProvider#getResult()
    */
   @Override
   public SimulationResult getSelectedResult() {
      return getModel().getCurrentResult();
   }
   
   /*
    * (non-Javadoc)
    * @see shuffle.fwk.data.simulation.util.SimulationResultProvider#getResults()
    */
   @Override
   public Collection<SimulationResult> getResults() {
      return getModel().getResults();
   }
   
   /*
    * (non-Javadoc)
    * @see shuffle.fwk.gui.user.GridPanelUser#getPaintAt(java.lang.Integer, java.lang.Integer)
    */
   @Override
   public SpeciesPaint getPaintAt(Integer row, Integer col) {
      return getModel().getPaintAt(row, col);
   }
   
   /*
    * (non-Javadoc)
    * @see shuffle.fwk.service.textdisplay.TextDisplayServiceUser#getPathManager()
    */
   @Override
   public ConfigManager getPathManager() {
      return getConfigFactory().getPathManager();
   }
   
   /*
    * (non-Javadoc)
    * @see shuffle.fwk.service.textdisplay.TextDisplayServiceUser#getPreferencesManager()
    */
   @Override
   public ConfigManager getPreferencesManager() {
      return getConfigFactory().getPreferencesManager();
   }
   
   /*
    * (non-Javadoc)
    * @see
    * shuffle.fwk.service.teams.EditTeamServiceUser#loadFromTeamManager(shuffle.fwk.config.manager
    * .TeamManager)
    */
   @Override
   public void loadFromTeamManager(TeamManager manager) {
      TeamManager curManager = getModel().getTeamManager();
      if (curManager.copyFromManager(manager)) {
         LOG.info(getString(KEY_TEAM_CHANGED));
         getModel().setDataChanged();
         repaint();
      }
   }
   
   /*
    * (non-Javadoc)
    * @see
    * shuffle.fwk.service.editspecies.EditSpeciesServiceUser#loadSpeciesManagerFrom(shuffle.fwk.
    * config.manager.SpeciesManager)
    */
   @Override
   public void loadSpeciesManagerFrom(SpeciesManager manager) {
      SpeciesManager curManager = getModel().getSpeciesManager();
      if (curManager.copyFromManager(manager)) {
         LOG.info(getString(KEY_SPECIES_CHANGED));
         getModel().setDataChanged();
         repaint();
      }
   }
   
   /*
    * (non-Javadoc)
    * @see
    * shuffle.fwk.service.editspecies.EditSpeciesServiceUser#loadImageManagerFrom(shuffle.fwk.config
    * .manager.ImageManager)
    */
   @Override
   public void loadImageManagerFrom(ImageManager manager) {
      ImageManager curManager = getView().getImageManager();
      if (curManager.copyFromManager(manager)) {
         LOG.info(getString(KEY_IMAGES_CHANGED));
         curManager.reloadIcons();
         repaint();
      }
   }
   
   /*
    * (non-Javadoc)
    * @see shuffle.fwk.gui.user.GradingModeUser#getCurrentGradingMode()
    */
   @Override
   public GradingMode getCurrentGradingMode() {
      return getModel().getCurrentGradingMode();
   }
   
   /*
    * (non-Javadoc)
    * @see shuffle.fwk.gui.user.GradingModeUser#setGradingMode(shuffle.fwk.GradingMode)
    */
   @Override
   public void setGradingMode(GradingMode mode) {
      if (getModel().setGradingMode(mode)) {
         LOG.info(getString(KEY_GRADING_CHANGED));
         repaint();
      }
   }
   
   /*
    * (non-Javadoc)
    * @see
    * shuffle.fwk.service.movechooser.MoveChooserServiceUser#setSelectedResult(shuffle.fwk.data.
    * simulation.SimulationResult)
    */
   @Override
   public void setSelectedResult(SimulationResult result) {
      if (getModel().setSelectedResult(result)) {
         repaint();
      }
   }
   
   /*
    * (non-Javadoc)
    * @see shuffle.fwk.gui.user.ShuffleMenuUser#setLocaleTo(java.util.Locale)
    */
   @Override
   public void setLocaleTo(Locale loc) {
      if (getModel().setLocaleTo(loc)) {
         repaint();
         getFrame().updateMinimumSize();
         getFrame().pack();
      }
   }
   
   /*
    * (non-Javadoc)
    * @see shuffle.fwk.gui.user.PaintsIndicatorUser#setTeamForStage(shuffle.fwk.data.Team,
    * shuffle.fwk.data.Stage)
    */
   @Override
   public void setTeamForStage(Team team, Stage stage) {
      if (getModel().getTeamManager().setTeamForStage(team, stage)) {
         getModel().setDataChanged();
         repaint();
      }
   }
   
   /*
    * (non-Javadoc)
    * @see shuffle.fwk.gui.user.PaintsIndicatorUser#setFrozenState(boolean)
    */
   @Override
   public void setPaintsFrozen(boolean selected) {
      boolean prev = getModel().arePaintsFrozen();
      if (prev != selected && getModel().toggleFrozenPaints()) {
         repaint();
      }
   }
   
   /*
    * (non-Javadoc)
    * @see shuffle.fwk.gui.user.PaintsIndicatorUser#getFrozenState()
    */
   @Override
   public boolean getFrozenState() {
      return getModel().arePaintsFrozen();
   }
}
