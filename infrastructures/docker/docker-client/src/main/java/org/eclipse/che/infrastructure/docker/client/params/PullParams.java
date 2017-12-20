/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.infrastructure.docker.client.params;

import static java.util.Objects.requireNonNull;
import static org.eclipse.che.infrastructure.docker.client.DockerRegistryAuthResolver.DEFAULT_REGISTRY_SYNONYMS;

import java.util.Objects;
import javax.validation.constraints.NotNull;
import org.eclipse.che.infrastructure.docker.client.DockerConnector;
import org.eclipse.che.infrastructure.docker.client.ProgressMonitor;
import org.eclipse.che.infrastructure.docker.client.dto.AuthConfigs;

/**
 * Arguments holder for {@link DockerConnector#pull(PullParams, ProgressMonitor)}.
 *
 * @author Mykola Morhun
 */
public class PullParams {

  private String image;
  private String tag;
  private String registry;
  private AuthConfigs authConfigs;

  /**
   * Creates arguments holder with required parameters.
   *
   * @param image name of the image to pull
   * @return arguments holder with required parameters
   * @throws NullPointerException if {@code image} null
   */
  public static PullParams create(@NotNull String image) {
    return new PullParams().withImage(image);
  }

  private PullParams() {}

  /**
   * Adds image to this parameters.
   *
   * @param image name of the image to pull
   * @return this params instance
   * @throws NullPointerException if {@code image} null
   */
  public PullParams withImage(@NotNull String image) {
    requireNonNull(image);
    this.image = image;
    return this;
  }

  /**
   * Adds tag to this parameters.
   *
   * @param tag tag of the image
   * @return this params instance
   */
  public PullParams withTag(String tag) {
    this.tag = tag;
    return this;
  }

  /**
   * Adds registry to this parameters.
   *
   * @param registry host and port of registry, e.g. localhost:5000. If it is not set, default value
   *     will be used
   * @return this params instance
   */
  public PullParams withRegistry(String registry) {
    this.registry = registry;
    return this;
  }

  /**
   * Adds auth configuration to this parameters.
   *
   * @param authConfigs authentication configuration for registries
   * @return this params instance
   */
  public PullParams withAuthConfigs(AuthConfigs authConfigs) {
    this.authConfigs = authConfigs;
    return this;
  }

  public String getImage() {
    return image;
  }

  public String getTag() {
    return tag;
  }

  public String getRegistry() {
    return registry;
  }

  public AuthConfigs getAuthConfigs() {
    return authConfigs;
  }

  /**
   * Returns full repo. It has following format: [registry/]image In case of docker.io registry is
   * omitted, otherwise it may cause some troubles with swarm
   */
  public String getFullRepo() {
    if (registry == null || DEFAULT_REGISTRY_SYNONYMS.contains(registry)) {
      return image;
    } else {
      return registry + '/' + image;
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    PullParams that = (PullParams) o;
    return Objects.equals(image, that.image)
        && Objects.equals(tag, that.tag)
        && Objects.equals(registry, that.registry)
        && Objects.equals(authConfigs, that.authConfigs);
  }

  @Override
  public int hashCode() {
    return Objects.hash(image, tag, registry, authConfigs);
  }

  @Override
  public String toString() {
    return "PullParams{"
        + "image='"
        + image
        + '\''
        + ", tag='"
        + tag
        + '\''
        + ", registry='"
        + registry
        + '\''
        + ", authConfigs="
        + authConfigs
        + '}';
  }
}
