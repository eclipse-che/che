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
import {Log} from "../../spi/log/log";
import {JsonRpcBus} from "../../spi/websocket/json-rpc-bus";
/**
 * Handle a promise that will be resolved when process/command is finished.
 * If process has error, promise will be rejected
 * @author Florent Benoit
 */
export class ProcessTerminatedEventPromiseMessageBusSubscriber implements MessageBusSubscriber {

    resolve : any;
    reject : any;
    promise: Promise<boolean>;
    private pid : number;
    private jsonRpcBus;

    constructor(jsonRpcBus : JsonRpcBus) {
        this.jsonRpcBus = jsonRpcBus;
        this.promise = new Promise<boolean>((resolve, reject) => {
            this.resolve = resolve;
            this.reject = reject;
        });
    }

    handleMessage(event: any) {
        if (this.pid) {
            if ('process_died' === event.method && event.params && event.params.pid === this.pid) {
                this.jsonRpcBus.close();
                this.resolve(true);
            }
        }
    }
    
    setPid(pid : number) {
        this.pid = pid;
    }

}
