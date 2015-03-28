/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.ide.tutorial.editor;

import static org.eclipse.che.ide.api.action.IdeActions.GROUP_FILE_NEW;
import static org.eclipse.che.ide.api.parts.PartStackType.EDITING;

import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.editor.EditorRegistry;
import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.ide.api.filetypes.FileType;
import org.eclipse.che.ide.api.filetypes.FileTypeRegistry;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import com.codenvy.ide.tutorial.editor.editor.GroovyEditorProvider;
import com.codenvy.ide.tutorial.editor.part.TutorialHowToPresenter;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/** Extension used to demonstrate the Editor API. */
@Singleton
@Extension(title = "Editor API tutorial", version = "1.0.0")
public class EditorTutorialExtension {
    public static final String GROOVY_MIME_TYPE = "text/x-groovy";

    @Inject
    public EditorTutorialExtension(WorkspaceAgent workspaceAgent,
                                   TutorialHowToPresenter howToPresenter,
                                   EditorRegistry editorRegistry,
                                   FileTypeRegistry fileTypeRegistry,
                                   GroovyEditorProvider groovyEditorProvider,
                                   EditorTutorialResource editorTutorialResource,
                                   ActionManager actionManager,
                                   NewGroovyFileAction newGroovyFileAction,
                                   EditorTutorialResource resource) {

        workspaceAgent.openPart(howToPresenter, EDITING);

        FileType groovyFile = new FileType("Groovy", resource.groovyFile(), GROOVY_MIME_TYPE, "groovy");
        fileTypeRegistry.registerFileType(groovyFile);

        editorRegistry.registerDefaultEditor(groovyFile, groovyEditorProvider);

        actionManager.registerAction("newGroovyFileActionId", newGroovyFileAction);
        DefaultActionGroup newGroup = (DefaultActionGroup)actionManager.getAction(GROUP_FILE_NEW);
        newGroup.addSeparator();
        newGroup.add(newGroovyFileAction);
    }
}
