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
package org.eclipse.che.api.workspace.server.spi;

/**
 * @author gazarenkov
 */
public final class RuntimeIdentity {

    private final String workspaceId;
    private final String envName;
    private final String owner;

    public RuntimeIdentity(String workspaceId, String envName, String owner) {
        this.workspaceId = workspaceId;
        this.envName = envName;
        this.owner = owner;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public String getEnvName() {
        return envName;
    }

    public String getOwner() {
        return owner;
    }

    @Override
    public int hashCode() {
        return (workspaceId + envName).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(! (obj instanceof RuntimeIdentity) )
            return false;
        RuntimeIdentity other = (RuntimeIdentity) obj;
        return workspaceId.equals(other.workspaceId) && envName.equals(other.envName);
    }

    @Override
    public String toString() {
        return "RuntimeIdentity: { workspace: " + workspaceId + " environment: " + envName + " owner: " + owner +" }";
    }
}
