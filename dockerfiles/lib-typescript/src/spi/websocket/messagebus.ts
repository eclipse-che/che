/*
 * Copyright (c) 2016-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
import {MessageBuilder} from './messagebuilder';
import {MessageBusSubscriber} from './messagebus-subscriber';
import {Log} from "../log/log";
import {Websocket} from "./websocket";


/**
 * Allow to handle calls to the MessageBus running inside Eclipse Che by sending and receiving the messages.
 * It handles the UUID messages and subscribers that can subscribe/unsubscribe
 * @author Florent Benoit
 */
export class MessageBus {

    websocketConnection : any;
    heartbeatPeriod : number;
    subscribersByChannel : Map<string, Array<MessageBusSubscriber>>;
    keepAlive : any;
    delaySend: Array<string>;
    websocketClient : any;
    closed : boolean;
    websocket : Websocket;

    constructor(websocketClient : any, url: string, websocket: Websocket, resolve : any, reject : any) {

        this.websocketClient = websocketClient;
        this.websocket = websocket;

        this.closed = false;
        this.delaySend = [];

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
            resolve(true);
            this.websocketConnection = connection;
            // push all previous messages
            this.delaySend.forEach((subscribeOrder) => this.send(subscribeOrder));

            // remove them
            this.delaySend.length = 0;

            // init keep alive
            this.setKeepAlive();

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

        });

        // now connect
        websocketClient.connect(url);



        this.heartbeatPeriod = 3000;//1000 * 50; //ping each 50 seconds

        this.subscribersByChannel = new Map<string, Array<MessageBusSubscriber>>();

    }

    close() {
        this.closed = true;
        this.websocketConnection.close();
    }


    /**
     * Sets the keep alive interval, which sends
     * ping frame to server to keep connection alive.
     * */
    setKeepAlive() {
        this.keepAlive = setTimeout(this.ping, this.heartbeatPeriod, this);
    }

    /**
     * Sends ping frame to server.
     */
    ping(instance) {
        instance.send(new MessageBuilder().ping().build());
    }

    /**
     * Restart ping timer (cancel previous and start again).
     */
    restartPing () {
        if (this.keepAlive) {
            clearTimeout(this.keepAlive);
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
    subscribe(channel, callback) {
        // already subscribed ?
        var existingSubscribers = this.subscribersByChannel.get(channel);
        if (!existingSubscribers) {
            // register callback

            var subscribers = [];
            subscribers.push(callback);
            this.subscribersByChannel.set(channel, subscribers);

            var subscribeOrder = new MessageBuilder().subscribe(channel).build();
            // send subscribe order
            if (!this.websocketConnection) {
                this.delaySend.push(subscribeOrder);
            } else {
                this.send(subscribeOrder);
            }

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
    unsubscribe(channel) {
        // already subscribed ?
        var existingSubscribers = this.subscribersByChannel.get(channel);
        // unable to cancel if not existing channel
        if (!existingSubscribers) {
            return;
        }

        if (existingSubscribers.length > 1) {
            // only remove callback
            for(let i = 0; i < existingSubscribers.length; i++) {
                delete existingSubscribers[i];
            }
        } else {
            // only one element, remove and send server message
            this.subscribersByChannel.delete(channel);

            // send unsubscribe order
            this.send(new MessageBuilder().unsubscribe(channel).build());
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

        // get headers
        var headers = jsonMessage.headers;

        var channelHeader;
        // found channel headers
        for(let i = 0; i < headers.length; i++) {
            let header = headers[i];
            if ('x-everrest-websocket-channel' === header.name) {
                channelHeader = header;
                continue;
            }
        }


        if (channelHeader) {
            // message for a channel, look at current subscribers
            var subscribers : Array<MessageBusSubscriber> = this.subscribersByChannel.get(channelHeader.value);
            if (subscribers) {
                subscribers.forEach((subscriber : MessageBusSubscriber) => {

                    // Convert to JSON object if it's a JSON body
                    var data;
                    try {
                        data = JSON.parse(jsonMessage.body);
                    } catch (error) {
                        // keep raw data
                        data = jsonMessage.body;
                    }
                    subscriber.handleMessage(data);
                });
            }
        }

        // restart ping after received message
        this.restartPing();
    }

}
