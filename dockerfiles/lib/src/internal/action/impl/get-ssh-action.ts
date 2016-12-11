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
        this.authData = AuthData.parse(this.url, this.username, this.password);
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
                let agents:Array<string> = workspaceDto.getConfig().getEnvironments().get(defaultEnv).getMachines().get("dev-machine").getAgents();

                if (agents.indexOf('org.eclipse.che.ssh') === -1) {
                    return Promise.reject("The SSH agent (org.eclipse.che.ssh) has been disabled for this workspace.")
                }

                foundWorkspaceDTO = workspaceDto;

            }).then((workspaceDto) => {

                // need to get ssh key for the workspace
                let ssh:Ssh = new Ssh(this.authData);
                return ssh.getPair("workspace", foundWorkspaceDTO.getId());
            }).then((sshPairDto : org.eclipse.che.api.ssh.shared.dto.SshPairDto) => {

                let runtime : org.eclipse.che.api.machine.shared.dto.MachineRuntimeInfoDto = foundWorkspaceDTO.getRuntime().getDevMachine().getRuntime();
                let user : string = runtime.getProperties().get("config.user");
                if (user === "") {
                    // user is root if not defined
                    user = "root";
                }
                let address: Array<string> = runtime.getServers().get("22/tcp").getProperties().getInternalAddress().split(":");
                let ip:string = address[0];
                let port:string = address[1];

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
