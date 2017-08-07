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
package org.eclipse.che.api.deploy;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;

import org.eclipse.che.api.core.rest.CheJsonProvider;
import org.eclipse.che.api.core.rest.MessageBodyAdapter;
import org.eclipse.che.api.core.rest.MessageBodyAdapterInterceptor;
import org.eclipse.che.api.factory.server.FactoryAcceptValidator;
import org.eclipse.che.api.factory.server.FactoryCreateValidator;
import org.eclipse.che.api.factory.server.FactoryEditValidator;
import org.eclipse.che.api.factory.server.FactoryParametersResolver;
import org.eclipse.che.api.installer.ExecInstaller;
import org.eclipse.che.api.installer.GitCredentialsInstaller;
import org.eclipse.che.api.installer.LSCSharpInstaller;
import org.eclipse.che.api.installer.LSJsonInstaller;
import org.eclipse.che.api.installer.LSPhpInstaller;
import org.eclipse.che.api.installer.LSPythonInstaller;
import org.eclipse.che.api.installer.LSTypeScriptInstaller;
import org.eclipse.che.api.installer.SshInstaller;
import org.eclipse.che.api.installer.TerminalInstaller;
import org.eclipse.che.api.installer.UnisonInstaller;
import org.eclipse.che.api.installer.WsInstaller;
import org.eclipse.che.api.installer.server.InstallerRegistry;
import org.eclipse.che.api.installer.server.InstallerRegistryProvider;
import org.eclipse.che.api.installer.server.InstallerRegistryService;
import org.eclipse.che.api.installer.shared.model.Installer;
import org.eclipse.che.api.recipe.JpaRecipeDao;
import org.eclipse.che.api.recipe.RecipeDao;
import org.eclipse.che.api.recipe.RecipeLoader;
import org.eclipse.che.api.recipe.RecipeService;
import org.eclipse.che.api.system.server.ServiceTermination;
import org.eclipse.che.api.system.server.SystemModule;
import org.eclipse.che.api.user.server.TokenValidator;
import org.eclipse.che.api.workspace.server.RemoveWorkspaceFilesAfterRemoveWorkspaceEventSubscriber;
import org.eclipse.che.api.workspace.server.adapter.StackMessageBodyAdapter;
import org.eclipse.che.api.workspace.server.adapter.WorkspaceConfigMessageBodyAdapter;
import org.eclipse.che.api.workspace.server.adapter.WorkspaceMessageBodyAdapter;
import org.eclipse.che.api.workspace.server.stack.StackLoader;
import org.eclipse.che.core.db.schema.SchemaInitializer;
import org.eclipse.che.inject.DynaModule;
import org.eclipse.che.plugin.github.factory.resolver.GithubFactoryParametersResolver;
import org.eclipse.che.workspace.infrastructure.docker.DockerInfraModule;
import org.eclipse.che.workspace.infrastructure.docker.local.LocalDockerModule;
import org.eclipse.che.workspace.infrastructure.docker.snapshot.JpaSnapshotDao;
import org.eclipse.che.workspace.infrastructure.docker.snapshot.SnapshotDao;
import org.eclipse.che.workspace.infrastructure.openshift.OpenShiftInfraModule;
import org.flywaydb.core.internal.util.PlaceholderReplacer;

import javax.sql.DataSource;

import static com.google.inject.matcher.Matchers.subclassesOf;
import static org.eclipse.che.inject.Matchers.names;

/** @author andrew00x */
@DynaModule
public class WsMasterModule extends AbstractModule {
    @Override
    protected void configure() {
        // db related components modules
        install(new com.google.inject.persist.jpa.JpaPersistModule("main"));
        install(new org.eclipse.che.account.api.AccountModule());
        install(new org.eclipse.che.api.user.server.jpa.UserJpaModule());
        install(new org.eclipse.che.api.ssh.server.jpa.SshJpaModule());
//        install(new org.eclipse.che.api.machine.server.jpa.MachineJpaModule());
        bind(RecipeDao.class).to(JpaRecipeDao.class);
        // TODO spi move into docker infra impl
        bind(SnapshotDao.class).to(JpaSnapshotDao.class);
        install(new org.eclipse.che.api.workspace.server.jpa.WorkspaceJpaModule());
        install(new org.eclipse.che.api.core.jsonrpc.impl.JsonRpcModule());
        install(new org.eclipse.che.api.core.websocket.impl.WebSocketModule());

        // db configuration
        bind(DataSource.class).toProvider(org.eclipse.che.core.db.h2.H2DataSourceProvider.class);
        bind(SchemaInitializer.class).to(org.eclipse.che.core.db.schema.impl.flyway.FlywaySchemaInitializer.class);
        bind(org.eclipse.che.core.db.DBInitializer.class).asEagerSingleton();
        bind(PlaceholderReplacer.class).toProvider(org.eclipse.che.core.db.schema.impl.flyway.PlaceholderReplacerProvider.class);

        //factory
        bind(FactoryAcceptValidator.class).to(org.eclipse.che.api.factory.server.impl.FactoryAcceptValidatorImpl.class);
        bind(FactoryCreateValidator.class).to(org.eclipse.che.api.factory.server.impl.FactoryCreateValidatorImpl.class);
        bind(FactoryEditValidator.class).to(org.eclipse.che.api.factory.server.impl.FactoryEditValidatorImpl.class);
        bind(org.eclipse.che.api.factory.server.FactoryService.class);
        install(new org.eclipse.che.api.factory.server.jpa.FactoryJpaModule());

        Multibinder<FactoryParametersResolver> factoryParametersResolverMultibinder =
                Multibinder.newSetBinder(binder(), FactoryParametersResolver.class);
        factoryParametersResolverMultibinder.addBinding()
                                            .to(GithubFactoryParametersResolver.class);

        bind(org.eclipse.che.api.user.server.CheUserCreator.class);

        bind(TokenValidator.class).to(org.eclipse.che.api.local.DummyTokenValidator.class);

        bind(org.eclipse.che.api.core.rest.ApiInfoService.class);
        bind(org.eclipse.che.api.project.server.template.ProjectTemplateDescriptionLoader.class).asEagerSingleton();
        bind(org.eclipse.che.api.project.server.template.ProjectTemplateRegistry.class);
        bind(org.eclipse.che.api.project.server.template.ProjectTemplateService.class);
        bind(org.eclipse.che.api.ssh.server.SshService.class);
        bind(RecipeService.class);
        bind(org.eclipse.che.api.user.server.UserService.class);
        bind(org.eclipse.che.api.user.server.ProfileService.class);
        bind(org.eclipse.che.api.user.server.PreferencesService.class);

        bind(org.eclipse.che.api.workspace.server.stack.StackLoader.class);
        MapBinder<String, String> stacks = MapBinder.newMapBinder(binder(), String.class, String.class,
                                                                  Names.named(StackLoader.CHE_PREDEFINED_STACKS));
        stacks.addBinding("stacks.json").toInstance("stacks-images");
        stacks.addBinding("che-in-che.json").toInstance("");
        bind(org.eclipse.che.api.workspace.server.stack.StackService.class);
        bind(org.eclipse.che.api.workspace.server.TemporaryWorkspaceRemover.class);
        bind(org.eclipse.che.api.workspace.server.WorkspaceService.class);
        bind(org.eclipse.che.api.workspace.server.OutputService.class);
        bind(org.eclipse.che.api.workspace.server.bootstrap.InstallerService.class);
        bind(org.eclipse.che.api.workspace.server.event.WorkspaceMessenger.class).asEagerSingleton();
        bind(org.eclipse.che.api.workspace.server.event.WorkspaceJsonRpcMessenger.class).asEagerSingleton();
        bind(org.eclipse.che.everrest.EverrestDownloadFileResponseFilter.class);
        bind(org.eclipse.che.everrest.ETagResponseFilter.class);

        bind(InstallerRegistry.class).toProvider(InstallerRegistryProvider.class);
        bind(InstallerRegistryService.class);

        // temporary solution
        bind(org.eclipse.che.api.workspace.server.event.RuntimeStatusJsonRpcMessenger.class).asEagerSingleton();
        bind(org.eclipse.che.api.workspace.server.event.MachineStatusJsonRpcMessenger.class).asEagerSingleton();
        bind(org.eclipse.che.api.workspace.server.event.ServerStatusJsonRpcMessenger.class).asEagerSingleton();
        bind(org.eclipse.che.api.workspace.server.event.InstallerLogJsonRpcMessenger.class).asEagerSingleton();
        bind(org.eclipse.che.api.workspace.server.event.MachineLogJsonRpcMessenger.class).asEagerSingleton();
        //

        bind(org.eclipse.che.security.oauth.OAuthAuthenticatorProvider.class)
                .to(org.eclipse.che.security.oauth.OAuthAuthenticatorProviderImpl.class);
        bind(org.eclipse.che.security.oauth.shared.OAuthTokenProvider.class)
                .to(org.eclipse.che.security.oauth.OAuthAuthenticatorTokenProvider.class);
        bind(org.eclipse.che.security.oauth.OAuthAuthenticationService.class);

        bind(org.eclipse.che.api.core.notification.WSocketEventBusServer.class);
        // additional ports for development of extensions
// FIXME: spi
//        Multibinder<org.eclipse.che.api.core.model.machine.ServerConf> machineServers
//                = Multibinder.newSetBinder(binder(),
//                                           org.eclipse.che.api.core.model.machine.ServerConf.class,
//                                           Names.named("machine.docker.dev_machine.machine_servers"));
//        machineServers.addBinding().toInstance(
//                new org.eclipse.che.api.machine.server.model.impl.ServerConfImpl(Constants.WSAGENT_DEBUG_REFERENCE, "4403/tcp", "http",
//                                                                                 null));

//        bind(org.eclipse.che.api.agent.server.WsAgentHealthChecker.class)
//                .to(org.eclipse.che.api.agent.server.WsAgentHealthCheckerImpl.class);

        bind(org.eclipse.che.api.recipe.RecipeLoader.class);
        Multibinder.newSetBinder(binder(), String.class, Names.named(RecipeLoader.CHE_PREDEFINED_RECIPES))
                   .addBinding().toInstance("predefined-recipes.json");

        // installers
        Multibinder<Installer> installers = Multibinder.newSetBinder(binder(), Installer.class);
        installers.addBinding().to(SshInstaller.class);
        installers.addBinding().to(UnisonInstaller.class);
        installers.addBinding().to(ExecInstaller.class);
        installers.addBinding().to(TerminalInstaller.class);
        installers.addBinding().to(WsInstaller.class);
        installers.addBinding().to(LSPhpInstaller.class);
        installers.addBinding().to(LSPythonInstaller.class);
        installers.addBinding().to(LSJsonInstaller.class);
        installers.addBinding().to(LSCSharpInstaller.class);
        installers.addBinding().to(LSTypeScriptInstaller.class);
        installers.addBinding().to(GitCredentialsInstaller.class);

        bind(org.eclipse.che.api.deploy.WsMasterAnalyticsAddresser.class);

// FIXME: spi
//        Multibinder<org.eclipse.che.api.machine.server.spi.InstanceProvider> machineImageProviderMultibinder =
//                Multibinder.newSetBinder(binder(), org.eclipse.che.api.machine.server.spi.InstanceProvider.class);
//        machineImageProviderMultibinder.addBinding().to(org.eclipse.che.plugin.docker.machine.DockerInstanceProvider.class);

//        bind(org.eclipse.che.api.environment.server.MachineInstanceProvider.class)
//                .to(org.eclipse.che.plugin.docker.machine.MachineProviderImpl.class);
        install(new org.eclipse.che.api.workspace.server.activity.inject.WorkspaceActivityModule());

        install(new org.eclipse.che.api.core.rest.CoreRestModule());
        install(new org.eclipse.che.api.core.util.FileCleaner.FileCleanerModule());
// FIXME: spi
//        install(new org.eclipse.che.plugin.docker.machine.local.LocalDockerModule());
//        install(new org.eclipse.che.api.machine.server.MachineModule());
// FIXME: spi
//        install(new org.eclipse.che.plugin.docker.machine.ext.DockerExtServerModule());
        install(new org.eclipse.che.swagger.deploy.DocsModule());
// FIXME: spi
//        install(new org.eclipse.che.workspace.infrastructure.docker.old.proxy.DockerProxyModule());
        install(new org.eclipse.che.commons.schedule.executor.ScheduleModule());

        final Multibinder<MessageBodyAdapter> adaptersMultibinder = Multibinder.newSetBinder(binder(), MessageBodyAdapter.class);
        adaptersMultibinder.addBinding().to(WorkspaceConfigMessageBodyAdapter.class);
        adaptersMultibinder.addBinding().to(WorkspaceMessageBodyAdapter.class);
        adaptersMultibinder.addBinding().to(StackMessageBodyAdapter.class);

        final MessageBodyAdapterInterceptor interceptor = new MessageBodyAdapterInterceptor();
        requestInjection(interceptor);
        bindInterceptor(subclassesOf(CheJsonProvider.class), names("readFrom"), interceptor);
// FIXME: spi
//        bind(org.eclipse.che.api.workspace.server.WorkspaceFilesCleaner.class);
//                .to(org.eclipse.che.workspace.infrastructure.docker.old.cleaner.LocalWorkspaceFilesCleaner.class);
//        bind(org.eclipse.che.api.environment.server.InfrastructureProvisioner.class)
//                .to(org.eclipse.che.plugin.docker.machine.local.LocalCheInfrastructureProvisioner.class);

        // system components
        install(new SystemModule());
        Multibinder.newSetBinder(binder(), ServiceTermination.class)
                   .addBinding()
                   .to(org.eclipse.che.api.workspace.server.WorkspaceServiceTermination.class);
// FIXME: spi
//        install(new org.eclipse.che.workspace.infrastructure.docker.old.config.dns.DnsResolversModule());

// FIXME: spi
//        bind(org.eclipse.che.api.agent.server.filters.AddExecInstallerInWorkspaceFilter.class);
//        bind(org.eclipse.che.api.agent.server.filters.AddExecInstallerInStackFilter.class);

// FIXME: spi
        install(new DockerInfraModule());
        install(new LocalDockerModule());
        install(new OpenShiftInfraModule());
        bind(RemoveWorkspaceFilesAfterRemoveWorkspaceEventSubscriber.class).asEagerSingleton();
    }
}
