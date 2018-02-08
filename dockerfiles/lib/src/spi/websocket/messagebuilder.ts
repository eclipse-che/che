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
import {UUID} from "../../utils/index";

/**
 * Generator of the messages to send with the {@link MessageBus} object.
 * @author Florent Benoit
 */
export class MessageBuilder {

  method: string;
  params: any;
  message: any;

  constructor(method? : string, params? : any) {
    if (method) {
      this.method = method;
    }
    if (params) {
       this.params = params;
    } else {
       this.params = {};
    }
    this.message = {};
    this.message.jsonrpc = '2.0'
    this.message.method = this.method;
    this.message.params = this.params;
  }

  subscribe(channel) {
    this.message.method = 'subscribe';
    this.message.params.method = channel;
    return this;
  }

  unsubscribe(channel) {
    this.message.method = 'unSubscribe';
    this.message.params.method = channel;
    return this;
  }

  /**
   * Prepares ping frame for server.
   *
   * @returns {MessageBuilder}
   */
  ping() {
    return this;
  }

  build() {
    return this.message;
  }

}
