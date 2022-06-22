/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import { WorkspaceStatus } from './WorkspaceStatus';
import { che } from '@eclipse-che/api';

export interface ITestWorkspaceUtil {
    waitWorkspaceStatus(namespace: string, workspaceName: string, expectedWorkspaceStatus: WorkspaceStatus) : void;

    stopWorkspaceByName(workspaceName: string) : void;

    /**
     * Delete a worksapce without stopping phase (similar with force deleting)
     */
    deleteWorkspaceByName(workspaceName: string) : void;

    /**
     * Stop workspace before deleting with checking stopping phase
     */
    stopAndDeleteWorkspaceByName(workspaceName: string) : void;

    /**
     * Stop all run workspaces in the namespace
     */
    stopAllRunningWorkspaces(namespace: string) : void;

    /**
     * Stop all run workspaces, check statused and remove the workspaces
     */
    stopAndDeleteAllRunningWorkspaces(namespace: string) : void;

    /**
     * Stop all run workspaces without stopping and waiting for of 'Stopped' phase
     * Similar with 'force' deleting
     */
    deleteAllWorkspaces(namespace: string) : void;

    /*=====================
     * DEPRECATED METHODS *
     *====================*/

    /**
     * @deprecated Method deprecated. Works with CHE server only
     */
    cleanUpAllWorkspaces() : void;

    /**
     * @deprecated Method deprecated. Works with CHE server only
     */
    cleanUpRunningWorkspace(workspaceName: string) : void;

    /**
     * @deprecated Method deprecated. Works with CHE server only
     */
    waitPluginAdding(namespace: string, workspaceName: string, pluginId: string) : void;

    /**
     * @deprecated Method deprecated. Works with CHE server only
     */
    removeWorkspaceById(id: string) : void;

    /**
     * @deprecated Method deprecated. Works with CHE server only
     */
    stopWorkspaceById(id: string) : void;

    /**
     * @deprecated Method deprecated. Works with CHE server only
     */
    getIdOfRunningWorkspace(workspaceName: string): Promise<string>;

    /**
     * @deprecated Method deprecated. Works with CHE server only
     */
    getIdOfRunningWorkspaces(): Promise<Array<string>>;

    /**
     * @deprecated Method deprecated. Works with CHE server only
     */
    createWsFromDevFile(customTemplate: che.workspace.devfile.Devfile): void;

    /**
     * @deprecated Method deprecated. Works with CHE server only
     */
    getBaseDevfile(): Promise<che.workspace.devfile.Devfile>;
    
    /**
     * @deprecated Method deprecated. Works with CHE server only
     */
    startWorkspace(workspaceId: string): void;
}
