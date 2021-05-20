/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.kubernetes.util;

import static org.testng.Assert.assertEquals;

import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link KubernetesSize}.
 *
 * @author Anton Korneta
 */
@Listeners(MockitoTestNGListener.class)
public class KubernetesSizeTest {

  @Test(dataProvider = "validSizes")
  public void testParseKubernetesMemoryFormatsToBytes(String kubeSize, long expectedBytes)
      throws Exception {
    assertEquals(KubernetesSize.toBytes(kubeSize), expectedBytes);
  }

  @Test(dataProvider = "validCpuLimits")
  public void testParseKubernetesCpuFormatsToCores(String kubeSize, float expectedCores)
      throws Exception {
    assertEquals(KubernetesSize.toCores(kubeSize), expectedCores);
  }

  @DataProvider(name = "validSizes")
  public Object[][] correctKubernetesMemoryFormats() {
    return new Object[][] {
      {"123Mi", 128974848}, {"129M", 129000000}, {"129e6", 129000000}, {"129e+6", 129000000}
    };
  }

  @DataProvider(name = "validCpuLimits")
  public Object[][] correctKubernetesCpuLimits() {
    return new Object[][] {{"0.1", 0.1f}, {"1250m", 1.250f}, {"-1", -1f}, {"60m", 0.06f}};
  }
}
