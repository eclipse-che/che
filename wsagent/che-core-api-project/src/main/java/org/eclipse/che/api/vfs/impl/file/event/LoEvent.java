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
import org.eclipse.che.api.vfs.VirtualFileSystem;
import org.eclipse.che.api.vfs.impl.file.FileTreeWatcher;
import org.eclipse.che.api.vfs.impl.file.FileWatcherNotificationListener;

/**
 * Low level virtual file system event implementation designed to represent
 * trivial file system items manipulations ({@link FileWatcherEventType}).
 * Roughly speaking it stands for physical file system operations (create file,
 * edit file, create folder, etc.).
 *
 *  * <p>
 *     It is used for both file and folder related events ({@link ItemType}) and
 *     contains all relevant data (time, path, event and item types).
 * </p>
 * <p>
 *     Note: {@link LoEvent#getTime()} and {@link LoEvent#withTime(long)} methods
 *     are designed to deal with the time that corresponds to the moment we caught
 *     the event inside the application, not the real item modification time that
 *     comes from underlying file system. This is due to the fact that most file
 *     systems support timestamps accuracy within one second which is not enough
 *     for our purposes.
 * </p>
 *
 * @author Dmitry Kuleshov
 *
 * @since 4.5
 */
@Beta
public class LoEvent {

    /**
     * Time in milliseconds to represent the moment VFS event is fired by
     * {@link FileTreeWatcher} and caught by one of {@link FileWatcherNotificationListener}
     * implementations
     */
    private long                 time;
    /**
     * Absolute item path in terms of {@link VirtualFileSystem}. It is expected
     * that this path starts from root position of file system. In common sense
     * it is the folder obtained by {@link VirtualFileSystem#getRoot()} method.
     */
    private String               path;
    private String               name;
    private FileWatcherEventType eventType;
    private ItemType             itemType;

    private LoEvent() {
    }

    public static LoEvent newInstance() {
        return new LoEvent();
    }

    public ItemType getItemType() {
        return itemType;
    }

    public LoEvent withItemType(ItemType itemType) {
        this.itemType = itemType;
        return this;
    }

    public long getTime() {
        return time;
    }

    public LoEvent withTime(long time) {
        this.time = time;
        return this;
    }

    public String getPath() {
        return path;
    }

    public LoEvent withPath(String path) {
        this.path = path;
        return this;
    }

    public String getName() {
        return name;
    }

    public LoEvent withName(String name) {
        this.name = name;
        return this;
    }

    public FileWatcherEventType getEventType() {
        return eventType;
    }

    public LoEvent withEventType(FileWatcherEventType eventType) {
        this.eventType = eventType;
        return this;
    }

    public enum ItemType {
        FILE, DIR, UNDEFINED
    }
}
