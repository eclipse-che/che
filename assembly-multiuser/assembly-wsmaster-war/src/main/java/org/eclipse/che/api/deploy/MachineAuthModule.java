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
import org.eclipse.che.api.environment.server.MachineLinksInjector;
import org.eclipse.che.api.workspace.server.WorkspaceServiceLinksInjector;
import org.eclipse.che.commons.auth.token.HeaderRequestTokenExtractor;
import org.eclipse.che.commons.auth.token.RequestTokenExtractor;
import org.eclipse.che.inject.DynaModule;
import org.eclipse.che.multiuser.machine.authentication.server.AuthWsAgentHealthChecker;
import org.eclipse.che.multiuser.machine.authentication.server.MachineAuthLinksInjector;
import org.eclipse.che.multiuser.machine.authentication.server.MachineSessionInvalidator;
import org.eclipse.che.multiuser.machine.authentication.server.MachineTokenPermissionsFilter;
import org.eclipse.che.multiuser.machine.authentication.server.MachineTokenRegistry;
import org.eclipse.che.multiuser.machine.authentication.server.MachineTokenService;
import org.eclipse.che.multiuser.machine.authentication.server.WorkspaceServiceAuthLinksInjector;
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
    bind(MachineLinksInjector.class).to(MachineAuthLinksInjector.class);
    bind(org.eclipse.che.api.agent.server.WsAgentHealthChecker.class)
        .to(AuthWsAgentHealthChecker.class);
    bind(MachineTokenPermissionsFilter.class);
    bind(MachineTokenService.class);
    bind(MachineTokenRegistry.class);
    bind(MachineSessionInvalidator.class);
    bind(RequestTokenExtractor.class).to(HeaderRequestTokenExtractor.class);
    bind(WorkspaceServiceLinksInjector.class).to(WorkspaceServiceAuthLinksInjector.class);
    bind(org.eclipse.che.api.environment.server.MachineInstanceProvider.class)
        .to(org.eclipse.che.plugin.docker.machine.AuthMachineProviderImpl.class);
  }
}
