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
public class RemoveImageParamsTest {
    
    private static final String  IMAGE          = "image";
    private static final Boolean FORCE          = Boolean.FALSE;

    private RemoveImageParams removeImageParams;

    @Test
    public void shouldCreateParamsObjectWithRequiredParameters() {
        removeImageParams = RemoveImageParams.create(IMAGE);

        assertEquals(removeImageParams.getImage(), IMAGE);

        assertNull(removeImageParams.isForce());
    }

    @Test
    public void shouldCreateParamsObjectWithAllPossibleParameters() {
        removeImageParams = RemoveImageParams.create(IMAGE)
                                             .withForce(FORCE);

        assertEquals(removeImageParams.getImage(), IMAGE);
        assertEquals(removeImageParams.isForce(), FORCE);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNullPointerExceptionIfImageRequiredParameterIsNull() {
        removeImageParams = RemoveImageParams.create(null);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNullPointerExceptionIfImageRequiredParameterResetWithNull() {
        removeImageParams = RemoveImageParams.create(IMAGE)
                                             .withImage(null);
    }

}
