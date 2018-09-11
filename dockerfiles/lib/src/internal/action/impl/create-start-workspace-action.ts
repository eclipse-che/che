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

import {ArgumentProcessor} from "../../../spi/decorator/argument-processor";
import {Workspace} from "../../../api/wsmaster/workspace/workspace";
import {AuthData} from "../../../api/wsmaster/auth/auth-data";
import {Parameter} from "../../../spi/decorator/parameter";
import {CreateWorkspaceConfig} from "../../../api/wsmaster/workspace/workspace";
import {Log} from "../../../spi/log/log";
/**
 * This class is managing a post-check operation by creating a workspace, starting it and displaying the log data.
 * @author Florent Benoit
 */
export class CreateStartWorkspaceAction {

    @Parameter({names: ["-s", "--url"], description: "Defines the url to be used"})
    url : string;

    @Parameter({names: ["-q", "--quiet"], description: "Run in quiet mode for this test."})
    isQuiet : boolean = false;

    @Parameter({names: ["-u", "--user"], description: "Defines the user to be used"})
    username : string;

    @Parameter({names: ["-w", "--password"], description: "Defines the password to be used"})
    password : string;

    authData: AuthData;
    workspace: Workspace;

    constructor(args:Array<string>) {
        ArgumentProcessor.inject(this, args);

        this.authData = new AuthData(this.url, this.username, this.password);
        this.workspace = new Workspace(this.authData);
    }

    run() : Promise<any> {
        // first, login
        return this.authData.login().then(() => {
            // then create the workspace
            let createWorkspaceConfig: CreateWorkspaceConfig = new CreateWorkspaceConfig();
            createWorkspaceConfig.name = 'your-first-workspace';
            Log.getLogger().info('Generating '+ createWorkspaceConfig.name + ' workspace');
            return this.workspace.createWorkspace(createWorkspaceConfig)
                .then(workspaceDto => {
                    // then start it
                    Log.getLogger().info('Starting workspace runtime');
                    return this.workspace.startWorkspace(workspaceDto.getId(), !this.isQuiet);
                }).then((workspaceDto) => {
                    let ideUrl: string = workspaceDto.getLinks().get("ide");
                    Log.getLogger().info(ideUrl);
                });

        });
    }

}
