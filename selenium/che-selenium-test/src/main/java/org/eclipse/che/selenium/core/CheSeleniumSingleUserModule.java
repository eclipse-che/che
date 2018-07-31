/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.core;

import com.google.inject.AbstractModule;
import org.eclipse.che.selenium.core.client.CheTestAuthServiceClient;
import org.eclipse.che.selenium.core.client.DummyCheTestMachineServiceClient;
import org.eclipse.che.selenium.core.client.TestAuthServiceClient;
import org.eclipse.che.selenium.core.client.TestMachineServiceClient;
import org.eclipse.che.selenium.core.provider.DefaultTestUserProvider;
import org.eclipse.che.selenium.core.user.SingleUserCheDefaultTestUserProvider;

/** @author Anton Korneta */
public class CheSeleniumSingleUserModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(TestAuthServiceClient.class).to(CheTestAuthServiceClient.class);
    bind(TestMachineServiceClient.class).to(DummyCheTestMachineServiceClient.class);
    bind(DefaultTestUserProvider.class).to(SingleUserCheDefaultTestUserProvider.class);
  }
}
