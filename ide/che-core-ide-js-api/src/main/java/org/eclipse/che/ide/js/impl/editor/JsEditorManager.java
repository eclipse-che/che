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

package org.eclipse.che.ide.js.impl.editor;

import com.google.web.bindery.event.shared.EventBus;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.ide.api.editor.events.FileEvent;
import org.eclipse.che.ide.js.api.Disposable;
import org.eclipse.che.ide.js.api.editor.EditorManager;
import org.eclipse.che.ide.js.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.js.api.editor.event.EditorOpenedEvent;
import org.eclipse.che.ide.js.api.editor.event.FileOperationEvent;
import org.eclipse.che.ide.js.api.event.Listener;
import org.eclipse.che.ide.util.ListenerManager;
import org.eclipse.che.ide.util.ListenerRegistrar.Remover;

/** @author Yevhen Vydolob */
@Singleton
public class JsEditorManager implements EditorManager {

  private final ListenerManager<Listener<FileOperationEvent>> fileOperationListeners =
      ListenerManager.create();

  private final ListenerManager<Listener<EditorOpenedEvent>> editorOpenedListeners =
      ListenerManager.create();

  @Inject
  public JsEditorManager(EventBus eventBus) {
    eventBus.addHandler(
        FileEvent.TYPE,
        event -> {
          FileOperationEvent operationEvent =
              new FileOperationEvent(event.getFile(), event.getOperationType());
          fileOperationListeners.dispatch(listener -> listener.on(operationEvent));
        });

    eventBus.addHandler(
        org.eclipse.che.ide.api.editor.EditorOpenedEvent.TYPE,
        event -> {
          EditorOpenedEvent openedEvent =
              new EditorOpenedEvent(event.getFile(), new EditorPartPresenter() {});

          editorOpenedListeners.dispatch(listener -> listener.on(openedEvent));
        });
  }

  @Override
  public Disposable addFileOperationListener(Listener<FileOperationEvent> listener) {
    return addHandler(fileOperationListeners, listener);
  }

  @Override
  public Disposable addEditorOpenedListener(Listener<EditorOpenedEvent> listener) {
    return addHandler(editorOpenedListeners, listener);
  }

  private <T> Disposable addHandler(ListenerManager<Listener<T>> manager, Listener<T> listener) {
    Remover remover = manager.add(listener);
    return remover::remove;
  }
}
