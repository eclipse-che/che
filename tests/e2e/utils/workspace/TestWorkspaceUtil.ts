/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import 'reflect-metadata';
import { che } from '@eclipse-che/api';
import { TestConstants } from '../../TestConstants';
import { injectable, inject } from 'inversify';
import { DriverHelper } from '../DriverHelper';
import { WorkspaceStatus } from './WorkspaceStatus';
import { error } from 'selenium-webdriver';
import { CheApiRequestHandler } from '../requestHandlers/CheApiRequestHandler';
import { CLASSES } from '../../inversify.types';
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
            Logger.error(`TestWorkspaceUtil.waitWorkspaceStatus is out of attempts. Expected status ${expectedStatus} not reached. Current status: ${workspaceStatus}`);
            throw new Error(`The workspace was not stopped in ${waitTime} ms. Currnet status is: ${workspaceStatus}`);
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

    // delete a worksapce without stopping phase (similar with force deleting)
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
            Logger.error(`TestWorkspaceUtil.deleteWorkspaceByName is out of attempts. Workspace was not yet deleted.`);
            throw new Error(`The workspace was not stopped in ${waitTime} ms.`);
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

    // stop all run workspaces, check statused and remove the workspaces
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

    /**
     * @deprecated Method deprecated. Works with CHE server only
     */
    public async waitPluginAdding(namespace: string, workspaceName: string, pluginName: string) {
        Logger.debug('TestWorkspaceUtil.waitPluginAdding');

        const workspaceStatusApiUrl: string = `${await this.apiUrlResolver.getWorkspacesApiUrl()}/${namespace}:${workspaceName}`;
        const attempts: number = TestConstants.TS_SELENIUM_PLUGIN_PRECENCE_ATTEMPTS;
        const polling: number = TestConstants.TS_SELENIUM_DEFAULT_POLLING;

        for (let i = 0; i < attempts; i++) {
            const response = await this.processRequestHandler.get(workspaceStatusApiUrl);

            if (response.status !== 200) {
                await this.driverHelper.wait(polling);
                continue;
            }

            const machines: string = JSON.stringify(response.data.runtime.machines);
            const isPluginPresent: boolean = machines.search(pluginName) > 0;

            if (isPluginPresent) {
                break;
            }

            if (i === attempts - 1) {
                throw new error.TimeoutError(`Exceeded maximum tries attempts, the '${pluginName}' plugin is not present in the workspace runtime.`);
            }

            await this.driverHelper.wait(polling);
        }
    }

    /**
     * @deprecated Method deprecated. Works with CHE server only
     */
    public async getListOfWorkspaceId(): Promise<string[]> {
        Logger.debug('TestWorkspaceUtil.getListOfWorkspaceId');

        const getAllWorkspacesResponse = await this.processRequestHandler.get(await this.apiUrlResolver.getWorkspacesApiUrl());

        interface IMyObj {
            id: string;
            status: string;
        }

        let stringified = JSON.stringify(getAllWorkspacesResponse.data);
        let arrayOfWorkspaces = <IMyObj[]>JSON.parse(stringified);
        let wsList: Array<string> = [];

        for (let entry of arrayOfWorkspaces) {
            wsList.push(entry.id);
        }

        return wsList;
    }

    /**
     * @deprecated Method deprecated. Works with CHE server only
     */
    public async getIdOfRunningWorkspace(wsName: string): Promise<string> {
        Logger.debug('TestWorkspaceUtil.getIdOfRunningWorkspace');

        const getWorkspacesByNameResponse = await this.processRequestHandler.get(`${await this.apiUrlResolver.getWorkspacesApiUrl()}/:${wsName}`);
        return getWorkspacesByNameResponse.data.id;

    }

    /**
     * @deprecated Method deprecated. Works with CHE server only
     */
    public async getIdOfRunningWorkspaces(): Promise<Array<string>> {
        Logger.debug('TestWorkspaceUtil.getIdOfRunningWorkspaces');

        try {
            const getAllWorkspacesResponse = await this.processRequestHandler.get(await this.apiUrlResolver.getWorkspacesApiUrl());

            interface IMyObj {
                id: string;
                status: string;
            }
            let stringified = JSON.stringify(getAllWorkspacesResponse.data);
            let arrayOfWorkspaces = <IMyObj[]>JSON.parse(stringified);
            let idOfRunningWorkspace: Array<string> = new Array();

            for (let entry of arrayOfWorkspaces) {
                if (entry.status === 'RUNNING') {
                    idOfRunningWorkspace.push(entry.id);
                }
            }

            return idOfRunningWorkspace;
        } catch (err) {
            console.log(`Getting id of running workspaces failed. URL used: ${await this.apiUrlResolver.getWorkspacesApiUrl()}`);
            throw err;
        }
    }

    /**
     * @deprecated Method deprecated. Works with CHE server only
     */
    public async removeWorkspaceById(id: string) {
        Logger.debug('TestWorkspaceUtil.removeWorkspaceById');

        const workspaceIdUrl: string = `${await this.apiUrlResolver.getWorkspacesApiUrl()}/${id}`;
        try {
            const deleteWorkspaceResponse = await this.processRequestHandler.delete(workspaceIdUrl);
            if (deleteWorkspaceResponse.status !== 204) {
                throw new Error(`Can not remove workspace. Code: ${deleteWorkspaceResponse.status} Data: ${deleteWorkspaceResponse.data}`);
            }
        } catch (err) {
            console.log(`Removing of workspace failed.`);
            throw err;
        }
    }

    /**
     * @deprecated Method deprecated. Works with CHE server only
     */
    public async stopWorkspaceById(id: string) {
        Logger.debug('TestWorkspaceUtil.stopWorkspaceById');

        const stopWorkspaceApiUrl: string = `${await this.apiUrlResolver.getWorkspacesApiUrl()}/${id}`;
        let stopWorkspaceResponse;

        try {
            stopWorkspaceResponse = await this.processRequestHandler.delete(`${stopWorkspaceApiUrl}`);
        } catch (err) {
            console.log(`Stop workspace call failed. URL used: ${stopWorkspaceApiUrl}`);
            throw err;
        }

        if (stopWorkspaceResponse.status !== 200) {
            throw new Error(`Can not stop workspace. Code: ${stopWorkspaceResponse.status} Data: ${stopWorkspaceResponse.data}`);
        }

        let stopped: boolean = false;
        let wsStatus = await this.processRequestHandler.get(stopWorkspaceApiUrl);
        for (let i = 0; i < TestConstants.TS_SELENIUM_PLUGIN_PRECENCE_ATTEMPTS; i++) {
            wsStatus = await this.processRequestHandler.get(stopWorkspaceApiUrl);
            if (wsStatus.data.status === WorkspaceStatus.STOPPED) {
                stopped = true;
                break;
            }
            await this.driverHelper.wait(TestConstants.TS_SELENIUM_DEFAULT_POLLING);
        }

        if (!stopped) {
            let waitTime = TestConstants.TS_SELENIUM_PLUGIN_PRECENCE_ATTEMPTS * TestConstants.TS_SELENIUM_DEFAULT_POLLING;
            throw new error.TimeoutError(`The workspace was not stopped in ${waitTime} ms. Currnet status is: ${wsStatus.data.status}`);
        }
    }

    /**
     * @deprecated Method deprecated. Works with CHE server only
     */
    public async cleanUpAllWorkspaces() {
        Logger.debug('TestWorkspaceUtil.cleanUpAllWorkspaces');

        let listOfRunningWorkspaces: Array<string> = await this.getIdOfRunningWorkspaces();
        for (const entry of listOfRunningWorkspaces) {
            await this.stopWorkspaceById(entry);
        }

        let listAllWorkspaces: Array<string> = await this.getListOfWorkspaceId();

        for (const entry of listAllWorkspaces) {
            await this.removeWorkspaceById(entry);
        }

    }

    /**
     * @deprecated Method deprecated. Works with CHE server only
     */
    public async cleanUpRunningWorkspace(workspaceName: string) {
        if (workspaceName === undefined || workspaceName.length === 0) {
            Logger.warn(`Could nod delete workspace because workspaceName is undefined or empty`);
            return;
        }

        Logger.debug(`TestWorkspaceUtil.cleanUpRunningWorkspace ${workspaceName}`);
        const workspaceID: string = await this.getIdOfRunningWorkspace(workspaceName);

        if (workspaceID === undefined || workspaceID.length === 0) {
            Logger.error(`Could nod delete workspace with name ${workspaceName} because workspaceID is undefined or empty`);
            return;
        }

        Logger.trace(`TestWorkspaceUtil.cleanUpRunningWorkspace Stopping workspace:${workspaceName} with ID:${workspaceID}`);
        await this.stopWorkspaceById(workspaceID);
        Logger.trace(`TestWorkspaceUtil.cleanUpRunningWorkspace Deleting workspace ${workspaceName}`);
        await this.removeWorkspaceById(workspaceID);
    }

    /**
     * @deprecated Method deprecated. Works with CHE server only
     */
    async createWsFromDevFile(customTemplate: che.workspace.devfile.Devfile) {
        Logger.debug('TestWorkspaceUtil.createWsFromDevFile');

        try {
            await this.processRequestHandler.post(await this.apiUrlResolver.getWorkspacesApiUrl() + '/devfile', customTemplate);
        } catch (error) {
            console.error(error);
            throw error;
        }
    }

    /**
     * @deprecated Method deprecated. Works with CHE server only
     */
    async getBaseDevfile(): Promise<che.workspace.devfile.Devfile> {
        Logger.debug('TestWorkspaceUtil.getBaseDevfile');

        const baseDevfile: che.workspace.devfile.Devfile = {
            apiVersion: '1.0.0',
            metadata: {
                name: 'test-workspace'
            }
        };

        return baseDevfile;
    }

    /**
     * @deprecated Method deprecated. Works with CHE server only
     */
    async startWorkspace(workspaceId: string) {
        Logger.debug('TestWorkspaceUtil.startWorkspace');

        try {
            await this.processRequestHandler.post(`${await this.apiUrlResolver.getWorkspacesApiUrl()}/${workspaceId}/runtime`);
        } catch (error) {
            console.error(error);
            throw error;
        }
    }

}
