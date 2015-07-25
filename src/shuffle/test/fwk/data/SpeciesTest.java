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

package shuffle.test.fwk.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import shuffle.fwk.data.Effect;
import shuffle.fwk.data.PkmType;
import shuffle.fwk.data.Species;

/**
 * @author Andrew Meyers
 *
 */
public class SpeciesTest {
   
   /**
    * @throws java.lang.Exception
    */
   @Before
   public void setUp() throws Exception {
   }
   
   /**
    * Test method for
    * {@link shuffle.fwk.data.Species#Species(java.lang.String, int, shuffle.fwk.data.PkmType, shuffle.fwk.data.Effect)}
    * .
    */
   @Test
   public final void testSpeciesStringIntPkmTypeComboEffect() {
      assertEquals("name -1 42 BUG NONE", new Species("name", 42, PkmType.BUG, Effect.NONE).toString());
      assertEquals("name -1 42 ICE GLALIE", new Species("name", 42, PkmType.ICE, Effect.GLALIE).toString());
      assertEquals("name -1 42 STEEL AIR", new Species("name", 42, PkmType.STEEL, Effect.AIR).toString());
      assertEquals("name -1 88 STEEL AIR", new Species("name", 88, PkmType.STEEL, Effect.AIR).toString());
      assertEquals("other -1 88 STEEL AIR", new Species("other", 88, PkmType.STEEL, Effect.AIR).toString());
      assertEquals("OTHER -1 88 STEEL AIR", new Species("OTHER", 88, PkmType.STEEL, Effect.AIR).toString());
   }
   
   /**
    * Test method for
    * {@link shuffle.fwk.data.Species#Species(java.lang.String, int, shuffle.fwk.data.PkmType, shuffle.fwk.data.Effect, java.lang.String)}
    * .
    */
   @Test
   public final void testSpeciesStringIntPkmTypeComboEffectStringComboEffect() {
      assertEquals("name -1 42 BUG NONE",
            new Species("name", null, 42, PkmType.BUG, Effect.NONE, null, null).toString());
      assertEquals("name -1 42 ICE GLALIE rep NONE", new Species("name", null, 42, PkmType.ICE, Effect.GLALIE, "rep",
            null).toString());
      assertEquals("name -1 42 STEEL AIR rep NONE", new Species("name", null, 42, PkmType.STEEL, Effect.AIR, "rep",
            null).toString());
      assertEquals("name -1 88 STEEL AIR REP NONE", new Species("name", null, 88, PkmType.STEEL, Effect.AIR, "REP",
            null).toString());
      assertEquals("other -1 88 STEEL AIR rep NONE", new Species("other", null, 88, PkmType.STEEL, Effect.AIR, "rep",
            null).toString());
      assertEquals("OTHER -1 88 STEEL AIR rep NONE", new Species("OTHER", null, 88, PkmType.STEEL, Effect.AIR, "rep",
            null).toString());
   }
   
   /**
    * Test method for {@link shuffle.fwk.data.Species#getName()}.
    */
   @Test
   public final void testGetName() {
      assertEquals("name", new Species("name", 42, PkmType.BUG, Effect.NONE).getName());
      assertEquals("name", new Species("name", 42, PkmType.ICE, Effect.GLALIE).getName());
      assertEquals("name", new Species("name", 42, PkmType.STEEL, Effect.AIR).getName());
      assertEquals("name", new Species("name", 88, PkmType.STEEL, Effect.AIR).getName());
      assertEquals("other", new Species("other", 88, PkmType.STEEL, Effect.AIR).getName());
      assertEquals("OTHER", new Species("OTHER", 88, PkmType.STEEL, Effect.AIR).getName());
   }
   
   /**
    * Test method for {@link shuffle.fwk.data.Species#getBaseAttack()}.
    */
   @Test
   public final void testGetBaseAttack() {
      assertEquals(42, new Species("name", 42, PkmType.STEEL, Effect.AIR).getBaseAttack());
      assertEquals(88, new Species("name", 88, PkmType.STEEL, Effect.AIR).getBaseAttack());
   }
   
   /**
    * Test method for {@link shuffle.fwk.data.Species#getAttack(int)}.
    */
   @Test
   public final void testGetAttack() {
      assertEquals(40, new Species("name", 40, PkmType.STEEL, Effect.AIR).getAttack(-1000));
      assertEquals(40, new Species("name", 40, PkmType.STEEL, Effect.AIR).getAttack(-1));
      assertEquals(40, new Species("name", 40, PkmType.STEEL, Effect.AIR).getAttack(0));
      assertEquals(40, new Species("name", 40, PkmType.STEEL, Effect.AIR).getAttack(1));
      assertEquals(43, new Species("name", 40, PkmType.STEEL, Effect.AIR).getAttack(2));
      assertEquals(46, new Species("name", 40, PkmType.STEEL, Effect.AIR).getAttack(3));
      assertEquals(48, new Species("name", 40, PkmType.STEEL, Effect.AIR).getAttack(4));
      assertEquals(50, new Species("name", 40, PkmType.STEEL, Effect.AIR).getAttack(5));
      assertEquals(52, new Species("name", 40, PkmType.STEEL, Effect.AIR).getAttack(6));
      assertEquals(54, new Species("name", 40, PkmType.STEEL, Effect.AIR).getAttack(7));
      assertEquals(56, new Species("name", 40, PkmType.STEEL, Effect.AIR).getAttack(8));
      assertEquals(58, new Species("name", 40, PkmType.STEEL, Effect.AIR).getAttack(9));
      assertEquals(60, new Species("name", 40, PkmType.STEEL, Effect.AIR).getAttack(10));
      assertEquals(60, new Species("name", 40, PkmType.STEEL, Effect.AIR).getAttack(11));
      assertEquals(60, new Species("name", 40, PkmType.STEEL, Effect.AIR).getAttack(50));
      assertEquals(60, new Species("name", 40, PkmType.STEEL, Effect.AIR).getAttack(100));
   }
   
   /**
    * Test method for {@link shuffle.fwk.data.Species#getType()}.
    */
   @Test
   public final void testGetType() {
      for (PkmType t : PkmType.values()) {
         assertEquals(t, new Species("name", 42, t, Effect.NONE).getType());
      }
   }
   
   /**
    * Test method for {@link shuffle.fwk.data.Species#getEffect()}.
    */
   @Test
   public final void testGetEffect() {
      for (Effect ce : Effect.values()) {
         assertEquals(ce, new Species("name", 42, PkmType.BUG, ce).getEffect());
      }
   }
   
   /**
    * Test method for {@link shuffle.fwk.data.Species#getMegaName()}.
    */
   @Test
   public final void testGetForms() {
      // Thoroughly tests that all characters are allowed, capitals are retained, all symbols
      // allowed.
      for (String s : new String[] { null, "a", "A", "abababa", "ABabAbBa", "qwertyuiopasdfghjklzxcvbnm", "1234567890",
            "~!@#$%^&*()_+{}|:\"<>?", "`-=[]\\;',./" }) {
         assertEquals(s, new Species("name", null, 42, PkmType.BUG, Effect.NONE, s, null).getMegaName());
      }
   }
   
   /**
    * Test method for {@link shuffle.fwk.data.Species#isFreezable()}.
    */
   @Test
   public final void testIsFreezable() {
      for (Effect ce : Effect.values()) {
         for (PkmType t : PkmType.values()) {
            Species s = new Species("name", 40, t, ce);
            assertTrue(s.isFreezable() || s.getEffect().equals(Effect.AIR));
         }
      }
   }
   
   /**
    * Test method for {@link shuffle.fwk.data.Species#toString()}.
    */
   @Test
   public final void testToString() {
      for (int baseAttack : new int[] { -1000, -1, 0, 40, 50, 60, 66, 999, 1000, 1500 }) {
         for (PkmType t : PkmType.values()) {
            for (Effect ce : Effect.values()) {
               assertEquals(properString("AName", 17, baseAttack, t, ce), new Species("AName", 17, baseAttack, t, ce,
                     null, Effect.NONE).toString());
            }
         }
      }
   }
   
   private String properString(String name, int number, int baseAttack, PkmType t, Effect ce) {
      int atk;
      if (baseAttack < 0) {
         atk = 0;
      } else if (baseAttack >= 999) {
         atk = 999;
      } else {
         atk = baseAttack;
      }
      return String.format("%s %d %d %s %s", name, number, atk, t.toString(), ce.toString());
   }
   
   /**
    * Test method for {@link shuffle.fwk.data.Species#equals(java.lang.Object)}.
    */
   @Test
   public final void testEqualsObject() {
      for (int baseAttack : new int[] { 0, 40, 50, 60, 66 }) {
         for (PkmType t : PkmType.values()) {
            for (Effect ce : Effect.values()) {
               assertEquals("Nothing should be different.", new Species("AName", baseAttack, t, ce), new Species(
                     "AName", baseAttack, t, ce));
               assertNotEquals("Name is supposed to be different", new Species("ANameE", baseAttack, t, ce),
                     new Species("AName", baseAttack, t, ce));
               assertNotEquals("Base Attack is suppoesd to be different.", new Species("AName", baseAttack + 1, t, ce),
                     new Species("AName", baseAttack, t, ce));
            }
         }
      }
   }
   
}
