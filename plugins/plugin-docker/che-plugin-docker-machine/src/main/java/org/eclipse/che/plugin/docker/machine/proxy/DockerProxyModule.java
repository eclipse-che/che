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
package org.eclipse.che.plugin.docker.machine.proxy;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;

import org.eclipse.che.plugin.docker.machine.DockerInstanceProvider;

/**
 * Module for components that are needed for deal with proxy in {@link DockerInstanceProvider}
 *
 * @author Mykola Morhun
 */
public class DockerProxyModule extends AbstractModule {

    @Override
    protected void configure() {
        Multibinder<String> proxySettingsEnvVars = Multibinder.newSetBinder(binder(),
                                                                            String.class,
                                                                            Names.named("machine.docker.machine_env"));
        proxySettingsEnvVars.addBinding()
                            .toProvider(org.eclipse.che.plugin.docker.machine.proxy.HttpProxyEnvVariableProvider.class);
        proxySettingsEnvVars.addBinding()
                            .toProvider(org.eclipse.che.plugin.docker.machine.proxy.HttpsProxyEnvVariableProvider.class);
    }

}
