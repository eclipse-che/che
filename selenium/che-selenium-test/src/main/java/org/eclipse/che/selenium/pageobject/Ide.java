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
package org.eclipse.che.selenium.pageobject;

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.EXPECTED_MESS_IN_CONSOLE_SEC;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.net.URL;
import javax.annotation.PreDestroy;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.provider.TestIdeUrlProvider;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.core.workspace.TestWorkspaceUrlResolver;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/** @author Vitaliy Gulyy */
@Singleton
public class Ide {
  private final SeleniumWebDriver seleniumWebDriver;
  private final TestIdeUrlProvider testIdeUrlProvider;
  private final TestWorkspaceUrlResolver testWorkspaceUrlResolver;

  @Inject
  public Ide(
      SeleniumWebDriver seleniumWebDriver,
      TestIdeUrlProvider testIdeUrlProvider,
      TestWorkspaceUrlResolver testWorkspaceUrlResolver) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.testIdeUrlProvider = testIdeUrlProvider;
    this.testWorkspaceUrlResolver = testWorkspaceUrlResolver;
  }

  @Deprecated
  public WebDriver driver() {
    return seleniumWebDriver;
  }

  public void open(TestWorkspace testWorkspace) throws Exception {
    addAuthenticationToken(testWorkspace);

    URL workspaceUrl = testWorkspaceUrlResolver.resolve(testWorkspace);
    seleniumWebDriver.get(workspaceUrl.toString());
  }

  private void addAuthenticationToken(TestWorkspace testWorkspace) {
    seleniumWebDriver.get(testIdeUrlProvider.get().toString());
    seleniumWebDriver
        .manage()
        .addCookie(new Cookie("session-access-key", testWorkspace.getOwner().getAuthToken()));
  }

  public void switchFromDashboard() {
    new WebDriverWait(seleniumWebDriver, EXPECTED_MESS_IN_CONSOLE_SEC)
        .until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.id("ide-application-iframe")));
  }

  @PreDestroy
  public void close() {
    seleniumWebDriver.quit();
  }
}
