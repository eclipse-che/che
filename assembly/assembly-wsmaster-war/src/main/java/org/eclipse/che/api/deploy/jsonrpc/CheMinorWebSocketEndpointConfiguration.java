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
package org.eclipse.che.api.deploy.jsonrpc;

import java.util.concurrent.ExecutorService;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.jsonrpc.commons.RequestProcessorConfigurationProvider;

/**
 * {@link RequestProcessorConfigurationProvider.Configuration} implementation used to configure
 * {@link CheMinorWebSocketEndpoint}
 */
public class CheMinorWebSocketEndpointConfiguration
    implements RequestProcessorConfigurationProvider.Configuration {

  private final ExecutorService executor;

  public static final String EXECUTOR_NAME = "che.core.jsonrpc.minor_executor";

  @Inject
  public CheMinorWebSocketEndpointConfiguration(@Named(EXECUTOR_NAME) ExecutorService executor) {
    this.executor = executor;
  }

  @Override
  public String getEndpointId() {
    return CheMinorWebSocketEndpoint.ENDPOINT_ID;
  }

  @Override
  public ExecutorService getExecutorService() {
    return executor;
  }
}
