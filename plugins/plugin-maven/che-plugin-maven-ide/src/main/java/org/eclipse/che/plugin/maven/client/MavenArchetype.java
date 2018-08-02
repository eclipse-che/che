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
package org.eclipse.che.plugin.maven.client;

import javax.validation.constraints.NotNull;
import org.eclipse.che.commons.annotation.Nullable;

/**
 * Describes the Maven archetype.
 *
 * @author Artem Zatsarynnyi
 */
public class MavenArchetype {
  private final String groupId;
  private final String artifactId;
  private final String version;
  private final String repository;

  /**
   * Create archetype description with the specified common properties' values.
   *
   * <p>External repository may be specified to search for an archetype that are not available on
   * Maven central repository.
   *
   * @param groupId the archetype's groupId
   * @param artifactId the archetype's artifactId
   * @param version the archetype's version
   * @param repository the repository where need to find the archetype
   */
  public MavenArchetype(
      @NotNull String groupId,
      @NotNull String artifactId,
      @NotNull String version,
      @Nullable String repository) {
    this.groupId = groupId;
    this.artifactId = artifactId;
    this.version = version;
    this.repository = repository;
  }

  @NotNull
  public String getGroupId() {
    return groupId;
  }

  @NotNull
  public String getArtifactId() {
    return artifactId;
  }

  @NotNull
  public String getVersion() {
    return version;
  }

  @Nullable
  public String getRepository() {
    return repository;
  }

  @Override
  public String toString() {
    return groupId + ':' + artifactId + ':' + version;
  }
}
