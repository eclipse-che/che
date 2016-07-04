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

import com.google.common.annotations.Beta;

import org.eclipse.che.api.vfs.Path;

import java.util.Optional;

import static java.io.File.separator;
import static org.eclipse.che.api.vfs.impl.file.event.EventTreeNode.newInstance;

/**
 * Helper for event tree related operations.
 *
 * @see {@link EventTreeNode}
 *
 * @author Dmitry Kuleshov
 *
 * @since 4.5
 */
@Beta
public class EventTreeHelper {

    /**
     * Adds corresponding event to an event tree.
     * <p>
     *     If event's tree parents are not yet in the tree they are implicitly created
     *     with default values and no events. For tree nodes created in such way call
     *     of {@link EventTreeNode#modificationOccurred()} method always return false.
     * </p>
     * @param root root node of the tree, node where event's absolute path starts
     *
     * @param loEvent event to be added
     */
    public static void addEventAndCreatePrecedingNodes(EventTreeNode root, LoEvent loEvent) {
        traverseAndCreate(root, Path.of(loEvent.getPath()))
                .withEvent(loEvent)
                .withPath(loEvent.getPath())
                .withType(loEvent.getItemType());
    }

    /**
     * Get a tree node according to a relative path starting from a predefined tree node.
     *
     * @param root node where relative path starts
     * @param relativePath relative path
     *
     * @return node if node with such path exists otherwise {@code null}
     */
    public static Optional<EventTreeNode> getTreeNode(EventTreeNode root, String relativePath) {
        Optional<EventTreeNode> current = Optional.of(root);

        if (relativePath.startsWith(separator)) {
            relativePath = relativePath.substring(1);
        }

        if (relativePath.endsWith(separator)) {
            relativePath = relativePath.substring(0, relativePath.length() - 1);
        }

        for (String segment : relativePath.split(separator)) {
            if (current.isPresent()) {
                current = current.get().getChild(segment);
            }
        }

        return current;
    }

    private static EventTreeNode traverseAndCreate(EventTreeNode root, Path path) {
        EventTreeNode current = root;

        for (int i = 0; i < path.length(); i++) {
            final String name = path.element(i);
            final EventTreeNode parent = current;
            final Optional<EventTreeNode> childOptional = current.getChild(name);

            current = childOptional.orElseGet(() -> newInstance().withName(name).withParent(parent));
        }

        return current;
    }
}
