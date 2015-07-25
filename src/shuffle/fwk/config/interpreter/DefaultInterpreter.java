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

package shuffle.fwk.config.interpreter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import shuffle.fwk.config.Interpreter;

/**
 * @author Andrew Meyers
 * @param <Y>
 *
 */
public class DefaultInterpreter<Y> implements Interpreter<Y> {
   private static final Logger LOG = Logger.getLogger(DefaultInterpreter.class.getName());
   private static final Pattern PATH_PATTERN = Pattern.compile("^\\s*(\\S+)\\s+(\\S+)\\s*$");
   
   @SuppressWarnings("unchecked")
   @Override
   public Map<String, List<Y>> interpret(String line) {
      Map<String, List<Y>> ret = new HashMap<String, List<Y>>();
      Matcher m = PATH_PATTERN.matcher(line);
      if (m.find()) {
         try {
            List<Y> value = new ArrayList<Y>();
            value.add((Y) m.group(2));
            ret.put(m.group(1), value);
         } catch (ClassCastException cce) {
            LOG.warning("Cannot cast value to the given class.");
         }
      }
      return ret;
   }
}
