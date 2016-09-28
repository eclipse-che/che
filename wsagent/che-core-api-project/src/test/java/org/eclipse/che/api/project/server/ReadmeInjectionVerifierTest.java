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

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.vfs.Path;
import org.eclipse.che.api.vfs.VirtualFile;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link ReadmeInjectionVerifier}
 *
 * @author Dmitry Kuleshov
 */
@RunWith(MockitoJUnitRunner.class)
public class ReadmeInjectionVerifierTest {

    private ReadmeInjectionVerifier readmeInjectionVerifier;

    @Mock
    private FolderEntry projectFolder;

    private Path projectPath;
    private Path projectParentPath;

    @Before
    public void setUp(){
        readmeInjectionVerifier = new ReadmeInjectionVerifier();
    }

    @Test
    public void shouldReturnTrueOnRootProject(){
        projectParentPath = Path.ROOT;
        projectPath = projectParentPath.newPath("project");
        when(projectFolder.getPath()).thenReturn(projectPath);

        final boolean rootProject = readmeInjectionVerifier.isRootProject(projectFolder);

        assertTrue(rootProject);

        verify(projectFolder).getPath();
    }

    @Test
    public void shouldReturnFalseOnNotRootProject(){
        projectParentPath = Path.ROOT.newPath("project");
        projectPath = projectParentPath.newPath("project");
        when(projectFolder.getPath()).thenReturn(projectPath);


        final boolean rootProject = readmeInjectionVerifier.isRootProject(projectFolder);

        assertFalse(rootProject);

        verify(projectFolder).getPath();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionOnImproperProjectLocation(){
        projectParentPath = Path.ROOT;
        projectPath = Path.ROOT;
        when(projectFolder.getPath()).thenReturn(projectPath);

        readmeInjectionVerifier.isRootProject(projectFolder);
    }

    @Test
    public void shouldReturnFalseIfReadmeIsPresent() throws ServerException {
        when(projectFolder.getChild(ReadmeInjectionVerifier.DEFAULT_README_NAME)).thenReturn(mock(VirtualFileEntry.class));

        final boolean readmeNotPresent = readmeInjectionVerifier.isReadmeNotPresent(projectFolder);

        assertFalse(readmeNotPresent);
    }

    @Test
    public void shouldReturnTrueIfReadmeIsNotPresent() throws ServerException {
        when(projectFolder.getChild(ReadmeInjectionVerifier.DEFAULT_README_NAME)).thenReturn(null);

        final boolean readmeNotPresent = readmeInjectionVerifier.isReadmeNotPresent(projectFolder);

        assertTrue(readmeNotPresent);
    }
}
