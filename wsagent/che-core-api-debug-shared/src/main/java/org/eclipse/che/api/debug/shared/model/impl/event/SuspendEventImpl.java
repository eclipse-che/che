/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.debug.shared.model.impl.event;

import org.eclipse.che.api.debug.shared.model.event.SuspendEvent;
import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.event.DebuggerEvent;

/**
 * @author Anatoliy Bazko
 */
public class SuspendEventImpl extends DebuggerEventImpl implements SuspendEvent {
    private final Location location;

    public SuspendEventImpl(Location location) {
        super(DebuggerEvent.TYPE.SUSPEND);
        this.location = location;
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SuspendEventImpl)) return false;
        if (!super.equals(o)) return false;

        SuspendEventImpl that = (SuspendEventImpl)o;

        return !(location != null ? !location.equals(that.location) : that.location != null);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (location != null ? location.hashCode() : 0);
        return result;
    }
}
