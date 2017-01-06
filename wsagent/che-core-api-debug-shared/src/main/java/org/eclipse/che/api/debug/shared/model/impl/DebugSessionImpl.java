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
package org.eclipse.che.api.debug.shared.model.impl;

import org.eclipse.che.api.debug.shared.model.DebugSession;
import org.eclipse.che.api.debug.shared.model.DebuggerInfo;

/**
 * @author Anatoliy Bazko
 */
public class DebugSessionImpl implements DebugSession {
    private final DebuggerInfo debuggerInfo;
    private final String id;
    private final String type;

    public DebugSessionImpl(DebuggerInfo debuggerInfo, String id, String type) {
        this.debuggerInfo = debuggerInfo;
        this.id = id;
        this.type = type;
    }

    @Override
    public DebuggerInfo getDebuggerInfo() {
        return debuggerInfo;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DebugSessionImpl)) return false;

        DebugSessionImpl that = (DebugSessionImpl)o;

        if (debuggerInfo != null ? !debuggerInfo.equals(that.debuggerInfo) : that.debuggerInfo != null) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        return !(type != null ? !type.equals(that.type) : that.type != null);
    }

    @Override
    public int hashCode() {
        int result = debuggerInfo != null ? debuggerInfo.hashCode() : 0;
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }
}
