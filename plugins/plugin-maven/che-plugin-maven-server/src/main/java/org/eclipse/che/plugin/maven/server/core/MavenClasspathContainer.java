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
package org.eclipse.che.plugin.maven.server.core;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;

/** @author Evgen Vidolob */
public class MavenClasspathContainer implements IClasspathContainer {
  public static final String CONTAINER_ID = "org.eclipse.che.MAVEN2_CLASSPATH_CONTAINER";
  private IClasspathEntry[] entries;

  public MavenClasspathContainer(IClasspathEntry[] entries) {
    this.entries = entries;
  }

  @Override
  public IClasspathEntry[] getClasspathEntries() {
    return entries;
  }

  @Override
  public String getDescription() {
    return "Maven Dependencies";
  }

  @Override
  public int getKind() {
    return IClasspathContainer.K_APPLICATION;
  }

  @Override
  public IPath getPath() {
    return new Path(CONTAINER_ID);
  }
}
