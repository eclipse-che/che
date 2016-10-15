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
package org.eclipse.che.plugin.maven.server;

import org.eclipse.che.api.project.shared.dto.event.PomModifiedEventDto;
import org.eclipse.che.api.vfs.impl.file.event.HiEvent;
import org.eclipse.che.api.vfs.impl.file.event.HiEventServerPublisher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;

import static java.io.File.separator;
import static org.eclipse.che.api.project.shared.dto.event.FileWatcherEventType.CREATED;
import static org.eclipse.che.api.project.shared.dto.event.FileWatcherEventType.MODIFIED;
import static org.eclipse.che.api.vfs.impl.file.event.HiEvent.Category.PROJECT_INFRASTRUCTURE;
import static org.eclipse.che.api.vfs.impl.file.event.LoEvent.ItemType.DIR;
import static org.eclipse.che.api.vfs.impl.file.event.LoEvent.ItemType.FILE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test for {@link PomModifiedHiEventDetector}
 *
 * @author Dmitry Kuleshov
 * @since 4.5
 */
@RunWith(MockitoJUnitRunner.class)
public class PomModifiedHiEventDetectorTest extends HiVfsEventDetectorTestHelper {

    private static final String CHE_PATH = separator + "che" + separator;
    private static final String FOLDER_1 = CHE_PATH + "folder1" + separator;
    private static final String PATH_2   = CHE_PATH + "folder2" + separator;
    private static final String TEST     = "test";
    private static final String POM_XML  = "pom.xml";
    @Mock
    private HiEventServerPublisher hiVfsEventServerPublisher;

    private PomModifiedHiEventDetector pomModifiedHiVfsEventDetector;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        pomModifiedHiVfsEventDetector = new PomModifiedHiEventDetector(hiVfsEventServerPublisher);
    }

    @Test
    public void shouldReturnEmptyListBecauseNoPomFileFound() {
        assertFalse(pomModifiedHiVfsEventDetector.detect(root).isPresent());
    }

    @Test
    public void shouldReturnEmptyListBecauseNoRelevantPomFileFound() {
        addEvent(POM_XML, FOLDER_1 + POM_XML, CREATED, FILE);
        addEvent(POM_XML, PATH_2 + POM_XML, MODIFIED, DIR);
        addEvent(TEST, CHE_PATH + POM_XML + separator + TEST, MODIFIED, FILE);

        assertFalse(pomModifiedHiVfsEventDetector.detect(root).isPresent());
    }

    @Test
    public void shouldReturnListWithPomEvent() {
        addEvent(POM_XML, FOLDER_1 + POM_XML, CREATED, FILE);
        addEvent(POM_XML, FOLDER_1 + POM_XML, MODIFIED, FILE);
        addEvent(POM_XML, PATH_2 + POM_XML, MODIFIED, DIR);
        addEvent(TEST, CHE_PATH + POM_XML + separator + TEST, MODIFIED, FILE);

        final Optional<HiEvent<PomModifiedEventDto>> eventOptional = pomModifiedHiVfsEventDetector.detect(root);
        assertTrue(eventOptional.isPresent());

        final HiEvent<PomModifiedEventDto> hiEvent = eventOptional.get();
        assertEquals(PROJECT_INFRASTRUCTURE, hiEvent.getCategory());
        assertEquals(FOLDER_1 + POM_XML, hiEvent.getDto().getPath());
    }

    @Test
    public void shouldReturnListWithPomWithHighestFsHierarchyPosition() {
        addEvent(POM_XML, FOLDER_1 + POM_XML, MODIFIED, FILE);
        addEvent(POM_XML, CHE_PATH + POM_XML, MODIFIED, FILE);

        final Optional<HiEvent<PomModifiedEventDto>> eventOptional = pomModifiedHiVfsEventDetector.detect(root);
        assertTrue(eventOptional.isPresent());

        final HiEvent<PomModifiedEventDto> hiEvent = eventOptional.get();
        assertEquals(CHE_PATH + POM_XML, hiEvent.getDto().getPath());
    }
}
