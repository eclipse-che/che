/*********************************************************************
 * Copyright (c) 2018 Red Hat, Inc.
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
import * as rm from 'typed-rest-client/RestClient'



@injectable()
export class TestWorkspaceUtil {
    private readonly driverHelper: DriverHelper;

    constructor(@inject(CLASSES.DriverHelper) driverHelper: DriverHelper) {
        this.driverHelper = driverHelper;
    }

    public async waitRunningStatus(workspaceNamespace: string, workspaceName: string) {
        const workspaceStatusApiUrl: string = `${TestConstants.TS_SELENIUM_BASE_URL}/api/workspace/${workspaceNamespace}:${workspaceName}`;
        const attempts: number = TestConstants.TS_SELENIUM_WORKSPACE_STATUS_ATTEMPTS;
        const polling: number = TestConstants.TS_SELENIUM_WORKSPACE_STATUS_POLLING;
        const runningWorkspaceStatus: string = 'RUNNING';
        const stoppedWorkspaceStatus: string = 'STOPPED';
        const startingWorkspaceStatus: string = 'STARTING';

        const rest: rm.RestClient = new rm.RestClient('rest-samples')

        for (let i = 0; i < attempts; i++) {
            let isWorkspaceStarting: boolean = false;

            const response: rm.IRestResponse<any> = await rest.get(workspaceStatusApiUrl)

            if (response.statusCode !== 200) {
                await this.driverHelper.wait(polling)
                continue
            }

            const workspaceStatus: string = await response.result.status

            if (workspaceStatus === runningWorkspaceStatus) {
                return;
            }

            if (workspaceStatus === startingWorkspaceStatus) {
                isWorkspaceStarting = true;
            }

            if ((workspaceStatus === stoppedWorkspaceStatus) && isWorkspaceStarting) {
                throw new Error("Workspace starting process is crushed")
            }

            await this.driverHelper.wait(polling)
        }

        throw new Error('Exceeded the maximum number of checking attempts, workspace has not been run')
    }

}
