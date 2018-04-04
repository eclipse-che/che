/*
 * Copyright (c) 2015-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
'use strict';
import {CheWebsocket} from '../../../components/api/che-websocket.factory';
import {DiagnosticCallback} from '../diagnostic-callback';

/**
 * Test for launching websocket connection to the workspace master
 * @author Florent Benoit
 */
export class DiagnosticsWebsocketWsMaster {

  static $inject = ['cheWebsocket', '$timeout'];

  /**
   * Websocket handling.
   */
  private cheWebsocket: CheWebsocket;

  /**
   * Timeout handling.
   */
  private $timeout: ng.ITimeoutService;

  /**
   * Default constructor
   */
  constructor(cheWebsocket: CheWebsocket, $timeout: ng.ITimeoutService) {
    this.cheWebsocket = cheWebsocket;
    this.$timeout = $timeout;
  }

  /**
   * Start the diagnostic and report all progress through the callback
   * @param {DiagnosticCallback} diagnosticCallback
   * @returns {ng.IPromise<any>} when test is finished
   */
  start(diagnosticCallback: DiagnosticCallback): ng.IPromise<any> {

    try {
      // define callback
      let callback = (message: any) => {
        if (!message) {
          diagnosticCallback.getMessageBus().unsubscribe('pong');
          diagnosticCallback.success('Websocket message received');
        }
      };

      // subscribe to the event
      diagnosticCallback.subscribeChannel('pong', callback);

      // default fallback if no answer in 5 seconds
      diagnosticCallback.delayError('No reply of websocket test after 5 seconds. Websocket is failing to connect to ' + this.cheWebsocket.wsUrl, 5000);

      // send the message
      diagnosticCallback.getMessageBus().ping();

    } catch (error) {
      diagnosticCallback.error('Unable to connect with websocket to ' + this.cheWebsocket.wsUrl + ': ' + error);
    }
    return diagnosticCallback.getPromise();
  }

}
