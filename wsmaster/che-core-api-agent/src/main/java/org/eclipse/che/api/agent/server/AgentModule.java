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
import com.google.inject.name.Names;

import org.eclipse.che.api.agent.server.impl.AgentProviderImpl;
import org.eclipse.che.api.agent.server.impl.AgentRegistryImpl;

import static java.lang.String.format;

/**
 * @author Anatolii Bazko
 */
public class AgentModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(AgentRegistry.class).to(AgentRegistryImpl.class);
        bind(AgentProvider.class).to(AgentProviderImpl.class);

        bindConstant().annotatedWith(Names.named("che.agent.url"))
                      .to(format("https://codenvy.com/update/repository/public/download/%s/%s",
                                 AgentRegistryImpl.FQN_TEMPLATE, AgentRegistryImpl.VERSION_TEMPLATE));

        bindConstant().annotatedWith(Names.named("che.agent.latest.url"))
                      .to(format("https://codenvy.com/update/repository/public/download/%s", AgentRegistryImpl.FQN_TEMPLATE));
    }
}
