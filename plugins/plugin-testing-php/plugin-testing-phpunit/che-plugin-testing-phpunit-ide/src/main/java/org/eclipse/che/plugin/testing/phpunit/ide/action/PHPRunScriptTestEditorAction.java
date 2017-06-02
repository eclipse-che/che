/*******************************************************************************
 * Copyright (c) 2016 Rogue Wave Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Rogue Wave Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.testing.phpunit.ide.action;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.ProjectAction;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorInput;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.filetypes.FileTypeRegistry;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.api.selection.SelectionAgent;
import org.eclipse.che.plugin.testing.ide.TestActionRunner;
import org.eclipse.che.plugin.testing.phpunit.ide.PHPUnitTestLocalizationConstant;
import org.eclipse.che.plugin.testing.phpunit.ide.PHPUnitTestResources;

import com.google.inject.Inject;

/**
 * "Run Script" PHPUnit test editor action.
 *
 * @author Bartlomiej Laczkowski
 */
public class PHPRunScriptTestEditorAction extends ProjectAction {

    private final TestActionRunner runner;
    private final AppContext       appContext;
    private final EditorAgent      editorAgent;
    private final FileTypeRegistry fileTypeRegistry;

    @Inject
    public PHPRunScriptTestEditorAction(TestActionRunner runner,
                                        EditorAgent editorAgent,
                                        FileTypeRegistry fileTypeRegistry,
                                        PHPUnitTestResources resources,
                                        AppContext appContext,
                                        SelectionAgent selectionAgent,
                                        PHPUnitTestLocalizationConstant localization) {
        super(localization.actionRunScriptTitle(), localization.actionRunScriptDescription(), resources.testIcon());
        this.runner = runner;
        this.appContext = appContext;
        this.editorAgent = editorAgent;
        this.fileTypeRegistry = fileTypeRegistry;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final Project project = appContext.getRootProject();
        EditorPartPresenter editorPart = editorAgent.getActiveEditor();
        final VirtualFile file = editorPart.getEditorInput().getFile();
        Map<String, String> parameters = new HashMap<>();
        parameters.put("testTarget", file.getLocation().toString());
        runner.run("PHPUnit", project.getPath(), parameters);
    }

    @Override
    protected void updateProjectAction(ActionEvent e) {
        if (editorAgent.getActiveEditor() != null) {
            EditorInput input = editorAgent.getActiveEditor().getEditorInput();
            VirtualFile file = input.getFile();
            final String fileExtension = fileTypeRegistry.getFileTypeByFile(file).getExtension();
            if ("php".equals(fileExtension) || "phtml".equals(fileExtension)) {
                e.getPresentation().setEnabledAndVisible(true);
                return;
            }
        }
        e.getPresentation().setEnabledAndVisible(false);
    }
}
