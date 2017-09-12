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
package org.eclipse.che.selenium.core;

import static org.eclipse.che.selenium.core.utils.PlatformUtils.isMac;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.name.Names;
import javax.inject.Named;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.selenium.core.action.ActionsFactory;
import org.eclipse.che.selenium.core.action.GenericActionsFactory;
import org.eclipse.che.selenium.core.action.MacOSActionsFactory;
import org.eclipse.che.selenium.core.client.CheTestAuthServiceClient;
import org.eclipse.che.selenium.core.client.CheTestMachineServiceClient;
import org.eclipse.che.selenium.core.client.CheTestUserServiceClient;
import org.eclipse.che.selenium.core.client.KeycloakTestAuthServiceClient;
import org.eclipse.che.selenium.core.client.KeycloakTestUserServiceClient;
import org.eclipse.che.selenium.core.client.TestAuthServiceClient;
import org.eclipse.che.selenium.core.client.TestMachineServiceClient;
import org.eclipse.che.selenium.core.client.TestUserServiceClient;
import org.eclipse.che.selenium.core.configuration.SeleniumTestConfiguration;
import org.eclipse.che.selenium.core.configuration.TestConfiguration;
import org.eclipse.che.selenium.core.provider.CheTestApiEndpointUrlProvider;
import org.eclipse.che.selenium.core.provider.CheTestDashboardUrlProvider;
import org.eclipse.che.selenium.core.provider.CheTestIdeUrlProvider;
import org.eclipse.che.selenium.core.provider.CheTestKeycloakProvider;
import org.eclipse.che.selenium.core.provider.CheTestRealmProvider;
import org.eclipse.che.selenium.core.provider.CheTestSvnPasswordProvider;
import org.eclipse.che.selenium.core.provider.CheTestSvnRepo1Provider;
import org.eclipse.che.selenium.core.provider.CheTestSvnRepo2Provider;
import org.eclipse.che.selenium.core.provider.CheTestSvnUsernameProvider;
import org.eclipse.che.selenium.core.provider.TestApiEndpointUrlProvider;
import org.eclipse.che.selenium.core.provider.TestDashboardUrlProvider;
import org.eclipse.che.selenium.core.provider.TestIdeUrlProvider;
import org.eclipse.che.selenium.core.provider.TestKeycloakProvider;
import org.eclipse.che.selenium.core.provider.TestRealmProvider;
import org.eclipse.che.selenium.core.provider.TestSvnPasswordProvider;
import org.eclipse.che.selenium.core.provider.TestSvnRepo1Provider;
import org.eclipse.che.selenium.core.provider.TestSvnRepo2Provider;
import org.eclipse.che.selenium.core.provider.TestSvnUsernameProvider;
import org.eclipse.che.selenium.core.requestfactory.TestAdminHttpJsonRequestFactory;
import org.eclipse.che.selenium.core.requestfactory.TestDefaultUserHttpJsonRequestFactory;
import org.eclipse.che.selenium.core.user.AdminTestUser;
import org.eclipse.che.selenium.core.user.CheAdminTestUser;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.user.MultiUserCheTestUserNamespaceResolver;
import org.eclipse.che.selenium.core.user.SingleUserCheTestUserNamespaceResolver;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.core.user.TestUserImpl;
import org.eclipse.che.selenium.core.user.TestUserNamespaceResolver;
import org.eclipse.che.selenium.core.workspace.MultiUserCheTestWorkspaceUrlResolver;
import org.eclipse.che.selenium.core.workspace.SingleUserCheTestWorkspaceUrlResolver;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.core.workspace.TestWorkspaceProvider;
import org.eclipse.che.selenium.core.workspace.TestWorkspaceProviderImpl;
import org.eclipse.che.selenium.core.workspace.TestWorkspaceUrlResolver;
import org.eclipse.che.selenium.core.workspace.WorkspaceTemplate;

/**
 * Guice module per suite.
 *
 * @author Anatolii Bazko
 */
public class CheSeleniumSuiteModule extends AbstractModule {

  private static final String CHE_MULTIUSER = "che.multiuser";

  @Override
  public void configure() {
    TestConfiguration config = new SeleniumTestConfiguration();
    config
        .getMap()
        .forEach((key, value) -> bindConstant().annotatedWith(Names.named(key)).to(value));

    bind(TestSvnPasswordProvider.class).to(CheTestSvnPasswordProvider.class);
    bind(TestSvnUsernameProvider.class).to(CheTestSvnUsernameProvider.class);
    bind(TestSvnRepo1Provider.class).to(CheTestSvnRepo1Provider.class);
    bind(TestSvnRepo2Provider.class).to(CheTestSvnRepo2Provider.class);
    bind(TestApiEndpointUrlProvider.class).to(CheTestApiEndpointUrlProvider.class);
    bind(TestIdeUrlProvider.class).to(CheTestIdeUrlProvider.class);
    bind(TestDashboardUrlProvider.class).to(CheTestDashboardUrlProvider.class);

    bind(HttpJsonRequestFactory.class).to(TestDefaultUserHttpJsonRequestFactory.class);

    bind(TestMachineServiceClient.class).to(CheTestMachineServiceClient.class);

    bind(TestKeycloakProvider.class).to(CheTestKeycloakProvider.class);
    bind(TestRealmProvider.class).to(CheTestRealmProvider.class);
    bind(AdminTestUser.class).to(CheAdminTestUser.class);

    bind(TestUser.class).to(TestUserImpl.class);
    bind(TestWorkspaceProvider.class).to(TestWorkspaceProviderImpl.class).asEagerSingleton();
  }

  @Provides
  public TestWorkspace getWorkspace(
      TestWorkspaceProvider testWorkspaceProvider,
      Provider<DefaultTestUser> defaultUserProvider,
      @Named("workspace.default_memory_gb") int defaultMemoryGb)
      throws Exception {

    TestWorkspace workspace =
        testWorkspaceProvider.createWorkspace(
            defaultUserProvider.get(), defaultMemoryGb, WorkspaceTemplate.DEFAULT);
    workspace.await();

    return workspace;
  }

  @Provides
  public ActionsFactory getActionFactory() {
    return isMac() ? new MacOSActionsFactory() : new GenericActionsFactory();
  }

  @Provides
  public TestAuthServiceClient getTestAuthServiceClient(
      @Named(CHE_MULTIUSER) boolean isMultiuser,
      @Named("sys.host") String host,
      @Named("sys.protocol") String protocol,
      TestRealmProvider realmProvider) {
    if (isMultiuser) {
      return new KeycloakTestAuthServiceClient(host, protocol, realmProvider);
    } else {
      return new CheTestAuthServiceClient();
    }
  }

  @Provides
  public TestUserServiceClient getTestUserServiceClient(
      @Named(CHE_MULTIUSER) boolean isMultiuser,
      TestRealmProvider testRealmProvider,
      TestKeycloakProvider testKeycloakProvider,
      TestApiEndpointUrlProvider apiEndpointProvider,
      TestAdminHttpJsonRequestFactory requestFactory) {
    if (isMultiuser) {
      return new KeycloakTestUserServiceClient(testRealmProvider, testKeycloakProvider);
    } else {
      return new CheTestUserServiceClient(apiEndpointProvider, requestFactory);
    }
  }

  @Provides
  public TestUserNamespaceResolver getTestUserNamespaceResolver(
      @Named(CHE_MULTIUSER) boolean isMultiuser) {
    if (isMultiuser) {
      return new MultiUserCheTestUserNamespaceResolver();
    } else {
      return new SingleUserCheTestUserNamespaceResolver();
    }
  }

  @Provides
  public TestWorkspaceUrlResolver getTestWorkspaceUrlResolver(
      @Named(CHE_MULTIUSER) boolean isMultiuser, TestIdeUrlProvider urlProvider) {
    if (isMultiuser) {
      return new MultiUserCheTestWorkspaceUrlResolver(urlProvider);
    } else {
      return new SingleUserCheTestWorkspaceUrlResolver(urlProvider);
    }
  }
}
