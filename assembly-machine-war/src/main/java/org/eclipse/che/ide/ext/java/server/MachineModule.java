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
package org.eclipse.che.ide.ext.java.server;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

import org.eclipse.che.api.auth.AuthenticationService;
import org.eclipse.che.api.auth.oauth.OAuthTokenProvider;
import org.eclipse.che.api.core.notification.WSocketEventBusServer;
import org.eclipse.che.api.core.rest.ApiInfoService;
import org.eclipse.che.api.core.rest.CoreRestModule;
import org.eclipse.che.api.machine.server.command.CommandService;
import org.eclipse.che.api.machine.server.recipe.PermissionsChecker;
import org.eclipse.che.api.machine.server.recipe.PermissionsCheckerImpl;
import org.eclipse.che.api.machine.server.recipe.RecipeService;
import org.eclipse.che.api.project.server.BaseProjectModule;
import org.eclipse.che.api.project.server.handlers.ProjectHandler;
import org.eclipse.che.api.user.server.UserProfileService;
import org.eclipse.che.api.user.server.UserService;
import org.eclipse.che.api.vfs.server.VirtualFileSystemModule;
import org.eclipse.che.api.vfs.server.VirtualFileSystemRegistry;
import org.eclipse.che.api.workspace.server.WorkspaceService;
import org.eclipse.che.everrest.CodenvyAsynchronousJobPool;
import org.eclipse.che.everrest.ETagResponseFilter;
import org.eclipse.che.generator.archetype.ArchetypeGenerator;
import org.eclipse.che.ide.extension.maven.server.projecttype.handler.AddMavenModuleHandler;
import org.eclipse.che.ide.extension.maven.server.projecttype.handler.ArchetypeGenerationStrategy;
import org.eclipse.che.ide.extension.maven.server.projecttype.handler.GeneratorStrategy;
import org.eclipse.che.ide.extension.maven.server.projecttype.handler.GetMavenModulesHandler;
import org.eclipse.che.ide.extension.maven.server.projecttype.handler.MavenProjectGenerator;
import org.eclipse.che.ide.extension.maven.server.projecttype.handler.MavenProjectImportedHandler;
import org.eclipse.che.ide.extension.maven.server.projecttype.handler.ProjectHasBecomeMaven;
import org.eclipse.che.inject.DynaModule;
import org.eclipse.che.security.oauth.OAuthAuthenticationService;
import org.eclipse.che.security.oauth.OAuthAuthenticatorProvider;
import org.eclipse.che.security.oauth.OAuthAuthenticatorProviderImpl;
import org.eclipse.che.security.oauth.OAuthAuthenticatorTokenProvider;
import org.eclipse.che.vfs.impl.fs.LocalFSMountStrategy;
import org.eclipse.che.vfs.impl.fs.LocalFileSystemRegistryPlugin;
import org.eclipse.che.vfs.impl.fs.LocalVirtualFileSystemRegistry;
import org.eclipse.che.vfs.impl.fs.MappedDirectoryLocalFSMountStrategy;
import org.eclipse.che.vfs.impl.fs.VirtualFileSystemFSModule;
import org.eclipse.che.vfs.impl.fs.WorkspaceToDirectoryMappingService;
import org.everrest.core.impl.async.AsynchronousJobPool;
import org.everrest.core.impl.async.AsynchronousJobService;
import org.everrest.guice.PathKey;

/**
 * @author Evgen Vidolob
 */
@DynaModule
public class MachineModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(ApiInfoService.class);



        Multibinder.newSetBinder(binder(), ProjectHandler.class).addBinding().to(MavenProjectGenerator.class);
        Multibinder.newSetBinder(binder(), ProjectHandler.class).addBinding().to(AddMavenModuleHandler.class);
        Multibinder.newSetBinder(binder(), ProjectHandler.class).addBinding().to(MavenProjectImportedHandler.class);
        Multibinder.newSetBinder(binder(), ProjectHandler.class).addBinding().to(ProjectHasBecomeMaven.class);
        Multibinder.newSetBinder(binder(), ProjectHandler.class).addBinding().to(GetMavenModulesHandler.class);
        bind(WorkspaceService.class);
        bind(LocalFileSystemRegistryPlugin.class);

        bind(LocalFSMountStrategy.class).to(MachineFSMountStrategy.class);
        bind(VirtualFileSystemRegistry.class).to(LocalVirtualFileSystemRegistry.class);

        install(new CoreRestModule());
        install(new BaseProjectModule());
        install(new VirtualFileSystemModule());
        install(new VirtualFileSystemFSModule());

        bind(ArchetypeGenerator.class);

        bind(AsynchronousJobPool.class).to(CodenvyAsynchronousJobPool.class);
        bind(new PathKey<>(AsynchronousJobService.class, "/async/{ws-id}")).to(AsynchronousJobService.class);
//        bind(WSocketEventBusClient.class).asEagerSingleton();

        Multibinder.newSetBinder(binder(), GeneratorStrategy.class).addBinding().to(ArchetypeGenerationStrategy.class);
    }
}
