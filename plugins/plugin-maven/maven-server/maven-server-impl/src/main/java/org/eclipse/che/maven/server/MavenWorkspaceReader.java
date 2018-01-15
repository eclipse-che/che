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
package org.eclipse.che.maven.server;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.repository.WorkspaceReader;
import org.eclipse.aether.repository.WorkspaceRepository;
import org.eclipse.che.maven.data.MavenKey;
import org.eclipse.che.maven.data.MavenWorkspaceCache;

/** @author Evgen Vidolob */
public class MavenWorkspaceReader implements WorkspaceReader {

  private final MavenWorkspaceCache workspaceCache;
  private final WorkspaceRepository repository;

  public MavenWorkspaceReader(MavenWorkspaceCache workspaceCache) {
    this.workspaceCache = workspaceCache;
    repository = new WorkspaceRepository();
  }

  @Override
  public WorkspaceRepository getRepository() {
    return repository;
  }

  @Override
  public File findArtifact(Artifact artifact) {
    MavenWorkspaceCache.Entry entry =
        workspaceCache.findEntry(
            new MavenKey(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion()));
    if (entry == null) {
      return null;
    }
    return entry.getFile(artifact.getExtension());
  }

  @Override
  public List<String> findVersions(Artifact artifact) {

    return workspaceCache
        .getAllKeys()
        .stream()
        .filter(key -> Objects.equals(key.getArtifactId(), artifact.getArtifactId()))
        .filter(key -> Objects.equals(key.getGroupId(), artifact.getGroupId()))
        .filter(key -> key.getVersion() != null)
        .map(MavenKey::getVersion)
        .collect(Collectors.toList());
  }
}
