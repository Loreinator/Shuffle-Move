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

package shuffle.fwk.log;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * @author Andrew Meyers
 *
 */
public class AlertLogHandler extends Handler {
   
   private static DateFormat df = DateFormat.getDateTimeInstance();
   private static Date date = new Date();
   
   private static String prev = "";
   
   private static Set<AlertReciever> recievers = new HashSet<AlertReciever>();
   
   public static void registerReciever(AlertReciever reciever) {
      if (reciever != null) {
         recievers.add(reciever);
      }
   }
   
   public static void unregisterReciever(AlertReciever reciever) {
      if (reciever != null) {
         recievers.remove(reciever);
      }
   }
   
   @Override
   public void close() throws SecurityException {
   }
   
   @Override
   public void flush() {
   }
   
   @Override
   public void publish(LogRecord arg0) {
      date.setTime(arg0.getMillis());
      String s = String.format("[%s] %s", df.format(date), arg0.getMessage());
      if (!s.equals(prev) && arg0.getLevel().intValue() >= Level.INFO.intValue()) {
         for (AlertReciever reciever : recievers) {
            reciever.processLogMessage(s);
         }
      }
      prev = s;
   }
}
