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
package org.eclipse.che.api.vfs.impl.file.event;

import org.eclipse.che.api.project.shared.dto.event.GitBranchCheckoutEventDto;
import org.eclipse.che.api.vfs.VirtualFile;
import org.eclipse.che.api.vfs.VirtualFileSystem;
import org.eclipse.che.api.vfs.VirtualFileSystemProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.util.Optional;

import static org.eclipse.che.api.project.shared.dto.event.FileWatcherEventType.CREATED;
import static org.eclipse.che.api.project.shared.dto.event.FileWatcherEventType.MODIFIED;
import static org.eclipse.che.api.vfs.impl.file.event.LoEvent.ItemType.FILE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test for {@link GitCheckoutHiEventDetector}
 *
 * @author Dmitry Kuleshov
 *
 * @since 4.5
 */
@RunWith(MockitoJUnitRunner.class)
public class GitCheckoutHiEventDetectorTest extends HiVfsEventDetectorTestHelper {

    private static final String PROJECT_NAME           = "project";
    private static final String HEAD_FILE_PATH         = ".git/HEAD";
    private static final String TEST_VALUE             = "TEST VALUE";
    private static final String HEAD_FILE_CONTENT      = "ref: refs" + File.separator + "heads" + File.separator + TEST_VALUE;
    private static final String HEAD_FILE_NAME         = "HEAD";
    private static final String GIT_OPERATIONS_CHANNEL = "git-operations-channel";

    @Mock
    private HiEventClientBroadcaster  hiVfsEventClientBroadcaster;
    @Mock
    private VirtualFileSystemProvider virtualFileSystemProvider;

    private GitCheckoutHiEventDetector gitCheckoutHiVfsEventDetector;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        gitCheckoutHiVfsEventDetector = new GitCheckoutHiEventDetector(virtualFileSystemProvider, hiVfsEventClientBroadcaster);
    }

    @Test
    public void shouldReturnEmptyListBecauseNoHeadFileModified() {
        assertFalse(gitCheckoutHiVfsEventDetector.detect(root).isPresent());
    }

    @Test
    public void shouldReturnEmptyListBecauseOfWrongHeadFileModification() {
        addEvent(HEAD_FILE_NAME, File.separator + PROJECT_NAME + File.separator + HEAD_FILE_PATH, CREATED, FILE);

        assertFalse(gitCheckoutHiVfsEventDetector.detect(root).isPresent());
    }

    @Test
    public void shouldReturnListWithCorrectEventBecauseHeadFileIsProperlyModified() throws Exception {
        VirtualFileSystem vfs = mock(VirtualFileSystem.class);
        VirtualFile file = mock(VirtualFile.class);

        when(virtualFileSystemProvider.getVirtualFileSystem()).thenReturn(vfs);
        when(vfs.getRoot()).thenReturn(file);
        when(file.getChild(any())).thenReturn(file);
        when(file.getContentAsString()).thenReturn(HEAD_FILE_CONTENT);

        addEvent(HEAD_FILE_NAME, File.separator + PROJECT_NAME + File.separator + HEAD_FILE_PATH, MODIFIED, FILE);

        Optional<HiEvent<GitBranchCheckoutEventDto>> optional = gitCheckoutHiVfsEventDetector.detect(root);
        assertTrue(optional.isPresent());

        HiEvent<GitBranchCheckoutEventDto> hiEvent = optional.get();
        assertEquals(GIT_OPERATIONS_CHANNEL, hiEvent.getChannel());
        assertEquals(TEST_VALUE, hiEvent.getDto().getBranchName());
    }
}
