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
package org.eclipse.che.wsagent.server;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import java.net.URI;
import org.eclipse.che.MachineTokenProvider;
import org.eclipse.che.UriApiEndpointProvider;
import org.eclipse.che.api.core.cors.CheWsAgentCorsAllowCredentialsProvider;
import org.eclipse.che.api.core.cors.CheWsAgentCorsAllowedOriginsProvider;
import org.eclipse.che.inject.DynaModule;
import org.eclipse.che.wsagent.server.appstate.AppStateService;

/**
 * Configuration of Che ws agent core part that can be different in different assembly.
 *
 * @author Sergii Kabashniuk
 */
@DynaModule
public class CheWsAgentModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(URI.class).annotatedWith(Names.named("che.api")).toProvider(UriApiEndpointProvider.class);
    bind(String.class)
        .annotatedWith(Names.named("machine.token"))
        .toProvider(MachineTokenProvider.class);

    bind(String.class)
        .annotatedWith(Names.named("wsagent.endpoint"))
        .toProvider(WsAgentURLProvider.class);

    bind(String.class)
        .annotatedWith(Names.named("che.cors.allowed_origins"))
        .toProvider(CheWsAgentCorsAllowedOriginsProvider.class);

    bind(Boolean.class)
        .annotatedWith(Names.named("che.cors.allow_credentials"))
        .toProvider(CheWsAgentCorsAllowCredentialsProvider.class);

    bind(AppStateService.class);
  }
}
