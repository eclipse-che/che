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
package org.eclipse.che.api.debug.shared.model.impl.event;

import org.eclipse.che.api.debug.shared.model.event.DebuggerEvent;

/** @author Anatoliy Bazko */
public class DebuggerEventImpl implements DebuggerEvent {
  private final TYPE type;

  public DebuggerEventImpl(TYPE type) {
    this.type = type;
  }

  @Override
  public TYPE getType() {
    return type;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof DebuggerEventImpl)) return false;

    DebuggerEventImpl that = (DebuggerEventImpl) o;

    return type == that.type;
  }

  @Override
  public int hashCode() {
    return type != null ? type.hashCode() : 0;
  }
}
