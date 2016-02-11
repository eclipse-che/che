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
package org.eclipse.che.ide.ext.java.jdi.client.debug;

import org.eclipse.che.ide.ext.java.jdi.shared.DebuggerInfo;

/**
 * Is used when debugger isn't connected to the JVM.
 * Contains empty data.
 *
 * @author Anatoliy Bazko
 */
public class EmptyDebuggerInfo implements DebuggerInfo {
    public static final DebuggerInfo INSTANCE = new EmptyDebuggerInfo();

    private EmptyDebuggerInfo() {
    }

    @Override
    public String getHost() {
        return "";
    }

    @Override
    public void setHost(String host) { }

    @Override
    public DebuggerInfo withHost(String host) {
        return null;
    }

    @Override
    public int getPort() {
        return 0;
    }

    @Override
    public void setPort(int port) { }

    @Override
    public DebuggerInfo withPort(int port) {
        return null;
    }

    @Override
    public String getId() {
        return "";
    }

    @Override
    public void setId(String id) { }

    @Override
    public DebuggerInfo withId(String id) {
        return null;
    }

    @Override
    public String getVmName() {
        return "";
    }

    @Override
    public void setVmName(String vmName) { }

    @Override
    public DebuggerInfo withVmName(String vmName) {
        return null;
    }

    @Override
    public String getVmVersion() {
        return "";
    }

    @Override
    public void setVmVersion(String vmVersion) {}

    @Override
    public DebuggerInfo withVmVersion(String vmVersion) {
        return null;
    }
}
