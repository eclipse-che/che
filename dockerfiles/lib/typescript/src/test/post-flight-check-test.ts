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
import {Websocket} from '../websocket';
import {MessageBus} from '../messagebus';
import {MessageBusSubscriber} from '../messagebus-subscriber';
import {WorkspaceDisplayOutputMessageBusSubscriber} from '../workspace-log-output-subscriber';
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

    websocket: Websocket;
    authData: AuthData;
    workspace: Workspace;
    promiseAuth: Promise<any>;

    constructor(args:Array<string>) {
        let updatedArgs = ArgumentProcessor.inject(this, args);

        this.websocket = new Websocket();

        // get options from arguments
        if (updatedArgs.length > 0) {
            this.authData = AuthData.parse(updatedArgs[0]);
        } else {
            this.authData = new AuthData();
        }

        // if login and password, get a token
        if (this.username && this.password) {
            // get token from login/password
            this.promiseAuth = this.authData.initToken(this.username, this.password);
        }

        this.workspace = new Workspace(this.authData);
    }

    run() : Promise<string> {

        Log.context = 'ECLIPSE CHE TEST/post-check';
        let p = new Promise<any>( (resolve, reject) => {

            var securedOrNot:string;
            if (this.authData.isSecured()) {
                securedOrNot = ' using SSL.';
            } else {
                securedOrNot = '.';
            }
            Log.getLogger().info('using hostname \"' + this.authData.getHostname() + '\" and port \"' + this.authData.getPort() + '\"' + securedOrNot);

            // we have a promise for auth, wait it
            if (this.promiseAuth) {
                this.promiseAuth.then(()=> {
                    resolve('Successfully authenticated.');
                }, error => {
                    reject('Error while authenticating: ' +  error.toString());
                })
            } else {
                resolve('No authentication required');
            }

        }).then((val:string) => {
            return this.createAndStart();
        });


        return p;

    }


    createAndStart(): Promise<any> {

        // create the workspace
        let createWorkspaceConfig: CreateWorkspaceConfig = new CreateWorkspaceConfig();
        createWorkspaceConfig.name = 'your-first-workspace';
        Log.getLogger().info('Generating '+ createWorkspaceConfig.name + ' workspace');
        var promise:Promise<WorkspaceDto> = this.workspace.createWorkspace(createWorkspaceConfig)
            .then(workspaceDto => {
                // start it
                Log.getLogger().info('Starting workspace runtime');
                return this.workspace.startWorkspace(workspaceDto.getId());
            }).then((workspaceDto) => {
                Log.getLogger().info('Stopping workspace runtime');
                // now stop it
                return this.workspace.stopWorkspace(workspaceDto.getId());
            }).then((workspaceDto) => {
                Log.getLogger().info('Deleting workspace runtime');
                // now delete it
                return this.workspace.deleteWorkspace(workspaceDto.getId());
            });

        return promise;
    }

}
