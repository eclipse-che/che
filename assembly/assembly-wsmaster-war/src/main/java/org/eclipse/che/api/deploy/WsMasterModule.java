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

import org.eclipse.che.api.machine.shared.Constants;
import org.eclipse.che.api.user.server.ProfileService;
import org.eclipse.che.inject.DynaModule;
import org.everrest.guice.ServiceBindingHelper;

/** @author andrew00x */
@DynaModule
public class WsMasterModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(org.eclipse.che.api.core.rest.ApiInfoService.class);
        bind(org.eclipse.che.api.project.server.template.ProjectTemplateDescriptionLoader.class).asEagerSingleton();
        bind(org.eclipse.che.api.project.server.template.ProjectTemplateRegistry.class);
        bind(org.eclipse.che.api.project.server.template.ProjectTemplateService.class);
        bind(org.eclipse.che.api.ssh.server.SshService.class);
        bind(org.eclipse.che.api.machine.server.recipe.RecipeService.class);
        bind(org.eclipse.che.api.user.server.UserService.class);
        bind(org.eclipse.che.api.user.server.ProfileService.class);
        bind(org.eclipse.che.api.user.server.PreferencesService.class);
        bind(org.eclipse.che.api.workspace.server.stack.StackLoader.class);
        bind(org.eclipse.che.api.workspace.server.stack.StackService.class);
        bind(org.eclipse.che.api.workspace.server.WorkspaceService.class);
        bind(org.eclipse.che.api.workspace.server.event.WorkspaceMessenger.class).asEagerSingleton();
        bind(org.everrest.core.impl.async.AsynchronousJobPool.class).to(org.eclipse.che.everrest.CheAsynchronousJobPool.class);
        bind(ServiceBindingHelper.bindingKey(org.everrest.core.impl.async.AsynchronousJobService.class, "/async/{ws-id}"))
                .to(org.everrest.core.impl.async.AsynchronousJobService.class);
        bind(org.eclipse.che.plugin.docker.machine.ext.DockerMachineExtServerChecker.class);
        bind(org.eclipse.che.plugin.docker.machine.ext.DockerMachineTerminalChecker.class);
        bind(org.eclipse.che.everrest.EverrestDownloadFileResponseFilter.class);
        bind(org.eclipse.che.everrest.ETagResponseFilter.class);

        bind(org.eclipse.che.security.oauth.OAuthAuthenticatorProvider.class)
                .to(org.eclipse.che.security.oauth.OAuthAuthenticatorProviderImpl.class);
        bind(org.eclipse.che.api.auth.oauth.OAuthTokenProvider.class)
                .to(org.eclipse.che.security.oauth.OAuthAuthenticatorTokenProvider.class);
        bind(org.eclipse.che.security.oauth.OAuthAuthenticationService.class);

        bind(org.eclipse.che.api.core.notification.WSocketEventBusServer.class);
        // additional ports for development of extensions
        Multibinder<org.eclipse.che.api.core.model.machine.ServerConf> machineServers = Multibinder.newSetBinder(binder(),
                                                                                   org.eclipse.che.api.core.model.machine.ServerConf.class,
                                                                                   Names.named("machine.docker.dev_machine.machine_servers"));
        machineServers.addBinding().toInstance(
                new org.eclipse.che.api.machine.server.model.impl.ServerConfImpl(Constants.WSAGENT_DEBUG_REFERENCE, "4403/tcp", "http",
                                                                                 null));

        bind(org.eclipse.che.api.machine.server.recipe.RecipeLoader.class);
        Multibinder.newSetBinder(binder(), String.class, Names.named("predefined.recipe.path"))
                   .addBinding()
                   .toInstance("predefined-recipes.json");


        bindConstant().annotatedWith(Names.named(org.eclipse.che.api.machine.server.wsagent.WsAgentLauncherImpl.WS_AGENT_PROCESS_START_COMMAND))
                      .to("rm -rf ~/che && mkdir -p ~/che && unzip -qq /mnt/che/ws-agent.zip -d ~/che/ws-agent && " +
                          "sudo chown -R $(id -u -n) /projects && " +
                          "export JPDA_ADDRESS=\"4403\" && ~/che/ws-agent/bin/catalina.sh jpda run");
        bindConstant().annotatedWith(Names.named(org.eclipse.che.plugin.docker.machine.DockerMachineImplTerminalLauncher.START_TERMINAL_COMMAND))
                      .to("mkdir -p ~/che " +
                          "&& cp /mnt/che/terminal -R ~/che" +
                          "&& ~/che/terminal/che-websocket-terminal -addr :4411 -cmd /bin/bash -static ~/che/terminal/");
        bind(org.eclipse.che.api.workspace.server.WorkspaceValidator.class)
                .to(org.eclipse.che.api.workspace.server.DefaultWorkspaceValidator.class);

        bind(org.eclipse.che.api.workspace.server.event.MachineStateListener.class).asEagerSingleton();

        bind(org.eclipse.che.api.machine.server.wsagent.WsAgentLauncher.class)
                .to(org.eclipse.che.api.machine.server.wsagent.WsAgentLauncherImpl.class);

        bind(org.eclipse.che.api.machine.server.terminal.MachineTerminalLauncher.class);
        bind(org.eclipse.che.api.deploy.WsMasterAnalyticsAddresser.class);

        Multibinder<org.eclipse.che.api.machine.server.spi.InstanceProvider> machineImageProviderMultibinder =
                Multibinder.newSetBinder(binder(), org.eclipse.che.api.machine.server.spi.InstanceProvider.class);
        machineImageProviderMultibinder.addBinding().to(org.eclipse.che.plugin.docker.machine.DockerInstanceProvider.class);

        install(new org.eclipse.che.api.core.rest.CoreRestModule());
        install(new org.eclipse.che.api.core.util.FileCleaner.FileCleanerModule());
        install(new org.eclipse.che.plugin.docker.machine.local.LocalDockerModule());
        install(new org.eclipse.che.api.machine.server.MachineModule());
        install(new org.eclipse.che.api.local.LocalInfrastructureModule());
        install(new org.eclipse.che.plugin.docker.machine.ext.DockerExtServerModule());
        install(new org.eclipse.che.plugin.docker.machine.ext.DockerTerminalModule());
        install(new org.eclipse.che.swagger.deploy.DocsModule());
        install(new org.eclipse.che.plugin.machine.ssh.SshMachineModule());
        install(new org.eclipse.che.plugin.docker.machine.proxy.DockerProxyModule());
        install(new org.eclipse.che.commons.schedule.executor.ScheduleModule());
    }
}
