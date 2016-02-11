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
package org.eclipse.che.ide.ext.java.client;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.event.FileEvent;
import org.eclipse.che.ide.api.event.FileEventHandler;
import org.eclipse.che.ide.api.event.SelectionChangedEvent;
import org.eclipse.che.ide.api.event.SelectionChangedHandler;
import org.eclipse.che.ide.api.project.tree.VirtualFile;
import org.eclipse.che.ide.api.selection.Selection;
import org.eclipse.che.ide.ext.java.client.projecttree.JavaSourceFolderUtil;
import org.eclipse.che.ide.extension.machine.client.command.valueproviders.CommandPropertyValueProvider;

import static org.eclipse.che.ide.api.event.FileEvent.FileOperation.CLOSE;

/**
 * Provides FQN of the Java-class which is opened in active editor.
 *
 * @author Artem Zatsarynnyy
 */
@Singleton
public class CurrentClassFQNProvider implements CommandPropertyValueProvider, FileEventHandler, SelectionChangedHandler {

    private static final String KEY = "${current.class.fqn}";
    private final EditorAgent editorAgent;

    private String value;

    @Inject
    public CurrentClassFQNProvider(EventBus eventBus, EditorAgent editorAgent) {
        this.editorAgent = editorAgent;
        this.value = "";

        eventBus.addHandler(SelectionChangedEvent.TYPE, this);
        eventBus.addHandler(FileEvent.TYPE, this);
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void onFileOperation(FileEvent event) {
        // the last file was closed
        if (event.getOperationType() == CLOSE && editorAgent.getOpenedEditors().isEmpty()) {
            value = "";
        }
    }

    @Override
    public void onSelectionChanged(SelectionChangedEvent event) {
        final Selection<?> selection = event.getSelection();
        if (selection == null || selection.isEmpty() || selection.isMultiSelection()) {
            value = "";
            return;
        }

        final Object selObject = selection.getHeadElement();

        if (selObject instanceof VirtualFile) {
            value = JavaSourceFolderUtil.getFQNForFile((VirtualFile)selObject);
        } else {
            value = "";
        }
    }
}
