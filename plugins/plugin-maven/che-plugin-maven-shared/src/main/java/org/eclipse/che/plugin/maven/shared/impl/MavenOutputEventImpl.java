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

import org.eclipse.che.plugin.maven.shared.event.MavenOutputEvent;

/** Implementation of base maven output event. */
public class MavenOutputEventImpl implements MavenOutputEvent {
  private final TYPE type;

  public MavenOutputEventImpl(TYPE type) {
    this.type = type;
  }

  @Override
  public TYPE getType() {
    return type;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof MavenOutputEventImpl)) return false;

    MavenOutputEventImpl that = (MavenOutputEventImpl) o;

    return type == that.type;
  }

  @Override
  public int hashCode() {
    return type != null ? type.hashCode() : 0;
  }
}
