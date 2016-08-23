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



import {AuthData} from "../auth/auth-data";
import {Websocket} from "../../../spi/websocket/websocket";
import {WorkspaceDto} from "./dto/workspacedto";
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
     * Create a workspace and return a promise with content of WorkspaceDto in case of success
     */
    createWorkspace(createWorkspaceConfig:CreateWorkspaceConfig):Promise<WorkspaceDto> {

        var workspace = {
            "defaultEnv": "default",
            "commands": createWorkspaceConfig.commands,
            "projects": [],
            "environments": [{
                "machineConfigs": [{
                    "dev": true,
                    "servers": [],
                    "envVariables": {},
                    "limits": {"ram": createWorkspaceConfig.ram},
                    "source": createWorkspaceConfig.machineConfigSource,
                    "name": "default",
                    "type": "docker",
                    "links": []
                }], "name": "default"
            }],
            "name": createWorkspaceConfig.name,
            "links": [],
            "description": null
        };

        var jsonRequest:HttpJsonRequest = new DefaultHttpJsonRequest(this.authData, '/api/workspace?account=', 201).setMethod('POST').setBody(workspace);
        return jsonRequest.request().then((jsonResponse:HttpJsonResponse) => {
            return new WorkspaceDto(JSON.parse(jsonResponse.getData()));
        });

    }


    /**
     * Start a workspace and provide a Promise with WorkspaceDto.
     */
    startWorkspace(workspaceId:string, displayLog?:boolean):Promise<WorkspaceDto> {

        var callbackSubscriber:WorkspaceStartEventPromiseMessageBusSubscriber;
        let userWorkspaceDto : WorkspaceDto;
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
                messageBus.subscribe(workspaceId + ':default:default', displayOutputWorkspaceSubscriber);
            }
            return userWorkspaceDto;
        }).then((workspaceDto) => {
            var jsonRequest:HttpJsonRequest = new DefaultHttpJsonRequest(this.authData, '/api/workspace/' + workspaceId + '/runtime?environment=default', 200).setMethod('POST');
            return jsonRequest.request().then((jsonResponse:HttpJsonResponse) => {
                return new WorkspaceDto(JSON.parse(jsonResponse.getData()));
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
    searchWorkspace(key:string):Promise<WorkspaceDto> {
        Log.getLogger().debug('search workspace with key', key);
        var jsonRequest:HttpJsonRequest = new DefaultHttpJsonRequest(this.authData, '/api/workspace/' + key, 200);
        return jsonRequest.request().then((jsonResponse:HttpJsonResponse) => {
            Log.getLogger().debug('got workspace with key', key, 'result: ', jsonResponse.getData());
            return new WorkspaceDto(JSON.parse(jsonResponse.getData()));
        });
    }

    /**
     * Search a workspace data by returning a Promise with WorkspaceDto.
     */
    existsWorkspace(key:string):Promise<WorkspaceDto> {
        Log.getLogger().debug('search workspace with key', key);

        return this.searchWorkspace(key).catch((error) => {
            return undefined;
        })
    }



    /**
     * Get a workspace data by returning a Promise with WorkspaceDto.
     */
    getWorkspace(workspaceId:string):Promise<WorkspaceDto> {
        var jsonRequest:HttpJsonRequest = new DefaultHttpJsonRequest(this.authData, '/api/workspace/' + workspaceId, 200);
        return jsonRequest.request().then((jsonResponse:HttpJsonResponse) => {
            return new WorkspaceDto(JSON.parse(jsonResponse.getData()));
        });
    }


    getMessageBus(workspaceDto:WorkspaceDto): Promise<MessageBus> {
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
        workspaceDto.getContent().links.forEach(workspaceLink => {
            if ('get workspace events channel' === workspaceLink.rel) {
                link = workspaceLink.href;
            }
        });

        return this.websocket.getMessageBus(link + '?token=' + this.authData.getToken(), workspaceId);
    }


    /**
     * Delete a workspace and returns a Promise with WorkspaceDto.
     */
    deleteWorkspace(workspaceId:string):Promise<WorkspaceDto> {
        var jsonRequest:HttpJsonRequest = new DefaultHttpJsonRequest(this.authData, '/api/workspace/' + workspaceId, 204).setMethod('DELETE');
        return this.getWorkspace(workspaceId).then((workspaceDto:WorkspaceDto) => {
            return jsonRequest.request().then((jsonResponse:HttpJsonResponse) => {
                return workspaceDto;
            });
        });
    }


    /**
     * Stop a workspace and returns a Promise with WorkspaceDto.
     */
    stopWorkspace(workspaceId:string):Promise<WorkspaceDto> {

        var jsonRequest:HttpJsonRequest = new DefaultHttpJsonRequest(this.authData, '/api/workspace/' + workspaceId + '/runtime', 204).setMethod('DELETE');
        var callbackSubscriber:WorkspaceStopEventPromiseMessageBusSubscriber;

        var userWorkspaceDto : WorkspaceDto;
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

    getWorkspaceAgent(workspaceDTO:WorkspaceDto):any {
        // search the workspace agent link
        let links:Array<any> = workspaceDTO.getContent().runtime.links;
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
    getMachineToken(workspaceDto:WorkspaceDto) {


        var jsonRequest:HttpJsonRequest = new DefaultHttpJsonRequest(this.authData, '/api/machine/token/' + workspaceDto.getId(), 200);
        return jsonRequest.request().then((jsonResponse:HttpJsonResponse) => {
            return JSON.parse(jsonResponse.getData()).machineToken;
        });

    }

}

export class CreateWorkspaceConfig {

    ram : number = 2048;
    machineConfigSource : any = {"type": "dockerfile", "content": RecipeBuilder.DEFAULT_DOCKERFILE_CONTENT};
    name: string = "default";
    commands: Array<CheFileStructWorkspaceCommand> = [];

}
