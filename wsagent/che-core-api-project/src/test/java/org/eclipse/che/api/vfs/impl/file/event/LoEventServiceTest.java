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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;

import static java.io.File.separator;
import static java.lang.System.currentTimeMillis;
import static java.lang.Thread.sleep;
import static org.eclipse.che.api.project.shared.dto.event.FileWatcherEventType.MODIFIED;
import static org.eclipse.che.api.vfs.impl.file.event.LoEvent.ItemType.FILE;
import static org.eclipse.che.api.vfs.impl.file.event.LoEvent.newInstance;
import static org.eclipse.che.api.vfs.impl.file.event.LoEventService.MAX_EVENT_INTERVAL_MILLIS;
import static org.eclipse.che.api.vfs.impl.file.event.LoEventService.MAX_TIME_SEGMENT_SIZE_MILLIS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test for {@link LoEventService}
 *
 * @author Dmitry Kuleshov
 *
 * @since 4.5
 */
@RunWith(MockitoJUnitRunner.class)
public class LoEventServiceTest {
    private static final long   TIMEOUT     = 2 * MAX_EVENT_INTERVAL_MILLIS;
    private static final String FOLDER_NAME = "folder";
    private static final String FILE_NAME   = "file";
    private static final String PATH        = separator + FOLDER_NAME + separator + FILE_NAME;

    @Spy
    private LoEventQueueHolder   loEventQueueHolder;
    @Spy
    private EventTreeQueueHolder eventTreeQueueHolder;
    @InjectMocks
    private LoEventService       loEventService;

    @Before
    public void setUp() throws Exception {
        loEventService.postConstruct();
    }

    @After
    public void tearDown() throws Exception {
        loEventService.preDestroy();
    }

    @Test
    public void shouldConsumeSimpleEventSegmentAndProduceEventTree() throws Exception {
        final LoEvent loEvent = getLoEvent(PATH, FILE_NAME);
        loEventQueueHolder.put(loEvent);
        final Optional<EventTreeNode> rootOptional = eventTreeQueueHolder.take();

        assertNotNull(rootOptional);
        assertTrue(rootOptional.isPresent());

        final EventTreeNode root = rootOptional.get();
        assertTrue(root.isRoot());
        assertFalse(root.modificationOccurred());

        final Optional<EventTreeNode> dirOptional = root.getChild(FOLDER_NAME);
        assertNotNull(dirOptional);
        assertTrue(dirOptional.isPresent());

        final EventTreeNode dir = dirOptional.get();
        assertEquals(1, dir.getChildren().size());
        assertFalse(dir.modificationOccurred());

        final Optional<EventTreeNode> fileOptional = dir.getChild(FILE_NAME);

        assertNotNull(fileOptional);
        assertTrue(fileOptional.isPresent());

        final EventTreeNode file = fileOptional.get();
        assertTrue(file.modificationOccurred());
        assertEquals(MODIFIED, file.getLastEventType());
        assertTrue(file.isFile());
        assertEquals(PATH, file.getPath());
        assertEquals(FILE_NAME, file.getName());
    }

    @Test
    public void shouldConsumeTwoEventSegmentsAndProduceTwoEventTries() throws Exception {
        final LoEvent loEventI = getLoEvent(separator + FOLDER_NAME + 1 + separator + FILE_NAME + 1, FILE_NAME + 1);
        loEventQueueHolder.put(loEventI);
        final Optional<EventTreeNode> rootOptionalI = eventTreeQueueHolder.take();

        sleep(TIMEOUT); // this is required to properly simulate delay between events, should exceed max event interval

        final LoEvent loEventII = getLoEvent(separator + FOLDER_NAME + 2 + separator + FILE_NAME + 2, FILE_NAME + 2);
        loEventQueueHolder.put(loEventII);
        final Optional<EventTreeNode> rootOptionalII = eventTreeQueueHolder.take();

        assertNotNull(rootOptionalI);
        assertTrue(rootOptionalI.isPresent());
        assertNotNull(rootOptionalII);
        assertTrue(rootOptionalII.isPresent());

        final EventTreeNode rootI = rootOptionalI.get();
        final EventTreeNode rootII = rootOptionalII.get();

        assertEquals(1, rootI.getChildren().size());
        assertEquals(1, rootII.getChildren().size());

        final Optional<EventTreeNode> folderOptionalI = rootI.getChild(FOLDER_NAME + 1);
        final Optional<EventTreeNode> folderOptionalII = rootII.getChild(FOLDER_NAME + 2);

        assertNotNull(folderOptionalI);
        assertTrue(folderOptionalI.isPresent());
        assertNotNull(folderOptionalI);
        assertTrue(folderOptionalII.isPresent());

        final EventTreeNode folderI = folderOptionalI.get();
        final EventTreeNode folderII = folderOptionalII.get();

        assertEquals(1, folderI.getChildren().size());
        assertEquals(1, folderII.getChildren().size());

        final Optional<EventTreeNode> fileOptionalI = folderI.getChild(FILE_NAME + 1);
        final Optional<EventTreeNode> fileOptionalII = folderII.getChild(FILE_NAME + 2);

        assertNotNull(fileOptionalI);
        assertTrue(fileOptionalI.isPresent());
        assertNotNull(fileOptionalII);
        assertTrue(fileOptionalII.isPresent());

        final EventTreeNode fileI = fileOptionalI.get();
        final EventTreeNode fileII = fileOptionalII.get();

        assertEquals(FILE_NAME + 1, fileI.getName());
        assertEquals(FILE_NAME + 2, fileII.getName());
    }

    @Test
    public void shouldCreateEventTreeBecauseOfExceedingEventSegmentTimeFrame() throws Exception {
        final long sleepTime = 150;
        final long start = currentTimeMillis();

        long totalCounter = 0;
        long firstSegmentCounter = 0;

        while ((currentTimeMillis() - start) < MAX_TIME_SEGMENT_SIZE_MILLIS + MAX_EVENT_INTERVAL_MILLIS) {
            if ((firstSegmentCounter == 0) &&  ((currentTimeMillis() - start) > MAX_TIME_SEGMENT_SIZE_MILLIS)) {
                firstSegmentCounter = totalCounter;
            }
            totalCounter++;
            loEventQueueHolder.put(getLoEvent(PATH + totalCounter, FILE_NAME + totalCounter));
            sleep(sleepTime);
        }

        final Optional<EventTreeNode> rootOptionalI = eventTreeQueueHolder.take();
        final Optional<EventTreeNode> rootOptionalII = eventTreeQueueHolder.take();
        assertTrue(rootOptionalI.isPresent());
        assertTrue(rootOptionalII.isPresent());

        final Optional<EventTreeNode> folderI = rootOptionalI.get().getFirstChild();
        final Optional<EventTreeNode> folderII = rootOptionalII.get().getFirstChild();
        assertTrue(folderI.isPresent());
        assertTrue(folderII.isPresent());

        assertEquals(firstSegmentCounter, folderI.get().getChildren().size());
        assertEquals(totalCounter - firstSegmentCounter, folderII.get().getChildren().size());
    }

    private LoEvent getLoEvent(String path, String name) {
        return newInstance().withName(name)
                            .withPath(path)
                            .withEventType(MODIFIED)
                            .withItemType(FILE)
                            .withTime(currentTimeMillis());
    }
}
