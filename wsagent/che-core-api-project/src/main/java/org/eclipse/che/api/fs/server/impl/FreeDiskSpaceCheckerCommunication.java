/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.fs.server.impl;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.jsonrpc.commons.ClientSubscriptionHandler;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;

/**
 * Communication channel between free disk space checker and clients.
 *
 * @author Vlad Zhukovskyi
 * @since 6.9.0
 * @see FreeDiskSpaceChecker
 */
@Singleton
public class FreeDiskSpaceCheckerCommunication {

  private static final String JSON_RPC_METHOD_NAME = "workspace/lowDiskSpace";

  private RequestTransmitter transmitter;
  private ClientSubscriptionHandler clientSubscriptionHandler;

  @Inject
  public FreeDiskSpaceCheckerCommunication(
      RequestTransmitter transmitter, ClientSubscriptionHandler clientSubscriptionHandler) {
    this.transmitter = transmitter;
    this.clientSubscriptionHandler = clientSubscriptionHandler;
  }

  public void broadcastLowDiskSpaceMessage() {
    clientSubscriptionHandler
        .getEndpointIds()
        .forEach(
            it ->
                transmitter
                    .newRequest()
                    .endpointId(it)
                    .methodName(JSON_RPC_METHOD_NAME)
                    .noParams()
                    .sendAndSkipResult());
  }
}
