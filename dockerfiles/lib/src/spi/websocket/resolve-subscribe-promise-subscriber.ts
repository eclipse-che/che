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

import {Log} from "../log/log";
import {MessageBusSubscriber} from "./messagebus-subscriber";
/**
 * Handle a promise that will be resolved when system is stopped.
 * If system has error, promise will be rejected
 * @author Florent Benoit
 */
export class ResolveSubscribePromiseSubscriber implements MessageBusSubscriber {

  resolve : any;
  reject : any;
  promise: Promise<string>;
  channel : string;

  constructor(channel : string) {
    this.channel = channel;
    this.promise = new Promise<string>((resolve, reject) => {
      this.resolve = resolve;
      this.reject = reject;
    });
  }

  handleMessage(message: any) {
    if (this.channel === message.channel) {
      this.resolve();
    } else if ('ERROR' === message.eventType) {
      try {
        let stringify: any = JSON.stringify(message);
        this.reject('Error when getting subscribe channel ' + stringify);
      } catch (error) {
        this.reject('Error when getting subscribe channel' + message.toString());
      }
    } else {
      this.reject('Error when getting subscribe channel ' + message.toString());
    }

  }

}
