/*  ShuffleMove - A program for identifying and simulating ideal moves in the game
 *  called Pokemon Shuffle.
 *  
 *  Copyright (C) 2015-2016  Andrew Meyers
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
package shuffle.test.fwk.config.manager;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import shuffle.fwk.ShuffleController;
import shuffle.fwk.config.ConfigFactory;
import shuffle.fwk.config.manager.ImageManager;
import shuffle.fwk.config.manager.SpeciesManager;
import shuffle.fwk.data.Effect;
import shuffle.fwk.data.Species;
import shuffle.fwk.data.SpeciesPaint;

/**
 * @author Andrew Meyers
 *
 */
public class DataIntegrityTest {
   
   static private ConfigFactory factory;
   static private MockHandler mockHandler;
   
   /**
    * @throws java.lang.Exception
    */
   @BeforeClass
   public static void setUpBeforeClass() throws Exception {
      ShuffleController.setUserHome("./Shuffle-Move");
      mockHandler = new MockHandler();
      mockHandler.setLevel(Level.WARNING);
      factory = new ConfigFactory();
      Logger.getLogger("").addHandler(mockHandler);
   }
   
   @Before
   public void setUpBefore() throws Exception {
      DataIntegrityTest.mockHandler.clear();
   }
   
   @After
   public void setUpAfter() throws Exception {
      DataIntegrityTest.mockHandler.clear();
   }
   
   @Test
   public final void testEffectManager() {
      factory.getEffectManager();
      if (!mockHandler.getLog().isEmpty()) {
         fail("Config Warnings: " + String.valueOf(mockHandler.getLog().size()));
      }
   }

   @Test
   public final void testEntryModeManager() {
      factory.getEntryModeManager();
      if (!mockHandler.getLog().isEmpty()) {
         fail("Config Warnings: " + String.valueOf(mockHandler.getLog().size()));
      }
   }

   @Test
   public final void testGradingModeManager() {
      factory.getGradingModeManager();
      if (!mockHandler.getLog().isEmpty()) {
         fail("Config Warnings: " + String.valueOf(mockHandler.getLog().size()));
      }
   }

   @Test
   public final void testImageManager() {
      factory.getImageManager();
      if (!mockHandler.getLog().isEmpty()) {
         fail("Config Warnings: " + String.valueOf(mockHandler.getLog().size()));
      }
   }

   @Test
   public final void testRosterManager() {
      factory.getRosterManager();
      if (!mockHandler.getLog().isEmpty()) {
         fail("Config Warnings: " + String.valueOf(mockHandler.getLog().size()));
      }
   }

   @Test
   public final void testSpeciesManager() {
      factory.getSpeciesManager();
      if (!mockHandler.getLog().isEmpty()) {
         fail("Config Warnings: " + String.valueOf(mockHandler.getLog().size()));
      }
   }
   
   @Test
   public final void testStageManager() {
      factory.getStageManager();
      if (!mockHandler.getLog().isEmpty()) {
         fail("Config Warnings: " + String.valueOf(mockHandler.getLog().size()));
      }
   }
   
   @Test
   public final void testTeamManager() {
      factory.getTeamManager();
      if (!mockHandler.getLog().isEmpty()) {
         fail("Config Warnings: " + String.valueOf(mockHandler.getLog().size()));
      }
   }
   
   @Test
   public final void testImagesForSpecies() {
      ImageManager imgManager = factory.getImageManager();
      SpeciesManager speciesManager = factory.getSpeciesManager();
      int missing = 0;
      List<SpeciesPaint> paintsToCheck = new ArrayList<SpeciesPaint>();
      for (Species s : speciesManager.getAllSpecies()) {
         paintsToCheck.add(new SpeciesPaint(s, false, false));
         paintsToCheck.add(new SpeciesPaint(s, true, false));
         if (s.getMegaName() != null // Has a mega name
               && !Effect.NONE.equals(s.getMegaEffect())) { // Mega effect is well defined
            paintsToCheck.add(new SpeciesPaint(s, false, true));
            paintsToCheck.add(new SpeciesPaint(s, true, true));
         }
      }
      for (SpeciesPaint sp : paintsToCheck) {
         if (imgManager.getImageFor(sp) == null) {
            missing++;
            String name = sp.isMega() ? sp.getSpecies().getMegaName() : sp.getSpecies().getName();
            System.out.println("Missing Image for: " + name + ", frozen: " + sp.isFrozen() + ", mega: " + sp.isMega());
         }
      }
      if (missing > 0) {
         fail("Missing images: " + String.valueOf(missing) + ", check species and icon mappings.");
      }
   }
   
   public static class MockHandler extends Handler {
      private final List<LogRecord> log = new ArrayList<LogRecord>();
      
      public List<LogRecord> getLog() {
         return log;
      }
      
      /*
       * (non-Javadoc)
       * @see java.util.logging.Handler#publish(java.util.logging.LogRecord)
       */
      @Override
      public void publish(LogRecord record) {
         if (record.getLevel().intValue() >= Level.WARNING.intValue()) {
            log.add(record);
         }
      }
      
      /*
       * (non-Javadoc)
       * @see java.util.logging.Handler#flush()
       */
      @Override
      public void flush() {
         // nothing to do
      }
      
      /*
       * (non-Javadoc)
       * @see java.util.logging.Handler#close()
       */
      @Override
      public void close() throws SecurityException {
         clear();
      }
      
      public void clear() {
         log.clear();
      }
      
   }
}
