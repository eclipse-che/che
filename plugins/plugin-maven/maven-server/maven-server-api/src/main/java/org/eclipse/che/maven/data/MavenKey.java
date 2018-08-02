/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.maven.data;

import java.io.Serializable;
import java.util.Objects;

/**
 * Data class for maven artifact id, contains group id, artifact id and version.
 *
 * @author Evgen Vidolob
 */
public class MavenKey implements Serializable {
  private static final long serialVersionUID = 1L;
  private final String groupId;
  private final String artifactId;
  private final String version;

  public MavenKey(String groupId, String artifactId, String version) {
    this.groupId = groupId;
    this.artifactId = artifactId;
    this.version = version;
  }

  public String getGroupId() {
    return groupId;
  }

  public String getArtifactId() {
    return artifactId;
  }

  public String getVersion() {
    return version;
  }

  @Override
  public int hashCode() {
    return Objects.hash(groupId, artifactId, version);
  }

  public boolean equals(String groupId, String artifactId) {
    if (this.groupId != null && !this.groupId.equals(groupId)) {
      return false;
    }

    if (this.artifactId != null && !this.artifactId.equals(artifactId)) {
      return false;
    }

    return true;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    MavenKey mavenKey = (MavenKey) o;
    return Objects.equals(groupId, mavenKey.groupId)
        && Objects.equals(artifactId, mavenKey.artifactId)
        && Objects.equals(version, mavenKey.version);
  }

  @Override
  public String toString() {
    return "MavenKey{"
        + "groupId='"
        + groupId
        + '\''
        + ", artifactId='"
        + artifactId
        + '\''
        + ", version='"
        + version
        + '\''
        + '}';
  }
}
