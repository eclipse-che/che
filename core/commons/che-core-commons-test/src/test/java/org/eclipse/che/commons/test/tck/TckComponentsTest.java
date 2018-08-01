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
package org.eclipse.che.commons.test.tck;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import javax.inject.Inject;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests for {@code org.eclipse.che.commons.test.tck.*} package.
 *
 * @author Yevhenii Voevodin
 */
@Listeners(TckListener.class)
@Test(suiteName = "tck")
public class TckComponentsTest {

  @Inject private TckRepository<Entity> tckRepository;

  @Inject private DBUrlProvider dbUrlProvider;

  @Test
  public void testComponentsAreInjected() {
    assertNotNull(tckRepository, "TckRepository is not injected");
    assertNotNull(dbUrlProvider, "DBUrlProvider is not injected");
    assertEquals(
        dbUrlProvider.getUrl(), DBServerListener.DB_SERVER_URL, "Value is set to ITestContext");
  }

  public interface Entity {}

  public interface DBUrlProvider {
    String getUrl();
  }
}
