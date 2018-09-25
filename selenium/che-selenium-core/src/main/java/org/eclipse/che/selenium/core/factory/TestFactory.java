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
package org.eclipse.che.selenium.core.factory;

import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.factory.shared.dto.FactoryDto;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.client.TestFactoryServiceClient;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.entrance.Entrance;
import org.eclipse.che.selenium.core.provider.TestDashboardUrlProvider;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.openqa.selenium.WebDriver;

/** @author Anatolii Bazko */
public class TestFactory {
  private final DefaultTestUser owner;
  private final FactoryDto factoryDto;
  private final TestDashboardUrlProvider dashboardUrl;
  private final TestFactoryServiceClient testFactoryServiceClient;
  private final TestWorkspaceServiceClient workspaceServiceClient;
  private final Entrance entrance;
  private final String factoryUrl;
  private final SeleniumWebDriver seleniumWebDriver;
  private final SeleniumWebDriverHelper seleniumWebDriverHelper;

  public TestFactory(
      String factoryUrl,
      DefaultTestUser owner,
      FactoryDto factoryDto,
      TestDashboardUrlProvider dashboardUrl,
      TestFactoryServiceClient factoryServiceClient,
      TestWorkspaceServiceClient workspaceServiceClient,
      Entrance entrance,
      SeleniumWebDriver seleniumWebDriver,
      SeleniumWebDriverHelper seleniumWebDriverHelper) {
    this.factoryDto = factoryDto;
    this.owner = owner;
    this.factoryUrl = factoryUrl;
    this.dashboardUrl = dashboardUrl;
    this.testFactoryServiceClient = factoryServiceClient;
    this.workspaceServiceClient = workspaceServiceClient;
    this.entrance = entrance;
    this.seleniumWebDriver = seleniumWebDriver;
    this.seleniumWebDriverHelper = seleniumWebDriverHelper;
  }

  /** Login to factory and open it by url. */
  public void authenticateAndOpen() {
    seleniumWebDriver.get(dashboardUrl.get().toString());
    entrance.login(owner);
    seleniumWebDriver.get(factoryUrl);
    seleniumWebDriverHelper.switchToIdeFrameAndWaitAvailability();
  }

  /** Opens factory url. */
  public void open(WebDriver driver) {
    driver.get(factoryUrl);
  }

  public void delete() throws Exception {
    seleniumWebDriver.quit();

    workspaceServiceClient.deleteFactoryWorkspaces(
        factoryDto.getWorkspace().getName(), owner.getName());
    deleteFactory();
  }

  private void deleteFactory() {
    if (isNamedFactory()) {
      testFactoryServiceClient.deleteFactory(factoryDto.getName());
    }
  }

  private boolean isNamedFactory() {
    return factoryDto.getName() != null;
  }

  public WorkspaceStatus getWorkspaceStatusAssociatedWithFactory() throws Exception {
    return workspaceServiceClient
        .getByName(factoryDto.getWorkspace().getName(), owner.getName())
        .getStatus();
  }
}
