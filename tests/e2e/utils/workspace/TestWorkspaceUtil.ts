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
import { WorkspaceStatus } from './WorkspaceStatus';
import { ITestWorkspaceUtil } from './ITestWorkspaceUtil';
import axios from 'axios';
import querystring from 'querystring';
import { error } from 'selenium-webdriver';
enum RequestType {
    GET,
    POST,
    DELETE
}

@injectable()
export class TestWorkspaceUtil implements ITestWorkspaceUtil {

    workspaceApiUrl: string = `${TestConstants.TS_SELENIUM_BASE_URL}/api/workspace`;

    constructor(@inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper,
        private readonly rest: rm.RestClient = new rm.RestClient('rest-samples')) {

    }

    public async waitWorkspaceStatus(namespace: string, workspaceName: string, expectedWorkspaceStatus: WorkspaceStatus) {
        const workspaceStatusApiUrl: string = `${this.workspaceApiUrl}/${namespace}:${workspaceName}`;
        const attempts: number = TestConstants.TS_SELENIUM_WORKSPACE_STATUS_ATTEMPTS;
        const polling: number = TestConstants.TS_SELENIUM_WORKSPACE_STATUS_POLLING;
        let workspaceStatus: string = '';

        for (let i = 0; i < attempts; i++) {
            const response: rm.IRestResponse<any> = await this.processRequest(RequestType.GET, workspaceStatusApiUrl);

            if (response.statusCode !== 200) {
                await this.driverHelper.wait(polling);
                continue;
            }

            workspaceStatus = await response.result.status;

            if (workspaceStatus === expectedWorkspaceStatus) {
                return;
            }

            await this.driverHelper.wait(polling);
        }

        throw new error.TimeoutError(`Exceeded the maximum number of checking attempts, workspace status is: '${workspaceStatus}' different to '${expectedWorkspaceStatus}'`);
    }

    public async waitPluginAdding(namespace: string, workspaceName: string, pluginName: string) {
        const workspaceStatusApiUrl: string = `${this.workspaceApiUrl}/${namespace}:${workspaceName}`;
        const attempts: number = TestConstants.TS_SELENIUM_PLUGIN_PRECENCE_ATTEMPTS;
        const polling: number = TestConstants.TS_SELENIUM_PLUGIN_PRECENCE_POLLING;

        for (let i = 0; i < attempts; i++) {
            const response: rm.IRestResponse<any> = await this.processRequest(RequestType.GET, workspaceStatusApiUrl);

            if (response.statusCode !== 200) {
                await this.driverHelper.wait(polling);
                continue;
            }

            const machines: string = JSON.stringify(response.result.runtime.machines);
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

    public async getListOfWorkspaceId() {
        const getAllWorkspacesResponse: rm.IRestResponse<any> = await this.processRequest(RequestType.GET, this.workspaceApiUrl);

        interface IMyObj {
            id: string;
            status: string;
        }
        let stringified = JSON.stringify(getAllWorkspacesResponse.result);
        let arrayOfWorkspaces = <IMyObj[]>JSON.parse(stringified);
        let wsList: Array<string> = [];
        for (let entry of arrayOfWorkspaces) {
            wsList.push(entry.id);
        }
        return wsList;
    }

    public async getIdOfRunningWorkspaces(): Promise<Array<string>> {
        try {
            const getAllWorkspacesResponse: rm.IRestResponse<any> = await this.processRequest(RequestType.GET, this.workspaceApiUrl);

            interface IMyObj {
                id: string;
                status: string;
            }
            let stringified = JSON.stringify(getAllWorkspacesResponse.result);
            let arrayOfWorkspaces = <IMyObj[]>JSON.parse(stringified);
            let idOfRunningWorkspace: Array<string> = new Array();

            for (let entry of arrayOfWorkspaces) {
                if (entry.status === 'RUNNING') {
                    idOfRunningWorkspace.push(entry.id);
                }
            }

            return idOfRunningWorkspace;
        } catch (err) {
            console.log(`Getting id of running workspaces failed. URL used: ${this.workspaceApiUrl}`);
            throw err;
        }

    }

    public async removeWorkspaceById(id: string) {
        const workspaceIdUrl: string = `${this.workspaceApiUrl}/${id}`;
        const attempts: number = TestConstants.TS_SELENIUM_PLUGIN_PRECENCE_ATTEMPTS;
        const polling: number = TestConstants.TS_SELENIUM_PLUGIN_PRECENCE_POLLING;
        let stopped: Boolean = false;

        for (let i = 0; i < attempts; i++) {

            const getInfoResponse: rm.IRestResponse<any> = await this.processRequest(RequestType.GET, workspaceIdUrl);

            if (getInfoResponse.result.status === 'STOPPED') {
                stopped = true;
                break;
            }
            await this.driverHelper.wait(polling);
        }

        if (stopped) {
            try {
                const deleteWorkspaceResponse: rm.IRestResponse<any> = await this.processRequest(RequestType.DELETE, workspaceIdUrl);

                // response code 204: "No Content" expected
                if (deleteWorkspaceResponse.statusCode !== 204) {
                    throw new Error(`Can not remove workspace. Code: ${deleteWorkspaceResponse.statusCode} Result: ${deleteWorkspaceResponse.result}`);
                }
            } catch (err) {
                console.log(`Removing of workspace failed.`);
                throw err;
            }
        } else {
            throw new Error(`Can not remove workspace with id ${id}, because it is still not in STOPPED state.`);
        }
    }

    async getCheBearerToken(): Promise<string> {
        let params = {};

        let keycloakUrl = TestConstants.TS_SELENIUM_BASE_URL;
        if ( keycloakUrl.substr(7, 4).includes('che')) {
            const keycloakAuthSuffix = '/auth/realms/che/protocol/openid-connect/token';
            keycloakUrl = keycloakUrl.replace('che', 'keycloak') + keycloakAuthSuffix;
            params = {
                client_id: 'che-public',
                username: TestConstants.TS_SELENIUM_USERNAME,
                password: TestConstants.TS_SELENIUM_PASSWORD,
                grant_type: 'password'
            };
        } else {
            const keycloakAuthSuffix = '/auth/realms/codeready/protocol/openid-connect/token';
            keycloakUrl = keycloakUrl.replace('codeready', 'keycloak') + keycloakAuthSuffix;
            params = {
                client_id: 'codeready-public',
                username: TestConstants.TS_SELENIUM_USERNAME,
                password: TestConstants.TS_SELENIUM_PASSWORD,
                grant_type: 'password'
            };
        }

        try {
            const responseToObtainBearerToken = await axios.post(keycloakUrl, querystring.stringify(params));
            return responseToObtainBearerToken.data.access_token;
        } catch (err) {
            console.log(`Can not get bearer token. URL used: ${keycloakUrl}`);
            throw err;
        }

    }

    public async stopWorkspaceById(id: string) {
        const stopWorkspaceApiUrl: string = `${this.workspaceApiUrl}/${id}/runtime`;
        try {
            const stopWorkspaceResponse: rm.IRestResponse<any> = await this.processRequest(RequestType.DELETE, stopWorkspaceApiUrl);

            // response code 204: "No Content" expected
            if (stopWorkspaceResponse.statusCode !== 204) {
                throw new Error(`Can not stop workspace. Code: ${stopWorkspaceResponse.statusCode} Result: ${stopWorkspaceResponse.result}`);
            }
        } catch (err) {
            console.log(`Stopping workspace failed. URL used: ${stopWorkspaceApiUrl}`);
            throw err;
        }

    }

    public async cleanUpAllWorkspaces() {
        let listOfRunningWorkspaces: Array<string> = await this.getIdOfRunningWorkspaces();
        for (const entry of listOfRunningWorkspaces) {
            await this.stopWorkspaceById(entry);
        }

        let listAllWorkspaces: Array<string> = await this.getListOfWorkspaceId();

        for (const entry of listAllWorkspaces) {
            this.removeWorkspaceById(entry);
        }

    }

    removeWorkspace(namespace: string, workspaceId: string): void {
        throw new Error('Method not implemented.');
    }

    stopWorkspace(namespace: string, workspaceId: string): void {
        throw new Error('Method not implemented.');
    }

    async processRequest(reqType: RequestType, url: string) {
        let response: rm.IRestResponse<any>;
        if (TestConstants.TS_SELENIUM_MULTIUSER === true) {
            switch (reqType) {
                case RequestType.GET: {
                    response = await this.rest.get(url, { additionalHeaders: { 'Authorization': 'Bearer ' + await this.getCheBearerToken() } });
                    break;
                }
                case RequestType.DELETE: {
                    response = await this.rest.del(url, { additionalHeaders: { 'Authorization': 'Bearer ' + await this.getCheBearerToken() } });
                    break;
                }
                default: {
                    throw new Error('Unknown RequestType: ' + reqType);
                }
            }
        } else {
            switch (reqType) {
                case RequestType.GET: {
                    response = await this.rest.get(url);
                    break;
                }
                case RequestType.DELETE: {
                    response = await this.rest.del(url);
                    break;
                }
                default: {
                    throw new Error('Unknown RequestType: ' + reqType);
                }
            }
        }
        return response;
    }

}
