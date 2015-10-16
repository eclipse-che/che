/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

import org.eclipse.che.plugin.internal.api.DtoBuilder;
import org.eclipse.che.plugin.internal.api.PluginConfiguration;
import org.eclipse.che.plugin.internal.api.PluginResolver;
import org.eclipse.che.plugin.internal.api.PluginInstaller;
import org.eclipse.che.plugin.internal.api.PluginManager;
import org.eclipse.che.plugin.internal.api.PluginRepository;
import org.eclipse.che.plugin.internal.builder.DtoBuilderImpl;
import org.eclipse.che.plugin.internal.installer.PluginInstallerImpl;
import org.eclipse.che.plugin.internal.manager.PluginManagerImpl;
import org.eclipse.che.plugin.internal.repository.PluginRepositoryImpl;
import org.eclipse.che.plugin.internal.resolver.MavenResolver;

/**
 * Defines the guice injection
 * @author Florent Benoit
 */
public class PluginGuiceModule extends AbstractModule {
    @Override
    protected void configure() {
        binder().bind(CustomExceptionMapper.class);

        binder().bind(PluginsService.class);

        binder().bind(PluginManager.class).to(PluginManagerImpl.class);
        binder().bind(DtoBuilder.class).to(DtoBuilderImpl.class);
        binder().bind(PluginRepository.class).to(PluginRepositoryImpl.class);
        binder().bind(PluginInstaller.class).to(PluginInstallerImpl.class);
        binder().bind(PluginConfiguration.class).to(ChePluginConfiguration.class);

        Multibinder<PluginResolver> pluginDownloaderMultibinder = Multibinder.newSetBinder(binder(), PluginResolver.class);
        pluginDownloaderMultibinder.addBinding().to(MavenResolver.class);

    }
}
