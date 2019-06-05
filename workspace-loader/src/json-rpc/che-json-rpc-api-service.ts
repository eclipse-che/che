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

import { ICommunicationClient, JsonRpcClient } from './json-rpc-client';

// tslint:disable:no-any

export class IChannel {
    subscription: string;
    unsubscription: string;
    notification: string;
}

/**
 * Class for basic CHE API communication methods.
 *
 * @author Ann Shumilova
 */
export class CheJsonRpcApiClient {
    /**
     * Client that implements JSON RPC protocol.
     */
    private jsonRpcClient: JsonRpcClient;
    /**
     * Communication client (can be http, websocket).
     */
    private client: ICommunicationClient;

    constructor(client: ICommunicationClient) {
        this.client = client;
        this.jsonRpcClient = new JsonRpcClient(client);
    }

    /**
     * Subscribe on the events from service.
     *
     * @param event event's name to subscribe
     * @param notification notification name to handle
     * @param handler event's handler
     * @param params params (optional)
     */
    /* tslint:disable-next-line:no-any */
    subscribe(event: string, notification: string, handler: Function, params?: any): void {
        this.jsonRpcClient.addNotificationHandler(notification, handler);
        this.jsonRpcClient.notify(event, params);
    }

    /**
     * Unsubscribe concrete handler from events from service.
     *
     * @param event event's name to unsubscribe
     * @param notification notification name binded to the event
     * @param handler handler to be removed
     * @param params params (optional)
     */
    /* tslint:disable-next-line:no-any */
    unsubscribe(event: string, notification: string, handler: Function, params?: any): void {
        this.jsonRpcClient.removeNotificationHandler(notification, handler);
        this.jsonRpcClient.notify(event, params);
    }

    /**
     * Connects to the pointed entrypoint
     *
     * @param entrypoint entrypoint to connect to
     * @returns {Promise<void>} promise
     */
    connect(entrypoint: () => string): Promise<void> {
        return this.client.connect(entrypoint);
    }

    /**
     * Makes request.
     *
     * @param method
     * @param params
     * @returns {Promise<any>}
     */
    request(method: string, params?: any): Promise<any> {
        return this.jsonRpcClient.request(method, params);
    }
}
