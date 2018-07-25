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

import java.io.File;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Objects;

/**
 * Data class form maven project problem.
 *
 * @author Evgen Vidolob
 */
public class MavenProjectProblem implements Serializable {
  private static final long serialVersionUID = 1L;

  private final String path;
  private final String description;
  private final MavenProblemType type;

  public MavenProjectProblem(String path, String description, MavenProblemType type) {
    this.path = path;
    this.description = description;
    this.type = type;
  }

  public String getPath() {
    return path;
  }

  public String getDescription() {
    return description;
  }

  public MavenProblemType getType() {
    return type;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    MavenProjectProblem that = (MavenProjectProblem) o;
    return Objects.equals(path, that.path)
        && Objects.equals(description, that.description)
        && Objects.equals(type, that.type);
  }

  @Override
  public int hashCode() {
    return Objects.hash(path, description, type);
  }

  @Override
  public String toString() {
    return path + ":" + description + ":" + type;
  }

  public static MavenProjectProblem newStructureProblem(String path, String description) {
    return newProblem(path, description, MavenProblemType.STRUCTURE);
  }

  public static MavenProjectProblem newSyntaxProblem(String path, MavenProblemType type) {
    return newProblem(
        path, MessageFormat.format("''{0} has syntax errors''", new File(path).getName()), type);
  }

  public static MavenProjectProblem newProblem(
      String path, String description, MavenProblemType type) {
    return new MavenProjectProblem(path, description, type);
  }
}
