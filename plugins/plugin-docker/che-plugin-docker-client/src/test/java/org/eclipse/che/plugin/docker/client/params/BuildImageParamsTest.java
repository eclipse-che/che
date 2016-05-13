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

import org.eclipse.che.plugin.docker.client.dto.AuthConfigs;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 * @author Mykola Morhun
 */
public class BuildImageParamsTest {

    private static final String      REPOSITORY        = "repository";
    private static final AuthConfigs AUTH_CONFIGS      = mock(AuthConfigs.class);
    private static final Boolean     DO_FORCE_PULL     = true;
    private static final Long        MEMORY_LIMIT      = 12345L;
    private static final Long        MEMORY_SWAP_LIMIT = 67890L;
    private static final File        FILE              = new File(".");
    private static final List<File>  FILES;

    static {
        FILES = new ArrayList<>();
        FILES.add(FILE);
    }

    private BuildImageParams buildImageParams;

    @BeforeMethod
    private void prepare() {
        buildImageParams = BuildImageParams.create(FILE);
    }

    @Test
    public void shouldCreateParamsObjectWithRequiredParameters() {
        buildImageParams = BuildImageParams.create(FILE);

        assertEquals(buildImageParams.getFiles(), FILES);

        assertNull(buildImageParams.getRepository());
        assertNull(buildImageParams.getAuthConfigs());
        assertNull(buildImageParams.isDoForcePull());
        assertNull(buildImageParams.getMemoryLimit());
        assertNull(buildImageParams.getMemorySwapLimit());
    }

    @Test
    public void shouldCreateParamsObjectWithAllPossibleParameters() {
        buildImageParams = BuildImageParams.create(FILE)
                                           .withRepository(REPOSITORY)
                                           .withAuthConfigs(AUTH_CONFIGS)
                                           .withDoForcePull(DO_FORCE_PULL)
                                           .withMemoryLimit(MEMORY_LIMIT)
                                           .withMemorySwapLimit(MEMORY_SWAP_LIMIT);

        assertEquals(buildImageParams.getFiles(), FILES);
        assertEquals(buildImageParams.getRepository(), REPOSITORY);
        assertEquals(buildImageParams.getAuthConfigs(), AUTH_CONFIGS);
        assertEquals(buildImageParams.isDoForcePull(), DO_FORCE_PULL);
        assertEquals(buildImageParams.getMemoryLimit(), MEMORY_LIMIT);
        assertEquals(buildImageParams.getMemorySwapLimit(), MEMORY_SWAP_LIMIT);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNullPointerExceptionIfFilesRequiredParameterIsNull() {
        File file = null;
        buildImageParams = BuildImageParams.create(file);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNullPointerExceptionIfFilesRequiredParameterResetWithNull() {
        File file = null;
        buildImageParams.withFiles(file);
    }

    @Test (expectedExceptions = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionIfSetEmptyFilesArray() {
        File[] files = new File[0];
        buildImageParams.withFiles(files);
    }

    @Test (expectedExceptions = NullPointerException.class)
    public void shouldThrowNullPointerExceptionIfSetFilesArrayWithNullElement() {
        buildImageParams.withFiles(FILE, null, FILE);
    }

    @Test
    public void repositoryParameterShouldEqualsNullIfItNotSet() {
        buildImageParams.withAuthConfigs(AUTH_CONFIGS)
                        .withDoForcePull(DO_FORCE_PULL)
                        .withMemoryLimit(MEMORY_LIMIT)
                        .withMemorySwapLimit(MEMORY_SWAP_LIMIT);

        assertNull(buildImageParams.getRepository());
    }

    @Test
    public void authConfigParameterShouldEqualsNullIfItNotSet() {
        buildImageParams.withRepository(REPOSITORY)
                        .withDoForcePull(DO_FORCE_PULL)
                        .withMemoryLimit(MEMORY_LIMIT)
                        .withMemorySwapLimit(MEMORY_SWAP_LIMIT);

        assertNull(buildImageParams.getAuthConfigs());
    }

    @Test
    public void doForcePullParameterShouldEqualsNullIfItNotSet() {
        buildImageParams.withRepository(REPOSITORY)
                        .withAuthConfigs(AUTH_CONFIGS)
                        .withMemoryLimit(MEMORY_LIMIT)
                        .withMemorySwapLimit(MEMORY_SWAP_LIMIT);

        assertNull(buildImageParams.isDoForcePull());
    }

    @Test
    public void memoryLimitParameterShouldEqualsNullIfItNotSet() {
        buildImageParams.withRepository(REPOSITORY)
                        .withAuthConfigs(AUTH_CONFIGS)
                        .withDoForcePull(DO_FORCE_PULL)
                        .withMemorySwapLimit(MEMORY_SWAP_LIMIT);

        assertNull(buildImageParams.getMemoryLimit());
    }

    @Test
    public void memorySwapLimitParameterShouldEqualsNullIfItNotSet() {
        buildImageParams.withRepository(REPOSITORY)
                        .withAuthConfigs(AUTH_CONFIGS)
                        .withDoForcePull(DO_FORCE_PULL)
                        .withMemoryLimit(MEMORY_LIMIT);

        assertNull(buildImageParams.getMemorySwapLimit());
    }

    @Test (expectedExceptions = NullPointerException.class)
    public void shouldThrowNullPointerExceptionIfAddNullAsFile() {
        File file = null;
        buildImageParams.addFiles(file);
    }

    @Test (expectedExceptions = NullPointerException.class)
    public void shouldThrowNullPointerExceptionIfAddFilesArrayWithNullElement() {
        buildImageParams.addFiles(FILE, null, FILE);
    }

    @Test
    public void shouldAddFileToFilesList() {
        File file = new File("../");
        List<File> files = new ArrayList<>();
        files.add(FILE);
        files.add(file);

        buildImageParams.addFiles(file);

        assertEquals(buildImageParams.getFiles(), files);
    }

}
