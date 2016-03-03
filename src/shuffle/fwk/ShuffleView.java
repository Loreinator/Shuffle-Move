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

import java.util.logging.Logger;

import shuffle.fwk.config.manager.ImageManager;
import shuffle.fwk.config.provider.ImageManagerProvider;
import shuffle.fwk.data.Effect;
import shuffle.fwk.data.Species;
import shuffle.fwk.data.SpeciesPaint;
import shuffle.fwk.data.Stage;
import shuffle.fwk.data.Team;

/**
 * @author Andrew Meyers
 *
 */
public class ShuffleView implements ImageManagerProvider {
   /** The logger for this view. */
   @SuppressWarnings("unused")
   private static final Logger LOG = Logger.getLogger(ShuffleView.class.getName());
   
   /** The controller for this view. */
   private ShuffleViewUser user = null;
   
   /**
    * Creates a new ShuffleView bound to the given ShuffleViewUser.
    * 
    * @param user
    *           The user to bind this new view to.
    */
   public ShuffleView(ShuffleViewUser user) {
      setUser(user);
   }
   
   /**
    * Gets the ShuffleViewUser to which this view is bound.
    * 
    * @return the ShuffleViewUser
    */
   public ShuffleViewUser getUser() {
      return user;
   }
   
   /**
    * Sets the user to which this view is bound.
    * 
    * @param user
    *           The ShuffleViewUser
    */
   public void setUser(ShuffleViewUser user) {
      this.user = user;
   }
   
   /**
    * Gets the suggested text for the given Object o, for use when usual display means are
    * unavailable. Species and SpeciesPaints will produce their key-bind as the string, and in the
    * case of SpeciesPaint if the paint is frozen it will be appended by 'f'.
    * 
    * @param o
    *           The Object.
    * @return A String which might represent the object.
    */
   public String getTextFor(Object o) {
      String ret = null;
      if (o != null) {
         Species s = null;
         boolean frozen = false;
         if (o instanceof Species) {
            s = (Species) o;
         }
         if (o instanceof SpeciesPaint) {
            s = ((SpeciesPaint) o).getSpecies();
            frozen = ((SpeciesPaint) o).isFrozen();
         }
         Stage currentStage = getUser().getBoardManager().getCurrentStage();
         Team currentTeam = getUser().getTeamManager().getTeamForStage(currentStage);
         if (s != null && currentTeam != null) {
            Character binding = currentTeam.getBinding(s);
            if (binding != null) {
               ret = Character.toString(binding);
               if (frozen) {
                  ret = ret + 'f';
               }
            } else {
               if (s.getEffect().equals(Effect.AIR)) {
                  ret = "?";
               }
            }
         }
         if (ret == null) {
            ret = o.toString();
         }
      } else {
         ret = "NA";// Fall back position
      }
      return ret;
   }
   
   /*
    * (non-Javadoc)
    * @see shuffle.fwk.config.provider.ImageManagerProvider#scaleBorderThickness(int)
    */
   @Override
   public Integer scaleBorderThickness(int given) {
      return getUser().scaleBorderThickness(given);
   }
   
   /*
    * (non-Javadoc)
    * @see shuffle.fwk.config.provider.ImageManagerProvider#getImageManager()
    */
   @Override
   public ImageManager getImageManager() {
      return getUser().getImageManager();
   }
}
