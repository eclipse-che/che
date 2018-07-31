/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.core.utils;

/**
 * Platform utils.
 *
 * @author Vlad Zhukovskyi
 */
public class PlatformUtils {

  private static final String OS = System.getProperty("os.name").toLowerCase();
  private static final boolean IS_MAC = OS.startsWith("mac");

  private PlatformUtils() {}

  /**
   * Returns whether current operation system is Mac OS or not.
   *
   * @return {@code true} is current operation system is Mac os
   */
  public static boolean isMac() {
    return IS_MAC;
  }
}
