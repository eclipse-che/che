/*******************************************************************************
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.deploy;

import com.google.inject.AbstractModule;

import org.eclipse.che.api.user.server.TokenValidator;
import org.eclipse.che.inject.DynaModule;

import javax.sql.DataSource;

/**
 *
 * Single-user version Che specific bindings
 *
 * @author Max Shaposhnik (mshaposh@redhat.com)
 */
@DynaModule
public class CheWsMasterModule extends AbstractModule {
    @Override
    protected void configure() {

        bind(TokenValidator.class).to(org.eclipse.che.api.local.DummyTokenValidator.class);

        bind(org.eclipse.che.api.agent.server.WsAgentHealthChecker.class)
                .to(org.eclipse.che.api.agent.server.WsAgentHealthCheckerImpl.class);

        bind(org.eclipse.che.api.environment.server.MachineInstanceProvider.class)
                .to(org.eclipse.che.plugin.docker.machine.MachineProviderImpl.class);

        bind(DataSource.class).toProvider(org.eclipse.che.core.db.h2.H2DataSourceProvider.class);
        install(new org.eclipse.che.api.user.server.jpa.UserJpaModule());
        bind(org.eclipse.che.api.user.server.CheUserCreator.class);
    }
}
