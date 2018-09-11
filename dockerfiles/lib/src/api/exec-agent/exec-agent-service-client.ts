/*
 * Copyright (c) 2016-2017 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc.- initial API and implementation
 */
import {ProcessTerminatedEventPromiseMessageBusSubscriber} from "./process-terminated-event-promise-subscriber";
import {AuthData} from "../wsmaster/auth/auth-data";
import {Websocket} from "../../spi/websocket/websocket";
import {Workspace} from "../wsmaster/workspace/workspace";
import {CheFileStructWorkspaceCommand} from "../../internal/dir/chefile-struct/che-file-struct";
import {JsonRpcBus} from "../../spi/websocket/json-rpc-bus";
import {ProcessAckPromiseMessageBusSubscriber} from "./process-ack-event-promise-subscriber";
import {ServerLocation} from "../../utils/server-location";

/**
 * Exec Agent service allowing to start/stop processes
 * @author Florent Benoit
 */
export class ExecAgentServiceClientImpl {

    /**
     * Authentication data
     */
    authData : AuthData;

    /**
     * websocket.
     */
    websocket : Websocket;

    workspace : Workspace;

    /**
     * Location of exec agent server
     */
    serverURL: string;

    constructor(workspace : Workspace, authData : AuthData, serverURL : string) {
        this.workspace = workspace;
        this.authData = authData;
        this.serverURL = serverURL;
        this.websocket = new Websocket();
    }

    getJsonRpcBus(): Promise<JsonRpcBus> {
        return this.websocket.getJsonRpcBus(this.serverURL + '?token=' + this.authData.getToken());
    }

    /**
     * Create a workspace and return a promise with content of WorkspaceDto in case of success
     */
    executeCommand(cheFileStructWorkspaceCommand:CheFileStructWorkspaceCommand,
                   uuid:string,
                   asynchronous : boolean = true):Promise<boolean> {

        let rpcCommand: any =
            {
                "jsonrpc": "2.0",
                "method": "process.start",
                "id": uuid,
                "params": {
                    "commandLine": cheFileStructWorkspaceCommand.commandLine,
                    "name": !cheFileStructWorkspaceCommand.name ? "custom-command" : cheFileStructWorkspaceCommand.name,
                    "type": !cheFileStructWorkspaceCommand.type ? "custom" : cheFileStructWorkspaceCommand.type
                }
            };

        // get JSON RPC Bus
        let processTerminatedEventPromiseMessageBusSubscriber : ProcessTerminatedEventPromiseMessageBusSubscriber;
        let userJsonRpcBus : JsonRpcBus;
        return this.getJsonRpcBus().then((jsonRpcBus:JsonRpcBus) => {
            userJsonRpcBus = jsonRpcBus;
            processTerminatedEventPromiseMessageBusSubscriber = new ProcessTerminatedEventPromiseMessageBusSubscriber(jsonRpcBus);
            let processAckPromiseMessageBusSubscriber : ProcessAckPromiseMessageBusSubscriber = new ProcessAckPromiseMessageBusSubscriber(uuid, jsonRpcBus, asynchronous, processTerminatedEventPromiseMessageBusSubscriber);
            jsonRpcBus.subscribe(processAckPromiseMessageBusSubscriber);
            jsonRpcBus.send(rpcCommand);
            return processAckPromiseMessageBusSubscriber.promise;
        }).then(() => {
            // subscribe to pid event end
            if (asynchronous) {
                return processTerminatedEventPromiseMessageBusSubscriber.promise;
            } else {
                // do not wait the end
                userJsonRpcBus.close();
                return Promise.resolve(true);
            }
        }).then(() => {
            return true;
        });

    }

}
