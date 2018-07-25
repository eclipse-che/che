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
import java.util.List;
import java.util.Objects;

/**
 * Data class for maven resource.
 *
 * @author Evgen Vidolob
 */
public class MavenResource implements Serializable {
  private static final long serialVersionUID = 1L;

  private final String directory;
  private final boolean filtered;
  private final String targetPath;
  private final List<String> includes;
  private final List<String> excludes;

  public MavenResource(
      String directory,
      boolean filtered,
      String targetPath,
      List<String> includes,
      List<String> excludes) {
    this.directory = directory;
    this.filtered = filtered;
    this.targetPath = targetPath;
    this.includes = includes;
    this.excludes = excludes;
  }

  public String getDirectory() {
    return directory;
  }

  public boolean isFiltered() {
    return filtered;
  }

  public String getTargetPath() {
    return targetPath;
  }

  public List<String> getIncludes() {
    return includes;
  }

  public List<String> getExcludes() {
    return excludes;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    MavenResource that = (MavenResource) o;
    return filtered == that.filtered
        && Objects.equals(directory, that.directory)
        && Objects.equals(targetPath, that.targetPath)
        && Objects.equals(includes, that.includes)
        && Objects.equals(excludes, that.excludes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(directory, filtered, targetPath, includes, excludes);
  }
}
