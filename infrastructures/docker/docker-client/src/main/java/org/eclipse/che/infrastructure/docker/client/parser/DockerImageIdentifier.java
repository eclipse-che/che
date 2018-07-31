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
package org.eclipse.che.infrastructure.docker.client.parser;

import static java.util.Objects.requireNonNull;

import java.util.Objects;
import javax.validation.constraints.NotNull;
import org.eclipse.che.commons.annotation.Nullable;

/**
 * Describes specific docker image.
 *
 * <p>Includes:
 *
 * <ul>
 *   <li>registry (e.g. registry-host.com:8080 or localhost)
 *   <li>repository (e.g. repo_part1/repo_part2/repo_part3)
 *   <li>tag
 *   <li>digest
 * </ul>
 *
 * Example: <br>
 * garagatyi/my_image <br>
 * ubuntu <br>
 * ubuntu:14.04 <br>
 * my_private_registry:15800/my_image1:latest <br>
 * my_private_registry:15800/my_image1@sha256:6b019df8c73bb42e606225ef935760b9c428521eba4ad2519ef3ff4cdb3dbd69
 *
 * @author Alexander Garagatyi
 */
public class DockerImageIdentifier {
  private final String registry;
  private final String repository;
  private final String tag;
  private final String digest;

  private DockerImageIdentifier(String registry, String repository, String tag, String digest) {
    this.registry = registry;
    this.repository = repository;
    this.tag = tag;
    this.digest = digest;
  }

  public static DockerImageIdentifierBuilder builder() {
    return new DockerImageIdentifierBuilder();
  }

  @Nullable
  public String getRegistry() {
    return registry;
  }

  public String getRepository() {
    return repository;
  }

  @Nullable
  public String getTag() {
    return tag;
  }

  @Nullable
  public String getDigest() {
    return digest;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof DockerImageIdentifier)) return false;
    DockerImageIdentifier that = (DockerImageIdentifier) o;
    return Objects.equals(registry, that.registry)
        && Objects.equals(repository, that.repository)
        && Objects.equals(tag, that.tag)
        && Objects.equals(digest, that.digest);
  }

  @Override
  public int hashCode() {
    return Objects.hash(registry, repository, tag, digest);
  }

  @Override
  public String toString() {
    return "DockerImageIdentifier{"
        + "registry='"
        + registry
        + '\''
        + ", repository='"
        + repository
        + '\''
        + ", tag='"
        + tag
        + '\''
        + ", digest='"
        + digest
        + '\''
        + '}';
  }

  public static class DockerImageIdentifierBuilder {
    private String registry;
    private String repository;
    private String tag;
    private String digest;

    public DockerImageIdentifier build() {
      return new DockerImageIdentifier(registry, repository, tag, digest);
    }

    public DockerImageIdentifierBuilder setRegistry(String registry) {
      this.registry = registry;
      return this;
    }

    public DockerImageIdentifierBuilder setRepository(@NotNull String repository) {
      requireNonNull(repository);
      this.repository = repository;
      return this;
    }

    public DockerImageIdentifierBuilder setTag(String tag) {
      this.tag = tag;
      return this;
    }

    public DockerImageIdentifierBuilder setDigest(String digest) {
      this.digest = digest;
      return this;
    }

    private DockerImageIdentifierBuilder() {}
  }
}
