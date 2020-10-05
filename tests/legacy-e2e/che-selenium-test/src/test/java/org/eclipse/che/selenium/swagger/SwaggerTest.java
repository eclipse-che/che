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
package org.eclipse.che.selenium.swagger;

import static org.testng.Assert.assertEquals;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.provider.TestIdeUrlProvider;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Andrey Chizhikov */
public class SwaggerTest {

  @Inject private TestIdeUrlProvider ideUrlProvider;
  @Inject private SeleniumWebDriver driver;

  private String swaggerUrl;

  @BeforeClass
  public void setUp() throws Exception {
    swaggerUrl = ideUrlProvider.get() + "swagger/";
  }

  @Test
  public void checkNameProjectTest() {
    driver.navigate().to(swaggerUrl);
    assertEquals(driver.getCurrentUrl(), swaggerUrl);
  }
}
