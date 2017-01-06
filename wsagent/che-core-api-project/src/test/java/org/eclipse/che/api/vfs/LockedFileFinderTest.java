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
package org.eclipse.che.api.vfs;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LockedFileFinderTest {

    @Test
    public void findsLockedFiles() throws Exception {
        VirtualFile lockedFileAA = mockLockFile("/a/locked-file");
        VirtualFile fileAB = mockFile("/a/file");
        VirtualFile fileBA = mockFile("/a/b/file");
        VirtualFile lockedFileBB = mockLockFile("/a/b/locked-file");
        VirtualFile folderB = mockFolder("/a/b", fileBA, lockedFileBB);
        VirtualFile folderA = mockFolder("/a", folderB, fileAB, lockedFileAA);

        Set<VirtualFile> lockedFiles = newHashSet(new LockedFileFinder(folderA).findLockedFiles());
        assertEquals(newHashSet(lockedFileAA, lockedFileBB), lockedFiles );
    }

    private VirtualFile mockFile(String path) throws Exception {
        VirtualFile file = mock(VirtualFile.class);
        when(file.isFile()).thenReturn(true);
        when(file.getPath()).thenReturn(Path.of(path));
        when(file.toString()).thenReturn(path);
        accept(file);
        return file;
    }

    private VirtualFile mockLockFile(String path) throws Exception {
        VirtualFile file = mockFile(path);
        when(file.isLocked()).thenReturn(true);
        return file;
    }

    private VirtualFile mockFolder(String path, VirtualFile... children) throws Exception {
        VirtualFile folder = mock(VirtualFile.class);
        when(folder.isFolder()).thenReturn(true);
        when(folder.getPath()).thenReturn(Path.of(path));
        when(folder.getChildren()).thenReturn(newArrayList(children));
        when(folder.toString()).thenReturn(path);
        accept(folder);
        return folder;
    }

    private void accept(VirtualFile virtualFile) throws Exception {
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                ((VirtualFileVisitor)invocation.getArguments()[0]).visit(virtualFile);
                return null;
            }
        }).when(virtualFile).accept(any(VirtualFileVisitor.class));
    }
}