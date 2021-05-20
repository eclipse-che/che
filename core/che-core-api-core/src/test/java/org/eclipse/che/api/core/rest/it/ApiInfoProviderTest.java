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
package org.eclipse.che.api.core.rest.it;

import static org.testng.Assert.*;

import org.eclipse.che.api.core.rest.ApiInfoProvider;
import org.eclipse.che.api.core.rest.shared.dto.ApiInfo;
import org.testng.annotations.Test;

public class ApiInfoProviderTest {

  @Test
  public void testGet() {
    // given
    ApiInfoProvider provider = new ApiInfoProvider("my custom build");
    // when
    ApiInfo apiInfo = provider.get();
    // then
    assertEquals(apiInfo.getBuildInfo(), "my custom build");
    assertNotNull(apiInfo.getScmRevision());
    assertNotNull(apiInfo.getSpecificationTitle());
    assertNotNull(apiInfo.getSpecificationVersion());
    assertNotNull(apiInfo.getImplementationVendor());
    assertNotNull(apiInfo.getImplementationVersion());
    assertEquals(apiInfo.getScmRevision().length(), 40);
    assertEquals(apiInfo.getSpecificationTitle(), "Che REST API");
    assertEquals(apiInfo.getSpecificationVersion(), "1.0-beta2");
    assertEquals(apiInfo.getImplementationVendor(), "Red Hat, Inc.");
    assertTrue(apiInfo.getImplementationVersion().length() > 4);
  }
}
