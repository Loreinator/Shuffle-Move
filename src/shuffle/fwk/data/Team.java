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

import java.util.List;
import java.util.function.Function;

import shuffle.fwk.config.manager.EffectManager;
import shuffle.fwk.config.manager.RosterManager;
import shuffle.fwk.config.manager.SpeciesManager;

/**
 * A Team of species with their bindings.
 * 
 * @author Andrew Meyers
 */
public interface Team {
   
   /**
    * Gets the species for this team that corresponds to the given keyCode binding, using the given
    * SpeciesManager for name to species conversion.
    * 
    * @param keyCode
    *           The keyCode binding
    * @param manager
    *           The SpeciesManager
    * @return The Species, or {@link Species#AIR} if there is no such Species. Returns null if the
    *         SpeciesManager given is null.
    */
   public abstract Species getSpecies(int keyCode, SpeciesManager manager);
   
   /**
    * Gets the species for this team that corresponds to the given Character binding, using the
    * given SpeciesManager for name to species conversion.
    * 
    * @param binding
    *           The character binding
    * @param manager
    *           The SpeciesManager
    * @return The Species, or {@link Species#AIR} if there is no such Species. Returns null if the
    *         SpeciesManager given is null.
    */
   public abstract Species getSpecies(Character binding, Function<String, Species> nameToSpecies);
   
   /**
    * Gets the name of the Species for this team that corresponds to the given keyCode binding.
    * 
    * @param keyCode
    *           The keyCode binding
    * @return The name, or null if there is no such Species.
    */
   public abstract String getName(int keyCode);
   
   /**
    * Gets the name of the Species for this team that corresponds to the given Character binding.
    * 
    * @param binding
    *           The character binding
    * @return The name, or null if there is no such Species.
    */
   public abstract String getName(Character binding);
   
   /**
    * Returns the character binding for the given name.
    * 
    * @param s
    *           The name
    * @return The character binding, or null if there is no such name for this team.
    */
   public abstract Character getBinding(String s);
   
   /**
    * Returns the character binding for the given species.
    * 
    * @param s
    *           The species
    * @return The character binding, or null if there is no such species for this team.
    */
   public abstract Character getBinding(Species s);
   
   /**
    * Returns a list of all names in this team.
    * 
    * @return A List of Strings.
    */
   public abstract List<String> getNames();
   
   /**
    * Returns a List of all Species in this team, using the given manager for name to species
    * conversion.
    * 
    * @param nameToSpecies
    *           TODO
    * 
    * @return A List of Species
    */
   public abstract List<Species> getSpecies(SpeciesManager manager);
   
   /**
    * Checks if the given name is this Team's Mega slot.
    * 
    * @param name
    *           The name of the species
    * @return True if the given name is for a base species that is in the mega slot for this team.
    */
   public abstract boolean isMegaSlot(String name);
   
   /**
    * Gets the threshold for mega evolution given a SpeciesManager to look up the mega slot's
    * species.
    */
   public abstract int getMegaThreshold(SpeciesManager speciesManager, RosterManager rosterManager,
         EffectManager effectManager);
   
   /**
    * Gets the current Mega slot name (non-mega form name). If null then there is no species
    * currently occupying the mega slot.
    * 
    * @return The name of the mega slot occupant (non-mega form name), null if there is none.
    */
   public abstract String getMegaSlotName();
   
}