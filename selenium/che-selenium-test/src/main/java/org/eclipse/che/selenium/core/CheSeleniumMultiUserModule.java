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

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Named;
import org.eclipse.che.selenium.core.client.CheTestMachineServiceClient;
import org.eclipse.che.selenium.core.client.KeycloakTestAuthServiceClient;
import org.eclipse.che.selenium.core.client.TestAuthServiceClient;
import org.eclipse.che.selenium.core.client.TestMachineServiceClient;
import org.eclipse.che.selenium.core.client.TestOrganizationServiceClient;
import org.eclipse.che.selenium.core.provider.AdminTestUserProvider;
import org.eclipse.che.selenium.core.provider.DefaultTestUserProvider;
import org.eclipse.che.selenium.core.provider.TestApiEndpointUrlProvider;
import org.eclipse.che.selenium.core.provider.TestUserProvider;
import org.eclipse.che.selenium.core.requestfactory.CheTestAdminHttpJsonRequestFactory;
import org.eclipse.che.selenium.core.user.AdminTestUser;
import org.eclipse.che.selenium.core.user.MultiUserCheAdminTestUserProvider;
import org.eclipse.che.selenium.core.user.MultiUserCheDefaultTestUserProvider;
import org.eclipse.che.selenium.core.user.MultiUserCheTestUserProvider;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.core.user.TestUserFactory;
import org.eclipse.che.selenium.core.user.TestUserImpl;

/** @author Anton Korneta */
public class CheSeleniumMultiUserModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(TestAuthServiceClient.class).to(KeycloakTestAuthServiceClient.class);
    bind(TestMachineServiceClient.class).to(CheTestMachineServiceClient.class);

    bind(DefaultTestUserProvider.class).to(MultiUserCheDefaultTestUserProvider.class);

    bind(TestUser.class).toProvider(TestUserProvider.class);
    bind(TestUserProvider.class).to(MultiUserCheTestUserProvider.class);

    bind(AdminTestUser.class).toProvider(AdminTestUserProvider.class);
    bind(AdminTestUserProvider.class).to(MultiUserCheAdminTestUserProvider.class);

    install(
        new FactoryModuleBuilder()
            .build(Key.get(new TypeLiteral<TestUserFactory<AdminTestUser>>() {}.getType())));

    install(
        new FactoryModuleBuilder()
            .build(Key.get(new TypeLiteral<TestUserFactory<TestUserImpl>>() {}.getType())));
  }

  @Provides
  @Named(CheSeleniumSuiteModule.ADMIN)
  public TestOrganizationServiceClient getAdminOrganizationServiceClient(
      TestApiEndpointUrlProvider apiEndpointUrlProvider,
      CheTestAdminHttpJsonRequestFactory requestFactory) {
    return new TestOrganizationServiceClient(apiEndpointUrlProvider, requestFactory);
  }
}
