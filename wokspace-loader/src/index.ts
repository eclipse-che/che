/*
 * Copyright (c) 2015-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
import "./style.less";
import { WebsocketClient } from './json-rpc/websocket-client';
import { CheJsonRpcMasterApi } from './json-rpc/che-json-rpc-master-api';
import { getWorkspace, startWorkspace } from './workspace';

const DEFAULT_WEBSOCKET_CONTEXT = '/api/websocket';

let wsClient = new WebsocketClient();
let wsUrl = formJsonRpcApiLocation(document.location) + DEFAULT_WEBSOCKET_CONTEXT;
let masterApi = new CheJsonRpcMasterApi(wsClient);
let workspaceKey = getWorkspaceKey();
masterApi.connect(wsUrl).then(() => {
    getWorkspace(getWorkspaceKey()).then(handleWorkspace).catch(err => {
        console.error(err);
    });
});

function handleWorkspace(ws: che.IWorkspace) {
    let machineStatusHandler = (message: any) => {
        console.log('machine status', message);
    };
    let machineOutputHandler = (message: any) => {
        document.getElementById("output").appendChild(document.createTextNode(message.text));
    };
    let workspaceStatusHandler = (message: any) => {
        console.log('workspace status', message);
        let workspaceStatus = message.status;

        if (workspaceStatus === 'RUNNING') {
            openIdeAfterRunning(ws);
        }

    };

    masterApi.subscribeEnvironmentStatus(ws.id, machineStatusHandler);
    masterApi.subscribeEnvironmentOutput(ws.id, machineOutputHandler);
    masterApi.subscribeWorkspaceStatus(ws.id, workspaceStatusHandler);

    if (ws.status === 'RUNNING') {
        openIdeAfterRunning(ws);
    } else if (ws.status === 'STARTING') {
        console.log("Workspace is STARTING, listening Workspace status");
    } else {
        console.log("Workspace is stopped, now try to start it");
        startIde(ws);
    }
}

function startIde(workspace: che.IWorkspace) {
    startWorkspace(workspace).then((ws) => {
        // workspace is starting now
    }, (error: any) => {
        console.error(error);
        // TODO show error on UI
    });
}

async function openIdeAfterRunning(ws: che.IWorkspace) {
    let startedWs = await getWorkspace(ws.id);
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
        ideUrl = ws.links.ide;
    }

    console.log('IDE url is: ', ideUrl);
    document.location.assign(ideUrl);
}

function formJsonRpcApiLocation(location: Location): string {
    let wsUrl;
    let wsProtocol;
    wsProtocol = 'http:' === location.protocol ? 'ws' : 'wss';
    wsUrl = wsProtocol + '://' + location.host;
    console.log(wsUrl);
    return wsUrl;
}

/** Returns workspace key from current address or empty string when it is undefined. */
function getWorkspaceKey(): string {
    let browserUrl = window.location.pathname;
    let result: string;

    result = browserUrl.substr(1);

    return result.substr(result.indexOf('/') + 1, result.length);

}
