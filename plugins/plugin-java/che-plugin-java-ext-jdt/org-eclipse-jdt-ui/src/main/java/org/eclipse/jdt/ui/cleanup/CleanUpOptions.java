/**
 * ***************************************************************************** Copyright (c) 2008,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.ui.cleanup;

import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.internal.corext.fix.CleanUpConstants;

/**
 * Allows to set and retrieve clean up settings for given options keys.
 *
 * @since 3.5
 * @noextend This class is not intended to be subclassed by clients.
 */
public class CleanUpOptions {

  private final Map<String, String> fOptions;

  /** True value */
  public static final String TRUE = "true"; // $NON-NLS-1$

  /** False value */
  public static final String FALSE = "false"; // $NON-NLS-1$

  /**
   * Creates a new CleanUpOptions instance with the given options.
   *
   * @param options map that maps clean ups keys (<code>String</code>) to a non-<code>null</code>
   *     string value
   */
  protected CleanUpOptions(Map<String, String> options) {
    fOptions = options;
  }

  /** Creates a new instance. */
  public CleanUpOptions() {
    fOptions = new Hashtable<String, String>();
  }

  /**
   * Tells whether the option with the given <code>key</code> is enabled.
   *
   * @param key the name of the option
   * @return <code>true</code> if enabled, <code>false</code> if not enabled or unknown key
   * @throws IllegalArgumentException if the key is <code>null</code>
   * @see CleanUpConstants
   */
  public boolean isEnabled(String key) {
    Assert.isLegal(key != null);
    Object value = fOptions.get(key);
    return CleanUpOptions.TRUE == value || CleanUpOptions.TRUE.equals(value);
  }

  /**
   * Returns the value for the given key.
   *
   * @param key the key of the value
   * @return the value associated with the key
   * @throws IllegalArgumentException if the key is null or unknown
   */
  public String getValue(String key) {
    Assert.isLegal(key != null);
    String value = fOptions.get(key);
    Assert.isLegal(value != null);
    return value;
  }

  /**
   * Sets the option for the given key to the given value.
   *
   * @param key the name of the option to set
   * @param value the value of the option
   * @throws IllegalArgumentException if the key is <code>null</code>
   * @see CleanUpOptions#TRUE
   * @see CleanUpOptions#FALSE
   */
  public void setOption(String key, String value) {
    Assert.isLegal(key != null);
    Assert.isLegal(value != null);
    fOptions.put(key, value);
  }

  /**
   * Returns an unmodifiable set of all known keys.
   *
   * @return an unmodifiable set of all keys
   */
  public Set<String> getKeys() {
    return Collections.unmodifiableSet(fOptions.keySet());
  }
}
