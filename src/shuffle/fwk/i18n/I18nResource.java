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

package shuffle.fwk.i18n;

import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Andrew Meyers
 *
 */
public class I18nResource {
   private static Logger LOG = Logger.getLogger(I18nResource.class.getName());
   
   private ResourceBundle rb = null;
   
   /**
    * @param bundle
    */
   public I18nResource(ResourceBundle bundle) {
      rb = bundle;
   }
   
   public String getString(String key, Object... params) {
      String ret = null;
      if (key != null && rb.containsKey(key)) {
         try {
            ret = rb.getString(key);
         } catch (Exception e) {
            LOG.log(Level.WARNING, "Cannot retrieve key: " + key);
         }
      }
      if (ret != null && params.length > 0) {
         ret = MessageFormat.format(ret, params);
      }
      return ret;
   }
}
