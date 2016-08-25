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

import {ProjectDto} from "./dto/projectdto";
import {AuthData} from "../auth/auth-data";
import {WorkspaceDto} from "../workspace/dto/workspacedto";
import {Log} from "../../../spi/log/log";

/**
 * Project class allowing to manage a project like updating project-type.
 * @author Florent Benoit
 */
export class Project {

    /**
     * The HTTP library used to call REST API.
     */
    http: any;

    /**
     * Authentication data
     */
    authData : AuthData;

    /**
     * Workspace DTO
     */
    workspaceDTO : WorkspaceDto;

    /**
     * Path to the workspace agent
     */
    wsAgentPath : string;

    constructor(workspaceDTO: WorkspaceDto) {
        this.workspaceDTO = workspaceDTO;

        // searche the workspace agent link
        let links : Array<any> = this.workspaceDTO.getContent().runtime.links;
        var hrefWsAgent;
        links.forEach((link) => {
           if ('wsagent' === link.rel) {
               hrefWsAgent = link.href;
           }
        });
        if (!hrefWsAgent) {
            throw new Error('unable to find the workspace agent in dto :' + JSON.stringify(this.workspaceDTO.getContent()));
        }
        var urlObject : any = require('url').parse(hrefWsAgent);

        this.authData = AuthData.parse(urlObject);
        if (this.authData.isSecured()) {
            this.http = require('https');
        } else {
            this.http = require('http');
        }

        this.wsAgentPath = urlObject.path;
    }

    /**
     * Get project details for a given project name
     */
    getProject(projectName) : Promise<ProjectDto> {
        var options = {
            hostname: this.authData.getHostname(),
            port: this.authData.getPort(),
            path: this.wsAgentPath + '/project/' + projectName + '?token=' + this.authData.getToken(),
            method: 'GET',
            headers: {
                'Accept': 'application/json, text/plain, */*',
                'Content-Type': 'application/json;charset=UTF-8'
            }
        };

        return new Promise<ProjectDto>((resolve, reject) => {
            var req = this.http.request(options, (res) => {

                var data:string = '';

                res.on('error', (error) => {
                    Log.getLogger().error('rejecting as we got error', error);
                    reject('invalid response' + error)
                });

                res.on('data', (body) => {
                    data += body;
                });

                res.on('end', () => {
                    if (res.statusCode == 200) {
                        resolve(new ProjectDto(JSON.parse(data)));
                    } else {
                        reject('Invalid response code');
                    }
                });
            });


            req.on('error', (err) => {
                Log.getLogger().error('rejecting as we got error', err);
                reject('HTTP error: ' + err);
            });


            req.write('{}');
            req.end();

        });

    }

    /**
     * Updates the given project with the provided project Type
     * @param projectType the type of the project that we want to set
     * @param projectDTO a DTO containing all attributes to set as new attributes
     * @return a promise with the updated Project DTO
     */
    updateType(projectName, projectType) : Promise<ProjectDto> {
        // first get project attributes
        return this.getProject(projectName).then((projectDto) => {
            // then update the project type
            projectDto.getContent().type = projectType;

            // and perform update of all these attributes
            return this.update(projectName, projectDto);
        })

    }


    /**
     * Updates the given project with the provided DTO
     * @param projectName the name of the project that will be updated
     * @param projectDTO a DTO containing all attributes to set as new attributes
     * @return a promise with the updated Project DTO
     */
    update(projectName: string, projectDto: ProjectDto): Promise<ProjectDto> {

        var options = {
            hostname: this.authData.getHostname(),
            port: this.authData.getPort(),
            path: this.wsAgentPath + '/project/' + projectName + '?token=' + this.authData.getToken(),
            method: 'PUT',
            headers: {
                'Accept': 'application/json, text/plain, */*',
                'Content-Type': 'application/json;charset=UTF-8'
            }
        };



        return new Promise<ProjectDto>((resolve, reject) => {
            var req = this.http.request(options, (res) => {

                var data:string = '';

                res.on('error', (error) => {
                    Log.getLogger().error('rejecting as we got error', error);
                    reject('invalid response' + error)
                });

                res.on('data', (body) => {
                    data += body;
                });

                res.on('end', () => {
                    if (res.statusCode == 200) {
                        resolve(new ProjectDto(JSON.parse(data)));
                    } else {
                        reject('Invalid response code while updating project with name "' + projectName + '". Error code "' + res.statusCode + '" and response is "' + data + "'.");
                    }
                });
            });


            req.on('error', (err) => {
                Log.getLogger().error('rejecting as we got error', err);
                reject('HTTP error: ' + err);
            });

            req.write(JSON.stringify(projectDto.getContent()));
            req.end();

        });
    }

}
