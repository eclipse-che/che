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
package org.eclipse.che.selenium.pageobject;

import static org.testng.Assert.fail;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.net.URL;
import javax.annotation.PreDestroy;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.entrance.Entrance;
import org.eclipse.che.selenium.core.utils.BrowserLogsUtil;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.core.workspace.TestWorkspaceUrlResolver;
import org.openqa.selenium.TimeoutException;

/**
 * @author Vitaliy Gulyy
 * @author Dmytro Nochevnov
 */
@Singleton
public class Ide {
  private final SeleniumWebDriver seleniumWebDriver;
  private final TestWorkspaceUrlResolver testWorkspaceUrlResolver;
  private final Entrance entrance;
  private final ProjectExplorer projectExplorer;
  private final BrowserLogsUtil browserLogsUtil;

  @Inject
  public Ide(
      SeleniumWebDriver seleniumWebDriver,
      TestWorkspaceUrlResolver testWorkspaceUrlResolver,
      Entrance entrance,
      ProjectExplorer projectExplorer,
      BrowserLogsUtil browserLogsUtil) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.testWorkspaceUrlResolver = testWorkspaceUrlResolver;
    this.entrance = entrance;
    this.projectExplorer = projectExplorer;
    this.browserLogsUtil = browserLogsUtil;
  }

  public void open(TestWorkspace testWorkspace) throws Exception {
    URL workspaceUrl = testWorkspaceUrlResolver.resolve(testWorkspace);
    seleniumWebDriver.get(workspaceUrl.toString());
    entrance.login(testWorkspace.getOwner());
    try {
      projectExplorer.waitProjectExplorer(60);
    } catch (TimeoutException ex) {
      browserLogsUtil.appendBrowserLogs();
      // Remove try-catch block after issue has been resolved
      fail("Known issue https://github.com/eclipse/che/issues/8468", ex);
    }
  }

  @PreDestroy
  public void close() {
    seleniumWebDriver.quit();
  }
}
