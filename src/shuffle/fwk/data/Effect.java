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

package shuffle.fwk.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import shuffle.fwk.data.simulation.SimulationCore;
import shuffle.fwk.data.simulation.SimulationTask;
import shuffle.fwk.data.simulation.effects.ActivateComboEffect;
import shuffle.fwk.data.simulation.effects.ActivateMegaComboEffect;
import shuffle.fwk.data.simulation.util.NumberSpan;

public enum Effect {
   /**
    * Attacks can occasionally deal greater damage than usual.
    */
   OPPORTUNIST {
      
      @Override
      public NumberSpan getScoreMultiplier(ActivateComboEffect comboEffect, SimulationTask task) {
         return getMultiplier(comboEffect, task, 0.5);
      }
   },
   /**
    * Attacks sometimes deal greater damage than usual. 1.5x modifier
    */
   HEAVY_HITTER {
      
      @Override
      public NumberSpan getScoreMultiplier(ActivateComboEffect comboEffect, SimulationTask task) {
         return getMultiplier(comboEffect, task, 0.5);
      }
   },
   /**
    * Attacks sometimes deal greater damage than usual. 1.5x modifier
    */
   DRAGON_TALON {
      
      @Override
      public NumberSpan getScoreMultiplier(ActivateComboEffect comboEffect, SimulationTask task) {
         return getMultiplier(comboEffect, task, 0.5);
      }
   },
   /**
    * Attacks do more damage when you make a match of 4.
    */
   POWER_OF_4 {
      @Override
      public NumberSpan getScoreMultiplier(ActivateComboEffect comboEffect, SimulationTask task) {
         return getMultiplier(comboEffect, task, 0.5);
      }
   },
   /**
    * Attacks do more damage when you make a match of 5.
    */
   POWER_OF_5 {
      @Override
      public NumberSpan getScoreMultiplier(ActivateComboEffect comboEffect, SimulationTask task) {
         return getMultiplier(comboEffect, task, 0.5);
      }
   },
   /**
    * Attacks do more damage when things are looking desperate.
    */
   LAST_DITCH_EFFORT {
      
      @Override
      protected boolean canActivate(ActivateComboEffect comboEffect, SimulationTask task) {
         return super.canActivate(comboEffect, task) && task.getState().getCore().getRemainingMoves() <= 3;
      }
      
      @Override
      public NumberSpan getScoreMultiplier(ActivateComboEffect comboEffect, SimulationTask task) {
         return getMultiplier(comboEffect, task, 0.5);
      }
   },
   /**
    * Does more damage the more times in a row it is triggered. <br>
    * hitting/damage streak, first activation: 1.2 <br>
    * hitting/damage streak, second activation: 1.44 (1.2^2) <br>
    * hitting/damage streak, third activation: 1.728 (1.2^3) <br>
    * hitting/damage streak, fourth or higher activation: 2
    */
   HITTING_STREAK {
      
      @Override
      public NumberSpan getScoreMultiplier(ActivateComboEffect comboEffect, SimulationTask task) {
         return getMultiplier(comboEffect, task, 0.2);
      }
   },
   /**
    * Does more damage the more times in a row it is triggered. <br>
    * hitting/damage streak, first activation: 1.2 <br>
    * hitting/damage streak, second activation: 1.44 (1.2^2) <br>
    * hitting/damage streak, third activation: 1.728 (1.2^3) <br>
    * hitting/damage streak, fourth or higher activation: 2
    */
   DAMAGE_STREAK {
      
      @Override
      public NumberSpan getScoreMultiplier(ActivateComboEffect comboEffect, SimulationTask task) {
         return getMultiplier(comboEffect, task, 0.2);
      }
   },
   /**
    * Damage may randomly be increased or decreased.
    */
   RISK_TAKER {
      @Override
      public NumberSpan getScoreMultiplier(ActivateComboEffect comboEffect, SimulationTask task) {
         if (canActivate(comboEffect, task)) {
            // Here we are tracking three separate things
            // The minimum possible damage multiplier
            double min = 1.0 / 3.0;
            // The maximum possible damage multiplier
            double max = 9.0 / 3.0;
            // The average boost compared to 1.0
            double normalAvgBoost = 2.0 / 3.0;
            double rate = getOdds(task, comboEffect.getNumBlocks());
            double avg = 1 + (normalAvgBoost * rate);
            // Which are merged into a single multiplier here,
            // the min is the lowest it could go, the max is the highest,
            // the 'avg' is the net average multiplier
            NumberSpan multiplier = new NumberSpan(min, max, avg, 1);
            return multiplier.multiplyBy(super.getScoreMultiplier(comboEffect, task));
         } else {
            // If it CAN'T occur, then don't incorporate it.
            // This is the special case of multipliers, since it can decrease as well as increase.
            return super.getScoreMultiplier(comboEffect, task);
         }
      }
   },
   /**
    * Increases damage done by your last three attacks in a stage by 50%.
    */
   SWARM {
      
      @Override
      protected boolean canActivate(ActivateComboEffect comboEffect, SimulationTask task) {
         return super.canActivate(comboEffect, task) && task.getState().getCore().getRemainingMoves() <= 3;
      }
      
      @Override
      public NumberSpan getScoreMultiplier(ActivateComboEffect comboEffect, SimulationTask task) {
         return getMultiplier(comboEffect, task, 0.5);
      }
   },
   /**
    * Attacks do more damage when things are looking desperate.
    */
   STEELY_RESOLVE {
      
      @Override
      protected boolean canActivate(ActivateComboEffect comboEffect, SimulationTask task) {
         return super.canActivate(comboEffect, task) && task.getState().getCore().getRemainingMoves() <= 3;
      }
      
      @Override
      public NumberSpan getScoreMultiplier(ActivateComboEffect comboEffect, SimulationTask task) {
         return getMultiplier(comboEffect, task, 0.5);
      }
   },
   /**
    * Increases damage done by any Fire types in a combo.
    */
   PYRE {
      
      @Override
      protected void doSpecial(ActivateComboEffect comboEffect, SimulationTask task) {
         ifThenSetSpecial(comboEffect, task, PkmType.FIRE, 0.2);
      }
   },
   /**
    * Increases damage done by any Dragon types in a combo.
    */
   DANCING_DRAGONS {
      
      @Override
      protected void doSpecial(ActivateComboEffect comboEffect, SimulationTask task) {
         ifThenSetSpecial(comboEffect, task, PkmType.DRAGON, 0.2);
      }
   },
   /**
    * Increases damage done by any fighting types in a combo.
    */
   PUMMEL {
      
      @Override
      protected void doSpecial(ActivateComboEffect comboEffect, SimulationTask task) {
         ifThenSetSpecial(comboEffect, task, PkmType.FIGHTING, 0.2);
      }
      
   },
   /**
    * Increases damage of Fairy-type moves in a Combo.
    */
   PIXIE_POWER {
      
      @Override
      protected void doSpecial(ActivateComboEffect comboEffect, SimulationTask task) {
         ifThenSetSpecial(comboEffect, task, PkmType.FAIRY, 0.5);
      }
   },
   /**
    * Increases damage of Dark-type moves in a combo.
    */
   SINISTER_POWER {
      
      @Override
      protected void doSpecial(ActivateComboEffect comboEffect, SimulationTask task) {
         ifThenSetSpecial(comboEffect, task, PkmType.DARK, 0.5);
      }
   },
   /**
    * The more of this Species in the puzzle area, the more damage.
    */
   CROWD_CONTROL {
      
      /**
       * {@inheritDoc}
       */
      @Override
      public NumberSpan getBonusValue(ActivateComboEffect comboEffect, SimulationTask task) {
         NumberSpan ret = new NumberSpan();
         if (canActivate(comboEffect, task)) {
            Board board = task.getState().getBoard();
            Species effectSpecies = task.getEffectSpecies(comboEffect.getCoords());
            int num = task
                  .findMatches(36, true,
                        (r, c, s) -> (s.equals(
                              effectSpecies)
                        && (!task.isActive(r, c) || board.isFrozenAt(r, c) || task.getClaimsFor(r, c).size() > 0)))
                  .size() / 2;
            ret = new NumberSpan(0, num, getOdds(task, comboEffect.getNumBlocks()));
         }
         return ret;
      }
      
      @Override
      public NumberSpan getBonusScoreFor(double basicScore, NumberSpan value, double typeModifier) {
         return value.multiplyBy(50);
      }
      
   },
   /**
    * The more disruptions on the board, the greater the damage.
    */
   COUNTERATTACK {
      
      /**
       * {@inheritDoc}
       */
      @Override
      public NumberSpan getBonusValue(ActivateComboEffect comboEffect, SimulationTask task) {
         NumberSpan ret = new NumberSpan();
         if (canActivate(comboEffect, task)) {
            Board board = task.getState().getBoard();
            int num = task.findMatches(36, false, (r, c, s) -> board.isFrozenAt(r, c) || s.getEffect().isDisruption())
                  .size() / 2;
            ret = new NumberSpan(0, num, getOdds(task, comboEffect.getNumBlocks()));
         }
         return ret;
      }
      
      @Override
      public NumberSpan getBonusScoreFor(double basicScore, NumberSpan value, double typeModifier) {
         return value.multiplyBy(20 * typeModifier);
      }
   },
   /**
    * Does more damage when the opponent has more HP left.
    */
   VITALITY_DRAIN {
      
      @Override
      protected void doSpecial(ActivateComboEffect comboEffect, SimulationTask task) {
         ifThenAddScore(comboEffect, task, () -> ((int) (0.1 * task.getState().getCore().getRemainingHealth())));
      }
   },
   /**
    * Sometimes increases damage and leaves opponent paralyzed.
    */
   QUAKE {
      
      @Override
      public NumberSpan getScoreMultiplier(ActivateComboEffect comboEffect, SimulationTask task) {
         return getMultiplier(comboEffect, task, 0.2);
      }
   },
   /**
    * Combos do more damage if the opponent is Ghost type.
    */
   FEARLESS {
      // TODO Apparently, this causes attacks to be super effective. Is it actually this, or is it a
      // flat multiplier?
   },
   /**
    * Does more damage against Flying, Bug, or Fairy types.
    */
   SWAT {
      
      private final List<PkmType> targets = Collections
            .unmodifiableList(Arrays.asList(PkmType.FLYING, PkmType.BUG, PkmType.FAIRY));
            
      @Override
      protected boolean canActivate(ActivateComboEffect comboEffect, SimulationTask task) {
         return targets.contains(task.getState().getCore().getStage().getType())
               && super.canActivate(comboEffect, task);
      }
      
      @Override
      public NumberSpan getScoreMultiplier(ActivateComboEffect comboEffect, SimulationTask task) {
         return getMultiplier(comboEffect, task, 0.2);
      }
   },
   /**
    * Increases damage for attacks that are not very effective.
    */
   BRUTE_FORCE {
      
      @Override
      protected boolean canActivate(ActivateComboEffect comboEffect, SimulationTask task) {
         if (!super.canActivate(comboEffect, task)) {
            return false;
         }
         Species effectSpecies = task.getEffectSpecies(comboEffect.getCoords());
         PkmType effectType = effectSpecies.getType();
         PkmType stageType = task.getState().getCore().getStage().getType();
         return PkmType.getMultiplier(effectType, stageType) < 1.0;
      }
      
      @Override
      public NumberSpan getScoreMultiplier(ActivateComboEffect comboEffect, SimulationTask task) {
         return getMultiplier(comboEffect, task, 1);
      }
   },
   /**
    * Occasionally erases one extra matching Species elsewhere.
    */
   QUIRKY {
      
      @Override
      protected void doSpecial(ActivateComboEffect comboEffect, SimulationTask task) {
         if (canActivate(comboEffect, task)) {
            Species effectSpecies = task.getEffectSpecies(comboEffect.getCoords());
            List<Integer> matches = task.findMatches(36, false, (r, c, s) -> s.equals(effectSpecies));
            if (!matches.isEmpty()) {
               double odds = getOdds(task, comboEffect.getNumBlocks());
               if (matches.size() / 2 > 1 || odds < 1.0) {
                  task.setIsRandom();
               }
               if (odds >= Math.random()) {
                  int blockIndex = getRandomInt(matches.size() / 2);
                  int row = matches.get(blockIndex * 2);
                  int col = matches.get(blockIndex * 2 + 1);
                  List<Integer> toErase = new ArrayList<Integer>(Arrays.asList(row, col));
                  eraseBonus(task, toErase, true);
               }
            }
         }
      }
   },
   /**
    * Occasionally erases two extra matching Species elsewhere.
    */
   QUIRKY_P {
      
      @Override
      protected void doSpecial(ActivateComboEffect comboEffect, SimulationTask task) {
         if (canActivate(comboEffect, task)) {
            Species effectSpecies = task.getEffectSpecies(comboEffect.getCoords());
            List<Integer> matches = task.findMatches(36, false, (r, c, s) -> s.equals(effectSpecies));
            if (!matches.isEmpty()) {
               double odds = getOdds(task, comboEffect.getNumBlocks());
               if (matches.size() / 2 > 2 || odds < 1.0) {
                  task.setIsRandom();
               }
               if (odds >= Math.random()) {
                  List<Integer> randoms = getUniqueRandoms(0, matches.size() / 2, 2);
                  List<Integer> toErase = new ArrayList<Integer>();
                  for (Integer i : randoms) {
                     int row = matches.get(i * 2);
                     int col = matches.get(i * 2 + 1);
                     toErase.addAll(Arrays.asList(row, col));
                  }
                  eraseBonus(task, toErase, true);
               }
            }
         }
      }
   },
   /**
    * Occasionally changes when a foe will next disrupt your play.
    */
   PRANK {
   
   },
   /**
    * Occasionally erases one of the foe's disruptions on the board.
    */
   STABILIZE {
      
      @Override
      protected void doSpecial(ActivateComboEffect comboEffect, SimulationTask task) {
         if (canActivate(comboEffect, task)) {
            Board board = task.getState().getBoard();
            List<Integer> matches = task.findMatches(36, false,
                  (r, c, s) -> board.isFrozenAt(r, c) || s.getEffect().isDisruption());
            if (!matches.isEmpty()) {
               double odds = getOdds(task, comboEffect.getNumBlocks());
               if (matches.size() > 2 || odds < 1.0) {
                  task.setIsRandom();
               }
               if (odds >= Math.random()) {
                  int blockIndex = getRandomInt(matches.size() / 2);
                  int row = matches.get(blockIndex * 2);
                  int col = matches.get(blockIndex * 2 + 1);
                  List<Integer> toErase = Arrays.asList(row, col);
                  eraseBonus(task, toErase, false);
               }
            }
         }
      }
   },
   /**
    * Occasionally erases two of the foe's disruptions on the board.
    */
   STABILIZE_P {
      
      @Override
      protected void doSpecial(ActivateComboEffect comboEffect, SimulationTask task) {
         if (canActivate(comboEffect, task)) {
            Board board = task.getState().getBoard();
            List<Integer> match = task.findMatches(36, false,
                  (r, c, s) -> board.isFrozenAt(r, c) || s.getEffect().isDisruption());
            if (!match.isEmpty()) {
               double odds = getOdds(task, comboEffect.getNumBlocks());
               if (match.size() / 2 > 2 || odds < 1.0) {
                  task.setIsRandom();
               }
               if (odds >= Math.random()) {
                  List<Integer> randoms = getUniqueRandoms(0, match.size() / 2, 2);
                  List<Integer> toClear = new ArrayList<Integer>(randoms.size() * 2);
                  for (int i : randoms) {
                     int row = match.get(i * 2);
                     int col = match.get(i * 2 + 1);
                     toClear.add(row);
                     toClear.add(col);
                  }
                  eraseBonus(task, toClear, false);
               }
            }
         }
      }
   },
   /**
    * Occasionally erases all of the foe's disruptions.
    */
   DISRUPT_BUSTER {
      
      @Override
      protected void doSpecial(ActivateComboEffect comboEffect, SimulationTask task) {
         if (canActivate(comboEffect, task)) {
            Board board = task.getState().getBoard();
            List<Integer> match = task.findMatches(36, false,
                  (r, c, s) -> board.isFrozenAt(r, c) || s.getEffect().isDisruption());
            if (!match.isEmpty()) {
               task.setIsRandom();
               if (doesActivate(comboEffect, task)) {
                  eraseBonus(task, match, false);
               }
            }
         }
      }
   },
   /**
    * Destroys one breakable-rock disruption without fail. (wood)
    */
   ROCK_BREAK {
      
      @Override
      protected void doSpecial(ActivateComboEffect comboEffect, SimulationTask task) {
         if (canActivate(comboEffect, task)) {
            List<Integer> matches = task.findMatches(36, false, (r, c, s) -> s.getEffect().equals(WOOD));
            if (!matches.isEmpty()) {
               if (matches.size() > 2) {
                  task.setIsRandom();
               }
               int blockIndex = getRandomInt(matches.size() / 2);
               int row = matches.get(blockIndex * 2);
               int col = matches.get(blockIndex * 2 + 1);
               List<Integer> toErase = Arrays.asList(row, col);
               WOOD.eraseBonus(task, toErase, true);
            }
         }
      }
   },
   /**
    * Clears clouds within 1 space.
    */
   CLOUD_CLEAR {
   
   },
   /**
    * Clears one unbreakable-block disruption without fail.
    */
   BLOCK_SMASH {
      
      @Override
      protected void doSpecial(ActivateComboEffect comboEffect, SimulationTask task) {
         boolean canActivate = canActivate(comboEffect, task);
         if (canActivate) {
            List<Integer> matches = task.findMatches(36, false, (r, c, s) -> s.getEffect().equals(METAL));
            if (matches.size() > 2) {
               task.setIsRandom();
            }
            if (!matches.isEmpty() && doesActivate(comboEffect, task)) {
               int blockIndex = getRandomInt(matches.size() / 2);
               int row = matches.get(blockIndex * 2);
               int col = matches.get(blockIndex * 2 + 1);
               List<Integer> toErase = Arrays.asList(row, col);
               eraseBonus(task, toErase, false);
            }
         }
      }
   },
   /**
    * Removes one non-Support Pokemon icon without fail. that means any pokemon that you were forced
    * to have by the stage.
    */
   EJECT {
   },
   /**
    * Removes one barrier-type disruption without fail.
    */
   BARRIER_BASH {
      
      @Override
      protected void doSpecial(ActivateComboEffect comboEffect, SimulationTask task) {
         if (canActivate(comboEffect, task)) {
            Board board = task.getState().getBoard();
            List<Integer> matches = task.findMatches(36, false, (r, c, s) -> board.isFrozenAt(r, c));
            if (!matches.isEmpty()) {
               if (matches.size() / 2 > 1) {
                  task.setIsRandom();
               }
               int blockIndex = getRandomInt(matches.size() / 2);
               int row = matches.get(blockIndex * 2);
               int col = matches.get(blockIndex * 2 + 1);
               task.unfreezeAt(Arrays.asList(row, col));
            }
         }
      }
      
   },
   /**
    * Can replace a disruption with one of your Pokemon.
    */
   SWAP {
   
   },
   /**
    * Can delay your opponent's disruptions for a turn.
    */
   CHILL {
   
   },
   /**
    * Can delay your opponent's disruptions for a turn.
    */
   ASTONISH {
   
   },
   /**
    * Occasionally disrupts a Ground-type opponent's disruptions.
    */
   FLAP {
   
   },
   /**
    * Can delay your opponent's disruptions for a turn.
    */
   MIND_ZAP {
   
   },
   /**
    * Can inflict the opponent with a burn for three turns. All Fire-type damage is increased by
    * 50%.
    */
   BURN {
      
      @Override
      protected void handleEffectFinished(ActivateComboEffect comboEffect, SimulationTask task) {
         ifThenSetSpecial(comboEffect, task, PkmType.FIRE, 0.5);
      }
   },
   /**
    * Leaves the foe spooked.
    */
   SPOOKIFY {
      
      @Override
      protected void handleEffectFinished(ActivateComboEffect comboEffect, SimulationTask task) {
         ifThenSetSpecial(comboEffect, task, PkmType.GHOST, 0.5);
      }
   },
   /**
    * Has a chance of freezing an opponent.
    */
   FREEZE {
      
      @Override
      protected void handleEffectFinished(ActivateComboEffect comboEffect, SimulationTask task) {
         ifThenSetSpecial(comboEffect, task, PkmType.ICE, 0.5);
      }
   },
   /**
    * Inflicts the opponent with sleep for three turns, preventing it from using its distortion.
    */
   SLEEP_CHARM {
   
   },
   /**
    * Leaves the foe paralyzed.
    */
   PARALYZE {
   
   },
   /**
    * Fills the Mega Guage of a Pokemon of the same type.
    */
   MEGA_BOOST {
      
      @Override
      protected boolean canActivate(ActivateComboEffect comboEffect, SimulationTask task) {
         if (task.getState().isMegaActive() || !super.canActivate(comboEffect, task)) {
            return false;
         }
         Species effectSpecies = task.getEffectSpecies(comboEffect.getCoords());
         Species megaSlot = task.getState().getCore().getMegaSlot();
         if (megaSlot != null && effectSpecies != null) {
            PkmType effectType = effectSpecies.getType();
            PkmType megaType = megaSlot.getType();
            return effectType.equals(megaType);
         } else {
            return false;
         }
      }
      
      @Override
      public void doSpecial(ActivateComboEffect comboEffect, SimulationTask task) {
         if (canActivate(comboEffect, task)) {
            double odds = getOdds(task, comboEffect.getNumBlocks());
            if (odds < 1.0) {
               task.setIsRandom();
            }
            if (odds >= Math.random()) {
               task.getState().increaseMegaProgress(3);
            }
         }
      }
   },
   /**
    * Attacks sometimes deal greater damage than usual.
    */
   HYPER_PUNCH {
      
      @Override
      public NumberSpan getScoreMultiplier(ActivateComboEffect comboEffect, SimulationTask task) {
         return getMultiplier(comboEffect, task, 2);
      }
   },
   /**
    * Same as {@link Effect#POWER_OF_4} except the modifier is 3.0 instead of 1.5
    */
   POWER_OF_4_P {
      
      @Override
      public NumberSpan getScoreMultiplier(ActivateComboEffect comboEffect, SimulationTask task) {
         return getMultiplier(comboEffect, task, 2);
      }
   },
   /**
    * Attacks do more damage when you make a match of 5.
    */
   POWER_OF_5_P {
      @Override
      public NumberSpan getScoreMultiplier(ActivateComboEffect comboEffect, SimulationTask task) {
         return getMultiplier(comboEffect, task, 2);
      }
   },
   /**
    * Identical to Crowd Control, except this activates more often.
    */
   CROWD_POWER {
      
      /**
       * {@inheritDoc}
       */
      @Override
      public NumberSpan getBonusValue(ActivateComboEffect comboEffect, SimulationTask task) {
         NumberSpan ret = new NumberSpan();
         if (canActivate(comboEffect, task)) {
            Board board = task.getState().getBoard();
            Species effectSpecies = task.getEffectSpecies(comboEffect.getCoords());
            int num = task
                  .findMatches(36, true,
                        (r, c, s) -> (s.equals(
                              effectSpecies)
                        && (!task.isActive(r, c) || board.isFrozenAt(r, c) || task.getClaimsFor(r, c).size() > 0)))
                  .size() / 2;
            ret = new NumberSpan(0, num, getOdds(task, comboEffect.getNumBlocks()));
         }
         return ret;
      }
      
      @Override
      public NumberSpan getBonusScoreFor(double basicScore, NumberSpan value, double typeModifier) {
         return value.multiplyBy(50);
      }
   },
   /**
    * Increases damage done by any Normal types in a combo. 2.5x multiplier
    */
   DOUBLE_NORMAL {
      
      @Override
      protected void doSpecial(ActivateComboEffect comboEffect, SimulationTask task) {
         ifThenSetSpecial(comboEffect, task, PkmType.NORMAL, 1.5);
      }
   },
   /**
    * Fills the Mega Guage of a Pokemon of the same type.
    */
   MEGA_BOOST_P {
      
      @Override
      protected boolean canActivate(ActivateComboEffect comboEffect, SimulationTask task) {
         return MEGA_BOOST.canActivate(comboEffect, task);
      }
      
      @Override
      public void doSpecial(ActivateComboEffect comboEffect, SimulationTask task) {
         if (canActivate(comboEffect, task)) {
            double odds = getOdds(task, comboEffect.getNumBlocks());
            if (odds < 1.0) {
               task.setIsRandom();
            }
            if (odds >= Math.random()) {
               task.getState().increaseMegaProgress(6);
            }
         }
      }
   },
   /**
    * Fills the Mega Guage of a Pokemon of the same type.
    */
   MEGA_BOOST_P_P {
      
      @Override
      protected boolean canActivate(ActivateComboEffect comboEffect, SimulationTask task) {
         return MEGA_BOOST.canActivate(comboEffect, task);
      }
      
      @Override
      public void doSpecial(ActivateComboEffect comboEffect, SimulationTask task) {
         if (canActivate(comboEffect, task)) {
            double odds = getOdds(task, comboEffect.getNumBlocks());
            if (odds < 1.0) {
               task.setIsRandom();
            }
            if (odds >= Math.random()) {
               task.getState().increaseMegaProgress(9);
            }
         }
      }
   },
   /**
    * Occasionally erases five of the foe's disruptions on the board.
    */
   STABILIZE_P_P {
      
      @Override
      protected void doSpecial(ActivateComboEffect comboEffect, SimulationTask task) {
         if (canActivate(comboEffect, task)) {
            Board board = task.getState().getBoard();
            List<Integer> match = task.findMatches(36, false,
                  (r, c, s) -> board.isFrozenAt(r, c) || s.getEffect().isDisruption());
            if (!match.isEmpty()) {
               double odds = getOdds(task, comboEffect.getNumBlocks());
               if (match.size() / 2 > 5 || odds < 1.0) {
                  task.setIsRandom();
               }
               if (odds >= Math.random()) {
                  List<Integer> randoms = getUniqueRandoms(0, match.size() / 2, 5);
                  List<Integer> toClear = new ArrayList<Integer>(randoms.size() * 2);
                  for (int i : randoms) {
                     int row = match.get(i * 2);
                     int col = match.get(i * 2 + 1);
                     toClear.add(row);
                     toClear.add(col);
                  }
                  eraseBonus(task, toClear, false);
               }
            }
         }
      }
   },
   /**
    * Sometimes destroys three breakable-rock disruptions. (wood)
    */
   ROCK_BREAK_P {
      
      @Override
      protected void doSpecial(ActivateComboEffect comboEffect, SimulationTask task) {
         if (canActivate(comboEffect, task)) {
            List<Integer> matches = task.findMatches(36, false, (r, c, s) -> s.getEffect().equals(WOOD));
            if (!matches.isEmpty()) {
               double odds = getOdds(task, comboEffect.getNumBlocks());
               if (matches.size() / 2 > 3 || odds < 1.0) {
                  task.setIsRandom();
               }
               if (odds >= Math.random()) {
                  List<Integer> randoms = getUniqueRandoms(0, matches.size() / 2, 3);
                  List<Integer> toErase = new ArrayList<Integer>(randoms.size() * 2);
                  for (int i : randoms) {
                     toErase.add(matches.get(i * 2));
                     toErase.add(matches.get(i * 2 + 1));
                  }
                  WOOD.eraseBonus(task, toErase, true);
               }
            }
         }
      }
   },
   /**
    * Sometimes destroys five breakable-rock disruptions. (wood)
    */
   ROCK_BREAK_P_P {
      
      @Override
      protected void doSpecial(ActivateComboEffect comboEffect, SimulationTask task) {
         if (canActivate(comboEffect, task)) {
            List<Integer> matches = task.findMatches(36, false, (r, c, s) -> s.getEffect().equals(WOOD));
            if (!matches.isEmpty()) {
               double odds = getOdds(task, comboEffect.getNumBlocks());
               if (matches.size() / 2 > 5 || odds < 1.0) {
                  task.setIsRandom();
               }
               if (odds >= Math.random()) {
                  List<Integer> randoms = getUniqueRandoms(0, matches.size() / 2, 5);
                  List<Integer> toErase = new ArrayList<Integer>(randoms.size() * 2);
                  for (int i : randoms) {
                     toErase.add(matches.get(i * 2));
                     toErase.add(matches.get(i * 2 + 1));
                  }
                  WOOD.eraseBonus(task, toErase, true);
               }
            }
         }
      }
   },
   /**
    * Clears 4 clouds within 1 space.
    */
   CLOUD_CLEAR_P {
   
   },
   /**
    * Clears 5 clouds within 1 space.
    */
   CLOUD_CLEAR_P_P {
   
   },
   /**
    * Same as {@link Effect#BLOCK_SMASH} except that the rate of occurance is "Sometimes" and the
    * number cleared is 3.
    */
   BLOCK_SMASH_P {
      
      @Override
      protected void doSpecial(ActivateComboEffect comboEffect, SimulationTask task) {
         if (canActivate(comboEffect, task)) {
            List<Integer> matches = task.findMatches(36, false, (r, c, s) -> s.getEffect().equals(METAL));
            if (!matches.isEmpty()) {
               double odds = getOdds(task, comboEffect.getNumBlocks());
               if (matches.size() / 2 > 3 || odds < 1.0) {
                  task.setIsRandom();
               }
               if (odds >= Math.random()) {
                  List<Integer> randoms = getUniqueRandoms(0, matches.size() / 2, 3);
                  List<Integer> toErase = new ArrayList<Integer>();
                  for (Integer i : randoms) {
                     int row = matches.get(i * 2);
                     int col = matches.get(i * 2 + 1);
                     toErase.addAll(Arrays.asList(row, col));
                  }
                  if (!toErase.isEmpty()) {
                     eraseBonus(task, toErase, true);
                  }
               }
            }
         }
      }
   },
   /**
    * Same as {@link Effect#BLOCK_SMASH} except that the rate of occurance is "Sometimes" and the
    * number cleared is 5.
    */
   BLOCK_SMASH_P_P {
      
      @Override
      protected void doSpecial(ActivateComboEffect comboEffect, SimulationTask task) {
         if (canActivate(comboEffect, task)) {
            List<Integer> matches = task.findMatches(36, false, (r, c, s) -> s.getEffect().equals(METAL));
            if (!matches.isEmpty()) {
               double odds = getOdds(task, comboEffect.getNumBlocks());
               if (matches.size() / 2 > 5 || odds < 1.0) {
                  task.setIsRandom();
               }
               if (odds >= Math.random()) {
                  List<Integer> randoms = getUniqueRandoms(0, matches.size() / 2, 5);
                  List<Integer> toErase = new ArrayList<Integer>();
                  for (Integer i : randoms) {
                     int row = matches.get(i * 2);
                     int col = matches.get(i * 2 + 1);
                     toErase.addAll(Arrays.asList(row, col));
                  }
                  if (!toErase.isEmpty()) {
                     eraseBonus(task, toErase, true);
                  }
               }
            }
         }
      }
   },
   /**
    * Removes 3 non-Support Pokemon icon without fail. that means any pokemon that you were forced
    * to have by the stage.
    */
   EJECT_P {
   },
   /**
    * Removes 5 non-Support Pokemon icon without fail. that means any pokemon that you were forced
    * to have by the stage.
    */
   EJECT_P_P {
   },
   /**
    * Removes 3 barrier-type disruption without fail.
    */
   BARRIER_BASH_P {
      
      @Override
      protected void doSpecial(ActivateComboEffect comboEffect, SimulationTask task) {
         if (canActivate(comboEffect, task)) {
            Board board = task.getState().getBoard();
            List<Integer> matches = task.findMatches(36, false, (r, c, s) -> board.isFrozenAt(r, c));
            if (!matches.isEmpty()) {
               double odds = getOdds(task, comboEffect.getNumBlocks());
               if (matches.size() / 2 > 3 || odds < 1.0) {
                  task.setIsRandom();
               }
               if (odds >= Math.random()) {
                  List<Integer> randoms = getUniqueRandoms(0, matches.size() / 2, 3);
                  List<Integer> toUnfreeze = new ArrayList<Integer>();
                  for (Integer i : randoms) {
                     int row = matches.get(i * 2);
                     int col = matches.get(i * 2 + 1);
                     toUnfreeze.addAll(Arrays.asList(row, col));
                  }
                  task.unfreezeAt(toUnfreeze);
               }
            }
         }
      }
      
   },
   /**
    * Removes 5 barrier-type disruption without fail.
    */
   BARRIER_BASH_P_P {
      
      @Override
      protected void doSpecial(ActivateComboEffect comboEffect, SimulationTask task) {
         if (canActivate(comboEffect, task)) {
            Board board = task.getState().getBoard();
            List<Integer> matches = task.findMatches(36, false, (r, c, s) -> board.isFrozenAt(r, c));
            if (!matches.isEmpty()) {
               double odds = getOdds(task, comboEffect.getNumBlocks());
               if (matches.size() / 2 > 5 || odds < 1.0) {
                  task.setIsRandom();
               }
               if (odds >= Math.random()) {
                  List<Integer> randoms = getUniqueRandoms(0, matches.size() / 2, 5);
                  List<Integer> toUnfreeze = new ArrayList<Integer>();
                  for (Integer i : randoms) {
                     int row = matches.get(i * 2);
                     int col = matches.get(i * 2 + 1);
                     toUnfreeze.addAll(Arrays.asList(row, col));
                  }
                  task.unfreezeAt(toUnfreeze);
               }
            }
         }
      }
      
   },
   /**
    * Can replace some disruptions with this Pokemon.
    */
   SWAP_P {
   
   },
   /**
    * Can replace many disruptions with this Pokemon.
    */
   SWAP_P_P {
   
   },
   /**
    * Leaves the foe Paralyzed
    */
   SHOCK_ATTACK {
   
   },
   /**
    * Attacks can occasionally deal greater damage than usual.
    */
   NOSEDIVE {
      
      @Override
      public NumberSpan getScoreMultiplier(ActivateComboEffect comboEffect, SimulationTask task) {
         return getMultiplier(comboEffect, task, 4);
      }
   },
   /**
    * Increases damage done by any flying types in a combo. 2.0x multiplier
    */
   SKY_BLAST {
      
      @Override
      protected void doSpecial(ActivateComboEffect comboEffect, SimulationTask task) {
         ifThenSetSpecial(comboEffect, task, PkmType.FLYING, 1.0);
      }
      
   },
   /**
    * Does mode damage when the opponent has more HP left.
    */
   POISONOUS_MIST {
      
      @Override
      protected void doSpecial(ActivateComboEffect comboEffect, SimulationTask task) {
         ifThenAddScore(comboEffect, task, () -> ((int) (0.1 * task.getState().getCore().getRemainingHealth())));
      }
   },
   /**
    * Does more damage when the opponent has more HP left.
    */
   DOWNPOUR {
      
      @Override
      protected void doSpecial(ActivateComboEffect comboEffect, SimulationTask task) {
         ifThenAddScore(comboEffect, task, () -> ((int) (0.1 * task.getState().getCore().getRemainingHealth())));
      }
   },
   /**
    * Attacks can occasionally deal greater damage than usual.
    */
   SUPER_BOLT {
      
      @Override
      public NumberSpan getScoreMultiplier(ActivateComboEffect comboEffect, SimulationTask task) {
         return getMultiplier(comboEffect, task, 9);
      }
   },
   /**
    * Same as {@link KANGASKHAN} but the mega threshold is slightly lower.
    */
   VENUSAUR {
      
      @Override
      public boolean isPersistent() {
         return true;
      }
      
      @Override
      protected ActivateComboEffect handlePlans(ActivateComboEffect comboEffect, SimulationTask task) {
         return KANGASKHAN.handlePlans(comboEffect, task);
      }
      
      @Override
      public List<Integer> getExtraBlocks(ActivateComboEffect comboEffect, SimulationTask task) {
         return KANGASKHAN.getExtraBlocks(comboEffect, task);
      }
      
      @Override
      public int getEffectRepeatDelay() {
         return KANGASKHAN.getEffectRepeatDelay();
      }
      
      @Override
      public int getValueLimit() {
         return KANGASKHAN.getValueLimit();
      }
      
      @Override
      public NumberSpan getBonusScoreFor(double basicScore, NumberSpan value, double typeModifier) {
         return value.multiplyBy(basicScore * 0.2 * typeModifier);
      }
      
   },
   /**
    * Erases out blocks within two spaces of all pokemon in the chain and uses them to make one
    * powerful attack.
    */
   BLASTOISE {
      
      @Override
      public boolean isPersistent() {
         return true;
      }
      
      @Override
      protected ActivateComboEffect handlePlans(ActivateComboEffect comboEffect, SimulationTask task) {
         return ALTARIA.handlePlans(comboEffect, task);
      }
      
      @Override
      public List<Integer> getExtraBlocks(ActivateComboEffect comboEffect, SimulationTask task) {
         return ALTARIA.getExtraBlocks(comboEffect, task);
      }
      
      @Override
      public int getEffectRepeatDelay() {
         return ALTARIA.getEffectRepeatDelay();
      }
      
      @Override
      public int getValueLimit() {
         return ALTARIA.getValueLimit();
      }
      
      @Override
      public NumberSpan getBonusScoreFor(double basicScore, NumberSpan value, double typeModifier) {
         return value.multiplyBy(basicScore * 0.2 * typeModifier);
      }
      
   },
   /**
    * Same as {@link #BLASTOISE} but for only one space.
    */
   AUDINO {
      
      @Override
      public boolean isPersistent() {
         return true;
      }
      
      @Override
      protected ActivateComboEffect handlePlans(ActivateComboEffect comboEffect, SimulationTask task) {
         if (comboEffect instanceof ActivateMegaComboEffect) {
            return comboEffect;
         } else {
            ActivateMegaComboEffect effect = new ActivateMegaComboEffect(comboEffect);
            addPlansForSurroundingBlocks(effect, task, 1);
            return effect;
         }
      }
      
      @Override
      public List<Integer> getExtraBlocks(ActivateComboEffect comboEffect, SimulationTask task) {
         return ALTARIA.getExtraBlocks(comboEffect, task);
      }
      
      @Override
      public int getEffectRepeatDelay() {
         return ALTARIA.getEffectRepeatDelay();
      }
      
      @Override
      public int getValueLimit() {
         return ALTARIA.getValueLimit();
      }
      
      @Override
      public NumberSpan getBonusScoreFor(double basicScore, NumberSpan value, double typeModifier) {
         return value.multiplyBy(basicScore * 0.2 * typeModifier);
      }
      
   },
   /**
    * A random lightning strike erases a jagged line of blocks. This line is produced by
    * independently tracking two ignorant strikes which begin at the top row, and each tick progress
    * down one and activate them for clearing, in sequence. The one below must be one of the three.
    * They can overlap strikes. The damage bonus is 1/6th of base for each additional block (if
    * strike only clears 11 then this is 11/6 bonus power).
    */
   AMPHAROS {
      
      @Override
      public boolean isPersistent() {
         return true;
      }
      
      @Override
      public int getEffectRepeatDelay() {
         return 10;
      }
      
      @Override
      protected ActivateComboEffect handlePlans(ActivateComboEffect comboEffect, SimulationTask task) {
         if (comboEffect instanceof ActivateMegaComboEffect) {
            return comboEffect;
         } else {
            // No matter what, this will always be inherently random.
            task.setIsRandom();
            ActivateMegaComboEffect effect = new ActivateMegaComboEffect(comboEffect);
            int col1 = 1 + getRandomInt(6); // [1,6]
            int col2 = 1 + getRandomInt(5); // [1,5]
            if (col2 >= col1) {
               col2++; // offset for map of [1,5] around the choice for col1
            }
            // First step
            effect.addPlannedOptions(Arrays.asList(1, col1, 1, col2));
            for (int row = 2; row <= Board.NUM_ROWS; row++) {
               col1 = getNextColumn(col1);
               col2 = getNextColumn(col2);
               effect.addPlannedOptions(Arrays.asList(row, col1, row, col2));
            }
            return effect;
         }
      }
      
      /**
       * @param col
       * @return
       */
      private int getNextColumn(int col) {
         int ret = col;
         if (ret <= 1) {
            ret += getRandomInt(3) == 0 ? 1 : 0;
            // 2/3 chance of staying in the same column, 1/3 chance of changing
         } else if (ret >= 6) {
            ret -= getRandomInt(3) == 0 ? 1 : 0;
            // same as above
         } else {
            ret += getRandomInt(3) - 1;
            // 1/3 chance of moving left, staying the same, or moving right
         }
         return ret;
      }
      
      /**
       * @param comboEffect
       * @param task
       * @return
       */
      @Override
      public List<Integer> getExtraBlocks(ActivateComboEffect comboEffect, SimulationTask task) {
         // Mega Ampharos makes its own selection and doesn't care about the effect or task state.
         return getNextPlan(comboEffect);
      }
      
      @Override
      public int getValueLimit() {
         return 12;
      }
      
      @Override
      public NumberSpan getBonusScoreFor(double basicScore, NumberSpan value, double typeModifier) {
         return value.multiplyBy(basicScore * 0.2 * typeModifier);
      }
      
   },
   /**
    * Same as {@link #BLASTOISE}
    */
   ALTARIA {
      
      @Override
      public boolean isPersistent() {
         return true;
      }
      
      @Override
      protected ActivateComboEffect handlePlans(ActivateComboEffect comboEffect, SimulationTask task) {
         if (comboEffect instanceof ActivateMegaComboEffect) {
            return comboEffect;
         } else {
            ActivateMegaComboEffect effect = new ActivateMegaComboEffect(comboEffect);
            addPlansForSurroundingBlocks(effect, task, 2);
            return effect;
         }
      }
      
      @Override
      public List<Integer> getExtraBlocks(ActivateComboEffect comboEffect, SimulationTask task) {
         return getNextPlan(comboEffect);
      }
      
      @Override
      public int getEffectRepeatDelay() {
         return 10;
      }
      
      @Override
      public int getValueLimit() {
         return 30;
      }
      
      @Override
      public NumberSpan getBonusScoreFor(double basicScore, NumberSpan value, double typeModifier) {
         return value.multiplyBy(basicScore * 0.2 * typeModifier);
      }
      
   },
   /**
    * Erases 5 pokemon in the rows above and beneath the pokemon in the chain. This happens at one
    * tick per block away, progressing outwards. Clearing forces erase.
    */
   LOPUNNY {
      
      @Override
      public boolean isPersistent() {
         return true;
      }
      
      @Override
      protected ActivateComboEffect handlePlans(ActivateComboEffect comboEffect, SimulationTask task) {
         if (comboEffect instanceof ActivateMegaComboEffect) {
            return comboEffect;
         } else {
            ActivateMegaComboEffect effect = new ActivateMegaComboEffect(comboEffect);
            List<Integer> limits = SimulationTask.getLimits(comboEffect.getCoords());
            int minRow = limits.get(0);
            int minCol = limits.get(1);
            int maxRow = limits.get(2);
            int maxCol = limits.get(3);
            for (int rowOffset = 1; rowOffset <= 5; rowOffset++) {
               List<Integer> planStep = new ArrayList<Integer>();
               int topRow = minRow - rowOffset;
               if (topRow >= 1) {
                  List<Integer> topLimits = Arrays.asList(topRow, minCol, topRow, maxCol);
                  List<Integer> topCoords = SimulationTask.getComboForLimits(topLimits);
                  planStep.addAll(topCoords);
               }
               int bottomRow = maxRow + rowOffset;
               if (bottomRow <= Board.NUM_ROWS) {
                  List<Integer> bottomLimits = Arrays.asList(bottomRow, minCol, bottomRow, maxCol);
                  List<Integer> bottomCoords = SimulationTask.getComboForLimits(bottomLimits);
                  planStep.addAll(bottomCoords);
               }
               if (!planStep.isEmpty()) {
                  effect.addPlannedOptions(planStep);
               }
            }
            return effect;
         }
      }
      
      @Override
      public List<Integer> getExtraBlocks(ActivateComboEffect comboEffect, SimulationTask task) {
         return KANGASKHAN.getExtraBlocks(comboEffect, task);
      }
      
      @Override
      public int getEffectRepeatDelay() {
         return KANGASKHAN.getEffectRepeatDelay();
      }
      
      @Override
      public int getValueLimit() {
         return KANGASKHAN.getValueLimit();
      }
      
      @Override
      public NumberSpan getBonusScoreFor(double basicScore, NumberSpan value, double typeModifier) {
         return value.multiplyBy(basicScore * 0.2 * typeModifier);
      }
      
   },
   /**
    * Same as {@link #LOPUNNY} but for left/right, not above/beneath.
    */
   KANGASKHAN {
      
      @Override
      public boolean isPersistent() {
         return true;
      }
      
      @Override
      protected ActivateComboEffect handlePlans(ActivateComboEffect comboEffect, SimulationTask task) {
         if (comboEffect instanceof ActivateMegaComboEffect) {
            return comboEffect;
         } else {
            ActivateMegaComboEffect effect = new ActivateMegaComboEffect(comboEffect);
            List<Integer> limits = SimulationTask.getLimits(comboEffect.getCoords());
            int minRow = limits.get(0);
            int minCol = limits.get(1);
            int maxRow = limits.get(2);
            int maxCol = limits.get(3);
            for (int colOffset = 1; colOffset <= 5; colOffset++) {
               List<Integer> planStep = new ArrayList<Integer>();
               int leftCol = minCol - colOffset;
               if (leftCol >= 1) {
                  List<Integer> leftLimits = Arrays.asList(minRow, leftCol, maxRow, leftCol);
                  List<Integer> leftCoords = SimulationTask.getComboForLimits(leftLimits);
                  planStep.addAll(leftCoords);
               }
               int rightCol = maxCol + colOffset;
               if (rightCol <= Board.NUM_COLS) {
                  List<Integer> rightLimits = Arrays.asList(minRow, rightCol, maxRow, rightCol);
                  List<Integer> rightCoords = SimulationTask.getComboForLimits(rightLimits);
                  planStep.addAll(rightCoords);
               }
               if (!planStep.isEmpty()) {
                  effect.addPlannedOptions(planStep);
               }
            }
            return effect;
         }
      }
      
      @Override
      public List<Integer> getExtraBlocks(ActivateComboEffect comboEffect, SimulationTask task) {
         return getNextPlan(comboEffect);
      }
      
      @Override
      public int getEffectRepeatDelay() {
         return 8;
      }
      
      @Override
      public int getValueLimit() {
         return 30;
      }
      
      @Override
      public NumberSpan getBonusScoreFor(double basicScore, NumberSpan value, double typeModifier) {
         return value.multiplyBy(basicScore * 0.2 * typeModifier);
      }
      
   },
   /**
    * Erases all Pokemon in a O-shaped pattern. There are exactly 12 blocks in this pattern. Score
    * is increased by #blocks cleared / 6 of base attack power, includes modifiers.
    */
   SABLEYE {
      
      @Override
      public boolean isPersistent() {
         return true;
      }
      
      @Override
      protected ActivateComboEffect handlePlans(ActivateComboEffect comboEffect, SimulationTask task) {
         if (comboEffect instanceof ActivateMegaComboEffect) {
            return comboEffect;
         } else {
            ActivateMegaComboEffect effect = new ActivateMegaComboEffect(comboEffect);
            // The pattern plan
            effect.addPlannedOptions(Arrays.asList(1, 3, 1, 4));
            effect.addPlannedOptions(Arrays.asList(2, 5, 3, 6));
            effect.addPlannedOptions(Arrays.asList(4, 6, 5, 5));
            effect.addPlannedOptions(Arrays.asList(6, 4, 6, 3));
            effect.addPlannedOptions(Arrays.asList(5, 2, 4, 1));
            effect.addPlannedOptions(Arrays.asList(3, 1, 2, 2));
            return effect;
         }
      }
      
      @Override
      public List<Integer> getExtraBlocks(ActivateComboEffect comboEffect, SimulationTask task) {
         return getNextPlan(comboEffect);
      }
      
      @Override
      public int getEffectRepeatDelay() {
         return 10;
      }
      
      @Override
      public int getValueLimit() {
         return 12;
      }
      
      @Override
      public NumberSpan getBonusScoreFor(double basicScore, NumberSpan value, double typeModifier) {
         return value.multiplyBy(basicScore * 0.2 * typeModifier);
      }
      
   },
   /**
    * Same as {@link #LOPUNNY}
    */
   LUCARIO {
      
      @Override
      public boolean isPersistent() {
         return true;
      }
      
      @Override
      protected ActivateComboEffect handlePlans(ActivateComboEffect comboEffect, SimulationTask task) {
         return LOPUNNY.handlePlans(comboEffect, task);
      }
      
      @Override
      public List<Integer> getExtraBlocks(ActivateComboEffect comboEffect, SimulationTask task) {
         return LOPUNNY.getExtraBlocks(comboEffect, task);
      }
      
      @Override
      public int getEffectRepeatDelay() {
         return LOPUNNY.getEffectRepeatDelay();
      }
      
      @Override
      public int getValueLimit() {
         return LOPUNNY.getValueLimit();
      }
      
      @Override
      public NumberSpan getBonusScoreFor(double basicScore, NumberSpan value, double typeModifier) {
         return value.multiplyBy(basicScore * 0.2 * typeModifier);
      }
      
   },
   /**
    * Add one more mega slowbro above the match. This is only available for matching up on the next
    * tick.
    */
   SLOWBRO {
      
      @Override
      public boolean isPersistent() {
         return true;
      }
      
      @Override
      protected ActivateComboEffect handlePlans(ActivateComboEffect comboEffect, SimulationTask task) {
         if (comboEffect instanceof ActivateMegaComboEffect) {
            return comboEffect;
         } else {
            ActivateMegaComboEffect effect = new ActivateMegaComboEffect(comboEffect);
            Board b = task.getState().getBoard();
            Species effectSpecies = task.getEffectSpecies(effect.getCoords());
            // Find the start points for where this effect needs to add
            List<Integer> startPoints = new ArrayList<Integer>();
            // it works, don't mess with it without extensive tests.
            for (int col = 1; col <= Board.NUM_COLS; col++) {
               for (int row = 2; row <= Board.NUM_ROWS; row++) {
                  Species curSpecies = b.getSpeciesAt(row, col);
                  Species aboveSpecies = b.getSpeciesAt(row - 1, col);
                  if (!task.isActive(row, col) && !task.isActive(row - 1, col) && curSpecies.equals(effectSpecies)
                        && !aboveSpecies.equals(effectSpecies)) {
                     startPoints.addAll(Arrays.asList(row - 1, col));
                  }
               }
            }
            // Fill out each start point into a plan
            for (int i = 0; i * 2 + 1 < startPoints.size(); i++) {
               int row = startPoints.get(i * 2);
               int col = startPoints.get(i * 2 + 1);
               if (row < 1 || row > Board.NUM_ROWS || col < 1) {
                  continue;
               }
               List<Integer> plan = new ArrayList<Integer>();
               while (row >= 1 && !task.isActive(row, col) && !b.getSpeciesAt(row, col).equals(effectSpecies)) {
                  plan.addAll(Arrays.asList(row, col));
                  row -= 1;
               }
               if (!plan.isEmpty()) {
                  effect.addPlannedOptions(plan);
               }
            }
            return effect;
         }
      }
      
      @Override
      public List<Integer> getExtraBlocks(ActivateComboEffect comboEffect, SimulationTask task) {
         List<Integer> toReplace = null;
         Species effectSpecies = task.getEffectSpecies(comboEffect.getCoords());
         Board b = task.getState().getBoard();
         if (comboEffect instanceof ActivateMegaComboEffect) {
            ActivateMegaComboEffect effect = (ActivateMegaComboEffect) comboEffect;
            List<Integer> plan;
            while (toReplace == null && effect.hasPlan()) {
               plan = effect.getNextPlan();
               if (plan.size() < 2) {
                  // empty plan = no point in trying it.
                  continue;
               }
               int row = plan.get(0);
               int col = plan.get(1);
               if (task.isActive(row + 1, col)) {
                  // If the spot below our plan is active, we can't use it.
                  continue;
               }
               for (int i = 0; toReplace == null && i * 2 + 1 < plan.size(); i++) {
                  row = plan.get(i * 2);
                  col = plan.get(i * 2 + 1);
                  Species curSpecies = b.getSpeciesAt(row, col);
                  if (task.isActive(row, col)) {
                     // if any of the spots are active we can't proceed further.
                     break;
                  }
                  if (!effectSpecies.equals(curSpecies)) {
                     // only an inactive non-species, for which the nearest same species
                     // below is inactive, is an acceptable selection
                     toReplace = Arrays.asList(row, col);
                  }
               }
            }
         }
         return toReplace;
      }
      
      @Override
      public void handleExtraBlocks(ActivateComboEffect comboEffect, SimulationTask task, List<Integer> extraBlocks) {
         Species toReplaceWith = task.getEffectSpecies(comboEffect.getCoords());
         Board b = task.getState().getBoard();
         for (int i = 0; i * 2 + 1 < extraBlocks.size(); i++) {
            int row = extraBlocks.get(i * 2);
            int col = extraBlocks.get(i * 2 + 1);
            if (!task.isActive(row, col)) {
               if (b.getSpeciesAt(row, col).getEffect().isDisruption()) {
                  task.getState().addDisruptionCleared(1);
               }
               b.setSpeciesAt(row, col, toReplaceWith);
               Collection<ActivateComboEffect> claimsFor = new ArrayList<ActivateComboEffect>(
                     task.getClaimsFor(row, col));
               for (ActivateComboEffect claim : claimsFor) {
                  task.removeClaim(claim);
               }
            }
         }
      }
      
      @Override
      public int getValueLimit() {
         return 18;
      }
      
   },
   /**
    * Add one more mega sharpedo above the match. This is only available for matching up on the next
    * tick.
    */
   SHARPEDO {
      
      @Override
      public boolean isPersistent() {
         return true;
      }
      
      @Override
      protected ActivateComboEffect handlePlans(ActivateComboEffect comboEffect, SimulationTask task) {
         return SLOWBRO.handlePlans(comboEffect, task);
      }
      
      @Override
      public List<Integer> getExtraBlocks(ActivateComboEffect comboEffect, SimulationTask task) {
         return SLOWBRO.getExtraBlocks(comboEffect, task);
      }
      
      @Override
      public void handleExtraBlocks(ActivateComboEffect comboEffect, SimulationTask task, List<Integer> extraBlocks) {
         SLOWBRO.handleExtraBlocks(comboEffect, task, extraBlocks);
      }
      
      @Override
      public int getValueLimit() {
         return 18;
      }
      
   },
   /**
    * Add one more mega heracross above the match. This is only available for matching up on the
    * next tick.
    */
   HERACROSS {
      
      @Override
      public boolean isPersistent() {
         return true;
      }
      
      @Override
      protected ActivateComboEffect handlePlans(ActivateComboEffect comboEffect, SimulationTask task) {
         if (comboEffect instanceof ActivateMegaComboEffect) {
            return comboEffect;
         } else {
            ActivateMegaComboEffect effect = new ActivateMegaComboEffect(comboEffect);
            Board b = task.getState().getBoard();
            Species effectSpecies = task.getEffectSpecies(effect.getCoords());
            // Find the start points for where this effect needs to add
            List<Integer> startPoints = new ArrayList<Integer>();
            // it works, don't mess with it without extensive tests.
            for (int row = 1; row <= Board.NUM_ROWS; row++) {
               for (int col = 2; col <= Board.NUM_COLS; col++) {
                  Species curSpecies = b.getSpeciesAt(row, col);
                  Species leftSpecies = b.getSpeciesAt(row, col - 1);
                  if (!task.isActive(row, col) && !task.isActive(row, col - 1) && curSpecies.equals(effectSpecies)
                        && !leftSpecies.equals(effectSpecies)) {
                     startPoints.addAll(Arrays.asList(row, col - 1));
                  }
               }
            }
            // Fill out each start point into a plan
            for (int i = 0; i * 2 + 1 < startPoints.size(); i++) {
               int row = startPoints.get(i * 2);
               int col = startPoints.get(i * 2 + 1);
               if (row < 1 || row > Board.NUM_ROWS || col < 1) {
                  continue;
               }
               List<Integer> plan = new ArrayList<Integer>();
               while (col >= 1 && !task.isActive(row, col) && !b.getSpeciesAt(row, col).equals(effectSpecies)) {
                  plan.addAll(Arrays.asList(row, col));
                  col -= 1;
               }
               if (!plan.isEmpty()) {
                  effect.addPlannedOptions(plan);
               }
            }
            return effect;
         }
      }
      
      @Override
      public List<Integer> getExtraBlocks(ActivateComboEffect comboEffect, SimulationTask task) {
         List<Integer> toReplace = null;
         Species effectSpecies = task.getEffectSpecies(comboEffect.getCoords());
         Board b = task.getState().getBoard();
         if (comboEffect instanceof ActivateMegaComboEffect) {
            ActivateMegaComboEffect effect = (ActivateMegaComboEffect) comboEffect;
            List<Integer> plan;
            while (toReplace == null && effect.hasPlan()) {
               plan = effect.getNextPlan();
               if (plan.size() < 2) {
                  // empty plan = no point in trying it.
                  continue;
               }
               int row = plan.get(0);
               int col = plan.get(1);
               if (task.isActive(row, col + 1)) {
                  // If the spot to the right of our plan is active, we can't use it.
                  continue;
               }
               for (int i = 0; toReplace == null && i * 2 + 1 < plan.size(); i++) {
                  row = plan.get(i * 2);
                  col = plan.get(i * 2 + 1);
                  Species curSpecies = b.getSpeciesAt(row, col);
                  if (task.isActive(row, col)) {
                     // if any of the spots are active we can't proceed further.
                     // IFF we haven't yet found the replacement spot.
                     break;
                  }
                  if (!effectSpecies.equals(curSpecies)) {
                     // only an inactive non-species, for which the nearest same species
                     // right is inactive, is an acceptable selection
                     toReplace = Arrays.asList(row, col);
                  }
               }
            }
         }
         return toReplace;
      }
      
      @Override
      public void handleExtraBlocks(ActivateComboEffect comboEffect, SimulationTask task, List<Integer> extraBlocks) {
         Species toReplaceWith = task.getEffectSpecies(comboEffect.getCoords());
         Board b = task.getState().getBoard();
         for (int i = 0; i * 2 + 1 < extraBlocks.size(); i++) {
            int row = extraBlocks.get(i * 2);
            int col = extraBlocks.get(i * 2 + 1);
            if (!task.isActive(row, col)) {
               if (b.getSpeciesAt(row, col).getEffect().isDisruption()) {
                  task.getState().addDisruptionCleared(1);
               }
               b.setSpeciesAt(row, col, toReplaceWith);
               Collection<ActivateComboEffect> claimsFor = new ArrayList<ActivateComboEffect>(
                     task.getClaimsFor(row, col));
               for (ActivateComboEffect claim : claimsFor) {
                  task.removeClaim(claim);
               }
            }
         }
      }
      
      @Override
      public int getValueLimit() {
         return 18;
      }
      
   },
   /**
    * Replace 3 random fire types with Blaziken. Only one species is selected at a time.
    */
   BLAZIKEN {
      
      @Override
      public boolean isPersistent() {
         return true;
      }
      
      @Override
      protected ActivateComboEffect handlePlans(ActivateComboEffect comboEffect, SimulationTask task) {
         if (comboEffect instanceof ActivateMegaComboEffect) {
            return comboEffect;
         } else {
            ActivateMegaComboEffect effect = new ActivateMegaComboEffect(comboEffect);
            Species dontMatch = task.getEffectSpecies(effect.getCoords());
            Species sel = getRandomSpeciesOfTypeFrom(dontMatch.getType(), task.getState().getBoard(), dontMatch, task);
            List<Integer> coords = task.findMatches(33, false, (r, c, s) -> s.equals(sel));
            if (coords.size() / 2 > 3) {
               task.setIsRandom();
            }
            List<Integer> indexOrder = getUniqueRandoms(0, coords.size() / 2, 3);
            // 3 random selections at most, of a single type-matched species.
            List<Integer> plan = new ArrayList<Integer>(coords.size());
            for (int i = 0; i < indexOrder.size(); i++) {
               int index = indexOrder.get(i);
               plan.add(coords.get(index * 2));
               plan.add(coords.get(index * 2 + 1));
            }
            effect.addPlannedOptions(plan);
            return effect;
         }
      }
      
      @Override
      public List<Integer> getExtraBlocks(ActivateComboEffect comboEffect, SimulationTask task) {
         List<Integer> toReplace = null;
         if (comboEffect instanceof ActivateMegaComboEffect) {
            ActivateMegaComboEffect effect = (ActivateMegaComboEffect) comboEffect;
            List<Integer> plan = effect.getNextPlan();
            if (plan != null) {
               while (plan.size() >= 2 && toReplace == null) {
                  int row = plan.remove(0);
                  int col = plan.remove(0);
                  if (!task.isActive(row, col)) {
                     toReplace = Arrays.asList(row, col);
                  }
               }
               if (plan.size() >= 2) {
                  // if the plan still has stuff to use, then re-queue it.
                  effect.addPlannedOptions(plan);
               }
            }
         }
         return toReplace;
      }
      
      @Override
      public void handleExtraBlocks(ActivateComboEffect comboEffect, SimulationTask task, List<Integer> extraBlocks) {
         Species toReplaceWith = task.getEffectSpecies(comboEffect.getCoords());
         Board b = task.getState().getBoard();
         for (int i = 0; i * 2 + 1 < extraBlocks.size(); i++) {
            int row = extraBlocks.get(i * 2);
            int col = extraBlocks.get(i * 2 + 1);
            if (!task.isActive(row, col)) {
               b.setSpeciesAt(row, col, toReplaceWith);
               Collection<ActivateComboEffect> claimsFor = new ArrayList<ActivateComboEffect>(
                     task.getClaimsFor(row, col));
               for (ActivateComboEffect claim : claimsFor) {
                  task.removeClaim(claim);
               }
            }
         }
      }
      
      @Override
      public int getValueLimit() {
         return 33;
      }
      
   },
   /**
    * Erases diagonal Pokemon from upper right to lower left corner. Blocks 3,5,7 in the 1-9 blocks
    * for each quadrant is the exact pattern. Same scoring as other megas that clear blocks.
    */
   MAWILE {
      
      @Override
      public boolean isPersistent() {
         return true;
      }
      
      @Override
      protected ActivateComboEffect handlePlans(ActivateComboEffect comboEffect, SimulationTask task) {
         if (comboEffect instanceof ActivateMegaComboEffect) {
            return comboEffect;
         } else {
            ActivateMegaComboEffect effect = new ActivateMegaComboEffect(comboEffect);
            // CENTER, Top Left, Bottom Right
            effect.addPlannedOptions(Arrays.asList(1, 6));
            effect.addPlannedOptions(Arrays.asList(2, 5, 1, 3, 4, 6));
            effect.addPlannedOptions(Arrays.asList(3, 4, 2, 2, 5, 5));
            effect.addPlannedOptions(Arrays.asList(4, 3, 3, 1, 6, 4));
            effect.addPlannedOptions(Arrays.asList(5, 2));
            effect.addPlannedOptions(Arrays.asList(6, 1));
            return effect;
         }
      }
      
      @Override
      public List<Integer> getExtraBlocks(ActivateComboEffect comboEffect, SimulationTask task) {
         return SABLEYE.getExtraBlocks(comboEffect, task);
      }
      
      @Override
      public int getEffectRepeatDelay() {
         return SABLEYE.getEffectRepeatDelay();
      }
      
      @Override
      public int getValueLimit() {
         return SABLEYE.getValueLimit();
      }
      
      @Override
      public NumberSpan getBonusScoreFor(double basicScore, NumberSpan value, double typeModifier) {
         return value.multiplyBy(basicScore * 0.2 * typeModifier);
      }
      
   },
   /**
    * Erases diagonal Pokemon from upper left to lower right corner. Blocks 1,5,9 in the 1-9 blocks
    * for each quadrant is the exact pattern. Same scoring as other megas that clear blocks.
    */
   GARCHOMP {
      
      @Override
      public boolean isPersistent() {
         return true;
      }
      
      @Override
      protected ActivateComboEffect handlePlans(ActivateComboEffect comboEffect, SimulationTask task) {
         if (comboEffect instanceof ActivateMegaComboEffect) {
            return comboEffect;
         } else {
            ActivateMegaComboEffect effect = new ActivateMegaComboEffect(comboEffect);
            // CENTER, Top Left, Bottom Right
            effect.addPlannedOptions(Arrays.asList(1, 1));
            effect.addPlannedOptions(Arrays.asList(2, 2, 1, 4, 4, 1));
            effect.addPlannedOptions(Arrays.asList(3, 3, 2, 5, 5, 2));
            effect.addPlannedOptions(Arrays.asList(4, 4, 3, 6, 6, 3));
            effect.addPlannedOptions(Arrays.asList(5, 5));
            effect.addPlannedOptions(Arrays.asList(6, 6));
            return effect;
         }
      }
      
      @Override
      public List<Integer> getExtraBlocks(ActivateComboEffect comboEffect, SimulationTask task) {
         return SABLEYE.getExtraBlocks(comboEffect, task);
      }
      
      @Override
      public int getEffectRepeatDelay() {
         return SABLEYE.getEffectRepeatDelay();
      }
      
      @Override
      public int getValueLimit() {
         return SABLEYE.getValueLimit();
      }
      
      @Override
      public NumberSpan getBonusScoreFor(double basicScore, NumberSpan value, double typeModifier) {
         return value.multiplyBy(basicScore * 0.2 * typeModifier);
      }
      
   },
   /**
    * Each 1 tick when this tries to begin its own clearing (its fully activated) this will erase
    * one additional mega gengar, from the top left in normal reading order. Score increases by 1/6
    * of base for each additional block.
    */
   GENGAR {
      
      @Override
      public boolean isPersistent() {
         return true;
      }
      
      /**
       * @param comboEffect
       * @param task
       * @return
       */
      @Override
      public List<Integer> getExtraBlocks(ActivateComboEffect comboEffect, SimulationTask task) {
         Species effectSpecies = task.getEffectSpecies(comboEffect.getCoords());
         List<Integer> toErase = task.findMatches(1, false, (r, c, s) -> s.equals(effectSpecies));
         return toErase.isEmpty() ? null : toErase;
      }
      
      @Override
      public int getValueLimit() {
         return 36;
      }
      
      @Override
      public NumberSpan getBonusScoreFor(double basicScore, NumberSpan value, double typeModifier) {
         return value.multiplyBy(basicScore * 0.2 * typeModifier);
      }
      
   },
   /**
    * Erases all pookemon in a V shapped pattern, same rules as other megas for scoring. Pattern is
    * simulataneous.
    */
   GLALIE {
      
      @Override
      public boolean isPersistent() {
         return true;
      }
      
      @Override
      protected ActivateComboEffect handlePlans(ActivateComboEffect comboEffect, SimulationTask task) {
         if (comboEffect instanceof ActivateMegaComboEffect) {
            return comboEffect;
         } else {
            ActivateMegaComboEffect effect = new ActivateMegaComboEffect(comboEffect);
            for (int step = 1; step <= 6; step++) {
               int offset = (step - 1) / 2;
               int left = 1 + offset;
               int right = 6 - offset;
               effect.addPlannedOptions(Arrays.asList(step, left, step, right));
            }
            return effect;
         }
      }
      
      @Override
      public List<Integer> getExtraBlocks(ActivateComboEffect comboEffect, SimulationTask task) {
         return SABLEYE.getExtraBlocks(comboEffect, task);
      }
      
      @Override
      public int getEffectRepeatDelay() {
         return SABLEYE.getEffectRepeatDelay();
      }
      
      @Override
      public int getValueLimit() {
         return SABLEYE.getValueLimit();
      }
      
      @Override
      public NumberSpan getBonusScoreFor(double basicScore, NumberSpan value, double typeModifier) {
         return value.multiplyBy(basicScore * 0.2 * typeModifier);
      }
      
   },
   /**
    * Erases rocks and blocks (max 10), increasing by 1/6 for each additional block, same chosen
    * order and timing as for Gengar.
    */
   AERODACTYL {
      
      @Override
      public boolean isPersistent() {
         return true;
      }
      
      /**
       * @param comboEffect
       * @param task
       * @return
       */
      @Override
      public List<Integer> getExtraBlocks(ActivateComboEffect comboEffect, SimulationTask task) {
         List<Integer> toErase = task.findMatches(1, false,
               (r, c, s) -> s.getEffect().equals(WOOD) || s.getEffect().equals(METAL));
         return toErase.isEmpty() ? null : toErase;
      }
      
      @Override
      public int getValueLimit() {
         return 10;
      }
      
      @Override
      public NumberSpan getBonusScoreFor(double basicScore, NumberSpan value, double typeModifier) {
         return value.multiplyBy(50 * typeModifier);
      }
      
   },
   /**
    * Clears a Pokemon with the same type as Mega Mewtwo Y (max 10) but NOT itself.
    */
   MEWTWO {
      @Override
      public boolean isPersistent() {
         return true;
      }
      
      @Override
      protected ActivateComboEffect handlePlans(ActivateComboEffect comboEffect, SimulationTask task) {
         Species dontMatch = task.getEffectSpecies(comboEffect.getCoords());
         ActivateMegaComboEffect effect;
         Species toMatch;
         if (comboEffect instanceof ActivateMegaComboEffect) {
            effect = (ActivateMegaComboEffect) comboEffect;
         } else {
            effect = new ActivateMegaComboEffect(comboEffect);
            toMatch = getRandomSpeciesOfTypeFrom(dontMatch.getType(), task.getState().getBoard(), dontMatch, task);
            effect.setTargetSpecies(toMatch);
         }
         return effect;
      }
      
      /**
       * @param comboEffect
       * @param task
       * @return
       */
      @Override
      public List<Integer> getExtraBlocks(ActivateComboEffect comboEffect, SimulationTask task) {
         
         Species toMatch;
         if (comboEffect instanceof ActivateMegaComboEffect) {
            toMatch = ((ActivateMegaComboEffect) comboEffect).getTargetSpecies();
         } else {
            Species dontMatch = task.getEffectSpecies(comboEffect.getCoords());
            toMatch = getRandomSpeciesOfTypeFrom(dontMatch.getType(), task.getState().getBoard(), dontMatch, task);
         }
         
         List<Integer> toErase = Collections.emptyList();
         if (toMatch != null) {
            toErase = task.findMatches(1, false, (r, c, s) -> s.equals(toMatch));
         }
         return toErase.isEmpty() ? null : toErase;
      }
      
      @Override
      public int getValueLimit() {
         return 10;
      }
      
      @Override
      public NumberSpan getBonusScoreFor(double basicScore, NumberSpan value, double typeModifier) {
         return value.multiplyBy(basicScore * 0.2 * typeModifier);
      }
      
   },
   /**
    * Same as {@link #MEWTWO}.
    */
   BANETTE {
      
      @Override
      public boolean isPersistent() {
         return true;
      }
      
      @Override
      protected ActivateComboEffect handlePlans(ActivateComboEffect comboEffect, SimulationTask task) {
         return MEWTWO.handlePlans(comboEffect, task);
      }
      
      /**
       * @param comboEffect
       * @param task
       * @return
       */
      @Override
      public List<Integer> getExtraBlocks(ActivateComboEffect comboEffect, SimulationTask task) {
         return MEWTWO.getExtraBlocks(comboEffect, task);
      }
      
      @Override
      public int getValueLimit() {
         return MEWTWO.getValueLimit();
      }
      
      @Override
      public NumberSpan getBonusScoreFor(double basicScore, NumberSpan value, double typeModifier) {
         return MEWTWO.getBonusScoreFor(basicScore, value, typeModifier);
      }
      
   },
   /**
    * A random lightning strike erases a jagged line of blocks. This line is produced by
    * independently tracking two ignorant strikes which begin at the top row, and each tick progress
    * down one and activate them for clearing, in sequence. The one below must be one of the three.
    * They can overlap strikes. The damage bonus is 1/6th of base for each additional block (if
    * strike only clears 11 then this is 11/6 bonus power).
    */
   MANECTRIC {
      
      @Override
      public boolean isPersistent() {
         return true;
      }
      
      @Override
      public int getEffectRepeatDelay() {
         return 10;
      }
      
      @Override
      protected ActivateComboEffect handlePlans(ActivateComboEffect comboEffect, SimulationTask task) {
         if (comboEffect instanceof ActivateMegaComboEffect) {
            return comboEffect;
         } else {
            task.setIsRandom();
            ActivateMegaComboEffect effect = new ActivateMegaComboEffect(comboEffect);
            int col1 = 1 + getRandomInt(3); // [1,3]
            int col2 = 4 + getRandomInt(3); // [4,6]
            effect.addPlannedOptions(Arrays.asList(1, col1, 1, col2));
            if (col2 >= 6) {
               col2--;
            }
            effect.addPlannedOptions(Arrays.asList(2, col1 + 1, 2, col2 + 1));
            for (int row = 3; row <= Board.NUM_ROWS; row += 2) {
               effect.addPlannedOptions(Arrays.asList(row, col1, row, col2));
               effect.addPlannedOptions(Arrays.asList(row + 1, col1 + 1, row + 1, col2 + 1));
            }
            return effect;
         }
      }
      
      /**
       * @param comboEffect
       * @param task
       * @return
       */
      @Override
      public List<Integer> getExtraBlocks(ActivateComboEffect comboEffect, SimulationTask task) {
         // Mega Ampharos is a honey badger. It makes its own selection and doesn't care
         // what the effect or task are. Its a honey badger so it doesn't give a %*(@.
         return getNextPlan(comboEffect);
      }
      
      @Override
      public int getValueLimit() {
         return 12;
      }
      
      @Override
      public NumberSpan getBonusScoreFor(double basicScore, NumberSpan value, double typeModifier) {
         return value.multiplyBy(basicScore * 0.2 * typeModifier);
      }
      
   },
   /**
    * Charizard Y: <br>
    * Erases tiles in a Y shape.
    */
   CHARIZARD_Y {
      
      @Override
      public boolean isPersistent() {
         return true;
      }
      
      @Override
      protected ActivateComboEffect handlePlans(ActivateComboEffect comboEffect, SimulationTask task) {
         if (comboEffect instanceof ActivateMegaComboEffect) {
            return comboEffect;
         } else {
            ActivateMegaComboEffect effect = new ActivateMegaComboEffect(comboEffect);
            effect.addPlannedOptions(Arrays.asList(1, 1, 1, 6));
            effect.addPlannedOptions(Arrays.asList(2, 2, 2, 5));
            effect.addPlannedOptions(Arrays.asList(3, 3, 3, 4));
            effect.addPlannedOptions(Arrays.asList(4, 3, 4, 4));
            effect.addPlannedOptions(Arrays.asList(5, 3, 5, 4));
            effect.addPlannedOptions(Arrays.asList(6, 3, 6, 4));
            return effect;
         }
      }
      
      @Override
      public List<Integer> getExtraBlocks(ActivateComboEffect comboEffect, SimulationTask task) {
         return SABLEYE.getExtraBlocks(comboEffect, task);
      }
      
      @Override
      public int getEffectRepeatDelay() {
         return SABLEYE.getEffectRepeatDelay();
      }
      
      @Override
      public int getValueLimit() {
         return SABLEYE.getValueLimit();
      }
      
      @Override
      public NumberSpan getBonusScoreFor(double basicScore, NumberSpan value, double typeModifier) {
         return value.multiplyBy(basicScore * 0.2 * typeModifier);
      }
      
   },
   /**
    * A random lightning strike erases a jagged line of blocks horizontally. This line is produced
    * by independently tracking two ignorant strikes which begin at the left column, and each tick
    * progress right one and activate them for clearing, in sequence. The one to the right must be
    * one of the three. They can overlap strikes. The damage bonus is 1/6th of base for each
    * additional block (if strike only clears 11 then this is 11/6 bonus power).
    */
   LATIAS {
      
      @Override
      public boolean isPersistent() {
         return true;
      }
      
      @Override
      public int getEffectRepeatDelay() {
         return 10;
      }
      
      @Override
      protected ActivateComboEffect handlePlans(ActivateComboEffect comboEffect, SimulationTask task) {
         if (comboEffect instanceof ActivateMegaComboEffect) {
            return comboEffect;
         } else {
            // No matter what, this will always be inherently random.
            task.setIsRandom();
            ActivateMegaComboEffect effect = new ActivateMegaComboEffect(comboEffect);
            int row1 = 1 + getRandomInt(6); // [1,6]
            int row2 = 1 + getRandomInt(5); // [1,5]
            if (row2 >= row1) {
               row2++; // offset for map of [1,5] around the choice for col1
            }
            // First step
            effect.addPlannedOptions(Arrays.asList(row1, 1, row2, 1));
            for (int col = 2; col <= Board.NUM_COLS; col++) {
               row1 = getNextRow(row1);
               row2 = getNextRow(row2);
               effect.addPlannedOptions(Arrays.asList(row1, col, row2, col));
            }
            return effect;
         }
      }
      
      /**
       * @param row
       * @return
       */
      private int getNextRow(int row) {
         int ret = row;
         if (ret <= 1) {
            ret += getRandomInt(3) == 0 ? 1 : 0;
            // 2/3 chance of staying in the same row, 1/3 chance of changing
         } else if (ret >= 6) {
            ret -= getRandomInt(3) == 0 ? 1 : 0;
            // same as above
         } else {
            ret += getRandomInt(3) - 1;
            // 1/3 chance of moving up, staying the same, or moving down
         }
         return ret;
      }
      
      /**
       * @param comboEffect
       * @param task
       * @return
       */
      @Override
      public List<Integer> getExtraBlocks(ActivateComboEffect comboEffect, SimulationTask task) {
         return getNextPlan(comboEffect);
      }
      
      @Override
      public int getValueLimit() {
         return 12;
      }
      
      @Override
      public NumberSpan getBonusScoreFor(double basicScore, NumberSpan value, double typeModifier) {
         return value.multiplyBy(basicScore * 0.2 * typeModifier);
      }
      
   },
   /**
    * No effect whatsoever, clears itself as a normal block without any additional effects.
    */
   NONE {
      // must not implement anything special - this is the default effect.
   },
   /**
    * Coin block, has a bonus GOLD production by 100,300,500 for 3x,4x,5x matches.
    */
   COIN {
      @Override
      public boolean isAutoGenerated() {
         return false;
      }
      
      @Override
      public void handleEffectFinished(ActivateComboEffect effect, SimulationTask task) {
         int n = effect.getNumBlocks();
         int coins = 200 * n - 500;
         task.getState().addGold(coins);
      }
      
      @Override
      public boolean canLevel() {
         return false;
      }
   },
   /**
    * The breakable-rock disruption, aka Wood. This does not combo, and does not give any score
    * bonus. This will shatter if a non-special activation occurs within the 4 up/down/left/right
    * neighbors of itself. This can be frozen, and if a neighbor activates nearby this will shatter
    * to air regardless of the freeze.
    */
   WOOD {
      @Override
      public boolean isPickable() {
         return false;
      }
      
      @Override
      public boolean isDroppable() {
         return false;
      }
      
      @Override
      public boolean isAutoGenerated() {
         return false;
      }
      
      @Override
      public int getErasureDelay() {
         return getDefaultErasureDelay() + 3;
      }
      
      @Override
      public Species getUnfreezeReplacement(Species s) {
         return Species.AIR;
      }
      
      @Override
      public void handleCombo(ActivateComboEffect comboEffect, SimulationTask task) {
         // Do nothing.
      }
      
      @Override
      public boolean canLevel() {
         return false;
      }
   },
   /**
    * The unbreakable-rock disruption, aka Metal. This does not combo, and does nto give any score
    * bonus. This will ignore all activity, but can still freeze. When frozen this cannot be removed
    * without an effect.
    */
   METAL {
      @Override
      public boolean isPickable() {
         return false;
      }
      
      @Override
      public boolean isDroppable() {
         return false;
      }
      
      @Override
      public boolean isAutoGenerated() {
         return false;
      }
      
      @Override
      public void handleCombo(ActivateComboEffect comboEffect, SimulationTask task) {
         // Do nothing.
      }
      
      @Override
      public boolean canLevel() {
         return false;
      }
   },
   /**
    * The air block, aka "nothing". This does not combo, should not be modeled for physics, and is
    * merely a receptor space where things can fall into and drop onto. Cannot be frozen, and does
    * not affect score in any way.
    */
   AIR {
      @Override
      public boolean isPickable() {
         return false;
      }
      
      @Override
      public boolean isAutoGenerated() {
         return false;
      }
      
      @Override
      public boolean canLevel() {
         return false;
      }
   };
   
   /**
    * Gets an effect which matches the given name. If there is none, then {@link #NONE} is returned.
    * 
    * @param effect
    * @return
    */
   public static Effect getEffect(String effect) {
      for (Effect e : Effect.values()) {
         if (e.toString().equals(effect)) {
            return e;
         }
      }
      return NONE;
   }
   
   /**
    * Gets n random numbers in the range [start, end) <br>
    * this is inclusive at start, exclusive at end. Order is not guaranteed.
    * 
    * @param start
    * @param end
    * @param n
    * @return
    */
   protected List<Integer> getUniqueRandoms(int start, int end, int n) {
      List<Integer> allIndexes = IntStream.range(start, end).boxed().collect(Collectors.toList());
      Collections.shuffle(allIndexes, r);
      return allIndexes.subList(0, Math.max(0, Math.min(n, allIndexes.size())));
   }
   
   /**
    * @param type
    * @param board
    * @param dontMatch
    * @return
    */
   protected Species getRandomSpeciesOfTypeFrom(PkmType type, Board board, Species dontMatch, SimulationTask task) {
      List<Species> options = getSpeciesOfTypeFrom(type, board, dontMatch, task);
      if (options.size() > 1) {
         task.setIsRandom();
      }
      Species result = null;
      if (!options.isEmpty()) {
         int randomFoundSpecies = getRandomInt(options.size());
         result = options.get(randomFoundSpecies);
      }
      return result;
   }
   
   /**
    * @param type
    * @param board
    * @param dontMatch
    * @param task
    * @return
    */
   public List<Species> getSpeciesOfTypeFrom(PkmType type, Board board, Species dontMatch, SimulationTask task) {
      List<Species> options = new ArrayList<Species>();
      Set<Species> contained = new HashSet<Species>();
      for (int row = 1; row <= Board.NUM_ROWS; row++) {
         for (int col = 1; col <= Board.NUM_COLS; col++) {
            Species cur = board.getSpeciesAt(row, col);
            if (!contained.contains(cur) && cur.getType().equals(type) && !cur.equals(dontMatch)
                  && !task.isActive(row, col)) {
               contained.add(cur);
               options.add(cur);
            }
         }
      }
      return options;
   }
   
   protected void addPlansForSurroundingBlocks(ActivateMegaComboEffect effect, SimulationTask task, int radius) {
      List<Integer> limits = SimulationTask.getLimits(effect.getCoords());
      int minRow = limits.get(0);
      int minCol = limits.get(1);
      int maxRow = limits.get(2);
      int maxCol = limits.get(3);
      Species effectSpecies = task.getEffectSpecies(effect.getCoords());
      List<List<Integer>> layerPlans = new ArrayList<List<Integer>>();
      Board b = task.getState().getBoard();
      // For each column, add stuff above and below the match
      for (int col = minCol; col <= maxCol; col++) {
         // Add the expansion of the given radius above the match limits.
         // This one goes upwards in row (lower row = higher on screen)
         addExpansionTo(layerPlans, b, effectSpecies, radius, minRow, col, -1, 0);
         // This one goes downwards in row (higher row = lower on screen)
         addExpansionTo(layerPlans, b, effectSpecies, radius, maxRow, col, 1, 0);
      }
      // For each row, add stuff to the left and right of the match.
      for (int row = minRow; row <= maxRow; row++) {
         // Adding the leftwards expansion
         addExpansionTo(layerPlans, b, effectSpecies, radius, row, minCol, 0, -1);
         // Adding the rightwards expansion
         addExpansionTo(layerPlans, b, effectSpecies, radius, row, minCol, 0, 1);
      }
      // Finally, add the plans
      for (List<Integer> plan : layerPlans) {
         effect.addPlannedOptions(plan);
      }
   }
   
   private void addExpansionTo(List<List<Integer>> layerPlans, Board board, Species boostSpecies, int radius, int row,
         int col, int rowDelta, int colDelta) {
      while (layerPlans.size() < radius) { // ensures that there is enough space for all the
                                           // results.
         layerPlans.add(new ArrayList<Integer>());
      }
      int curRadius = 1;
      int curRow = row + rowDelta;
      int curCol = col + colDelta;
      while (curRow >= 1 && curRow <= Board.NUM_ROWS // row valid
            && curCol >= 1 && curCol <= Board.NUM_COLS // column valid
            && curRadius <= radius) { // radius within bounds
         layerPlans.get(curRadius - 1).addAll(Arrays.asList(curRow, curCol));
         if (curRadius > 1 || !board.getSpeciesAt(curRow, curCol).equals(boostSpecies)) {
            // When radius is 1, this will only be added for things that are not
            // the boost species. Basically, boosts near the origin combo allow
            // the expansion to be larger by the number adjacent to the combo.
            curRadius += 1;
         }
         curRow += rowDelta;
         curCol += colDelta;
      }
      
   }
   
   public List<Integer> getExtraBlocks(ActivateComboEffect comboEffect, SimulationTask task) {
      return null;
   }
   
   public int getValueLimit() {
      return 0;
   }
   
   /**
    * Can a species with this effect be picked up, if not frozen? This is also synonymous with
    * having an attack not equal to 0 (by convention).
    * 
    * @return
    */
   public boolean isPickable() {
      return true;
   }
   
   /**
    * Can a species with this effect be dropped on, if not frozen?
    * 
    * @return
    */
   public boolean isDroppable() {
      return true;
   }
   
   public boolean isAutoGenerated() {
      return true;
   }
   
   public int getEffectRepeatDelay() {
      return 20;
   }
   
   public int getErasureDelay() {
      return getDefaultErasureDelay();
   }
   
   public static int getDefaultErasureDelay() {
      return 82;
   }
   
   public Species getUnfreezeReplacement(Species s) {
      return s;
   }
   
   public void handleCombo(ActivateComboEffect comboEffect, SimulationTask task) {
      ActivateComboEffect effect = handlePlans(comboEffect, task);
      List<Integer> extraBlocks = getExtraBlocks(effect, task);
      doSpecial(comboEffect, task);
      int value = 0;
      if (effect instanceof ActivateMegaComboEffect) {
         ActivateMegaComboEffect activateMegaComboEffect = (ActivateMegaComboEffect) effect;
         value = activateMegaComboEffect.getInt();
      }
      if (extraBlocks == null || value >= getValueLimit()) {
         // Finish
         handleBonusScore(effect, task);
         task.handleMainComboResult(comboEffect, this);
         handleEffectFinished(effect, task);
      } else {
         // Erase another bonus
         Set<List<Integer>> extraCoords = new HashSet<List<Integer>>();
         for (int i = 0; i * 2 + 1 < extraBlocks.size(); i++) {
            int row = extraBlocks.get(i * 2);
            int col = extraBlocks.get(i * 2 + 1);
            if (!task.isActive(row, col)) {
               extraCoords.add(Arrays.asList(row, col));
            }
         }
         // Adds one for every UNIQUE non-active erase coordinates.
         // Certain effects depend on this distinction (Ampharos in particular)
         value += extraCoords.size();
         // But, even if they were active, they need to be handled (ampharos craziness)
         handleExtraBlocks(effect, task, extraBlocks);
         ActivateMegaComboEffect repeatEffect = new ActivateMegaComboEffect(effect);
         repeatEffect.setInt(value);
         task.removeActive(comboEffect);
         task.scheduleEffect(repeatEffect, getEffectRepeatDelay());
      }
   }
   
   protected void doSpecial(ActivateComboEffect comboEffect, SimulationTask task) {
      // Nothing by default.
   }
   
   protected void handleEffectFinished(ActivateComboEffect comboEffect, SimulationTask task) {
      // Nothing by default.
   }
   
   protected ActivateComboEffect handlePlans(ActivateComboEffect comboEffect, SimulationTask task) {
      return comboEffect;
   }
   
   /**
    * @param comboEffect
    * @return
    */
   protected List<Integer> getNextPlan(ActivateComboEffect comboEffect) {
      List<Integer> toErase = null;
      if (comboEffect instanceof ActivateMegaComboEffect) {
         ActivateMegaComboEffect effect = (ActivateMegaComboEffect) comboEffect;
         List<Integer> plan = effect.getNextPlan();
         if (plan != null) {
            toErase = new ArrayList<Integer>();
            toErase.addAll(plan);
         }
      }
      return toErase;
   }
   
   /**
    * @param task
    * @param extraBlocks
    */
   public void handleExtraBlocks(ActivateComboEffect comboEffect, SimulationTask task, List<Integer> extraBlocks) {
      eraseBonus(task, extraBlocks, true);
   }
   
   /**
    * @param comboEffect
    * @param task
    */
   public void handleBonusScore(ActivateComboEffect comboEffect, SimulationTask task) {
      NumberSpan value = getBonusValue(comboEffect, task);
      Species effectSpecies = task.getEffectSpecies(comboEffect.getCoords());
      double basicScore = task.getBasicScoreFor(effectSpecies);
      double typeModifier = task.getTypeModifier(effectSpecies);
      NumberSpan bonusScore = getBonusScoreFor(basicScore, value, typeModifier);
      if (bonusScore.getAverage() > 0) {
         task.addScore(new NumberSpan(bonusScore));
      }
   }
   
   public NumberSpan getBonusValue(ActivateComboEffect comboEffect, SimulationTask task) {
      NumberSpan value = new NumberSpan();
      if (comboEffect instanceof ActivateMegaComboEffect) {
         value = value.add(((ActivateMegaComboEffect) comboEffect).getInt());
      }
      return value;
   }
   
   /**
    * Same as {@link Effect#eraseBonus(SimulationTask, List, boolean)} except this passes true for
    * 'forceErase'
    * 
    * @param task
    *           The task to schedule an erasure in.
    * @param toErase
    *           The pairs of row, column coordinates that denote all tiles affected.
    */
   protected void eraseBonus(SimulationTask task, List<Integer> toErase) {
      eraseBonus(task, toErase, true);
   }
   
   /**
    * Erases the given coordinates in the task, forcing them to fully clear if 'forceErase' is true.
    * 
    * @param task
    *           The task to schedule an erasure in.
    * @param toErase
    *           The pairs of row, column coordinates that denote all tiles affected.
    * @param forceErase
    *           If true, the tiles will be set to Air instead of being allowed to thaw if frozen.
    */
   protected void eraseBonus(SimulationTask task, List<Integer> toErase, boolean forceErase) {
      if (!toErase.isEmpty()) {
         task.eraseBonusIn(toErase, getErasureDelay(), forceErase);
      }
   }
   
   public boolean isPersistent() {
      return false;
   }
   
   /**
    * The returned value is a multiplier for the main score for the given comboEffect in the given
    * task. This is for use via burn, combo chainers, etc.
    * 
    * @param comboEffect
    * @param task
    * @return
    */
   public NumberSpan getScoreMultiplier(ActivateComboEffect comboEffect, SimulationTask task) {
      Species effectSpecies = task.getEffectSpecies(comboEffect.getCoords());
      PkmType effectType = effectSpecies.getType();
      return task.getSpecialTypeMultiplier(effectType);
   }
   
   /**
    * This is a passthrough for modifying the bonus effect score which is added on at the same time
    * that a mega is done. The returned integer value is the interpreted score value given the basic
    * score of the affected block, and the number of blocks as an argument for whatever algorithm
    * decides it. <br>
    * <br>
    * The default is to ignore and return 0.
    * 
    * @param basicScore
    * @param value
    * @param typeModifier
    * @return
    */
   public NumberSpan getBonusScoreFor(double basicScore, NumberSpan value, double typeModifier) {
      return new NumberSpan();
   }
   
   public static int getCoinBias() {
      return 1000000;
   }
   
   private static Random r = new Random(System.nanoTime());
   
   /**
    * Returns an integer between 0 and the given bound, inclusive at 0 and exclusive at the bound.
    * 
    * @param bound
    * @return
    */
   protected static final int getRandomInt(int bound) {
      r.setSeed(r.nextLong());
      return r.nextInt(bound);
   }
   
   /**
    * @return
    */
   public boolean canLevel() {
      return true;
   }
   
   public boolean isDisruption() {
      return !canLevel() && !equals(AIR);
   }
   
   protected double getOdds(SimulationTask task, int num) {
      return task.getState().getCore().getOdds(this, num);
   }
   
   // protected double getOdds(int numBlocks) {
   // // n is between 3 and 5, inclusive
   // int n = Math.max(3, Math.min(6, numBlocks));
   // // get the odds array index at n-3
   // return odds[n - 3];
   // }
   
   /**
    * Returns true if the effect is allowed to possibly occur.
    */
   protected boolean canActivate(ActivateComboEffect comboEffect, SimulationTask task) {
      SimulationCore core = task.getState().getCore();
      boolean coreAllowsIt = !core.isDisabledEffect(this);
      int threshold = core.getEffectThreshold();
      if (threshold > 0) {
         double adjustedThreshold = threshold / 100.0;
         coreAllowsIt &= getOdds(task, comboEffect.getNumBlocks()) >= adjustedThreshold;
      }
      return coreAllowsIt && comboEffect.getNumCombosOnActivate() == 0;
   }
   
   protected boolean doesActivate(ActivateComboEffect comboEffect, SimulationTask task) {
      double odds = getOdds(task, comboEffect.getNumBlocks());
      return odds >= 1.0 || odds >= Math.random();
   }
   
   protected boolean canAndDoesActivate(ActivateComboEffect comboEffect, SimulationTask task) {
      return canActivate(comboEffect, task) && doesActivate(comboEffect, task);
   }
   
   protected final NumberSpan getMultiplier(ActivateComboEffect comboEffect, SimulationTask task, Number bonus) {
      Species effectSpecies = task.getEffectSpecies(comboEffect.getCoords());
      NumberSpan multiplier = task.getSpecialTypeMultiplier(effectSpecies.getType());
      if (canActivate(comboEffect, task)) {
         if (bonus.doubleValue() > 0) {
            multiplier = new NumberSpan(1, bonus, getOdds(task, comboEffect.getNumBlocks())).multiplyBy(multiplier);
         }
      }
      return multiplier;
   }
   
   protected final void ifThenAddScore(ActivateComboEffect comboEffect, SimulationTask task,
         Supplier<Number> supplier) {
      if (canActivate(comboEffect, task)) {
         Number value = supplier.get();
         if (value.doubleValue() > 0) {
            task.addScore(new NumberSpan(0, value, getOdds(task, comboEffect.getNumBlocks())));
         }
      }
   }
   
   protected final void ifThenSetSpecial(ActivateComboEffect comboEffect, SimulationTask task, PkmType type,
         Number bonus) {
      if (canActivate(comboEffect, task)) {
         if (bonus.doubleValue() > 0) {
            task.setSpecialTypeMultiplier(type, new NumberSpan(1, bonus, getOdds(task, comboEffect.getNumBlocks())));
         }
      }
   }
}
