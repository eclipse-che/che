/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.core.internal.preferences;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import org.eclipse.core.internal.preferences.Base64;
import org.eclipse.core.internal.preferences.ImmutableMap;
import org.eclipse.core.internal.preferences.PrefsMessages;
import org.eclipse.core.internal.preferences.SafeFileInputStream;
import org.eclipse.core.internal.preferences.SafeFileOutputStream;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IPreferenceNodeVisitor;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/** @author Evgen Vidolob */
public class ChePreferences implements IEclipsePreferences {

  protected static final String VERSION_KEY = "eclipse.preferences.version"; // $NON-NLS-1$
  protected static final String VERSION_VALUE = "1"; // $NON-NLS-1$
  protected static final String DOUBLE_SLASH = "//"; // $NON-NLS-1$
  protected static final String EMPTY_STRING = ""; // $NON-NLS-1$
  private static final String FALSE = "false"; // $NON-NLS-1$
  private static final String TRUE = "true"; // $NON-NLS-1$
  /** Protects write access to properties and children. */
  protected ImmutableMap properties = ImmutableMap.EMPTY;

  private final Object childAndPropertyLock = new Object();
  protected boolean dirty = false;
  private final String filePath;

  public ChePreferences(String filePath) {
    this.filePath = filePath;
  }

  /**
   * Loads the preference node. This method returns silently if the node does not exist in the
   * backing store (for example non-existent project).
   *
   * @throws BackingStoreException if the node exists in the backing store but it could not be
   *     loaded
   */
  protected void load() throws BackingStoreException {
    load(filePath);
  }

  protected void load(String location) throws BackingStoreException {
    if (location == null) {
      return;
    }
    Properties fromDisk = loadProperties(location);
    convertFromProperties(this, fromDisk, false);
  }

  protected static Properties loadProperties(String location) throws BackingStoreException {
    //        if (DEBUG_PREFERENCE_GENERAL)
    //            PrefsMessages.message("Loading preferences from file: " + location); //$NON-NLS-1$
    InputStream input = null;
    Properties result = new Properties();
    try {
      input = new SafeFileInputStream(new File(location));
      result.load(input);
    } catch (FileNotFoundException e) {
      // file doesn't exist but that's ok.
      //            if (DEBUG_PREFERENCE_GENERAL)
      //                PrefsMessages.message("Preference file does not exist: " + location);
      // //$NON-NLS-1$
      return result;
    } catch (IOException e) {
      //            String message = NLS.bind(PrefsMessages.preferences_loadException, location);
      //            log(new Status(IStatus.INFO, PrefsMessages.OWNER_NAME, IStatus.INFO, message,
      // e));
      throw new BackingStoreException(e.getMessage(), e);
    } finally {
      if (input != null)
        try {
          input.close();
        } catch (IOException e) {
          // ignore
        }
    }
    return result;
  }

  /*
   * Helper method to convert this node to a Properties file suitable
   * for persistence.
   */
  protected Properties convertToProperties(Properties result, String prefix)
      throws BackingStoreException {
    // add the key/value pairs from this node
    boolean addSeparator = prefix.length() != 0;
    // thread safety: copy reference in case of concurrent change
    ImmutableMap temp;
    synchronized (childAndPropertyLock) {
      temp = properties;
    }
    String[] keys = temp.keys();
    for (int i = 0, imax = keys.length; i < imax; i++) {
      String value = temp.get(keys[i]);
      if (value != null) result.put(encodePath(prefix, keys[i]), value);
    }
    //        // recursively add the child information
    //        IEclipsePreferences[] childNodes = getChildren(true);
    //        for (int i = 0; i < childNodes.length; i++) {
    //            ChePreferences child = (ChePreferences) childNodes[i];
    //            String fullPath = addSeparator ? prefix + PATH_SEPARATOR + child.name() :
    // child.name();
    //            child.convertToProperties(result, fullPath);
    //        }
    return result;
  }

  /*
   * Encode the given path and key combo to a form which is suitable for
   * persisting or using when searching. If the key contains a slash character
   * then we must use a double-slash to indicate the end of the
   * path/the beginning of the key.
   */
  public static String encodePath(String path, String key) {
    String result;
    int pathLength = path == null ? 0 : path.length();
    if (key.indexOf(IPath.SEPARATOR) == -1) {
      if (pathLength == 0) result = key;
      else result = path + IPath.SEPARATOR + key;
    } else {
      if (pathLength == 0) result = DOUBLE_SLASH + key;
      else result = path + DOUBLE_SLASH + key;
    }
    return result;
  }

  /*
   * Version 1 (current version)
   * path/key=value
   */
  protected static void convertFromProperties(
      ChePreferences node, Properties table, boolean notify) {
    String version = table.getProperty(VERSION_KEY);
    if (version == null || !VERSION_VALUE.equals(version)) {
      // ignore for now
    }
    table.remove(VERSION_KEY);
    for (Iterator i = table.keySet().iterator(); i.hasNext(); ) {
      String fullKey = (String) i.next();
      String value = table.getProperty(fullKey);
      if (value != null) {
        String[] splitPath = decodePath(fullKey);
        String path = splitPath[0];
        path = makeRelative(path);
        String key = splitPath[1];
        //                if (DEBUG_PREFERENCE_SET)
        //                    PrefsMessages.message("Setting preference: " + path + '/' + key + '='
        // + value); //$NON-NLS-1$
        // use internal methods to avoid notifying listeners
        ChePreferences childNode = (ChePreferences) node.internalNode(path, false, null);
        String oldValue = childNode.internalPut(key, value);
        //                // notify listeners if applicable
        //                if (notify && !value.equals(oldValue))
        //                    childNode.firePreferenceEvent(key, oldValue, value);
      }
    }
    //        PreferencesService.getDefault().shareStrings();
  }

  /**
   * Stores the given (key,value) pair, performing lazy initialization of the properties field if
   * necessary. Returns the old value for the given key, or null if no value existed.
   */
  protected String internalPut(String key, String newValue) {
    synchronized (childAndPropertyLock) {
      // illegal state if this node has been removed
      //            checkRemoved();
      String oldValue = properties.get(key);
      if (oldValue != null && oldValue.equals(newValue)) return oldValue;
      //            if (DEBUG_PREFERENCE_SET)
      //                PrefsMessages.message("Setting preference: " + absolutePath() + '/' + key +
      // '=' + newValue); //$NON-NLS-1$
      properties = properties.put(key, newValue);
      return oldValue;
    }
  }

  /** Implements the node(String) method, and optionally notifies listeners. */
  protected IEclipsePreferences internalNode(String path, boolean notify, Object context) {

    //         illegal state if this node has been removed
    //        checkRemoved();

    // short circuit this node
    //        if (path.length() == 0)
    // TODO only
    return this;
    //
    //        // if we have an absolute path use the root relative to
    //        // this node instead of the global root
    //        // in case we have a different hierarchy. (e.g. export)
    //        if (path.charAt(0) == IPath.SEPARATOR)
    //            return (IEclipsePreferences) calculateRoot().node(path.substring(1));
    //
    //        int index = path.indexOf(IPath.SEPARATOR);
    //        String key = index == -1 ? path : path.substring(0, index);
    //        boolean added = false;
    //        IEclipsePreferences child = getChild(key, context, true);
    //        if (child == null) {
    //            child = create(this, key, context);
    //            added = true;
    //        }
    //        // notify listeners if a child was added
    //        if (added && notify)
    //            fireNodeEvent(new NodeChangeEvent(this, child), true);
    //        return (IEclipsePreferences) child.node(index == -1 ? EMPTY_STRING :
    // path.substring(index + 1));
  }

  //    /**
  //     * Thread safe way to obtain a child for a given key. Returns the child
  //     * that matches the given key, or null if there is no matching child.
  //     */
  //    protected IEclipsePreferences getChild(String key, Object context, boolean create) {
  //        synchronized (childAndPropertyLock) {
  //            if (children == null)
  //                return null;
  //            Object value = children.get(key);
  //            if (value == null)
  //                return null;
  //            if (value instanceof IEclipsePreferences)
  //                return (IEclipsePreferences) value;
  //            // if we aren't supposed to create this node, then
  //            // just return null
  //            if (!create)
  //                return null;
  //        }
  //        return addChild(key, create(this, key, context));
  //    }

  private IEclipsePreferences calculateRoot() {
    IEclipsePreferences result = this;
    while (result.parent() != null) result = (IEclipsePreferences) result.parent();
    return result;
  }
  /*
   * Return a relative path
   */
  public static String makeRelative(String path) {
    String result = path;
    if (path == null) return EMPTY_STRING;
    if (path.length() > 0 && path.charAt(0) == IPath.SEPARATOR)
      result = path.length() == 0 ? EMPTY_STRING : path.substring(1);
    return result;
  }

  /*
   * Return a 2 element String array.
   * 	element 0 - the path
   * 	element 1 - the key
   * The path may be null.
   * The key is never null.
   */
  public static String[] decodePath(String fullPath) {
    String key = null;
    String path = null;

    // check to see if we have an indicator which tells us where the path ends
    int index = fullPath.indexOf(DOUBLE_SLASH);
    if (index == -1) {
      // we don't have a double-slash telling us where the path ends
      // so the path is up to the last slash character
      int lastIndex = fullPath.lastIndexOf(IPath.SEPARATOR);
      if (lastIndex == -1) {
        key = fullPath;
      } else {
        path = fullPath.substring(0, lastIndex);
        key = fullPath.substring(lastIndex + 1);
      }
    } else {
      // the child path is up to the double-slash and the key
      // is the string after it
      path = fullPath.substring(0, index);
      key = fullPath.substring(index + 2);
    }

    // adjust if we have an absolute path
    if (path != null)
      if (path.length() == 0) path = null;
      else if (path.charAt(0) == IPath.SEPARATOR) path = path.substring(1);

    return new String[] {path, key};
  }

  @Override
  public void addNodeChangeListener(INodeChangeListener listener) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void removeNodeChangeListener(INodeChangeListener listener) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void addPreferenceChangeListener(IPreferenceChangeListener listener) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void removePreferenceChangeListener(IPreferenceChangeListener listener) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void removeNode() throws BackingStoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public String name() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String absolutePath() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void flush() throws BackingStoreException {
    IEclipsePreferences toFlush = null;
    synchronized (childAndPropertyLock) {
      toFlush = internalFlush();
    }
    // if we aren't at the right level, then flush the appropriate node
    if (toFlush != null) toFlush.flush();
  }

  /*
   * Do the real flushing in a non-synchronized internal method so sub-classes
   * (mainly ProjectPreferences and ProfilePreferences) don't cause deadlocks.
   *
   * If this node is not responsible for persistence (a load level), then this method
   * returns the node that should be flushed. Returns null if this method performed
   * the flush.
   */
  protected IEclipsePreferences internalFlush() throws BackingStoreException {
    //         illegal state if this node has been removed
    //        checkRemoved();

    //        IEclipsePreferences loadLevel = null;//getLoadLevel();

    //        // if this node or a parent is not the load level, then flush the children
    //        if (loadLevel == null) {
    //            String[] childrenNames = childrenNames();
    //            for (int i = 0; i < childrenNames.length; i++)
    //                node(childrenNames[i]).flush();
    //            return null;
    //        }
    //
    //        // a parent is the load level for this node
    //        if (this != loadLevel)
    //            return loadLevel;

    // this node is a load level
    // any work to do?
    if (!dirty) return null;
    // remove dirty bit before saving, to ensure that concurrent
    // changes during save mark the store as dirty
    dirty = false;
    try {
      save();
    } catch (BackingStoreException e) {
      // mark it dirty again because the save failed
      dirty = true;
      throw e;
    }
    return null;
  }

  /**
   * Saves the preference node. This method returns silently if the node does not exist in the
   * backing store (for example non-existent project)
   *
   * @throws BackingStoreException if the node exists in the backing store but it could not be saved
   */
  protected void save() throws BackingStoreException {
    //        if (descriptor == null) {
    save(filePath);
    //        } else {
    //            descriptor.save(absolutePath(), convertToProperties(new Properties(), ""));
    // //$NON-NLS-1$
    //        }
  }

  protected void save(String location) throws BackingStoreException {
    if (location == null) {
      //            if (DEBUG_PREFERENCE_GENERAL)
      //                PrefsMessages.message("Unable to determine location of preference file for
      // node: " + absolutePath()); //$NON-NLS-1$
      return;
    }
    //        if (DEBUG_PREFERENCE_GENERAL)
    //            PrefsMessages.message("Saving preferences to file: " + location); //$NON-NLS-1$
    Properties table = convertToProperties(new SortedProperties(), EMPTY_STRING);
    if (table.isEmpty()) {
      File file = new File(location);
      // nothing to save. delete existing file if one exists.
      if (file.exists() && !file.delete()) {
        //                String message = NLS.bind(PrefsMessages.preferences_failedDelete,
        // location);
        ResourcesPlugin.log(
            new Status(
                IStatus.WARNING,
                PrefsMessages.OWNER_NAME,
                IStatus.WARNING,
                "preferences save failed, file was delete",
                null));
      }
      return;
    }
    table.put(VERSION_KEY, VERSION_VALUE);
    write(table, location);
  }

  /*
   * Helper method to persist a Properties object to the filesystem. We use this
   * helper so we can remove the date/timestamp that Properties#store always
   * puts in the file.
   */
  protected static void write(Properties properties, String location) throws BackingStoreException {
    // create the parent directories if they don't exist
    //        File parentFile = new File(location);
    //        if (parentFile == null)
    //            return;
    //        parentFile.mkdirs();

    OutputStream output = null;
    try {
      File file = new File(location);
      if (!file.exists()) {
        File parentFile = file.getParentFile();
        if (!parentFile.exists()) {
          parentFile.mkdirs();
        }
        file.createNewFile();
      }
      output = new SafeFileOutputStream(file);
      output.write(removeTimestampFromTable(properties).getBytes("UTF-8")); // $NON-NLS-1$
      output.flush();
    } catch (IOException e) {
      //            String message = NLS.bind(PrefsMessages.preferences_saveException, location);
      ResourcesPlugin.log(
          new Status(
              IStatus.ERROR,
              PrefsMessages.OWNER_NAME,
              IStatus.ERROR,
              "preferences_saveException",
              e));
      throw new BackingStoreException("preferences_saveException");
    } finally {
      if (output != null)
        try {
          output.close();
        } catch (IOException e) {
          // ignore
        }
    }
  }

  protected static String removeTimestampFromTable(Properties properties) throws IOException {
    // store the properties in a string and then skip the first line (date/timestamp)
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    try {
      properties.store(output, null);
    } finally {
      output.close();
    }
    String string = output.toString("UTF-8"); // $NON-NLS-1$
    String separator = System.getProperty("line.separator"); // $NON-NLS-1$
    return string.substring(string.indexOf(separator) + separator.length());
  }

  @Override
  public void sync() throws BackingStoreException {
    load();
    flush();
  }

  @Override
  public void put(String key, String newValue) {
    if (key == null || newValue == null) throw new NullPointerException();
    String oldValue = internalPut(key, newValue);
    if (!newValue.equals(oldValue)) {
      makeDirty();
      //            firePreferenceEvent(key, oldValue, newValue);
    }
  }

  protected void makeDirty() {
    //        EclipsePreferences node = this;
    //        while (node != null && !node.removed) {
    dirty = true;
    //            node = (EclipsePreferences) node.parent();
    //        }
  }

  @Override
  public String get(String key, String defaultValue) {
    String value = internalGet(key);
    return value == null ? defaultValue : value;
  }

  /** Returns the existing value at the given key, or null if no such value exists. */
  protected String internalGet(String key) {
    // throw NPE if key is null
    if (key == null) throw new NullPointerException();
    // illegal state if this node has been removed
    //        checkRemoved();
    String result;
    synchronized (childAndPropertyLock) {
      result = properties.get(key);
    }
    //        if (DEBUG_PREFERENCE_GET)
    //            PrefsMessages.message("Getting preference value: " + absolutePath() + '/' + key +
    // "->" + result); //$NON-NLS-1$ //$NON-NLS-2$
    return result;
  }

  @Override
  public void remove(String key) {
    String oldValue;
    synchronized (childAndPropertyLock) {
      // illegal state if this node has been removed
      //            checkRemoved();
      oldValue = properties.get(key);
      if (oldValue == null) return;
      properties = properties.removeKey(key);
    }
    makeDirty();
  }

  @Override
  public void clear() throws BackingStoreException {
    // illegal state if this node has been removed
    //        checkRemoved();
    // call each one separately (instead of Properties.clear) so
    // clients get change notification
    String[] keys;
    synchronized (childAndPropertyLock) {
      keys = properties.keys();
    }
    // don't synchronize remove call because it calls listeners
    for (int i = 0; i < keys.length; i++) remove(keys[i]);
    makeDirty();
  }

  @Override
  public void putInt(String key, int value) {
    if (key == null) throw new NullPointerException();
    String newValue = Integer.toString(value);
    String oldValue = internalPut(key, newValue);
    if (!newValue.equals(oldValue)) {
      makeDirty();
      //            firePreferenceEvent(key, oldValue, newValue);
    }
  }

  @Override
  public int getInt(String key, int defaultValue) {
    String value = internalGet(key);
    int result = defaultValue;
    if (value != null)
      try {
        result = Integer.parseInt(value);
      } catch (NumberFormatException e) {
        // use default
      }
    return result;
  }

  @Override
  public void putLong(String key, long value) {
    if (key == null) throw new NullPointerException();
    String newValue = Long.toString(value);
    String oldValue = internalPut(key, newValue);
    if (!newValue.equals(oldValue)) {
      makeDirty();
      //            firePreferenceEvent(key, oldValue, newValue);
    }
  }

  @Override
  public long getLong(String key, long defaultValue) {
    String value = internalGet(key);
    long result = defaultValue;
    if (value != null)
      try {
        result = Long.parseLong(value);
      } catch (NumberFormatException e) {
        // use default
      }
    return result;
  }

  @Override
  public void putBoolean(String key, boolean value) {
    if (key == null) throw new NullPointerException();
    String newValue = value ? TRUE : FALSE;
    String oldValue = internalPut(key, newValue);
    if (!newValue.equals(oldValue)) {
      makeDirty();
      //            firePreferenceEvent(key, oldValue, newValue);
    }
  }

  @Override
  public boolean getBoolean(String key, boolean defaultValue) {
    String value = internalGet(key);
    return value == null ? defaultValue : TRUE.equalsIgnoreCase(value);
  }

  @Override
  public void putFloat(String key, float value) {
    if (key == null) throw new NullPointerException();
    String newValue = Float.toString(value);
    String oldValue = internalPut(key, newValue);
    if (!newValue.equals(oldValue)) {
      makeDirty();
      //            firePreferenceEvent(key, oldValue, newValue);
    }
  }

  @Override
  public float getFloat(String key, float defaultValue) {
    String value = internalGet(key);
    float result = defaultValue;
    if (value != null)
      try {
        result = Float.parseFloat(value);
      } catch (NumberFormatException e) {
        // use default
      }
    return result;
  }

  @Override
  public void putDouble(String key, double value) {
    if (key == null) throw new NullPointerException();
    String newValue = Double.toString(value);
    String oldValue = internalPut(key, newValue);
    if (!newValue.equals(oldValue)) {
      makeDirty();
      //            firePreferenceEvent(key, oldValue, newValue);
    }
  }

  @Override
  public double getDouble(String key, double defaultValue) {
    String value = internalGet(key);
    double result = defaultValue;
    if (value != null)
      try {
        result = Double.parseDouble(value);
      } catch (NumberFormatException e) {
        // use default
      }
    return result;
  }

  @Override
  public void putByteArray(String key, byte[] value) {
    if (key == null || value == null) throw new NullPointerException();
    String newValue = new String(Base64.encode(value));
    String oldValue = internalPut(key, newValue);
    if (!newValue.equals(oldValue)) {
      makeDirty();
      //            firePreferenceEvent(key, oldValue, newValue);
    }
  }

  @Override
  public byte[] getByteArray(String key, byte[] defaultValue) {
    String value = internalGet(key);
    return value == null ? defaultValue : Base64.decode(value.getBytes());
  }

  @Override
  public String[] keys() throws BackingStoreException {
    // illegal state if this node has been removed
    synchronized (childAndPropertyLock) {
      //            checkRemoved();
      return properties.keys();
    }
  }

  @Override
  public String[] childrenNames() throws BackingStoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Preferences parent() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Preferences node(String path) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean nodeExists(String pathName) throws BackingStoreException {
    return false;
  }

  @Override
  public void accept(IPreferenceNodeVisitor visitor) throws BackingStoreException {
    throw new UnsupportedOperationException();
  }

  protected class SortedProperties extends Properties {

    private static final long serialVersionUID = 1L;

    public SortedProperties() {
      super();
    }

    /* (non-Javadoc)
     * @see java.util.Hashtable#keys()
     */
    public synchronized Enumeration keys() {
      TreeSet set = new TreeSet();
      for (Enumeration e = super.keys(); e.hasMoreElements(); ) set.add(e.nextElement());
      return Collections.enumeration(set);
    }

    /* (non-Javadoc)
     * @see java.util.Hashtable#entrySet()
     */
    public Set entrySet() {
      TreeSet set =
          new TreeSet(
              new Comparator() {
                public int compare(Object e1, Object e2) {
                  String s1 = (String) ((Map.Entry) e1).getKey();
                  String s2 = (String) ((Map.Entry) e2).getKey();
                  return s1.compareTo(s2);
                }
              });
      for (Iterator i = super.entrySet().iterator(); i.hasNext(); ) set.add(i.next());
      return set;
    }
  }
}
