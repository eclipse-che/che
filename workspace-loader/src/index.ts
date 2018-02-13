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

const css = require('./style.css');
import { WebsocketClient } from './json-rpc/websocket-client';
import { CheJsonRpcMasterApi } from './json-rpc/che-json-rpc-master-api';
import { getWorkspace, startWorkspace } from './workspace';
import { Loader } from './loader/loader'

const WEBSOCKET_CONTEXT = '/api/websocket';

let websocketURL = websocketBaseURL() + WEBSOCKET_CONTEXT;
let master = new CheJsonRpcMasterApi(new WebsocketClient());
let loader = new Loader();

/** Ask dashboard to show the IDE. */
window.parent.postMessage("show-ide", "*");

master.connect(websocketURL).then(() => {
    getWorkspace(getWorkspaceKey())
        .then(handleWorkspace)
        .catch(err => {console.error(err);});
});

function handleWorkspace(workspace: che.IWorkspace) {
    let startAfterStopping = false;

    if (workspace.status === 'RUNNING') {
        openIDE(workspace);
        return;
    }

    /** Handle machine status */
    master.subscribeEnvironmentStatus(workspace.id, (message: any) => {
        console.log('machine status', message);
    });

    /** Handle environment output */
    master.subscribeEnvironmentOutput(workspace.id, (message: any) => {
        loader.log(message.text);
    });

    /** Handle changing workspace status */
    master.subscribeWorkspaceStatus(workspace.id, (message: any) => {
        let status = message.status;

        if (status === 'RUNNING') {
            openIDE(workspace);
        } else if (status === 'STOPPED' && startAfterStopping) {
            startWorkspace(workspace);
        }
    });

    if (workspace.status === 'STOPPED') {
        startWorkspace(workspace);
    } else if (workspace.status === 'STOPPING') {
        startAfterStopping = true;
    }
}

/**
 * Opens IDE for the workspace.
 * 
 * @param workspace workspace
 */
async function openIDE(workspace: che.IWorkspace) {
    let startedWs = await getWorkspace(workspace.id);
    let machines = startedWs.runtime.machines;

    let ideUrl: string;
    Object.keys(machines).forEach((key, index) => {
        let servers = machines[key].servers;
        Object.keys(servers).forEach((key, index) => {
            let att = servers[key].attributes;
            if (att['type'] === 'ide') {
                ideUrl = servers[key].url;
            }
        });
    });

    if (!ideUrl) {
        ideUrl = workspace.links.ide;
    }

    document.location.assign(ideUrl);
}

/** Returns base websocket URL. */
function websocketBaseURL(): string {
    let wsProtocol = 'http:' === document.location.protocol ? 'ws' : 'wss';
    return wsProtocol + '://' + document.location.host;
}

/** Returns workspace key from current address or empty string when it is undefined. */
function getWorkspaceKey(): string {
    let result: string = window.location.pathname.substr(1);
    return result.substr(result.indexOf('/') + 1, result.length);
}
