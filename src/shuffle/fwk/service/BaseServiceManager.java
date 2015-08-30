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

package shuffle.fwk.service;

import java.awt.Frame;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.JDialog;

/**
 * This manages services and their users. All service creation and destruction should progress
 * through this class alone.
 * 
 * @author Andrew Meyers
 *
 */
public class BaseServiceManager {
   /** The logger for this {@link #BaseServiceManager()} */
   private static final Logger LOG = Logger.getLogger(BaseServiceManager.class.getName());
   /** The service to user map, used for obtaining the service user from within a service. */
   private static final Map<Service<?>, Object> SERVICE_TO_USER_MAP = new HashMap<Service<?>, Object>();
   /** The id to service map, used as a registration. */
   private static final Map<Long, Service<?>> ID_TO_SERVICE_MAP = new HashMap<Long, Service<?>>();
   
   /** Simply here to avoid any instantiation. */
   private BaseServiceManager() {
      throw new IllegalAccessError("Cannot instantiate a BaseServiceManager.");
   }
   
   /**
    * Gets a new service for the given class and user.
    * 
    * @param c
    *           The class of service to use.
    * @param user
    *           The user for the new service.
    * @param owner
    *           The frame owner for the service
    * @return The new service instance with the given user as its sole user. This does not call
    *         {@link Service#setupGUI(Frame)}, {@link Service#updateGUI()} or
    *         {@link Service#launch()}. If there is a problem this might return null.
    */
   public static <U extends Object, S extends Service<U>> boolean launchServiceByClass(Class<? extends S> c, U user,
         Frame owner) {
      boolean success = false;
      S ret = null;
      for (Service<?> s : SERVICE_TO_USER_MAP.keySet()) {
         if (c.isInstance(s)) {
            s.pullToFront();
            return true;
         }
      }
      try {
         ret = c.newInstance();
         SERVICE_TO_USER_MAP.put(ret, user);
         ret.setupGUI(owner);
         ret.launch();
         success = true;
      } catch (InstantiationException | IllegalAccessException e) {
         LOG.severe("Problem creating a new service by class. " + e.getMessage());
      }
      return success;
   }
   
   /**
    * Gets the user for the given service. If there is a problem this might return null.
    * 
    * @param service
    *           The service to search with.
    * @param userClass
    *           The user class that it needs to be able to cast into.
    * @return The user, if the registered user for the given service is of the class userClass. Null
    *         if otherwise.
    */
   public static <S extends Service<U>, U extends Object> U getUser(S service, Class<U> userClass) {
      Object user = SERVICE_TO_USER_MAP.get(service);
      U ret = null;
      try {
         ret = userClass.cast(user);
      } catch (ClassCastException e) {
         LOG.severe("Cannot retrieve service user." + e.getMessage());
      }
      return ret;
   }
   
   /**
    * Gets a new id, randomly chosen, which is unique. The id is then reserved, and not released
    * until the caller releases it via #deregisterId(long)
    * 
    * @return
    */
   public static Long getNewId(Service<?> s) {
      Long id = Double.doubleToLongBits(Math.random());
      while (ID_TO_SERVICE_MAP.containsKey(id)) {
         id = Double.doubleToLongBits(Math.random());
      }
      ID_TO_SERVICE_MAP.put(id, s);
      return id;
   }
   
   /**
    * Unregisters the given id. All traces of the service are removed.
    * 
    * @param id
    *           The id of the service to be removed.
    * @return True if successful, false if otherwise.
    */
   public static boolean unregisterService(long id) {
      boolean exitStatus = false;
      if (ID_TO_SERVICE_MAP.get(id) == null) { // If not registered or was unregistered.
         LOG.warning("Cannot deregister an unregistered service.");
      } else {
         Service<?> service = ID_TO_SERVICE_MAP.put(id, null);
         if (service != null) {
            SERVICE_TO_USER_MAP.remove(service);
         }
         exitStatus = true;
      }
      return exitStatus;
   }
   
   /**
    * Obtains an unmodifiable view of all dialogs for registered services.
    * 
    * @return An unmodifiable collection of non-null JDialogs
    */
   public static Collection<JDialog> getAllDialogs() {
      ArrayList<JDialog> dialogs = new ArrayList<JDialog>(SERVICE_TO_USER_MAP.size());
      for (Service<?> s : SERVICE_TO_USER_MAP.keySet()) {
         if (s == null) {
            continue;
         }
         JDialog d = s.getDialog();
         if (d != null) {
            dialogs.add(d);
         }
      }
      return Collections.unmodifiableCollection(dialogs);
   }

}
