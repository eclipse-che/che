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
package org.eclipse.che.api.deploy;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;

import org.eclipse.che.api.auth.AuthenticationService;
import org.eclipse.che.api.core.notification.WSocketEventBusServer;
import org.eclipse.che.api.core.rest.ApiInfoService;
import org.eclipse.che.api.core.rest.CoreRestModule;
import org.eclipse.che.api.local.LocalInfrastructureModule;
import org.eclipse.che.api.machine.server.MachineModule;
import org.eclipse.che.api.machine.server.recipe.PermissionsChecker;
import org.eclipse.che.api.machine.server.recipe.PermissionsCheckerImpl;
import org.eclipse.che.api.machine.server.recipe.RecipeLoader;
import org.eclipse.che.api.machine.server.recipe.RecipeService;
import org.eclipse.che.api.project.server.ProjectTemplateDescriptionLoader;
import org.eclipse.che.api.project.server.ProjectTemplateRegistry;
import org.eclipse.che.api.project.server.ProjectTemplateService;
import org.eclipse.che.api.ssh.server.SshService;
import org.eclipse.che.api.user.server.UserProfileService;
import org.eclipse.che.api.user.server.UserService;
import org.eclipse.che.api.workspace.server.WorkspaceConfigValidator;
import org.eclipse.che.api.workspace.server.WorkspaceService;
import org.eclipse.che.api.workspace.server.event.MachineStateListener;
import org.eclipse.che.api.workspace.server.event.WorkspaceMessenger;
import org.eclipse.che.everrest.CodenvyAsynchronousJobPool;
import org.eclipse.che.everrest.ETagResponseFilter;
import org.eclipse.che.everrest.EverrestDownloadFileResponseFilter;
import org.eclipse.che.inject.DynaModule;
import org.eclipse.che.plugin.docker.machine.ServerConf;
import org.eclipse.che.plugin.docker.machine.ext.DockerExtServerModule;
import org.eclipse.che.plugin.docker.machine.ext.DockerMachineExtServerChecker;
import org.eclipse.che.plugin.docker.machine.ext.DockerMachineTerminalChecker;
import org.eclipse.che.plugin.docker.machine.local.LocalDockerModule;
import org.everrest.core.impl.async.AsynchronousJobPool;
import org.everrest.core.impl.async.AsynchronousJobService;
import org.everrest.guice.ServiceBindingHelper;

/** @author andrew00x */
@DynaModule
public class ApiModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(WorkspaceService.class);
        bind(ProjectTemplateRegistry.class);
        bind(ProjectTemplateDescriptionLoader.class).asEagerSingleton();
        bind(ProjectTemplateService.class);

        bind(ApiInfoService.class);

        bind(AuthenticationService.class);
        bind(EverrestDownloadFileResponseFilter.class);
        bind(ETagResponseFilter.class);

//        bind(DockerVersionVerifier.class).asEagerSingleton();

        bind(UserService.class);
        bind(UserProfileService.class);

        bind(SshService.class);

        bind(org.eclipse.che.security.oauth.OAuthAuthenticatorProvider.class)
                .to(org.eclipse.che.security.oauth.OAuthAuthenticatorProviderImpl.class);
        bind(org.eclipse.che.api.auth.oauth.OAuthTokenProvider.class)
                .to(org.eclipse.che.security.oauth.OAuthAuthenticatorTokenProvider.class);

        bind(org.eclipse.che.security.oauth.OAuthAuthenticationService.class);

        bind(RecipeService.class);
        bind(PermissionsChecker.class).to(PermissionsCheckerImpl.class);

//        bind(LocalFSMountStrategy.class).to(MappedDirectoryLocalFSMountStrategy.class);
//
//        bind(WorkspaceToDirectoryMappingService.class);

        bind(WorkspaceMessenger.class).asEagerSingleton();


        bind(AsynchronousJobPool.class).to(CodenvyAsynchronousJobPool.class);
        bind(ServiceBindingHelper.bindingKey(AsynchronousJobService.class, "/async/{ws-id}")).to(AsynchronousJobService.class);

        bind(WSocketEventBusServer.class);

//        bind(VirtualFileSystemRegistry.class).to(LocalVirtualFileSystemRegistry.class);

        install(new CoreRestModule());
//        install(new AnalyticsModule());
//        install(new FactoryModule());
        install(new LocalDockerModule());
        install(new MachineModule());
        install(new LocalInfrastructureModule());

        install(new DockerExtServerModule());
        install(new org.eclipse.che.plugin.docker.machine.ext.DockerTerminalModule());
        install(new org.eclipse.che.swagger.deploy.DocsModule());

        bind(DockerMachineExtServerChecker.class);
        bind(DockerMachineTerminalChecker.class);

        // additional ports for development of extensions
        Multibinder<ServerConf> machineServers = Multibinder.newSetBinder(binder(),
                                                                          ServerConf.class,
                                                                          Names.named("machine.docker.dev_machine.machine_servers"));
        machineServers.addBinding().toInstance(new ServerConf("extensions-debug", "4403", "http"));

        bind(RecipeLoader.class);
        Multibinder.newSetBinder(binder(), String.class, Names.named("predefined.recipe.path"))
                   .addBinding()
                   .toInstance("predefined-recipes.json");

        bindConstant().annotatedWith(Names.named(org.eclipse.che.api.machine.server.WsAgentLauncherImpl.WS_AGENT_PROCESS_START_COMMAND))
                      .to("rm -rf ~/che && mkdir -p ~/che && unzip /mnt/che/ws-agent.zip -d ~/che/ws-agent && " +
                          "export JPDA_ADDRESS=\"4403\" && ~/che/ws-agent/bin/catalina.sh jpda run");

        install(new org.eclipse.che.plugin.docker.machine.ext.LocalStorageModule());

        bind(WorkspaceConfigValidator.class).to(org.eclipse.che.api.workspace.server.DefaultWorkspaceConfigValidator.class);
        bind(MachineStateListener.class).asEagerSingleton();
        bind(org.eclipse.che.api.machine.server.WsAgentLauncher.class).to(org.eclipse.che.api.machine.server.WsAgentLauncherImpl.class);
    }
}
