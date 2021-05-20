/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
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

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.multibindings.Multibinder;
import org.eclipse.che.api.core.jsonrpc.commons.RequestProcessorConfigurationProvider;

/** Configures JSON RPC WebSocket Endpoints. */
public class CheJsonRpcWebSocketConfigurationModule implements Module {
  @Override
  public void configure(Binder binder) {

    Multibinder<RequestProcessorConfigurationProvider.Configuration> configurationMultibinder =
        Multibinder.newSetBinder(binder, RequestProcessorConfigurationProvider.Configuration.class);
    configurationMultibinder.addBinding().to(CheMajorWebSocketEndpointConfiguration.class);
  }
}
