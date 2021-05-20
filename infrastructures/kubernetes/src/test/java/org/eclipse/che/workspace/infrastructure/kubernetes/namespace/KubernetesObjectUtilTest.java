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
package org.eclipse.che.workspace.infrastructure.kubernetes.namespace;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class KubernetesObjectUtilTest {
  @Test(dataProvider = "testNames")
  public void shouldTestisValidConfigMapKeyName(String nameToTest, boolean isValid) {
    assertEquals(KubernetesObjectUtil.isValidConfigMapKeyName(nameToTest), isValid);
  }

  @DataProvider
  public static Object[][] testNames() {
    return new Object[][] {
      new Object[] {"foo.bar", true},
      new Object[] {"https://foo.bar", false},
      new Object[] {"_fef_123-ah_*zz**", false},
      new Object[] {"_fef_123-ah_z.z", true},
      new Object[] {"a-b#-hello", false},
      new Object[] {"a---------b", true},
      new Object[] {"--ab--", true}
    };
  }
}
