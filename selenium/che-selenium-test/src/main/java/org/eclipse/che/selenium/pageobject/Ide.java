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

import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Profile.PROFILE_MENU;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.PREPARING_WS_TIMEOUT_SEC;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.net.URL;
import javax.annotation.PreDestroy;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.entrance.Entrance;
import org.eclipse.che.selenium.core.utils.BrowserLogsUtil;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.core.workspace.TestWorkspaceUrlResolver;
import org.eclipse.che.selenium.pageobject.machineperspective.MachineTerminal;
import org.openqa.selenium.logging.LogType;

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
  private final MachineTerminal terminal;
  private final Menu menu;
  private BrowserLogsUtil browserLogsUtil;

  @Inject
  public Ide(
      SeleniumWebDriver seleniumWebDriver,
      TestWorkspaceUrlResolver testWorkspaceUrlResolver,
      Entrance entrance,
      ProjectExplorer projectExplorer,
      MachineTerminal terminal,
      Menu menu,
            BrowserLogsUtil browserLogsUtil) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.testWorkspaceUrlResolver = testWorkspaceUrlResolver;
    this.entrance = entrance;
    this.projectExplorer = projectExplorer;
    this.terminal = terminal;
    this.menu = menu;
    this.browserLogsUtil = browserLogsUtil;
  }

  public void open(TestWorkspace testWorkspace) throws Exception {
    URL workspaceUrl = testWorkspaceUrlResolver.resolve(testWorkspace);
    seleniumWebDriver.get(workspaceUrl.toString());
    entrance.login(testWorkspace.getOwner());
    browserLogsUtil.getLogs();
    browserLogsUtil.getLogs(LogType.PERFORMANCE).forEach(logEntry -> {
      JsonParser jsonParser = new JsonParser();
      JsonElement jsonElement = jsonParser.parse(logEntry.getMessage());
      JsonObject jsonObject = jsonElement.getAsJsonObject().get("message").getAsJsonObject();
      String networkValue = jsonObject.get("method").getAsString();
      if (networkValue.equals("Network.responseReceived")){
        if (jsonObject.get("params").toString().contains("application/json"))
          System.out.print(jsonObject.get("params"));
      }
    });

  }

  public void waitOpenedWorkspaceIsReadyToUse() {
    projectExplorer.waitProjectExplorer(PREPARING_WS_TIMEOUT_SEC);
    terminal.waitTerminalTab(PREPARING_WS_TIMEOUT_SEC);
    menu.waitMenuItemIsEnabled(PROFILE_MENU);
  }

  @PreDestroy
  public void close() {
    seleniumWebDriver.quit();
  }
}
