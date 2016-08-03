/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.agent.server;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

import org.eclipse.che.api.agent.server.impl.AgentProviderImpl;
import org.eclipse.che.api.agent.server.impl.RemoteAgentRegistryUrlProviderImpl;
import org.eclipse.che.api.agent.server.impl.BaseAgentFactory;
import org.eclipse.che.api.agent.server.impl.RemoteAgentRegistryImpl;

/**
 * @author Anatolii Bazko
 */
public class AgentModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(AgentRegistryUrlProvider.class).to(RemoteAgentRegistryUrlProviderImpl.class);
        bind(AgentRegistry.class).to(RemoteAgentRegistryImpl.class);
        bind(AgentProvider.class).to(AgentProviderImpl.class);

        Multibinder<AgentFactory> agentFactoryMultibinder = Multibinder.newSetBinder(binder(), AgentFactory.class);
        agentFactoryMultibinder.addBinding().to(BaseAgentFactory.class);
    }
}
