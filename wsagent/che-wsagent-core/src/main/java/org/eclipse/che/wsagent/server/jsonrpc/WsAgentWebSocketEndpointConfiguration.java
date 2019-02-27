/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.wsagent.server.jsonrpc;

import java.util.concurrent.ExecutorService;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.jsonrpc.commons.RequestProcessorConfigurationProvider;

/**
 * {@link RequestProcessorConfigurationProvider.Configuration} implementation used to configure
 * {@link WsAgentWebSocketEndpoint}
 */
public class WsAgentWebSocketEndpointConfiguration
    implements RequestProcessorConfigurationProvider.Configuration {

  public static final String EXECUTOR_NAME = "che.core.jsonrpc.executor";

  private final ExecutorService executor;

  @Inject
  public WsAgentWebSocketEndpointConfiguration(@Named(EXECUTOR_NAME) ExecutorService executor) {
    this.executor = executor;
  }

  @Override
  public String getEndpointId() {
    return WsAgentWebSocketEndpoint.ENDPOINT_ID;
  }

  @Override
  public ExecutorService getExecutorService() {
    return executor;
  }
}
