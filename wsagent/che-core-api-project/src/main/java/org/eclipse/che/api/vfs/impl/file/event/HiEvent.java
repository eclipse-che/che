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

import java.util.LinkedList;
import java.util.List;

/**
 * High level virtual file system event implementation. Unlike {@link LoEvent}
 * it is to represent not physical but logical events. In terms of file system
 * logical events are those that may be caused by one or more physical events.
 * In other words sets of low level VFS events result in high level VFS events.
 * <p>
 *     Small example: creation of project's root folder ({@link LoEvent})
 *     along with populating it with content (creating new files and folders -
 *     also {@link LoEvent}) indicates that most likely a project is imported.
 *     And project import is a {@link HiEvent}.
 * </p>
 * <p>
 *     Note: We consider that the user of this class is responsible for setting
 *     proper broadcasters and (if it is required) all additional information
 *     (e.g. web socket channel).
 * </p>
 * <p>
 *     Each high level VFS event instance can have arbitrary number (but not less
 *     than one) of {@link HiEventBroadcaster} defined. By design an instance
 *     of {@link HiEventDetector} which is responsible for creating of event
 *     is also responsible for definition of a list of corresponding broadcasters.
 * </p>
 *
 * @author Dmitry Kuleshov
 *
 * @since 4.5
 */
@Beta
public class HiEvent<T> {
    /**
     * Should be a plain DTO object representing this event. Is designed to be
     * further used both for client-side and server-side notifications.
     * <p>
     *     Note: The implementation of DTO should fulfill all restrictions and
     *     be located in {@code org.eclipse.che.api.project.shared.dto.event}
     *     package
     * </p>
     */
    private T                        dto;
    /**
     * Field holds category property of an event. Please see {@link Category}
     */
    private Category                 category;
    private List<HiEventBroadcaster> broadcasters;
    /**
     * Web socket channel name.
     * <p>
     *     It is ignored if event is not expected to be sent over web socket
     *     (e.g. server side event)
     * </p>
     */
    private String                   channel;

    private HiEvent() {
        this.broadcasters = new LinkedList<>();
    }

    public static <T> HiEvent<T> newInstance(Class<T> type) {
        return new HiEvent<>();
    }

    public T getDto() {
        return dto;
    }

    public HiEvent<T> withDto(T dto) {
        this.dto = dto;
        return this;
    }

    public String getChannel() {
        return channel;
    }

    public HiEvent<T> withChannel(String channel) {
        this.channel = channel;
        return this;
    }

    public Category getCategory() {
        return category;
    }

    public HiEvent<T> withCategory(Category category) {
        this.category = category;
        return this;
    }

    public HiEvent<T> withBroadcaster(HiEventBroadcaster hiEventBroadcaster) {
        this.broadcasters.add(hiEventBroadcaster);
        return this;
    }

    public void broadcast() {
        broadcasters.stream().forEach(o -> o.broadcast(this));
    }


    /**
     * Simple enumeration to represent event categorizing mechanics.
     * <p>
     *     The idea is to allow only one high level event per category for
     *     a single low level events set (tree snapshot). If we have several
     *     high level events with the same category we are to choose the most
     *     appropriate event according to its priority.
     * </p>
     * <p>
     *     Note: UNDEFINED is used for all events that have no category.
     * </p>
     */
    @Beta
    public enum Category {
        UNDEFINED,
        PROJECT_INFRASTRUCTURE;

        private long priority;

        public long getPriority() {
            return priority;
        }

        public Category withPriority(long priority) {
            this.priority = priority;
            return this;
        }

    }
}
