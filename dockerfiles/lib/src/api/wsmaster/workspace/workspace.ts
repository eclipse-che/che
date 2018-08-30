/*
 * Copyright (c) 2016-2017 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc.- initial API and implementation
 */

import {org} from "../../dto/che-dto"
import {AuthData} from "../auth/auth-data";
import {Websocket} from "../../../spi/websocket/websocket";
import {HttpJsonRequest} from "../../../spi/http/default-http-json-request";
import {DefaultHttpJsonRequest} from "../../../spi/http/default-http-json-request";
import {HttpJsonResponse} from "../../../spi/http/default-http-json-request";
import {WorkspaceStartEventPromiseMessageBusSubscriber} from "./workspace-start-event-promise-subscriber";
import {JsonRpcBus} from "../../../spi/websocket/json-rpc-bus";
import {WorkspaceDisplayOutputMessageBusSubscriber} from "./workspace-log-output-subscriber";
import {Log} from "../../../spi/log/log";
import {WorkspaceStopEventPromiseMessageBusSubscriber} from "./workspace-stop-event-promise-subscriber";
import {RecipeBuilder} from "../../../spi/docker/recipebuilder";
import {CheFileStructWorkspaceCommand} from "../../../internal/dir/chefile-struct/che-file-struct";
import {MessageBusSubscriber} from "../../../spi";

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
        var jsonRequest:HttpJsonRequest = new DefaultHttpJsonRequest(this.authData, null, '/api/workspace/', 200);
        return jsonRequest.request().then((jsonResponse:HttpJsonResponse) => {
            return jsonResponse.asArrayDto(org.eclipse.che.api.workspace.shared.dto.WorkspaceDtoImpl);
        });
    }

    /**
     * Gets a workspace config DTO object from a createWorkspaceConfiguration object
     * @param createWorkspaceConfig
     */
    getWorkspaceConfigDto(createWorkspaceConfig:CreateWorkspaceConfig) : org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto {
        let devMachine : org.eclipse.che.api.workspace.shared.dto.MachineConfigDto = new org.eclipse.che.api.workspace.shared.dto.MachineConfigDtoImpl();
        devMachine.getInstallers().push("org.eclipse.che.exec");
        devMachine.getInstallers().push("org.eclipse.che.terminal");
        devMachine.getInstallers().push("org.eclipse.che.ws-agent");
        devMachine.getInstallers().push("org.eclipse.che.ssh");
        devMachine.getAttributes().set("memoryLimitBytes", "2147483648");

        let defaultEnvironment : org.eclipse.che.api.workspace.shared.dto.EnvironmentDto = new org.eclipse.che.api.workspace.shared.dto.EnvironmentDtoImpl();
        defaultEnvironment.getMachines().set("dev-machine", devMachine);
        defaultEnvironment.setRecipe(new org.eclipse.che.api.workspace.shared.dto.RecipeDtoImpl(createWorkspaceConfig.machineConfigSource));


        let commandsToCreate : Array<org.eclipse.che.api.workspace.shared.dto.CommandDto> = new Array;
        createWorkspaceConfig.commands.forEach(commandConfig => {
            let command : org.eclipse.che.api.workspace.shared.dto.CommandDto = new org.eclipse.che.api.workspace.shared.dto.CommandDtoImpl();
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

        var jsonRequest:HttpJsonRequest = new DefaultHttpJsonRequest(this.authData, null, '/api/workspace?account=', 201).setMethod('POST').setBody(workspaceConfigDto);
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
        var jsonRequest:HttpJsonRequest = new DefaultHttpJsonRequest(this.authData, null, '/api/workspace/' + workspaceId + '/runtime?environment=default', 200).setMethod('POST');
        return jsonRequest.request().then((jsonResponse:HttpJsonResponse) => {
          return jsonResponse.asDto(org.eclipse.che.api.workspace.shared.dto.WorkspaceDtoImpl);
        })}).then((workspaceDto) => {
        userWorkspaceDto = workspaceDto;
        return this.getJsonRpcBus(workspaceDto);
      }).then((jsonRpcBus: JsonRpcBus) => {

        let rpcCommand: any =
            {
              "jsonrpc": "2.0",
              "method": "subscribe",
              "params": {method: "workspace/statusChanged", scope: {workspaceId: workspaceId}}
            };
        jsonRpcBus.send(rpcCommand)
        var displayOutputWorkspaceSubscriber: MessageBusSubscriber = new WorkspaceDisplayOutputMessageBusSubscriber();
        callbackSubscriber = new WorkspaceStartEventPromiseMessageBusSubscriber(jsonRpcBus, userWorkspaceDto);
        jsonRpcBus.subscribe( callbackSubscriber);
        jsonRpcBus.subscribe(callbackSubscriber);
        if (displayLog) {
          let rpcLogCommand: any =
              {
                "jsonrpc": "2.0",
                "method": "subscribe",
                "params": {method: "machine/log", scope: {workspaceId: workspaceId}}
              };
          jsonRpcBus.send(rpcLogCommand);
          let rpcInstallerLogCommand: any =
              {
                "jsonrpc": "2.0",
                "method": "subscribe",
                "params": {method: "installer/log", scope: {workspaceId: workspaceId}}
              };
          jsonRpcBus.send(rpcInstallerLogCommand);

          jsonRpcBus.subscribe(displayOutputWorkspaceSubscriber);
          jsonRpcBus.subscribe(displayOutputWorkspaceSubscriber);
        }
        return userWorkspaceDto;
      }).then((workspaceDto) => {
        return callbackSubscriber.promise;
        }).then(() => {
        return this.getWorkspace(workspaceId);
        });
    };


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

        var jsonRequest:HttpJsonRequest = new DefaultHttpJsonRequest(this.authData, null, '/api/workspace/' + key, 200);
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
        var jsonRequest:HttpJsonRequest = new DefaultHttpJsonRequest(this.authData, null, '/api/workspace/' + workspaceId, 200);
        return jsonRequest.request().then((jsonResponse:HttpJsonResponse) => {
            return jsonResponse.asDto(org.eclipse.che.api.workspace.shared.dto.WorkspaceDtoImpl);
        });
    }


  getJsonRpcBus(workspaceDto:org.eclipse.che.api.workspace.shared.dto.WorkspaceDto): Promise<JsonRpcBus> {
    // get id
    let workspaceId:string = workspaceDto.getId();

    // get links for WS
    var link:string = workspaceDto.getLinks().get("environment/statusChannel");


    return this.websocket.getJsonRpcBus(link + '?token=' + this.authData.getToken());
  }


    /**
     * Delete a workspace and returns a Promise with WorkspaceDto.
     */
    deleteWorkspace(workspaceId:string):Promise<org.eclipse.che.api.workspace.shared.dto.WorkspaceDto> {
        var jsonRequest:HttpJsonRequest = new DefaultHttpJsonRequest(this.authData, null, '/api/workspace/' + workspaceId, 204).setMethod('DELETE');
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

        var jsonRequest:HttpJsonRequest = new DefaultHttpJsonRequest(this.authData, null, '/api/workspace/' + workspaceId + '/runtime', 204).setMethod('DELETE');
        var callbackSubscriber:WorkspaceStopEventPromiseMessageBusSubscriber;

        var userWorkspaceDto : org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
        // get workspace DTO
        return this.getWorkspace(workspaceId).then((workspaceDto) => {
            userWorkspaceDto = workspaceDto;
            return this.getJsonRpcBus(workspaceDto);
        }).then((jsonRpcBus : JsonRpcBus) => {
            callbackSubscriber = new WorkspaceStopEventPromiseMessageBusSubscriber(jsonRpcBus, userWorkspaceDto);
            jsonRpcBus.subscribe(callbackSubscriber);
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


        var jsonRequest:HttpJsonRequest = new DefaultHttpJsonRequest(this.authData, null, '/api/machine/token/' + workspaceDto.getId(), 200);
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
