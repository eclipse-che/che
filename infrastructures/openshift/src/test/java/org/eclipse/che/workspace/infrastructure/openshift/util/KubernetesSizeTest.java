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
package org.eclipse.che.workspace.infrastructure.openshift.util;

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

  @DataProvider(name = "validSizes")
  public Object[][] correctKubernetesMemoryFormats() {
    return new Object[][] {
      {"123Mi", 128974848}, {"129M", 129000000}, {"129e6", 129000000}, {"129e+6", 129000000}
    };
  }
}
