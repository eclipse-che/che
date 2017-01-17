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

import org.eclipse.che.api.debug.shared.model.StackFrameDump;
import org.eclipse.che.api.debug.shared.model.ThreadDump;
import org.eclipse.che.api.debug.shared.model.ThreadStatus;

import java.util.List;
import java.util.Objects;

/**
 * @author Anatolii Bazko
 */
public class ThreadDumpImpl implements ThreadDump {
    private final String                         name;
    private final String                         groupName;
    private final ThreadStatus                   status;
    private final boolean                        isSuspended;
    private final List<? extends StackFrameDump> frames;

    public ThreadDumpImpl(String name,
                          String groupName,
                          ThreadStatus status,
                          boolean isSuspended,
                          List<? extends StackFrameDump> frames) {
        this.name = name;
        this.groupName = groupName;
        this.status = status;
        this.isSuspended = isSuspended;
        this.frames = frames;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getGroupName() {
        return groupName;
    }

    @Override
    public ThreadStatus getStatus() {
        return status;
    }

    @Override
    public boolean isSuspended() {
        return isSuspended;
    }

    @Override
    public List<? extends StackFrameDump> getFrames() {
        return frames;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ThreadDumpImpl)) {
            return false;
        }
        final ThreadDumpImpl that = (ThreadDumpImpl)obj;
        return isSuspended == that.isSuspended
               && Objects.equals(name, that.name)
               && Objects.equals(groupName, that.groupName)
               && Objects.equals(status, that.status)
               && getFrames().equals(that.getFrames());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(name);
        hash = 31 * hash + Objects.hashCode(groupName);
        hash = 31 * hash + Objects.hashCode(status);
        hash = 31 * hash + Boolean.hashCode(isSuspended);
        hash = 31 * hash + getFrames().hashCode();
        return hash;
    }

    @Override
    public String toString() {
        return "ThreadDumpImpl{" +
               "name='" + name + '\'' +
               ", groupName='" + groupName + '\'' +
               ", status=" + status +
               ", isSuspended=" + isSuspended +
               ", frames=" + frames +
               '}';
    }
}
