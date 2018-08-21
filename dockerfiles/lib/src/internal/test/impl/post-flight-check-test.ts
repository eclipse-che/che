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
import {Parameter} from "../../../spi/decorator/parameter";
import {AuthData} from "../../../api/wsmaster/auth/auth-data";
import {Workspace} from "../../../api/wsmaster/workspace/workspace";
import {ArgumentProcessor} from "../../../spi/decorator/argument-processor";
import {CreateWorkspaceConfig} from "../../../api/wsmaster/workspace/workspace";
import {Log} from "../../../spi/log/log";
import {I18n} from "../../../spi/i18n/i18n";
import {Message} from "../../../spi/decorator/message";

/**
 * This class is managing a post-check operation by creating a workspace, starting it and displaying the log data.
 * @author Florent Benoit
 */
export class PostFlightCheckTest {

    @Parameter({names: ["-q", "--quiet"], description: "Run in quiet mode for this test."})
    isQuiet : boolean = false;

    @Parameter({names: ["-u", "--user"], description: "Defines the user to be used"})
    username : string;

    @Parameter({names: ["-w", "--password"], description: "Defines the password to be used"})
    password : string;

    @Parameter({names: ["-p", "--port"], description: "Defines the optional port if no url is given"})
    portNumber : number;

    authData: AuthData;
    workspace: Workspace;

    @Message('internal/test/impl/post-flight-check-test')
    i18n : I18n;

    constructor(args:Array<string>) {
        let updatedArgs = ArgumentProcessor.inject(this, args);

        let url: string;
        // get options from arguments
        if (updatedArgs.length > 0) {
            url = updatedArgs[0];
        }
        this.authData = new AuthData(url, this.username, this.password);
        if (this.portNumber) {
            this.authData.getMasterLocation().setPort(this.portNumber);
        }
        this.workspace = new Workspace(this.authData);
    }

    run() : Promise<any> {
        // first, login
        return this.authData.login().then(() => {
            // then create the workspace
            let createWorkspaceConfig: CreateWorkspaceConfig = new CreateWorkspaceConfig();
            createWorkspaceConfig.name = 'your-first-workspace';
            Log.getLogger().info(this.i18n.get('run.generating',createWorkspaceConfig.name));

            return this.workspace.createWorkspace(createWorkspaceConfig)
                .then(workspaceDto => {
                    // then start it
                    Log.getLogger().info(this.i18n.get('run.starting-workspace'));
                    return this.workspace.startWorkspace(workspaceDto.getId(), !this.isQuiet);
                }).then((workspaceDto) => {
                    // then stop it
                    Log.getLogger().info(this.i18n.get('run.stopping-workspace'));
                    return this.workspace.stopWorkspace(workspaceDto.getId());
                }).then((workspaceDto) => {
                    // then delete it
                    Log.getLogger().info(this.i18n.get('run.deleting-workspace'));
                    return this.workspace.deleteWorkspace(workspaceDto.getId());
                });

        });
    }

}
