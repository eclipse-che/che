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


import {MessageBus} from './messagebus';

/**
 * This class is handling the websocket handling by providing a {@link MessageBus} object
 * @author Florent Benoit
 */
export class Websocket {

    /**
     * Websocket client object.
     */
    wsClient: any;

    /**
     * Map of bus per workspace ID.
     */
    websocketConnections: Map<string, MessageBus>;

    /**
     * Default constructor initializing websocket.
     */
    constructor() {
        this.wsClient = require('websocket').client;
        this.websocketConnections = new Map<string, MessageBus>();
    }

    /**
     * Gets a MessageBus object for a remote workspace, by providing the remote URL to this websocket
     * @param websocketURL the remote host base WS url
     * @param workspaceId the workspaceID used as suffix for the URL
     */
    getMessageBus(websocketURL, workspaceId) : Promise<MessageBus> {
        var bus: MessageBus = this.websocketConnections.get(workspaceId);
        if (bus) {
            return Promise.resolve(bus);
        }
        var webSocketClient: any = new this.wsClient();
        var remoteWebsocketUrl: string = websocketURL;
        let promise : Promise<MessageBus> = new Promise<MessageBus>((resolve, reject) => {
            bus = new MessageBus(webSocketClient, remoteWebsocketUrl, workspaceId, this, resolve, reject);
            this.websocketConnections.set(workspaceId, bus);
        });

        return promise.then(() => {
            return bus;
        });

    }


    cleanup(workspaceId) {
        this.websocketConnections.delete(workspaceId);
    }

}
