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
package org.eclipse.che.ide.api.event;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.parts.EditorTab;
import org.eclipse.che.ide.api.resources.VirtualFile;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

import static org.eclipse.che.ide.api.event.FileEvent.FileOperation.CLOSE;
import static org.eclipse.che.ide.api.event.FileEvent.FileOperation.OPEN;
import static org.eclipse.che.ide.api.event.FileEvent.FileOperation.SAVE;

/**
 * Event that describes the fact that file is going to be opened.
 *
 * @author Nikolay Zamosenchuk
 * @author Artem Zatsarynnyi
 * @author Roman Nikitenko
 */
public class FileEvent extends GwtEvent<FileEvent.FileEventHandler> {

    /** Handles OpenFileEvent */
    public interface FileEventHandler extends EventHandler {
        void onFileOperation(FileEvent event);
    }

    public static Type<FileEventHandler> TYPE = new Type<>();
    private VirtualFile   file;
    private FileOperation fileOperation;
    private EditorTab     tab;

    /**
     * Creates new {@link FileEvent} with info about virtual file.
     *
     * @param file
     *         {@link VirtualFile} that represents an affected file
     * @param fileOperation
     *         file operation
     */
    private FileEvent(VirtualFile file, FileOperation fileOperation) {
        this.file = file;
        this.fileOperation = fileOperation;
    }

    /**
     * Creates new {@link FileEvent} with info about editor tab.
     *
     * @param tab
     *         {@link EditorTab} that represents an affected file to perform {@code fileOperation}
     * @param fileOperation
     *         file operation
     */
    private FileEvent(EditorTab tab, FileOperation fileOperation) {
        this(tab.getFile(), fileOperation);
        this.tab = tab;
    }

    /**
     * Creates a event for {@code FileOperation.OPEN}.
     */
    public static FileEvent createOpenFileEvent(VirtualFile file) {
        return new FileEvent(file, OPEN);
    }

    /**
     * Creates a event for {@code FileOperation.CLOSE}.
     * Note: the pane which contains this {@code tab} will be closed when the pane doesn't contains editors anymore.
     *
     * @param tab
     *         tab of the file to close
     */
    public static FileEvent createCloseFileEvent(EditorTab tab) {
        return new FileEvent(tab, CLOSE);
    }

    /**
     * Creates a event for {@code FileOperation.SAVE}.
     */
    public static FileEvent createSaveFileEvent(VirtualFile file) {
        return new FileEvent(file, SAVE);
    }

    /** {@inheritDoc} */
    @Override
    public Type<FileEventHandler> getAssociatedType() {
        return TYPE;
    }

    /** @return {@link VirtualFile} that represents an affected file */
    public VirtualFile getFile() {
        return file;
    }

    /** @return the type of operation performed with file */
    public FileOperation getOperationType() {
        return fileOperation;
    }

    @Nullable
    public EditorTab getEditorTab() {
        return tab;
    }

    @Override
    protected void dispatch(FileEventHandler handler) {
        handler.onFileOperation(this);
    }

    public enum FileOperation {
        OPEN, SAVE, CLOSE
    }
}
