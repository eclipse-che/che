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
package org.eclipse.che.workspace.infrastructure.docker.config.proxy;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;

import java.util.Map;

/**
 * Module that injects components needed to run docker machines behind proxies.
 *
 * @author Mykola Morhun
 * @author Roman Iuvshyn
 */
public class DockerProxyModule extends AbstractModule {

    @Override
    protected void configure() {
        Multibinder<String> proxySettingsEnvVars = Multibinder.newSetBinder(binder(),
                                                                            String.class,
                                                                            Names.named("machine.docker.machine_env"));
        proxySettingsEnvVars.addBinding()
                            .toProvider(HttpProxyEnvVariableProvider.class);
        proxySettingsEnvVars.addBinding()
                            .toProvider(HttpsProxyEnvVariableProvider.class);
        proxySettingsEnvVars.addBinding()
                            .toProvider(NoProxyEnvVariableProvider.class);
    }

}
