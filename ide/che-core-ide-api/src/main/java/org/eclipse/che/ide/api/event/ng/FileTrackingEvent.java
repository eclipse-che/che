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
package org.eclipse.che.ide.api.event.ng;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

import org.eclipse.che.api.project.shared.dto.event.FileTrackingOperationDto;

import static org.eclipse.che.api.project.shared.dto.event.FileTrackingOperationDto.Type.MOVE;
import static org.eclipse.che.api.project.shared.dto.event.FileTrackingOperationDto.Type.RESUME;
import static org.eclipse.che.api.project.shared.dto.event.FileTrackingOperationDto.Type.START;
import static org.eclipse.che.api.project.shared.dto.event.FileTrackingOperationDto.Type.STOP;
import static org.eclipse.che.api.project.shared.dto.event.FileTrackingOperationDto.Type.SUSPEND;

/**
 * Consumed by {@link ClientServerEventService} and sent to server side so we could manage
 * server side VFS file watching from client.
 *
 * @author Dmitry Kuleshov
 */
public class FileTrackingEvent extends GwtEvent<FileTrackingEvent.FileTrackingEventHandler> {

    public static Type<FileTrackingEventHandler> TYPE = new Type<>();

    private final String path;

    private final String oldPath;

    private final FileTrackingOperationDto.Type type;

    private FileTrackingEvent(String path, String oldPath, FileTrackingOperationDto.Type type) {
        this.path = path;
        this.oldPath = oldPath;
        this.type = type;
    }

    public static FileTrackingEvent newFileTrackingSuspendEvent() {
        return new FileTrackingEvent(null, null, SUSPEND);
    }

    public static FileTrackingEvent newFileTrackingResumeEvent() {
        return new FileTrackingEvent(null, null, RESUME);
    }

    public static FileTrackingEvent newFileTrackingStartEvent(String path) {
        return new FileTrackingEvent(path, null, START);
    }

    public static FileTrackingEvent newFileTrackingStopEvent(String path) {
        return new FileTrackingEvent(path, null, STOP);
    }

    public static FileTrackingEvent newFileTrackingMoveEvent(String path, String oldPath) {
        return new FileTrackingEvent(path, oldPath, MOVE);
    }

    public String getOldPath() {
        return oldPath;
    }

    public FileTrackingOperationDto.Type getType() {
        return type;
    }

    public String getPath() {
        return path;
    }

    @Override
    public Type<FileTrackingEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(FileTrackingEventHandler handler) {
        handler.onEvent(this);
    }

    public interface FileTrackingEventHandler extends EventHandler {
        void onEvent(FileTrackingEvent event);
    }
}
