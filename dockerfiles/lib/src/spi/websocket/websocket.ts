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

import {JsonRpcBus} from "./json-rpc-bus";

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
     * Default constructor initializing websocket.
     */
    constructor() {
        this.wsClient = require('websocket').client;
    }

    /**
     * Gets a MessageBus object for a remote workspace, by providing the remote URL to this websocket
     * @param websocketURL the remote host base WS url
     */
    getJsonRpcBus(websocketURL) : Promise<JsonRpcBus> {
        var webSocketClient: any = new this.wsClient();
        var remoteWebsocketUrl: string = websocketURL;
        let promise : Promise<JsonRpcBus> = new Promise<JsonRpcBus>((resolve, reject) => {
            return new JsonRpcBus(webSocketClient, remoteWebsocketUrl, this, resolve, reject);
        });
        return promise;
    }

}
