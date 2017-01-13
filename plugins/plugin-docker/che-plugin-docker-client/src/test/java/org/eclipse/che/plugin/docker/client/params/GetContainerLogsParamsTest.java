/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * @author Mykola Morhun
 */
public class GetContainerLogsParamsTest {

    private static final String  CONTAINER  = "container";
    private static final boolean DETAILS    = false;
    private static final boolean FOLLOW     = true;
    private static final long    SINCE      = 123456789L;
    private static final boolean TIMESTAMPS = false;
    private static final String  TAIL       = "all";

    private GetContainerLogsParams getContainerLogsParams;

    @Test
    public void shouldCreateParamsObjectWithRequiredParameters() {
        getContainerLogsParams = GetContainerLogsParams.create(CONTAINER);

        assertEquals(getContainerLogsParams.getContainer(), CONTAINER);

        assertNull(getContainerLogsParams.isDetails());
        assertNull(getContainerLogsParams.isFollow());
        assertNull(getContainerLogsParams.getSince());
        assertNull(getContainerLogsParams.isTimestamps());
        assertNull(getContainerLogsParams.getTail());
    }

    @Test
    public void shouldCreateParamsObjectWithAllPossibleParameters() {
        getContainerLogsParams = GetContainerLogsParams.create(CONTAINER)
                                                       .withDetails(DETAILS)
                                                       .withFollow(FOLLOW)
                                                       .withSince(SINCE)
                                                       .withTimestamps(TIMESTAMPS)
                                                       .withTail(TAIL);

        assertEquals(getContainerLogsParams.getContainer(), CONTAINER);
        assertTrue(getContainerLogsParams.isDetails() == DETAILS);
        assertTrue(getContainerLogsParams.isFollow() == FOLLOW);
        assertTrue(getContainerLogsParams.getSince() == SINCE);
        assertTrue(getContainerLogsParams.isTimestamps() == TIMESTAMPS);
        assertEquals(getContainerLogsParams.getTail(), TAIL);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNullPointerExceptionIfContainerRequiredParameterIsNull() {
        getContainerLogsParams = GetContainerLogsParams.create(null);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNullPointerExceptionIfContainerRequiredParameterResetWithNull() {
        getContainerLogsParams.withContainer(null);
    }

    @Test
    public void detailsParameterShouldEqualsNullIfItNotSet() {
        getContainerLogsParams = GetContainerLogsParams.create(CONTAINER)
                                                       .withFollow(FOLLOW)
                                                       .withSince(SINCE)
                                                       .withTimestamps(TIMESTAMPS)
                                                       .withTail(TAIL);

        assertNull(getContainerLogsParams.isDetails());
    }

    @Test
    public void followParameterShouldEqualsNullIfItNotSet() {
        getContainerLogsParams = GetContainerLogsParams.create(CONTAINER)
                                                       .withDetails(DETAILS)
                                                       .withSince(SINCE)
                                                       .withTimestamps(TIMESTAMPS)
                                                       .withTail(TAIL);

        assertNull(getContainerLogsParams.isFollow());
    }

    @Test
    public void sinceParameterShouldEqualsNullIfItNotSet() {
        getContainerLogsParams = GetContainerLogsParams.create(CONTAINER)
                                                       .withDetails(DETAILS)
                                                       .withFollow(FOLLOW)
                                                       .withTimestamps(TIMESTAMPS)
                                                       .withTail(TAIL);

        assertNull(getContainerLogsParams.getSince());
    }

    @Test
    public void timestampsParameterShouldEqualsNullIfItNotSet() {
        getContainerLogsParams = GetContainerLogsParams.create(CONTAINER)
                                                       .withDetails(DETAILS)
                                                       .withFollow(FOLLOW)
                                                       .withSince(SINCE)
                                                       .withTail(TAIL);

        assertNull(getContainerLogsParams.isTimestamps());
    }

    @Test
    public void tailParameterShouldEqualsNullIfItNotSet() {
        getContainerLogsParams = GetContainerLogsParams.create(CONTAINER)
                                                       .withDetails(DETAILS)
                                                       .withFollow(FOLLOW)
                                                       .withSince(SINCE)
                                                       .withTimestamps(TIMESTAMPS);

        assertNull(getContainerLogsParams.getTail());
    }

}
