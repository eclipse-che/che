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

import {WorkspaceDto} from './dto/workspacedto';
import {AuthData} from "./auth-data";
import {Log} from "./log";
import {RecipeBuilder} from "./recipebuilder";
import {CheFileStructWorkspaceCommand} from "./chefile-struct/che-file-struct";
import {Websocket} from "./websocket";
import {MessageBus} from "./messagebus";
import {MessageBusSubscriber} from "./messagebus-subscriber";
import {WorkspaceStopEventPromiseMessageBusSubscriber} from "./workspace-stop-event-promise-subscriber";
import {WorkspaceDisplayOutputMessageBusSubscriber} from "./workspace-log-output-subscriber";
import {WorkspaceStartEventPromiseMessageBusSubscriber} from "./workspace-start-event-promise-subscriber";
import {DefaultHttpJsonRequest} from "./default-http-json-request";
import {HttpJsonRequest} from "./default-http-json-request";
import {HttpJsonResponse} from "./default-http-json-request";

/**
 * Workspace class allowing to manage a workspace, like create/start/stop, etc operations
 * @author Florent Benoit
 */
export class Workspace {

    /**
     * Authentication data
     */
    authData : AuthData;

    /**
     * websocket.
     */
    websocket : Websocket;

    http : any;


    constructor(authData : AuthData) {
        this.authData = authData;
        this.websocket = new Websocket();
        if (authData.isSecured()) {
            this.http = require('https');
        } else {
            this.http = require('http');
        }
    }


    /**
     * Create a workspace and return a promise with content of WorkspaceDto in case of success
     */
    createWorkspace(createWorkspaceConfig: CreateWorkspaceConfig) : Promise<WorkspaceDto> {

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

        var jsonRequest : HttpJsonRequest = new DefaultHttpJsonRequest(this.authData, '/api/workspace?account=', 201).setMethod('POST').setBody(workspace);
        return jsonRequest.request().then((jsonResponse : HttpJsonResponse) => {
            return new WorkspaceDto(JSON.parse(jsonResponse.getData()));
        });

    }


    /**
     * Start a workspace and provide a Promise with WorkspaceDto.
     */
    startWorkspace(workspaceId: string, displayLog? : boolean) : Promise<WorkspaceDto> {

        var callbackSubscriber : WorkspaceStartEventPromiseMessageBusSubscriber;

        // get workspace DTO
        return this.getWorkspace(workspaceId).then((workspaceDto) => {
            var messageBus:MessageBus = this.getMessageBus(workspaceDto);
            var displayOutputWorkspaceSubscriber:MessageBusSubscriber = new WorkspaceDisplayOutputMessageBusSubscriber();
            callbackSubscriber = new WorkspaceStartEventPromiseMessageBusSubscriber(messageBus, workspaceDto);
            messageBus.subscribe('workspace:' + workspaceId, callbackSubscriber);
            let channel:string = 'machine:status:' + workspaceId + ':default';
            messageBus.subscribe(channel, callbackSubscriber);
            if (displayLog) {
                messageBus.subscribe('workspace:' + workspaceId + ':ext-server:output', displayOutputWorkspaceSubscriber);
                messageBus.subscribe(workspaceId + ':default:default', displayOutputWorkspaceSubscriber);
            }
            // wait to connect websocket
            var waitTill = new Date(new Date().getTime() + 4 * 1000);
            while(waitTill > new Date()){}
            return workspaceDto;
        }).then((workspaceDto) => {
            var jsonRequest : HttpJsonRequest = new DefaultHttpJsonRequest(this.authData, '/api/workspace/' + workspaceId + '/runtime?environment=default', 200).setMethod('POST');
            return jsonRequest.request().then((jsonResponse : HttpJsonResponse) => {
                return new WorkspaceDto(JSON.parse(jsonResponse.getData()));
            }).then((workspaceDto) => {
                return callbackSubscriber.promise;
            }).then(() => {
                return this.getWorkspace(workspaceId);
            });
        });


    }


    /**
     * Get a workspace data by returning a Promise with WorkspaceDto.
     */
    getWorkspace(workspaceId: string) : Promise<WorkspaceDto> {
        var jsonRequest : HttpJsonRequest = new DefaultHttpJsonRequest(this.authData, '/api/workspace/' + workspaceId, 200);
        return jsonRequest.request().then((jsonResponse : HttpJsonResponse) => {
            return new WorkspaceDto(JSON.parse(jsonResponse.getData()));
        });
    }


    getMessageBus(workspaceDto: WorkspaceDto) : MessageBus {
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
    deleteWorkspace(workspaceId: string) : Promise<WorkspaceDto> {
        var jsonRequest : HttpJsonRequest = new DefaultHttpJsonRequest(this.authData, '/api/workspace/' + workspaceId, 204).setMethod('DELETE');
        return this.getWorkspace(workspaceId).then((workspaceDto : WorkspaceDto) => {
            return jsonRequest.request().then((jsonResponse : HttpJsonResponse) => {
                return workspaceDto;
            });
        });
    }


    /**
     * Stop a workspace and returns a Promise with WorkspaceDto.
     */
    stopWorkspace(workspaceId: string) : Promise<WorkspaceDto> {

        var jsonRequest : HttpJsonRequest = new DefaultHttpJsonRequest(this.authData, '/api/workspace/' + workspaceId + '/runtime', 204).setMethod('DELETE');
        var callbackSubscriber:WorkspaceStopEventPromiseMessageBusSubscriber;

        // get workspace DTO
        return this.getWorkspace(workspaceId).then((workspaceDto) => {
            var messageBus:MessageBus = this.getMessageBus(workspaceDto);
            callbackSubscriber = new WorkspaceStopEventPromiseMessageBusSubscriber(messageBus, workspaceDto);
            messageBus.subscribe('workspace:' + workspaceId, callbackSubscriber);
            // wait to connect websocket
            var waitTill = new Date(new Date().getTime() + 4 * 1000);
            while(waitTill > new Date()){}
            return workspaceDto;
        }).then((workspaceDto) => {
            return jsonRequest.request().then((jsonResponse : HttpJsonResponse) => {
                return workspaceDto;
            });
        }).then((workspaceDto) => {
            return callbackSubscriber.promise;
        });
    }

}



export class CreateWorkspaceConfig {

    ram : number = 2048;
    dockerContent: string;
    machineConfigSource : any = {"type": "dockerfile", "content": RecipeBuilder.DEFAULT_DOCKERFILE_CONTENT};
    name: string = "default";
    commands: Array<CheFileStructWorkspaceCommand> = [];

}
