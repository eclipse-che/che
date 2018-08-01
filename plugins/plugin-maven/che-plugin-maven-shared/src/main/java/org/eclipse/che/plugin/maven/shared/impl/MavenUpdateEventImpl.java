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
package org.eclipse.che.plugin.maven.shared.impl;

import java.util.List;
import org.eclipse.che.plugin.maven.shared.event.MavenUpdateEvent;

/** Implementation of the {@link MavenUpdateEvent}. */
public class MavenUpdateEventImpl extends MavenOutputEventImpl implements MavenUpdateEvent {
  private final List<String> updated;
  private final List<String> removed;

  public MavenUpdateEventImpl(List<String> updated, List<String> removed, TYPE type) {
    super(type);
    this.removed = removed;
    this.updated = updated;
  }

  @Override
  public List<String> getUpdatedProjects() {
    return updated;
  }

  @Override
  public List<String> getRemovedProjects() {
    return removed;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof MavenUpdateEventImpl)) return false;
    if (!super.equals(o)) return false;

    MavenUpdateEventImpl that = (MavenUpdateEventImpl) o;
    return removed.equals(that.removed) && updated.equals(that.updated);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result =
        31 * result
            + (removed != null ? removed.hashCode() : 0)
            + (updated != null ? updated.hashCode() : 0);
    return result;
  }
}
