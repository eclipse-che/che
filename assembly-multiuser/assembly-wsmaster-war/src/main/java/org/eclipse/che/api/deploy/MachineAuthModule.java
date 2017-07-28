/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.deploy;

import com.google.inject.AbstractModule;

import org.eclipse.che.api.environment.server.MachineLinksInjector;
import org.eclipse.che.api.workspace.server.WorkspaceServiceLinksInjector;
import org.eclipse.che.commons.auth.token.HeaderRequestTokenExtractor;
import org.eclipse.che.commons.auth.token.RequestTokenExtractor;
import org.eclipse.che.inject.DynaModule;
import org.eclipse.che.machine.authentication.server.MachineAuthLinksInjector;
import org.eclipse.che.machine.authentication.server.interceptor.InterceptorModule;

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
                .to(org.eclipse.che.machine.authentication.server.AuthWsAgentHealthChecker.class);
        bind(org.eclipse.che.machine.authentication.server.MachineTokenService.class);
        bind(org.eclipse.che.machine.authentication.server.MachineTokenRegistry.class);
        bind(RequestTokenExtractor.class).to(HeaderRequestTokenExtractor.class);
        bind(WorkspaceServiceLinksInjector.class).to(org.eclipse.che.machine.authentication.server.WorkspaceServiceAuthLinksInjector.class);
        bind(org.eclipse.che.api.environment.server.MachineInstanceProvider.class)
                .to(org.eclipse.che.plugin.docker.machine.AuthMachineProviderImpl.class);
    }
}
