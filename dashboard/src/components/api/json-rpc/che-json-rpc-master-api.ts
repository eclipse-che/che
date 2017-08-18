/*
 * Copyright (c) 2015-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
'use strict';
import {CheJsonRpcApiClient, IChannel} from './che-json-rpc-api-service';
import {ICommunicationClient} from './json-rpc-client';

enum MasterChannels {
  ENVIRONMENT_OUTPUT, ENVIRONMENT_STATUS, WS_AGENT_OUTPUT, WORKSPACE_STATUS
}

const websocketMasterApi: string = '/wsmaster/websocket';

/**
 * Client API for workspace master interactions.
 *
 * @author Ann Shumilova
 */
export class CheJsonRpcMasterApi {
  private cheJsonRpcApi: CheJsonRpcApiClient;
  private channels: Map<MasterChannels, IChannel>;
  private clientId: string;

  constructor (client: ICommunicationClient, entrypoint: string) {
    this.cheJsonRpcApi = new CheJsonRpcApiClient(client);

    this.channels = new Map<MasterChannels, IChannel>();
    this.channels.set(MasterChannels.ENVIRONMENT_OUTPUT, {
      subscription: 'event:environment-output:subscribe-by-machine-name',
      unsubscription: 'event:environment-output:un-subscribe-by-machine-name',
      notification: 'event:environment-output:message'
    });

    this.channels.set(MasterChannels.ENVIRONMENT_STATUS, {
      subscription: 'event:environment-status:subscribe',
      unsubscription: 'event:environment-status:un-subscribe',
      notification: 'event:environment-status:changed'
    });

    this.channels.set(MasterChannels.WS_AGENT_OUTPUT, {
      subscription: 'event:ws-agent-output:subscribe',
      unsubscription: 'event:ws-agent-output:un-subscribe',
      notification: 'event:ws-agent-output:message'
    });

    this.channels.set(MasterChannels.WORKSPACE_STATUS, {
      subscription: 'event:workspace-status:subscribe',
      unsubscription: 'event:workspace-status:un-subscribe',
      notification: 'event:workspace-status:changed'
    });

    this.connect(entrypoint);
  }

  /**
   * Opens connection to pointed entrypoint.
   *
   * @param entrypoint
   * @returns {IPromise<IHttpPromiseCallbackArg<any>>}
   */
  connect(entrypoint: string): ng.IPromise<any> {
    return this.cheJsonRpcApi.connect(entrypoint + websocketMasterApi).then(() => {
      return this.fetchClientId();
    });
  }

  /**
   * Subscribes the environment output.
   *
   * @param workspaceId workspace's id
   * @param machineName machine's name
   * @param callback callback to process event
   */
  subscribeEnvironmentOutput(workspaceId: string, machineName: string, callback: Function): void {
    let channel = this.channels.get(MasterChannels.ENVIRONMENT_OUTPUT);
    let params = [workspaceId + '::' + machineName];
    this.cheJsonRpcApi.subscribe(channel.subscription, channel.notification, callback, params);
  }

  /**
   * Un-subscribes the pointed callback from the environment output.
   *
   * @param workspaceId workspace's id
   * @param machineName machine's name
   * @param callback callback to process event
   */
  unSubscribeEnvironmentOutput(workspaceId: string, machineName: string, callback: Function): void {
    let channel = this.channels.get(MasterChannels.ENVIRONMENT_OUTPUT);
    let params = [workspaceId + '::' + machineName];
    this.cheJsonRpcApi.unsubscribe(channel.unsubscription, channel.notification, callback, params);
  }

  /**
   * Subscribes the environment status changed.
   *
   * @param workspaceId workspace's id
   * @param callback callback to process event
   */
  subscribeEnvironmentStatus(workspaceId: string, callback: Function): void {
    let channel = this.channels.get(MasterChannels.ENVIRONMENT_STATUS);
    let params = [workspaceId];
    this.cheJsonRpcApi.subscribe(channel.subscription, channel.notification, callback, params);
  }

  /**
   * Un-subscribes the pointed callback from environment status changed.
   *
   * @param workspaceId workspace's id
   * @param callback callback to process event
   */
  unSubscribeEnvironmentStatus(workspaceId: string, callback: Function): void {
    let channel = this.channels.get(MasterChannels.ENVIRONMENT_STATUS);
    let params = [workspaceId];
    this.cheJsonRpcApi.unsubscribe(channel.unsubscription, channel.notification, callback, params);
  }

  /**
   * Subscribes on workspace agent output.
   *
   * @param workspaceId workspace's id
   * @param callback callback to process event
   */
  subscribeWsAgentOutput(workspaceId: string, callback: Function): void {
    let channel = this.channels.get(MasterChannels.WS_AGENT_OUTPUT);
    let params = [workspaceId];
    this.cheJsonRpcApi.subscribe(channel.subscription, channel.notification, callback, params);
  }

  /**
   * Un-subscribes from workspace agent output.
   *
   * @param workspaceId workspace's id
   * @param callback callback to process event
   */
  unSubscribeWsAgentOutput(workspaceId: string, callback: Function): void {
    let channel = this.channels.get(MasterChannels.WS_AGENT_OUTPUT);
    let params = [workspaceId];
    this.cheJsonRpcApi.unsubscribe(channel.unsubscription, channel.notification, callback, params);
  }

  /**
   * Subscribes to workspace's status.
   *
   * @param workspaceId workspace's id
   * @param callback callback to process event
   */
  subscribeWorkspaceStatus(workspaceId: string, callback: Function): void {
    let channel = this.channels.get(MasterChannels.WORKSPACE_STATUS);
    let params = [workspaceId];
    let statusHandler = (message: any) => {
      if (workspaceId === message.workspaceId) {
        callback(message);
      }
    };
    this.cheJsonRpcApi.subscribe(channel.subscription, channel.notification, statusHandler, params);
  }

  /**
   * Un-subscribes pointed callback from workspace's status.
   *
   * @param workspaceId
   * @param callback
   */
  unSubscribeWorkspaceStatus(workspaceId: string, callback: Function): void {
    let channel = this.channels.get(MasterChannels.WORKSPACE_STATUS);
    let params = [workspaceId];
    this.cheJsonRpcApi.unsubscribe(channel.unsubscription, channel.notification, callback, params);
  }

  /**
   * Fetch client's id and strores it.
   *
   * @returns {IPromise<TResult>}
   */
  fetchClientId(): ng.IPromise<any> {
    return this.cheJsonRpcApi.request('websocketIdService/getId').then((data: any) => {
      this.clientId = data[0];
    });
  }

  /**
   * Returns client's id.
   *
   * @returns {string} clinet connection identifier
   */
  getClientId(): string {
    return this.clientId;
  }
}
