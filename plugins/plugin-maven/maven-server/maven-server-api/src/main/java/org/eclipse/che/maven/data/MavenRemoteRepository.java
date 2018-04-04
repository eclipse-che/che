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
import java.util.Objects;

/**
 * Data class for org.eclipse.aether.repository.RemoteRepository
 *
 * @author Evgen Vidolob
 */
public class MavenRemoteRepository implements Serializable {
  private static final long serialVersionUID = 1L;

  private final String id;
  private final String name;
  private final String url;
  private final String layout;
  private final MavenRepositoryPolicy releasesPolicy;
  private final MavenRepositoryPolicy snapshotsPolicy;

  public MavenRemoteRepository(
      String id,
      String name,
      String url,
      String layout,
      MavenRepositoryPolicy releasesPolicy,
      MavenRepositoryPolicy snapshotsPolicy) {
    this.id = id;
    this.name = name;
    this.url = url;
    this.layout = layout;
    this.releasesPolicy = releasesPolicy;
    this.snapshotsPolicy = snapshotsPolicy;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getUrl() {
    return url;
  }

  public String getLayout() {
    return layout;
  }

  public MavenRepositoryPolicy getReleasesPolicy() {
    return releasesPolicy;
  }

  public MavenRepositoryPolicy getSnapshotsPolicy() {
    return snapshotsPolicy;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    MavenRemoteRepository that = (MavenRemoteRepository) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
