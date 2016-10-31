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
package org.eclipse.che.ide.ext.java.client.refactoring.move;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.filetypes.FileTypeRegistry;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.refactoring.RefactorInfo;
import org.eclipse.che.ide.ext.java.client.refactoring.move.wizard.MovePresenter;
import org.eclipse.che.ide.ext.java.client.resource.SourceFolderMarker;
import org.eclipse.che.ide.ext.java.client.util.JavaUtil;

import static org.eclipse.che.ide.api.resources.Resource.FILE;
import static org.eclipse.che.ide.ext.java.client.refactoring.move.MoveType.REFACTOR_MENU;
import static org.eclipse.che.ide.ext.java.client.refactoring.move.RefactoredItemType.COMPILATION_UNIT;
import static org.eclipse.che.ide.ext.java.client.refactoring.move.RefactoredItemType.PACKAGE;

/**
 * @author Dmitry Shnurenko
 * @author Valeriy Svydenko
 */
@Singleton
public class MoveAction extends AbstractPerspectiveAction {

    private final MovePresenter    movePresenter;
    private final AppContext       appContext;
    private final FileTypeRegistry fileTypeRegistry;

    @Inject
    public MoveAction(JavaLocalizationConstant locale,
                      MovePresenter movePresenter,
                      AppContext appContext,
                      FileTypeRegistry fileTypeRegistry) {
        super(null, locale.moveActionName(), locale.moveActionDescription());

        this.movePresenter = movePresenter;
        this.appContext = appContext;
        this.fileTypeRegistry = fileTypeRegistry;
    }

    /** {@inheritDoc} */
    @Override
    public void updateInPerspective(ActionEvent event) {
        event.getPresentation().setVisible(true);

        final Resource[] resources = appContext.getResources();

        if (resources == null || resources.length != 1) {
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
            event.getPresentation().setEnabled(JavaUtil.isJavaProject(project.get()) && srcFolder.isPresent() && isJavaFile((File)resource));
        } else if (resource instanceof Container) {
            event.getPresentation().setEnabled(JavaUtil.isJavaProject(project.get()) && srcFolder.isPresent());
        }
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent actionEvent) {
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

        movePresenter.show(RefactorInfo.of(REFACTOR_MENU, renamedItemType, resources));
    }

    protected boolean isJavaFile(VirtualFile file) {
        final String ext = fileTypeRegistry.getFileTypeByFile(file).getExtension();

        return "java".equals(ext) || "class".equals(ext);
    }
}
