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
// imports
import {org} from "../../../api/dto/che-dto"
import {Argument} from "../../../spi/decorator/parameter";
import {Parameter} from "../../../spi/decorator/parameter";
import {AuthData} from "../../../api/wsmaster/auth/auth-data";
import {Workspace} from "../../../api/wsmaster/workspace/workspace";
import {ArgumentProcessor} from "../../../spi/decorator/argument-processor";
import {Log} from "../../../spi/log/log";
import {MachineServiceClientImpl} from "../../../api/wsmaster/machine/machine-service-client";
import {UUID} from "../../../utils/uuid";
import {CheFileStructWorkspaceCommand} from "../../dir/chefile-struct/che-file-struct";
import {CheFileStructWorkspaceCommandImpl} from "../../dir/chefile-struct/che-file-struct";
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
                if ('RUNNING' !== workspaceDto.getStatus()) {
                    throw new Error('Workspace should be in running state. Current state is ' + workspaceDto.getStatus());
                }

                // get dev machine
                let machineId : string = workspaceDto.getRuntime().getDevMachine().getId();

                // now, execute command
                let uuid : string = UUID.build();
                let channel : string = 'process:output:' + uuid;
                let machineServiceClient : MachineServiceClientImpl = new MachineServiceClientImpl(this.workspace, this.authData);

                let workspaceCommand : CheFileStructWorkspaceCommand = new CheFileStructWorkspaceCommandImpl();
                workspaceCommand.commandLine = this.args.join(" ");
                return machineServiceClient.executeCommand(workspaceDto, machineId, workspaceCommand, channel);
            }).then((machineProcessDto: org.eclipse.che.api.machine.shared.dto.MachineProcessDto) => {
                // command executed
            });
        });
    }

}
