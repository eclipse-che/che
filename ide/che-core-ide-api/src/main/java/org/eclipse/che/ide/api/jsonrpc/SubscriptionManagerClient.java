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
package org.eclipse.che.ide.api.jsonrpc;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Map;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.api.core.notification.dto.EventSubscription;
import org.eclipse.che.ide.dto.DtoFactory;

@Singleton
public class SubscriptionManagerClient {
  private final RequestTransmitter requestTransmitter;
  private final DtoFactory dtoFactory;

  @Inject
  SubscriptionManagerClient(RequestTransmitter requestTransmitter, DtoFactory dtoFactory) {
    this.requestTransmitter = requestTransmitter;
    this.dtoFactory = dtoFactory;
  }

  public void subscribe(String endpointId, String method, Map<String, String> scope) {
    requestTransmitter
        .newRequest()
        .endpointId(endpointId)
        .methodName("subscribe")
        .paramsAsDto(
            dtoFactory.createDto(EventSubscription.class).withMethod(method).withScope(scope))
        .sendAndSkipResult();
  }

  public void unSubscribe(String endpointId, String method, Map<String, String> scope) {
    requestTransmitter
        .newRequest()
        .endpointId(endpointId)
        .methodName("unSubscribe")
        .paramsAsDto(
            dtoFactory.createDto(EventSubscription.class).withMethod(method).withScope(scope))
        .sendAndSkipResult();
  }
}
