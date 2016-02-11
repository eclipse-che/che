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
package org.eclipse.che.ide.ext.java.client.editor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.event.ActivePartChangedEvent;
import org.eclipse.che.ide.api.event.ActivePartChangedHandler;
import org.eclipse.che.ide.api.parts.PartPresenter;
import org.eclipse.che.ide.api.parts.PropertyListener;
import org.eclipse.che.ide.ext.java.client.project.node.JavaFileNode;
import org.eclipse.che.ide.ext.java.client.project.node.PackageNode;
import org.eclipse.che.ide.jseditor.client.texteditor.EmbeddedTextEditorPresenter;
import org.eclipse.che.ide.project.event.ResourceNodeDeletedEvent;
import org.eclipse.che.ide.project.node.ResourceBasedNode;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Evgen Vidolob
 */
@Singleton
public class FileWatcher {

    @Inject
    private EditorAgent editorAgent;

    private Set<EmbeddedTextEditorPresenter> editor2reconcile = new HashSet<>();

    @Inject
    private void handleFileOperations(EventBus eventBus) {

        eventBus.addHandler(ResourceNodeDeletedEvent.getType(), new ResourceNodeDeletedEvent.ResourceNodeDeletedHandler() {
            @Override
            public void onResourceEvent(ResourceNodeDeletedEvent event) {
                ResourceBasedNode node = event.getNode();
                if (node instanceof PackageNode || node instanceof JavaFileNode) {
                    reparseAllOpenedFiles();
                }
            }
        });

        eventBus.addHandler(ActivePartChangedEvent.TYPE, new ActivePartChangedHandler() {
            @Override
            public void onActivePartChanged(ActivePartChangedEvent event) {
                if (event.getActivePart() instanceof EmbeddedTextEditorPresenter) {
                    if (editor2reconcile.contains(event.getActivePart())) {
                        reParseEditor((EmbeddedTextEditorPresenter<?>)event.getActivePart());
                    }
                }
            }
        });
    }

    private void reParseEditor(EmbeddedTextEditorPresenter<?> editor) {
        editor.refreshEditor();
        editor2reconcile.remove(editor);
    }

    public void editorOpened(final EditorPartPresenter editor) {
        final PropertyListener propertyListener = new PropertyListener() {
            @Override
            public void propertyChanged(PartPresenter source, int propId) {
                if (propId == EditorPartPresenter.PROP_DIRTY) {
                    if (!editor.isDirty()) {
                        reparseAllOpenedFiles();
                        //remove just saved editor
                        editor2reconcile.remove((EmbeddedTextEditorPresenter)editor);
                    }
                }
            }
        };
        editor.addPropertyListener(propertyListener);
    }

    private void reparseAllOpenedFiles() {
        Map<String, EditorPartPresenter> openedEditors = editorAgent.getOpenedEditors();
        for (EditorPartPresenter editorPartPresenter: openedEditors.values()) {
            if (editorPartPresenter instanceof EmbeddedTextEditorPresenter) {
                final EmbeddedTextEditorPresenter< ? > editor = (EmbeddedTextEditorPresenter< ? >)editorPartPresenter;
                editor.refreshEditor();
            }
        }
    }
}
