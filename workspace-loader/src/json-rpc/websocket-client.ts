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
import { ICommunicationClient, CommunicationClientEvent } from './json-rpc-client';
import * as ReconnectingWebsocket from 'reconnecting-websocket';
const RWS = require('reconnecting-websocket');

/**
 * The implementation for JSON RPC protocol communication through websocket.
 *
 * @author Ann Shumilova
 */
export class WebsocketClient implements ICommunicationClient {
    private websocketStream: ReconnectingWebsocket;
    private handlers: {[event: string]: Function[]} =  {};

    /**
     * Performs connection to the pointed entrypoint.
     *
     * @param entrypoint the entrypoint to connect to
     */
    connect(entrypoint: string): Promise<void> {
        return new Promise((resolve, reject) => {
            this.websocketStream = new RWS(entrypoint, [], {});
            this.websocketStream.addEventListener("open", data => {
                const event: CommunicationClientEvent = "open";
                this.callHandlers(event, data);
                resolve();
            });
            this.websocketStream.addEventListener("error", error => {
                const event: CommunicationClientEvent = "error";
                this.callHandlers(event, error);
                reject();
            });
            this.websocketStream.addEventListener("message", (message) => {
                const data = JSON.parse(message.data);
                const event: CommunicationClientEvent = "message";
                this.callHandlers(event, data);
            });
            this.websocketStream.addEventListener("close", error => {
                const event: CommunicationClientEvent = "close";
                this.callHandlers(event, error);
            });
        });
    }

    /**
     * Adds a listener on an event.
     *
     * @param {communicationClientEvent} event
     * @param {Function} handler
     */
    addListener(event: CommunicationClientEvent, handler: Function): void {
        if (!this.handlers[event]) {
            this.handlers[event] = [];
        }
        this.handlers[event].push(handler);
    }

    /**
     * Removes a listener.
     *
     * @param {communicationClientEvent} event
     * @param {Function} handler
     */
    removeListener(event: CommunicationClientEvent, handler: Function): void {
        if (!this.handlers[event] || !handler) {
            return;
        }
        const index = this.handlers[event].indexOf(handler);
        if (index === -1) {
            return;
        }
        this.handlers[event].splice(index, 1);
    }

    /**
     * Performs closing the connection.
     * @param {number} code close code
     */
    disconnect(code?: number): void {
        if (this.websocketStream) {
            this.websocketStream.close(code ? code : undefined);
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

    private callHandlers(event: CommunicationClientEvent, data?: any): void {
        if (this.handlers[event] && this.handlers[event].length > 0) {
            this.handlers[event].forEach(handler => handler(data));
        }
    }
}
