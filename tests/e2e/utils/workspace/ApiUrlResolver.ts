import { IApiUrlResolver } from './IApiUrlResolver';

/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import { injectable } from 'inversify';


@injectable()
export class ApiUrlResolver implements IApiUrlResolver {
    private  dashboardApiUrl: string = 'dashboard/api/namespace';

    public getWorkspaceApiUrl(namespace: string, workspaceName: string) {
        return `${this.getWorkspacesApiUrl(namespace)}/${workspaceName}`;
    }

    public getWorkspacesApiUrl(namespace: string) {
        return `${this.dashboardApiUrl}/${namespace}-che/devworkspaces`;
    }
}

