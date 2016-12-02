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
import {org} from "../../../api/dto/che-dto"
import {ProcessTerminatedEventPromiseMessageBusSubscriber} from "./process-terminated-event-promise-subscriber";
import {ProcesLogOutputMessageBusSubscriber} from "./process-log-output-subscriber";
import {AuthData} from "../auth/auth-data";
import {Websocket} from "../../../spi/websocket/websocket";
import {Workspace} from "../workspace/workspace";
import {MessageBusSubscriber} from "../../../spi/websocket/messagebus-subscriber";
import {MessageBus} from "../../../spi/websocket/messagebus";
import {HttpJsonRequest} from "../../../spi/http/default-http-json-request";
import {DefaultHttpJsonRequest} from "../../../spi/http/default-http-json-request";
import {HttpJsonResponse} from "../../../spi/http/default-http-json-request";
import {CheFileStructWorkspaceCommand} from "../../../internal/dir/chefile-struct/che-file-struct";

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
    executeCommand(workspaceDto : org.eclipse.che.api.workspace.shared.dto.WorkspaceDto,
                   machineId:string,
                   cheFileStructWorkspaceCommand:CheFileStructWorkspaceCommand,
                   outputChannel:string,
                   asynchronous : boolean = true):Promise<org.eclipse.che.api.machine.shared.dto.MachineProcessDto> {

        let command:any = {
            "name" : !cheFileStructWorkspaceCommand.name ? "custom-command" : cheFileStructWorkspaceCommand.name,
            "type" : !cheFileStructWorkspaceCommand.type ? "custom" : cheFileStructWorkspaceCommand.type,
            "commandLine": cheFileStructWorkspaceCommand.commandLine
        };

        let path : string = '/api/workspace/' + workspaceDto.getId() + '/machine/' + machineId + '/command/?outputChannel=' + outputChannel;
        // get MessageBus
        var displayOutputWorkspaceSubscriber:MessageBusSubscriber = new ProcesLogOutputMessageBusSubscriber();
        let processTerminatedEventPromiseMessageBusSubscriber : ProcessTerminatedEventPromiseMessageBusSubscriber;
        let userMachineProcessDto : org.eclipse.che.api.machine.shared.dto.MachineProcessDto;
        let userMessageBus : MessageBus;
        return this.workspace.getMessageBus(workspaceDto).then((messageBus:MessageBus) => {
            userMessageBus = messageBus;
            processTerminatedEventPromiseMessageBusSubscriber = new ProcessTerminatedEventPromiseMessageBusSubscriber(messageBus);

            // subscribe to websocket
            if (asynchronous) {
                messageBus.subscribe(outputChannel, displayOutputWorkspaceSubscriber);
                messageBus.subscribe('machine:process:' + machineId, processTerminatedEventPromiseMessageBusSubscriber);
            }

            var jsonRequest:HttpJsonRequest = new DefaultHttpJsonRequest(this.authData, path, 200).setMethod('POST').setBody(command);
            return jsonRequest.request();
        }).then((jsonResponse : HttpJsonResponse) => {
            // return response
            return jsonResponse.asDto(org.eclipse.che.api.machine.shared.dto.MachineProcessDtoImpl);
        }).then((machineProcessDto : org.eclipse.che.api.machine.shared.dto.MachineProcessDto) => {
            userMachineProcessDto = machineProcessDto;
            // get pid
            let pid: number = machineProcessDto.getPid();

            // subscribe to pid event end
            if (asynchronous) {
                return processTerminatedEventPromiseMessageBusSubscriber.promise;
            } else {
                // do not wait the end
                userMessageBus.close();
                return true;
            }
        }).then(() => {
            return userMachineProcessDto;
        });

    }

}
