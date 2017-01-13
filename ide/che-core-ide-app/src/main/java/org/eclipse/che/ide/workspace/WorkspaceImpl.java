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
package org.eclipse.che.ide.workspace;

import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.core.model.workspace.WorkspaceRuntime;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;

import java.util.Map;

/**
 * Data object for {@link Workspace}.
 *
 * @author Vitalii Parfonov
 */
public class WorkspaceImpl implements Workspace {

    private final String              id;
    private final WorkspaceRuntime    workspaceRuntime;
    private final String              namespace;
    private final WorkspaceStatus     status;
    private final Map<String, String> attributes;
    private final boolean             temporary;
    private final WorkspaceConfig     config;


    public WorkspaceImpl(Workspace workspace) {
        id = workspace.getId();
        workspaceRuntime = workspace.getRuntime();
        namespace = workspace.getNamespace();
        status = workspace.getStatus();
        attributes = workspace.getAttributes();
        temporary = workspace.isTemporary();
        config = workspace.getConfig();
    }


    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getNamespace() {
        return namespace;
    }

    @Override
    public WorkspaceStatus getStatus() {
        return status;
    }

    @Override
    public Map<String, String> getAttributes() {
        return attributes;
    }

    @Override
    public boolean isTemporary() {
        return temporary;
    }

    @Override
    public WorkspaceConfig getConfig() {
        return config;
    }

    @Override
    public WorkspaceRuntime getRuntime() {
        return workspaceRuntime;
    }
}
