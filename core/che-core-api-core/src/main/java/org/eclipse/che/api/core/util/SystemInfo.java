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
package org.eclipse.che.api.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides information about operating system.
 *
 * @author <a href="mailto:aparfonov@codenvy.com">Andrey Parfonov</a>
 */
public class SystemInfo {
  private static final Logger LOG = LoggerFactory.getLogger(SystemInfo.class);

  public static final String OS = System.getProperty("os.name").toLowerCase();
  private static final boolean linux = OS.startsWith("linux");
  private static final boolean mac = OS.startsWith("mac");
  private static final boolean windows = OS.startsWith("windows");
  private static final boolean unix = !windows;

  public static boolean isLinux() {
    return linux;
  }

  public static boolean isWindows() {
    return windows;
  }

  public static boolean isMacOS() {
    return mac;
  }

  public static boolean isUnix() {
    return unix;
  }

  private SystemInfo() {}
}
