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

import org.eclipse.che.plugin.docker.client.dto.AuthConfigs;
import org.mockito.Mock;
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

    @Mock
    private AuthConfigs authConfigs;

    private PullParams pullParams;

    @Test
    public void shouldCreateParamsObjectWithRequiredParameters() {
        pullParams = PullParams.create(IMAGE);

        assertEquals(pullParams.getImage(), IMAGE);

        assertNull(pullParams.getTag());
        assertNull(pullParams.getRegistry());
        assertNull(pullParams.getAuthConfigs());
    }

    @Test
    public void shouldCreateParamsObjectWithAllPossibleParameters() {
        pullParams = PullParams.create(IMAGE)
                               .withTag(TAG)
                               .withRegistry(REGISTRY)
                               .withAuthConfigs(authConfigs);

        assertEquals(pullParams.getImage(), IMAGE);
        assertEquals(pullParams.getTag(), TAG);
        assertEquals(pullParams.getRegistry(), REGISTRY);
        assertEquals(pullParams.getAuthConfigs(), authConfigs);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNullPointerExceptionIfImageRequiredParameterIsNull() {
        pullParams = PullParams.create(null);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNullPointerExceptionIfImageRequiredParameterResetWithNull() {
        pullParams = PullParams.create(IMAGE)
                               .withImage(null);
    }

    @Test
    public void tagParameterShouldEqualsNullIfItNotSet() {
        pullParams = PullParams.create(IMAGE)
                               .withRegistry(REGISTRY)
                               .withAuthConfigs(authConfigs);

        assertNull(pullParams.getTag());
    }

    @Test
    public void registryParameterShouldEqualsNullIfItNotSet() {
        pullParams = PullParams.create(IMAGE)
                               .withTag(TAG)
                               .withAuthConfigs(authConfigs);

        assertNull(pullParams.getRegistry());
    }

    @Test
    public void AuthConfigsParameterShouldEqualsNullIfItNotSet() {
        pullParams = PullParams.create(IMAGE)
                               .withRegistry(REGISTRY)
                               .withTag(TAG);

        assertNull(pullParams.getAuthConfigs());
    }

    @Test
    public void getFullRepoShouldReturnRegistryAndImage() {
        pullParams = PullParams.create(IMAGE)
                               .withRegistry(REGISTRY)
                               .withTag(TAG);

        assertEquals(pullParams.getFullRepo(), REGISTRY + '/' + IMAGE);
    }

    @Test
    public void getFullRepoShouldReturnImageOnlyIfRegistryIsNotSet() {
        pullParams = PullParams.create(IMAGE)
                               .withTag(TAG);

        assertEquals(pullParams.getFullRepo(), IMAGE);
    }

    @Test
    public void getFullRepoShouldReturnImageOnlyIfRegistryIsDockerHub() {
        pullParams = PullParams.create(IMAGE)
                               .withRegistry("docker.io")
                               .withTag(TAG);

        assertEquals(pullParams.getFullRepo(), IMAGE);
    }

}
