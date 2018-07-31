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
package org.eclipse.che.ide.resources.impl;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.resources.ResourceDelta;
import org.eclipse.che.ide.resource.Path;

/**
 * Default implementation of the {@code ResourceDelta}.
 *
 * @author Vlad Zhukovskiy
 * @see ResourceDelta
 * @since 4.4.0
 */
@Beta
public class ResourceDeltaImpl implements ResourceDelta {

  private Resource newResource;
  private Resource oldResource;

  protected static int KIND_MASK = 0xF;
  protected int status;

  public ResourceDeltaImpl(Resource resource, int status) {
    this(resource, null, status);
  }

  public ResourceDeltaImpl(Resource newResource, Resource oldResource, int status) {
    this.newResource = Preconditions.checkNotNull(newResource, "Null resource occurred");
    this.oldResource = oldResource;
    this.status = status;
  }

  /** {@inheritDoc} */
  @Override
  public int getKind() {
    return status & KIND_MASK;
  }

  /** {@inheritDoc} */
  @Override
  public int getFlags() {
    return status & ~KIND_MASK;
  }

  /** {@inheritDoc} */
  @Override
  public Path getFromPath() {
    if ((status & MOVED_FROM) != 0 || (status & COPIED_FROM) != 0) {
      return oldResource.getLocation();
    }

    return null;
  }

  /** {@inheritDoc} */
  @Override
  public Path getToPath() {
    if ((status & MOVED_TO) != 0) {
      return newResource.getLocation();
    }

    return null;
  }

  /** {@inheritDoc} */
  @Override
  public Resource getResource() {
    return newResource;
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).addValue(getDebugInfo()).toString();
  }

  protected String getDebugInfo() {
    final StringBuilder sb = new StringBuilder(getResource().getLocation().toString());
    sb.append('[');

    switch (getKind()) {
      case ADDED:
        sb.append("created");
        break;
      case REMOVED:
        sb.append("removed");
        break;
      case UPDATED:
        sb.append("updated");
        break;
      default:
        sb.append('?');
        break;
    }

    sb.append("]: {");
    int changeFlags = getFlags();
    boolean prev = false;
    if ((changeFlags & CONTENT) != 0) {
      sb.append("CONTENT");
      prev = true;
    }

    if ((changeFlags & MOVED_FROM) != 0) {
      if (prev) {
        sb.append(" | ");
      }
      sb.append("MOVED_FROM(").append(getFromPath()).append(")");
      prev = true;
    }

    if ((changeFlags & MOVED_TO) != 0) {
      if (prev) {
        sb.append(" | ");
      }
      sb.append("MOVED_TO(").append(getToPath()).append(")");
      prev = true;
    }

    if ((changeFlags & COPIED_FROM) != 0) {
      if (prev) {
        sb.append(" | ");
      }
      sb.append("COPIED_FROM(").append(getFromPath()).append(")");
    }

    sb.append("}");
    return sb.toString();
  }
}
