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
package org.eclipse.che.workspace.infrastructure.kubernetes.server.secure.jwtproxy;

import static org.mockito.Mockito.when;

import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.DefaultHostExternalServiceExposureStrategy;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.MultiHostExternalServiceExposureStrategy;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.SingleHostExternalServiceExposureStrategy;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MockitoTestNGListener.class)
public class PathBasePrefixProviderTest {
  private static final String WORKSPACE_ID = "42";

  @Mock private RuntimeIdentity identity;

  @BeforeMethod
  public void setup() {
    when(identity.getWorkspaceId()).thenReturn(WORKSPACE_ID);
  }

  @Test(dataProvider = "expectedPrefixesByStrategy")
  public void testPathPrefixBasedOnWorkspaceForSingleAndDefaultHostStrategies(
      String strategy, String expectedPrefix) {
    PathBasePrefixProvider provider = new PathBasePrefixProvider(strategy);
    Assert.assertEquals(expectedPrefix, provider.getPathPrefix(identity));
  }

  @DataProvider
  public static Object[][] expectedPrefixesByStrategy() {
    return new Object[][] {
      {SingleHostExternalServiceExposureStrategy.SINGLE_HOST_STRATEGY, WORKSPACE_ID},
      {DefaultHostExternalServiceExposureStrategy.DEFAULT_HOST_STRATEGY, WORKSPACE_ID},
      {MultiHostExternalServiceExposureStrategy.MULTI_HOST_STRATEGY, ""}
    };
  }
}
