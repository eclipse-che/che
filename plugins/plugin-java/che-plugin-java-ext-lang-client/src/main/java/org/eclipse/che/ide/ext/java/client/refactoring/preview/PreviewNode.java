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
package org.eclipse.che.ide.ext.java.client.refactoring.preview;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.lsp4j.ResourceChange;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

/**
 * Describes a node from the tree of changes in Preview page.
 *
 * @author Valeriy Svydenko
 */
public class PreviewNode {
  private String id;
  private String description;
  private boolean enable;
  private Either<ResourceChange, TextEdit> data;
  private List<PreviewNode> children;
  private PreviewNode parent;
  private String uri;

  public PreviewNode() {
    this.children = new ArrayList<>();
  }

  /** Returns id of node. */
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  /** Returns description which describes current node. */
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  /** Returns {@code true} if node is chose otherwise returns {@code false}. */
  public boolean isEnable() {
    return enable;
  }

  public void setEnable(boolean enable) {
    this.enable = enable;
  }

  /** Returns data of current node it can be {@link ResourceChange} or {@link TextEdit}. */
  public Either<ResourceChange, TextEdit> getData() {
    return data;
  }

  public void setData(Either<ResourceChange, TextEdit> data) {
    this.data = data;
  }

  /** Returns list of children. */
  public List<PreviewNode> getChildren() {
    return children;
  }

  public void setChildren(List<PreviewNode> children) {
    this.children = children;
  }

  /** Returns parent node if current node is leaf or null if it isn't. */
  public PreviewNode getParent() {
    return parent;
  }

  public void setParent(PreviewNode parent) {
    this.parent = parent;
  }

  public boolean hasParent() {
    return parent != null;
  }

  /** Returns uri of the resource which is related to current change. */
  public String getUri() {
    return uri;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    PreviewNode other = (PreviewNode) obj;
    if (this.children == null) {
      if (other.children != null) {
        return false;
      }
    } else if (!this.children.equals(other.children)) {
      return false;
    }
    if (this.id == null) {
      if (other.id != null) {
        return false;
      }
    } else if (!this.id.equals(other.id)) {
      return false;
    }
    if (this.uri == null) {
      if (other.uri != null) {
        return false;
      }
    } else if (!this.uri.equals(other.uri)) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((this.children == null) ? 0 : this.children.hashCode());
    result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
    result = prime * result + ((this.uri == null) ? 0 : this.uri.hashCode());
    return result;
  }
}
