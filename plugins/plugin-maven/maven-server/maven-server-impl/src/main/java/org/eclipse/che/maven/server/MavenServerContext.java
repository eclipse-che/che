/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.maven.server;

/**
 * Maven server context, contains some top level configured components.
 *
 * @author Evgen Vidolob
 */
public class MavenServerContext {
  private static MavenServerLogger logger;
  private static MavenServerDownloadListener listener;

  public static MavenServerLogger getLogger() {
    return logger;
  }

  public static MavenServerDownloadListener getListener() {
    return listener;
  }

  public static void setLoggerAndListener(
      MavenServerLogger logger, MavenServerDownloadListener listener) {
    MavenServerContext.logger = logger;
    MavenServerContext.listener = listener;
  }
}
