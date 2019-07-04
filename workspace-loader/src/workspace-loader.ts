/*
 * Copyright (c) 2018-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
'use strict';

import { WebsocketClient } from './json-rpc/websocket-client';
import { CheJsonRpcMasterApi, WorkspaceStatusChangedEvent } from './json-rpc/che-json-rpc-master-api';
import { Loader } from './loader/loader';
import { che } from '@eclipse-che/api';

// tslint:disable:no-any

const WEBSOCKET_CONTEXT = '/api/websocket';

export class WorkspaceLoader {

    workspace: che.workspace.Workspace;
    startAfterStopping = false;

    constructor(private readonly loader: Loader,
        private readonly keycloak?: any) {
        /** Ask dashboard to show the IDE. */
        window.parent.postMessage('show-ide', '*');
    }

    async load(): Promise<void> {
        const workspaceKey = this.getWorkspaceKey();

        if (!workspaceKey || workspaceKey === '') {
            console.error('Workspace is not defined');
            return;
        }

        try {
            this.workspace = await this.getWorkspace(workspaceKey);
            await this.handleWorkspace();
            await this.openIDE();
        } catch (err) {
            if (err) {
                console.error(err);
                this.loader.error(err);
            } else {
                this.loader.error('Unknown error has happened, try to reload page');
            }
            this.loader.hideLoader();
            this.loader.showReload();
        }
    }

    /**
     * Returns workspace key from current address or empty string when it is undefined.
     */
    getWorkspaceKey(): string {
        const result: string = window.location.pathname.substr(1);
        return result.substr(result.indexOf('/') + 1, result.length);
    }

    /**
     * Returns base websocket URL.
     */
    websocketBaseURL(): string {
        const wsProtocol = 'http:' === document.location.protocol ? 'ws' : 'wss';
        return wsProtocol + '://' + document.location.host;
    }

    /**
     * Returns query string.
     */
    getQueryString(): string {
        return location.search;
    }

    /**
     * Get workspace by ID.
     *
     * @param workspaceId workspace id
     */
    getWorkspace(workspaceId: string): Promise<che.workspace.Workspace> {
        const request = new XMLHttpRequest();
        request.open('GET', '/api/workspace/' + workspaceId);
        return this.setAuthorizationHeader(request).then((xhr: XMLHttpRequest) =>
            new Promise<che.workspace.Workspace>((resolve, reject) => {
                xhr.send();
                xhr.onreadystatechange = () => {
                    if (xhr.readyState !== 4) { return; }
                    if (xhr.status !== 200) {
                        const errorMessage = 'Failed to get the workspace: "' + this.getRequestErrorMessage(xhr) + '"';
                        reject(new Error(errorMessage));
                        return;
                    }
                    resolve(JSON.parse(xhr.responseText));
                };
            }));
    }

    /**
     * Start current workspace.
     */
    startWorkspace(): Promise<che.workspace.Workspace> {
        const request = new XMLHttpRequest();
        request.open('POST', `/api/workspace/${this.workspace.id}/runtime`);
        return this.setAuthorizationHeader(request).then((xhr: XMLHttpRequest) =>
            new Promise<che.workspace.Workspace>((resolve, reject) => {
                xhr.send();
                xhr.onreadystatechange = () => {
                    if (xhr.readyState !== 4) { return; }
                    if (xhr.status !== 200) {
                        const errorMessage = 'Failed to start the workspace: "' + this.getRequestErrorMessage(xhr) + '"';
                        reject(new Error(errorMessage));
                        return;
                    }
                    resolve(JSON.parse(xhr.responseText));
                };
            }));
    }

    getRequestErrorMessage(xhr: XMLHttpRequest): string {
        let errorMessage;
        try {
            const response = JSON.parse(xhr.responseText);
            errorMessage = response.message;
        } catch (e) { }

        if (errorMessage) {
            return errorMessage;
        }

        if (xhr.statusText) {
            return xhr.statusText;
        }

        return 'Unknown error';
    }

    /**
     * Handles workspace status.
     */
    handleWorkspace(): Promise<void> {
        if (this.workspace.status === 'RUNNING') {
            return new Promise((resolve, reject) => {
                this.checkWorkspaceRuntime().then(resolve, reject);
            });
        } else if (this.workspace.status === 'STOPPING') {
            this.startAfterStopping = true;
        }

        const masterApiConnectionPromise = new Promise((resolve, reject) => {
            if (this.workspace.status === 'STOPPED') {
                this.startWorkspace().then(resolve, reject);
            } else {
                resolve();
            }
        }).then(() => this.connectMasterApi());

        const runningOnConnectionPromise = masterApiConnectionPromise
            .then((masterApi: CheJsonRpcMasterApi) =>
                new Promise((resolve, reject) => {
                    masterApi.addListener('open', () => {
                        this.checkWorkspaceRuntime().then(resolve, reject);
                    });
                }));

        const runningOnStatusChangePromise = masterApiConnectionPromise
            .then((masterApi: CheJsonRpcMasterApi) =>
                this.subscribeWorkspaceEvents(masterApi));

        return Promise.race([runningOnConnectionPromise, runningOnStatusChangePromise]);
    }

    /**
     * Shows environment outputs.
     *
     * @param message output message
     */
    onEnvironmentOutput(message): void {
        this.loader.log(message);
    }

    connectMasterApi(): Promise<CheJsonRpcMasterApi> {
        return new Promise((resolve, reject) => {
            const entryPoint = this.websocketBaseURL() + WEBSOCKET_CONTEXT;
            const master = new CheJsonRpcMasterApi(new WebsocketClient(), entryPoint, this);
            master.connect()
                .then(() => resolve(master))
                .catch((error: any) => reject(error));
        });
    }

    /**
     * Subscribes to the workspace events.
     */
    subscribeWorkspaceEvents(masterApi: CheJsonRpcMasterApi): Promise<any> {
        return new Promise((resolve, reject) => {
            masterApi.subscribeEnvironmentOutput(this.workspace.id,
                (message: any) => this.onEnvironmentOutput(message.text));
            masterApi.subscribeInstallerOutput(this.workspace.id,
                (message: any) => this.onEnvironmentOutput(message.text));
            masterApi.subscribeWorkspaceStatus(this.workspace.id,
                (message: WorkspaceStatusChangedEvent) => {
                    if (message.error) {
                        reject(new Error(`Failed to run the workspace: "${message.error}"`));
                    } else if (message.status === 'RUNNING') {
                        this.checkWorkspaceRuntime().then(resolve, reject);
                    } else if (message.status === 'STOPPED') {
                        if (message.prevStatus === 'STARTING') {
                            this.loader.error('Workspace stopped.');
                            this.loader.hideLoader();
                            this.loader.showReload();
                        }
                        if (this.startAfterStopping) {
                            this.startWorkspace().catch((error: any) => reject(error));
                        }
                    }
                });
        });
    }

    checkWorkspaceRuntime(): Promise<any> {
        return new Promise((resolve, reject) => {
            this.getWorkspace(this.workspace.id).then(workspace => {
                if (workspace.status === 'RUNNING') {
                    if (workspace.runtime) {
                        resolve();
                    } else {
                        reject(new Error('You do not have permissions to access workspace runtime, in this case IDE cannot be loaded.'));
                    }
                }
            });
        });
    }

    /**
     * Opens IDE for the workspace.
     */
    openIDE(): void {
        this.getWorkspace(this.workspace.id).then(workspace => {
            const machines = workspace.runtime.machines || [];
            for (const machineName of Object.keys(machines)) {
                const servers = machines[machineName].servers || [];
                for (const serverId of Object.keys(servers)) {
                    const attributes = servers[serverId].attributes;
                    if (attributes['type'] === 'ide') {
                        this.openURL(servers[serverId].url + this.getQueryString());
                        return;
                    }
                }
            }
            this.openURL(workspace.links.ide + this.getQueryString());
        });
    }

    /**
     * Schedule opening URL.
     * Scheduling prevents appearing an error net::ERR_CONNECTION_REFUSED instead opening the URL.
     *
     * @param url url to be opened
     */
    openURL(url: string): void {
        // Preconfigured IDE may use dedicated port. In this case Chrome browser fails
        // with error net::ERR_CONNECTION_REFUSED. Timer helps to open the URL without errors.
        setTimeout(() => {
            window.location.href = url;
        }, 100);
    }

    setAuthorizationHeader(xhr: XMLHttpRequest): Promise<XMLHttpRequest> {
        return new Promise((resolve, reject) => {
            if (this.keycloak && this.keycloak.token) {
                this.keycloak.updateToken(5).success(() => {
                    xhr.setRequestHeader('Authorization', 'Bearer ' + this.keycloak.token);
                    resolve(xhr);
                }).error(() => {
                    console.log('Failed to refresh token');
                    window.sessionStorage.setItem('oidcIdeRedirectUrl', location.href);
                    this.keycloak.login();
                    reject(new Error('Failed to refresh token'));
                });
            }

            resolve(xhr);
        });
    }

    getAuthenticationToken(): string {
        return this.keycloak && this.keycloak.token ? '?token=' + this.keycloak.token : '';
    }

}
