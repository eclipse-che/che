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
package org.eclipse.che.plugin.docker.machine.proxy;

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
    Multibinder<String> proxySettingsEnvVars =
        Multibinder.newSetBinder(binder(), String.class, Names.named("machine.docker.machine_env"));
    proxySettingsEnvVars
        .addBinding()
        .toProvider(org.eclipse.che.plugin.docker.machine.proxy.HttpProxyEnvVariableProvider.class);
    proxySettingsEnvVars
        .addBinding()
        .toProvider(
            org.eclipse.che.plugin.docker.machine.proxy.HttpsProxyEnvVariableProvider.class);
    proxySettingsEnvVars
        .addBinding()
        .toProvider(org.eclipse.che.plugin.docker.machine.proxy.NoProxyEnvVariableProvider.class);
    bind(new TypeLiteral<Map<String, String>>() {})
        .annotatedWith(Names.named("che.docker.build_args"))
        .toProvider(DockerBuildArgsProvider.class);
  }
}
