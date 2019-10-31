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
package org.eclipse.che.selenium.pageobject.dashboard.workspaces;

import static java.lang.String.format;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceConfig.Locators.CONFIG_EDITOR;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceConfig.Locators.CONFIG_FORM;

import com.google.common.base.Joiner;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.support.PageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class WorkspaceConfig {
  private final SeleniumWebDriverHelper seleniumWebDriverHelper;
  private final Logger LOG = LoggerFactory.getLogger(WorkspaceConfig.class);

  @Inject
  public WorkspaceConfig(
      SeleniumWebDriver seleniumWebDriver, SeleniumWebDriverHelper seleniumWebDriverHelper) {
    this.seleniumWebDriverHelper = seleniumWebDriverHelper;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  public interface Locators {
    String CONFIG_FORM = "//ng-form[@name='workspaceDevfileForm']";
    String CONFIG_EDITOR = "//ng-form[@name='workspaceConfigForm']//div[@class='CodeMirror-code']";
  }

  public void waitConfigForm() {
    seleniumWebDriverHelper.waitVisibility(By.xpath(CONFIG_FORM));
  }

  public String getWorkspaceConfig() {
    return seleniumWebDriverHelper.waitVisibilityAndGetText(By.xpath(CONFIG_EDITOR), 20);
  }

  public String createExpectedWorkspaceConfig(String workspaceName)
      throws IOException, URISyntaxException {
    String expectedWorkspaceConfTemplate = Joiner.on('\n').join(getConfigFileContent());
    return format(expectedWorkspaceConfTemplate, workspaceName);
  }

  private Path getConfigTemplatePath() throws URISyntaxException {
    URL fileUrl =
        getClass()
            .getResource("/org/eclipse/che/selenium/dashboard/expectedWorkspaceConfTemplate.txt");

    return Paths.get(fileUrl.toURI());
  }

  private List<String> getConfigFileContent() throws URISyntaxException, IOException {
    return Files.readAllLines(getConfigTemplatePath());
  }
}
