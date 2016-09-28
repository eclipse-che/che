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
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link ReadmeInjectionHandler}
 *
 * @author Dmitry Kuleshov
 */
@RunWith(MockitoJUnitRunner.class)
public class ReadmeInjectionHandlerTest {
    @Mock
    private  ReadmeInjectionVerifier  verifier;
    @Mock
    private  ReadmeInjectionPerformer performer;
    @InjectMocks
    private ReadmeInjectionHandler injectionHandler;

    @Mock
    private FolderEntry projectFolder;

    @Before
    public void setUp() throws ServerException {
        when(verifier.isReadmeNotPresent(any())).thenReturn(true);
        when(verifier.isRootProject(any())).thenReturn(true);
    }

    @Test
    public void shouldCheckIfRootProject(){
        injectionHandler.handleReadmeInjection(projectFolder);

        verify(verifier).isRootProject(projectFolder);
    }

    @Test
    public void shouldCheckIfReadmeIsNotPresent() throws ServerException {
        injectionHandler.handleReadmeInjection(projectFolder);

        verify(verifier).isReadmeNotPresent(projectFolder);
    }

    @Test
    public void shouldRunInjectionIfVerificationOk() throws IOException, ForbiddenException, ConflictException, ServerException {
        injectionHandler.handleReadmeInjection(projectFolder);

        verify(performer).injectReadmeTo(projectFolder);
    }

    @Test
    public void shouldNotRunInjectionIfIsNotRoot() throws IOException, ForbiddenException, ConflictException, ServerException {
        when(verifier.isRootProject(any())).thenReturn(false);

        injectionHandler.handleReadmeInjection(projectFolder);

        verify(performer,never()).injectReadmeTo(projectFolder);
    }

    @Test
    public void shouldNotRunInjectionIfReadmeIsPresent() throws ServerException, ConflictException, IOException, ForbiddenException {
        when(verifier.isReadmeNotPresent(any())).thenReturn(false);

        injectionHandler.handleReadmeInjection(projectFolder);

        verify(performer,never()).injectReadmeTo(projectFolder);
    }
}
