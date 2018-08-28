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

import {org} from "../../dto/che-dto"
import {AuthData} from "../auth/auth-data";
import {Websocket} from "../../../spi/websocket/websocket";
import {HttpJsonRequest} from "../../../spi/http/default-http-json-request";
import {DefaultHttpJsonRequest} from "../../../spi/http/default-http-json-request";
import {HttpJsonResponse} from "../../../spi/http/default-http-json-request";
import {JsonRpcBus} from "../../../spi/websocket/json-rpc-bus";
import {SystemStopEventPromiseMessageBusSubscriber} from "./system-stop-event-promise-subscriber";

/**
 * System class allowing to get state of system and perform graceful stop, etc.
 * @author Florent Benoit
 */
export class System {

  /**
   * Authentication data
   */
  authData:AuthData;

  /**
   * websocket.
   */
  websocket:Websocket;

  constructor(authData:AuthData) {
    this.authData = authData;
    this.websocket = new Websocket();
  }

  /**
   * Get state of the system
   */
  getState():Promise<org.eclipse.che.api.system.shared.dto.SystemStateDto> {
    let jsonRequest: HttpJsonRequest = new DefaultHttpJsonRequest(this.authData, null, '/api/system/state', 200);
    return jsonRequest.request().then((jsonResponse:HttpJsonResponse) => {
      return jsonResponse.asDto(org.eclipse.che.api.system.shared.dto.SystemStateDtoImpl);
    });
  }

  /**
   * Get the message bus for the given System state DTO.
   * @param systemStateDto the current DTO
   * @returns {Promise<MessageBus>}
   */
  getMessageBus(systemStateDto : org.eclipse.che.api.system.shared.dto.SystemStateDto): Promise<JsonRpcBus> {

    // get link for websocket
    let websocketLink: string;
    systemStateDto.getLinks().forEach(stateLink => {
      if ('system.state.channel' === stateLink.getRel()) {
        websocketLink = stateLink.getHref();
      }
    });
    return this.websocket.getJsonRpcBus(websocketLink + '?token=' + this.authData.getToken());
  }


  /**
   * Stop the server and return a promise that will wait for the ready to shutdown event
   */
  gracefulStop():Promise<org.eclipse.che.api.system.shared.dto.SystemStateDto> {
    // get workspace DTO
    return this.getState().then((systemStateDto: org.eclipse.che.api.system.shared.dto.SystemStateDto) => {
      if ('READY_TO_SHUTDOWN' === systemStateDto.getStatus()) {
        return this.readyTobeShutdown(systemStateDto);
      } else if ('RUNNING' === systemStateDto.getStatus()) {
        // call stop and wait
        return this.shutdown(systemStateDto, true);
      } else {
        // only wait as stop has been called
        return this.shutdown(systemStateDto, false);
      }
    });
  }



  /**
   * In that case, system is already in a state ready to be shutdown so we do nothing
   * @returns {Promise<string>}
   */
  readyTobeShutdown(currentSystemStateDto: org.eclipse.che.api.system.shared.dto.SystemStateDto) : Promise<org.eclipse.che.api.system.shared.dto.SystemStateDto> {
    return Promise.resolve(currentSystemStateDto);
  }

  /**
   * We want or we're already in a shutdown process so we need to wait the end of the action
   * @param systemStateDto the current state
   * @param callStop if true, we need to call the stop action, else we only listen to the stop process event
   * @returns {Promise<string>}
   */
  shutdown(systemStateDto: org.eclipse.che.api.system.shared.dto.SystemStateDto, callStop: boolean) : Promise<org.eclipse.che.api.system.shared.dto.SystemStateDto> {
    let callbackSubscriber : SystemStopEventPromiseMessageBusSubscriber;
    return this.getMessageBus(systemStateDto).then((messageBus: JsonRpcBus) => {
      callbackSubscriber = new SystemStopEventPromiseMessageBusSubscriber(messageBus);
      let channelToListen : string;
      systemStateDto.getLinks().forEach(stateLink => {
        if ('system.state.channel' === stateLink.getRel()) {
          channelToListen = stateLink.getParameters()[0].getDefaultValue();
        }
      });
      return messageBus.subscribeAsync(channelToListen, callbackSubscriber);
    }).then((subscribed: string) => {
         // to give some time for subscription request to finish, since
         // it is no any response from the server for them
         return this.delay(2000);
    }).then(() => {
      if (callStop) {
        let jsonRequest: HttpJsonRequest = new DefaultHttpJsonRequest(this.authData, null, '/api/system/stop', 204).setMethod('POST');
        return jsonRequest.request().then((jsonResponse: HttpJsonResponse) => {
          return;
        }).then(() => {
          return callbackSubscriber.promise;
        }).then(() => {
          return this.getState();
        });
      } else {
        return callbackSubscriber.promise.then(() => {
          return this.getState();
        });
      }
    });
  }

  delay(ms: number) : Promise<void> {
    return new Promise<void>(function(resolve) {
        setTimeout(resolve, ms);
    });
  }

}
