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

import {CheJsonRpcMasterApi} from './che-json-rpc-master-api';
import {WebsocketClient} from './websocket-client';

/**
 * This class manages the api connection through JSON RPC.
 *
 * @author Ann Shumilova
 */
export class CheJsonRpcApi {

  static $inject = ['$q', '$websocket', '$log', '$timeout'];

  private $q: ng.IQService;
  private $websocket: any;
  private $log: ng.ILogService;
  private jsonRpcApiConnection: Map<string, CheJsonRpcMasterApi>;

  private $timeout: ng.ITimeoutService;

  /**
   * Default constructor that is using resource
   */
  constructor($q: ng.IQService,
              $websocket: any,
              $log: ng.ILogService,
              $timeout: ng.ITimeoutService) {
    this.$q = $q;
    this.$websocket = $websocket;
    this.$log = $log;
    this.$timeout = $timeout;
    this.jsonRpcApiConnection = new Map<string, CheJsonRpcMasterApi>();
  }

  getJsonRpcMasterApi(entrypoint: string): CheJsonRpcMasterApi {
   if (this.jsonRpcApiConnection.has(entrypoint)) {
     return this.jsonRpcApiConnection.get(entrypoint);
   } else {
     let websocketClient = new WebsocketClient(this.$websocket, this.$q);
     let cheJsonRpcMasterApi: CheJsonRpcMasterApi = new CheJsonRpcMasterApi(websocketClient, entrypoint, this.$log, this.$timeout);
     this.jsonRpcApiConnection.set(entrypoint, cheJsonRpcMasterApi);
     return cheJsonRpcMasterApi;
   }
  }
}
