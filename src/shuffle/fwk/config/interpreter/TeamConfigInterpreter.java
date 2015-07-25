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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import shuffle.fwk.config.Interpreter;
import shuffle.fwk.data.TeamImpl;

/**
 * @author Andrew Meyers
 *
 */
public class TeamConfigInterpreter implements Interpreter<TeamImpl> {
   
   @SuppressWarnings("unused")
   private static final Logger LOG = Logger.getLogger(TeamConfigInterpreter.class.getName());
   
   private static final String REGEX = "^\\s*(\\S+)\\s+(\\S+)(?:\\s+((?:[^,\\s]?[,])*[^,\\s]?))?(?:\\s+([^\\s,]+)(?:\\s*[\\s,]\\s*([^\\s,]+))?)?\\s*$";
   // Stagename ListofSpecies ListofKeybinds MegaName,enabled
   private static final Pattern TEAM_PATTERN = Pattern.compile(REGEX);
   
   @Override
   public Map<String, List<TeamImpl>> interpret(String line) {
      Map<String, List<TeamImpl>> ret = new HashMap<String, List<TeamImpl>>();
      Matcher m = TEAM_PATTERN.matcher(line);
      if (m.find()) {
         List<TeamImpl> value = new ArrayList<TeamImpl>();
         TeamImpl t = makeTeam(m.group(2), m.group(3));
         String megaName = m.group(4);
         t.setMegaSlot(megaName);
         value.add(t);
         ret.put(m.group(1), value);
      }
      return ret;
   }
   
   private TeamImpl makeTeam(String names, String bindings) {
      if (names == null || names.isEmpty()) {
         return new TeamImpl();
      }
      List<String> nameList = new ArrayList<String>();
      List<String> bindingList = new ArrayList<String>();
      
      nameList.addAll(Arrays.asList(names.split("[,]")));
      if (bindings != null && !bindings.isEmpty()) {
         bindingList.addAll(Arrays.asList(bindings.split("[,]", -1)));
      }
      TeamImpl ret = new TeamImpl();
      for (int i = 0; i < nameList.size(); i++) {
         String name = nameList.get(i);
         Character binding = null;
         if (i < bindingList.size() && bindingList.get(i).length() > 0) {
            binding = bindingList.get(i).charAt(0);
         }
         ret.addName(name, binding);
      }
      return ret;
   }
   
}
