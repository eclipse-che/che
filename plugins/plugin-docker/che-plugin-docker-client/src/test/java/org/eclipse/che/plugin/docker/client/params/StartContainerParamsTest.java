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
public class StartContainerParamsTest {

    private static final String CONTAINER = "container";

    private StartContainerParams startContainerParams;

    @Test
    public void shouldCreateParamsObjectWithRequiredParameters() {
        startContainerParams = StartContainerParams.create(CONTAINER);

        assertEquals(startContainerParams.getContainer(), CONTAINER);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNullPointerExceptionIfContainerRequiredParameterIsNull() {
        startContainerParams = StartContainerParams.create(null);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNullPointerExceptionIfContainerRequiredParameterResetWithNull() {
        startContainerParams = StartContainerParams.create(CONTAINER)
                                                   .withContainer(null);
    }

}
