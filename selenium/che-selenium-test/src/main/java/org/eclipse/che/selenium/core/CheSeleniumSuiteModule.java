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
package org.eclipse.che.selenium.core;

import static com.google.inject.name.Names.named;
import static java.lang.String.format;
import static org.eclipse.che.selenium.core.utils.PlatformUtils.isMac;
import static org.eclipse.che.selenium.core.workspace.WorkspaceTemplate.DEFAULT;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Named;
import java.io.IOException;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.selenium.core.action.ActionsFactory;
import org.eclipse.che.selenium.core.action.GenericActionsFactory;
import org.eclipse.che.selenium.core.action.MacOSActionsFactory;
import org.eclipse.che.selenium.core.client.CheTestUserServiceClient;
import org.eclipse.che.selenium.core.client.CheTestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.client.TestAuthServiceClient;
import org.eclipse.che.selenium.core.client.TestGitHubRepository;
import org.eclipse.che.selenium.core.client.TestUserServiceClient;
import org.eclipse.che.selenium.core.client.TestUserServiceClientFactory;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClientFactory;
import org.eclipse.che.selenium.core.client.keycloak.KeycloakTestAuthServiceClient;
import org.eclipse.che.selenium.core.client.keycloak.OsioKeycloakTestAuthServiceClient;
import org.eclipse.che.selenium.core.configuration.SeleniumTestConfiguration;
import org.eclipse.che.selenium.core.configuration.TestConfiguration;
import org.eclipse.che.selenium.core.constant.Infrastructure;
import org.eclipse.che.selenium.core.pageobject.PageObjectsInjector;
import org.eclipse.che.selenium.core.provider.CheTestApiEndpointUrlProvider;
import org.eclipse.che.selenium.core.provider.CheTestDashboardUrlProvider;
import org.eclipse.che.selenium.core.provider.CheTestIdeUrlProvider;
import org.eclipse.che.selenium.core.provider.CheTestWorkspaceAgentApiEndpointUrlProvider;
import org.eclipse.che.selenium.core.provider.DefaultTestUserProvider;
import org.eclipse.che.selenium.core.provider.TestApiEndpointUrlProvider;
import org.eclipse.che.selenium.core.provider.TestDashboardUrlProvider;
import org.eclipse.che.selenium.core.provider.TestIdeUrlProvider;
import org.eclipse.che.selenium.core.provider.TestWorkspaceAgentApiEndpointUrlProvider;
import org.eclipse.che.selenium.core.requestfactory.CheTestDefaultHttpJsonRequestFactory;
import org.eclipse.che.selenium.core.requestfactory.TestUserHttpJsonRequestFactory;
import org.eclipse.che.selenium.core.requestfactory.TestUserHttpJsonRequestFactoryCreator;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.user.TestUserFactory;
import org.eclipse.che.selenium.core.webdriver.log.WebDriverLogsReaderFactory;
import org.eclipse.che.selenium.core.workspace.CheTestWorkspaceProvider;
import org.eclipse.che.selenium.core.workspace.CheTestWorkspaceUrlResolver;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.core.workspace.TestWorkspaceProvider;
import org.eclipse.che.selenium.core.workspace.TestWorkspaceUrlResolver;
import org.eclipse.che.selenium.pageobject.PageObjectsInjectorImpl;

/**
 * Guice module per suite.
 *
 * @author Anatolii Bazko
 * @author Dmytro Nochevnov
 */
public class CheSeleniumSuiteModule extends AbstractModule {

  public static final String AUXILIARY = "auxiliary";

  private static final String CHE_MULTIUSER_VARIABLE = "che.multiuser";
  private static final String CHE_INFRASTRUCTURE_VARIABLE = "che.infrastructure";

  @Override
  public void configure() {
    TestConfiguration config = new SeleniumTestConfiguration();
    config.getMap().forEach((key, value) -> bindConstant().annotatedWith(named(key)).to(value));

    bind(DefaultTestUser.class).toProvider(DefaultTestUserProvider.class);
    install(
        new FactoryModuleBuilder()
            .build(Key.get(new TypeLiteral<TestUserFactory<DefaultTestUser>>() {}.getType())));

    bind(TestUserServiceClient.class).to(CheTestUserServiceClient.class);

    bind(HttpJsonRequestFactory.class).to(TestUserHttpJsonRequestFactory.class);
    bind(TestUserHttpJsonRequestFactory.class).to(CheTestDefaultHttpJsonRequestFactory.class);

    bind(TestApiEndpointUrlProvider.class).to(CheTestApiEndpointUrlProvider.class);
    bind(TestIdeUrlProvider.class).to(CheTestIdeUrlProvider.class);
    bind(TestDashboardUrlProvider.class).to(CheTestDashboardUrlProvider.class);
    bind(TestWorkspaceAgentApiEndpointUrlProvider.class)
        .to(CheTestWorkspaceAgentApiEndpointUrlProvider.class);

    bind(TestWorkspaceUrlResolver.class).to(CheTestWorkspaceUrlResolver.class);

    install(
        new FactoryModuleBuilder()
            .implement(TestWorkspaceServiceClient.class, CheTestWorkspaceServiceClient.class)
            .build(TestWorkspaceServiceClientFactory.class));

    bind(TestWorkspaceServiceClient.class).to(CheTestWorkspaceServiceClient.class);
    bind(TestWorkspaceProvider.class).to(CheTestWorkspaceProvider.class).asEagerSingleton();

    install(new FactoryModuleBuilder().build(TestUserHttpJsonRequestFactoryCreator.class));
    install(new FactoryModuleBuilder().build(TestUserServiceClientFactory.class));
    install(new FactoryModuleBuilder().build(WebDriverLogsReaderFactory.class));

    bind(PageObjectsInjector.class).to(PageObjectsInjectorImpl.class);

    configureInfrastructureRelatedDependencies(config);

    if (config.getBoolean(CHE_MULTIUSER_VARIABLE)) {
      install(new CheSeleniumMultiUserModule());
    } else {
      install(new CheSeleniumSingleUserModule());
    }

    install(new TestExecutionModule());
  }

  private void configureInfrastructureRelatedDependencies(TestConfiguration config) {
    final Infrastructure cheInfrastructure =
        Infrastructure.valueOf(config.getString(CHE_INFRASTRUCTURE_VARIABLE).toUpperCase());
    switch (cheInfrastructure) {
      case OPENSHIFT:
      case K8S:
        if (config.getBoolean(CHE_MULTIUSER_VARIABLE)) {
          bind(TestAuthServiceClient.class).to(KeycloakTestAuthServiceClient.class);
        }

        install(new CheSeleniumOpenShiftModule());
        break;

      case OSIO:
        if (config.getBoolean(CHE_MULTIUSER_VARIABLE)) {
          bind(TestAuthServiceClient.class).to(OsioKeycloakTestAuthServiceClient.class);
        }

        install(new CheSeleniumOpenShiftModule());
        break;

      case DOCKER:
        if (config.getBoolean(CHE_MULTIUSER_VARIABLE)) {
          bind(TestAuthServiceClient.class).to(KeycloakTestAuthServiceClient.class);
        }

        install(new CheSeleniumDockerModule());
        break;

      default:
        throw new RuntimeException(
            format("Infrastructure '%s' hasn't been supported by tests.", cheInfrastructure));
    }
  }

  @Provides
  public TestWorkspace getWorkspace(
      TestWorkspaceProvider workspaceProvider,
      DefaultTestUser testUser,
      @Named("workspace.default_memory_gb") int defaultMemoryGb)
      throws Exception {
    TestWorkspace ws = workspaceProvider.createWorkspace(testUser, defaultMemoryGb, DEFAULT, true);
    ws.await();
    return ws;
  }

  @Provides
  public ActionsFactory getActionFactory() {
    return isMac() ? new MacOSActionsFactory() : new GenericActionsFactory();
  }

  @Provides
  @Named(AUXILIARY)
  public TestGitHubRepository getTestGitHubRepository(
      @Named("github.auxiliary.username") String gitHubAuxiliaryUsername,
      @Named("github.auxiliary.password") String gitHubAuxiliaryPassword)
      throws IOException, InterruptedException {
    return new TestGitHubRepository(gitHubAuxiliaryUsername, gitHubAuxiliaryPassword);
  }
}
