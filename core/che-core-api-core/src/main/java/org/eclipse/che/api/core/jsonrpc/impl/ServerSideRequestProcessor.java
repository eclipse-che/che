/*
 * Copyright (c) 2012-2019 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.core.jsonrpc.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.concurrent.ExecutorService;
import org.eclipse.che.api.core.jsonrpc.commons.RequestProcessor;
import org.eclipse.che.api.core.jsonrpc.commons.RequestProcessorConfigurationProvider;
import org.eclipse.che.api.core.jsonrpc.commons.RequestProcessorConfigurationProvider.Configuration;
import org.eclipse.che.commons.lang.concurrent.ThreadLocalPropagateContext;

@Singleton
public class ServerSideRequestProcessor implements RequestProcessor {
  private final RequestProcessorConfigurationProvider requestProcessorConfigurator;

  @Inject
  public ServerSideRequestProcessor(
      RequestProcessorConfigurationProvider requestProcessorConfigurator) {
    this.requestProcessorConfigurator = requestProcessorConfigurator;
  }

  @Override
  public void process(String endpointId, Runnable runnable) {
    Configuration configuration = requestProcessorConfigurator.get(endpointId);
    ExecutorService executionService = configuration.getExecutorService();
    executionService.execute(ThreadLocalPropagateContext.wrap(runnable));
  }
}
