/*********************************************************************
 * Copyright (c) 2019-2023 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import { inject, injectable } from 'inversify';
import { CLASSES } from '../../configs/inversify.types';
import { Logger } from '../Logger';
import { CheApiRequestHandler } from '../request-handlers/CheApiRequestHandler';

@injectable()
export class ApiUrlResolver {
    private static readonly DASHBOARD_API_URL: string = 'dashboard/api/namespace';
    private static readonly KUBERNETES_API_URL: string = 'api/kubernetes/namespace';

    private userNamespace: string = '';

    constructor(@inject(CLASSES.CheApiRequestHandler) private readonly processRequestHandler: CheApiRequestHandler) {}

    public async getWorkspaceApiUrl(workspaceName: string): Promise<string> {
        return `${await this.getWorkspacesApiUrl()}/${workspaceName}`;
    }

    public async getWorkspacesApiUrl(): Promise<string> {
        const namespace = await this.obtainUserNamespace();
        return `${ApiUrlResolver.DASHBOARD_API_URL}/${namespace}/devworkspaces`;
    }

    public getKubernetesApiUrl(): string {
        return ApiUrlResolver.KUBERNETES_API_URL;
    }

    private async obtainUserNamespace(): Promise<string> {
        Logger.debug(`ApiUrlResolver.obtainUserNamespace ${this.userNamespace}`);
        if (this.userNamespace.length === 0) {
            Logger.trace(`ApiUrlResolver.obtainUserNamespace USER_NAMESPACE.length = 0, calling kubernetes API`);
            const kubernetesResponse = await this.processRequestHandler.get(ApiUrlResolver.KUBERNETES_API_URL);
            if (kubernetesResponse.status !== 200) {
                throw new Error(`Cannot get user namespace from kubernetes API. Code: ${kubernetesResponse.status} Data: ${kubernetesResponse.data}`);
            }
            this.userNamespace = kubernetesResponse.data[0].name;
            Logger.debug(`ApiUrlResolver.obtainUserNamespace kubeapi success: ${this.userNamespace}`);
        }
        return this.userNamespace;
    }
}
