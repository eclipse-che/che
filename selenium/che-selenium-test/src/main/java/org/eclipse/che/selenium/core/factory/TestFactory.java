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
package org.eclipse.che.selenium.core.factory;

import org.eclipse.che.api.factory.shared.dto.FactoryDto;
import org.eclipse.che.selenium.core.client.TestFactoryServiceClient;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.provider.TestDashboardUrlProvider;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.pageobject.site.LoginPage;
import org.openqa.selenium.WebDriver;

/** @author Anatolii Bazko */
public class TestFactory {
  private final TestUser owner;
  private final FactoryDto factoryDto;
  private final TestDashboardUrlProvider dashboardUrl;
  private final TestFactoryServiceClient testFactoryServiceClient;
  private final TestWorkspaceServiceClient workspaceServiceClient;
  private final LoginPage loginPage;

  private final String factoryUrl;

  public TestFactory(
      String factoryUrl,
      TestUser owner,
      FactoryDto factoryDto,
      TestDashboardUrlProvider dashboardUrl,
      TestFactoryServiceClient factoryServiceClient,
      TestWorkspaceServiceClient workspaceServiceClient,
      LoginPage loginPage) {
    this.factoryDto = factoryDto;
    this.owner = owner;
    this.factoryUrl = factoryUrl;
    this.dashboardUrl = dashboardUrl;
    this.testFactoryServiceClient = factoryServiceClient;
    this.workspaceServiceClient = workspaceServiceClient;
    this.loginPage = loginPage;
  }

  /** Adds authentication token into the browser and opens factory url. */
  public void authenticateAndOpen(WebDriver driver) throws Exception {
    driver.get(dashboardUrl.get().toString());

    if (loginPage.isOpened()) {
      loginPage.login(owner.getName(), owner.getPassword());
    }

    driver.get(factoryUrl);
  }

  /** Opens factory url. */
  public void open(WebDriver driver) throws Exception {
    driver.get(factoryUrl);
  }

  public void delete() throws Exception {
    workspaceServiceClient.deleteFactoryWorkspaces(
        factoryDto.getWorkspace().getName(), owner.getName());
    deleteFactory();
  }

  private void deleteFactory() throws Exception {
    if (isNamedFactory()) {
      testFactoryServiceClient.deleteFactory(factoryDto.getName());
    }
  }

  private boolean isNamedFactory() {
    return factoryDto.getName() != null;
  }
}
