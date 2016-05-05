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
package org.eclipse.che.plugin.docker.client.params;

import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 * @author Mykola Morhun
 */
public class StopContainerParamsTest {

    private static final String   CONTAINER = "container";
    private static final Long     TIMEOUT   = 2L;
    private static final TimeUnit TIMEUNIT  = TimeUnit.MINUTES;

    private StopContainerParams stopContainerParams;

    @Test
    public void shouldCreateParamsObjectWithRequiredParameters() {
        stopContainerParams = StopContainerParams.from(CONTAINER);

        assertEquals(stopContainerParams.getContainer(), CONTAINER);

        assertNull(stopContainerParams.getTimeout());
        assertNull(stopContainerParams.getTimeunit());
    }

    @Test
    public void shouldCreateParamsObjectWithAllPossibleParameters() {
        stopContainerParams = StopContainerParams.from(CONTAINER)
                                                 .withTimeout(TIMEOUT, TIMEUNIT);

        assertEquals(stopContainerParams.getContainer(), CONTAINER);
        assertEquals(stopContainerParams.getTimeout(), TIMEOUT);
        assertEquals(stopContainerParams.getTimeunit(), TIMEUNIT);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNullPointerExceptionIfContainerRequiredParameterIsNull() {
        stopContainerParams = StopContainerParams.from(null);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNullPointerExceptionIfContainerRequiredParameterResetWithNull() {
        stopContainerParams = StopContainerParams.from(CONTAINER)
                                                 .withContainer(null);
    }

    @Test
    public void timeoutParameterShouldEqualsNullIfItNotSet() {
        stopContainerParams = StopContainerParams.from(CONTAINER);

        assertNull(stopContainerParams.getTimeout());
    }

    @Test
    public void timeUnitParameterShouldBeInSecondsIfNotSet() {
        stopContainerParams = StopContainerParams.from(CONTAINER)
                                                 .withTimeout(TIMEOUT);

        assertEquals(stopContainerParams.getTimeunit(), TimeUnit.SECONDS);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNullPointerExceptionIfTimeUnitParameterIsNull() {
        stopContainerParams = StopContainerParams.from(CONTAINER)
                                                 .withTimeout(TIMEOUT, null);
    }

}
