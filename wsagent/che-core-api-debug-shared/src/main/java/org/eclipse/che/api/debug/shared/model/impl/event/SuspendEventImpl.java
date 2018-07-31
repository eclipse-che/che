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
package org.eclipse.che.api.debug.shared.model.impl.event;

import com.google.common.base.Objects;
import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.SuspendPolicy;
import org.eclipse.che.api.debug.shared.model.event.DebuggerEvent;
import org.eclipse.che.api.debug.shared.model.event.SuspendEvent;

/** @author Anatoliy Bazko */
public class SuspendEventImpl extends DebuggerEventImpl implements SuspendEvent {
  private final Location location;
  private final SuspendPolicy suspendPolicy;

  public SuspendEventImpl(Location location, SuspendPolicy suspendPolicy) {
    super(DebuggerEvent.TYPE.SUSPEND);
    this.location = location;
    this.suspendPolicy = suspendPolicy;
  }

  @Override
  public Location getLocation() {
    return location;
  }

  @Override
  public SuspendPolicy getSuspendPolicy() {
    return suspendPolicy;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof SuspendEventImpl)) return false;
    if (!super.equals(o)) return false;
    SuspendEventImpl that = (SuspendEventImpl) o;
    return Objects.equal(location, that.location) && suspendPolicy == that.suspendPolicy;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(super.hashCode(), location, suspendPolicy);
  }

  @Override
  public String toString() {
    return "SuspendEventImpl{" + "location=" + location + ", suspendPolicy=" + suspendPolicy + '}';
  }
}
