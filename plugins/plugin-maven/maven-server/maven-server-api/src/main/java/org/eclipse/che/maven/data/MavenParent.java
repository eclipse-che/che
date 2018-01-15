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
