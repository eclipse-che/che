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
package org.eclipse.che.api.project.server;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test for {@link ReadmeInjectionPerformer}
 *
 * @author Dmitry Kuleshov
 */
@RunWith(MockitoJUnitRunner.class)
public class ReadmeInjectionPerformerTest {
    @Mock
    private  UtilityFolderProvider utilityFolderProvider;
    @Mock
    private  ReadmeContentProvider readmeContentProvider;
    @InjectMocks
    private ReadmeInjectionPerformer readmeInjectionPerformer;

    @Mock
    private FolderEntry projectFolder;
    @Mock
    private FolderEntry utilityFolder;

    @Before
    public void setUp() throws ServerException, ForbiddenException, ConflictException {
        when(utilityFolderProvider.get(any())).thenReturn(utilityFolder);
    }

    @Test
    public void shouldRunFolderProvider() throws IOException, ForbiddenException, ConflictException, ServerException {
        readmeInjectionPerformer.injectReadmeTo(projectFolder);

        verify(utilityFolderProvider).get(projectFolder);

    }

    @Test
    public void shouldRunContentProvider() throws IOException, ForbiddenException, ConflictException, ServerException {
        readmeInjectionPerformer.injectReadmeTo(projectFolder);

        verify(readmeContentProvider).get();
        verify(readmeContentProvider).getFilename();

    }

    @Test
    public void shouldCreateFile() throws IOException, ForbiddenException, ConflictException, ServerException {
        final byte[] bytes = {};
        final String test = "test";

        when(readmeContentProvider.get()).thenReturn(bytes);
        when(readmeContentProvider.getFilename()).thenReturn(test);
        when(utilityFolderProvider.get(projectFolder)).thenReturn(utilityFolder);

        readmeInjectionPerformer.injectReadmeTo(projectFolder);

        verify(utilityFolder).createFile(test, bytes);
    }
}
