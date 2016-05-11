/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.api.debugger.shared.model.impl;

import org.eclipse.che.ide.api.debugger.shared.model.Location;

/**
 * @author Anatoliy Bazko
 */
public class LocationImpl implements Location {
    private final String target;
    private final int    lineNumber;

    public LocationImpl(String target, int lineNumber) {
        this.target = target;
        this.lineNumber = lineNumber;
    }

    public LocationImpl(String target) {
        this(target, 0);
    }

    @Override
    public String getTarget() {
        return target;
    }

    @Override
    public int getLineNumber() {
        return lineNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LocationImpl)) return false;

        LocationImpl location = (LocationImpl)o;

        if (lineNumber != location.lineNumber) return false;
        return !(target != null ? !target.equals(location.target) : location.target != null);

    }

    @Override
    public int hashCode() {
        int result = target != null ? target.hashCode() : 0;
        result = 31 * result + lineNumber;
        return result;
    }
}
