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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Stack;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;

import shuffle.fwk.config.ConfigFactory;
import shuffle.fwk.config.ConfigManager;
import shuffle.fwk.config.EntryType;
import shuffle.fwk.config.manager.BoardManager;
import shuffle.fwk.config.manager.RosterManager;
import shuffle.fwk.config.manager.SpeciesManager;
import shuffle.fwk.config.manager.StageManager;
import shuffle.fwk.config.manager.TeamManager;
import shuffle.fwk.config.provider.BoardManagerProvider;
import shuffle.fwk.config.provider.PreferencesManagerProvider;
import shuffle.fwk.config.provider.RosterManagerProvider;
import shuffle.fwk.config.provider.SpeciesManagerProvider;
import shuffle.fwk.config.provider.StageManagerProvider;
import shuffle.fwk.config.provider.TeamManagerProvider;
import shuffle.fwk.data.Board;
import shuffle.fwk.data.Effect;
import shuffle.fwk.data.Species;
import shuffle.fwk.data.SpeciesPaint;
import shuffle.fwk.data.Stage;
import shuffle.fwk.data.Team;
import shuffle.fwk.data.simulation.SimulationCore;
import shuffle.fwk.data.simulation.SimulationResult;
import shuffle.fwk.i18n.I18nUser;

/**
 * The model of the program, this handles all the important data. All getters or save operations
 * have a public visibility, all setters or load/edit operations must be protected (access only via
 * helpers in ShuffleController).
 * 
 * @author Andrew Meyers
 *
 */
public class ShuffleModel implements BoardManagerProvider, PreferencesManagerProvider, RosterManagerProvider,
      SpeciesManagerProvider, StageManagerProvider, TeamManagerProvider, I18nUser {
   /** The logger for this controller. */
   private static final Logger LOG = Logger.getLogger(ShuffleModel.class.getName());
   /** The controller for this model. */
   private final ShuffleModelUser user;
   /** The delay, in Milliseconds, after a change before the core is started. */
   private static final int SIMULATION_DELAY = 150;
   // defaults
   private static final boolean DEFAULT_AUTO_COMPUTE = true;
   private static final int DEFAULT_FEEDER_HEIGHT = 0;
   private static final int DEFAULT_NUM_FEEDERS = 1;
   private static final GradingMode DEFAULT_GRADING = GradingMode.SCORE;
   private static final String BUILD_REPORT_FILE = "bugs/buildReport.xml";
   private static final String BUILD_REPORT_RESOURCE = "config/buildReport.xml";
   private static final String BUG_DETAILS_FILE = "bugs/bugDetails.txt";
   // config keys
   private static final String KEY_AUTO_COMPUTE = "AUTO_COMPUTE";
   private static final String KEY_NUM_FEEDERS = "NUM_FEEDERS";
   private static final String KEY_FEEDER_HEIGHT = "FEEDER_HEIGHT";
   private static final String KEY_GRADING_MODE = "GRADING_MODE";
   private static final String KEY_LOAD_LOCALE = "LOAD_LOCALE_FROM_CONFIG";
   private static final String KEY_LOCALE_STATE = "LAST_LOCALE";
   private static final String KEY_MOVES_REMAINING = "STAGE_MOVES_REMAINING";
   private static final String KEY_HEALTH_REMAINING = "STAGE_HEALTH_REMAINING";
   // i18n keys
   private static final String KEY_SIMULATION_START = "log.sim.start";
   private static final String KEY_SIMULATION_COMPLETE = "log.sim.complete";
   private static final String KEY_BUG_FILE_SUCCESS = "log.bugfile.success";
   private static final String KEY_BUG_FILE_IOE = "log.error.bugfile.ioe";
   private static final String KEY_BUG_FILE_READONLY = "log.error.bugfile.readonly";
   private static final String KEY_BUG_FILE_SAVEPROBLEM = "log.error.bugfile.saveproblem";
   private static final String KEY_BUG_REPORT_PROBLEM = "log.error.bugreport.problem";
   private static final String KEY_SELECTING_RESULT = "log.result.selected";
   // bug report components
   private Project errorProject = null;
   private BuildListener antListener = null;
   
   // Managers
   private BoardManager boardManager = null;
   private RosterManager rosterManager = null;
   private TeamManager teamManager = null;
   private StageManager stageManager = null;
   private SpeciesManager speciesManager = null;
   private ConfigManager prefManager = null;
   
   // Interface controls
   /** Current mode of entry in the interface. */
   private EntryMode curMode = EntryMode.PAINT;
   /** Current species to be painted. */
   private Species curSpecies = Species.AIR;
   /** Current frozen state of the paint. */
   private boolean frozen = false;
   /** Current position of the cursor for entry. */
   private int curPos = 1;
   private GradingMode gradeMode = null;
   private Locale prevLocale = null;
   
   private boolean resultsCurrent = false;
   private boolean resultsComputing = false;
   private SimulationResult selectedResult = null;
   private TreeSet<SimulationResult> bestResults = null;
   
   private ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(1);
   private ForkJoinPool forkJoinPool = getNewPool();
   private UUID processUUID = null;
   
   private Stack<UndoRedoItem> undoStack = new Stack<UndoRedoItem>();
   private Stack<UndoRedoItem> redoStack = new Stack<UndoRedoItem>();
   
   /**
    * Creates a Shuffle model, bound to the given ShuffleController.
    * 
    * @param shuffleController
    */
   public ShuffleModel(ShuffleModelUser user) {
      this.user = user;
   }
   
   // Important methods
   /** Returns the controller for this model. */
   public ShuffleModelUser getUser() {
      return user;
   }
   
   protected void checkLocaleConfig() {
      ConfigManager preferencesManager = getPreferencesManager();
      if (preferencesManager.getBooleanValue(KEY_LOAD_LOCALE, false)) {
         String localeString = preferencesManager.getStringValue(KEY_LOCALE_STATE, "");
         if (localeString != null && !localeString.isEmpty()) {
            Locale loc = Locale.forLanguageTag(localeString);
            if (loc != null) {
               Locale.setDefault(loc);
               prevLocale = loc;
            }
         }
      }
   }
   
   /**
    * @param loc
    * @return
    */
   protected boolean setLocaleTo(Locale loc) {
      boolean changing = loc != null && !loc.equals(prevLocale);
      if (changing) {
         prevLocale = loc;
         Locale.setDefault(loc);
         String localeString = loc.toLanguageTag();
         getPreferencesManager().setEntry(EntryType.STRING, KEY_LOCALE_STATE, localeString);
         getPreferencesManager().setEntry(EntryType.BOOLEAN, KEY_LOAD_LOCALE, true);
      }
      return changing;
   }
   
   // General method for loading
   /**
    * Loads all data for the model to the state it would be if the application restarted, except for
    * the current entry mode and cursor. If anything changed as a result of this, then True is
    * returned. False is returned otherwise.
    */
   protected boolean loadAllData() {
      boolean changed = false;
      changed |= getConfigFactory().loadAllFromConfig();
      changed |= getBoardManager().loadFromConfig();
      changed |= frozen;
      frozen = false;
      SpeciesManager manager = getSpeciesManager();
      Iterator<Species> itr = getCurrentTeam().getSpecies(manager).iterator();
      if (itr.hasNext()) {
         curSpecies = itr.next();
      } else {
         curSpecies = Species.AIR;
      }
      return changed;
   }
   
   // General method for saving
   /**
    * Saves all data in the model for the Board state, current Stage, Roster, and the Team for every
    * stage.
    */
   public void saveAllData() {
      getConfigFactory().saveAllToConfig();
      getBoardManager().saveBoard();
   }
   
   // MANAGER GETTERS
   /**
    * @return
    */
   private ConfigFactory getConfigFactory() {
      return getUser().getConfigFactory();
   }
   
   /**
    * Gets the BoardManager.
    */
   @Override
   public BoardManager getBoardManager() {
      if (boardManager == null) {
         boardManager = new BoardManager(getConfigFactory());
      }
      return boardManager;
   }
   
   /**
    * Gets the RosterManager
    */
   @Override
   public RosterManager getRosterManager() {
      if (rosterManager == null) {
         rosterManager = getConfigFactory().getRosterManager();
      }
      return rosterManager;
   }
   
   /**
    * Gets the TeamManager
    */
   @Override
   public TeamManager getTeamManager() {
      if (teamManager == null) {
         teamManager = getConfigFactory().getTeamManager();
      }
      return teamManager;
   }
   
   /**
    * Gets the StageManager
    */
   @Override
   public StageManager getStageManager() {
      if (stageManager == null) {
         stageManager = getConfigFactory().getStageManager();
      }
      return stageManager;
   }
   
   /**
    * Gets the SpeciesManager
    */
   @Override
   public SpeciesManager getSpeciesManager() {
      if (speciesManager == null) {
         speciesManager = getConfigFactory().getSpeciesManager();
      }
      return speciesManager;
   }
   
   /**
    * Gets the Preferences ConfigManager.
    */
   @Override
   public ConfigManager getPreferencesManager() {
      if (prefManager == null) {
         prefManager = getConfigFactory().getPreferencesManager();
      }
      return prefManager;
   }
   
   // BOARD MANAGER METHODS
   /**
    * Loads the board manager and all loaders or managers it depends on, from configuration.
    * 
    * @return True if anything might have changed. False otherwise.
    */
   protected boolean loadBoard() {
      return getBoardManager().loadFromConfig();
   }
   
   /**
    * Saves the board and current stage to disc.
    */
   public void saveBoard() {
      getBoardManager().saveBoard();
   }
   
   /**
    * Returns a copy of the board. Altering the returned Board will not affect anything else.
    * 
    * @return A copy of the current Board.
    */
   public Board getBoard() {
      return new Board(getBoardManager().getBoard());
   }
   
   /**
    * Resets the board. Has no effect if the board is already pristine.
    * 
    * @return True if the board changed, False otherwise.
    */
   protected boolean clearBoard() {
      String before = getBoardManager().getBoard().toString();
      getBoardManager().getBoard().clear();
      return !before.equals(getBoardManager().getBoard().toString());
   }
   
   /**
    * Gets the current SpeciesPaint that describes the given coordinates. This accounts for frozen
    * and mega states.
    * 
    * @return the SpeciesPaint which best describes the given coordinates in the current Board.
    */
   public SpeciesPaint getPaintAt(int row, int col) {
      Species s = getBoard().getSpeciesAt(row, col);
      Boolean speciesFrozen = getBoard().isFrozenAt(row, col);
      Boolean speciesMega = isMegaActive(s.getName());
      return new SpeciesPaint(s, speciesFrozen, speciesMega);
   }
   
   /**
    * @return
    */
   public boolean isMegaSlotActive() {
      return isMegaAllowed() && getMegaProgress() >= getCurrentThreshold();
   }
   
   public boolean isMegaActive(String s) {
      return getCurrentTeam().isMegaSlot(s) && isMegaSlotActive();
   }
   
   /**
    * Returns true if this action changed the internal board in some way.
    * 
    * @param row
    *           the row within [1, 6]
    * @param col
    *           the column within [1, 6]
    * @param paint
    *           the Species to paint with. if null, species will not change.
    * @param freeze
    *           the freeze state to apply. if null, frozen state will not change.
    * @return True if this action changed anything. False otherwise.
    */
   protected boolean paintAt(int row, int col, Species paint, Boolean freeze) {
      if (row < 1 || row > 6 || col < 0 || col > 6) {
         return false;
      }
      boolean changed = false;
      changed |= getBoardManager().getBoard().setSpeciesAt(row, col, paint);
      changed |= getBoardManager().getBoard().setFrozenAt(row, col, freeze);
      return changed;
   }
   
   /**
    * Returns true if this action changed the internal board in some way.
    * 
    * @param row
    *           the row within [1, 6]
    * @param col
    *           the column within [1, 6]
    * @param paint
    *           the SpeciesPaint to paint with. if null, nothing changes.
    * @return True if this action changed anything. False otherwise.
    */
   protected boolean paintAt(int row, int col, SpeciesPaint paint) {
      return paintAt(row, col, paint.getSpecies(), paint.isFrozen());
   }
   
   /**
    * Gets the current board stage.
    * 
    * @return A Stage, the current board.
    */
   public Stage getCurrentStage() {
      return getBoardManager().getCurrentStage();
   }
   
   /**
    * Sets the current stage to the given value. Returns true if this altered the model in some way.
    * 
    * @param stage
    * @return True if the stage changed, False otherwise.
    */
   protected boolean setCurrentStage(Stage stage) {
      Team prevTeam = getCurrentTeam();
      boolean changed = getBoardManager().setStage(stage);
      if (changed) {
         int prevProgress = getMegaProgress();
         getBoardManager().loadBoardForStage(stage, false);
         setCursorTo(1, 1);
         Team newTeam = getCurrentTeam();
         if (newTeam == null || newTeam.getNames().isEmpty()) {
            getTeamManager().setTeamForStage(prevTeam, stage);
         }
         
         int newThreshold = newTeam.getMegaThreshold(getSpeciesManager(), getUser().getRosterManager());
         int prevThreshold = prevTeam.getMegaThreshold(getSpeciesManager(), getUser().getRosterManager());
         int newProgress;
         if (prevProgress == prevThreshold) {
            newProgress = newThreshold;
         } else {
            newProgress = Math.min(prevProgress, newThreshold);
         }
         setMegaProgress(newProgress);
         
         setRemainingHealth(stage.getHealth());
         setRemainingMoves(stage.getMoves());
         undoStack.clear();
         redoStack.clear();
      }
      SpeciesManager manager = getSpeciesManager();
      List<Species> species = getCurrentTeam().getSpecies(manager);
      if (!species.contains(curSpecies)) {
         if (species.size() > 0) {
            curSpecies = species.get(0);
         } else {
            curSpecies = Species.AIR;
         }
      }
      return changed;
   }
   
   /**
    * Loads the current default board for this stage, if it exists. Returns true if this changed the
    * board, false if otherwise.
    * 
    * @param stage
    * @return True if the board changed, False otherwise.
    */
   protected boolean loadDefaultBoard() {
      boolean changed = getBoardManager().loadBoardForStage(getCurrentStage(), true);
      if (changed) {
         setCursorTo(1, 1);
      }
      return changed;
   }
   
   // Team methods
   private Team getCurrentTeam() {
      return getTeamManager().getTeamForStage(getCurrentStage());
   }
   
   // Cur mode methods
   /**
    * Returns the current entry mode. This will never return null.
    * 
    * @return
    */
   public EntryMode getCurrentMode() {
      return curMode;
   }
   
   /**
    * Sets the current mode to the given EntryMode value. If the value is null or is equal to the
    * previous mode this will not alter the current mode.
    * 
    * @param em
    *           The mode to set.
    * @return True if the mode changed. False if otherwise.
    */
   protected boolean setCurrentMode(EntryMode em) {
      boolean changing = em != null && !em.equals(curMode);
      if (changing) {
         curMode = em;
      }
      return changing;
   }
   
   // Cursor position methods
   /**
    * Gets the equivalent position index for the given row and column coordinates.
    * 
    * @param row
    *           an int in [1, {@link Board#NUM_COLS}]
    * @param col
    *           an int in [1, {@link Board#NUM_ROWS}]
    * @return The position index in [1, {@link Board#NUM_CELLS}].
    * @see #getCoordsFromPosition(int)
    */
   public static int getCursorPositionFor(int row, int col) {
      return fixPosition(col + Board.NUM_COLS * (row - 1));
   }
   
   /**
    * Gets the coordinates from the given position, as a List of two Integers.
    * 
    * @param pos
    *           The position index within [1, {@link Boad#NUM_CELLS}].
    * @return Coordinates as a list of two Integers within [1, {@link Board#NUM_COLS}]x[1,
    *         {@link Board#NUM_ROWS}]
    */
   public static List<Integer> getCoordsFromPosition(int pos) {
      pos = fixPosition(pos);
      int row = (pos - 1) / Board.NUM_COLS + 1;
      int col = (pos - 1) % Board.NUM_COLS + 1;
      return new ArrayList<Integer>(Arrays.asList(row, col));
   }
   
   /**
    * Fixes the given position value to the bounds of [1, {@link Board#NUM_CELLS}]
    * 
    * @param given
    *           The given position value.
    * @return The fixed position which is valid for the Board.
    */
   private static int fixPosition(int given) {
      int ret = given;
      while (ret < 1) {
         ret += Board.NUM_CELLS;
      }
      while (ret > Board.NUM_CELLS) {
         ret -= Board.NUM_CELLS;
      }
      return ret;
   }
   
   /**
    * Gets the current Cursor position.
    * 
    * @return A List of two integers corresponding to the position.
    * @see #getCoordsFromPosition(int)
    */
   public List<Integer> getCurrentCursor() {
      return getCoordsFromPosition(curPos);
   }
   
   /**
    * Gets the previous cursor coordinates as a List of two Integers. This is equivalent to
    * {@link #getCoordsFromPosition(int)} with an argument of the current position minus one.
    * 
    * @return The List of two integers corresponding to the previous cursor coordinates.
    * @see #getCoordsFromPosition(int)
    */
   public List<Integer> getPreviousCursor() {
      return getCoordsFromPosition(curPos - 1);
   }
   
   /**
    * Advances the current cursor by the given int.
    * 
    * @param i
    *           The number of positions to advance by (negative will backtrack, 0 will not move it).
    * @return True if the cursor moved. False otherwise.
    */
   protected boolean advanceCursorBy(int i) {
      int oldPos = curPos;
      curPos = fixPosition(curPos + i);
      return curPos != oldPos;
   }
   
   /**
    * Sets the current entry cursor to the given row and column position.
    * 
    * @param row
    *           an int in [1, {@link Board#NUM_COLS}]
    * @param col
    *           an int in [1, {@link Board#NUM_ROWS}]
    * @return True if the cursor moved. False otherwise.
    */
   protected boolean setCursorTo(int row, int col) {
      return advanceCursorBy(getCursorPositionFor(row, col) - curPos);
   }
   
   // Frozen state methods
   /**
    * Returns the current frozen state.
    * 
    * @return True if the current paints are frozen, False otherwise.
    */
   public boolean arePaintsFrozen() {
      return frozen;
   }
   
   /**
    * Changes the current frozen state to the opposite state.
    * 
    * @return Returns true if the frozen state changed successfully.
    */
   protected boolean toggleFrozenPaints() {
      frozen = !frozen;
      return true;
   }
   
   // Selected species methods
   /**
    * Sets the selected species to the given {@link Species}.
    * 
    * @param value
    * @return True if the current species changed.
    */
   protected boolean setSelectedSpecies(Species value) {
      boolean changed = !value.equals(curSpecies);
      if (changed) {
         curSpecies = value;
      }
      return changed;
   }
   
   /**
    * Returns the current species.
    * 
    * @return The current {@link Species}
    */
   public Species getSelectedSpecies() {
      SpeciesManager manager = getSpeciesManager();
      List<Species> species = getCurrentTeam().getSpecies(manager);
      if (!species.contains(curSpecies)) {
         if (species.size() > 0) {
            curSpecies = species.get(0);
         } else {
            curSpecies = Species.AIR;
         }
      }
      return curSpecies;
   }
   
   // Selected paints methods
   /**
    * Returns the currently selected species paint.
    * 
    * @return The currently selected {@link SpeciesPaint}.
    */
   public SpeciesPaint getCurrentSpeciesPaint() {
      Species s = getSelectedSpecies();
      return new SpeciesPaint(s, frozen, isMegaActive(s.getName()));
   }
   
   /**
    * Returns the list of current species paints.
    * 
    * @return {@link List}&lt{@link SpeciesPaint}&gt which reflects the current frozen state and
    *         team.
    */
   public List<SpeciesPaint> getCurrentPaints() {
      List<SpeciesPaint> ret = new ArrayList<SpeciesPaint>();
      SpeciesManager manager = getSpeciesManager();
      List<Species> species = getCurrentTeam().getSpecies(manager);
      for (Species s : species) {
         boolean isFrozen = !s.getEffect().equals(Effect.AIR) && frozen;
         boolean isMega = isMegaActive(s.getName());
         ret.add(new SpeciesPaint(s, isFrozen, isMega));
      }
      return ret;
   }
   
   public void setDataChanged() {
      recomputeResults(false);
   }
   
   public SimulationResult getCurrentResult() {
      if (!resultsCurrent && !resultsComputing) {
         recomputeResults(false);
      }
      return selectedResult;
   }
   
   public Collection<SimulationResult> getResults() {
      if (!resultsCurrent && !resultsComputing) {
         recomputeResults(false);
      }
      return bestResults;
   }
   
   public void computeNow() {
      recomputeResults(true);
   }
   
   /**
    * 
    */
   private void recomputeResults(boolean force) {
      resultsCurrent = false;
      bestResults = null;
      selectedResult = null;
      if (force || getAutoCompute()) {
         regenerateResult(force);
      }
   }
   
   private void regenerateResult(boolean computeNow) {
      if (resultsComputing) {
         forkJoinPool.shutdownNow();
         forkJoinPool = getNewPool();
      }
      resultsComputing = true;
      processUUID = UUID.randomUUID();
      SimulationCore core = new SimulationCore(getUser(), processUUID);
      scheduledExecutor.schedule(new Runnable() {
         @Override
         public void run() {
            if (core.isCurrent()) {
               LOG.info(getString(KEY_SIMULATION_START));
               forkJoinPool.execute(core);
            }
         }
         
      }, computeNow ? 0 : SIMULATION_DELAY, TimeUnit.MILLISECONDS);
   }
   
   private ForkJoinPool getNewPool() {
      ForkJoinPool pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors(),
            new ForkJoinPool.ForkJoinWorkerThreadFactory() {
               @Override
               public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
                  final ForkJoinWorkerThread result = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
                  result.setPriority(Thread.MIN_PRIORITY);
                  return result;
               }
            }, null, false);
      return pool;
   }
   
   public int getMegaProgress() {
      return getBoard().getMegaProgress();
   }
   
   public boolean isMegaAllowed() {
      String megaSlotName = getCurrentTeam().getMegaSlotName();
      return megaSlotName != null;
   }
   
   public synchronized boolean setBestResults(Collection<SimulationResult> results) {
      long endTime = System.currentTimeMillis();
      resultsCurrent = true;
      resultsComputing = false;
      boolean changed = false;
      if (results != null && !results.isEmpty()) {
         SimulationResult firstResult = results.iterator().next();
         if (firstResult != null && firstResult.getID() != null && firstResult.getID().equals(processUUID)) {
            bestResults = new TreeSet<SimulationResult>(getCurrentGradingMode().getGradingMetric());
            bestResults.addAll(results);
            selectedResult = bestResults.iterator().next();
            long startTime = selectedResult.getStartTime();
            changed = true;
            if (SwingUtilities.isEventDispatchThread()) {
               LOG.info(getString(KEY_SIMULATION_COMPLETE, endTime - startTime));
            } else {
               SwingUtilities.invokeLater(new Runnable() {
                  @Override
                  public void run() {
                     LOG.info(getString(KEY_SIMULATION_COMPLETE, endTime - startTime));
                  }
               });
            }
         }
      }
      return changed;
   }
   
   /**
    * @param result
    * @return
    */
   protected boolean setSelectedResult(SimulationResult result) {
      boolean changed = false;
      if (result != null && bestResults != null && bestResults.contains(result)) {
         selectedResult = result;
         changed = true;
         LOG.info(getString(KEY_SELECTING_RESULT, selectedResult.toString()));
      }
      return changed;
   }
   
   public boolean doSelectedMove() {
      if (selectedResult == null || resultsComputing) {
         return false;
      }
      // capture the current board, and remaining health and moves.
      Board prevBoard = getBoard();
      int prevHealth = getRemainingHealth();
      int prevMoves = getRemainingMoves();
      // identify the new state
      Board newBoard = selectedResult.getBoard();
      int newHealth = (int) (Math.max(prevHealth - selectedResult.getNetScore().getAverage(), 0));
      int newMoves = selectedResult.getMove().isEmpty() ? prevMoves : Math.max(prevMoves - 1, 1);
      // if the state is different,
      boolean changed = getBoardManager().setBoard(newBoard) || newHealth != prevHealth || prevMoves != newMoves;
      if (changed) {
         // then push the current state to the undo stack
         undoStack.push(new UndoRedoItem(prevBoard, prevHealth, prevMoves));
         // clear the redo stack of any possible states
         redoStack.clear();
         setRemainingHealth(newHealth);
         setRemainingMoves(newMoves);
         setDataChanged();
      }
      return changed;
   }
   
   public boolean undoMove() {
      if (undoStack.isEmpty()) {
         return false;
      }
      // push the current state on the redo stack
      redoStack.push(getCurrentState());
      // capture the current moves and health remaining
      int prevHealth = getRemainingHealth();
      int prevMoves = getRemainingMoves();
      // Pop the new state
      UndoRedoItem undone = undoStack.pop();
      Board newBoard = undone.getBoard();
      int newHealth = undone.getHealth();
      int newMoves = undone.getMoves();
      // if anything changes,
      boolean changed = getBoardManager().setBoard(newBoard) || newHealth != prevHealth || newMoves != prevMoves;
      if (changed) {
         // then continue on to update health and moves
         setRemainingHealth(undone.getHealth());
         setRemainingMoves(undone.getMoves());
         setDataChanged();
      }
      return changed;
   }
   
   public boolean redoMove() {
      if (redoStack.isEmpty()) {
         return false;
      }
      // push the current state to the undo stack
      undoStack.push(getCurrentState());
      
      // capture the current moves and health remaining
      int prevHealth = getRemainingHealth();
      int prevMoves = getRemainingMoves();
      // Pop the new state
      UndoRedoItem redone = redoStack.pop();
      Board newBoard = redone.getBoard();
      int newHealth = redone.getHealth();
      int newMoves = redone.getMoves();
      // if anything changes,
      boolean changed = getBoardManager().setBoard(newBoard) || newHealth != prevHealth || newMoves != prevMoves;
      if (changed) {
         // then continue on to update health and moves
         setRemainingHealth(redone.getHealth());
         setRemainingMoves(redone.getMoves());
         setDataChanged();
      }
      return changed;
   }
   
   private UndoRedoItem getCurrentState() {
      return new UndoRedoItem(getBoard(), getRemainingHealth(), getRemainingMoves());
   }

   public UUID getAcceptedId() {
      return processUUID;
   }
   
   public void reportBug(String message) {
      new File("bugs").getAbsoluteFile().mkdir();
      makeBugMessageFile(message);
      copyBuildXML();
      buildBugReport();
      cleanupBuildXML();
   }
   
   private void copyBuildXML() {
      InputStream is = null;
      try {
         is = ClassLoader.getSystemResourceAsStream(BUILD_REPORT_RESOURCE);
         String absolutePath = new File(BUILD_REPORT_FILE).getAbsolutePath();
         Path path = Paths.get(absolutePath);
         Files.copy(is, path, StandardCopyOption.REPLACE_EXISTING);
      } catch (Exception e) {
         LOG.fine("ShuffleModel.copyBuildXML() : " + e.getLocalizedMessage());
      }
   }
   
   private void cleanupBuildXML() {
      File f = new File(BUILD_REPORT_FILE).getAbsoluteFile();
      try {
         f.delete();
      } catch (Exception e) {
         LOG.fine("ShuffleModel.cleanupBuildXML() : " + e.getLocalizedMessage());
      }
   }
   
   /**
    * @param message
    */
   private void makeBugMessageFile(String message) {
      File file = null;
      try {
         file = ((File) EntryType.FILE.parseValue(null, BUG_DETAILS_FILE)).getAbsoluteFile();
      } catch (Exception e) {
         if (e != null && e instanceof IOException) {
            LOG.log(Level.WARNING, getString(KEY_BUG_FILE_IOE), e);
         } else {
            LOG.log(Level.WARNING, getString(KEY_BUG_FILE_SAVEPROBLEM, BUG_DETAILS_FILE), e);
         }
      }
      if (file != null) {
         try {
            file.createNewFile();
            if (!file.canWrite()) {
               LOG.warning(getString(KEY_BUG_FILE_READONLY, file.getAbsolutePath()));
            }
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
               bw.write(message);
            } catch (IOException e) {
               LOG.log(Level.WARNING, getString(KEY_BUG_FILE_SAVEPROBLEM, file.getAbsolutePath()), e);
            }
         } catch (IOException e) {
            LOG.warning(getString(KEY_BUG_FILE_READONLY, file.getAbsolutePath()));
         }
      }
   }
   
   /**
	 * 
	 */
   private void buildBugReport() {
      try {
         Project p = getErrorProject();
         p.executeTarget(p.getDefaultTarget());
         LOG.log(Level.INFO,
               getString(KEY_BUG_FILE_SUCCESS, Paths.get(p.getProperty("zipname")).toAbsolutePath().toString()));
      } catch (Exception e) {
         LOG.info(getString(KEY_BUG_REPORT_PROBLEM, e.getMessage()));
         e.printStackTrace();
      }
   }
   
   /**
    * @return
    * @throws Exception
    */
   private Project getErrorProject() throws Exception {
      if (errorProject == null) {
         Path buildFilePath = Paths.get(BUILD_REPORT_FILE);
         LOG.finer("Build file path set to " + String.valueOf(buildFilePath));
         Project p = new Project();
         p.setSystemProperties();
         p.setUserProperty("ant.file", buildFilePath.toAbsolutePath().toString());
         LOG.finer("Project ant.file property set.");
         p.init();
         LOG.finer("Project.init() called.");
         ProjectHelper helper = ProjectHelper.getProjectHelper();
         LOG.finer("ProjectHelper created.");
         p.addReference("ant.projectHelper", helper);
         LOG.finer("Project ant.projectHelper reference set.");
         p.addReference("user.dir", System.getProperty("user.dir"));
         LOG.finer("Project user.dir reference set.");
         helper.parse(p, buildFilePath.toAbsolutePath().toFile());
         LOG.finer("PrjectHelper.parse() called.");
         errorProject = p;
         p.addBuildListener(getBuildListener());
         LOG.finer("BuildListener added.");
      }
      return errorProject;
   }
   
   private BuildListener getBuildListener() {
      if (antListener == null) {
         DefaultLogger logger = new DefaultLogger();
         PrintStream ps = null;
         try {
            ps = new PrintStream(new File("log/bugReportLog.txt").getAbsolutePath());
            ps.print("");
         } catch (FileNotFoundException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionDetails = sw.toString();
            LOG.warning(exceptionDetails);
         }
         if (ps == null) {
            logger.setErrorPrintStream(System.err);
            logger.setOutputPrintStream(System.out);
         } else {
            logger.setErrorPrintStream(ps);
            logger.setOutputPrintStream(ps);
         }
         logger.setMessageOutputLevel(Project.MSG_INFO);
         antListener = logger;
      }
      return antListener;
   }
   
   protected boolean setAutoCompute(boolean autoCompute) {
      boolean prevState = getAutoCompute();
      boolean changing = prevState != autoCompute;
      if (changing) {
         getPreferencesManager().setEntry(EntryType.BOOLEAN, KEY_AUTO_COMPUTE, autoCompute);
      }
      return changing;
   }
   
   public boolean setFeederPreferences(int numFeeders, int feederHeight, boolean autoCompute) {
      boolean changing = numFeeders != getNumFeeders() || feederHeight != getFeederHeight()
            || autoCompute != getAutoCompute();
      if (changing) {
         getPreferencesManager().setEntry(EntryType.INTEGER, KEY_NUM_FEEDERS, numFeeders);
         getPreferencesManager().setEntry(EntryType.INTEGER, KEY_FEEDER_HEIGHT, feederHeight);
         getPreferencesManager().setEntry(EntryType.BOOLEAN, KEY_AUTO_COMPUTE, autoCompute);
      }
      return changing;
   }
   
   public boolean getAutoCompute() {
      return getPreferencesManager().getBooleanValue(KEY_AUTO_COMPUTE, DEFAULT_AUTO_COMPUTE);
   }
   
   public int getNumFeeders() {
      return getPreferencesManager().getIntegerValue(KEY_NUM_FEEDERS, DEFAULT_NUM_FEEDERS);
   }
   
   public int getFeederHeight() {
      return getPreferencesManager().getIntegerValue(KEY_FEEDER_HEIGHT, DEFAULT_FEEDER_HEIGHT);
   }
   
   /**
    * @param active
    * @return
    */
   public boolean setMegaActive(boolean active) {
      int threshold = getCurrentThreshold();
      int newProgress = active ? threshold : 0;
      return setMegaProgress(newProgress);
   }
   
   public boolean setMegaProgress(int progress) {
      int threshold = getCurrentThreshold();
      progress = Math.min(threshold, Math.max(progress, 0));
      String megaSlotName = getCurrentTeam().getMegaSlotName();
      int prevProgress = getMegaProgress();
      boolean changed = megaSlotName != null && prevProgress != progress;
      if (changed) {
         Board toSet = getBoard();
         toSet.setMegaProgress(progress);
         getBoardManager().setBoard(toSet);
      }
      return changed;
   }
   
   /**
    * @return
    */
   private int getCurrentThreshold() {
      return getCurrentTeam().getMegaThreshold(getSpeciesManager(), getUser().getRosterManager());
   }
   
   /**
    * @return
    */
   public Comparator<SimulationResult> getGradingMetric() {
      return getCurrentGradingMode().getGradingMetric();
   }
   
   public GradingMode getCurrentGradingMode() {
      GradingMode mode = gradeMode;
      if (mode == null) {
         String modeName = getPreferencesManager().getStringValue(KEY_GRADING_MODE);
         if (modeName != null) {
            for (GradingMode gm : GradingMode.values()) {
               if (gm.toString().equals(modeName)) {
                  mode = gm;
                  break;
               }
            }
         }
         if (mode == null) {
            mode = DEFAULT_GRADING;
         }
         setGradeMode(mode);
      }
      return mode;
   }
   
   protected boolean setGradingMode(GradingMode mode) {
      boolean changed = mode != null && mode != getCurrentGradingMode() && setGradeMode(mode);
      if (changed) {
         if (bestResults != null && !bestResults.isEmpty()) {
            Collection<SimulationResult> prev = new ArrayList<SimulationResult>(bestResults);
            bestResults = new TreeSet<SimulationResult>(getCurrentGradingMode().getGradingMetric());
            for (SimulationResult result : prev) {
               bestResults.add(result);
            }
            selectedResult = bestResults.iterator().next();
         }
      }
      return changed;
   }
   
   private boolean setGradeMode(GradingMode mode) {
      if (mode == null) {
         return false;
      }
      boolean changed = gradeMode != mode;
      if (changed) {
         gradeMode = mode;
         getPreferencesManager().setEntry(EntryType.STRING, KEY_GRADING_MODE, mode.toString());
      }
      return changed;
   }

   /**
    * Gets the number of remaining moves for the current stage.
    * 
    * @return The number of moves left, as an integer.
    */
   public int getRemainingMoves() {
      return getPreferencesManager().getIntegerValue(KEY_MOVES_REMAINING, Stage.DEFAULT_MOVES);
   }

   /**
    * Gets the remaining health for the current stage.
    * 
    * @return The amount of health remaining, as an integer.
    */
   public int getRemainingHealth() {
      return getPreferencesManager().getIntegerValue(KEY_HEALTH_REMAINING, Stage.DEFAULT_HEALTH);
   }
   
   /**
    * Sets the remaining moves for the current stage.
    */
   protected boolean setRemainingMoves(int moves) {
      boolean changed = moves != getRemainingMoves();
      getPreferencesManager().setEntry(EntryType.INTEGER, KEY_MOVES_REMAINING, moves);
      return changed;
   }
   
   /**
    * Sets the remaining health for the current stage.
    */
   protected boolean setRemainingHealth(int health) {
      boolean changed = health != getRemainingHealth();
      getPreferencesManager().setEntry(EntryType.INTEGER, KEY_HEALTH_REMAINING, health);
      return changed;
   }
   
   private class UndoRedoItem {
      private final Board board;
      private final int health;
      private final int moves;
      
      public UndoRedoItem(Board b, int hp, int movesLeft) {
         board = b;
         health = hp;
         moves = movesLeft;
      }
      
      public Board getBoard() {
         return board;
      }
      
      public int getHealth() {
         return health;
      }
      
      public int getMoves() {
         return moves;
      }
   }
   
   public static final String KEY_DISABLED_EFFECTS = "DISABLED_EFFECTS";
   
   /**
    * @param disabledEffects
    * @return
    */
   protected boolean setDisabledEffects(Collection<Effect> disabledEffects) {
      boolean changed = false;
      StringBuilder sb = new StringBuilder();
      for (Effect e : disabledEffects) {
         sb.append(" ");
         sb.append(e.toString());
      }
      String saveString = sb.toString().trim();
      ConfigManager preferencesManager = getPreferencesManager();
      if (saveString.isEmpty()) {
         changed |= preferencesManager.removeEntry(EntryType.STRING, KEY_DISABLED_EFFECTS);
      } else {
         changed |= preferencesManager.setEntry(EntryType.STRING, KEY_DISABLED_EFFECTS, saveString);
      }
      return changed;
   }
   
   public Collection<Effect> getDisabledEffects() {
      Collection<Effect> ret = new ArrayList<Effect>();
      String disabledEffectsString = getPreferencesManager().getStringValue(KEY_DISABLED_EFFECTS, "").trim();
      String[] tokens = disabledEffectsString.split("\\s");
      for (String s : tokens) {
         Effect e = Effect.getEffect(s);
         if (!e.equals(Effect.NONE)) {
            ret.add(e);
         }
      }
      return ret;
   }
}
