/*
 * Copyright (c) 2015-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
'use strict';
import {CheJsonRpcApiClient, IChannel} from './che-json-rpc-api-service';
import {ICommunicationClient} from './json-rpc-client';

enum WsAgentChannels {
  IMPORT_PROJECT
}

/**
 * Client API for workspace agent interactions.
 *
 * @author Ann Shumilova
 */
export class CheJsonRpcWsagentApi {
  private cheJsonRpcApi: CheJsonRpcApiClient;
  private channels: Map<WsAgentChannels, IChannel>;

  constructor (client: ICommunicationClient) {
    this.cheJsonRpcApi = new CheJsonRpcApiClient(client);

    this.channels = new Map<WsAgentChannels, IChannel>();
    this.channels.set(WsAgentChannels.IMPORT_PROJECT, {
      subscription: 'importProject/subscribe',
      unsubscription: 'importProject/unSubscribe',
      notification: 'importProject/progress/'
    });
  }

  /**
   * Connect pointed entrypoint with provided client's id
   *
   * @param entrypoint entrypoint to connect to
   * @param clientId client's connection identificator
   * @returns {ng.IPromise<any>}
   */
  connect(entrypoint: string, clientId: string): ng.IPromise<any> {
    let clientParam = 'clientId=' + clientId;
    if (/\?/.test(entrypoint) === false) {
      clientParam = '?' + clientParam;
    } else {
      clientParam = '&' + clientParam;
    }
    return this.cheJsonRpcApi.connect(entrypoint + clientParam);
  }

  /**
   * Subscribes on project's import output.
   *
   * @param projectName project name
   * @param callback callback to handle event
   */
  subscribeProjectImport(projectName: string, callback: Function): void {
    let channel = this.channels.get(WsAgentChannels.IMPORT_PROJECT);
    this.cheJsonRpcApi.subscribe(channel.subscription, channel.notification + projectName, callback);
  }

  /**
   * Un-subscribes the pointed callback from projects's import output
   *
   * @param projectName project's name
   * @param callback callback to be un-subscribed
   */
  unSubscribeProjectImport(projectName: string, callback: Function): void {
    let channel = this.channels.get(WsAgentChannels.IMPORT_PROJECT);
    this.cheJsonRpcApi.unsubscribe(channel.unsubscription, channel.notification + projectName, callback);
  }
}
