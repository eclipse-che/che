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
package org.eclipse.che.maven.data;

import java.io.Serializable;

/**
 * Data class for org.apache.maven.model.Parent
 *
 * @author Evgen Vidolob
 */
public class MavenParent implements Serializable {
  private static final long serialVersionUID = 1L;

  private final MavenKey mavenKey;
  private final String relativePath;

  public MavenParent(MavenKey mavenKey, String relativePath) {
    this.mavenKey = mavenKey;
    this.relativePath = relativePath;
  }

  public MavenKey getMavenKey() {
    return mavenKey;
  }

  public String getRelativePath() {
    return relativePath;
  }
}
