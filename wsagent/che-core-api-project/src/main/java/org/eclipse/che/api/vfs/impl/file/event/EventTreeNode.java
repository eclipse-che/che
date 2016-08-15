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

import org.eclipse.che.api.project.shared.dto.event.FileWatcherEventType;
import org.eclipse.che.api.vfs.impl.file.event.LoEvent.ItemType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Optional.empty;
import static java.util.stream.Stream.concat;
import static org.eclipse.che.api.vfs.impl.file.event.LoEvent.ItemType.DIR;
import static org.eclipse.che.api.vfs.impl.file.event.LoEvent.ItemType.FILE;
import static org.eclipse.che.api.vfs.impl.file.event.LoEvent.ItemType.UNDEFINED;

/**
 * Virtual file system event tree is a data structure to store low level VFS
 * events in a specific manner. Events are stored in a such way that an event
 * tree corresponds to a file system items tree but includes only those tree
 * branches that contain modified file system items.
 * <p>
 *     By design event tree represents a time-based event segment. All events
 *     close enough (in terms of time line) to each other are included to the
 *     tree.
 * </p>
 * <p>
 *     This class uses {@link LinkedHashMap} to store an event chain, not to
 *     lose data when we have several sequential modifications of a single
 *     tree item.
 * </p>
 * <p>
 *     Note: for convenience there is a predefined event tree root node - '/',
 *     which is defined by {@link EventTreeNode#ROOT_NODE_NAME} constant.
 *     All trees must be started from that node using corresponding factory
 *     method {@link EventTreeNode#newRootInstance()}
 * </p>
 *
 * @author Dmitry Kuleshov
 *
 * @since 4.5
 */
@Beta
public class EventTreeNode {

    /**
     * Tree root node name. All properly constructed trees must be started from this node.
     */
    private static final String ROOT_NODE_NAME = "/";

    private List<EventTreeNode>             children;
    private String                          name;
    private String                          path;
    private ItemType                        type;
    /**
     * Event chain to store all events occurred within a single time
     * segment with this event tree node instance.
     * Key - timestamp in millis, value - event type
     */
    private Map<Long, FileWatcherEventType> events;

    private EventTreeNode() {
        this.events = new LinkedHashMap<>();
        this.children = new LinkedList<>();
        this.type = UNDEFINED;
    }

    public static EventTreeNode newRootInstance() {
        return EventTreeNode.newInstance().withName(ROOT_NODE_NAME);
    }

    public static EventTreeNode newInstance() {
        return new EventTreeNode();
    }

    public EventTreeNode withName(String name) {
        this.name = name;
        return this;
    }

    public EventTreeNode withParent(EventTreeNode parent) {
        parent.withChild(this);
        return this;
    }


    public EventTreeNode withChild(EventTreeNode child) {
        this.children.add(child);
        return this;
    }

    public EventTreeNode withEvent(LoEvent loEvent) {
        this.events.put(loEvent.getTime(), loEvent.getEventType());
        return this;
    }

    public EventTreeNode withPath(String path) {
        this.path = path;
        return this;
    }

    public EventTreeNode withType(ItemType type) {
        this.type = type;
        return this;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public ItemType getType() {
        return type;
    }

    public List<EventTreeNode> getChildren() {
        return children;
    }

    public Optional<EventTreeNode> getChild(String name) {
        for (EventTreeNode node : children) {
            if (node.getName().equals(name)) {
                return Optional.of(node);
            }
        }

        return empty();
    }

    public Optional<EventTreeNode> getFirstChild() {
        if (children.isEmpty()) {
            return empty();
        }

        return Optional.of(children.get(0));
    }

    public Map<Long, FileWatcherEventType> getEvents() {
        return events;
    }

    public FileWatcherEventType getLastEventType() {
        final List<Map.Entry<Long, FileWatcherEventType>> entryList = new ArrayList<>(events.entrySet());
        final Map.Entry<Long, FileWatcherEventType> lastEntry = entryList.get(entryList.size() - 1);

        return lastEntry.getValue();
    }

    public boolean modificationOccurred() {
        return !events.isEmpty();
    }

    public boolean isFile() {
        return type.equals(FILE);
    }

    public boolean isDir() {
        return type.equals(DIR);
    }

    public boolean isRoot() {
        return ROOT_NODE_NAME.equals(name);
    }

    public Stream<EventTreeNode> stream() {
        return concat(Stream.of(this), this.children.stream().flatMap(EventTreeNode::stream));
    }
}
