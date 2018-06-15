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
package org.eclipse.che.selenium.hotupdate;

import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Profile.PROFILE_MENU;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.provider.CheTestApiEndpointUrlProvider;
import org.eclipse.che.selenium.core.requestfactory.CheTestAdminHttpJsonRequestFactory;
import org.eclipse.che.selenium.core.utils.process.ProcessAgent;
import org.eclipse.che.selenium.core.utils.process.ProcessAgentException;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CheTerminal;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class RecreateUpdate {
  @Inject CheTestAdminHttpJsonRequestFactory testUserHttpJsonRequestFactory;
  @Inject CheTestApiEndpointUrlProvider cheTestApiEndpointUrlProvider;
  @Inject ProjectExplorer projectExplorer;
  @Inject private ProcessAgent processAgent;
  @Inject private TestWorkspace workspace;
  @Inject private Ide ide;
  @Inject private SeleniumWebDriver seleniumWebDriver;
  @Inject private CheTerminal terminal;
  @Inject private Menu menu;

  private String result;

  @BeforeTest
  public void setUp() throws ProcessAgentException {
    String command = "/tmp/oc describe dc/che | grep Strategy";
    result = processAgent.process(command);
  }

  @Test
  public void checkRecreateUpdateStrategy() throws Exception {
    String RestUrlForSuspending = cheTestApiEndpointUrlProvider.get().toString() + "system/stop";
    ide.open(workspace);
    testUserHttpJsonRequestFactory.fromUrl(RestUrlForSuspending).usePostMethod().request();
    checkWorkspaceIsNotAvailable();
    result.trim();
  }

  private void checkWorkspaceIsNotAvailable() {
    int disappearanceWorkspaceTimeout = 3;
    projectExplorer.waitProjectExplorerIsNotPresent(disappearanceWorkspaceTimeout);
    terminal.waitTerminalIsNotPresent(disappearanceWorkspaceTimeout);
  }
}
