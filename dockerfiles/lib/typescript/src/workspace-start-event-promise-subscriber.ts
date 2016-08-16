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

import {MessageBusSubscriber} from './messagebus-subscriber';
import {MessageBus} from './messagebus';
import {AuthData} from './auth-data';
import {WorkspaceDto} from './dto/workspacedto';
import {Log} from "./log";

/**
 * Handle a promise that will be resolved when workspace is started.
 * If workspace has error, promise will be rejected
 * @author Florent Benoit
 */
export class WorkspaceStartEventPromiseMessageBusSubscriber implements MessageBusSubscriber {

    messageBus : MessageBus;
    workspaceDto : WorkspaceDto;

    resolve : any;
    reject : any;
    promise: Promise<string>;

    constructor(messageBus : MessageBus, workspaceDto : WorkspaceDto) {
        this.messageBus = messageBus;
        this.workspaceDto = workspaceDto;
        this.promise = new Promise<string>((resolve, reject) => {
            this.resolve = resolve;
            this.reject = reject;
        });
    }

    handleMessage(message: any) {
        if ('RUNNING' === message.eventType) {
            this.resolve(this.workspaceDto);
            this.messageBus.close();
        } else if ('ERROR' === message.eventType) {
            console.log('there is an error when starting workspace', message, message.eventType);
            try {
                let parsed: any = JSON.parse(message);
                console.log('there is an error when starting workspace', parsed);

                this.reject('Error when starting the workspace' + parsed);
            } catch (error) {
                this.reject('Error when starting the workspace' + message.toString());
            }
            this.messageBus.close();
        } else {
            Log.getLogger().debug('Event on workspace : ', message.eventType);
        }

    }

}
