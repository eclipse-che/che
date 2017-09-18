/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.deploy;

import com.google.inject.AbstractModule;
import org.eclipse.che.api.workspace.server.WorkspaceServiceLinksInjector;
import org.eclipse.che.commons.auth.token.ChainedTokenExtractor;
import org.eclipse.che.commons.auth.token.RequestTokenExtractor;
import org.eclipse.che.inject.DynaModule;
import org.eclipse.che.multiuser.machine.authentication.server.interceptor.InterceptorModule;

/**
 * Machine authentification bindings.
 *
 * @author Max Shaposhnik (mshaposh@redhat.com)
 */
@DynaModule
public class MachineAuthModule extends AbstractModule {

  @Override
  protected void configure() {
    install(new InterceptorModule());
    bind(org.eclipse.che.api.agent.server.WsAgentHealthChecker.class)
        .to(org.eclipse.che.multiuser.machine.authentication.server.AuthWsAgentHealthChecker.class);
    bind(
        org.eclipse.che.multiuser.machine.authentication.server.MachineTokenPermissionsFilter
            .class);
    bind(org.eclipse.che.multiuser.machine.authentication.server.MachineTokenService.class);
    bind(org.eclipse.che.multiuser.machine.authentication.server.MachineTokenRegistry.class);
    bind(org.eclipse.che.multiuser.machine.authentication.server.MachineSessionInvalidator.class);
    bind(RequestTokenExtractor.class).to(ChainedTokenExtractor.class);
    bind(WorkspaceServiceLinksInjector.class)
        .to(
            org.eclipse.che.multiuser.machine.authentication.server
                .WorkspaceServiceAuthLinksInjector.class);
    bind(org.eclipse.che.api.environment.server.MachineInstanceProvider.class)
        .to(org.eclipse.che.plugin.docker.machine.AuthMachineProviderImpl.class);
  }
}
