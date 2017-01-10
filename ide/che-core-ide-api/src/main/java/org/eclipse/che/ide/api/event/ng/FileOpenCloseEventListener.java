/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.api.event.ng;

import com.google.inject.Provider;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.event.FileEvent;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.util.loging.Log;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Objects;

import static org.eclipse.che.ide.api.event.ng.FileTrackingEvent.newFileTrackingStartEvent;
import static org.eclipse.che.ide.api.event.ng.FileTrackingEvent.newFileTrackingStopEvent;

/**
 * File open/close event listener aimed to wrap {@link FileEvent} into {@link FileTrackingEvent}
 * which is consumed by {@link ClientServerEventService} and sent to server side for further
 * processing.
 *
 * @author Dmitry Kuleshov
 */
@Singleton
public class FileOpenCloseEventListener {

    @Inject
    public FileOpenCloseEventListener(final Provider<EditorAgent> editorAgentProvider,
                                      final DeletedFilesController deletedFilesController,
                                      final EventBus eventBus) {

        Log.debug(getClass(), "Adding file event listener");
        eventBus.addHandler(FileEvent.TYPE, new FileEvent.FileEventHandler() {
            @Override
            public void onFileOperation(FileEvent event) {
                final Path path = event.getFile().getLocation();

                switch (event.getOperationType()) {
                    case OPEN: {
                        processFileOpen(path);

                        break;
                    }
                    case CLOSE: {
                        final EditorPartPresenter closingEditor = event.getEditorTab().getRelativeEditorPart();
                        final List<EditorPartPresenter> openedEditors = editorAgentProvider.get().getOpenedEditors();

                        processFileClose(closingEditor, openedEditors, path);

                        break;
                    }
                }
            }

            private void processFileOpen(Path path) {
                eventBus.fireEvent(newFileTrackingStartEvent(path.toString()));
            }

            private void processFileClose(EditorPartPresenter closingEditor, List<EditorPartPresenter> openedEditors, Path path) {
                for (final EditorPartPresenter editor : openedEditors) {
                    final Path editorFilePath = editor.getEditorInput().getFile().getLocation();
                    if (Objects.equals(path, editorFilePath) && closingEditor != editor) {
                        return;
                    }
                }

                eventBus.fireEvent(newFileTrackingStopEvent(path.toString()));

            }
        });
    }
}
