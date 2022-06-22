/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import { inject, injectable } from 'inversify';
import { CLASSES } from '../../inversify.types';
import { Logger } from '../Logger';
import { CheApiRequestHandler } from '../requestHandlers/CheApiRequestHandler';

@injectable()
export class ApiUrlResolver {
    static USER_NAMESPACE: string = '';

    private dashboardApiUrl: string = 'dashboard/api/namespace';
    private kubernetesApiUrl: string = 'api/kubernetes/namespace';

    constructor(@inject(CLASSES.CheApiRequestHandler) private readonly processRequestHandler: CheApiRequestHandler) {}

    public async getWorkspaceApiUrl(workspaceName: string): Promise<string> {
        return `${await this.getWorkspacesApiUrl()}/${workspaceName}`;
    }

    public async getWorkspacesApiUrl(): Promise<string> {
        const namespace = await this.obtainUserNamespace();
        return `${this.dashboardApiUrl}/${namespace}/devworkspaces`;
    }

    public getKubernetesApiUrl(): string {
        return this.kubernetesApiUrl;
    }

    private async obtainUserNamespace() : Promise<string> {
        Logger.debug(`ApiUrlResolver.obtainUserNamespace ${ApiUrlResolver.USER_NAMESPACE}`);
        if (ApiUrlResolver.USER_NAMESPACE.length === 0) {
            Logger.trace(`ApiUrlResolver.obtainUserNamespace USER_NAMESPACE.length = 0, calling kubernetes API`);
            const kubernetesResponse = await this.processRequestHandler.get(this.kubernetesApiUrl);
            if (kubernetesResponse.status !== 200) {
                throw new Error(`Cannot get user namespace from kubernetes API. Code: ${kubernetesResponse.status} Data: ${kubernetesResponse.data}`);
            }
            ApiUrlResolver.USER_NAMESPACE = kubernetesResponse.data[0].name;
            Logger.debug(`ApiUrlResolver.obtainUserNamespace kubeapi success: ${ApiUrlResolver.USER_NAMESPACE}`);
        }
        return ApiUrlResolver.USER_NAMESPACE;
    }
}
