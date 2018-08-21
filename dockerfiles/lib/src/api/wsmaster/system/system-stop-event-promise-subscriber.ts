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

import {org} from "../../../api/dto/che-dto"
import {MessageBusSubscriber} from "../../../spi/websocket/messagebus-subscriber";
import {JsonRpcBus} from "../../../spi/websocket/json-rpc-bus";
import {Log} from "../../../spi/log/log";
/**
 * Handle a promise that will be resolved when system is stopped.
 * If system has error, promise will be rejected
 * @author Florent Benoit
 */
export class SystemStopEventPromiseMessageBusSubscriber implements MessageBusSubscriber {

  /**
   * Bus used to collect events
   */
  messageBus : JsonRpcBus;

  /**
   * Resolve method to call when we're ready to shutdown the system.
   */
  resolve : any;

  /**
   * Reject method on the promise if we need to abort the current process.
   */
  reject : any;

  /**
   * The promise used to defer the resolution.
   */
  promise: Promise<string>;

  constructor(messageBus : JsonRpcBus) {
    this.messageBus = messageBus;
    this.promise = new Promise<string>((resolve, reject) => {
      this.resolve = resolve;
      this.reject = reject;
    });
  }

  handleMessage(message: any) {
    // Ready to shutdown, it means we have finished the graceful stop and we can resolve the promise
    if ('READY_TO_SHUTDOWN' === message.params.status) {
      this.resolve();
      this.messageBus.close();
    } else if ('ERROR' === message.params.status) {
      try {
        let stringify: any = JSON.stringify(message);
        this.reject('Error when stopping the system ' + stringify);
      } catch (error) {
        this.reject('Error when stopping the system ' + message.toString());
      }
      this.messageBus.close();
    } else {
      // Changing the status, displaying it to the user
      Log.getLogger().info("System going into status '" + message.params.status + "' (was '" + message.prevStatus + "')");
    }

  }

}
