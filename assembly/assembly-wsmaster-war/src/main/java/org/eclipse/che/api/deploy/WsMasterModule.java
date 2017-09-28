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
package org.eclipse.che.api.deploy;

import static com.google.inject.matcher.Matchers.subclassesOf;
import static org.eclipse.che.inject.Matchers.names;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import java.util.Set;
import org.eclipse.che.api.core.rest.CheJsonProvider;
import org.eclipse.che.api.core.rest.MessageBodyAdapter;
import org.eclipse.che.api.core.rest.MessageBodyAdapterInterceptor;
import org.eclipse.che.api.factory.server.FactoryAcceptValidator;
import org.eclipse.che.api.factory.server.FactoryCreateValidator;
import org.eclipse.che.api.factory.server.FactoryEditValidator;
import org.eclipse.che.api.factory.server.FactoryParametersResolver;
import org.eclipse.che.api.installer.server.InstallerModule;
import org.eclipse.che.api.installer.server.impl.InstallersProvider;
import org.eclipse.che.api.installer.shared.model.Installer;
import org.eclipse.che.api.recipe.JpaRecipeDao;
import org.eclipse.che.api.recipe.RecipeDao;
import org.eclipse.che.api.recipe.RecipeLoader;
import org.eclipse.che.api.recipe.RecipeService;
import org.eclipse.che.api.system.server.ServiceTermination;
import org.eclipse.che.api.system.server.SystemModule;
import org.eclipse.che.api.workspace.server.adapter.StackMessageBodyAdapter;
import org.eclipse.che.api.workspace.server.adapter.WorkspaceConfigMessageBodyAdapter;
import org.eclipse.che.api.workspace.server.adapter.WorkspaceMessageBodyAdapter;
import org.eclipse.che.api.workspace.server.hc.ServerCheckerFactory;
import org.eclipse.che.api.workspace.server.hc.ServerCheckerFactoryImpl;
import org.eclipse.che.api.workspace.server.stack.StackLoader;
import org.eclipse.che.core.db.schema.SchemaInitializer;
import org.eclipse.che.inject.DynaModule;
import org.eclipse.che.plugin.github.factory.resolver.GithubFactoryParametersResolver;
import org.flywaydb.core.internal.util.PlaceholderReplacer;

/** @author andrew00x */
@DynaModule
public class WsMasterModule extends AbstractModule {
  @Override
  protected void configure() {
    // db related components modules
    install(new com.google.inject.persist.jpa.JpaPersistModule("main"));
    install(new org.eclipse.che.account.api.AccountModule());
    install(new org.eclipse.che.api.ssh.server.jpa.SshJpaModule());
    bind(RecipeDao.class).to(JpaRecipeDao.class);
    install(new org.eclipse.che.api.core.jsonrpc.impl.JsonRpcModule());
    install(new org.eclipse.che.api.core.websocket.impl.WebSocketModule());

    // db configuration
    bind(SchemaInitializer.class)
        .to(org.eclipse.che.core.db.schema.impl.flyway.FlywaySchemaInitializer.class);
    bind(org.eclipse.che.core.db.DBInitializer.class).asEagerSingleton();
    bind(PlaceholderReplacer.class)
        .toProvider(org.eclipse.che.core.db.schema.impl.flyway.PlaceholderReplacerProvider.class);

    //factory
    bind(FactoryAcceptValidator.class)
        .to(org.eclipse.che.api.factory.server.impl.FactoryAcceptValidatorImpl.class);
    bind(FactoryCreateValidator.class)
        .to(org.eclipse.che.api.factory.server.impl.FactoryCreateValidatorImpl.class);
    bind(FactoryEditValidator.class)
        .to(org.eclipse.che.api.factory.server.impl.FactoryEditValidatorImpl.class);
    bind(org.eclipse.che.api.factory.server.FactoryService.class);
    install(new org.eclipse.che.api.factory.server.jpa.FactoryJpaModule());

    Multibinder<FactoryParametersResolver> factoryParametersResolverMultibinder =
        Multibinder.newSetBinder(binder(), FactoryParametersResolver.class);
    factoryParametersResolverMultibinder.addBinding().to(GithubFactoryParametersResolver.class);

    bind(org.eclipse.che.api.core.rest.ApiInfoService.class);
    bind(org.eclipse.che.api.project.server.template.ProjectTemplateDescriptionLoader.class)
        .asEagerSingleton();
    bind(org.eclipse.che.api.project.server.template.ProjectTemplateRegistry.class);
    bind(org.eclipse.che.api.project.server.template.ProjectTemplateService.class);
    bind(org.eclipse.che.api.ssh.server.SshService.class);
    bind(RecipeService.class);
    bind(org.eclipse.che.api.user.server.UserService.class);
    bind(org.eclipse.che.api.user.server.ProfileService.class);
    bind(org.eclipse.che.api.user.server.PreferencesService.class);

    MapBinder<String, String> stacks =
        MapBinder.newMapBinder(
            binder(), String.class, String.class, Names.named(StackLoader.CHE_PREDEFINED_STACKS));
    stacks.addBinding("stacks.json").toInstance("stacks-images");
    stacks.addBinding("che-in-che.json").toInstance("");
    bind(org.eclipse.che.api.workspace.server.stack.StackService.class);
    bind(org.eclipse.che.api.workspace.server.TemporaryWorkspaceRemover.class);
    bind(org.eclipse.che.api.workspace.server.WorkspaceService.class);
    bind(org.eclipse.che.api.workspace.server.bootstrap.InstallerService.class);
    bind(org.eclipse.che.api.workspace.server.event.WorkspaceMessenger.class).asEagerSingleton();
    bind(org.eclipse.che.api.workspace.server.event.WorkspaceJsonRpcMessenger.class)
        .asEagerSingleton();
    bind(org.eclipse.che.everrest.EverrestDownloadFileResponseFilter.class);
    bind(org.eclipse.che.everrest.ETagResponseFilter.class);

    // temporary solution
    bind(org.eclipse.che.api.workspace.server.event.RuntimeStatusJsonRpcMessenger.class)
        .asEagerSingleton();
    bind(org.eclipse.che.api.workspace.server.event.MachineStatusJsonRpcMessenger.class)
        .asEagerSingleton();
    bind(org.eclipse.che.api.workspace.server.event.ServerStatusJsonRpcMessenger.class)
        .asEagerSingleton();
    bind(org.eclipse.che.api.workspace.server.event.InstallerLogJsonRpcMessenger.class)
        .asEagerSingleton();
    bind(org.eclipse.che.api.workspace.server.event.MachineLogJsonRpcMessenger.class)
        .asEagerSingleton();

    bind(org.eclipse.che.security.oauth.OAuthAuthenticatorProvider.class)
        .to(org.eclipse.che.security.oauth.OAuthAuthenticatorProviderImpl.class);
    bind(org.eclipse.che.security.oauth.shared.OAuthTokenProvider.class)
        .to(org.eclipse.che.security.oauth.OAuthAuthenticatorTokenProvider.class);
    bind(org.eclipse.che.security.oauth.OAuthAuthenticationService.class);

    bind(org.eclipse.che.api.core.notification.WSocketEventBusServer.class);

    bind(org.eclipse.che.api.recipe.RecipeLoader.class);
    Multibinder.newSetBinder(
            binder(), String.class, Names.named(RecipeLoader.CHE_PREDEFINED_RECIPES))
        .addBinding()
        .toInstance("predefined-recipes.json");

    // installers
    install(new InstallerModule());
    binder().bind(new TypeLiteral<Set<Installer>>() {}).toProvider(InstallersProvider.class);

    bind(org.eclipse.che.api.deploy.WsMasterAnalyticsAddresser.class);

    install(new org.eclipse.che.plugin.activity.inject.WorkspaceActivityModule());

    install(new org.eclipse.che.api.core.rest.CoreRestModule());
    install(new org.eclipse.che.api.core.util.FileCleaner.FileCleanerModule());
    install(new org.eclipse.che.swagger.deploy.DocsModule());
    install(new org.eclipse.che.commons.schedule.executor.ScheduleModule());

    final Multibinder<MessageBodyAdapter> adaptersMultibinder =
        Multibinder.newSetBinder(binder(), MessageBodyAdapter.class);
    adaptersMultibinder.addBinding().to(WorkspaceConfigMessageBodyAdapter.class);
    adaptersMultibinder.addBinding().to(WorkspaceMessageBodyAdapter.class);
    adaptersMultibinder.addBinding().to(StackMessageBodyAdapter.class);

    final MessageBodyAdapterInterceptor interceptor = new MessageBodyAdapterInterceptor();
    requestInjection(interceptor);
    bindInterceptor(subclassesOf(CheJsonProvider.class), names("readFrom"), interceptor);

    // system components
    install(new SystemModule());
    Multibinder.newSetBinder(binder(), ServiceTermination.class)
        .addBinding()
        .to(org.eclipse.che.api.workspace.server.WorkspaceServiceTermination.class);
    // FIXME: spi
    //        bind(org.eclipse.che.api.agent.server.filters.AddExecInstallerInWorkspaceFilter.class);
    //        bind(org.eclipse.che.api.agent.server.filters.AddExecInstallerInStackFilter.class);

    bind(ServerCheckerFactory.class).to(ServerCheckerFactoryImpl.class);
  }
}
