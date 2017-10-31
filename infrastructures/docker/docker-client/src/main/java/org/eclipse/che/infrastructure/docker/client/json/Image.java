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
package org.eclipse.che.infrastructure.docker.client.json;

import java.util.Arrays;
import java.util.Map;

/** @author andrew00x */
public class Image {
  private String[] repoTags;
  private String parentId;
  private String id;
  private long created;
  private long size;
  private long virtualSize;
  private Map<String, String> labels;

  /** Return a collection of tags grouped under a common prefix (the name component before ':'). */
  public String[] getRepoTags() {
    return repoTags;
  }

  public void setRepoTags(String[] repoTags) {
    this.repoTags = repoTags;
  }

  /** Return uniquely identifies the image. */
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  /** Return ISO-8601 formatted combined date and time at which the image was created. */
  public long getCreated() {
    return created;
  }

  public void setCreated(long created) {
    this.created = created;
  }

  /** Return the size in bytes of the filesystem changeset associated with the image layer. */
  public long getSize() {
    return size;
  }

  public void setSize(long size) {
    this.size = size;
  }

  /** Return the virtual size of the image */
  public long getVirtualSize() {
    return virtualSize;
  }

  public void setVirtualSize(long virtualSize) {
    this.virtualSize = virtualSize;
  }

  /** Return parent IDs of image layers to find the root ancestor */
  public String getParentId() {
    return parentId;
  }

  public void setParentId(String parentId) {
    this.parentId = parentId;
  }

  /** Returns metadata of the image */
  public Map<String, String> getLabels() {
    return labels;
  }

  public void setLabels(Map<String, String> labels) {
    this.labels = labels;
  }

  @Override
  public String toString() {
    return "Image{"
        + "repoTags="
        + Arrays.toString(repoTags)
        + ", parentId='"
        + parentId
        + '\''
        + ", id='"
        + id
        + '\''
        + ", created="
        + created
        + ", size="
        + size
        + ", virtualSize="
        + virtualSize
        + ", labels="
        + labels
        + '}';
  }
}
