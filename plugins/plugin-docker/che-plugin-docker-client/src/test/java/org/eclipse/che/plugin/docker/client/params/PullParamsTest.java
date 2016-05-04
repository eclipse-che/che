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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 * @author Mykola Morhun
 */
public class PullParamsTest {

    private static final String IMAGE    = "image";
    private static final String TAG      = "tag";
    private static final String REGISTRY = "registry";

    private PullParams pullParams;

    @Test
    public void shouldCreateParamsObjectWithRequiredParameters() {
        pullParams = PullParams.from(IMAGE);

        assertEquals(pullParams.getImage(), IMAGE);

        assertNull(pullParams.getTag());
        assertNull(pullParams.getRegistry());
    }

    @Test
    public void shouldCreateParamsObjectWithAllPossibleParameters() {
        pullParams = PullParams.from(IMAGE)
                               .withTag(TAG)
                               .withRegistry(REGISTRY);

        assertEquals(pullParams.getImage(), IMAGE);
        assertEquals(pullParams.getTag(), TAG);
        assertEquals(pullParams.getRegistry(), REGISTRY);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNullPointerExceptionIfImageRequiredParameterIsNull() {
        pullParams = PullParams.from(null);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNullPointerExceptionIfImageRequiredParameterResetWithNull() {
        pullParams = PullParams.from(IMAGE)
                               .withImage(null);
    }

    @Test
    public void tagParameterShouldEqualsNullIfItNotSet() {
        pullParams = PullParams.from(IMAGE)
                               .withRegistry(REGISTRY);

        assertNull(pullParams.getTag());
    }

    @Test
    public void registryParameterShouldEqualsNullIfItNotSet() {
        pullParams = PullParams.from(IMAGE)
                               .withTag(TAG);

        assertNull(pullParams.getRegistry());
    }

}
