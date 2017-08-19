/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2008 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.che.jface.text.templates.persistence;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/** @since 3.0 */
class TemplatePersistenceMessages {

  private static final String RESOURCE_BUNDLE = TemplatePersistenceMessages.class.getName();
  private static ResourceBundle fgResourceBundle = ResourceBundle.getBundle(RESOURCE_BUNDLE);

  private TemplatePersistenceMessages() {}

  public static String getString(String key) {
    try {
      return fgResourceBundle.getString(key);
    } catch (MissingResourceException e) {
      return '!' + key + '!';
    }
  }

  public static String getFormattedString(String key, Object arg) {
    return MessageFormat.format(getString(key), arg);
  }

  public static String getFormattedString(String key, Object[] args) {
    return MessageFormat.format(getString(key), args);
  }
}
