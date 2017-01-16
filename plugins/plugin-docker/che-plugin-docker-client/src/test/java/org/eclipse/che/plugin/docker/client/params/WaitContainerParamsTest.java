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

/**
 * @author Mykola Morhun
 */
public class WaitContainerParamsTest {

    private static final String CONTAINER = "container";

    private WaitContainerParams waitContainerParams;

    @Test
    public void shouldCreateParamsObjectWithRequiredParameters() {
        waitContainerParams = WaitContainerParams.create(CONTAINER);

        assertEquals(waitContainerParams.getContainer(), CONTAINER);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNullPointerExceptionIfContainerRequiredParameterIsNull() {
        waitContainerParams = WaitContainerParams.create(null);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNullPointerExceptionIfContainerRequiredParameterResetWithNull() {
        waitContainerParams = WaitContainerParams.create(CONTAINER)
                                                 .withContainer(null);
    }
}
