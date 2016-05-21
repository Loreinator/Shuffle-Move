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

package shuffle.fwk.config.manager;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import shuffle.fwk.config.ConfigFactory;
import shuffle.fwk.config.ConfigManager;
import shuffle.fwk.config.EntryType;
import shuffle.fwk.data.PkmType;
import shuffle.fwk.data.Species;
import shuffle.fwk.data.SpeciesPaint;
import shuffle.fwk.i18n.I18nUser;

/**
 * @author Andrew Meyers
 *
 */
public class ImageManager extends ConfigManager implements I18nUser {
   
   private static final Logger LOG = Logger.getLogger(ImageManager.class.getName());
   static {
      LOG.setLevel(Level.FINE);
   }
   
   private static final String KEY_TYPE_PATH_FORMAT = "TYPE_PATH_FORMAT";
   private static final String KEY_FROZEN_MASK = "FROZEN_MASK";
   private static final String TYPE_WIDTH = "TYPE_WIDTH";
   private static final String TYPE_HEIGHT = "TYPE_HEIGHT";
   private static final String ICON_WIDTH = "ICON_WIDTH";
   private static final String ICON_HEIGHT = "ICON_HEIGHT";
   private static final String CANDY_WIDTH = "CANDY_WIDTH";
   private static final String CANDY_HEIGHT = "CANDY_HEIGHT";
   private static final String KEY_IMAGE_SCALING = "IMAGE_SCALING";
   public static final String KEY_CANDY = "Candy";
   public static final String KEY_SKILL_BOOSTER = "Skill_Booster";
   private static final Collection<String> SPECIAL_NAMES = Arrays.asList(KEY_FROZEN_MASK, KEY_CANDY);
   
   private static final String KEY_IOE = "error.ioe";
   
   private static final int DEFAULT_ICON_WIDTH = 50;
   private static final int DEFAULT_ICON_HEIGHT = 50;
   private static final int DEFAULT_TYPE_WIDTH = 48;
   private static final int DEFAULT_TYPE_HEIGHT = 16;
   private static final int DEFAULT_CANDY_WIDTH = 16;
   private static final int DEFAULT_CANDY_HEIGHT = 16;
   
   private int iconWidth;
   private int iconHeight;
   private int typeWidth;
   private int typeHeight;
   private int specialWidth;
   private int specialHeight;
   
   private Map<String, ImageIcon> iconMap;
   private Map<String, ImageIcon> frozenIconMap;
   
   /**
    * @param loadPaths
    * @param writePaths
    */
   public ImageManager(List<String> loadPaths, List<String> writePaths, ConfigFactory factory) {
      super(loadPaths, writePaths, factory);
   }
   
   public ImageManager(ConfigManager manager) {
      super(manager);
   }
   
   @Override
   protected boolean shouldUpdate() {
      return true;
   }
   
   /**
    * @return the iconMap
    */
   private Map<String, ImageIcon> getIconMap() {
      if (iconMap == null) {
         iconMap = new HashMap<String, ImageIcon>();
      }
      return iconMap;
   }
   
   /**
    * @return the frozenIconMap
    */
   private Map<String, ImageIcon> getFrozenIconMap() {
      if (frozenIconMap == null) {
         frozenIconMap = new HashMap<String, ImageIcon>();
      }
      return frozenIconMap;
   }
   
   @Override
   public boolean loadFromConfig() {
      boolean changed = super.loadFromConfig();
      if (changed) {
         reloadIcons();
      }
      return changed;
   }
   
   @Override
   protected <T extends ConfigManager> void onCopyFrom(T manager) {
      if (manager instanceof ImageManager) {
         ImageManager other = (ImageManager) manager;
         for (String key : other.getIconMap().keySet()) {
            ImageIcon value = other.getIconMap().get(key);
            getIconMap().put(key, value);
         }
         for (String key : other.getFrozenIconMap().keySet()) {
            ImageIcon value = other.getFrozenIconMap().get(key);
            getFrozenIconMap().put(key, value);
         }
      } else {
         reloadIcons();
      }
   }
   
   /**
    * Loads icons for the given keys. This will first attempt to load resources, then also see if
    * the user has defined any file location for them. If they have then it will be used instead.
    * 
    * @param keys
    */
   public void loadIcons(Collection<String> keys) {
      if (keys == null || keys.isEmpty()) {
         return;
      }
      updateImgSizes();
      Map<String, BufferedImage> imageLoadingMap = new HashMap<String, BufferedImage>();
      // Checks resources
      for (String key : keys) {
         if (key == null || key.isEmpty()) {
            continue;
         }
         getIconMap().remove(key);
         String resource = getResourceValue(key);
         InputStream is = getResourceInputStream(resource);
         if (is != null) {
            BufferedImage img = getBufferedImage(is, resource);
            if (img != null) {
               imageLoadingMap.put(key, img);
            }
         }
      }
      // Checks user-defined files
      for (String key : getKeys(EntryType.FILE)) {
         File f = getFileValue(key);
         BufferedImage img = getBufferedImage(f);
         if (img != null) {
            imageLoadingMap.put(key, img);
         }
      }
      // Composes them into frozen and non-frozen icons
      BufferedImage frozenMask = imageLoadingMap.get(KEY_FROZEN_MASK);
      for (String key : imageLoadingMap.keySet()) {
         BufferedImage img = imageLoadingMap.get(key);
         if (SPECIAL_NAMES.contains(key)) {
            continue;
         } else {
            if (frozenMask != null) {
               getFrozenIconMap().put(key, getImageIconForImages(img, frozenMask, iconWidth, iconHeight));
            }
            getIconMap().put(key, getImageIconForImage(img, iconWidth, iconHeight));
         }
      }
      if (imageLoadingMap.containsKey(KEY_CANDY)) {
         BufferedImage img = imageLoadingMap.get(KEY_CANDY);
         getIconMap().put(KEY_CANDY, getImageIconForImage(img, specialWidth, specialHeight));
      }
      if (imageLoadingMap.containsKey(KEY_SKILL_BOOSTER)) {
         BufferedImage img = imageLoadingMap.get(KEY_SKILL_BOOSTER);
         getIconMap().put(KEY_SKILL_BOOSTER, getImageIconForImage(img, specialWidth, specialHeight));
      }
   }
   
   public void reloadIcons() {
      updateImgSizes();
      getIconMap().clear();
      loadIcons(getKeys(EntryType.RESOURCE));
      ConfigManager prefManager = getFactory().getPreferencesManager();
      String typeIconFormat = prefManager.getStringValue(KEY_TYPE_PATH_FORMAT, "img/types/%s.png");
      for (PkmType t : PkmType.values()) {
         String path = String.format(typeIconFormat, t.toString());
         InputStream is = getResourceInputStream(path);
         BufferedImage img = getBufferedImage(is, path);
         if (img != null) {
            getIconMap().put(t.toString(), getImageIconForImage(img, typeWidth, typeHeight));
         }
      }
   }
   
   private void updateImgSizes() {
      ConfigManager prefConfigManager = getFactory().getPreferencesManager();
      typeWidth = prefConfigManager.getIntegerValue(TYPE_WIDTH, DEFAULT_TYPE_WIDTH);
      typeHeight = prefConfigManager.getIntegerValue(TYPE_HEIGHT, DEFAULT_TYPE_HEIGHT);
      iconWidth = prefConfigManager.getIntegerValue(ICON_WIDTH, DEFAULT_ICON_WIDTH);
      iconHeight = prefConfigManager.getIntegerValue(ICON_HEIGHT, DEFAULT_ICON_HEIGHT);
      specialWidth = prefConfigManager.getIntegerValue(CANDY_WIDTH, DEFAULT_CANDY_WIDTH);
      specialHeight = prefConfigManager.getIntegerValue(CANDY_HEIGHT, DEFAULT_CANDY_HEIGHT);
      Integer imageScale = prefConfigManager.getIntegerValue(KEY_IMAGE_SCALING);
      if (imageScale != null && imageScale >= 1 && imageScale <= 10000) {
         float scale = imageScale.floatValue() / 100.0f;
         typeWidth *= scale;
         typeHeight *= scale;
         iconWidth *= scale;
         iconHeight *= scale;
         specialWidth *= scale;
         specialHeight *= scale;
      }
   }
   
   private ImageIcon getImageIconForImage(BufferedImage source, int width, int height) {
      return getImageIconForImages(source, null, width, height);
   }
   
   private ImageIcon getImageIconForImages(BufferedImage source, BufferedImage mask, int width, int height) {
      BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
      Graphics2D g = result.createGraphics();
      g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
      g.drawImage(source, 0, 0, width, height, null);
      if (mask != null) {
         g.drawImage(mask, 0, 0, width, height, null);
      }
      g.dispose();
      return new ImageIcon(result);
   }
   
   public ImageIcon getImageFor(Object o) {
      ImageIcon ret = null;
      if (o != null) {
         Species s = null;
         boolean frozen = false;
         boolean mega = false;
         if (o instanceof Species) {
            s = (Species) o;
         }
         if (o instanceof SpeciesPaint) {
            s = ((SpeciesPaint) o).getSpecies();
            frozen = ((SpeciesPaint) o).isFrozen();
            mega = ((SpeciesPaint) o).isMega();
         }
         if (s != null) {
            String name = mega ? s.getMegaName() : s.getName();
            if (frozen) {
               ret = getFrozenIconMap().get(name);
            } else {
               ret = getIconMap().get(name);
            }
         }
         if (ret == null) {
            ret = getIconMap().get(o.toString());
         }
      }
      return ret;
   }
   
   private BufferedImage getBufferedImage(File f) {
      BufferedImage img = null;
      try (FileInputStream is = new FileInputStream(f)) {
         img = ImageIO.read(is);
      } catch (IOException e) {
         LOG.log(Level.WARNING, getString(KEY_IOE, f.getPath()), e);
      }
      return img;
   }
   
   private BufferedImage getBufferedImage(InputStream is, String path) {
      BufferedImage img = null;
      try {
         img = ImageIO.read(is);
      } catch (IOException e) {
         LOG.log(Level.WARNING, getString(KEY_IOE, path), e);
      } finally {
         try {
            is.close();
         } catch (IOException e) {
            LOG.log(Level.WARNING, getString(KEY_IOE, path), e);
         }
      }
      return img;
   }
   
}
