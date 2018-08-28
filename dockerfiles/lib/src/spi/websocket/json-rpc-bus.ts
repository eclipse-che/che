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
import {MessageBusSubscriber} from './messagebus-subscriber';
import {MessageBuilder} from './messagebuilder';
import {Log} from "../log/log";
import {Websocket} from "./websocket";


/**
 * Allow to handle calls to the JSON RPC bus running inside Eclipse Che by sending and receiving the messages.
 * It handles the UUID messages and subscribers that can subscribe/unsubscribe
 * @author Florent Benoit
 */
export class JsonRpcBus {

    websocketConnection : any;
    subscribers : Array<MessageBusSubscriber>;
    websocketClient : any;
    closed : boolean;
    websocket : Websocket;

    constructor(websocketClient : any, url: string, websocket: Websocket, resolve : any, reject : any) {

        this.websocketClient = websocketClient;
        this.websocket = websocket;

        this.closed = false;

        var client = websocketClient.on('connectFailed', (error) => {
            Log.getLogger().error('Connect Error: ' + error.toString());
            reject(error);
        });


        client.on('error', error => {
            Log.getLogger().error('websocketclient error', error.toString());
            reject(error);
        });

        client.on('connect', (connection) => {
            // resolve the promise of connecting to the bus
            this.websocketConnection = connection;

            connection.on('error', (error) => {
                if (!this.closed) {
                    Log.getLogger().error("Connection Error: " + error.toString());
                }
            });
            connection.on('close', () => {
                if (!this.closed) {
                    Log.getLogger().error('Websocket connection closed');
                }
            });
            connection.on('message', (message) => {
                if (message.type === 'utf8') {
                    this.handleMessage(message.utf8Data);
                }
            });
            resolve(this);

        });

        // now connect
        websocketClient.connect(url);
        this.subscribers = new Array<MessageBusSubscriber>();

    }

    close() {
        this.closed = true;
        this.websocketConnection.close();
    }

    /**
     * Subscribes a new callback which will listener for messages sent to the specified channel.
     * Upon the first subscribe to a channel, a message is sent to the server to
     * subscribe the client for that channel. Subsequent subscribes for a channel
     * already previously subscribed to do not trigger a send of another message
     * to the server because the client has already a subscription, and merely registers
     * (client side) the additional handler to be fired for events received on the respective channel.
     */
    subscribe(callback) {
        // already subscribed ?
        var existingSubscriberIndex = this.subscribers.indexOf(callback);
        if (existingSubscriberIndex === -1) {
            // register callback
            this.subscribers.push(callback);
        }
    }

    /**
     * Asynchronous subscribe method that is returning  promise that will be resolved when subscribe callback has been sent from the remote side
     * @param channel the channel on which we want to subscribe
     * @param callback the callback subscriber
     * @returns {any} promise
     */
    subscribeAsync(channel, callback) : Promise<string> {
        // already subscribed ?
        var existingSubscriberIndex = this.subscribers.indexOf(callback);
        if (existingSubscriberIndex === -1) {
            // register callback
            this.subscribers.push(callback);
            var subscribeOrder = new MessageBuilder().subscribe(channel).build();
            this.send(subscribeOrder);
          }
        return Promise.resolve("true");
    }

    /**
     * Unsubscribes a previously subscribed handler listening on the specified channel.
     * If it's the last unsubscribe to a channel, a message is sent to the server to
     * unsubscribe the client for that channel.
     */
    unsubscribe(callback) {
        // already subscribed ?
        var existingSubscriberIndex: number = this.subscribers.indexOf(callback);
        if (existingSubscriberIndex > 0) {
            delete this.subscribers[existingSubscriberIndex];
        }
    }

    send(message) {
        var stringified = JSON.stringify(message);
        this.websocketConnection.sendUTF(stringified);
    }

    handleMessage(message) {
        // handling the receive of a message
        // needs to parse it
        var jsonMessage = JSON.parse(message);
        this.processMessage(jsonMessage);
    }

    /**
     * Process the JSON given message by using the given channelHeader value
     * @param jsonMessage the message to parse and then send to subscribers
     */
    processMessage(jsonMessage : any) {
        // message for a channel, look at current subscribers
        this.subscribers.forEach((subscriber : MessageBusSubscriber) => {
            subscriber.handleMessage(jsonMessage);
        });
    }

}
