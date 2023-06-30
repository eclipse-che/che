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
import { injectable, inject } from 'inversify';
import { DriverHelper } from '../DriverHelper';
import { WorkspaceStatus } from './WorkspaceStatus';
import { error } from 'selenium-webdriver';
import { CheApiRequestHandler } from '../request-handlers/CheApiRequestHandler';
import { CLASSES } from '../../configs/inversify.types';
import { Logger } from '../Logger';
import axios, { AxiosResponse } from 'axios';
import { ITestWorkspaceUtil } from './ITestWorkspaceUtil';
import { ApiUrlResolver } from './ApiUrlResolver';
import { TimeoutConstants } from '../../constants/TimeoutConstants';

@injectable()
export class TestWorkspaceUtil implements ITestWorkspaceUtil {
    readonly polling: number = TimeoutConstants.TS_SELENIUM_DEFAULT_POLLING;
    readonly attempts: number = TimeoutConstants.TS_DASHBOARD_WORKSPACE_STOP_TIMEOUT / this.polling;

    constructor(
        @inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper,
        @inject(CLASSES.CheApiRequestHandler) private readonly processRequestHandler: CheApiRequestHandler,
        @inject(CLASSES.ApiUrlResolver) private readonly apiUrlResolver: ApiUrlResolver
    ) { }

    async waitWorkspaceStatus(workspaceName: string, expectedWorkspaceStatus: WorkspaceStatus): Promise<void> {
        Logger.debug('TestWorkspaceUtil.waitWorkspaceStatus');

        let workspaceStatus: string = '';
        let expectedStatus: boolean = false;
        for (let i: number = 0; i < this.attempts; i++) {
            const response: AxiosResponse = await this.processRequestHandler.get(await this.apiUrlResolver.getWorkspaceApiUrl(workspaceName));

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
            let waitTime: number = this.attempts * this.polling;
            throw new error.TimeoutError(`The workspace was not stopped in ${waitTime} ms. Current status is: ${workspaceStatus}`);
        }
    }

    async stopWorkspaceByName(workspaceName: string): Promise<void> {
        Logger.debug(`TestWorkspaceUtil.stopWorkspaceByName: ${workspaceName}`);

        const stopWorkspaceApiUrl: string = await this.apiUrlResolver.getWorkspaceApiUrl(workspaceName);
        let stopWorkspaceResponse: AxiosResponse;

        try {
            stopWorkspaceResponse = await this.processRequestHandler.patch(stopWorkspaceApiUrl, [{'op': 'replace', 'path': '/spec/started', 'value': false}]);
        } catch (err) {
            Logger.error(`Stop workspace call failed. URL used: ${stopWorkspaceApiUrl}`);
            throw err;
        }

        if (stopWorkspaceResponse.status !== 200) {
            throw new Error(`Cannot stop workspace. Code: ${stopWorkspaceResponse.status} Data: ${stopWorkspaceResponse.data}`);
        }

        await this.waitWorkspaceStatus(workspaceName, WorkspaceStatus.STOPPED);
        Logger.debug(`TestWorkspaceUtil.stopWorkspaceByName: ${workspaceName} stopped successfully`);
    }

    // delete a workspace without stopping phase (similar with force deleting)
    async deleteWorkspaceByName(workspaceName: string): Promise<void> {
        Logger.debug(`TestWorkspaceUtil.deleteWorkspaceByName: ${workspaceName}` );

        const deleteWorkspaceApiUrl: string = await this.apiUrlResolver.getWorkspaceApiUrl(workspaceName);
        let deleteWorkspaceResponse: AxiosResponse;
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

        for (let i: number = 0; i < this.attempts; i++) {
            try {
                deleteWorkspaceResponse = await this.processRequestHandler.get(deleteWorkspaceApiUrl);
            } catch (error) {
                if (axios.isAxiosError(error) && error.response?.status === 404) {
                    deleteWorkspaceStatus = true;
                    Logger.debug(`TestWorkspaceUtil.stopWorkspaceByName: ${workspaceName} deleted successfully`);
                    break;
                }
            }
        }

        if (!deleteWorkspaceStatus) {
            let waitTime: number = this.attempts * this.polling;
            throw new error.TimeoutError(`The workspace was not deleted in ${waitTime} ms.`);
        }
    }

    // stop workspace before deleting with checking stopping phase
    async stopAndDeleteWorkspaceByName(workspaceName: string): Promise<void> {
        Logger.debug('TestWorkspaceUtil.stopAndDeleteWorkspaceByName');

        await this.stopWorkspaceByName(workspaceName);
        await this.deleteWorkspaceByName(workspaceName);
    }

    // stop all run workspaces in the namespace
    async stopAllRunningWorkspaces(namespace: string): Promise<void> {
        Logger.debug('TestWorkspaceUtil.stopAllRunProjects');
        let response: AxiosResponse = await this.processRequestHandler.get(await this.apiUrlResolver.getWorkspacesApiUrl());
        for (let i: number = 0; i < response.data.items.length; i++) {
            Logger.info('The project is being stopped: ' +  response.data.items[i].metadata.name);
            await this.stopWorkspaceByName(response.data.items[i].metadata.name);
        }
    }

    // stop all run workspaces, check statuses and remove the workspaces
    async stopAndDeleteAllRunningWorkspaces(namespace: string): Promise<void> {
        Logger.debug('TestWorkspaceUtil.stopAndDeleteAllRunProjects');
        let response: AxiosResponse = await this.processRequestHandler.get(await this.apiUrlResolver.getWorkspacesApiUrl());
        await this.stopAllRunningWorkspaces(namespace);
        for (let i: number = 0; i < response.data.items.length; i++) {
            Logger.info('The project is being deleted: ' +  response.data.items[i].metadata.name);
            await this.deleteWorkspaceByName(response.data.items[i].metadata.name);
        }
    }

    // stop all run workspaces without stopping and waiting for of 'Stopped' phase
    // similar with 'force' deleting
    async deleteAllWorkspaces(namespace: string): Promise<void> {
        Logger.debug('TestWorkspaceUtil.deleteAllRunProjects');
        let response: AxiosResponse = await this.processRequestHandler.get(await this.apiUrlResolver.getWorkspacesApiUrl());

        for (let i: number = 0; i < response.data.items.length; i++) {
            Logger.info('The project is being deleted .......: ' +  response.data.items[i].metadata.name);
            await this.deleteWorkspaceByName(response.data.items[i].metadata.name);
        }
    }
}
