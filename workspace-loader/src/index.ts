/*
 * Copyright (c) 2018-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
'use strict';

require('./style.css');

import { WebsocketClient } from './json-rpc/websocket-client';
import { CheJsonRpcMasterApi } from './json-rpc/che-json-rpc-master-api';
import { Loader } from './loader/loader'

const WEBSOCKET_CONTEXT = '/api/websocket';

export class WorkspaceLoader {

    loader: Loader;
    workspace: che.IWorkspace;
    startAfterStopping = false;

    constructor(loader: Loader) {
        this.loader = loader;

        /** Ask dashboard to show the IDE. */
        window.parent.postMessage("show-ide", "*");
    }

    /**
     * Loads the workspace.
     */
    load(): Promise<void> {
        let workspaceKey = this.getWorkspaceKey();

        if (!workspaceKey || workspaceKey === "") {
            console.error("Workspace is not defined");
            return;
        }

        return this.getWorkspace(workspaceKey)
            .then((workspace) => {
                this.workspace = workspace;
                return this.handleWorkspace();
            })
            .catch(err => {
                console.error(err);
            });
    }

    /**
     * Returns workspace key from current address or empty string when it is undefined.
     */
    getWorkspaceKey(): string {
        let result: string = window.location.pathname.substr(1);
        return result.substr(result.indexOf('/') + 1, result.length);
    }

    /**
     * Returns base websocket URL.
     */
    websocketBaseURL(): string {
        let wsProtocol = 'http:' === document.location.protocol ? 'ws' : 'wss';
        return wsProtocol + '://' + document.location.host;
    }

    /**
     * Get workspace by ID.
     * 
     * @param workspaceId workspace id
     */
    getWorkspace(workspaceId: string): Promise<che.IWorkspace> {
        return new Promise((resolve, reject) => {
            let request = new XMLHttpRequest();
            request.open("GET", '/api/workspace/' + workspaceId);
            request.send();
            request.onreadystatechange = function () {
                if (this.readyState !== 4) { return; }
                if (this.status !== 200) {
                    reject(this.status ? this.statusText : "Unknown error");
                    return;
                }
                resolve(JSON.parse(this.responseText));
            };
        });        
    }

    /**
     * Start current workspace.
     */
    startWorkspace(): Promise<che.IWorkspace> {
        return new Promise((resolve, reject) => {
            let request = new XMLHttpRequest();
            request.open("POST", `/api/workspace/${this.workspace.id}/runtime`);
            request.send();
            request.onreadystatechange = function () {
                if (this.readyState !== 4) { return; }
                if (this.status !== 200) {
                    reject(this.status ? this.statusText : "Unknown error");
                    return;
                }
                resolve(JSON.parse(this.responseText));
            };
        });        
    }

    /**
     * Handles workspace status.
     */
    handleWorkspace(): Promise<void> {
        if (this.workspace.status === 'RUNNING') {
            this.openIDE();
            return;
        }

        return this.subscribeWorkspaceEvents().then(() => {
            if (this.workspace.status === 'STOPPED') {
                this.startWorkspace();
            } else if (this.workspace.status === 'STOPPING') {
                this.startAfterStopping = true;
            }
        });
    }

    /**
     * Shows environment outputs.
     * 
     * @param message output message
     */
    onEnvironmentOutput(message) : void {
        this.loader.log(message);
    }

    /**
     * Handles changing of workspace status.
     * 
     * @param status workspace status
     */
    onWorkspaceStatusChanged(status) : void {
        if (status === 'RUNNING') {
            this.openIDE();
        } else if (status === 'STOPPED' && this.startAfterStopping) {
            this.startWorkspace();
        }
    }

    /**
     * Subscribes to the workspace events.
     */
    subscribeWorkspaceEvents() : Promise<any> {
        let master = new CheJsonRpcMasterApi(new WebsocketClient());
        return new Promise((resolve) => {
            master.connect(this.websocketBaseURL() + WEBSOCKET_CONTEXT).then(() => {
                master.subscribeEnvironmentOutput(this.workspace.id, 
                    (message: any) => this.onEnvironmentOutput(message.text));

                master.subscribeWorkspaceStatus(this.workspace.id, 
                    (message: any) => this.onWorkspaceStatusChanged(message.status));

                resolve();
            });
        });
    }

    /**
     * Opens IDE for the workspace.
     */
    openIDE() : void {
        this.getWorkspace(this.workspace.id).then((workspace) => {
            let machines = workspace.runtime.machines;
            for (let machineName in machines) {
                let servers = machines[machineName].servers;
                for (let serverId in servers) {
                    let attributes = servers[serverId].attributes;
                    if (attributes['type'] === 'ide') {
                        document.location.assign(servers[serverId].url);
                        return;
                    }
                }
            }

            document.location.assign(workspace.links.ide);
        });
    }

};

/** Initialize */
if (document.getElementById('workspace-console')) {
    new WorkspaceLoader(new Loader()).load();
}
