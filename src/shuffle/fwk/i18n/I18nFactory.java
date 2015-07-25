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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Andrew Meyers
 *
 */
public class I18nFactory {
   private static Logger LOG = Logger.getLogger(I18nFactory.class.getName());
   
   private static Map<Object, ResourceBundle> bundles = new HashMap<Object, ResourceBundle>();
   
   public static String getString(Class<? extends Object> c, String key, Object... params) {
      I18nResource resource = getResource(c.getName());
      String value = null;
      if (resource != null && key != null) {
         value = resource.getString(key, params);
      }
      if (value == null) {
         value = key;
      }
      return value;
   }
   
   public static I18nResource getResource(String resourceName) {
      return getResource(resourceName, Locale.getDefault());
   }
   
   public static I18nResource getResource(String resourceName, Locale locale) {
      ResourceBundle bundle = bundles.get(getKey(resourceName, locale));
      if (bundle == null) {
         try {
            ClassLoader resourceLoader = I18nFactory.class.getClassLoader();
            bundle = ResourceBundle.getBundle(resourceName, locale, resourceLoader);
         } catch (Exception e) {
            if (e instanceof MissingResourceException) {
               LOG.warning("Resource missing: " + resourceName);
            }
            LOG.log(Level.WARNING, "Cannot load resource " + resourceName, e);
         }
      }
      return getI18nResource(bundle);
   }
   
   /**
    * @param bundle
    * @return
    */
   private static I18nResource getI18nResource(ResourceBundle bundle) {
      I18nResource resource = null;
      if (bundle != null) {
         resource = new I18nResource(bundle);
      }
      return resource;
   }
   
   private static Object getKey(String resourceName, Locale locale) {
      return Arrays.asList(resourceName, locale);
   }
   
   public static void clearCache() {
      ResourceBundle.clearCache();
   }
}
