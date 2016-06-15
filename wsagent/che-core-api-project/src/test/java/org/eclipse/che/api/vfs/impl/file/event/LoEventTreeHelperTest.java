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

import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static java.io.File.separator;
import static org.eclipse.che.api.vfs.impl.file.event.EventTreeHelper.addEventAndCreatePrecedingNodes;
import static org.eclipse.che.api.vfs.impl.file.event.EventTreeHelper.getTreeNode;
import static org.eclipse.che.api.vfs.impl.file.event.EventTreeNode.newRootInstance;
import static org.eclipse.che.api.vfs.impl.file.event.LoEvent.newInstance;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Test for {@link EventTreeHelper}
 *
 * @author Dmitry Kuleshov
 *
 * @since 4.5
 */
public class LoEventTreeHelperTest {
    private static final String CHE  = "che";
    private static final String CHU  = "chu";
    private static final String CHA  = "cha";
    private static final String CHI  = "chi";
    private static final String PATH = separator + CHE + separator + CHU + separator + CHA + separator + CHI;

    private EventTreeNode root;

    @Before
    public void setUp() throws Exception {
        root = newRootInstance();
    }

    @Test
    public void shouldImplicitlyCreateAllAncestors() {
        final LoEvent loEvent = newInstance().withPath(PATH);
        addEventAndCreatePrecedingNodes(root, loEvent);
        testNode(testNode(testNode(testNode(root, CHE), CHU), CHA), CHI);
    }

    @Test
    public void shouldGetExistingTreeNode() {
        final LoEvent loEvent = newInstance().withName(CHI).withPath(PATH);

        addEventAndCreatePrecedingNodes(root, loEvent);

        final Optional<EventTreeNode> nodeOptional = getTreeNode(root, PATH);
        assertTrue(nodeOptional.isPresent());

        final EventTreeNode node = nodeOptional.get();
        assertEquals(loEvent.getPath(), node.getPath());
        assertEquals(loEvent.getName(), node.getName());
    }

    @Test
    public void shouldNotGetNonExistingTreeNode() {
        assertFalse(getTreeNode(root, CHE).isPresent());
    }

    private EventTreeNode testNode(EventTreeNode parent, String name) {
        final Optional<EventTreeNode> nodeOptional = parent.getFirstChild();
        assertTrue(nodeOptional.isPresent());

        final EventTreeNode node = nodeOptional.get();
        assertNotNull(node);
        assertEquals(name, node.getName());

        return node;
    }
}
