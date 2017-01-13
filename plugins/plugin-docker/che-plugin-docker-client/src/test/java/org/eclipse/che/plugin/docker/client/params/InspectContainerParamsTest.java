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
public class InspectContainerParamsTest {

    private static final String  CONTAINER             = "container";
    private static final boolean RETURN_CONTAINER_SIZE = true;

    private InspectContainerParams inspectContainerParams;

    @Test
    public void shouldCreateParamsObjectWithRequiredParameters() {
        inspectContainerParams = InspectContainerParams.create(CONTAINER);

        assertEquals(inspectContainerParams.getContainer(), CONTAINER);

        assertNull(inspectContainerParams.isReturnContainerSize());
    }

    @Test
    public void shouldCreateParamsObjectWithAllPossibleParameters() {
        inspectContainerParams = InspectContainerParams.create(CONTAINER)
                                                       .withReturnContainerSize(RETURN_CONTAINER_SIZE);

        assertEquals(inspectContainerParams.getContainer(), CONTAINER);
        assertTrue(inspectContainerParams.isReturnContainerSize() == RETURN_CONTAINER_SIZE);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNullPointerExceptionIfContainerRequiredParameterIsNull() {
        inspectContainerParams = InspectContainerParams.create(null);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNullPointerExceptionIfContainerRequiredParameterResetWithNull() {
        inspectContainerParams.withContainer(null);
    }

}
