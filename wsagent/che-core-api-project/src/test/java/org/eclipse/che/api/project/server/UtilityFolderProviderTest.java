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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.eclipse.che.api.project.server.UtilityFolderProvider.DEFAULT_FOLDER_NAME;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link UtilityFolderProvider}
 *
 * @author Dmitry Kuleshov
 */
@RunWith(MockitoJUnitRunner.class)
public class UtilityFolderProviderTest {
    private final static String DEFINED_FOLDER_NAME     = ".che";

    private UtilityFolderProvider utilityFolderProvider;

    @Mock
    private FolderEntry folderEntry;

    @Before
    public void setUp() throws Exception {
        utilityFolderProvider = new UtilityFolderProvider();
    }

    @Test
    public void shouldGetExistingFolder() throws ServerException, ConflictException, ForbiddenException {
        when(this.folderEntry.getChildFolder(DEFINED_FOLDER_NAME)).thenReturn(this.folderEntry);

        final FolderEntry folderEntry = utilityFolderProvider.get(this.folderEntry);

        verify(this.folderEntry).getChildFolder(DEFINED_FOLDER_NAME);
        verify(this.folderEntry, never()).createFolder(DEFINED_FOLDER_NAME);

        assertEquals(this.folderEntry, folderEntry);
    }

    @Test
    public void shouldCreateNonExistingFolder() throws ServerException, ConflictException, ForbiddenException {
        when(this.folderEntry.getChildFolder(DEFINED_FOLDER_NAME)).thenReturn(null);
        when(this.folderEntry.createFolder(DEFINED_FOLDER_NAME)).thenReturn(this.folderEntry);

        final FolderEntry folderEntry = utilityFolderProvider.get(this.folderEntry);

        verify(this.folderEntry).getChildFolder(DEFINED_FOLDER_NAME);
        verify(this.folderEntry).createFolder(DEFINED_FOLDER_NAME);

        assertEquals(this.folderEntry, folderEntry);
    }
}
