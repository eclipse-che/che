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
public class RemoveContainerParamsTest {

    private static final String  CONTAINER      = "container";
    private static final Boolean FORCE          = Boolean.FALSE;
    private static final Boolean REMOVE_VOLUMES = Boolean.TRUE;

    private RemoveContainerParams removeContainerParams;

    @Test
    public void shouldCreateParamsObjectWithRequiredParameters() {
        removeContainerParams = RemoveContainerParams.create(CONTAINER);

        assertEquals(removeContainerParams.getContainer(), CONTAINER);

        assertNull(removeContainerParams.isForce());
        assertNull(removeContainerParams.isRemoveVolumes());
    }

    @Test
    public void shouldCreateParamsObjectWithAllPossibleParameters() {
        removeContainerParams = RemoveContainerParams.create(CONTAINER)
                                                     .withForce(FORCE)
                                                     .withRemoveVolumes(REMOVE_VOLUMES);

        assertEquals(removeContainerParams.getContainer(), CONTAINER);
        assertEquals(removeContainerParams.isForce(), FORCE);
        assertEquals(removeContainerParams.isRemoveVolumes(), REMOVE_VOLUMES);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNullPointerExceptionIfContainerRequiredParameterIsNull() {
        removeContainerParams = RemoveContainerParams.create(null);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNullPointerExceptionIfContainerRequiredParameterResetWithNull() {
        removeContainerParams = RemoveContainerParams.create(CONTAINER)
                                                     .withContainer(null);
    }

    @Test
    public void forceParameterShouldEqualsNullIfItNotSet() {
        removeContainerParams = RemoveContainerParams.create(CONTAINER)
                                                     .withRemoveVolumes(REMOVE_VOLUMES);

        assertNull(removeContainerParams.isForce());
    }

    @Test
    public void removeVolumesParameterShouldEqualsNullIfItNotSet() {
        removeContainerParams = RemoveContainerParams.create(CONTAINER)
                                                     .withForce(FORCE);

        assertNull(removeContainerParams.isRemoveVolumes());
    }

}
