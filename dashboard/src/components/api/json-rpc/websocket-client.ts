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
import {communicationClientEvent, ICommunicationClient} from './json-rpc-client';

/**
 * The implementation for JSON RPC protocol communication through websocket.
 *
 * @author Ann Shumilova
 */
export class WebsocketClient implements ICommunicationClient {
  onResponse: Function;
  private $websocket: any;
  private $q: ng.IQService;
  private websocketStream;

  private handlers: {[event: string]: Function[]};

  constructor ($websocket: any, $q: ng.IQService) {
    this.$websocket = $websocket;
    this.$q = $q;

    this.handlers = {};
  }

  /**
   * Performs connection to the pointed entrypoint.
   *
   * @param entrypoint the entrypoint to connect to
   */
  connect(entrypoint: string): ng.IPromise<any> {
    let deferred = this.$q.defer();
    this.websocketStream = this.$websocket(entrypoint);

    this.websocketStream.onOpen(() => {
      const event: communicationClientEvent = 'open';
      if (this.handlers[event] && this.handlers[event].length > 0) {
        this.handlers[event].forEach((handler: Function) => handler() );
      }

      deferred.resolve();
    });
    this.websocketStream.onError(() => {
      const event: communicationClientEvent = 'error';
      if (!this.handlers[event] || this.handlers[event].length === 0) {
        return;
      }

      this.handlers[event].forEach((handler: Function) => handler() );

      deferred.reject();
    });
    this.websocketStream.onMessage((message: any) => {
      const data = JSON.parse(message.data);

      const event: communicationClientEvent = 'response';
      if (!this.handlers[event] || this.handlers[event].length === 0) {
        return;
      }

      this.handlers[event].forEach((handler: Function) => handler(data) );
    });
    this.websocketStream.onClose(() => {
      const event: communicationClientEvent = 'close';
      if (!this.handlers[event] || this.handlers[event].length === 0) {
        return;
      }

      this.handlers[event].forEach((handler: Function) => handler() );
    });

    return deferred.promise;
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
   * Adds a listener on an event.
   *
   * @param {communicationClientEvent} event
   * @param {Function} handler
   */
  addListener(event: communicationClientEvent, handler: Function): void {
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
  removeListener(event: communicationClientEvent, handler: Function): void {
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
   * Sends pointed data.
   *
   * @param data to be sent
   */
  send(data: any): void {
    this.websocketStream.send(data);
  }

  /**
   * Provides defered object.
   *
   * @returns {IDeferred<T>}
   */
  getDeferred(): ng.IDeferred<any> {
    return this.$q.defer();
  }
}
