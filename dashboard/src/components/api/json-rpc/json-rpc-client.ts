/*
 * Copyright (c) 2015-2018 Red Hat, Inc.
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

const JSON_RPC_VERSION: string = '2.0';

/* tslint:disable */
export type communicationClientEvent = 'close' | 'error' | 'open' | 'response';
/* tslint:enable */

/**
 * Interface for communication between two entrypoints.
 * The implementation can be through websocket or http protocol.
 */
export interface ICommunicationClient {
  /**
   * Performs connections.
   *
   * @param entrypoint
   */
  connect(entrypoint: string): ng.IPromise<any>;
  /**
   * Close the connection.
   */
  disconnect(): void;
  /**
   * Adds listener on client event.
   */
  addListener(event: communicationClientEvent, handler: Function): void;
  /**
   * Removes listener.
   *
   * @param {communicationClientEvent} event
   * @param {Function} handler
   */
  removeListener(event: communicationClientEvent, handler: Function): void;
  /**
   * Send pointed data.
   *
   * @param data data to be sent
   */
  send(data: any): void;
  /**
   * Provides deferred obejct.
   */
  getDeferred(): ng.IDeferred<any>;
}

interface IRequest {
  jsonrpc: string;
  id: string;
  method: string;
  params: any;
}

interface IResponse {
  jsonrpc: string;
  id: string;
  result?: any;
  error?: IError;
}

interface INotification {
  jsonrpc: string;
  method: string;
  params: any;
}

interface IError {
  number: number;
  message: string;
  data?: any;
}

/**
 * This client is handling the JSON RPC requests, responses and notifications.
 *
 * @author Ann Shumilova
 */
export class JsonRpcClient {
  /**
   * Client for performing communications.
   */
  private client: ICommunicationClient;
  /**
   * The list of the pending requests by request id.
   */
  private pendingRequests: Map<string, ng.IDeferred<any>>;
  /**
   * The list of notification handlers by method name.
   */
  private notificationHandlers: Map<string, Array<Function>>;
  private counter: number = 100;

  constructor (client: ICommunicationClient) {
    this.client = client;
    this.pendingRequests = new Map<string, ng.IDeferred<any>>();
    this.notificationHandlers = new Map<string, Array<Function>>();

    this.client.addListener('response', (message: any) => {
      this.processResponse(message);
    });
  }

  /**
   * Performs JSON RPC request.
   *
   * @param method method's name
   * @param params params
   * @returns {IPromise<any>}
   */
  request(method: string, params?: any): ng.IPromise<any> {
    let deferred = this.client.getDeferred();
    let id: string = (this.counter++).toString();
    this.pendingRequests.set(id, deferred);

    let request: IRequest = {
      jsonrpc: JSON_RPC_VERSION,
      id: id,
      method: method,
      params: params
    };

    this.client.send(request);
    return deferred.promise;
  }

  /**
   * Sends JSON RPC notification.
   *
   * @param method method's name
   * @param params params (optional)
   */
  notify(method: string, params?: any): void {
    let request: INotification = {
      jsonrpc: JSON_RPC_VERSION,
      method: method,
      params: params
    };
    this.client.send(request);
  }

  /**
   * Adds notification handler.
   *
   * @param method method's name
   * @param handler handler to process notification
   */
  public addNotificationHandler(method: string, handler: Function): void {
    let handlers = this.notificationHandlers.get(method);

    if (handlers) {
      handlers.push(handler);
    } else {
      handlers = [handler];
      this.notificationHandlers.set(method, handlers);
    }
  }

  /**
   * Removes notification handler.
   *
   * @param method method's name
   * @param handler handler
   */
  public removeNotificationHandler(method: string, handler: Function): void {
    let handlers = this.notificationHandlers.get(method);

    if (handlers && handler) {
      handlers.splice(handlers.indexOf(handler), 1);
    }
  }

  /**
   * Processes response - detects whether it is JSON RPC response or notification.
   *
   * @param message
   */
  private processResponse(message: any): void {
    if (message.id && this.pendingRequests.has(message.id)) {
      this.processResponseMessage(message);
    } else {
      this.processNotification(message);
    }
  }

  /**
   * Processes JSON RPC notification.
   *
   * @param message message
   */
  private processNotification(message: any): void {
    let method = message.method;
    let handlers = this.notificationHandlers.get(method);
    if (handlers && handlers.length > 0) {
      handlers.forEach((handler: Function) => {
        handler(message.params);
      });
    }
  }

  /**
   * Process JSON RPC response.
   *
   * @param message
   */
  private processResponseMessage(message: any): void {
    let promise = this.pendingRequests.get(message.id);
    if (message.result) {
      promise.resolve(message.result);
      return;
    }
    if (message.error) {
      promise.reject(message.error);
    }
  }
}
