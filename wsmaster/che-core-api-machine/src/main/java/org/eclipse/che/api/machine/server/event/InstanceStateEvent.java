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
package org.eclipse.che.api.machine.server.event;

/**
 * Describe instance state change.
 *
 * @author Alexander Garagatyi
 */
public class InstanceStateEvent {
    /**
     * Type of state change of a machine instance.<br>
     * Consider that machine implementation may or may not support each state change type.
     */
    public enum Type {
        DIE,
        OOM
    }

    private String machineId;
    private String workspaceId;
    private Type   type;

    public InstanceStateEvent(String machineId,
                              String workspaceId,
                              Type type) {
        this.machineId = machineId;
        this.workspaceId = workspaceId;
        this.type = type;
    }

    public String getMachineId() {
        return machineId;
    }

    public Type getType() {
        return type;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }
}
