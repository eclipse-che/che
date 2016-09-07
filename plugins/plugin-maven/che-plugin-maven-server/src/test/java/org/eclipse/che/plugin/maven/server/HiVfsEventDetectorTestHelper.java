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

import org.eclipse.che.api.project.shared.dto.event.FileWatcherEventType;
import org.eclipse.che.api.vfs.impl.file.event.EventTreeNode;
import org.eclipse.che.api.vfs.impl.file.event.LoEvent;
import org.eclipse.che.api.vfs.impl.file.event.LoEvent.ItemType;

import static org.eclipse.che.api.vfs.impl.file.event.EventTreeHelper.addEventAndCreatePrecedingNodes;
import static org.eclipse.che.api.vfs.impl.file.event.EventTreeNode.newRootInstance;
import static org.eclipse.che.api.vfs.impl.file.event.LoEvent.newInstance;

/**
 * @author Dmitry Kuleshov
 * @since 4.5
 */
abstract class HiVfsEventDetectorTestHelper {

    protected EventTreeNode root;

    public void setUp() throws Exception {
        root = newRootInstance();
    }

    void addEvent(String name,
                  String path,
                  FileWatcherEventType eventType,
                  ItemType itemType) {

        LoEvent loEvent = newInstance().withName(name)
                                       .withPath(path)
                                       .withEventType(eventType)
                                       .withItemType(itemType)
                                       .withTime(System.currentTimeMillis());

        addEventAndCreatePrecedingNodes(root, loEvent);
    }
}
