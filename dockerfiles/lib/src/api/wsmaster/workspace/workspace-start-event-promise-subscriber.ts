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
import {Log} from "../../../spi/log/log";
import {JsonRpcBus} from "../../../spi/websocket/json-rpc-bus";
/**
 * Handle a promise that will be resolved when workspace is started.
 * If workspace has error, promise will be rejected
 * @author Florent Benoit
 */
export class WorkspaceStartEventPromiseMessageBusSubscriber implements MessageBusSubscriber {

  workspaceDto : org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;

  resolve : any;
  reject : any;
  promise: Promise<string>;
  jsonRpcBus: JsonRpcBus;

  constructor(jsonRpcBus : JsonRpcBus, workspaceDto : org.eclipse.che.api.workspace.shared.dto.WorkspaceDto) {
    this.jsonRpcBus = jsonRpcBus;
    this.workspaceDto = workspaceDto;
    this.promise = new Promise<string>((resolve, reject) => {
      this.resolve = resolve;
      this.reject = reject;
    });
  }
  handleMessage(message: any) {
    if (message.error) {
      Log.getLogger().error('Error on workspace : ', message);
      try {
        let stringify: any = JSON.stringify(message);
        this.reject('Error when starting the workspace' + stringify);
      } catch (error) {
        this.reject('Error when starting the workspace' + message.toString());
      }

    }

    if (message.method === 'workspace/statusChanged' && message.params.workspaceId === this.workspaceDto.getId()) {
      if ('RUNNING' === message.params.status) {
        this.jsonRpcBus.close();
        this.resolve(this.workspaceDto);
      } else if ('ERROR' === message.params.status) {
        try {
          let stringify: any = JSON.stringify(message);
          this.reject('Error when starting the workspace' + stringify);
        } catch (error) {
          this.reject('Error when starting the workspace' + message.toString());
        }
        this.jsonRpcBus.close();
      }
    } else {
      Log.getLogger().debug('Event on workspace : ', message);
    }

  }

}
