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
