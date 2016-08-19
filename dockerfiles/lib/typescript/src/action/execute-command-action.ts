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


// imports
import {AuthData} from "../auth-data";
import {Log} from "../log";
import {Parameter, ParameterType} from "../parameter/parameter";
import {ArgumentProcessor} from "../parameter/argument-processor";
import {Argument} from "../parameter/parameter";
import {Workspace} from "../workspace";
import {MachineServiceClientImpl} from "../machine-service-client";
import {MachineProcessDto} from "../dto/machine-process-dto";
import {UUID} from "../uuid";

/**
 * This class is handling the removal of a user
 * @author Florent Benoit
 */
export class ExecuteCommandAction {

    @Argument({description: "Defines the workspace to be used"})
    workspaceName : string;


    @Parameter({names: ["-s", "--url"], description: "Defines the url to be used"})
    url : string;

    @Parameter({names: ["-u", "--user"], description: "Defines the user to be used"})
    username : string;

    @Parameter({names: ["-w", "--password"], description: "Defines the password to be used"})
    password : string;


    args: Array<string>;
    authData: AuthData;

    workspace : Workspace;
    constructor(args:Array<string>) {
        this.args = ArgumentProcessor.inject(this, args);
        this.authData = AuthData.parse(this.url, this.username, this.password);
        // disable printing info
        this.authData.printInfo = false;
        Log.disablePrefix();
        this.workspace = new Workspace(this.authData);
    }

    run() : Promise<any> {
        // first, login
        return this.authData.login().then(() => {

            // then, search workspace
            return this.workspace.searchWorkspace(this.workspaceName).then((workspaceDto) => {

                // check status
                if ('RUNNING' !== workspaceDto.getContent().status) {
                    throw new Error('Workspace should be in running state. Current state is ' + workspaceDto.getContent().status);
                }

                // get dev machine
                let machineId : string = workspaceDto.getContent().runtime.devMachine.id;

                // now, execute command
                let uuid : string = UUID.build();
                let channel : string = 'process:output:' + uuid;
                let machineServiceClient : MachineServiceClientImpl = new MachineServiceClientImpl(this.workspace, this.authData);
                return machineServiceClient.executeCommand(workspaceDto, machineId, this.args.join(" "), channel);
            }).then((machineProcessDto: MachineProcessDto) => {
                // command executed
            });
        });
    }

}
