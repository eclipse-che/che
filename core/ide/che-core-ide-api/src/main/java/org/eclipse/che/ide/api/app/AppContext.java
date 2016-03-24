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
package org.eclipse.che.ide.api.app;

import org.eclipse.che.api.factory.shared.dto.Factory;
import org.eclipse.che.api.workspace.shared.dto.UsersWorkspaceDto;

import java.util.List;

/**
 * Represents current context of the IDE application.
 *
 * @author Vitaly Parfonov
 * @author Artem Zatsarynnyi
 */
public interface AppContext {

    /** Returns list of start-up actions with parameters that comes form URL during IDE initialization. */
    List<StartUpAction> getStartAppActions();

    UsersWorkspaceDto getWorkspace();

    void setWorkspace(UsersWorkspaceDto workspace);

    /** Returns id of current workspace of throws IllegalArgumentException if workspace is null. */
    String getWorkspaceId();

    /**
     * Returns {@link CurrentProject} instance that describes the project
     * that is currently opened or <code>null</code> if none opened.
     * <p/>
     * Note that current project may also represent a project's module.
     *
     * @return opened project or <code>null</code> if none opened
     */
    CurrentProject getCurrentProject();

    /**
     * Returns current user.
     *
     * @return current user
     */
    CurrentUser getCurrentUser();

    void setCurrentUser(CurrentUser currentUser);

    /**
     * Returns list of projects paths which are in importing state.
     *
     * @return list of project paths
     */
    List<String> getImportingProjects();

    /**
     * Adds project path to list of projects which are in importing state.
     *
     * @param pathToProject
     *         project path
     */
    void addProjectToImporting(String pathToProject);

    /**
     * Removes project path to list of projects which are in importing state.
     *
     * @param pathToProject
     *         project path
     */
    void removeProjectFromImporting(String pathToProject);

    /**
     * List of action with params that comes from startup URL.
     * Can be processed after IDE initialization as usual after
     * starting ws-agent.
     */
    void setStartUpActions(List<StartUpAction> startUpActions);

    /**
     * Returns {@link Factory} instance which id was set on startup,
     * or {@code null} if no factory was specified.
     *
     * @return loaded factory or {@code null}
     */
    Factory getFactory();

    void setFactory(Factory factory);

    /** Returns ID of the developer machine (where workspace is bound). */
    String getDevMachineId();

    void setDevMachineId(String id);

    String getProjectsRoot();

    void setProjectsRoot(String projectsRoot);

    /** Returns URL to send requests to workspace agent. */
    String getWsAgentURL();
}
