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
import {org} from "../../../api/dto/che-dto"
import {Argument} from "../../../spi/decorator/parameter";
import {Parameter} from "../../../spi/decorator/parameter";
import {AuthData} from "../../../api/wsmaster/auth/auth-data";
import {Workspace} from "../../../api/wsmaster/workspace/workspace";
import {ArgumentProcessor} from "../../../spi/decorator/argument-processor";
import {Log} from "../../../spi/log/log";
import {Ssh} from "../../../api/wsmaster/ssh/ssh";

/**
 * This class is handling the retrieval of default private ssh key of a workspace, login name and port to use
 * @author Florent Benoit
 */
export class GetSshDataAction {

    @Argument({description: "Defines the workspace to be used. use workspaceId or :workspaceName as argument"})
    workspaceName : string;


    @Parameter({names: ["-s", "--url"], description: "Defines the url to be used"})
    url : string;

    @Parameter({names: ["-u", "--user"], description: "Defines the user to be used"})
    username : string;

    @Parameter({names: ["-w", "--password"], description: "Defines the password to be used"})
    password : string;


    args: Array<string>;
    authData: AuthData;

    fs = require('fs');
    path = require('path');


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
            let foundWorkspaceDTO : org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;

            return this.workspace.searchWorkspace(this.workspaceName).then((workspaceDto) => {

                // check status
                if ('RUNNING' !== workspaceDto.getStatus()) {
                    throw new Error('Workspace should be in running state. Current state is ' + workspaceDto.getStatus());
                }

                // Check ssh agent is there
                let defaultEnv:string = workspaceDto.getConfig().getDefaultEnv();
                let agents:Array<string> = workspaceDto.getConfig().getEnvironments().get(defaultEnv).getMachines().get("dev-machine").getInstallers();

                if (agents.indexOf('org.eclipse.che.ssh') === -1) {
                    return Promise.reject("The SSH agent (org.eclipse.che.ssh) has been disabled for this workspace.")
                }

                foundWorkspaceDTO = workspaceDto;

            }).then(() => {

                // need to get ssh key for the workspace
                let ssh:Ssh = new Ssh(this.authData);
                return ssh.getPair("workspace", foundWorkspaceDTO.getId());
            }).then((sshPairDto : org.eclipse.che.api.ssh.shared.dto.SshPairDto) => {

                let runtime : org.eclipse.che.api.workspace.shared.dto.RuntimeDto = foundWorkspaceDTO.getRuntime();
                let user : string = "root";
                let machines = foundWorkspaceDTO.getRuntime().getMachines();
                let sshAgentServer = machines.get("dev-machine").getServers().get("ssh");

                let address: Array<string> = sshAgentServer.getUrl().replace("/", "").split(":");
                let ip:string = address[1];
                let port:string = address[2];

                Log.getLogger().direct("SSH_IP=" + ip);
                Log.getLogger().direct("SSH_PORT=" + port);
                Log.getLogger().direct("SSH_USER=" + user);
                Log.getLogger().direct("SSH_PRIVATE_KEY='");
                Log.getLogger().direct(sshPairDto.getPrivateKey());
                Log.getLogger().direct("'");


                return Promise.resolve("ok");
            });
        });
    }

}
