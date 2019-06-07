/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import { TestConstants } from '../../TestConstants';
import { injectable, inject } from 'inversify';
import { DriverHelper } from '../DriverHelper';
import { CLASSES } from '../../inversify.types';
import 'reflect-metadata';
import * as rm from 'typed-rest-client/RestClient';

export enum WorkspaceStatus {
    RUNNING = 'RUNNING',
    STOPPED = 'STOPPED',
    STARTING = 'STARTING'
}

@injectable()
export class TestWorkspaceUtil {
    constructor(@inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper,
        private readonly rest: rm.RestClient = new rm.RestClient('rest-samples')) { }

    public async waitWorkspaceStatus(namespace: string, workspaceName: string, expectedWorkspaceStatus: WorkspaceStatus) {
        const workspaceStatusApiUrl: string = `${TestConstants.TS_SELENIUM_BASE_URL}/api/workspace/${namespace}:${workspaceName}`;
        const attempts: number = TestConstants.TS_SELENIUM_WORKSPACE_STATUS_ATTEMPTS;
        const polling: number = TestConstants.TS_SELENIUM_WORKSPACE_STATUS_POLLING;

        for (let i = 0; i < attempts; i++) {
            const response: rm.IRestResponse<any> = await this.rest.get(workspaceStatusApiUrl);

            if (response.statusCode !== 200) {
                await this.driverHelper.wait(polling);
                continue;
            }

            const workspaceStatus: string = await response.result.status;

            if (workspaceStatus === expectedWorkspaceStatus) {
                return;
            }

            await this.driverHelper.wait(polling);
        }

        throw new Error(`Exceeded the maximum number of checking attempts, workspace status is different to '${expectedWorkspaceStatus}'`);
    }

    public async waitPluginAdding(namespace: string, workspaceName: string, pluginId: string) {
        const workspaceStatusApiUrl: string = `${TestConstants.TS_SELENIUM_BASE_URL}/api/workspace/${namespace}:${workspaceName}`;
        const attempts: number = TestConstants.TS_SELENIUM_PLUGIN_PRECENCE_ATTEMPTS;
        const polling: number = TestConstants.TS_SELENIUM_PLUGIN_PRECENCE_POLLING;

        for (let i = 0; i < attempts; i++) {
            const response: rm.IRestResponse<any> = await this.rest.get(workspaceStatusApiUrl);

            if (response.statusCode !== 200) {
                await this.driverHelper.wait(polling);
                continue;
            }

            const machines: string = JSON.stringify(response.result.runtime.machines);
            const isPluginPresent: boolean = machines.search(pluginId) > 0;

            if (isPluginPresent) {
                break;
            }

            if (i === attempts - 1) {
                throw new Error(`Exceeded maximum tries attempts, the '${pluginId}' plugin is not present in the workspace runtime.`);
            }

            await this.driverHelper.wait(polling);
        }
    }

}
