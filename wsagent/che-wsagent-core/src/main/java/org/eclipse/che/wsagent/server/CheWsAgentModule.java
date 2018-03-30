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
package org.eclipse.che.wsagent.server;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import java.net.URI;
import java.security.PublicKey;
import org.eclipse.che.MachinePublicKeyProvider;
import org.eclipse.che.MachineTokenProvider;
import org.eclipse.che.UriApiEndpointProvider;
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

    bind(WsAgentAnalyticsAddresser.class);

    bind(String.class)
        .annotatedWith(Names.named("wsagent.endpoint"))
        .toProvider(WsAgentURLProvider.class);

    bind(PublicKey.class)
        .annotatedWith(Names.named("signature.public.key"))
        .toProvider(MachinePublicKeyProvider.class);

    bind(AppStateService.class);
  }
}
