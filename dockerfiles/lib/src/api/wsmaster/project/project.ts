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
import {Log} from "../../../spi/log/log";
import {HttpJsonRequest} from "../../../spi/http/default-http-json-request";
import {DefaultHttpJsonRequest} from "../../../spi/http/default-http-json-request";
import {HttpJsonResponse} from "../../../spi/http/default-http-json-request";

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
    workspaceDTO : org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;

    /**
     * Path to the workspace agent
     */
    wsAgentPath : string;

    constructor(workspaceDTO: org.eclipse.che.api.workspace.shared.dto.WorkspaceDto) {
        this.workspaceDTO = workspaceDTO;

        // search the workspace agent link
        let servers : Map<string, org.eclipse.che.api.machine.shared.dto.ServerDto> = this.workspaceDTO.getRuntime().getDevMachine().getRuntime().getServers();

        var hrefWsAgent;
        for (let server of servers.values()) {
            if (server.getRef() === 'wsagent') {
                hrefWsAgent = server.getProperties().getInternalUrl();
            }
        }

        if (!hrefWsAgent) {
            throw new Error('unable to find the workspace agent link from workspace :' + workspaceDTO.getConfig().getName() + " with JSON " + workspaceDTO.toJson());
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
    getProject(projectName) : Promise<org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto> {

        var jsonRequest:HttpJsonRequest = new DefaultHttpJsonRequest(this.authData, this.wsAgentPath + '/project/' + projectName, 200);
        return jsonRequest.request().then((jsonResponse:HttpJsonResponse) => {
            return jsonResponse.asDto(org.eclipse.che.api.workspace.shared.dto.ProjectConfigDtoImpl);
        });
    }

    /**
     * Updates the given project with the provided project Type
     * @param projectType the type of the project that we want to set
     * @param projectDTO a DTO containing all attributes to set as new attributes
     * @return a promise with the updated Project DTO
     */
    updateType(projectName, projectType) : Promise<org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto> {
        // first get project attributes
        return this.getProject(projectName).then((projectDto) => {
            // then update the project type
            projectDto.setType(projectType);

            // and perform update of all these attributes
            return this.update(projectName, projectDto);
        })

    }

    /**
     */
    estimateType(projectName, projectType) : Promise<org.eclipse.che.api.project.shared.dto.SourceEstimation> {
        var jsonRequest:HttpJsonRequest = new DefaultHttpJsonRequest(this.authData, this.wsAgentPath + '/project/estimate/' + projectName + '?type=' + projectType, 200);
        return jsonRequest.request().then((jsonResponse:HttpJsonResponse) => {
            return jsonResponse.asDto(org.eclipse.che.api.project.shared.dto.SourceEstimationImpl);
        });

    }

    /**
     * Updates the given project with the provided DTO
     * @param projectName the name of the project that will be updated
     * @param projectDTO a DTO containing all attributes to set as new attributes
     * @return a promise with the updated Project DTO
     */
    update(projectName: string, projectDto: org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto): Promise<org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto> {

        var jsonRequest:HttpJsonRequest = new DefaultHttpJsonRequest(this.authData, this.wsAgentPath + '/project/' + projectName, 200).setMethod("PUT").setBody(projectDto);
        return jsonRequest.request().then((jsonResponse:HttpJsonResponse) => {
            return jsonResponse.asDto(org.eclipse.che.api.workspace.shared.dto.ProjectConfigDtoImpl);
        });
    }


    /**
     *
     * @param projectName
     * @param sourceStorageDto
     * @returns {Promise<TResult>}
     */
    importProject(projectName: string, sourceStorageDto : org.eclipse.che.api.workspace.shared.dto.SourceStorageDto) : Promise<void> {

        var jsonRequest:HttpJsonRequest = new DefaultHttpJsonRequest(this.authData, this.wsAgentPath + '/project/import/' + projectName, 204).setMethod("POST").setBody(sourceStorageDto);
        return jsonRequest.request().then((jsonResponse:HttpJsonResponse) => {
            return;
        });
    }


}
