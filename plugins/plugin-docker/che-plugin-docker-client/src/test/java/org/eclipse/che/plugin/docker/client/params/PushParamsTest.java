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
public class PushParamsTest {

    private static final String REPOSITORY = "repository";
    private static final String TAG        = "tag";
    private static final String REGISTRY   = "registry";

    private PullParams pullParams;

    @Test
    public void shouldCreateParamsObjectWithRequiredParameters() {
        pullParams = PullParams.from(REPOSITORY);

        assertEquals(pullParams.getImage(), REPOSITORY);

        assertNull(pullParams.getTag());
        assertNull(pullParams.getRegistry());
    }

    @Test
    public void shouldCreateParamsObjectWithAllPossibleParameters() {
        pullParams = PullParams.from(REPOSITORY)
                               .withTag(TAG)
                               .withRegistry(REGISTRY);

        assertEquals(pullParams.getImage(), REPOSITORY);
        assertEquals(pullParams.getTag(), TAG);
        assertEquals(pullParams.getRegistry(), REGISTRY);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNullPointerExceptionIfRepositoryRequiredParameterIsNull() {
        pullParams = PullParams.from(null);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNullPointerExceptionIfRepositoryRequiredParameterResetWithNull() {
        pullParams = PullParams.from(REPOSITORY)
                               .withImage(null);
    }

    @Test
    public void tagParameterShouldEqualsNullIfItNotSet() {
        pullParams = PullParams.from(REPOSITORY)
                               .withRegistry(REGISTRY);

        assertNull(pullParams.getTag());
    }

    @Test
    public void registryParameterShouldEqualsNullIfItNotSet() {
        pullParams = PullParams.from(REPOSITORY)
                               .withTag(TAG);

        assertNull(pullParams.getRegistry());
    }

}
