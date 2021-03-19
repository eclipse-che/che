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
    cleanUpAllWorkspaces() : void;
    waitWorkspaceStatus(namespace: string, workspaceName: string, expectedWorkspaceStatus: WorkspaceStatus) : void;
    waitPluginAdding(namespace: string, workspaceName: string, pluginId: string) : void;
    removeWorkspaceById(id: string) : void;
    stopWorkspaceById(id: string) : void;
    getIdOfRunningWorkspace(namespace: string): Promise<string>;
    getIdOfRunningWorkspaces(): Promise<Array<string>>;
    createWsFromDevFile(customTemplate: che.workspace.devfile.Devfile): void;
    getBaseDevfile(): Promise<che.workspace.devfile.Devfile>
}
