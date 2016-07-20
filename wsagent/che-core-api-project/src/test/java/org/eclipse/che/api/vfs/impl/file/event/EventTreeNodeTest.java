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

import org.eclipse.che.api.project.shared.dto.event.FileWatcherEventType;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static java.io.File.separator;
import static org.eclipse.che.api.project.shared.dto.event.FileWatcherEventType.CREATED;
import static org.eclipse.che.api.project.shared.dto.event.FileWatcherEventType.MODIFIED;
import static org.eclipse.che.api.vfs.impl.file.event.LoEvent.ItemType.FILE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test for {@link EventTreeNode}
 *
 * @author Dmitry Kuleshov
 *
 * @since 4.5
 */
public class EventTreeNodeTest {

    private static final String TEST_PATH    = separator + "che" + separator + "file";
    private static final String CHILD_NAME_1 = "Child 1";
    private static final String CHILD_NAME_2 = "Child 2";

    private EventTreeNode root;
    private EventTreeNode child;
    private EventTreeNode anotherChild;

    @Before
    public void setUp() {
        root = EventTreeNode.newRootInstance();
        child = EventTreeNode.newInstance();
        anotherChild = EventTreeNode.newInstance();
    }

    @Test
    public void shouldProperlyAddChild() {
        root.withChild(child);

        final Optional<EventTreeNode> optional = root.getFirstChild();

        assertTrue(optional.isPresent());
        assertEquals(optional.get(), child);
        assertEquals(1, root.getChildren().size());
    }

    @Test
    public void shouldProperlyAddEvent() {
        long time = System.currentTimeMillis();

        assertTrue(child.getEvents().isEmpty());

        child.withEvent(getLoVfsEvent(time, CREATED));

        assertEquals(1, child.getEvents().size());
        assertNotNull(child.getEvents().get(time));
    }

    @Test
    public void shouldProperlyGetFirstChild() {
        root.withChild(child.withName(CHILD_NAME_1));
        root.withChild(anotherChild.withName(CHILD_NAME_2));

        assertTrue(root.getFirstChild().isPresent());
        assertEquals(CHILD_NAME_1, root.getFirstChild().get().getName());
    }

    @Test
    public void shouldProperlyGetLastEventType() {
        child.withEvent(getLoVfsEvent(System.currentTimeMillis(), CREATED));
        child.withEvent(getLoVfsEvent(System.currentTimeMillis(), MODIFIED));

        assertEquals(MODIFIED, child.getLastEventType());
    }

    private LoEvent getLoVfsEvent(long time, FileWatcherEventType type) {
        return LoEvent.newInstance()
                      .withPath(TEST_PATH)
                      .withTime(time)
                      .withItemType(FILE)
                      .withEventType(type);
    }

}
