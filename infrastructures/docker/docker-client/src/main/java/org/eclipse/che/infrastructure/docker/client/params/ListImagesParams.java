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

import java.util.Objects;
import org.eclipse.che.infrastructure.docker.client.DockerConnector;
import org.eclipse.che.infrastructure.docker.client.json.Filters;

/**
 * Arguments holder for {@link DockerConnector#listImages(ListImagesParams)}.
 *
 * @author Mykola Morhun
 */
public class ListImagesParams {

  private Boolean all;
  private Filters filters;
  private Boolean digests;

  /** Creates arguments holder. */
  public static ListImagesParams create() {
    return new ListImagesParams();
  }

  private ListImagesParams() {}

  /**
   * Adds all flag to this parameters.
   *
   * @param all if true show all images. Only images from a final layer (no children) are shown by
   *     default.
   * @return this params instance
   */
  public ListImagesParams withAll(Boolean all) {
    this.all = all;
    return this;
  }

  /**
   * Adds filters to this parameters.
   *
   * @param filters Available filters:
   *     <ul>
   *       <li><code>before</code>=(<code>&lt;image-name&gt;[:&lt;tag&gt;]</code>, <code>
   *           &lt;image id&gt;</code> or <code>&lt;image@digest&gt;</code>)
   *       <li><code>dangling=true</code>
   *       <li><code>label=key</code> or <code>label="key=value"</code> of an image label
   *       <li><code>reference</code>=(<code>&lt;image-name&gt;[:&lt;tag&gt;]</code>)
   *       <li><code>since</code>=(<code>&lt;image-name&gt;[:&lt;tag&gt;]</code>, <code>
   *           &lt;image id&gt;</code> or <code>&lt;image@digest&gt;</code>)
   *     </ul>
   *
   * @return this params instance
   */
  public ListImagesParams withFilters(Filters filters) {
    this.filters = filters;
    return this;
  }

  /**
   * Adds all flag to this parameters.
   *
   * @param digests if true show digest information on each image
   * @return this params instance
   */
  public ListImagesParams withDigestst(Boolean digests) {
    this.digests = digests;
    return this;
  }

  public Boolean getAll() {
    return all;
  }

  public Filters getFilters() {
    return filters;
  }

  public Boolean getDigests() {
    return digests;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ListImagesParams that = (ListImagesParams) o;
    return Objects.equals(all, that.all)
        && Objects.equals(filters, that.filters)
        && Objects.equals(digests, that.digests);
  }

  @Override
  public int hashCode() {
    return Objects.hash(all, filters, digests);
  }
}
