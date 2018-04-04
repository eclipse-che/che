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
import org.eclipse.che.infrastructure.docker.client.DockerConnector;
import org.eclipse.che.infrastructure.docker.client.json.Filters;

/**
 * Arguments holder for {@link DockerConnector#listContainers(ListContainersParams)}.
 *
 * @author Alexander Andrienko
 */
public class ListContainersParams {
  private Boolean all;
  private Integer limit;
  private String since;
  private String before;
  private Boolean size;
  private Filters filters;

  private ListContainersParams() {}

  /** Creates and returns arguments holder. */
  public static ListContainersParams create() {
    return new ListContainersParams();
  }

  /**
   * Adds parameter show all containers. Only running containers are shown by default.
   *
   * @param all if true show all containers, if false show only running containers
   */
  public ListContainersParams withAll(boolean all) {
    this.all = all;
    return this;
  }

  /**
   * Adds parameter show limit last created containers, include non-running ones.
   *
   * @param limit amount elements of the list containers
   */
  public ListContainersParams withLimit(int limit) {
    this.limit = limit;
    return this;
  }

  /**
   * Adds parameter show only containers created since container with {@code id}.
   *
   * @param id container id
   */
  public ListContainersParams withSince(String id) {
    requireNonNull(id);
    this.since = id;
    return this;
  }

  /**
   * Adds parameter show only containers created before container with {@code id}.
   *
   * @param id container id
   */
  public ListContainersParams withBefore(String id) {
    requireNonNull(id);
    this.before = id;
    return this;
  }

  /**
   * Adds parameter show docker container size information.
   *
   * @param size if size = true then api add container size information, otherwise hide this
   *     information. Warning: if size = true docker api need more time for calculation container
   *     size
   */
  public ListContainersParams withSize(boolean size) {
    this.size = size;
    return this;
  }

  /**
   * Adds parameter filters for filter list containers. See more {@link Filters}
   *
   * @param filters parameters for filter list containers. Filter values:
   *     <li>exited=<int>; -- containers with exit code of <int>;
   *     <li>status=(created|restarting|running|paused|exited|dead)
   *     <li>label=key or label="key=value" of a container label
   *     <li>isolation=(default|process|hyperv) (Windows daemon only)
   *     <li>ancestor=(<image-name>[:<tag>], <image id> or <image@digest>)
   *     <li>before=(<container id> or <container name>)
   *     <li>since=(<container id> or <container name>)
   *     <li>volume=(<volume name> or <mount point destination>)
   */
  public ListContainersParams withFilters(Filters filters) {
    requireNonNull(filters);
    this.filters = filters;
    return this;
  }

  public Boolean isAll() {
    return all;
  }

  public Integer getLimit() {
    return limit;
  }

  public String getSince() {
    return since;
  }

  public String getBefore() {
    return before;
  }

  public Boolean isSize() {
    return size;
  }

  public Filters getFilters() {
    return filters;
  }

  @Override
  public String toString() {
    return "ListContainersParams{"
        + "all="
        + all
        + ", limit="
        + limit
        + ", since='"
        + since
        + '\''
        + ", before='"
        + before
        + '\''
        + ", size="
        + size
        + ", filters="
        + filters
        + '}';
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof ListContainersParams)) return false;
    ListContainersParams another = (ListContainersParams) obj;

    return Objects.equals(isAll(), another.isAll())
        && Objects.equals(getLimit(), another.getLimit())
        && Objects.equals(getSince(), another.getSince())
        && Objects.equals(getBefore(), another.getBefore())
        && Objects.equals(isSize(), another.isSize())
        && Objects.equals(getFilters(), another.getFilters());
  }

  @Override
  public int hashCode() {
    return Objects.hash(isAll(), getLimit(), getSince(), getBefore(), isSize(), getFilters());
  }
}
