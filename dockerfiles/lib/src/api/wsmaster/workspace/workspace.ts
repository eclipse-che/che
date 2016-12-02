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
import {AuthData} from "../auth/auth-data";
import {Websocket} from "../../../spi/websocket/websocket";
import {HttpJsonRequest} from "../../../spi/http/default-http-json-request";
import {DefaultHttpJsonRequest} from "../../../spi/http/default-http-json-request";
import {HttpJsonResponse} from "../../../spi/http/default-http-json-request";
import {WorkspaceStartEventPromiseMessageBusSubscriber} from "./workspace-start-event-promise-subscriber";
import {MessageBus} from "../../../spi/websocket/messagebus";
import {MessageBusSubscriber} from "../../../spi/websocket/messagebus-subscriber";
import {WorkspaceDisplayOutputMessageBusSubscriber} from "./workspace-log-output-subscriber";
import {Log} from "../../../spi/log/log";
import {WorkspaceStopEventPromiseMessageBusSubscriber} from "./workspace-stop-event-promise-subscriber";
import {RecipeBuilder} from "../../../spi/docker/recipebuilder";
import {CheFileStructWorkspaceCommand} from "../../../internal/dir/chefile-struct/che-file-struct";

/**
 * Workspace class allowing to manage a workspace, like create/start/stop, etc operations
 * @author Florent Benoit
 */
export class Workspace {

    /**
     * Authentication data
     */
    authData:AuthData;

    /**
     * websocket.
     */
    websocket:Websocket;

    constructor(authData:AuthData) {
        this.authData = authData;
        this.websocket = new Websocket();
    }



    /**
     * Get all workspaces
     */
    getWorkspaces():Promise<Array<org.eclipse.che.api.workspace.shared.dto.WorkspaceDto>> {
        var jsonRequest:HttpJsonRequest = new DefaultHttpJsonRequest(this.authData, '/api/workspace/', 200);
        return jsonRequest.request().then((jsonResponse:HttpJsonResponse) => {
            return jsonResponse.asArrayDto(org.eclipse.che.api.workspace.shared.dto.WorkspaceDtoImpl);
        });
    }

    /**
     * Gets a workspace config DTO object from a createWorkspaceConfiguration object
     * @param createWorkspaceConfig
     */
    getWorkspaceConfigDto(createWorkspaceConfig:CreateWorkspaceConfig) : org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto {
        let devMachine : org.eclipse.che.api.workspace.shared.dto.ExtendedMachineDto = new org.eclipse.che.api.workspace.shared.dto.ExtendedMachineDtoImpl();
        devMachine.getAgents().push("org.eclipse.che.terminal");
        devMachine.getAgents().push("org.eclipse.che.ws-agent");
        devMachine.getAgents().push("org.eclipse.che.ssh");
        devMachine.getAttributes().set("memoryLimitBytes", "2147483648");

        let defaultEnvironment : org.eclipse.che.api.workspace.shared.dto.EnvironmentDto = new org.eclipse.che.api.workspace.shared.dto.EnvironmentDtoImpl();
        defaultEnvironment.getMachines().set("dev-machine", devMachine);
        defaultEnvironment.setRecipe(new org.eclipse.che.api.workspace.shared.dto.EnvironmentRecipeDtoImpl(createWorkspaceConfig.machineConfigSource));


        let commandsToCreate : Array<org.eclipse.che.api.machine.shared.dto.CommandDto> = new Array;
        createWorkspaceConfig.commands.forEach(commandConfig => {
            let command : org.eclipse.che.api.machine.shared.dto.CommandDto = new org.eclipse.che.api.machine.shared.dto.CommandDtoImpl();
            command.withCommandLine(commandConfig.commandLine).withName(commandConfig.name).withType(commandConfig.type);
            if (commandConfig.attributes.previewUrl) {
                command.getAttributes().set("previewUrl", commandConfig.attributes.previewUrl);
            }
            commandsToCreate.push(command);
        });

        let workspaceConfigDto : org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto = new org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDtoImpl();
        workspaceConfigDto.withDefaultEnv("default").withName(createWorkspaceConfig.name).withCommands(commandsToCreate);
        workspaceConfigDto.getEnvironments().set("default", defaultEnvironment);

        return workspaceConfigDto;
    }


    /**
     * Create a workspace and return a promise with content of WorkspaceDto in case of success
     */
    createWorkspace(createWorkspaceConfig:CreateWorkspaceConfig):Promise<org.eclipse.che.api.workspace.shared.dto.WorkspaceDto> {

        let workspaceConfigDto : org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto = this.getWorkspaceConfigDto(createWorkspaceConfig);

        // TODO use ram ?
        //createWorkspaceConfig.ram

        var jsonRequest:HttpJsonRequest = new DefaultHttpJsonRequest(this.authData, '/api/workspace?account=', 201).setMethod('POST').setBody(workspaceConfigDto);
        return jsonRequest.request().then((jsonResponse:HttpJsonResponse) => {
            return jsonResponse.asDto(org.eclipse.che.api.workspace.shared.dto.WorkspaceDtoImpl);
        });

    }


    /**
     * Start a workspace and provide a Promise with WorkspaceDto.
     */
    startWorkspace(workspaceId:string, displayLog?:boolean):Promise<org.eclipse.che.api.workspace.shared.dto.WorkspaceDto> {

        var callbackSubscriber:WorkspaceStartEventPromiseMessageBusSubscriber;
        let userWorkspaceDto : org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
        // get workspace DTO
        return this.getWorkspace(workspaceId).then((workspaceDto) => {
            userWorkspaceDto = workspaceDto;
            return this.getMessageBus(workspaceDto);
        }).then((messageBus: MessageBus) => {
            var displayOutputWorkspaceSubscriber:MessageBusSubscriber = new WorkspaceDisplayOutputMessageBusSubscriber();
            callbackSubscriber = new WorkspaceStartEventPromiseMessageBusSubscriber(messageBus, userWorkspaceDto);
            messageBus.subscribe('workspace:' + workspaceId, callbackSubscriber);
            let channel:string = 'machine:status:' + workspaceId + ':default';
            messageBus.subscribe(channel, callbackSubscriber);
            if (displayLog) {
                messageBus.subscribe('workspace:' + workspaceId + ':ext-server:output', displayOutputWorkspaceSubscriber);
                messageBus.subscribe('workspace:' + workspaceId + ':environment_output', displayOutputWorkspaceSubscriber);
                messageBus.subscribe(workspaceId + ':default:default', displayOutputWorkspaceSubscriber);
            }
            return userWorkspaceDto;
        }).then((workspaceDto) => {
            var jsonRequest:HttpJsonRequest = new DefaultHttpJsonRequest(this.authData, '/api/workspace/' + workspaceId + '/runtime?environment=default', 200).setMethod('POST');
            return jsonRequest.request().then((jsonResponse:HttpJsonResponse) => {
                return jsonResponse.asDto(org.eclipse.che.api.workspace.shared.dto.WorkspaceDtoImpl);
            }).then((workspaceDto) => {
                return callbackSubscriber.promise;
            }).then(() => {
                return this.getWorkspace(workspaceId);
            });
        });


    }


    /**
     * Search a workspace data by returning a Promise with WorkspaceDto.
     */
    searchWorkspace(key:string):Promise<org.eclipse.che.api.workspace.shared.dto.WorkspaceDto> {
        Log.getLogger().debug('search workspace with key', key);

        // if workspace key is too short it's a workspace name
        if (key && key.length < 21) {
            if (key.indexOf(":") < 0) {
                key = ":" + key;
            }
        }

        var jsonRequest:HttpJsonRequest = new DefaultHttpJsonRequest(this.authData, '/api/workspace/' + key, 200);
        return jsonRequest.request().then((jsonResponse:HttpJsonResponse) => {
            Log.getLogger().debug('got workspace with key', key, 'result: ', jsonResponse.getData());
            return jsonResponse.asDto(org.eclipse.che.api.workspace.shared.dto.WorkspaceDtoImpl);
        });
    }

    /**
     * Search a workspace data by returning a Promise with WorkspaceDto.
     */
    existsWorkspace(key:string):Promise<org.eclipse.che.api.workspace.shared.dto.WorkspaceDto> {
        Log.getLogger().debug('search workspace with key', key);

        return this.searchWorkspace(key).catch((error) => {
            return undefined;
        })
    }

    /**
     * Get a workspace data by returning a Promise with WorkspaceDto.
     */
    getWorkspace(workspaceId:string):Promise<org.eclipse.che.api.workspace.shared.dto.WorkspaceDto> {
        var jsonRequest:HttpJsonRequest = new DefaultHttpJsonRequest(this.authData, '/api/workspace/' + workspaceId, 200);
        return jsonRequest.request().then((jsonResponse:HttpJsonResponse) => {
            return jsonResponse.asDto(org.eclipse.che.api.workspace.shared.dto.WorkspaceDtoImpl);
        });
    }


    getMessageBus(workspaceDto:org.eclipse.che.api.workspace.shared.dto.WorkspaceDto): Promise<MessageBus> {
        // get id
        let workspaceId:string = workspaceDto.getId();

        var protocol:string;
        if (this.authData.isSecured()) {
            protocol = 'wss';
        } else {
            protocol = 'ws';
        }

        // get links for WS
        var link:string;
        workspaceDto.getLinks().forEach(workspaceLink => {
            if ('get workspace events channel' === workspaceLink.getRel()) {
                link = workspaceLink.getHref();
            }
        });

        return this.websocket.getMessageBus(link + '?token=' + this.authData.getToken());
    }


    /**
     * Delete a workspace and returns a Promise with WorkspaceDto.
     */
    deleteWorkspace(workspaceId:string):Promise<org.eclipse.che.api.workspace.shared.dto.WorkspaceDto> {
        var jsonRequest:HttpJsonRequest = new DefaultHttpJsonRequest(this.authData, '/api/workspace/' + workspaceId, 204).setMethod('DELETE');
        return this.getWorkspace(workspaceId).then((workspaceDto:org.eclipse.che.api.workspace.shared.dto.WorkspaceDto) => {
            return jsonRequest.request().then((jsonResponse:HttpJsonResponse) => {
                return workspaceDto;
            });
        });
    }


    /**
     * Stop a workspace and returns a Promise with WorkspaceDto.
     */
    stopWorkspace(workspaceId:string):Promise<org.eclipse.che.api.workspace.shared.dto.WorkspaceDto> {

        var jsonRequest:HttpJsonRequest = new DefaultHttpJsonRequest(this.authData, '/api/workspace/' + workspaceId + '/runtime', 204).setMethod('DELETE');
        var callbackSubscriber:WorkspaceStopEventPromiseMessageBusSubscriber;

        var userWorkspaceDto : org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
        // get workspace DTO
        return this.getWorkspace(workspaceId).then((workspaceDto) => {
            userWorkspaceDto = workspaceDto;
            return this.getMessageBus(workspaceDto);
        }).then((messageBus : MessageBus) => {
            callbackSubscriber = new WorkspaceStopEventPromiseMessageBusSubscriber(messageBus, userWorkspaceDto);
            messageBus.subscribe('workspace:' + workspaceId, callbackSubscriber);
            return userWorkspaceDto;
        }).then((workspaceDto) => {
            return jsonRequest.request().then((jsonResponse:HttpJsonResponse) => {
                return workspaceDto;
            });
        }).then((workspaceDto) => {
            return callbackSubscriber.promise;
        });
    }

    getWorkspaceAgent(workspaceDTO:org.eclipse.che.api.workspace.shared.dto.WorkspaceDto):any {
        // search the workspace agent link
        let links:Array<any> = workspaceDTO.getRuntime().getLinks();
        var hrefWsAgent;
        links.forEach((link) => {
            if ('wsagent' === link.rel) {
                hrefWsAgent = link.href;
            }
        });
        return require('url').parse(hrefWsAgent);
    }

    /**
     * Provides machine token for given workspace
     * @param workspaceId the ID of the workspace
     * @returns {*}
     */
    getMachineToken(workspaceDto:org.eclipse.che.api.workspace.shared.dto.WorkspaceDto) {


        var jsonRequest:HttpJsonRequest = new DefaultHttpJsonRequest(this.authData, '/api/machine/token/' + workspaceDto.getId(), 200);
        return jsonRequest.request().then((jsonResponse:HttpJsonResponse) => {
            return JSON.parse(jsonResponse.getData()).machineToken;
        });

    }

}

export class CreateWorkspaceConfig {
    ram : number = 2048;
    machineConfigSource : any = {"contentType": "text/x-dockerfile", "type": "dockerfile", "content": RecipeBuilder.DEFAULT_DOCKERFILE_CONTENT};
    name: string = "default";
    commands: Array<CheFileStructWorkspaceCommand> = [];

}
