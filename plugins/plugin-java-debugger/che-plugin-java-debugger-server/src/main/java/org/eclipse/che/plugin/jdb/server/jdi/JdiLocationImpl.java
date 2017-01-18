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
package org.eclipse.che.plugin.jdb.server.jdi;

import com.sun.jdi.StackFrame;

import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.Method;
import org.eclipse.che.api.debug.shared.model.impl.LocationImpl;
import org.eclipse.che.api.debugger.server.exceptions.DebuggerException;
import org.eclipse.che.plugin.jdb.server.utils.JavaDebuggerUtils;

/**
 * @author Anatolii Bazko
 */
public class JdiLocationImpl implements JdiLocation {
    private static final JavaDebuggerUtils debuggerUtil = new JavaDebuggerUtils();

    private final Method   method;
    private final Location location;

    public JdiLocationImpl(StackFrame stackFrame) {
        com.sun.jdi.Location jdiLocation = stackFrame.location();
        this.location = findLocation(jdiLocation);
        this.method = new JdiMethodImpl(stackFrame, jdiLocation.method());
    }

    public JdiLocationImpl(com.sun.jdi.Location jdiLocation) {
        this.location = findLocation(jdiLocation);
        this.method = new JdiMethodImpl(jdiLocation.method());
    }

    @Override
    public String getTarget() {
        return location.getTarget();
    }

    @Override
    public int getLineNumber() {
        return location.getLineNumber();
    }

    @Override
    public String getResourcePath() {
        return location.getResourcePath();
    }

    @Override
    public boolean isExternalResource() {
        return location.isExternalResource();
    }

    @Override
    public int getExternalResourceId() {
        return location.getExternalResourceId();
    }

    @Override
    public String getResourceProjectPath() {
        return location.getResourceProjectPath();
    }

    @Override
    public Method getMethod() {
        return method;
    }

    private Location findLocation(com.sun.jdi.Location jdiLocation) {
        Location location;
        try {
            location = debuggerUtil.getLocation(jdiLocation);
        } catch (DebuggerException e) {
            location = new LocationImpl(jdiLocation.declaringType().name(), jdiLocation.lineNumber());
        }
        return location;
    }
}
