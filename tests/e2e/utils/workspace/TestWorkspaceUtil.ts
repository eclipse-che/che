/*********************************************************************
 * Copyright (c) 2019-2023 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import 'reflect-metadata';
import { TestConstants } from '../../constants/TestConstants';
import { injectable, inject } from 'inversify';
import { DriverHelper } from '../DriverHelper';
import { WorkspaceStatus } from './WorkspaceStatus';
import { error } from 'selenium-webdriver';
import { CheApiRequestHandler } from '../request-handlers/CheApiRequestHandler';
import { CLASSES } from '../../configs/inversify.types';
import { Logger } from '../Logger';
import axios from 'axios';
import { ITestWorkspaceUtil } from './ITestWorkspaceUtil';
import { ApiUrlResolver } from './ApiUrlResolver';

@injectable()
export class TestWorkspaceUtil implements ITestWorkspaceUtil {
    readonly attempts: number = TestConstants.TS_SELENIUM_WORKSPACE_STATUS_ATTEMPTS;
    readonly polling: number = TestConstants.TS_SELENIUM_WORKSPACE_STATUS_POLLING;

    constructor(
        @inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper,
        @inject(CLASSES.CheApiRequestHandler) private readonly processRequestHandler: CheApiRequestHandler,
        @inject(CLASSES.ApiUrlResolver) private readonly apiUrlResolver: ApiUrlResolver
    ) { }

    public async waitWorkspaceStatus(workspaceName: string, expectedWorkspaceStatus: WorkspaceStatus) {
        Logger.debug('TestWorkspaceUtil.waitWorkspaceStatus');

        let workspaceStatus: string = '';
        let expectedStatus: boolean = false;
        for (let i = 0; i < this.attempts; i++) {
            const response = await this.processRequestHandler.get(await this.apiUrlResolver.getWorkspaceApiUrl(workspaceName));

            if (response.status !== 200) {
                throw new Error(`Can not get status of a workspace. Code: ${response.status} Data: ${response.data}`);
            }

            workspaceStatus = await response.data.status.phase;

            if (workspaceStatus === expectedWorkspaceStatus) {
                expectedStatus = true;
                break;
            }

            await this.driverHelper.wait(this.polling);
        }

        if (!expectedStatus) {
            let waitTime = this.attempts * this.polling;
            throw new error.TimeoutError(`The workspace was not stopped in ${waitTime} ms. Currnet status is: ${workspaceStatus}`);
        }
    }

    public async stopWorkspaceByName(workspaceName: string) {
        Logger.debug('TestWorkspaceUtil.stopWorkspaceByName');

        const stopWorkspaceApiUrl: string = await this.apiUrlResolver.getWorkspaceApiUrl(workspaceName);
        let stopWorkspaceResponse;

        try {
            stopWorkspaceResponse = await this.processRequestHandler.patch(stopWorkspaceApiUrl, [{'op': 'replace', 'path': '/spec/started', 'value': false}]);
        } catch (err) {
            console.log(`Stop workspace call failed. URL used: ${stopWorkspaceApiUrl}`);
            throw err;
        }

        if (stopWorkspaceResponse.status !== 200) {
            throw new Error(`Cannot stop workspace. Code: ${stopWorkspaceResponse.status} Data: ${stopWorkspaceResponse.data}`);
        }

        await this.waitWorkspaceStatus(workspaceName, WorkspaceStatus.STOPPED);
    }

    // delete a workspace without stopping phase (similar with force deleting)
    public async deleteWorkspaceByName(workspaceName: string) {
        Logger.debug(`TestWorkspaceUtil.deleteWorkspaceByName ${workspaceName}` );

        const deleteWorkspaceApiUrl: string = await this.apiUrlResolver.getWorkspaceApiUrl(workspaceName);
        let deleteWorkspaceResponse;
        let deleteWorkspaceStatus: boolean = false;
        try {
            deleteWorkspaceResponse = await this.processRequestHandler.delete(deleteWorkspaceApiUrl);
        } catch (error) {
            if (axios.isAxiosError(error) && error.response?.status === 404) {
                Logger.error(`The workspace :${workspaceName} not found`);
                throw error;
            }
            Logger.error(`Delete workspace call failed. URL used: ${deleteWorkspaceStatus}`);
            throw error;
        }

        if (deleteWorkspaceResponse.status !== 204) {
            throw new Error(`Can not delete workspace. Code: ${deleteWorkspaceResponse.status} Data: ${deleteWorkspaceResponse.data}`);
        }

        for (let i = 0; i < this.attempts; i++) {
            try {
                deleteWorkspaceResponse = await this.processRequestHandler.get(deleteWorkspaceApiUrl);
            } catch (error) {
                if (axios.isAxiosError(error) && error.response?.status === 404) {
                    deleteWorkspaceStatus = true;
                    break;
                }
            }
        }

        if (!deleteWorkspaceStatus) {
            let waitTime = this.attempts * this.polling;
            throw new error.TimeoutError(`The workspace was not stopped in ${waitTime} ms.`);
        }
    }

    // stop workspace before deleting with checking stopping phase
    public async stopAndDeleteWorkspaceByName(workspaceName: string) {
        Logger.debug('TestWorkspaceUtil.stopAndDeleteWorkspaceByName');

        await this.stopWorkspaceByName(workspaceName);
        await this.deleteWorkspaceByName(workspaceName);
    }

    // stop all run workspaces in the namespace
    public async stopAllRunningWorkspaces(namespace: string) {
        Logger.debug('TestWorkspaceUtil.stopAllRunProjects');
        let response = await this.processRequestHandler.get(await this.apiUrlResolver.getWorkspacesApiUrl());
        for (let i = 0; i < response.data.items.length; i++) {
            Logger.info('The project is being stopped: ' +  response.data.items[i].metadata.name);
            await this.stopWorkspaceByName(response.data.items[i].metadata.name);
        }
    }

    // stop all run workspaces, check statuses and remove the workspaces
    public async stopAndDeleteAllRunningWorkspaces(namespace: string) {
        Logger.debug('TestWorkspaceUtil.stopAndDeleteAllRunProjects');
        let response = await this.processRequestHandler.get(await this.apiUrlResolver.getWorkspacesApiUrl());
        await this.stopAllRunningWorkspaces(namespace);
        for (let i = 0; i < response.data.items.length; i++) {
            Logger.info('The project is being deleted: ' +  response.data.items[i].metadata.name);
            await this.deleteWorkspaceByName(response.data.items[i].metadata.name);
        }
    }

    // stop all run workspaces without stopping and waiting for of 'Stopped' phase
    // similar with 'force' deleting
    public async deleteAllWorkspaces(namespace: string) {
        Logger.debug('TestWorkspaceUtil.deleteAllRunProjects');
        let response = await this.processRequestHandler.get(await this.apiUrlResolver.getWorkspacesApiUrl());

        for (let i = 0; i < response.data.items.length; i++) {
            Logger.info('The project is being deleted .......: ' +  response.data.items[i].metadata.name);
            await this.deleteWorkspaceByName(response.data.items[i].metadata.name);
        }
    }
}
