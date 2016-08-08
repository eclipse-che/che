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
import {Workspace} from './workspace';
import {WorkspaceDto} from './dto/workspacedto';
import {Websocket} from './websocket';
import {MessageBus} from './messagebus';
import {WorkspaceEventMessageBusSubscriber} from './workspace-event-subscriber';
import {MessageBusSubscriber} from './messagebus-subscriber';
import {WorkspaceDisplayOutputMessageBusSubscriber} from './workspace-log-output-subscriber';
import {AuthData} from "./auth-data";
import {Log} from "./log";


/**
 * This class is managing a post-check operation by creating a workspace, starting it and displaying the log data.
 * @author Florent Benoit
 */
export class PostCheck {


    websocket: Websocket;
    authData: AuthData;
    workspace: Workspace;
    promiseAuth: Promise<any>;

    constructor(args:Array<string>) {
        this.websocket = new Websocket();

        if (args.length > 0) {
            this.authData = AuthData.parse(args[0]);
        } else {
            this.authData = new AuthData();
        }

        // if login and password, get a token
        if (args.length >=3) {
            // get token from login/password
            this.promiseAuth = this.authData.initToken(args[1], args[2]);
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
        var promise:Promise<WorkspaceDto> = this.workspace.createWorkspace('your-first-workspace', 'FROM codenvy/ubuntu_jdk8')
            .then(workspaceDto => {

                // get id
                let workspaceId:string = workspaceDto.getId();

                var protocol: string;
                if (this.authData.isSecured()) {
                    protocol = 'wss';
                } else {
                    protocol = 'ws';
                }

                // get links for WS
                var link: string;
                workspaceDto.getContent().links.forEach(workspaceLink => {
                    if ('get workspace events channel' === workspaceLink.rel) {
                        link = workspaceLink.href;
                    }
                });

                var messageBus:MessageBus = this.websocket.getMessageBus(link + '?token=' + this.authData.getToken() , workspaceId);

                var callbackSubscriber:MessageBusSubscriber = new WorkspaceEventMessageBusSubscriber(messageBus, workspaceDto);
                var displayOutputWorkspaceSubscriber:MessageBusSubscriber = new WorkspaceDisplayOutputMessageBusSubscriber();


                messageBus.subscribe('workspace:' + workspaceId + ':ext-server:output', displayOutputWorkspaceSubscriber);
                messageBus.subscribe(workspaceId + ':default:default', displayOutputWorkspaceSubscriber);
                messageBus.subscribe('workspace:' + workspaceId, callbackSubscriber);

                // wait to connect websocket
                var waitTill = new Date(new Date().getTime() + 4 * 1000);
                while(waitTill > new Date()){}

                // start it
                var startWorkspacePromise:Promise<WorkspaceDto> = this.workspace.startWorkspace(workspaceDto.getId());
                startWorkspacePromise.then(workspaceDto => {
                    return new Promise<WorkspaceDto>( (resolve, reject) => {
                        Log.getLogger().info('Workspace is now starting...');
                        resolve(workspaceDto);
                    });
                }, error => {
                    return new Promise<WorkspaceDto>( (resolve, reject) => {
                        reject(error);
                    });
                });

                return startWorkspacePromise;

            }, r => {
                return new Promise<WorkspaceDto>( (resolve, reject) => {
                    reject(r);
                });
            });

        return promise;
    }

}
