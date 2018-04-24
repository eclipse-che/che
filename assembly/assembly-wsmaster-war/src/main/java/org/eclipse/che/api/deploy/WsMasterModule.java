/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
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
import static org.eclipse.che.multiuser.api.permission.server.SystemDomain.SYSTEM_DOMAIN_ACTIONS;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.sql.DataSource;
import org.eclipse.che.agent.exec.client.ExecAgentClientFactory;
import org.eclipse.che.api.core.notification.InmemoryRemoteSubscriptionStorage;
import org.eclipse.che.api.core.notification.RemoteSubscriptionStorage;
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
import org.eclipse.che.api.system.server.ServiceTermination;
import org.eclipse.che.api.system.server.SystemModule;
import org.eclipse.che.api.user.server.TokenValidator;
import org.eclipse.che.api.user.server.jpa.JpaPreferenceDao;
import org.eclipse.che.api.user.server.jpa.JpaUserDao;
import org.eclipse.che.api.user.server.spi.PreferenceDao;
import org.eclipse.che.api.user.server.spi.UserDao;
import org.eclipse.che.api.workspace.server.DefaultWorkspaceLockService;
import org.eclipse.che.api.workspace.server.DefaultWorkspaceStatusCache;
import org.eclipse.che.api.workspace.server.WorkspaceLockService;
import org.eclipse.che.api.workspace.server.WorkspaceStatusCache;
import org.eclipse.che.api.workspace.server.hc.ServersCheckerFactory;
import org.eclipse.che.api.workspace.server.spi.provision.InstallerConfigProvisioner;
import org.eclipse.che.api.workspace.server.spi.provision.InternalEnvironmentProvisioner;
import org.eclipse.che.api.workspace.server.spi.provision.ProjectsVolumeForWsAgentProvisioner;
import org.eclipse.che.api.workspace.server.spi.provision.env.AgentAuthEnableEnvVarProvider;
import org.eclipse.che.api.workspace.server.spi.provision.env.CheApiEnvVarProvider;
import org.eclipse.che.api.workspace.server.spi.provision.env.CheApiExternalEnvVarProvider;
import org.eclipse.che.api.workspace.server.spi.provision.env.CheApiInternalEnvVarProvider;
import org.eclipse.che.api.workspace.server.spi.provision.env.EnvVarEnvironmentProvisioner;
import org.eclipse.che.api.workspace.server.spi.provision.env.EnvVarProvider;
import org.eclipse.che.api.workspace.server.spi.provision.env.JavaOptsEnvVariableProvider;
import org.eclipse.che.api.workspace.server.spi.provision.env.MachineTokenEnvVarProvider;
import org.eclipse.che.api.workspace.server.spi.provision.env.MavenOptsEnvVariableProvider;
import org.eclipse.che.api.workspace.server.spi.provision.env.ProjectsRootEnvVariableProvider;
import org.eclipse.che.api.workspace.server.spi.provision.env.WorkspaceAgentJavaOptsEnvVariableProvider;
import org.eclipse.che.api.workspace.server.spi.provision.env.WorkspaceIdEnvVarProvider;
import org.eclipse.che.api.workspace.server.spi.provision.env.WorkspaceMavenServerJavaOptsEnvVariableProvider;
import org.eclipse.che.api.workspace.server.stack.StackLoader;
import org.eclipse.che.api.workspace.server.token.MachineTokenProvider;
import org.eclipse.che.commons.auth.token.ChainedTokenExtractor;
import org.eclipse.che.commons.auth.token.RequestTokenExtractor;
import org.eclipse.che.core.db.schema.SchemaInitializer;
import org.eclipse.che.inject.DynaModule;
import org.eclipse.che.mail.template.ST.STTemplateProcessorImpl;
import org.eclipse.che.mail.template.TemplateProcessor;
import org.eclipse.che.multiuser.api.permission.server.AdminPermissionInitializer;
import org.eclipse.che.multiuser.api.permission.server.PermissionChecker;
import org.eclipse.che.multiuser.api.permission.server.PermissionCheckerImpl;
import org.eclipse.che.multiuser.api.workspace.activity.MultiUserWorkspaceActivityModule;
import org.eclipse.che.multiuser.keycloak.server.deploy.KeycloakModule;
import org.eclipse.che.multiuser.machine.authentication.server.MachineAuthModule;
import org.eclipse.che.multiuser.organization.api.OrganizationApiModule;
import org.eclipse.che.multiuser.organization.api.OrganizationJpaModule;
import org.eclipse.che.multiuser.permission.user.UserServicePermissionsFilter;
import org.eclipse.che.multiuser.resource.api.ResourceModule;
import org.eclipse.che.plugin.github.factory.resolver.GithubFactoryParametersResolver;
import org.eclipse.che.security.PBKDF2PasswordEncryptor;
import org.eclipse.che.security.PasswordEncryptor;
import org.eclipse.che.workspace.infrastructure.docker.DockerInfraModule;
import org.eclipse.che.workspace.infrastructure.docker.local.LocalDockerModule;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesInfraModule;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesInfrastructure;
import org.eclipse.che.workspace.infrastructure.openshift.OpenShiftInfraModule;
import org.eclipse.che.workspace.infrastructure.openshift.OpenShiftInfrastructure;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.flywaydb.core.internal.util.PlaceholderReplacer;

/** @author andrew00x */
@DynaModule
public class WsMasterModule extends AbstractModule {

  @Override
  protected void configure() {
    // db related components modules
    install(new org.eclipse.che.account.api.AccountModule());
    install(new org.eclipse.che.api.ssh.server.jpa.SshJpaModule());
    install(new org.eclipse.che.api.core.jsonrpc.impl.JsonRpcModule());
    install(new org.eclipse.che.api.core.websocket.impl.WebSocketModule());

    // db configuration
    bind(SchemaInitializer.class)
        .to(org.eclipse.che.core.db.schema.impl.flyway.FlywaySchemaInitializer.class);
    bind(org.eclipse.che.core.db.DBInitializer.class).asEagerSingleton();
    bind(PlaceholderReplacer.class)
        .toProvider(org.eclipse.che.core.db.schema.impl.flyway.PlaceholderReplacerProvider.class);

    // factory
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
    bind(WorkspaceLockService.class).to(DefaultWorkspaceLockService.class);
    bind(WorkspaceStatusCache.class).to(DefaultWorkspaceStatusCache.class);
    install(new FactoryModuleBuilder().build(ServersCheckerFactory.class));
    install(new FactoryModuleBuilder().build(ExecAgentClientFactory.class));
    bind(org.eclipse.che.api.logger.LoggerService.class);

    Multibinder<InternalEnvironmentProvisioner> internalEnvironmentProvisioners =
        Multibinder.newSetBinder(binder(), InternalEnvironmentProvisioner.class);
    internalEnvironmentProvisioners.addBinding().to(InstallerConfigProvisioner.class);
    internalEnvironmentProvisioners.addBinding().to(EnvVarEnvironmentProvisioner.class);
    internalEnvironmentProvisioners.addBinding().to(ProjectsVolumeForWsAgentProvisioner.class);

    Multibinder<EnvVarProvider> envVarProviders =
        Multibinder.newSetBinder(binder(), EnvVarProvider.class);
    envVarProviders.addBinding().to(CheApiEnvVarProvider.class);
    envVarProviders.addBinding().to(CheApiInternalEnvVarProvider.class);
    envVarProviders.addBinding().to(CheApiExternalEnvVarProvider.class);
    envVarProviders.addBinding().to(MachineTokenEnvVarProvider.class);
    envVarProviders.addBinding().to(WorkspaceIdEnvVarProvider.class);

    envVarProviders.addBinding().to(JavaOptsEnvVariableProvider.class);
    envVarProviders.addBinding().to(MavenOptsEnvVariableProvider.class);
    envVarProviders.addBinding().to(ProjectsRootEnvVariableProvider.class);
    envVarProviders.addBinding().to(AgentAuthEnableEnvVarProvider.class);
    envVarProviders.addBinding().to(WorkspaceAgentJavaOptsEnvVariableProvider.class);
    envVarProviders.addBinding().to(WorkspaceMavenServerJavaOptsEnvVariableProvider.class);

    bind(org.eclipse.che.api.workspace.server.bootstrap.InstallerService.class);
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
    bind(org.eclipse.che.api.workspace.server.event.InstallerStatusJsonRpcMessenger.class)
        .asEagerSingleton();
    bind(org.eclipse.che.api.workspace.server.event.InstallerLogJsonRpcMessenger.class)
        .asEagerSingleton();
    bind(org.eclipse.che.api.workspace.server.event.MachineLogJsonRpcMessenger.class)
        .asEagerSingleton();

    bind(org.eclipse.che.security.oauth.OAuthAuthenticatorProvider.class)
        .to(org.eclipse.che.security.oauth.OAuthAuthenticatorProviderImpl.class);

    // installers
    install(new InstallerModule());
    binder().bind(new TypeLiteral<Set<Installer>>() {}).toProvider(InstallersProvider.class);

    bind(org.eclipse.che.api.deploy.WsMasterAnalyticsAddresser.class);

    install(new org.eclipse.che.api.core.rest.CoreRestModule());
    install(new org.eclipse.che.api.core.util.FileCleaner.FileCleanerModule());
    install(new org.eclipse.che.swagger.deploy.DocsModule());
    install(new org.eclipse.che.commons.schedule.executor.ScheduleModule());

    final Multibinder<MessageBodyAdapter> adaptersMultibinder =
        Multibinder.newSetBinder(binder(), MessageBodyAdapter.class);

    final MessageBodyAdapterInterceptor interceptor = new MessageBodyAdapterInterceptor();
    requestInjection(interceptor);
    bindInterceptor(subclassesOf(CheJsonProvider.class), names("readFrom"), interceptor);

    // system components
    install(new SystemModule());
    Multibinder.newSetBinder(binder(), ServiceTermination.class)
        .addBinding()
        .to(org.eclipse.che.api.workspace.server.WorkspaceServiceTermination.class);

    final Map<String, String> persistenceProperties = new HashMap<>();
    persistenceProperties.put(PersistenceUnitProperties.TARGET_SERVER, "None");
    persistenceProperties.put(PersistenceUnitProperties.LOGGING_LOGGER, "DefaultLogger");
    persistenceProperties.put(PersistenceUnitProperties.LOGGING_LEVEL, "SEVERE");
    persistenceProperties.put(
        PersistenceUnitProperties.NON_JTA_DATASOURCE, "java:/comp/env/jdbc/che");
    bindConstant().annotatedWith(Names.named("jndi.datasource.name")).to("java:/comp/env/jdbc/che");

    String infrastructure = System.getenv("CHE_INFRASTRUCTURE_ACTIVE");
    if (Boolean.valueOf(System.getenv("CHE_MULTIUSER"))) {
      configureMultiUserMode(persistenceProperties, infrastructure);
    } else {
      configureSingleUserMode(persistenceProperties);
    }

    install(
        new com.google.inject.persist.jpa.JpaPersistModule("main")
            .properties(persistenceProperties));

    if (OpenShiftInfrastructure.NAME.equals(infrastructure)) {
      install(new OpenShiftInfraModule());
    } else if (KubernetesInfrastructure.NAME.equals(infrastructure)) {
      install(new KubernetesInfraModule());
    } else {
      install(new LocalDockerModule());
      install(new DockerInfraModule());
    }

    bind(org.eclipse.che.api.user.server.AppStatesPreferenceCleaner.class);
  }

  private void configureSingleUserMode(Map<String, String> persistenceProperties) {
    persistenceProperties.put(
        PersistenceUnitProperties.EXCEPTION_HANDLER_CLASS,
        "org.eclipse.che.core.db.h2.jpa.eclipselink.H2ExceptionHandler");
    bind(TokenValidator.class).to(org.eclipse.che.api.local.DummyTokenValidator.class);
    bind(MachineTokenProvider.class).to(MachineTokenProvider.EmptyMachineTokenProvider.class);

    bind(org.eclipse.che.api.workspace.server.stack.StackLoader.class);
    bind(DataSource.class).toProvider(org.eclipse.che.core.db.h2.H2DataSourceProvider.class);

    install(new org.eclipse.che.api.user.server.jpa.UserJpaModule());
    install(new org.eclipse.che.api.workspace.server.jpa.WorkspaceJpaModule());

    bind(org.eclipse.che.api.user.server.CheUserCreator.class);

    bindConstant().annotatedWith(Names.named("che.agents.auth_enabled")).to(false);

    bind(org.eclipse.che.security.oauth.shared.OAuthTokenProvider.class)
        .to(org.eclipse.che.security.oauth.OAuthAuthenticatorTokenProvider.class);
    bind(org.eclipse.che.security.oauth.OAuthAuthenticationService.class);
    bind(RemoteSubscriptionStorage.class).to(InmemoryRemoteSubscriptionStorage.class);

    install(new org.eclipse.che.api.workspace.activity.inject.WorkspaceActivityModule());
  }

  private void configureMultiUserMode(
      Map<String, String> persistenceProperties, String infrastructure) {
    if (OpenShiftInfrastructure.NAME.equals(infrastructure)
        || KubernetesInfrastructure.NAME.equals(infrastructure)) {
      // Replication is disabled until closing JPA JChannel issue won't be fixed
      // install(new ReplicationModule(persistenceProperties));
      bind(RemoteSubscriptionStorage.class).to(InmemoryRemoteSubscriptionStorage.class);
    } else {
      bind(RemoteSubscriptionStorage.class).to(InmemoryRemoteSubscriptionStorage.class);
    }
    persistenceProperties.put(
        PersistenceUnitProperties.EXCEPTION_HANDLER_CLASS,
        "org.eclipse.che.core.db.postgresql.jpa.eclipselink.PostgreSqlExceptionHandler");

    bind(TemplateProcessor.class).to(STTemplateProcessorImpl.class);
    bind(DataSource.class).toProvider(org.eclipse.che.core.db.JndiDataSourceProvider.class);

    install(new org.eclipse.che.multiuser.api.permission.server.jpa.SystemPermissionsJpaModule());
    install(new org.eclipse.che.multiuser.api.permission.server.PermissionsModule());
    install(
        new org.eclipse.che.multiuser.permission.workspace.server.WorkspaceApiPermissionsModule());
    install(
        new org.eclipse.che.multiuser.permission.workspace.server.jpa
            .MultiuserWorkspaceJpaModule());
    install(new MultiUserWorkspaceActivityModule());

    // Permission filters
    bind(org.eclipse.che.multiuser.permission.system.SystemServicePermissionsFilter.class);

    Multibinder<String> binder =
        Multibinder.newSetBinder(binder(), String.class, Names.named(SYSTEM_DOMAIN_ACTIONS));
    binder.addBinding().toInstance(UserServicePermissionsFilter.MANAGE_USERS_ACTION);
    bind(org.eclipse.che.multiuser.permission.user.UserProfileServicePermissionsFilter.class);
    bind(org.eclipse.che.multiuser.permission.user.UserServicePermissionsFilter.class);
    bind(org.eclipse.che.multiuser.permission.logger.LoggerServicePermissionsFilter.class);

    bind(org.eclipse.che.multiuser.permission.factory.FactoryPermissionsFilter.class);
    bind(
        org.eclipse.che.multiuser.permission.installer.InstallerRegistryServicePermissionsFilter
            .class);
    bind(org.eclipse.che.multiuser.permission.workspace.activity.ActivityPermissionsFilter.class);
    bind(AdminPermissionInitializer.class).asEagerSingleton();
    bind(
        org.eclipse.che.multiuser.permission.resource.filters.ResourceServicePermissionsFilter
            .class);
    bind(
        org.eclipse.che.multiuser.permission.resource.filters
            .FreeResourcesLimitServicePermissionsFilter.class);

    install(new ResourceModule());
    install(new OrganizationApiModule());
    install(new OrganizationJpaModule());

    install(new KeycloakModule());

    install(new MachineAuthModule());
    bind(RequestTokenExtractor.class).to(ChainedTokenExtractor.class);

    // User and profile - use profile from keycloak and other stuff is JPA
    bind(PasswordEncryptor.class).to(PBKDF2PasswordEncryptor.class);
    bind(UserDao.class).to(JpaUserDao.class);
    bind(PreferenceDao.class).to(JpaPreferenceDao.class);
    bind(PermissionChecker.class).to(PermissionCheckerImpl.class);

    bindConstant().annotatedWith(Names.named("che.agents.auth_enabled")).to(true);
  }
}
