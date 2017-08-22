/**
 * ***************************************************************************** Copyright (c) 2005,
 * 2008 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.ltk.internal.core.refactoring;

import java.text.MessageFormat;

/**
 * Helper class to format message strings.
 *
 * @since 3.1
 */
public class Messages {

  public static String format(String message, Object object) {
    return MessageFormat.format(message, new Object[] {object});
  }

  public static String format(String message, Object[] objects) {
    return MessageFormat.format(message, objects);
  }

  private Messages() {
    // Not for instantiation
  }
}
