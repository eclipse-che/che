/*
 * Copyright (c) 2018-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
'use strict';
import { ICommunicationClient } from './json-rpc-client';

/**
 * The implementation for JSON RPC protocol communication through websocket.
 *
 * @author Ann Shumilova
 */
export class WebsocketClient implements ICommunicationClient {
    onResponse: Function;
    private websocketStream: WebSocket;

    constructor() {

    }

    /**
     * Performs connection to the pointed entrypoint.
     *
     * @param entrypoint the entrypoint to connect to
     */
    connect(entrypoint: string): Promise<void> {
        return new Promise((resolve, reject) => {
            this.websocketStream = new WebSocket(entrypoint);
            this.websocketStream.addEventListener("open", () => {
                resolve();
            });

            this.websocketStream.addEventListener("error", () => {
                reject();
            });
            this.websocketStream.addEventListener("message", (message) => {
                let data = JSON.parse(message.data);
                this.onResponse(data);
            });
        });

    }

    /**
     * Performs closing the connection.
     */
    disconnect(): void {
        if (this.websocketStream) {
            this.websocketStream.close();
        }
    }

    /**
     * Sends pointed data.
     *
     * @param data to be sent
     */
    send(data: any): void {
        this.websocketStream.send(JSON.stringify(data));
    }
}
