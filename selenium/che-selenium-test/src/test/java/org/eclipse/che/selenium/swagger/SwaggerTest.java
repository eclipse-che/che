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
package org.eclipse.che.selenium.swagger;

import static org.testng.Assert.assertTrue;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.provider.TestIdeUrlProvider;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Swagger;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Andrey Chizhikov */
public class SwaggerTest {

  @Inject private TestIdeUrlProvider ideUrlProvider;
  @Inject private Ide ide;
  @Inject private TestWorkspace workspace;
  @Inject private Loader loader;
  @Inject private Swagger swagger;
  @Inject private SeleniumWebDriver driver;

  private String swaggerUrl;

  @BeforeClass
  public void setUp() throws Exception {
    swaggerUrl = ideUrlProvider.get() + "swagger/";
    ide.open(workspace);
  }

  @Test
  public void checkNameProjectTest() throws Exception {
    driver.navigate().to(swaggerUrl);

    assertTrue(swagger.getWsNamesFromWorkspacePage().contains(workspace.getName()));
  }
}
