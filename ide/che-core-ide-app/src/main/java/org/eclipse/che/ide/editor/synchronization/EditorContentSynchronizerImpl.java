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
package org.eclipse.che.ide.editor.synchronization;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.EditorWithAutoSave;
import org.eclipse.che.ide.api.event.ActivePartChangedEvent;
import org.eclipse.che.ide.api.event.ActivePartChangedHandler;
import org.eclipse.che.ide.api.parts.EditorPartStack;
import org.eclipse.che.ide.api.parts.PartPresenter;
import org.eclipse.che.ide.api.resources.ResourceChangedEvent;
import org.eclipse.che.ide.api.resources.ResourceChangedEvent.ResourceChangedHandler;
import org.eclipse.che.ide.api.resources.ResourceDelta;
import org.eclipse.che.ide.resource.Path;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.eclipse.che.ide.api.resources.ResourceDelta.ADDED;
import static org.eclipse.che.ide.api.resources.ResourceDelta.MOVED_FROM;
import static org.eclipse.che.ide.api.resources.ResourceDelta.MOVED_TO;


/**
 * The default implementation of {@link EditorContentSynchronizer}.
 * The synchronizer of content for opened files with the same {@link Path}.
 * Used to sync the content of opened files in different {@link EditorPartStack}s.
 * Note: this implementation disables autosave feature for implementations of {@link EditorWithAutoSave} with the same {@link Path} except
 * active editor.
 *
 * @author Roman Nikitenko
 */
@Singleton
public class EditorContentSynchronizerImpl implements EditorContentSynchronizer, ActivePartChangedHandler,
                                                      ResourceChangedHandler {
    final Map<Path, EditorGroupSynchronization> editorGroups;
    final Provider<EditorGroupSynchronization>  editorGroupSyncProvider;


    @Inject
    public EditorContentSynchronizerImpl(EventBus eventBus,
                                         Provider<EditorGroupSynchronization> editorGroupSyncProvider) {
        this.editorGroupSyncProvider = editorGroupSyncProvider;
        this.editorGroups = new HashMap<>();
        eventBus.addHandler(ActivePartChangedEvent.TYPE, this);
        eventBus.addHandler(ResourceChangedEvent.getType(), this);
    }

    /**
     * Begins to track given editor to sync its content with opened files with the same {@link Path}.
     *
     * @param editor
     *         editor to sync content
     */
    @Override
    public void trackEditor(EditorPartPresenter editor) {
        Path path = editor.getEditorInput().getFile().getLocation();
        if (editorGroups.containsKey(path)) {
            editorGroups.get(path).addEditor(editor);
        } else {
            EditorGroupSynchronization group = editorGroupSyncProvider.get();
            editorGroups.put(path, group);
            group.addEditor(editor);
        }
    }

    /**
     * Stops to track given editor.
     *
     * @param editor
     *         editor to stop tracking
     */
    @Override
    public void unTrackEditor(EditorPartPresenter editor) {
        Path path = editor.getEditorInput().getFile().getLocation();
        EditorGroupSynchronization group = editorGroups.get(path);
        if (group == null) {
            return;
        }
        group.removeEditor(editor);

        if (group.getSynchronizedEditors().isEmpty()) {
            group.unInstall();
            editorGroups.remove(path);
        }
    }

    @Override
    public void onActivePartChanged(ActivePartChangedEvent event) {
        PartPresenter activePart = event.getActivePart();
        if (!(activePart instanceof EditorPartPresenter)) {
            return;
        }

        EditorPartPresenter activeEditor = (EditorPartPresenter)activePart;
        Path path = activeEditor.getEditorInput().getFile().getLocation();
        if (editorGroups.containsKey(path)) {
            editorGroups.get(path).onActiveEditorChanged(activeEditor);
        }
    }

    @Override
    public void onResourceChanged(ResourceChangedEvent event) {
        final ResourceDelta delta = event.getDelta();
        if (delta.getKind() != ADDED || (delta.getFlags() & (MOVED_FROM | MOVED_TO)) == 0) {
            return;
        }

        final Path fromPath = delta.getFromPath();
        final Path toPath = delta.getToPath();

        if (delta.getResource().isFile()) {
            onFileChanged(fromPath, toPath);
        } else {
            onFolderChanged(fromPath, toPath);
        }
    }

    private void onFileChanged(Path fromPath, Path toPath) {
        final EditorGroupSynchronization group = editorGroups.remove(fromPath);
        if (group != null) {
            editorGroups.put(toPath, group);
        }
    }

    private void onFolderChanged(Path fromPath, Path toPath) {
        final Map<Path, EditorGroupSynchronization> newGroups = new HashMap<>(editorGroups.size());
        final Iterator<Map.Entry<Path, EditorGroupSynchronization>> iterator = editorGroups.entrySet().iterator();
        while (iterator.hasNext()) {
            final Map.Entry<Path, EditorGroupSynchronization> entry = iterator.next();
            final Path groupPath = entry.getKey();
            final EditorGroupSynchronization group = entry.getValue();

            if (fromPath.isPrefixOf(groupPath)) {
                final Path relPath = groupPath.removeFirstSegments(fromPath.segmentCount());
                final Path newPath = toPath.append(relPath);

                newGroups.put(newPath, group);
                iterator.remove();
            }
        }

        editorGroups.putAll(newGroups);
    }
}
