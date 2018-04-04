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
package org.eclipse.che.infrastructure.docker.client.params;

import static java.util.Objects.requireNonNull;

import java.util.Objects;
import javax.validation.constraints.NotNull;
import org.eclipse.che.infrastructure.docker.client.DockerConnector;

/**
 * Arguments holder for {@link DockerConnector#commit(CommitParams)}.
 *
 * @author Mykola Morhun
 */
public class CommitParams {

  private String container;
  private String repository;
  private String tag;
  private String comment;
  private String author;

  /**
   * Creates arguments holder with required parameters.
   *
   * @param container id or name of container
   * @param repository full repository name
   * @return arguments holder with required parameters
   * @throws NullPointerException if {@code container} or {@code repository} is null
   * @deprecated use {@link CommitParams#create(String)} instead
   */
  @Deprecated
  public static CommitParams create(@NotNull String container, @NotNull String repository) {
    return new CommitParams().withContainer(container).withRepository(repository);
  }

  /**
   * Creates arguments holder with required parameters.
   *
   * @param container id or name of container
   * @return arguments holder with required parameters
   * @throws NullPointerException if {@code container} is null
   */
  public static CommitParams create(@NotNull String container) {
    return new CommitParams().withContainer(container);
  }

  private CommitParams() {}

  /**
   * Adds container to this parameters.
   *
   * @param container id or name of container
   * @return this params instance
   * @throws NullPointerException if {@code container} is null
   */
  public CommitParams withContainer(@NotNull String container) {
    requireNonNull(container);
    this.container = container;
    return this;
  }

  /**
   * Adds repository to this parameters.
   *
   * @param repository full repository name
   * @return this params instance
   */
  public CommitParams withRepository(String repository) {
    this.repository = repository;
    return this;
  }

  /**
   * Adds tag to this parameters.
   *
   * @param tag tag of the image
   * @return this params instance
   */
  public CommitParams withTag(String tag) {
    this.tag = tag;
    return this;
  }

  /**
   * Adds tag commit message to this parameters.
   *
   * @param comment commit message
   * @return this params instance
   */
  public CommitParams withComment(String comment) {
    this.comment = comment;
    return this;
  }

  /**
   * Adds author of it commit to this parameters.
   *
   * @param author author of the commit
   * @return this params instance
   */
  public CommitParams withAuthor(String author) {
    this.author = author;
    return this;
  }

  public String getContainer() {
    return container;
  }

  public String getRepository() {
    return repository;
  }

  public String getTag() {
    return tag;
  }

  public String getComment() {
    return comment;
  }

  public String getAuthor() {
    return author;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    CommitParams that = (CommitParams) o;
    return Objects.equals(container, that.container)
        && Objects.equals(repository, that.repository)
        && Objects.equals(tag, that.tag)
        && Objects.equals(comment, that.comment)
        && Objects.equals(author, that.author);
  }

  @Override
  public int hashCode() {
    return Objects.hash(container, repository, tag, comment, author);
  }
}
