/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.impl.DefaultJwtParser;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.eclipse.che.api.core.notification.RemoteSubscriptionStorage;
import org.eclipse.che.api.core.rest.CheJsonProvider;
import org.eclipse.che.api.core.rest.MessageBodyAdapter;
import org.eclipse.che.api.core.rest.MessageBodyAdapterInterceptor;
import org.eclipse.che.api.deploy.jsonrpc.CheJsonRpcWebSocketConfigurationModule;
import org.eclipse.che.api.factory.server.FactoryAcceptValidator;
import org.eclipse.che.api.factory.server.FactoryCreateValidator;
import org.eclipse.che.api.factory.server.FactoryEditValidator;
import org.eclipse.che.api.factory.server.FactoryParametersResolver;
import org.eclipse.che.api.factory.server.bitbucket.BitbucketServerAuthorizingFactoryParametersResolver;
import org.eclipse.che.api.factory.server.github.GithubFactoryParametersResolver;
import org.eclipse.che.api.factory.server.gitlab.GitlabFactoryParametersResolver;
import org.eclipse.che.api.infraproxy.server.InfraProxyModule;
import org.eclipse.che.api.metrics.WsMasterMetricsModule;
import org.eclipse.che.api.system.server.ServiceTermination;
import org.eclipse.che.api.system.server.SystemModule;
import org.eclipse.che.api.user.server.TokenValidator;
import org.eclipse.che.api.user.server.jpa.JpaPreferenceDao;
import org.eclipse.che.api.user.server.jpa.JpaUserDao;
import org.eclipse.che.api.user.server.spi.NoopProfileDao;
import org.eclipse.che.api.user.server.spi.PreferenceDao;
import org.eclipse.che.api.user.server.spi.ProfileDao;
import org.eclipse.che.api.user.server.spi.UserDao;
import org.eclipse.che.api.workspace.server.WorkspaceEntityProvider;
import org.eclipse.che.api.workspace.server.WorkspaceLockService;
import org.eclipse.che.api.workspace.server.WorkspaceStatusCache;
import org.eclipse.che.api.workspace.server.devfile.DevfileModule;
import org.eclipse.che.api.workspace.server.hc.ServersCheckerFactory;
import org.eclipse.che.api.workspace.server.spi.provision.InternalEnvironmentProvisioner;
import org.eclipse.che.api.workspace.server.spi.provision.MachineNameProvisioner;
import org.eclipse.che.api.workspace.server.spi.provision.env.AgentAuthEnableEnvVarProvider;
import org.eclipse.che.api.workspace.server.spi.provision.env.CheApiEnvVarProvider;
import org.eclipse.che.api.workspace.server.spi.provision.env.CheApiExternalEnvVarProvider;
import org.eclipse.che.api.workspace.server.spi.provision.env.CheApiInternalEnvVarProvider;
import org.eclipse.che.api.workspace.server.spi.provision.env.EnvVarEnvironmentProvisioner;
import org.eclipse.che.api.workspace.server.spi.provision.env.EnvVarProvider;
import org.eclipse.che.api.workspace.server.spi.provision.env.JavaOptsEnvVariableProvider;
import org.eclipse.che.api.workspace.server.spi.provision.env.LegacyEnvVarProvider;
import org.eclipse.che.api.workspace.server.spi.provision.env.MachineTokenEnvVarProvider;
import org.eclipse.che.api.workspace.server.spi.provision.env.MavenOptsEnvVariableProvider;
import org.eclipse.che.api.workspace.server.spi.provision.env.ProjectsRootEnvVariableProvider;
import org.eclipse.che.api.workspace.server.spi.provision.env.WorkspaceIdEnvVarProvider;
import org.eclipse.che.api.workspace.server.spi.provision.env.WorkspaceNameEnvVarProvider;
import org.eclipse.che.api.workspace.server.spi.provision.env.WorkspaceNamespaceNameEnvVarProvider;
import org.eclipse.che.api.workspace.server.token.MachineTokenProvider;
import org.eclipse.che.api.workspace.server.wsplugins.ChePluginsApplier;
import org.eclipse.che.commons.observability.deploy.ExecutorWrapperModule;
import org.eclipse.che.core.db.DBTermination;
import org.eclipse.che.core.db.schema.SchemaInitializer;
import org.eclipse.che.core.tracing.metrics.TracingMetricsModule;
import org.eclipse.che.inject.DynaModule;
import org.eclipse.che.multiuser.api.authentication.commons.token.ChainedTokenExtractor;
import org.eclipse.che.multiuser.api.authentication.commons.token.HeaderRequestTokenExtractor;
import org.eclipse.che.multiuser.api.authentication.commons.token.RequestTokenExtractor;
import org.eclipse.che.multiuser.api.permission.server.AdminPermissionInitializer;
import org.eclipse.che.multiuser.api.permission.server.PermissionChecker;
import org.eclipse.che.multiuser.api.permission.server.PermissionCheckerImpl;
import org.eclipse.che.multiuser.api.workspace.activity.MultiUserWorkspaceActivityModule;
import org.eclipse.che.multiuser.keycloak.server.deploy.KeycloakModule;
import org.eclipse.che.multiuser.keycloak.server.deploy.KeycloakUserRemoverModule;
import org.eclipse.che.multiuser.machine.authentication.server.MachineAuthModule;
import org.eclipse.che.multiuser.organization.api.OrganizationApiModule;
import org.eclipse.che.multiuser.organization.api.OrganizationJpaModule;
import org.eclipse.che.multiuser.permission.user.UserServicePermissionsFilter;
import org.eclipse.che.multiuser.resource.api.ResourceModule;
import org.eclipse.che.security.PBKDF2PasswordEncryptor;
import org.eclipse.che.security.PasswordEncryptor;
import org.eclipse.che.security.oauth.EmbeddedOAuthAPI;
import org.eclipse.che.security.oauth.OAuthAPI;
import org.eclipse.che.security.oauth.OpenShiftOAuthModule;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesInfraModule;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesInfrastructure;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.secure.SecureServerExposer;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.secure.SecureServerExposerFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.secure.jwtproxy.PassThroughProxySecureServerExposer;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.secure.jwtproxy.factory.JwtProxyConfigBuilderFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.secure.jwtproxy.factory.JwtProxyProvisionerFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.secure.jwtproxy.factory.JwtProxySecureServerExposerFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.secure.jwtproxy.factory.PassThroughProxyProvisionerFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.secure.jwtproxy.factory.PassThroughProxySecureServerExposerFactory;
import org.eclipse.che.workspace.infrastructure.metrics.InfrastructureMetricsModule;
import org.eclipse.che.workspace.infrastructure.openshift.OpenShiftClientConfigFactory;
import org.eclipse.che.workspace.infrastructure.openshift.OpenShiftInfraModule;
import org.eclipse.che.workspace.infrastructure.openshift.OpenShiftInfrastructure;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironment;
import org.eclipse.che.workspace.infrastructure.openshift.multiuser.oauth.KeycloakProviderConfigFactory;
import org.eclipse.che.workspace.infrastructure.openshift.multiuser.oauth.OpenshiftProviderConfigFactory;
import org.eclipse.che.workspace.infrastructure.openshift.multiuser.oauth.OpenshiftUserDao;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.flywaydb.core.internal.util.PlaceholderReplacer;

/** @author andrew00x */
@DynaModule
public class WsMasterModule extends AbstractModule {

  @Override
  protected void configure() {
    // Workaround for https://github.com/fabric8io/kubernetes-client/issues/2212
    // OkHttp wrongly detects JDK8u251 and higher as JDK9 which enables Http2 unsupported for JDK8.
    // Can be removed after upgrade to Fabric8 4.10.2 or higher or to Java 11
    if (System.getProperty("java.version", "").startsWith("1.8")) {
      System.setProperty("http2.disable", "true");
    }

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

    // Service-specific factory resolvers.
    Multibinder<FactoryParametersResolver> factoryParametersResolverMultibinder =
        Multibinder.newSetBinder(binder(), FactoryParametersResolver.class);
    factoryParametersResolverMultibinder.addBinding().to(GithubFactoryParametersResolver.class);
    factoryParametersResolverMultibinder
        .addBinding()
        .to(BitbucketServerAuthorizingFactoryParametersResolver.class);
    factoryParametersResolverMultibinder.addBinding().to(GitlabFactoryParametersResolver.class);

    install(new org.eclipse.che.api.factory.server.scm.KubernetesScmModule());
    install(new org.eclipse.che.api.factory.server.bitbucket.BitbucketServerModule());
    install(new org.eclipse.che.api.factory.server.gitlab.GitlabModule());

    bind(org.eclipse.che.api.core.rest.ApiInfoService.class);
    bind(org.eclipse.che.api.ssh.server.SshService.class);
    bind(org.eclipse.che.api.user.server.UserService.class);
    bind(org.eclipse.che.api.user.server.ProfileService.class);
    bind(org.eclipse.che.api.user.server.PreferencesService.class);
    bind(org.eclipse.che.security.oauth.OAuthAuthenticationService.class);
    bind(org.eclipse.che.security.oauth1.OAuthAuthenticationService.class);

    install(new DevfileModule());

    bind(WorkspaceEntityProvider.class);
    bind(org.eclipse.che.api.workspace.server.TemporaryWorkspaceRemover.class);
    bind(org.eclipse.che.api.workspace.server.WorkspaceService.class);
    bind(org.eclipse.che.api.devfile.server.DevfileService.class);
    bind(org.eclipse.che.api.devfile.server.UserDevfileEntityProvider.class);
    install(new FactoryModuleBuilder().build(ServersCheckerFactory.class));

    Multibinder<InternalEnvironmentProvisioner> internalEnvironmentProvisioners =
        Multibinder.newSetBinder(binder(), InternalEnvironmentProvisioner.class);
    internalEnvironmentProvisioners.addBinding().to(EnvVarEnvironmentProvisioner.class);
    internalEnvironmentProvisioners.addBinding().to(MachineNameProvisioner.class);

    Multibinder<EnvVarProvider> envVarProviders =
        Multibinder.newSetBinder(binder(), EnvVarProvider.class);
    envVarProviders.addBinding().to(CheApiEnvVarProvider.class);
    envVarProviders.addBinding().to(CheApiInternalEnvVarProvider.class);
    envVarProviders.addBinding().to(CheApiExternalEnvVarProvider.class);
    envVarProviders.addBinding().to(MachineTokenEnvVarProvider.class);
    envVarProviders.addBinding().to(WorkspaceIdEnvVarProvider.class);
    envVarProviders.addBinding().to(WorkspaceNamespaceNameEnvVarProvider.class);
    envVarProviders.addBinding().to(WorkspaceNameEnvVarProvider.class);
    envVarProviders.addBinding().to(ProjectsRootEnvVariableProvider.class);

    Multibinder<LegacyEnvVarProvider> legacyEnvVarProviderMultibinders =
        Multibinder.newSetBinder(binder(), LegacyEnvVarProvider.class);
    legacyEnvVarProviderMultibinders.addBinding().to(JavaOptsEnvVariableProvider.class);
    legacyEnvVarProviderMultibinders.addBinding().to(MavenOptsEnvVariableProvider.class);

    legacyEnvVarProviderMultibinders.addBinding().to(AgentAuthEnableEnvVarProvider.class);
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
    bind(org.eclipse.che.api.workspace.server.event.RuntimeLogJsonRpcMessenger.class)
        .asEagerSingleton();

    bind(org.eclipse.che.security.oauth.OAuthAuthenticatorProvider.class)
        .to(org.eclipse.che.security.oauth.OAuthAuthenticatorProviderImpl.class);

    install(new org.eclipse.che.api.core.rest.CoreRestModule());
    install(new org.eclipse.che.api.core.util.FileCleaner.FileCleanerModule());
    install(new org.eclipse.che.swagger.deploy.DocsModule());
    install(new org.eclipse.che.commons.schedule.executor.ScheduleModule());
    install(new org.eclipse.che.api.logger.deploy.LoggerModule());

    final Multibinder<MessageBodyAdapter> adaptersMultibinder =
        Multibinder.newSetBinder(binder(), MessageBodyAdapter.class);

    final MessageBodyAdapterInterceptor interceptor = new MessageBodyAdapterInterceptor();
    requestInjection(interceptor);
    bindInterceptor(subclassesOf(CheJsonProvider.class), names("readFrom"), interceptor);

    // system components
    install(new SystemModule());
    Multibinder<ServiceTermination> terminationMultiBinder =
        Multibinder.newSetBinder(binder(), ServiceTermination.class);
    terminationMultiBinder
        .addBinding()
        .to(org.eclipse.che.api.workspace.server.WorkspaceServiceTermination.class);
    terminationMultiBinder
        .addBinding()
        .to(org.eclipse.che.api.system.server.CronThreadPullTermination.class);
    terminationMultiBinder
        .addBinding()
        .to(org.eclipse.che.api.workspace.server.hc.probe.ProbeSchedulerTermination.class);
    bind(DBTermination.class);

    final Map<String, String> persistenceProperties = new HashMap<>();
    persistenceProperties.put(PersistenceUnitProperties.TARGET_SERVER, "None");
    persistenceProperties.put(PersistenceUnitProperties.LOGGING_LOGGER, "DefaultLogger");
    persistenceProperties.put(PersistenceUnitProperties.LOGGING_LEVEL, "SEVERE");
    persistenceProperties.put(
        PersistenceUnitProperties.NON_JTA_DATASOURCE, "java:/comp/env/jdbc/che");
    bindConstant().annotatedWith(Names.named("jndi.datasource.name")).to("java:/comp/env/jdbc/che");

    String infrastructure = System.getenv("CHE_INFRASTRUCTURE_ACTIVE");

    install(new FactoryModuleBuilder().build(JwtProxyConfigBuilderFactory.class));
    install(new FactoryModuleBuilder().build(PassThroughProxyProvisionerFactory.class));
    installDefaultSecureServerExposer(infrastructure);
    install(new org.eclipse.che.security.oauth1.BitbucketModule());

    if (Boolean.parseBoolean(System.getenv("CHE_MULTIUSER"))) {
      configureMultiUserMode(persistenceProperties, infrastructure);
    } else {
      configureSingleUserMode(persistenceProperties, infrastructure);
    }

    install(
        new com.google.inject.persist.jpa.JpaPersistModule("main")
            .properties(persistenceProperties));

    if (OpenShiftInfrastructure.NAME.equals(infrastructure)) {
      install(new OpenShiftInfraModule());
    } else if (KubernetesInfrastructure.NAME.equals(infrastructure)) {
      install(new KubernetesInfraModule());
    }
    install(new CheJsonRpcWebSocketConfigurationModule());

    bind(org.eclipse.che.api.user.server.AppStatesPreferenceCleaner.class);
    MapBinder.newMapBinder(binder(), String.class, ChePluginsApplier.class);

    if (Boolean.valueOf(System.getenv("CHE_TRACING_ENABLED"))) {
      install(new org.eclipse.che.core.tracing.TracingModule());
    } else {
      install(new org.eclipse.che.core.tracing.NopTracingModule());
    }
    if (Boolean.valueOf(System.getenv("CHE_METRICS_ENABLED"))) {
      install(new org.eclipse.che.core.metrics.MetricsModule());
      install(new WsMasterMetricsModule());
      install(new InfrastructureMetricsModule());
    } else {
      install(new org.eclipse.che.core.metrics.NoopMetricsModule());
    }
    if (Boolean.valueOf(System.getenv("CHE_TRACING_ENABLED"))
        && Boolean.valueOf(System.getenv("CHE_METRICS_ENABLED"))) {
      install(new TracingMetricsModule());
    }
    install(new ExecutorWrapperModule());

    install(new OpenShiftOAuthModule());
  }

  private void configureSingleUserMode(
      Map<String, String> persistenceProperties, String infrastructure) {
    persistenceProperties.put(
        PersistenceUnitProperties.EXCEPTION_HANDLER_CLASS,
        "org.eclipse.che.core.db.h2.jpa.eclipselink.H2ExceptionHandler");
    bind(TokenValidator.class).to(org.eclipse.che.api.local.DummyTokenValidator.class);
    bind(MachineTokenProvider.class).to(MachineTokenProvider.EmptyMachineTokenProvider.class);

    bind(DataSource.class).toProvider(org.eclipse.che.core.db.h2.H2DataSourceProvider.class);

    install(new org.eclipse.che.api.user.server.jpa.UserJpaModule());
    install(new org.eclipse.che.api.workspace.server.jpa.WorkspaceJpaModule());
    install(new org.eclipse.che.api.devfile.server.jpa.UserDevfileJpaModule());

    bind(org.eclipse.che.api.user.server.CheUserCreator.class);

    bindConstant().annotatedWith(Names.named("che.agents.auth_enabled")).to(false);

    bind(org.eclipse.che.security.oauth.shared.OAuthTokenProvider.class)
        .to(org.eclipse.che.security.oauth.OAuthAuthenticatorTokenProvider.class);
    bind(OAuthAPI.class).to(EmbeddedOAuthAPI.class);

    bind(RemoteSubscriptionStorage.class)
        .to(org.eclipse.che.api.core.notification.InmemoryRemoteSubscriptionStorage.class);
    bind(WorkspaceLockService.class)
        .to(org.eclipse.che.api.workspace.server.DefaultWorkspaceLockService.class);
    bind(WorkspaceStatusCache.class)
        .to(org.eclipse.che.api.workspace.server.DefaultWorkspaceStatusCache.class);

    install(new org.eclipse.che.api.workspace.activity.inject.WorkspaceActivityModule());

    // In single user mode jwtproxy provisioner isn't actually bound at all, but since
    // it is the new default, we need to "fake it" by binding the passthrough provisioner
    // as the jwtproxy impl.
    configureImpostorJwtProxySecureProvisioner(infrastructure);
  }

  private void configureMultiUserMode(
      Map<String, String> persistenceProperties, String infrastructure) {
    if (OpenShiftInfrastructure.NAME.equals(infrastructure)
        || KubernetesInfrastructure.NAME.equals(infrastructure)) {
      install(new ReplicationModule(persistenceProperties));
      bind(
          org.eclipse.che.multiuser.permission.workspace.infra.kubernetes
              .BrokerServicePermissionFilter.class);
      configureJwtProxySecureProvisioner(infrastructure);
    } else {
      bind(RemoteSubscriptionStorage.class)
          .to(org.eclipse.che.api.core.notification.InmemoryRemoteSubscriptionStorage.class);
      bind(WorkspaceLockService.class)
          .to(org.eclipse.che.api.workspace.server.DefaultWorkspaceLockService.class);
      bind(WorkspaceStatusCache.class)
          .to(org.eclipse.che.api.workspace.server.DefaultWorkspaceStatusCache.class);
    }

    if (OpenShiftInfrastructure.NAME.equals(infrastructure)) {
      if (Boolean.parseBoolean(System.getenv("CHE_OPENSHIFTUSER"))) {
        bind(OpenShiftClientConfigFactory.class).to(OpenshiftProviderConfigFactory.class);
      } else {
        bind(OpenShiftClientConfigFactory.class).to(KeycloakProviderConfigFactory.class);
      }
    }

    persistenceProperties.put(
        PersistenceUnitProperties.EXCEPTION_HANDLER_CLASS,
        "org.eclipse.che.core.db.postgresql.jpa.eclipselink.PostgreSqlExceptionHandler");
    bind(DataSource.class).toProvider(org.eclipse.che.core.db.JndiDataSourceProvider.class);

    install(new org.eclipse.che.multiuser.api.permission.server.jpa.SystemPermissionsJpaModule());
    install(new org.eclipse.che.multiuser.api.permission.server.PermissionsModule());
    install(
        new org.eclipse.che.multiuser.permission.workspace.server.WorkspaceApiPermissionsModule());
    install(
        new org.eclipse.che.multiuser.permission.workspace.server.jpa
            .MultiuserWorkspaceJpaModule());
    install(new MultiUserWorkspaceActivityModule());
    install(
        new org.eclipse.che.multiuser.permission.devfile.server.jpa
            .MultiuserUserDevfileJpaModule());
    install(
        new org.eclipse.che.multiuser.permission.devfile.server.UserDevfileApiPermissionsModule());

    // Permission filters
    bind(org.eclipse.che.multiuser.permission.system.SystemServicePermissionsFilter.class);
    bind(org.eclipse.che.multiuser.permission.system.JvmServicePermissionsFilter.class);
    bind(
        org.eclipse.che.multiuser.permission.system.SystemEventsSubscriptionPermissionsCheck.class);

    Multibinder<String> binder =
        Multibinder.newSetBinder(binder(), String.class, Names.named(SYSTEM_DOMAIN_ACTIONS));
    binder.addBinding().toInstance(UserServicePermissionsFilter.MANAGE_USERS_ACTION);
    bind(org.eclipse.che.multiuser.permission.user.UserProfileServicePermissionsFilter.class);
    bind(org.eclipse.che.multiuser.permission.user.UserServicePermissionsFilter.class);
    bind(org.eclipse.che.multiuser.permission.logger.LoggerServicePermissionsFilter.class);

    bind(org.eclipse.che.multiuser.permission.workspace.activity.ActivityPermissionsFilter.class);

    if (!Boolean.parseBoolean(System.getenv("CHE_OPENSHIFTUSER"))) {
      bind(AdminPermissionInitializer.class).asEagerSingleton();
    }
    bind(
        org.eclipse.che.multiuser.permission.resource.filters.ResourceServicePermissionsFilter
            .class);
    bind(
        org.eclipse.che.multiuser.permission.resource.filters
            .FreeResourcesLimitServicePermissionsFilter.class);

    install(new ResourceModule());
    install(new OrganizationApiModule());
    install(new OrganizationJpaModule());

    if (Boolean.parseBoolean(System.getenv("CHE_OPENSHIFTUSER"))) {
      bind(TokenValidator.class).to(org.eclipse.che.api.local.DummyTokenValidator.class);
      bind(JwtParser.class).to(DefaultJwtParser.class);
      bind(ProfileDao.class).to(NoopProfileDao.class);
      bind(OAuthAPI.class).to(EmbeddedOAuthAPI.class);
      bind(RequestTokenExtractor.class).to(HeaderRequestTokenExtractor.class);
    } else {
      install(new KeycloakModule());
      install(new KeycloakUserRemoverModule());
      bind(RequestTokenExtractor.class).to(ChainedTokenExtractor.class);
    }

    install(new MachineAuthModule());

    // User and profile - use profile from keycloak and other stuff is JPA
    bind(PasswordEncryptor.class).to(PBKDF2PasswordEncryptor.class);
    if (Boolean.parseBoolean(System.getenv("CHE_OPENSHIFTUSER"))) {
      bind(UserDao.class).to(OpenshiftUserDao.class);
    } else {
      bind(UserDao.class).to(JpaUserDao.class);
    }
    bind(PreferenceDao.class).to(JpaPreferenceDao.class);
    bind(PermissionChecker.class).to(PermissionCheckerImpl.class);

    bindConstant().annotatedWith(Names.named("che.agents.auth_enabled")).to(true);

    if (OpenShiftInfrastructure.NAME.equals(infrastructure)) {
      install(new InfraProxyModule());
    }
  }

  private void configureJwtProxySecureProvisioner(String infrastructure) {
    install(new FactoryModuleBuilder().build(JwtProxyProvisionerFactory.class));
    if (KubernetesInfrastructure.NAME.equals(infrastructure)) {
      install(
          new FactoryModuleBuilder()
              .build(
                  new TypeLiteral<JwtProxySecureServerExposerFactory<KubernetesEnvironment>>() {}));
      MapBinder.newMapBinder(
              binder(),
              new TypeLiteral<String>() {},
              new TypeLiteral<SecureServerExposerFactory<KubernetesEnvironment>>() {})
          .addBinding("jwtproxy")
          .to(new TypeLiteral<JwtProxySecureServerExposerFactory<KubernetesEnvironment>>() {});
    } else {
      install(
          new FactoryModuleBuilder()
              .build(
                  new TypeLiteral<JwtProxySecureServerExposerFactory<OpenShiftEnvironment>>() {}));
      MapBinder.newMapBinder(
              binder(),
              new TypeLiteral<String>() {},
              new TypeLiteral<SecureServerExposerFactory<OpenShiftEnvironment>>() {})
          .addBinding("jwtproxy")
          .to(new TypeLiteral<JwtProxySecureServerExposerFactory<OpenShiftEnvironment>>() {});
    }
  }

  private void configureImpostorJwtProxySecureProvisioner(String infrastructure) {
    if (KubernetesInfrastructure.NAME.equals(infrastructure)) {
      MapBinder.newMapBinder(
              binder(),
              new TypeLiteral<String>() {},
              new TypeLiteral<SecureServerExposerFactory<KubernetesEnvironment>>() {})
          .addBinding("jwtproxy")
          .to(
              new TypeLiteral<
                  PassThroughProxySecureServerExposerFactory<KubernetesEnvironment>>() {});
    } else {
      MapBinder.newMapBinder(
              binder(),
              new TypeLiteral<String>() {},
              new TypeLiteral<SecureServerExposerFactory<OpenShiftEnvironment>>() {})
          .addBinding("jwtproxy")
          .to(
              new TypeLiteral<
                  PassThroughProxySecureServerExposerFactory<OpenShiftEnvironment>>() {});
    }
  }

  private void installDefaultSecureServerExposer(String infrastructure) {
    if (KubernetesInfrastructure.NAME.equals(infrastructure)) {
      MapBinder<String, SecureServerExposerFactory<KubernetesEnvironment>>
          secureServerExposerFactories =
              MapBinder.newMapBinder(binder(), new TypeLiteral<>() {}, new TypeLiteral<>() {});

      install(
          new FactoryModuleBuilder()
              .implement(
                  new TypeLiteral<SecureServerExposer<KubernetesEnvironment>>() {},
                  new TypeLiteral<PassThroughProxySecureServerExposer<KubernetesEnvironment>>() {})
              .build(
                  new TypeLiteral<
                      PassThroughProxySecureServerExposerFactory<KubernetesEnvironment>>() {}));

      secureServerExposerFactories
          .addBinding("default")
          .to(
              new TypeLiteral<
                  PassThroughProxySecureServerExposerFactory<KubernetesEnvironment>>() {});
    } else {
      MapBinder<String, SecureServerExposerFactory<OpenShiftEnvironment>>
          secureServerExposerFactories =
              MapBinder.newMapBinder(binder(), new TypeLiteral<>() {}, new TypeLiteral<>() {});

      install(
          new FactoryModuleBuilder()
              .implement(
                  new TypeLiteral<SecureServerExposer<OpenShiftEnvironment>>() {},
                  new TypeLiteral<PassThroughProxySecureServerExposer<OpenShiftEnvironment>>() {})
              .build(
                  new TypeLiteral<
                      PassThroughProxySecureServerExposerFactory<OpenShiftEnvironment>>() {}));

      secureServerExposerFactories
          .addBinding("default")
          .to(
              new TypeLiteral<
                  PassThroughProxySecureServerExposerFactory<OpenShiftEnvironment>>() {});
    }
  }
}
