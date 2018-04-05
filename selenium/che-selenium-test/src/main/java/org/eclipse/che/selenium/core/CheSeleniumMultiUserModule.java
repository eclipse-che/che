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
import org.eclipse.che.selenium.core.client.CheTestMachineServiceClient;
import org.eclipse.che.selenium.core.client.KeycloakTestAuthServiceClient;
import org.eclipse.che.selenium.core.client.TestAuthServiceClient;
import org.eclipse.che.selenium.core.client.TestMachineServiceClient;
import org.eclipse.che.selenium.core.user.AdminTestUser;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.user.MultiUserCheAdminTestUserProvider;
import org.eclipse.che.selenium.core.user.MultiUserCheDefaultTestUserProvider;
import org.eclipse.che.selenium.core.user.MultiUserCheTestUserProvider;
import org.eclipse.che.selenium.core.user.TestUser;

/** @author Anton Korneta */
public class CheSeleniumMultiUserModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(TestAuthServiceClient.class).to(KeycloakTestAuthServiceClient.class);
    bind(TestMachineServiceClient.class).to(CheTestMachineServiceClient.class);
    bind(TestUser.class).toProvider(MultiUserCheTestUserProvider.class);
    bind(DefaultTestUser.class).toProvider(MultiUserCheDefaultTestUserProvider.class);
    bind(AdminTestUser.class).toProvider(MultiUserCheAdminTestUserProvider.class);
  }
}
