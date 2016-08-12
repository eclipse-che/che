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


    constructor(authData : AuthData) {
        this.authData = authData;
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
    startWorkspace(workspaceId: string) : Promise<WorkspaceDto> {

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
        return p;
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

}



export class CreateWorkspaceConfig {

    ram : number = 2048;
    dockerContent: string;
    machineConfigSource : any = {"type": "dockerfile", "content": RecipeBuilder.DEFAULT_DOCKERFILE_CONTENT};
    name: string = "default";
    commands: Array<CheFileStructWorkspaceCommand> = [];

}
