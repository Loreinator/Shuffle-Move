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

package shuffle.fwk.config;

/**
 * @author Andrew Meyers
 *
 */
public class ConfigEntry {
   private static final String TOSTRING_FORMAT = "%s: %s";
   
   private final EntryType type;
   private final Object value;
   
   public ConfigEntry(EntryType type, Object value) throws NullPointerException, IllegalArgumentException {
      if (value == null || type == null) {
         throw new NullPointerException("Cannot specify a null value or type for ConfigEntry.");
      }
      this.value = value;
      this.type = type;
      if (!type.getDataClass().isInstance(value)) {
         throw new IllegalArgumentException(String.format("Value %s cannot be paired with EntryType %s",
               String.valueOf(value), String.valueOf(type)));
      }
   }
   
   public EntryType getEntryType() {
      return type;
   }
   
   public Class<? extends Object> getValueClass() {
      return type.getDataClass();
   }
   
   public Object getValue() {
      return value;
   }
   
   public String getSaveString() throws Exception {
      return type.getDataString(value);
   }
   
   public static ConfigEntry getEntryFor(String type, String key, String value) throws Exception {
      EntryType entryType = Enum.valueOf(EntryType.class, type);
      Object entryValue = entryType.parseValue(key, value);
      ConfigEntry ret = new ConfigEntry(entryType, entryValue);
      return ret;
   }
   
   @Override
   public String toString() {
      return String.format(TOSTRING_FORMAT, String.valueOf(type), String.valueOf(value));
   }
   
   /*
    * (non-Javadoc)
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + (type == null ? 0 : type.hashCode());
      result = prime * result + (value == null ? 0 : value.hashCode());
      return result;
   }
   
   /*
    * (non-Javadoc)
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj) {
      boolean equal = obj != null && obj instanceof ConfigEntry;
      if (equal) {
         ConfigEntry other = (ConfigEntry) obj;
         equal &= type.equals(other.type);
         equal &= value.equals(other.value);
      }
      return equal;
   }
   
}
