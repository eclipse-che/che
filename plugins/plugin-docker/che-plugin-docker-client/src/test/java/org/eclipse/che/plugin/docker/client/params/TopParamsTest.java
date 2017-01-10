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

/**
 * @author Mykola Morhun
 */
public class TopParamsTest {

    private static final String   CONTAINER = "container";
    private static final String[] PS_ARGS = {"arg1", "arg2"};

    private TopParams topParams;

    @Test
    public void shouldCreateParamsObjectWithRequiredParameters() {
        topParams = TopParams.create(CONTAINER);

        assertEquals(topParams.getContainer(), CONTAINER);

        assertNull(topParams.getPsArgs());
    }

    @Test
    public void shouldCreateParamsObjectWithAllPossibleParameters() {
        topParams = TopParams.create(CONTAINER)
                             .withPsArgs(PS_ARGS);

        assertEquals(topParams.getContainer(), CONTAINER);
        assertEquals(topParams.getPsArgs(), PS_ARGS);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNullPointerExceptionIfContainerRequiredParameterIsNull() {
        topParams = TopParams.create(null);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNullPointerExceptionIfContainerRequiredParameterResetWithNull() {
        topParams = TopParams.create(CONTAINER)
                             .withContainer(null);
    }

}
