/*
 * Copyright (c) 2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */


import {MessageBusSubscriber} from "../../../spi/websocket/messagebus-subscriber";
import {MessageBus} from "../../../spi/websocket/messagebus";
import {Log} from "../../../spi/log/log";
/**
 * Handle a promise that will be resolved when process/command is finished.
 * If process has error, promise will be rejected
 * @author Florent Benoit
 */
export class ProcessTerminatedEventPromiseMessageBusSubscriber implements MessageBusSubscriber {

    messageBus : MessageBus;

    resolve : any;
    reject : any;
    promise: Promise<boolean>;

    constructor(messageBus : MessageBus) {
        this.messageBus = messageBus;
        this.promise = new Promise<boolean>((resolve, reject) => {
            this.resolve = resolve;
            this.reject = reject;
        });
    }

    handleMessage(message: any) {
        if ('STOPPED' === message.eventType) {
            this.resolve(true);
            this.messageBus.close();
        } else if ('ERROR' === message.eventType) {
            try {
                let stringify: any = JSON.stringify(message);
                this.reject('Error when executing the command' + stringify);
            } catch (error) {
                this.reject('Error when executing the command' + message.toString());
            }
            this.messageBus.close();
        } else {
            Log.getLogger().debug('Event on command : ', message.eventType);
        }

    }

}
