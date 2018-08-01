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
import java.security.PublicKey;
import org.eclipse.che.MachinePublicKeyProvider;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.commons.auth.token.ChainedTokenExtractor;
import org.eclipse.che.commons.auth.token.RequestTokenExtractor;
import org.eclipse.che.inject.DynaModule;

/** Provide multi user specific implementation of ws-agent components. */
@DynaModule
public class WsAgentAuthModule extends AbstractModule {
  @Override
  protected void configure() {
    if (Boolean.valueOf(System.getenv("CHE_AUTH_ENABLED"))) {
      configureMultiUserMode();
    }
  }

  private void configureMultiUserMode() {
    bind(HttpJsonRequestFactory.class).to(AgentHttpJsonRequestFactory.class);
    bind(RequestTokenExtractor.class).to(ChainedTokenExtractor.class);
    bind(PublicKey.class)
        .annotatedWith(Names.named("signature.public.key"))
        .toProvider(MachinePublicKeyProvider.class);
  }
}
