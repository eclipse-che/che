/*
 * Copyright (c) 2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */

import {ProcessTerminatedEventPromiseMessageBusSubscriber} from "./process-terminated-event-promise-subscriber";
import {ProcesLogOutputMessageBusSubscriber} from "./process-log-output-subscriber";
import {AuthData} from "../auth/auth-data";
import {Websocket} from "../../../spi/websocket/websocket";
import {Workspace} from "../workspace/workspace";
import {WorkspaceDto} from "../workspace/dto/workspacedto";
import {MachineProcessDto} from "./dto/machine-process-dto";
import {MessageBusSubscriber} from "../../../spi/websocket/messagebus-subscriber";
import {MessageBus} from "../../../spi/websocket/messagebus";
import {HttpJsonRequest} from "../../../spi/http/default-http-json-request";
import {DefaultHttpJsonRequest} from "../../../spi/http/default-http-json-request";
import {HttpJsonResponse} from "../../../spi/http/default-http-json-request";

/**
 * Workspace class allowing to manage a workspace, like create/start/stop, etc operations
 * @author Florent Benoit
 */
export class MachineServiceClientImpl {

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


    /**
     * Create a workspace and return a promise with content of WorkspaceDto in case of success
     */
    executeCommand(workspaceDto : WorkspaceDto,
                   machineId:string,
                   commandLine:any,
                   outputChannel:string):Promise<MachineProcessDto> {

        let command:any = {
            "name" : "custom",
            "type" : "custom",
            "commandLine": commandLine
        };

        let path : string = '/api/machine/' + machineId + '/command/?outputChannel=' + outputChannel;
        // get MessageBus
        var displayOutputWorkspaceSubscriber:MessageBusSubscriber = new ProcesLogOutputMessageBusSubscriber();
        let processTerminatedEventPromiseMessageBusSubscriber : ProcessTerminatedEventPromiseMessageBusSubscriber;
        let userMachineProcessDto : MachineProcessDto;
        return this.workspace.getMessageBus(workspaceDto).then((messageBus:MessageBus) => {

            processTerminatedEventPromiseMessageBusSubscriber = new ProcessTerminatedEventPromiseMessageBusSubscriber(messageBus);

            // subscribe to websocket
            messageBus.subscribe(outputChannel, displayOutputWorkspaceSubscriber);
            messageBus.subscribe('machine:process:' + machineId, processTerminatedEventPromiseMessageBusSubscriber);


            var jsonRequest:HttpJsonRequest = new DefaultHttpJsonRequest(this.authData, path, 200).setMethod('POST').setBody(command);
            return jsonRequest.request();
        }).then((jsonResponse : HttpJsonResponse) => {
            // return response
            return new MachineProcessDto(JSON.parse(jsonResponse.getData()));
        }).then((machineProcessDto : MachineProcessDto) => {
            userMachineProcessDto = machineProcessDto;
            // get pid
            let pid: number = machineProcessDto.getContent().pid;

            // subscribe to pid event end
            return processTerminatedEventPromiseMessageBusSubscriber.promise;
        }).then(() => {
            return userMachineProcessDto;
        });

    }

}
