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
package org.eclipse.che.selenium.core;

import static java.lang.Boolean.parseBoolean;
import static java.lang.String.format;
import static org.eclipse.che.selenium.core.utils.PlatformUtils.isMac;
import static org.eclipse.che.selenium.core.workspace.WorkspaceTemplate.DEFAULT;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Names;
import javax.inject.Named;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.selenium.core.action.ActionsFactory;
import org.eclipse.che.selenium.core.action.GenericActionsFactory;
import org.eclipse.che.selenium.core.action.MacOSActionsFactory;
import org.eclipse.che.selenium.core.client.CheTestUserServiceClient;
import org.eclipse.che.selenium.core.client.TestOrganizationServiceClient;
import org.eclipse.che.selenium.core.client.TestUserServiceClient;
import org.eclipse.che.selenium.core.client.TestUserServiceClientFactory;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClientFactory;
import org.eclipse.che.selenium.core.configuration.SeleniumTestConfiguration;
import org.eclipse.che.selenium.core.configuration.TestConfiguration;
import org.eclipse.che.selenium.core.pageobject.PageObjectsInjector;
import org.eclipse.che.selenium.core.provider.CheTestApiEndpointUrlProvider;
import org.eclipse.che.selenium.core.provider.CheTestDashboardUrlProvider;
import org.eclipse.che.selenium.core.provider.CheTestIdeUrlProvider;
import org.eclipse.che.selenium.core.provider.CheTestOfflineToAccessTokenExchangeApiEndpointUrlProvider;
import org.eclipse.che.selenium.core.provider.CheTestWorkspaceAgentApiEndpointUrlProvider;
import org.eclipse.che.selenium.core.provider.TestApiEndpointUrlProvider;
import org.eclipse.che.selenium.core.provider.TestDashboardUrlProvider;
import org.eclipse.che.selenium.core.provider.TestIdeUrlProvider;
import org.eclipse.che.selenium.core.provider.TestOfflineToAccessTokenExchangeApiEndpointUrlProvider;
import org.eclipse.che.selenium.core.provider.TestWorkspaceAgentApiEndpointUrlProvider;
import org.eclipse.che.selenium.core.requestfactory.CheTestAdminHttpJsonRequestFactory;
import org.eclipse.che.selenium.core.requestfactory.CheTestDefaultUserHttpJsonRequestFactory;
import org.eclipse.che.selenium.core.requestfactory.TestUserHttpJsonRequestFactory;
import org.eclipse.che.selenium.core.requestfactory.TestUserHttpJsonRequestFactoryCreator;
import org.eclipse.che.selenium.core.user.CheDefaultTestUser;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.core.user.TestUserFactory;
import org.eclipse.che.selenium.core.workspace.CheTestWorkspaceUrlResolver;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.core.workspace.TestWorkspaceProvider;
import org.eclipse.che.selenium.core.workspace.TestWorkspaceProviderImpl;
import org.eclipse.che.selenium.core.workspace.TestWorkspaceUrlResolver;
import org.eclipse.che.selenium.pageobject.PageObjectsInjectorImpl;

/**
 * Guice module per suite.
 *
 * @author Anatolii Bazko
 */
public class CheSeleniumSuiteModule extends AbstractModule {

  private static final String CHE_MULTIUSER_VARIABLE = "CHE_MULTIUSER";
  private static final String CHE_INFRASTRUCTURE_VARIABLE = "CHE_INFRASTRUCTURE";
  private static final String DOCKER_INFRASTRUCTURE = "docker";
  private static final String OPENSHIFT_INFRASTRUCTURE = "openshift";

  @Override
  public void configure() {
    TestConfiguration config = new SeleniumTestConfiguration();
    config
        .getMap()
        .forEach((key, value) -> bindConstant().annotatedWith(Names.named(key)).to(value));

    bind(TestUser.class).to(CheDefaultTestUser.class);

    bind(TestUserServiceClient.class).to(CheTestUserServiceClient.class);

    bind(HttpJsonRequestFactory.class).to(TestUserHttpJsonRequestFactory.class);
    bind(TestUserHttpJsonRequestFactory.class).to(CheTestDefaultUserHttpJsonRequestFactory.class);

    bind(TestApiEndpointUrlProvider.class).to(CheTestApiEndpointUrlProvider.class);
    bind(TestIdeUrlProvider.class).to(CheTestIdeUrlProvider.class);
    bind(TestDashboardUrlProvider.class).to(CheTestDashboardUrlProvider.class);
    bind(TestOfflineToAccessTokenExchangeApiEndpointUrlProvider.class)
        .to(CheTestOfflineToAccessTokenExchangeApiEndpointUrlProvider.class);
    bind(TestWorkspaceAgentApiEndpointUrlProvider.class)
        .to(CheTestWorkspaceAgentApiEndpointUrlProvider.class);

    bind(TestWorkspaceProvider.class).to(TestWorkspaceProviderImpl.class).asEagerSingleton();
    bind(TestWorkspaceUrlResolver.class).to(CheTestWorkspaceUrlResolver.class);

    install(new FactoryModuleBuilder().build(TestUserHttpJsonRequestFactoryCreator.class));
    install(new FactoryModuleBuilder().build(TestWorkspaceServiceClientFactory.class));
    install(new FactoryModuleBuilder().build(TestUserFactory.class));
    install(new FactoryModuleBuilder().build(TestUserServiceClientFactory.class));

    bind(PageObjectsInjector.class).to(PageObjectsInjectorImpl.class);

    if (parseBoolean(System.getenv(CHE_MULTIUSER_VARIABLE))) {
      install(new CheSeleniumMultiUserModule());
    } else {
      install(new CheSeleniumSingleUserModule());
    }

    String cheInfrastructure = System.getenv(CHE_INFRASTRUCTURE_VARIABLE);
    if (cheInfrastructure == null || cheInfrastructure.isEmpty()) {
      throw new RuntimeException(
          format(
              "Che infrastructure should be defined by environment variable '%s'.",
              CHE_INFRASTRUCTURE_VARIABLE));
    } else if (cheInfrastructure.equalsIgnoreCase(DOCKER_INFRASTRUCTURE)) {
      install(new CheSeleniumDockerModule());
    } else if (cheInfrastructure.equalsIgnoreCase(OPENSHIFT_INFRASTRUCTURE)) {
      install(new CheSeleniumOpenshiftModule());
    } else {
      throw new RuntimeException(
          format("Infrastructure '%s' hasn't been supported by tests.", cheInfrastructure));
    }
  }

  @Provides
  public TestWorkspace getWorkspace(
      TestWorkspaceProvider workspaceProvider,
      TestUser testUser,
      @Named("workspace.default_memory_gb") int defaultMemoryGb)
      throws Exception {
    TestWorkspace ws = workspaceProvider.createWorkspace(testUser, defaultMemoryGb, DEFAULT);
    ws.await();
    return ws;
  }

  @Provides
  @Named("admin")
  public TestOrganizationServiceClient getAdminOrganizationServiceClient(
      TestApiEndpointUrlProvider apiEndpointUrlProvider,
      CheTestAdminHttpJsonRequestFactory requestFactory) {
    return new TestOrganizationServiceClient(apiEndpointUrlProvider, requestFactory);
  }

  @Provides
  public ActionsFactory getActionFactory() {
    return isMac() ? new MacOSActionsFactory() : new GenericActionsFactory();
  }
}
