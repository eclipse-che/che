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
 * Logic on events received by the remote workspace. When workspace is going to running state, we close the websocket and display the IDE URL.
 * @author Florent Benoit
 */
export class WorkspaceEventMessageBusSubscriber implements MessageBusSubscriber {

    messageBus : MessageBus;
    workspaceDto : WorkspaceDto;


    constructor(messageBus : MessageBus, workspaceDto : WorkspaceDto) {
        this.messageBus = messageBus;
        this.workspaceDto = workspaceDto;
    }

    handleMessage(message: any) {
        if ('RUNNING' === message.eventType) {

            // search IDE url link
            let ideUrl: string;
            this.workspaceDto.getContent().links.forEach((link) => {
                if ('ide url' === link.rel) {
                    ideUrl = link.href;
                }
            });
            Log.getLogger().info('Workspace is now running. Please connect to ' + ideUrl);
            this.messageBus.close();
        } else if ('ERROR' === message.eventType) {
            Log.getLogger().error('Error when starting the workspace', message);
            this.messageBus.close();
            process.exit(1);
        } else {
            Log.getLogger().info('Event on workspace : ', message.eventType);
        }

    }

}
