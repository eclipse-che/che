/*
 * Copyright (c) 2018-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
'use strict';

import { ICommunicationClient, CommunicationClientEvent } from './json-rpc-client';
import * as ReconnectingWebsocket from 'reconnecting-websocket';

// tslint:disable:no-any

const RWS = require('reconnecting-websocket');

/**
 * The implementation for JSON RPC protocol communication through websocket.
 *
 * @author Ann Shumilova
 */
export class WebsocketClient implements ICommunicationClient {
    private websocketStream: ReconnectingWebsocket;
    private handlers: { [event: string]: Function[] } = {};

    /**
     * Performs connection to the pointed entrypoint.
     *
     * @param entrypoint the entrypoint to connect to
     */
    connect(entrypoint: (() => string)): Promise<void> {
        return new Promise((resolve, reject) => {
            this.websocketStream = new RWS(entrypoint, [], {
                connectionTimeout: 10000
            });
            this.websocketStream.addEventListener('open', (event: Event) => {
                const eventType: CommunicationClientEvent = 'open';
                this.callHandlers(eventType, event);
                resolve();
            });
            this.websocketStream.addEventListener('error', (event: Event) => {
                const eventType: CommunicationClientEvent = 'error';
                this.callHandlers(eventType, event);
                reject();
            });
            this.websocketStream.addEventListener('message', (message: any) => {
                const data = JSON.parse(message.data);
                const eventType: CommunicationClientEvent = 'message';
                this.callHandlers(eventType, data);
            });
            this.websocketStream.addEventListener('close', (event: Event) => {
                const eventType: CommunicationClientEvent = 'close';
                this.callHandlers(eventType, event);
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
     * @param {communicationClientEvent} eventType
     * @param {Function} handler
     */
    removeListener(eventType: CommunicationClientEvent, handler: Function): void {
        if (!this.handlers[eventType] || !handler) {
            return;
        }
        const index = this.handlers[eventType].indexOf(handler);
        if (index === -1) {
            return;
        }
        this.handlers[eventType].splice(index, 1);
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

    private sleep(ms: number): Promise<any> {
        return new Promise<any>(resolve => setTimeout(resolve, ms));
    }

    /**
     * Sends pointed data.
     *
     * @param data to be sent
     */
    async send(data: any): Promise<void> {
        while (this.websocketStream.readyState !== this.websocketStream.OPEN) {
            /* Wait for the reconnection establshed. */
            await this.sleep(1000);
        }
        return this.websocketStream.send(JSON.stringify(data));
    }

    private callHandlers(event: CommunicationClientEvent, data?: any): void {
        if (this.handlers[event] && this.handlers[event].length > 0) {
            this.handlers[event].forEach((handler: Function) => handler(data));
        }
    }
}
