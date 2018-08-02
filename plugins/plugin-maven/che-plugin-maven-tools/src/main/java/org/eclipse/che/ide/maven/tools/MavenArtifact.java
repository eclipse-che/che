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
package org.eclipse.che.ide.maven.tools;

/** @author andrew00x */
public class MavenArtifact {
  private String groupId;
  private String artifactId;
  private String type;
  private String classifier;
  private String version;
  private String scope;

  public MavenArtifact(
      String groupId,
      String artifactId,
      String type,
      String classifier,
      String version,
      String scope) {
    this.groupId = groupId;
    this.artifactId = artifactId;
    this.type = type;
    this.classifier = classifier;
    this.version = version;
    this.scope = scope;
  }

  public MavenArtifact() {}

  public String getGroupId() {
    return groupId;
  }

  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  public String getArtifactId() {
    return artifactId;
  }

  public void setArtifactId(String artifactId) {
    this.artifactId = artifactId;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getClassifier() {
    return classifier;
  }

  public void setClassifier(String classifier) {
    this.classifier = classifier;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getScope() {
    return scope;
  }

  public void setScope(String scope) {
    this.scope = scope;
  }

  @Override
  public String toString() {
    return "MavenArtifact{"
        + "groupId='"
        + groupId
        + '\''
        + ", artifactId='"
        + artifactId
        + '\''
        + ", type='"
        + type
        + '\''
        + ", classifier='"
        + classifier
        + '\''
        + ", version='"
        + version
        + '\''
        + ", scope='"
        + scope
        + '\''
        + '}';
  }
}
