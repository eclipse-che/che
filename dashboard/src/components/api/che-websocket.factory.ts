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

/* exported MessageBus */

/**
 * This class is handling the Websocket exchange
 * @author Florent Benoit
 */
export class CheWebsocket {

  static $inject = ['$websocket', '$location', '$interval', 'proxySettings', 'userDashboardConfig', 'keycloakAuth'];

  private bus : MessageBus;
  private wsBaseUrl : string;
  private remoteBus : MessageBus;
  private $interval : ng.IIntervalService;
  private $websocket : any;

  /**
   * Default constructor that is using resource
   */
  constructor ($websocket: any,
               $location: ng.ILocationService,
               $interval: ng.IIntervalService,
               proxySettings: string,
               userDashboardConfig: any,
               keycloakAuth: any) {

    this.$websocket = $websocket;
    this.$interval = $interval;

    let wsUrl;
    let inDevMode = userDashboardConfig.developmentMode;

    if (inDevMode) {
      // it handle then http and https
      wsUrl = proxySettings.replace('http', 'ws') + '/api/ws';
    } else {

      let wsProtocol;
      if ('http' === $location.protocol()) {
        wsProtocol = 'ws';
      } else {
        wsProtocol = 'wss';
      }

      wsUrl = wsProtocol + '://' + $location.host() + ':' + $location.port() + '/api/ws';
    }
    let keycloakToken = keycloakAuth.isPresent ? '?token=' + keycloakAuth.keycloak.token : '';
    this.wsBaseUrl = wsUrl + keycloakToken;
    this.bus = null;
    this.remoteBus = null;
  }

  get wsUrl(): string {
    return this.wsBaseUrl;
  }

  getExistingBus(datastream: any): MessageBus {
    return new MessageBus(datastream, this.$interval);
  }

  getBus() : MessageBus {
    if (!this.bus) {
      // needs to initialize
      this.bus = new MessageBus(this.$websocket(this.wsBaseUrl), this.$interval);
      this.bus.onClose(
        () => {
          // remove it from the cache so new calls will create a new instance
          this.bus.closed = true;
          this.bus = null;
        }
      );
    }
    return this.bus;
  }

  /**
   * Gets a bus for a remote workspace, by providing the remote URL to this websocket
   * @param {string} websocketURL the remote host base WS url
   */
  getRemoteBus(websocketURL: string): MessageBus {
    if (!this.remoteBus) {
      // needs to initialize
      this.remoteBus = new MessageBus(this.$websocket(websocketURL), this.$interval);
      this.remoteBus.onClose(
        () => {
          // remove it from the cache so new calls will create a new instance
          this.remoteBus.closed = true;
          this.remoteBus = null;
        }
      );
    }
    return this.remoteBus;
  }

}


class MessageBuilder {

  private static TYPE : string = 'x-everrest-websocket-message-type';
  private method : string;
  private path : string;
  private message : any;

  constructor(method? : string, path? : string) {
    if (method) {
      this.method = method;
    } else {
      this.method = 'POST';
    }
    if (path) {
      this.path = path;
    } else {
      this.path = null;
    }


    this.message = {};
    // add uuid
    this.message.uuid = this.buildUUID();

    this.message.method = this.method;
    this.message.path = this.path;
    this.message.headers = [];
    this.message.body = '';
  }

  subscribe(channel: any): MessageBuilder {
    let header = {name: MessageBuilder.TYPE, value: 'subscribe-channel'};
    this.message.headers.push(header);
    this.message.body = JSON.stringify({channel: channel});
    return this;
  }

  unsubscribe(channel: any): MessageBuilder {
    let header = {name: MessageBuilder.TYPE, value: 'unsubscribe-channel'};
    this.message.headers.push(header);
    this.message.body = JSON.stringify({channel: channel});
    return this;
  }

  /**
   * Prepares ping frame for server.
   *
   * @returns {MessageBuilder}
   */
  ping(): MessageBuilder {
    let header = {name: MessageBuilder.TYPE, value: 'ping'};
    this.message.headers.push(header);
    return this;
  }

  build(): any {
    return this.message;
  }

  buildUUID(): string {
    /* tslint:disable */
    let time = new Date().getTime();
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (match: string) => {
      let rem = (time + 16 * Math.random()) % 16 | 0; // jshint ignore:line
      time = Math.floor(time / 16);
      return (match === 'x' ? rem : rem & 7 | 8).toString(16); // jshint ignore:line
    });
    /* tslint:enable */
  }

}

export class MessageBus {

  closed: boolean;
  datastream: any;
  private $interval: ng.IIntervalService;
  private heartbeatPeriod: number;
  private subscribersByChannel: Map<string, any>;
  private keepAlive : ng.IPromise<any>;

  constructor(datastream: any,
              $interval: ng.IIntervalService) {
    this.datastream = datastream;
    this.$interval = $interval;

    this.heartbeatPeriod = 1000 * 50; // ping each 50 seconds

    this.subscribersByChannel = new Map();

    this.setKeepAlive();
    this.datastream.onMessage((message: any) => { this.handleMessage(message); });
  }

  public isClosed() : boolean {
    return this.closed;
  }

  /**
   * Sets the keep alive interval, which sends
   * ping frame to server to keep connection alive.
   */
  setKeepAlive(): void {
    this.keepAlive = this.$interval(() => {
      this.ping();
    }, this.heartbeatPeriod);
  }

  /**
   * Sends ping frame to server.
   */
  ping(): void {
    this.send(new MessageBuilder().ping().build());
  }

  /**
   * Handles websocket closed event.
   *
   * @param callback
   */
  onClose(callback: Function): void {
    this.datastream.onClose(callback);
  }

  /**
   * Handles websocket error event.
   *
   * @param callback
   */
  onError(callback: Function): void {
    this.datastream.onError(callback);
  }

  /**
   * Restart ping timer (cancel previous and start again).
   */
  restartPing(): void {
    if (this.keepAlive) {
      this.$interval.cancel(this.keepAlive);
    }

    this.setKeepAlive();
  }

  /**
   * Subscribes a new callback which will listener for messages sent to the specified channel.
   * Upon the first subscribe to a channel, a message is sent to the server to
   * subscribe the client for that channel. Subsequent subscribes for a channel
   * already previously subscribed to do not trigger a send of another message
   * to the server because the client has already a subscription, and merely registers
   * (client side) the additional handler to be fired for events received on the respective channel.
   */
  subscribe(channel: any, callback: Function): void {
    // already subscribed ?
    let existingSubscribers = this.subscribersByChannel.get(channel);
    if (!existingSubscribers) {
      // register callback

      let subscribers = [];
      subscribers.push(callback);
      this.subscribersByChannel.set(channel, subscribers);

      // send subscribe order
      this.send(new MessageBuilder().subscribe(channel).build());
    } else {
      // existing there, add only callback
      existingSubscribers.push(callback);
    }
  }



  /**
   * Unsubscribes a previously subscribed handler listening on the specified channel.
   * If it's the last unsubscribe to a channel, a message is sent to the server to
   * unsubscribe the client for that channel.
   */
  unsubscribe(channel: any): void {
    // already subscribed ?
    let existingSubscribers = this.subscribersByChannel.get(channel);
    // unable to cancel if not existing channel
    if (!existingSubscribers) {
      return;
    }

    if (existingSubscribers > 1) {
      // only remove callback
      for (let i = 0; i < existingSubscribers.length; i++) {
        delete existingSubscribers[i];
      }
    } else {
      // only one element, remove and send server message
      this.subscribersByChannel.delete(channel);

      // send unsubscribe order
      this.send(new MessageBuilder().unsubscribe(channel).build());
    }
  }

  send(message: any): void {
    let stringified = JSON.stringify(message);
    this.datastream.send(stringified);
  }

  handleMessage(message: any): void {
    // handling the receive of a message
    // needs to parse it
    let jsonMessage = JSON.parse(message.data);

    // get headers
    let headers = jsonMessage.headers;


    let channelHeader;
    // found channel headers
    for (let i = 0; i < headers.length; i++) {
      let header = headers[i];
      if ('x-everrest-websocket-channel' === header.name) {
        channelHeader = header;
      }
    }

    // handle case when we don't have channel but a raw message
    if (!channelHeader && headers.length === 1 && headers[0].name === 'x-everrest-websocket-message-type') {
      channelHeader = headers[0];
    }

    if (channelHeader) {
      // message for a channel, look at current subscribers
      let subscribers = this.subscribersByChannel.get(channelHeader.value);
      if (subscribers) {
        subscribers.forEach((subscriber: any) => {
          try {
            subscriber(angular.fromJson(jsonMessage.body));
          } catch (e) {
            subscriber(jsonMessage.body);
          }
        });
      }
    }

    // restart ping after received message
    this.restartPing();
  }

}

