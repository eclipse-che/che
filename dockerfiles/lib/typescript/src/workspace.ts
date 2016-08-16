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

/**
 * Workspace class allowing to manage a workspace, like create/start/stop, etc operations
 * @author Florent Benoit
 */
export class Workspace {

    /**
     * The HTTP library used to call REST API.
     */
    http: any;

    /**
     * Authentication data
     */
    authData : AuthData;

    /**
     * websocket.
     */
    websocket : Websocket;


    constructor(authData : AuthData) {
        this.authData = authData;
        if (authData.isSecured()) {
            this.http = require('https');
        } else {
            this.http = require('http');
        }

        this.websocket = new Websocket();
    }


    /**
     * Create a workspace and return a promise with content of WorkspaceDto in case of success
     */
    createWorkspace(createWorkspaceConfig: CreateWorkspaceConfig) : Promise<WorkspaceDto> {

        var options = {
            hostname: this.authData.getHostname(),
            port: this.authData.getPort(),
            path: '/api/workspace?account=&token=' + this.authData.getToken(),
            method: 'POST',
            headers: {
                'Accept': 'application/json, text/plain, */*',
                'Content-Type': 'application/json;charset=UTF-8'
            }
        };

        let p = new Promise<WorkspaceDto>( (resolve, reject) => {
            var req = this.http.request(options,  (res) => {

                var data: string = '';

                res.on('error',  (error) => {
                    Log.getLogger().error('rejecting as we got error', error);
                    reject('invalid response' + error)
                });

                res.on('data',  (body) => {
                        data += body;
                });

                res.on('end', () => {
                    if (res.statusCode == 201) {
                        resolve(new WorkspaceDto(JSON.parse(data)));
                    } else {
                        reject('create workspace: Invalid response code' + res.statusCode + ':' + data.toString());
                    }
                });
            });



            req.on('error', (err) => {
                Log.getLogger().error('rejecting as we got error', err);
                reject('HTTP error: ' + err);
            });

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


            req.write(JSON.stringify(workspace));
            req.end();

        });
        return p;
    }


    /**
     * Start a workspace and provide a Promise with WorkspaceDto.
     */
    startWorkspace(workspaceId: string, displayLog? : boolean) : Promise<WorkspaceDto> {
        var userWorkspaceDto : WorkspaceDto;

        var options = {
            hostname: this.authData.getHostname(),
            port: this.authData.getPort(),
            path: '/api/workspace/' + workspaceId + '/runtime?environment=default&token=' + this.authData.getToken(),
            method: 'POST',
            headers: {
                'Accept': 'application/json, text/plain, */*',
                'Content-Type': 'application/json;charset=UTF-8'
            }
        };

        let p = new Promise<WorkspaceDto>( (resolve, reject) => {
            var req = this.http.request(options,  (res) => {

                var data: string = '';

                res.on('error',  (body)=> {
                    Log.getLogger().error('got the following error', body.toString());
                    reject(body);
                });

                res.on('data',  (body) => {
                    data += body;
                });

                res.on('end', () => {
                    if (res.statusCode == 200) {
                        // workspace created, continue
                        resolve(new WorkspaceDto(JSON.parse(data)));
                    } else {
                        reject('startWorkspace: Invalid response code' + res.statusCode + ':' + data.toString());
                    }
                });

            });

            req.on('error', (err) => {
                reject('HTTP error: ' + err);
            });

            req.write('{}');
            req.end();

        });

        return p.then((workspaceDto) => {
            userWorkspaceDto = workspaceDto;
            var messageBus:MessageBus = this.getMessageBus(workspaceDto);
            var displayOutputWorkspaceSubscriber:MessageBusSubscriber = new WorkspaceDisplayOutputMessageBusSubscriber();
            var callbackSubscriber : WorkspaceStartEventPromiseMessageBusSubscriber = new WorkspaceStartEventPromiseMessageBusSubscriber(messageBus, workspaceDto);
            messageBus.subscribe('workspace:' + workspaceId, callbackSubscriber);
            if (displayLog) {
                messageBus.subscribe('workspace:' + workspaceId + ':ext-server:output', displayOutputWorkspaceSubscriber);
                messageBus.subscribe(workspaceId + ':default:default', displayOutputWorkspaceSubscriber);
            }
            return callbackSubscriber.promise;
        }).then(() => {
            return this.getWorkspace(workspaceId);
        });
    }


    /**
     * Get a workspace data by returning a Promise with WorkspaceDto.
     */
    getWorkspace(workspaceId: string) : Promise<WorkspaceDto> {

        var options = {
            hostname: this.authData.getHostname(),
            port: this.authData.getPort(),
            path: '/api/workspace/' + workspaceId + '?token=' + this.authData.getToken(),
            method: 'GET',
            headers: {
                'Accept': 'application/json, text/plain, */*',
                'Content-Type': 'application/json;charset=UTF-8'
            }
        };

        let p = new Promise<WorkspaceDto>( (resolve, reject) => {
            var req = this.http.request(options,  (res) => {

                var data: string = '';

                res.on('error',  (body)=> {
                    Log.getLogger().error('got the following error', body.toString());
                    reject(body);
                });

                res.on('data',  (body) => {
                    data += body;
                });

                res.on('end', () => {
                    if (res.statusCode == 200) {
                        // workspace created, continue
                        resolve(new WorkspaceDto(JSON.parse(data)));
                    } else {
                        reject('get workspace: Invalid response code' + res.statusCode + ':' + data.toString());
                    }
                });

            });

            req.on('error', (err) => {
                reject('HTTP error: ' + err);
            });

            req.write('{}');
            req.end();

        });
        return p;
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

        var options = {
            hostname: this.authData.getHostname(),
            port: this.authData.getPort(),
            path: '/api/workspace/' + workspaceId + '?token=' + this.authData.getToken(),
            method: 'DELETE',
            headers: {
                'Accept': 'application/json, text/plain, */*',
                'Content-Type': 'application/json;charset=UTF-8'
            }
        };

        return this.getWorkspace(workspaceId).then((workspaceDto) => {
            return workspaceDto;
        }).then((workspaceDto) => {
            let p = new Promise<WorkspaceDto>( (resolve, reject) => {
                var req = this.http.request(options,  (res) => {

                    var data: string = '';

                    res.on('error',  (body)=> {
                        Log.getLogger().error('got the following error', body.toString());
                        reject(body);
                    });

                    res.on('data',  (body) => {
                        data += body;
                    });

                    res.on('end', () => {
                        if (res.statusCode == 204) {
                            // workspace deleted, continue
                            resolve(workspaceDto);
                        } else {
                            reject('delete workspace: Invalid response code' + res.statusCode + ':' + data.toString());
                        }
                    });

                });

                req.on('error', (err) => {
                    reject('HTTP error: ' + err);
                });

                req.write('{}');
                req.end();

            });

            return p;
        });
    }


    /**
     * Stop a workspace and returns a Promise with WorkspaceDto.
     */
    stopWorkspace(workspaceId: string) : Promise<WorkspaceDto> {

        var options = {
            hostname: this.authData.getHostname(),
            port: this.authData.getPort(),
            path: '/api/workspace/' + workspaceId + '/runtime?token=' + this.authData.getToken(),
            method: 'DELETE',
            headers: {
                'Accept': 'application/json, text/plain, */*',
                'Content-Type': 'application/json;charset=UTF-8'
            }
        };

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

            let p = new Promise<WorkspaceDto>( (resolve, reject) => {
                var req = this.http.request(options,  (res) => {

                    var data: string = '';

                    res.on('error',  (body)=> {
                        Log.getLogger().error('got the following error', body.toString());
                        reject(body);
                    });

                    res.on('data',  (body) => {
                        data += body;
                    });

                    res.on('end', () => {
                        if (res.statusCode == 204) {
                            // workspace created, continue
                            Log.getLogger().debug('got data ===', data);
                            Log.getLogger().debug('got data ===', data.toString());
                            resolve(workspaceDto);
                        } else {
                            reject('stop workspace: Invalid response code' + res.statusCode + ':' + data.toString());
                        }
                    });

                });

                req.on('error', (err) => {
                    reject('HTTP error: ' + err);
                });

                req.write('{}');
                req.end();

            });

            return p;
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
