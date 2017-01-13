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
package org.eclipse.che.api.project.server.handlers;

import org.eclipse.che.api.vfs.Path;
import org.eclipse.che.api.vfs.VirtualFile;
import org.eclipse.che.api.vfs.VirtualFileSystem;
import org.eclipse.che.api.vfs.VirtualFileSystemProvider;
import org.testng.annotations.Test;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *  @author Vitalii Parfonov
 */
public class CreateBaseProjectTypeHandlerTest {

    @Test
    public void testCreateProject() throws Exception {
        Path path = Path.of("test");
        VirtualFileSystemProvider virtualFileSystemProvider = mock(VirtualFileSystemProvider.class);
        VirtualFileSystem virtualFileSystem = mock(VirtualFileSystem.class);

        VirtualFile base = mock(VirtualFile.class);
        when(base.isRoot()).thenReturn(false);


        VirtualFile root = mock(VirtualFile.class);
        when(root.isRoot()).thenReturn(true);
        when(root.createFolder(anyString())).thenReturn(base);
        when(virtualFileSystem.getRoot()).thenReturn(root);


        when(virtualFileSystemProvider.getVirtualFileSystem()).thenReturn(virtualFileSystem);
        when(virtualFileSystem.getRoot()).thenReturn(root);


        CreateBaseProjectTypeHandler createBaseProjectTypeHandler = new CreateBaseProjectTypeHandler(virtualFileSystemProvider);
        createBaseProjectTypeHandler.onCreateProject(path, null, null);
        verify(root).createFolder("test");
    }
}
