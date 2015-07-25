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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class ShuffleLogFormatter extends Formatter {
   
   private final Date dat = new Date();
   private final static String format = "[{0,date} {0,time}]";
   private final MessageFormat formatter = new MessageFormat(format);
   private final Object[] arg = new Object[1];
   
   @Override
   public synchronized String format(LogRecord record) {
      StringBuilder sb = new StringBuilder();
      
      dat.setTime(record.getMillis());
      arg[0] = dat;
      
      // Date and time
      StringBuffer text = new StringBuffer();
      formatter.format(arg, text, null);
      sb.append(text);
      sb.append(" [");
      
      // Level
      sb.append(record.getLevel().getLocalizedName());
      sb.append("] ");
      
      // The actual message
      sb.append(formatMessage(record));
      sb.append("\n");
      
      // Exception handling
      if (record.getThrown() != null) {
         try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            record.getThrown().printStackTrace(pw);
            pw.close();
            sb.append(sw.toString());
         } catch (Exception ex) {
         }
      }
      return sb.toString();
   }
}
