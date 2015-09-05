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

package shuffle.fwk.update;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import shuffle.fwk.config.EntryType;
import shuffle.fwk.i18n.I18nUser;
import shuffle.fwk.service.update.UpdateService;

public class UpdateCheck implements I18nUser {
   private static final Logger LOG = Logger.getLogger(UpdateCheck.class.getName());
   private static final String VERSION_SITE = "http://loresoftworks.noip.me/shuffleversion.html";
   private static final String VERSION_REGEX = "(v\\d+\\.\\d+\\.\\d+).*href=\"(http\\S+)\">";
   private static final Pattern VERSION_PATTERN = Pattern.compile(VERSION_REGEX);
   private static final String VERSION_EXTRACT = "v(\\d+)\\.(\\d+)\\.(\\d+)";
   private static final Pattern VERSION_EXTRACT_PATTERN = Pattern.compile(VERSION_EXTRACT);
   private static final String ZIP_NAME = "Shuffle Move %s.zip";
   
   private static final String KEY_CORRUPT_PATH = "updatecheck.site.corruptpath";
   private static final String KEY_SITE_IOEXCEPTION = "updatecheck.site.ioexception";
   private static final String KEY_SITE_USEMANUAL = "updatecheck.site.pleaseusemanual";
   private static final String KEY_DOWNLOAD_READY = "updatecheck.ready";
   private static final String KEY_NOUPDATES = "updatecheck.noupdates";
   private static final String KEY_UPTODATE = "updatecheck.uptodate";
   private static final String KEY_OUTOFDATE = "updatecheck.outofdate";
   private static final String KEY_GET_IOEXCEPTION = "updatecheck.get.ioexception";
   private static final String KEY_GET_USEMANUAL = "updatecheck.get.pleaseusemanual";
   private static final String KEY_GET_INVALID = "updateCheck.get.versioninvalid";
   
   public static final ExecutorService EXECUTOR = new ScheduledThreadPoolExecutor(1);
   public static final String PROPERTY_DONE = "PROPERTY_DONE";
   public static final String PROPERTY_MESSAGE = "MESSAGE";
   public static final String NO_VERSION = "v0.0.0";
   
   public Map<String, String> getAvailableVersions() {
      Map<String, String> ret = new HashMap<String, String>();
      try (InputStream is = new URL(VERSION_SITE).openStream()) {
         BufferedReader br = new BufferedReader(new InputStreamReader(is));
         String line;
         while ((line = br.readLine()) != null) {
            Matcher m = VERSION_PATTERN.matcher(line);
            if (m.find()) {
               ret.put(m.group(1), m.group(2));
            }
         }
      } catch (MalformedURLException mue) {
         LOG.log(Level.SEVERE, getString(KEY_CORRUPT_PATH), mue);
      } catch (IOException ioe) {
         LOG.log(Level.SEVERE, getString(KEY_SITE_IOEXCEPTION), ioe);
         LOG.warning(getString(KEY_SITE_USEMANUAL));
      }
      return ret;
   }
   
   public static void main(String[] args) {
      String path = "http://www.serebii.net/shuffle/pokemon/%s.png";
      String savePath = "downloads/%s.png";
      List<String> namesToTry = Arrays.asList("%s", "%s-m", "%s-mx", "%s-my");
      NumberFormat format = new DecimalFormat("000");
      TreeSet<Integer> megas = new TreeSet<Integer>(Arrays.asList(15, 18, 80, 208, 254, 260, 302, 323, 334, 362, 373,
            376, 380, 381, 384, 428, 475, 531, 719, 3, 6, 9, 65, 94, 115, 127, 130, 142, 150, 181, 212, 214, 229, 248,
            257, 282, 303, 306, 308, 310, 354, 359, 445, 448, 460));
      for (int i = 1; i < 723; i++) {
         String name = format.format(i);
         if (megas.contains(i)) {
            for (String nameToTry : namesToTry) {
               String fileName = String.format(nameToTry, name);
               String saveAt = String.format(savePath, fileName);
               String loadFrom = String.format(path, fileName);
               saveToFile(saveAt, loadFrom);
            }
         } else {
            String fileName = String.format(namesToTry.get(0), name);
            String saveAt = String.format(savePath, fileName);
            String loadFrom = String.format(path, fileName);
            saveToFile(saveAt, loadFrom);
         }
      }
   }
   
   public boolean isNewestVersion(String curVersion, Map<String, String> availableVersions) {
      String newestVersion = getNewestVersion(availableVersions);
      return getVersionNumber(curVersion) >= getVersionNumber(newestVersion);
   }
   
   /**
    * @return
    */
   public String getNewestVersion(Map<String, String> availableVersions) {
      String newestVersion = NO_VERSION;
      if (!availableVersions.isEmpty()) {
         for (String v : availableVersions.keySet()) {
            if (getVersionNumber(v) > getVersionNumber(newestVersion)) {
               newestVersion = v;
            }
         }
      }
      return newestVersion;
   }
   
   public void doUpdate(String curVersion, boolean force) {
      doUpdate(curVersion, force, null);
   }
   
   public void doUpdate(String curVersion, boolean force, PropertyChangeListener listener) {
      Map<String, String> availableVersions = getAvailableVersions();
      boolean listenerHandled = false;
      if (!availableVersions.isEmpty()) {
         String newestVersion = getNewestVersion(availableVersions);
         String target = versionCheck(curVersion, availableVersions);
         if (force) {
            target = availableVersions.get(newestVersion);
         }
         if (target == null) {
            listener.propertyChange(new PropertyChangeEvent(this, PROPERTY_MESSAGE, "", UpdateService.KEY_UPTODATE));
         } else {
            File file = null;
            try {
               file = getFile(getZipName(newestVersion));
            } catch (IOException e) {
               String ioExceptionString = getString(KEY_GET_IOEXCEPTION, newestVersion);
               LOG.log(Level.SEVERE, ioExceptionString, e);
               LOG.warning(getString(KEY_GET_USEMANUAL));
            }
            if (file != null && file.exists() && !force) {
               LOG.warning(getString(KEY_DOWNLOAD_READY) + " " + file.getAbsolutePath());
               listener.propertyChange(new PropertyChangeEvent(this, PROPERTY_MESSAGE, "",
                     UpdateService.KEY_PLEASE_UNPACK));
               listenerHandled = true;
            } else {
               getVersion(newestVersion, target, listener);
               listenerHandled = true;
            }
         }
      } else {
         LOG.warning(getString(KEY_NOUPDATES));
      }
      if (!listenerHandled && listener != null) {
         listener.propertyChange(new PropertyChangeEvent(this, PROPERTY_DONE, 0, 0));
      }
   }
   
   public String versionCheck(String curVersion, Map<String, String> availableVersions) {
      String newestVersion = getNewestVersion(availableVersions);
      String target = null;
      if (getVersionNumber(curVersion) >= getVersionNumber(newestVersion)) {
         LOG.info(getString(KEY_UPTODATE, newestVersion, curVersion));
      } else {
         LOG.info(getString(KEY_OUTOFDATE));
         target = availableVersions.get(newestVersion);
      }
      return target;
   }
   
   /**
    * @param newestVersion
    */
   public void getVersion(String newestVersion, String fileUrl, PropertyChangeListener listener) {
      EXECUTOR.submit(new Runnable() {
         @Override
         public void run() {
            try (InputStream stream = new URL(fileUrl).openStream()) {
               File file = getFile(getZipName(newestVersion));
               Files.copy(stream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
               LOG.warning(getString(KEY_DOWNLOAD_READY) + " " + file.getAbsolutePath());
               listener.propertyChange(new PropertyChangeEvent(this, PROPERTY_MESSAGE, "",
                     UpdateService.KEY_PLEASE_UNPACK));
            } catch (IOException e) {
               String ioExceptionString = getString(KEY_GET_IOEXCEPTION, newestVersion);
               LOG.log(Level.SEVERE, ioExceptionString, e);
               LOG.warning(getString(KEY_GET_USEMANUAL));
               listener.propertyChange(new PropertyChangeEvent(this, PROPERTY_MESSAGE, "", ioExceptionString));
            } finally {
               listener.propertyChange(new PropertyChangeEvent(this, PROPERTY_DONE, 0, 0));
            }
         }
      });
   }
   
   private File getFile(String path) throws IOException {
      try {
         return (File) EntryType.FILE.parseValue(null, path);
      } catch (Exception e) {
         if (e instanceof IOException) {
            throw (IOException) e;
         }
      }
      return null;
   }
   
   private static void saveToFile(String fileName, String fileUrl) {
      try (InputStream stream = new URL(fileUrl).openStream()) {
         // System.out.println("Saving to path: " + path.getFileName());
         Files.copy(stream, Paths.get(fileName), StandardCopyOption.REPLACE_EXISTING);
      } catch (IOException e) {
         System.out.println("Bad path: " + e.getMessage());
      }
   }
   
   /**
    * @param newestVersion
    * @return
    */
   public static String getZipName(String newestVersion) {
      return String.format(ZIP_NAME, newestVersion);
   }
   
   public int getVersionNumber(String version) {
      int ret = 0;
      try {
         ret = parseVersionNumber(version);
      } catch (NumberFormatException e) {
         LOG.log(Level.SEVERE, getString(KEY_GET_INVALID, version), e);
      }
      return ret;
   }
   
   public int[] getVersionNumbers(String version) {
      int[] ret = new int[3];
      try {
         Matcher m = VERSION_EXTRACT_PATTERN.matcher(version);
         if (m.find()) {
            for (int i = 0; i < 3; i++) {
               ret[i] = Math.max(0, Integer.parseInt(m.group(i + 1)));
            }
         }
      } catch (NumberFormatException e) {
         LOG.log(Level.SEVERE, getString(KEY_GET_INVALID, version), e);
      }
      return ret;
   }

   public static String getVersionString(int version) {
      String versionString;
      if (version <= 0) {
         versionString = "v0.0.0";
      } else {
         int subminor = version % 1000;
         int remainder = version / 1000;
         int minor = remainder % 1000;
         int major = remainder / 1000;
         versionString = String.format("v%d.%d.%d", major, minor, subminor);
      }
      return versionString;
   }
   
   public static int parseVersionNumber(String version) throws NumberFormatException {
      Matcher m = VERSION_EXTRACT_PATTERN.matcher(version);
      int ret = 0;
      if (m.find()) {
         for (int i = 0; i < 3; i++) {
            ret += Math.pow(1000, i) * Math.max(0, Integer.parseInt(m.group(3 - i)));
         }
      }
      return ret;
   }
}
