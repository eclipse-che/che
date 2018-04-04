/*
 * Copyright (c) 2016-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc.- initial API and implementation
 */

import {MessageBusSubscriber} from "../../spi/websocket/messagebus-subscriber";
import {ProcessTerminatedEventPromiseMessageBusSubscriber} from "./process-terminated-event-promise-subscriber";
import {JsonRpcBus} from "../../spi/websocket/json-rpc-bus";
import {ProcesLogOutputMessageBusSubscriber} from "./process-log-output-subscriber";
import {Log} from "../../spi/log/log";
/**
 * Handle a promise that will be resolved when process/command is finished.
 * If process has error, promise will be rejected
 * @author Florent Benoit
 */
export class ProcessAckPromiseMessageBusSubscriber implements MessageBusSubscriber {

    resolve : any;
    reject : any;
    promise: Promise<boolean>;
    private id : string;
    private processTerminatedEventPromiseMessageBusSubscriber : ProcessTerminatedEventPromiseMessageBusSubscriber;
    private asynchronous : boolean;
    private jsonRpcBus : JsonRpcBus;

    constructor(id: string, jsonRpcBus : JsonRpcBus, asynchronous : boolean, processTerminatedEventPromiseMessageBusSubscriber : ProcessTerminatedEventPromiseMessageBusSubscriber) {
        this.id = id;
        this.jsonRpcBus = jsonRpcBus;
        this.asynchronous = asynchronous;
        this.processTerminatedEventPromiseMessageBusSubscriber = processTerminatedEventPromiseMessageBusSubscriber;
        this.promise = new Promise<boolean>((resolve, reject) => {
            this.resolve = resolve;
            this.reject = reject;
        });
    }

    handleMessage(event: any) {
        if (event.id && event.id === this.id) {
            if (event.result) {
                this.processTerminatedEventPromiseMessageBusSubscriber.setPid(event.result.pid);
                // subscribe to websocket
                if (this.asynchronous) {
                    this.jsonRpcBus.subscribe(new ProcesLogOutputMessageBusSubscriber(event.result.pid));
                    this.jsonRpcBus.subscribe(this.processTerminatedEventPromiseMessageBusSubscriber);
                }
                this.resolve(true);
            } else if (event.error) {
                this.reject(event.error);
            }
        }
    }

}
