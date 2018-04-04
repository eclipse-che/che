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

import java.io.File;
import java.io.Serializable;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * Data class for org.apache.maven.artifact.Artifact
 *
 * @author Evgen Vidolob
 */
public class MavenArtifact implements Serializable {

  public static final long serialVersionUID = 1L;

  private final String groupId;
  private final String artifactId;
  private final String version;
  private final String baseVersion;
  private final String type;
  private final String classifier;
  private final String scope;
  private final boolean optional;
  private final String extension;
  private final File file;
  private final File localRepo;
  private final boolean resolved;
  private final boolean stubbed;

  public MavenArtifact(
      String groupId,
      String artifactId,
      String version,
      String baseVersion,
      String type,
      String classifier,
      String scope,
      boolean optional,
      String extension,
      File file,
      File localRepo,
      boolean resolved,
      boolean stubbed) {
    this.groupId = groupId;
    this.artifactId = artifactId;
    this.version = version;
    this.baseVersion = baseVersion;
    this.type = type;
    this.classifier = classifier;
    this.scope = scope;
    this.optional = optional;
    this.extension = extension;
    this.file = file;
    this.localRepo = localRepo;
    this.resolved = resolved;
    this.stubbed = stubbed;
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

  public String getBaseVersion() {
    return baseVersion;
  }

  public String getType() {
    return type;
  }

  public String getClassifier() {
    return classifier;
  }

  public String getScope() {
    return scope;
  }

  public boolean isOptional() {
    return optional;
  }

  public String getExtension() {
    return extension;
  }

  public boolean isResolved() {
    if (resolved && !stubbed) {
      return file.exists();
    }

    return false;
  }

  public File getFile() {
    return file;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    MavenArtifact that = (MavenArtifact) o;
    return Objects.equals(groupId, that.groupId)
        && Objects.equals(artifactId, that.artifactId)
        && Objects.equals(version, that.version)
        && Objects.equals(baseVersion, that.baseVersion)
        && Objects.equals(type, that.type)
        && Objects.equals(classifier, that.classifier)
        && Objects.equals(scope, that.scope)
        && Objects.equals(extension, that.extension)
        && Objects.equals(file, that.file);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        groupId, artifactId, version, baseVersion, type, classifier, scope, extension, file);
  }

  public String getDisplayString() {
    StringJoiner joiner = new StringJoiner(":");
    joiner.setEmptyValue("<unknown>");
    joiner.add(groupId).add(artifactId).add(version);
    return joiner.toString();
  }
}
