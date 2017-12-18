/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.core;

import com.google.inject.Provider;
import com.google.web.bindery.event.shared.EventBus;
import java.util.List;
import java.util.Objects;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.events.FileEvent;
import org.eclipse.che.ide.api.filewatcher.ClientServerEventService;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.util.loging.Log;

/**
 * File open/close event listener aimed to wrap {@link FileEvent} into {@code FileTrackingEvent}
 * which is consumed by {@link ClientServerEventService} and sent to server side for further
 * processing.
 *
 * @author Dmitry Kuleshov
 */
@Singleton
public class FileOpenCloseEventListener {

  @Inject
  public FileOpenCloseEventListener(
      final Provider<EditorAgent> editorAgentProvider,
      final EventBus eventBus,
      final ClientServerEventService clientServerEventService) {

    Log.debug(getClass(), "Adding file event listener");
    eventBus.addHandler(
        FileEvent.TYPE,
        new FileEvent.FileEventHandler() {
          @Override
          public void onFileOperation(FileEvent event) {
            final Path path = event.getFile().getLocation();
            final EditorAgent editorAgent = editorAgentProvider.get();

            switch (event.getOperationType()) {
              case OPEN:
                {
                  processFileOpen(path);
                  break;
                }
              case CLOSE:
                {
                  final EditorPartPresenter closingEditor =
                      event.getEditorTab().getRelativeEditorPart();
                  final List<EditorPartPresenter> openedEditors = editorAgent.getOpenedEditors();

                  processFileClose(closingEditor, openedEditors, path);

                  break;
                }
              default:
            }
          }

          private void processFileOpen(Path path) {
            clientServerEventService.sendFileTrackingStartEvent(path.toString());
          }

          private void processFileClose(
              EditorPartPresenter closingEditor,
              List<EditorPartPresenter> openedEditors,
              Path path) {
            for (final EditorPartPresenter editor : openedEditors) {
              final Path editorFilePath = editor.getEditorInput().getFile().getLocation();
              if (Objects.equals(path, editorFilePath) && closingEditor != editor) {
                return;
              }
            }

            clientServerEventService.sendFileTrackingStopEvent(path.toString());
          }
        });
  }
}
