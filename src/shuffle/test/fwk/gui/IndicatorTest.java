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

package shuffle.test.fwk.gui;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JFrame;

import shuffle.fwk.ShuffleController;
import shuffle.fwk.config.ConfigFactory;
import shuffle.fwk.config.ConfigManager;
import shuffle.fwk.data.PkmType;
import shuffle.fwk.data.Species;
import shuffle.fwk.data.SpeciesPaint;
import shuffle.fwk.gui.Indicator;

/**
 * @author Andrew Meyers
 *
 */
public class IndicatorTest {
   private ShuffleController ctrl = new ShuffleController();
   
   public static void main(String[] args) {
      new IndicatorTest().testIndicatorShuffleView();
   }
   
   public final void testIndicatorShuffleView() {
      JFrame newFrame = new JFrame();
      
      Container mainPanel = newFrame.getContentPane();
      mainPanel.setLayout(new GridBagLayout());
      GridBagConstraints c = new GridBagConstraints();
      c.fill = GridBagConstraints.BOTH;
      c.weightx = 1.0;
      c.weighty = 1.0;
      c.gridx = 1;
      c.gridy = 1;
      
      for (PkmType t : PkmType.values()) {
         Indicator<PkmType> ind = new Indicator<PkmType>(ctrl);
         ind.setVisualized(t);
         mainPanel.add(ind, c);
         c.gridy++;
         if (c.gridy > 10) {
            c.gridy = 1;
            c.gridx++;
         }
      }
      ConfigManager manager = new ConfigFactory().getSpeciesManager();
      for (Species s : manager.getSpeciesValues()) {
         Indicator<SpeciesPaint> ind = new Indicator<SpeciesPaint>(ctrl);
         ind.setVisualized(new SpeciesPaint(s, true));
         mainPanel.add(ind, c);
         c.gridy++;
         if (c.gridy > 10) {
            c.gridy = 1;
            c.gridx++;
         }
      }
      
      newFrame.setResizable(false);
      newFrame.pack();
      newFrame.setLocationRelativeTo(null);
      newFrame.setVisible(true);
      newFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      // while(newFrame.isVisible()){
      // }
   }
   
}
