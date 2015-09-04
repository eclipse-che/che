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

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;

import org.eclipse.che.api.auth.AuthenticationService;
import org.eclipse.che.api.core.notification.WSocketEventBusServer;
import org.eclipse.che.api.core.rest.ApiInfoService;
import org.eclipse.che.api.core.rest.CoreRestModule;
import org.eclipse.che.api.machine.server.MachineClientImpl;
import org.eclipse.che.api.workspace.server.MachineClient;
import org.eclipse.che.plugin.docker.machine.ext.DockerMachineExtServerLauncher;
import org.eclipse.che.api.machine.server.command.CommandService;
import org.eclipse.che.api.machine.server.recipe.PermissionsChecker;
import org.eclipse.che.api.machine.server.recipe.PermissionsCheckerImpl;
import org.eclipse.che.api.machine.server.recipe.RecipeService;
import org.eclipse.che.api.user.server.UserProfileService;
import org.eclipse.che.api.user.server.UserService;
import org.eclipse.che.api.workspace.server.WorkspaceService;
import org.eclipse.che.everrest.CodenvyAsynchronousJobPool;
import org.eclipse.che.everrest.ETagResponseFilter;
import org.eclipse.che.ide.ext.ssh.server.KeyService;
import org.eclipse.che.ide.ext.ssh.server.SshKeyStore;
import org.eclipse.che.ide.ext.ssh.server.UserProfileSshKeyStore;
import org.eclipse.che.inject.DynaModule;
import org.eclipse.che.plugin.docker.machine.ServerConf;
import org.eclipse.che.plugin.docker.machine.ext.DockerExtServerModule;
import org.eclipse.che.plugin.docker.machine.local.LocalDockerModule;
import org.everrest.core.impl.async.AsynchronousJobPool;
import org.everrest.core.impl.async.AsynchronousJobService;
import org.everrest.guice.PathKey;

/** @author andrew00x */
@DynaModule
public class ApiModule extends AbstractModule {
    @Override
    protected void configure() {

        bind(WorkspaceService.class);
        bind(MachineClient.class).to(MachineClientImpl.class).in(Singleton.class);

        bind(ApiInfoService.class);

        bind(AuthenticationService.class);
        bind(ETagResponseFilter.class);

//        bind(DockerVersionVerifier.class).asEagerSingleton();

        bind(UserService.class);
        bind(UserProfileService.class);

//        bind(LocalFileSystemRegistryPlugin.class);

        bind(RecipeService.class);
        bind(PermissionsChecker.class).to(PermissionsCheckerImpl.class);

        bind(CommandService.class);

//        bind(BuilderSelectionStrategy.class).to(LastInUseBuilderSelectionStrategy.class);
//        bind(BuilderService.class);
//        bind(BuilderAdminService.class);
//        bind(SlaveBuilderService.class);

//        bind(LocalFSMountStrategy.class).to(MappedDirectoryLocalFSMountStrategy.class);
//        bind(RunnerSelectionStrategy.class).to(LastInUseRunnerSelectionStrategy.class);
//        bind(RunnerService.class);
//        bind(RunnerAdminService.class);
//        bind(SlaveRunnerService.class);
//
//        bind(DebuggerService.class);
//        bind(FormatService.class);

//        bind(WorkspaceToDirectoryMappingService.class);

        bind(KeyService.class);
        bind(SshKeyStore.class).to(UserProfileSshKeyStore.class);

//        bind(OAuthAuthenticationService.class);
//        bind(OAuthTokenProvider.class).to(OAuthAuthenticatorTokenProvider.class);
//        bind(OAuthAuthenticatorProvider.class).to(OAuthAuthenticatorProviderImpl.class);


        bind(AsynchronousJobPool.class).to(CodenvyAsynchronousJobPool.class);
        bind(new PathKey<>(AsynchronousJobService.class, "/async/{ws-id}")).to(AsynchronousJobService.class);

        bind(WSocketEventBusServer.class);

//        bind(VirtualFileSystemRegistry.class).to(LocalVirtualFileSystemRegistry.class);

//        install(new ArchetypeGeneratorModule());

        install(new CoreRestModule());
//        install(new AnalyticsModule());
//        install(new BaseProjectModule());
//        install(new AnalyticsModule());
//        install(new BaseProjectModule());
//        install(new BuilderModule());
//        install(new RunnerModule());
//        install(new VirtualFileSystemModule());
//        install(new VirtualFileSystemFSModule());
//        install(new FactoryModule());
        install(new LocalDockerModule());

        install(new DockerExtServerModule());
        install(new org.eclipse.che.plugin.docker.machine.ext.DockerTerminalModule());

        // additional ports for development of extensions
        Multibinder<ServerConf> machineServers = Multibinder.newSetBinder(binder(),
                                                                          ServerConf.class,
                                                                          Names.named("machine.docker.dev_machine.machine_servers"));
        machineServers.addBinding().toInstance(new ServerConf("extensions-debug", "4403", "http"));

        bindConstant().annotatedWith(Names.named(DockerMachineExtServerLauncher.START_EXT_SERVER_COMMAND))
                      .to("mkdir -p ~/che && unzip /mnt/che/ext-server.zip -d ~/che/ext-server && " +
                          "export JPDA_ADDRESS=\"4403\" && ~/che/ext-server/bin/catalina.sh jpda start");

        install(new org.eclipse.che.plugin.docker.machine.ext.LocalStorageModule());
    }
}
