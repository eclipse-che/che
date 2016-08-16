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
import {Workspace, CreateWorkspaceConfig} from '../workspace';
import {WorkspaceDto} from '../dto/workspacedto';
import {AuthData} from "../auth-data";
import {Log} from "../log";
import {Parameter, ParameterType} from "../parameter/parameter";
import {ArgumentProcessor} from "../parameter/argument-processor";

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

    authData: AuthData;
    workspace: Workspace;

    constructor(args:Array<string>) {
        let updatedArgs = ArgumentProcessor.inject(this, args);

        let url: string;
        // get options from arguments
        if (updatedArgs.length > 0) {
            url = updatedArgs[0];
        }
        this.authData = AuthData.parse(url, this.username, this.password);
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
                    // then stop it
                    Log.getLogger().info('Stopping workspace runtime');
                    return this.workspace.stopWorkspace(workspaceDto.getId());
                }).then((workspaceDto) => {
                    // then delete it
                    Log.getLogger().info('Deleting workspace runtime');
                    return this.workspace.deleteWorkspace(workspaceDto.getId());
                });

        });
    }

}
