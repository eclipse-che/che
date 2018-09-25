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
// imports
import {Argument} from "../../../spi/decorator/parameter";
import {Parameter} from "../../../spi/decorator/parameter";
import {AuthData} from "../../../api/wsmaster/auth/auth-data";
import {Workspace} from "../../../api/wsmaster/workspace/workspace";
import {ArgumentProcessor} from "../../../spi/decorator/argument-processor";
import {Log} from "../../../spi/log/log";
import {UUID} from "../../../utils/uuid";
import {CheFileStructWorkspaceCommand} from "../../dir/chefile-struct/che-file-struct";
import {CheFileStructWorkspaceCommandImpl} from "../../dir/chefile-struct/che-file-struct";
import {ExecAgentServiceClientImpl} from "../../../api/exec-agent/exec-agent-service-client";
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
        this.authData = new AuthData(this.url, this.username, this.password);
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

                // get exec-agent URI
                let machines = workspaceDto.getRuntime().getMachines();
                let execAgentServer = machines.get("dev-machine").getServers().get("exec-agent/ws");
                let execAgentURI = execAgentServer.getUrl();

                // now, execute command
                let uuid : string = UUID.build();
                let execAgentServiceClientImpl : ExecAgentServiceClientImpl = new ExecAgentServiceClientImpl(this.workspace, this.authData, execAgentURI);

                let workspaceCommand : CheFileStructWorkspaceCommand = new CheFileStructWorkspaceCommandImpl();
                workspaceCommand.commandLine = this.args.join(" ");
                return execAgentServiceClientImpl.executeCommand(workspaceCommand, uuid);
            });
        });
    }

}
