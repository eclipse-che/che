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
package org.eclipse.che.api.deploy;

import org.eclipse.che.api.builder.BuilderAdminService;
import org.eclipse.che.api.builder.BuilderSelectionStrategy;
import org.eclipse.che.api.builder.BuilderService;
import org.eclipse.che.api.builder.LastInUseBuilderSelectionStrategy;
import org.eclipse.che.api.builder.internal.BuilderModule;
import org.eclipse.che.api.builder.internal.SlaveBuilderService;
import org.eclipse.che.api.runner.LastInUseRunnerSelectionStrategy;
import org.eclipse.che.api.runner.RunnerAdminService;
import org.eclipse.che.api.runner.RunnerSelectionStrategy;
import org.eclipse.che.api.runner.RunnerService;
import org.eclipse.che.api.runner.internal.RunnerModule;
import org.eclipse.che.api.runner.internal.SlaveRunnerService;

import org.eclipse.che.api.analytics.AnalyticsModule;
import org.eclipse.che.api.project.server.BaseProjectModule;
import org.eclipse.che.api.user.server.UserProfileService;
import org.eclipse.che.api.user.server.UserService;
import org.eclipse.che.api.workspace.server.WorkspaceService;

import org.eclipse.che.api.vfs.server.VirtualFileSystemModule;
import org.eclipse.che.everrest.CodenvyAsynchronousJobPool;

import org.eclipse.che.api.core.rest.CoreRestModule;
import org.eclipse.che.generator.archetype.ArchetypeGeneratorModule;
import org.eclipse.che.inject.DynaModule;
import org.eclipse.che.vfs.impl.fs.LocalFileSystemRegistryPlugin;
import org.eclipse.che.vfs.impl.fs.VirtualFileSystemFSModule;
import com.google.inject.AbstractModule;

import org.everrest.core.impl.async.AsynchronousJobPool;
import org.everrest.core.impl.async.AsynchronousJobService;
import org.everrest.guice.PathKey;

/** @author andrew00x */
@DynaModule
public class ApiModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(WorkspaceService.class);

        bind(LocalFileSystemRegistryPlugin.class);

        bind(BuilderSelectionStrategy.class).toInstance(new LastInUseBuilderSelectionStrategy());
        bind(BuilderService.class);
        bind(BuilderAdminService.class);
        bind(SlaveBuilderService.class);

        bind(RunnerSelectionStrategy.class).toInstance(new LastInUseRunnerSelectionStrategy());
        bind(RunnerService.class);
        bind(RunnerAdminService.class);
        bind(SlaveRunnerService.class);

        bind(UserService.class);
        bind(UserProfileService.class);

        bind(AsynchronousJobPool.class).to(CodenvyAsynchronousJobPool.class);
        bind(new PathKey<>(AsynchronousJobService.class, "/async/{ws-id}")).to(AsynchronousJobService.class);

        install(new ArchetypeGeneratorModule());

        install(new CoreRestModule());
        install(new AnalyticsModule());
        install(new BaseProjectModule());
        install(new BuilderModule());
        install(new RunnerModule());
        install(new VirtualFileSystemModule());
        install(new VirtualFileSystemFSModule());
    }
}
