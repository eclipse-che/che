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
package org.eclipse.che.infrastructure.docker.client;

import static org.testng.Assert.assertEquals;

import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Test for {@link org.eclipse.che.infrastructure.docker.client.DockerApiVersionPathPrefixProvider}
 *
 * @author Alexander Andrienko
 */
@Listeners(MockitoTestNGListener.class)
public class DockerApiVersionPathPrefixProviderTest {

  private static DockerApiVersionPathPrefixProvider dockerApiVersionPathPrefixProvider;

  @DataProvider(name = "validApiVersionValues")
  public static Object[][] validApiVersionValues() {
    return new Object[][] {{"", ""}, {"1", "/v1"}, {"1.18", "/v1.18"}};
  }

  @Test(dataProvider = "validApiVersionValues")
  public void apiVersionPrefixShouldBeEmpty(String apiVersion, String expectedApiVersionPrefix) {
    dockerApiVersionPathPrefixProvider = new DockerApiVersionPathPrefixProvider(apiVersion);

    assertEquals(dockerApiVersionPathPrefixProvider.get(), expectedApiVersionPrefix);
  }

  @DataProvider(name = "invalidApiVersionValues")
  public static Object[][] invalidApiVersionValues() {
    return new Object[][] {
      {"invalid"},
      {"1 .18"},
      {".1.18"},
      {"1..18"},
      {"1-18"},
      {"1$18"},
      {"1%18"},
      {"1*18"},
      {"1_18"},
      {"1   18"},
      {"1,,18"},
      {".1"},
      {",1"},
      {"1."},
      {".1."},
      {",1,"},
      {"1,18 "},
      {"1a18 "},
      {"1B18 "},
      {"a1"},
      {"1a"},
      {"Ab"},
      {"1.18a"}
    };
  }

  @Test(
      dataProvider = "invalidApiVersionValues",
      expectedExceptions = IllegalArgumentException.class,
      expectedExceptionsMessageRegExp =
          "Invalid property format: '.*'. Valid docker api version contains digits "
              + "which can be separated by symbol '.'. For example: '1', '1.18'")
  public void apiVersionPrefixShouldBeEmpty(String apiVersion) {
    dockerApiVersionPathPrefixProvider = new DockerApiVersionPathPrefixProvider(apiVersion);
  }
}
