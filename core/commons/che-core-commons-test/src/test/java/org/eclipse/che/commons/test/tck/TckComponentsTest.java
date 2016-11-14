/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.commons.test.tck;

import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.inject.Inject;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Tests for {@code org.eclipse.che.commons.test.tck.*} package.
 *
 * @author Yevhenii Voevodin
 */
@Listeners(TckListener.class)
public class TckComponentsTest {

    @Inject
    private TckRepository<Entity> tckRepository;

    @Inject
    private DBUrlProvider dbUrlProvider;

    @Test
    public void testComponentsAreInjected() {
        assertNotNull(tckRepository, "TckRepository is not injected");
        assertNotNull(dbUrlProvider, "DBUrlProvider is not injected");
        assertEquals(dbUrlProvider.getUrl(), DBServerListener.DB_SERVER_URL, "Value is set to ITestContext");
    }

    public interface Entity {}

    public interface DBUrlProvider {
        String getUrl();
    }
}
