/*
 * Copyright (c) 2016-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
import {org} from "../../api/dto/che-dto";
import {ProcessTerminatedEventPromiseMessageBusSubscriber} from "./process-terminated-event-promise-subscriber";
import {AuthData} from "../wsmaster/auth/auth-data";
import {Websocket} from "../../spi/websocket/websocket";
import {Workspace} from "../wsmaster/workspace/workspace";
import {CheFileStructWorkspaceCommand} from "../../internal/dir/chefile-struct/che-file-struct";
import {Log} from "../../spi/log/log";
import {JsonRpcBus} from "../../spi/websocket/json-rpc-bus";
import {ProcessAckPromiseMessageBusSubscriber} from "./process-ack-event-promise-subscriber";

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

    constructor(workspace : Workspace, authData : AuthData) {
        this.workspace = workspace;
        this.authData = authData;
        this.websocket = new Websocket();
    }

    getJsonRpcBus(workspaceDto : org.eclipse.che.api.workspace.shared.dto.WorkspaceDto): Promise<JsonRpcBus> {
        var protocol:string;
        if (this.authData.isSecured()) {
            protocol = 'wss';
        } else {
            protocol = 'ws';
        }

        // get links for WS
        var link:string = protocol + '://' + this.authData.hostname + ":" + this.authData.port + '/connect';

        return this.websocket.getJsonRpcBus(link + '?token=' + this.authData.getToken());
    }

    /**
     * Create a workspace and return a promise with content of WorkspaceDto in case of success
     */
    executeCommand(workspaceDto : org.eclipse.che.api.workspace.shared.dto.WorkspaceDto,
                   machineId:string,
                   cheFileStructWorkspaceCommand:CheFileStructWorkspaceCommand,
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
        return this.getJsonRpcBus(workspaceDto).then((jsonRpcBus:JsonRpcBus) => {
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
