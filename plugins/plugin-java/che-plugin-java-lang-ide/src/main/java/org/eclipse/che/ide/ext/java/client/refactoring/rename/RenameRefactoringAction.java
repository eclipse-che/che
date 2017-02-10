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
package org.eclipse.che.ide.ext.java.client.refactoring.rename;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.event.ActivePartChangedEvent;
import org.eclipse.che.ide.api.event.ActivePartChangedHandler;
import org.eclipse.che.ide.api.filetypes.FileTypeRegistry;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.refactoring.RefactorInfo;
import org.eclipse.che.ide.ext.java.client.refactoring.move.RefactoredItemType;
import org.eclipse.che.ide.ext.java.client.refactoring.rename.wizard.RenamePresenter;
import org.eclipse.che.ide.ext.java.client.resource.SourceFolderMarker;
import org.eclipse.che.ide.ext.java.client.util.JavaUtil;

import static org.eclipse.che.ide.api.resources.Resource.FILE;
import static org.eclipse.che.ide.ext.java.client.refactoring.move.RefactoredItemType.COMPILATION_UNIT;
import static org.eclipse.che.ide.ext.java.client.refactoring.move.RefactoredItemType.PACKAGE;

/**
 * Action for launch rename refactoring of java files
 *
 * @author Alexander Andrienko
 * @author Valeriy Svydenko
 * @author Vlad Zhukovskyi
 */
@Singleton
public class RenameRefactoringAction extends AbstractPerspectiveAction implements ActivePartChangedHandler {

    private final EditorAgent           editorAgent;
    private final RenamePresenter       renamePresenter;
    private final JavaRefactoringRename javaRefactoringRename;
    private final AppContext            appContext;
    private final FileTypeRegistry      fileTypeRegistry;

    private boolean editorInFocus;

    @Inject
    public RenameRefactoringAction(EditorAgent editorAgent,
                                   RenamePresenter renamePresenter,
                                   EventBus eventBus,
                                   JavaLocalizationConstant locale,
                                   JavaRefactoringRename javaRefactoringRename,
                                   AppContext appContext,
                                   FileTypeRegistry fileTypeRegistry) {
        super(null, locale.renameRefactoringActionName(), locale.renameRefactoringActionDescription());
        this.editorAgent = editorAgent;
        this.renamePresenter = renamePresenter;
        this.javaRefactoringRename = javaRefactoringRename;
        this.appContext = appContext;
        this.fileTypeRegistry = fileTypeRegistry;
        this.editorInFocus = false;

        eventBus.addHandler(ActivePartChangedEvent.TYPE, this);
    }

    @Override
    public void actionPerformed(ActionEvent event) {

        if (editorInFocus) {
            final EditorPartPresenter editorPart = editorAgent.getActiveEditor();
            if (editorPart == null || !(editorPart instanceof TextEditor)) {
                return;
            }

            javaRefactoringRename.refactor((TextEditor)editorPart);
        } else {
            final Resource[] resources = appContext.getResources();

            if (resources == null || resources.length > 1) {
                return;
            }

            final Resource resource = resources[0];

            final Optional<Project> project = resource.getRelatedProject();

            if (!JavaUtil.isJavaProject(project.get())) {
                return;
            }

            final Optional<Resource> srcFolder = resource.getParentWithMarker(SourceFolderMarker.ID);

            if (!srcFolder.isPresent() || resource.getLocation().equals(srcFolder.get().getLocation())) {
                return;
            }

            RefactoredItemType renamedItemType = null;

            if (resource.getResourceType() == FILE && isJavaFile((File)resource)) {
                renamedItemType = COMPILATION_UNIT;
            } else if (resource instanceof Container) {
                renamedItemType = PACKAGE;
            }

            if (renamedItemType == null) {
                return;
            }

            renamePresenter.show(RefactorInfo.of(renamedItemType, resources));
        }
    }

    @Override
    public void updateInPerspective(ActionEvent event) {
        event.getPresentation().setVisible(true);

        if (editorInFocus) {
            final EditorPartPresenter editorPart = editorAgent.getActiveEditor();
            if (editorPart == null || !(editorPart instanceof TextEditor)) {
                event.getPresentation().setEnabled(false);
                return;
            }

            final VirtualFile file = editorPart.getEditorInput().getFile();

            if (file instanceof File) {
                final Optional<Project> project = ((File)file).getRelatedProject();

                if (!project.isPresent()) {
                    event.getPresentation().setEnabled(false);
                    return;
                }

                event.getPresentation().setEnabled(JavaUtil.isJavaProject(project.get()) && isJavaFile(file));
            } else {
                event.getPresentation().setEnabled(isJavaFile(file));
            }

        } else {
            final Resource[] resources = appContext.getResources();

            if (resources == null || resources.length > 1) {
                event.getPresentation().setEnabled(false);
                return;
            }

            final Resource resource = resources[0];

            final Optional<Project> project = resource.getRelatedProject();

            if (!project.isPresent()) {
                event.getPresentation().setEnabled(false);
                return;
            }

            final Optional<Resource> srcFolder = resource.getParentWithMarker(SourceFolderMarker.ID);

            if (resource.getResourceType() == FILE) {
                event.getPresentation()
                     .setEnabled(JavaUtil.isJavaProject(project.get()) && srcFolder.isPresent() && isJavaFile((File)resource));
            } else if (resource instanceof Container) {
                event.getPresentation().setEnabled(JavaUtil.isJavaProject(project.get()) && srcFolder.isPresent());
            }
        }
    }

    protected boolean isJavaFile(VirtualFile file) {
        String fileExtension = fileTypeRegistry.getFileTypeByFile(file).getExtension();

        return fileExtension.equals("java") || fileExtension.equals("class");
    }

    @Override
    public void onActivePartChanged(ActivePartChangedEvent event) {
        editorInFocus = event.getActivePart() instanceof EditorPartPresenter;
    }
}
