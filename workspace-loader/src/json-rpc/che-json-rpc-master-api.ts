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

import { che } from '@eclipse-che/api';
import { CheJsonRpcApiClient } from './che-json-rpc-api-service';
import { ICommunicationClient, CODE_REQUEST_TIMEOUT, CommunicationClientEvent } from './json-rpc-client';
import { WorkspaceLoader } from '../workspace-loader';

enum MasterChannels {
    ENVIRONMENT_OUTPUT = 'runtime/log',
    ENVIRONMENT_STATUS = 'machine/statusChanged',
    INSTALLER_OUTPUT = 'installer/log',
    WORKSPACE_STATUS = 'workspace/statusChanged'
}
const SUBSCRIBE: string = 'subscribe';
const UNSUBSCRIBE: string = 'unsubscribe';

export interface WorkspaceStatusChangedEvent {
    status: string;
    prevStatus: string;
    workspaceId: string;
    error: string;
}

/**
 * Client API for workspace master interactions.
 *
 * @author Ann Shumilova
 */
export class CheJsonRpcMasterApi {
    private cheJsonRpcApi: CheJsonRpcApiClient;
    private clientId: string;

    private checkingInterval: number;
    private checkingDelay = 10000;
    private fetchingClientIdTimeout = 5000;

    constructor(private readonly client: ICommunicationClient,
        private readonly entryPoint: string,
        private readonly loader: WorkspaceLoader) {
        this.cheJsonRpcApi = new CheJsonRpcApiClient(client);

        client.addListener('open', () => this.onConnectionOpen());
    }

    addListener(eventType: CommunicationClientEvent, handler: Function): void {
        this.client.addListener(eventType, handler);
    }

    removeListener(eventType: CommunicationClientEvent, handler: Function): void {
        this.client.removeListener(eventType, handler);
    }

    onConnectionOpen(): void {
        if (this.checkingInterval) {
            clearInterval(this.checkingInterval);
            this.checkingInterval = undefined;
        }

        this.checkingInterval = setInterval(() => {
            let isAlive = false;
            const fetchClientPromise = new Promise(resolve => {
                this.fetchClientId().then(() => {
                    isAlive = true;
                    resolve(isAlive);
                }, () => {
                    isAlive = false;
                    resolve(isAlive);
                });
            });

            // this is timeout of fetchClientId request
            const fetchClientTimeoutPromise = new Promise(resolve => {
                setTimeout(() => {
                    resolve(isAlive);
                }, this.fetchingClientIdTimeout);
            });

            Promise.race([fetchClientPromise, fetchClientTimeoutPromise]).then((_isAlive: boolean) => {
                if (_isAlive) {
                    return;
                }

                clearInterval(this.checkingInterval);
                this.checkingInterval = undefined;

                this.client.disconnect(CODE_REQUEST_TIMEOUT);
            });

        }, this.checkingDelay);
    }

    /**
     * Opens connection to pointed entryPoint.
     *
     * @returns {Promise<void>}
     */
    connect(): Promise<void> {
        const entryPointFunction = () => {
            const entryPoint = this.entryPoint + this.loader.getAuthenticationToken();
            if (this.clientId) {
                let clientId = `clientId=${this.clientId}`;
                // in case of reconnection
                // we need to test entrypoint on existing query parameters
                // to add already gotten clientId
                if (/\?/.test(entryPoint) === false) {
                    clientId = '?' + clientId;
                } else {
                    clientId = '&' + clientId;
                }
                return entryPoint + clientId;
            }
            return entryPoint;
        };

        return this.cheJsonRpcApi.connect(entryPointFunction).then(() =>
            this.fetchClientId()
        );
    }

    /**
     * Subscribes the environment output.
     *
     * @param workspaceId workspace's id
     * @param machineName machine's name
     * @param callback callback to process event
     */
    subscribeEnvironmentOutput(workspaceId: string, callback: Function): void {
        this.subscribe(MasterChannels.ENVIRONMENT_OUTPUT, workspaceId, callback);
    }

    /**
     * Un-subscribes the pointed callback from the environment output.
     *
     * @param workspaceId workspace's id
     * @param machineName machine's name
     * @param callback callback to process event
     */
    unSubscribeEnvironmentOutput(workspaceId: string, callback: Function): void {
        this.unsubscribe(MasterChannels.ENVIRONMENT_OUTPUT, workspaceId, callback);
    }

    /**
     * Subscribes the environment status changed.
     *
     * @param workspaceId workspace's id
     * @param callback callback to process event
     */
    subscribeEnvironmentStatus(workspaceId: string, callback: Function): void {
        this.subscribe(MasterChannels.ENVIRONMENT_STATUS, workspaceId, callback);
    }

    /**
     * Un-subscribes the pointed callback from environment status changed.
     *
     * @param workspaceId workspace's id
     * @param callback callback to process event
     */
    unSubscribeEnvironmentStatus(workspaceId: string, callback: Function): void {
        this.unsubscribe(MasterChannels.ENVIRONMENT_STATUS, workspaceId, callback);
    }

    /**
     * Subscribes on workspace agent output.
     *
     * @param workspaceId workspace's id
     * @param callback callback to process event
     */
    subscribeInstallerOutput(workspaceId: string, callback: Function): void {
        this.subscribe(MasterChannels.INSTALLER_OUTPUT, workspaceId, callback);
    }

    /**
     * Un-subscribes from workspace agent output.
     *
     * @param workspaceId workspace's id
     * @param callback callback to process event
     */
    unSubscribeInstallerOutput(workspaceId: string, callback: Function): void {
        this.unsubscribe(MasterChannels.INSTALLER_OUTPUT, workspaceId, callback);
    }

    /**
     * Subscribes to workspace's status.
     *
     * @param workspaceId workspace's id
     * @param callback callback to process event
     */
    subscribeWorkspaceStatus(workspaceId: string, callback: Function): void {
        const statusHandler = (message: che.workspace.event.WorkspaceStatusEvent) => {
            if (workspaceId === message.workspaceId) {
                callback(message);
            }
        };
        this.subscribe(MasterChannels.WORKSPACE_STATUS, workspaceId, statusHandler);
    }

    /**
     * Un-subscribes pointed callback from workspace's status.
     *
     * @param workspaceId
     * @param callback
     */
    unSubscribeWorkspaceStatus(workspaceId: string, callback: Function): void {
        this.unsubscribe(MasterChannels.WORKSPACE_STATUS, workspaceId, callback);
    }

    /**
     * Fetch client's id and stores it.
     *
     * @returns {Promise<void>}
     */
    fetchClientId(): Promise<void> {
        return this.cheJsonRpcApi.request('websocketIdService/getId').then((data: string[]) => {
            this.clientId = data[0];
        });
    }

    /**
     * Returns client's id.
     *
     * @returns {string} client connection identifier
     */
    getClientId(): string {
        return this.clientId;
    }

    /**
     * Performs subscribe to the pointed channel for pointed workspace's ID and callback.
     *
     * @param channel channel to un-subscribe
     * @param workspaceId workspace's id
     * @param callback callback
     */
    private subscribe(channel: MasterChannels, workspaceId: string, callback: Function): void {
        const method: string = channel.toString();
        const params = { method: method, scope: { workspaceId: workspaceId } };
        this.cheJsonRpcApi.subscribe(SUBSCRIBE, method, callback, params);
    }

    /**
     * Performs un-subscribe of the pointed channel by pointed workspace's ID and callback.
     *
     * @param channel channel to un-subscribe
     * @param workspaceId workspace's id
     * @param callback callback
     */
    private unsubscribe(channel: MasterChannels, workspaceId: string, callback: Function): void {
        const method: string = channel.toString();
        const params = { method: method, scope: { workspaceId: workspaceId } };
        this.cheJsonRpcApi.unsubscribe(UNSUBSCRIBE, method, callback, params);
    }
}
