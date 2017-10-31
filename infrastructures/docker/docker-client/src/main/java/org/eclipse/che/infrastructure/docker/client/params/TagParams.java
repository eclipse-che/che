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

import java.util.Objects;
import javax.validation.constraints.NotNull;
import org.eclipse.che.infrastructure.docker.client.DockerConnector;

/**
 * Arguments holder for {@link DockerConnector#tag(TagParams)}.
 *
 * @author Mykola Morhun
 */
public class TagParams {

  private String image;
  private String repository;
  private String tag;
  private Boolean force;

  /**
   * Creates arguments holder with required parameters.
   *
   * @param image image name
   * @param repository the repository to tag in
   * @return arguments holder with required parameters
   * @throws NullPointerException if {@code image} or {@code repository} is null
   */
  public static TagParams create(@NotNull String image, @NotNull String repository) {
    return new TagParams().withImage(image).withRepository(repository);
  }

  private TagParams() {}

  /**
   * Adds image to this parameters.
   *
   * @param image image name
   * @return this params instance
   * @throws NullPointerException if {@code image} is null
   */
  public TagParams withImage(@NotNull String image) {
    requireNonNull(image);
    this.image = image;
    return this;
  }

  /**
   * Adds repository to this parameters.
   *
   * @param repository the repository to tag in
   * @return this params instance
   * @throws NullPointerException if {@code repository} is null
   */
  public TagParams withRepository(@NotNull String repository) {
    requireNonNull(repository);
    this.repository = repository;
    return this;
  }

  /**
   * Adds tag to this parameters.
   *
   * @param tag new tag name
   * @return this params instance
   */
  public TagParams withTag(String tag) {
    this.tag = tag;
    return this;
  }

  /**
   * Adds force flag to this parameters.
   *
   * @param force force tagging of the image
   * @return this params instance
   */
  public TagParams withForce(boolean force) {
    this.force = force;
    return this;
  }

  public String getImage() {
    return image;
  }

  public String getRepository() {
    return repository;
  }

  public String getTag() {
    return tag;
  }

  public Boolean isForce() {
    return force;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    TagParams tagParams = (TagParams) o;
    return Objects.equals(image, tagParams.image)
        && Objects.equals(repository, tagParams.repository)
        && Objects.equals(tag, tagParams.tag)
        && Objects.equals(force, tagParams.force);
  }

  @Override
  public int hashCode() {
    return Objects.hash(image, repository, tag, force);
  }

  @Override
  public String toString() {
    return "TagParams{"
        + "image='"
        + image
        + '\''
        + ", repository='"
        + repository
        + '\''
        + ", tag='"
        + tag
        + '\''
        + ", force="
        + force
        + '}';
  }
}
