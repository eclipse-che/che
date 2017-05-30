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

import com.google.common.hash.Hashing;
import com.google.common.io.ByteSource;

import org.eclipse.che.commons.lang.Pair;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.ByteArrayInputStream;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HashSumsCounterTest {

    @Test
    public void countsHashSums() throws Exception {
        VirtualFile fileAB = mockFile("/a/file", "file1".getBytes());
        VirtualFile fileBA = mockFile("/a/b/file", "file2".getBytes());
        VirtualFile folderB = mockFolder("/a/b", fileBA);
        VirtualFile folderA = mockFolder("/a", folderB, fileAB);
        Set<Pair<String, String>> expected = newHashSet(Pair.of(countMd5Sum("file1".getBytes()), "file"),
                                                        Pair.of(countMd5Sum("file2".getBytes()), "b/file"));

        Set<Pair<String, String>> hashSums = newHashSet(new HashSumsCounter(folderA, Hashing.md5()).countHashSums());

        assertEquals(expected, hashSums);
    }

    private String countMd5Sum(byte[] bytes) throws Exception {
        return ByteSource.wrap(bytes).hash(Hashing.md5()).toString();
    }

    private VirtualFile mockFile(String path, byte[] content) throws Exception {
        VirtualFile file = mock(VirtualFile.class);
        when(file.isFile()).thenReturn(true);
        when(file.getPath()).thenReturn(Path.of(path));
        when(file.toString()).thenReturn(path);
        when(file.getContent()).thenReturn(new ByteArrayInputStream(content));
        accept(file);
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