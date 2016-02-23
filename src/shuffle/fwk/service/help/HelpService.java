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

package shuffle.fwk.service.help;

import shuffle.fwk.config.ConfigManager;
import shuffle.fwk.config.EntryType;
import shuffle.fwk.i18n.I18nUser;
import shuffle.fwk.service.textdisplay.TextDisplayService;

/**
 * @author Andrew Meyers
 *
 */
public class HelpService extends TextDisplayService implements I18nUser {
   private static final String KEY_TITLE = "text.title";
   private static final String KEY_RESOURCE_PATH = "resource.path";
   public static final String KEY_AUTOLAUNCH_HELP = "AUTOLAUNCH_HELP";
   private static final String DEFAULT_FILE_KEY = "HELP_FILE";
   
   @Override
   protected String getFilePath() {
      String path = getString(KEY_RESOURCE_PATH);
      return path.equals(KEY_RESOURCE_PATH) ? null : path;
   }
   
   @Override
   protected String getDefaultFileKey() {
      return DEFAULT_FILE_KEY;
   }
   
   @Override
   protected String getTitle(String userTitle) {
      return getString(KEY_TITLE, userTitle);
   }
   
   @Override
   protected void onLaunch() {
      ConfigManager prefManager = getUser().getPreferencesManager();
      prefManager.setEntry(EntryType.BOOLEAN, KEY_AUTOLAUNCH_HELP, true);
      prefManager.saveDataToConfig();
   }
   
   @Override
   protected void onDispose() {
      ConfigManager prefManager = getUser().getPreferencesManager();
      prefManager.setEntry(EntryType.BOOLEAN, KEY_AUTOLAUNCH_HELP, false);
      prefManager.saveDataToConfig();
   }
   
   @Override
   protected void onHide() {
      onDispose();
   }
}
