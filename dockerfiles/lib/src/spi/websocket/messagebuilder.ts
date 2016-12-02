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
import {UUID} from "../../utils/index";

/**
 * Generator of the messages to send with the {@link MessageBus} object.
 * @author Florent Benoit
 */
export class MessageBuilder {

    method: string;
    path: string;
    TYPE: string;
    message : any;

    constructor(method? : string, path? : string) {
        this.TYPE = 'x-everrest-websocket-message-type';
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
        this.message.uuid = UUID.build();

        this.message.method = this.method;
        this.message.path = this.path;
        this.message.headers = [];
        this.message.body;
    }

    subscribe(channel) {
        var header = {name: this.TYPE, value: 'subscribe-channel'};
        this.message.headers.push(header);
        this.message.body = JSON.stringify({channel: channel});
        return this;
    }

    unsubscribe(channel) {
        var header = {name:this.TYPE, value: 'unsubscribe-channel'};
        this.message.headers.push(header);
        this.message.body = JSON.stringify({channel: channel});
        return this;
    }

    /**
     * Prepares ping frame for server.
     *
     * @returns {MessageBuilder}
     */
    ping() {
        var header = {name:this.TYPE, value: 'ping'};
        this.message.headers.push(header);
        return this;
    }

    build() {
        return this.message;
    }

}
