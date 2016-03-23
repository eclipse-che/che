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
package org.eclipse.che.ide.ext.java.client.action;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.ProjectAction;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;
import org.eclipse.che.ide.api.selection.Selection;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.JavaResources;
import org.eclipse.che.ide.ext.java.client.newsourcefile.NewJavaSourceFilePresenter;
import org.eclipse.che.ide.ext.java.client.project.node.PackageNode;
import org.eclipse.che.ide.ext.java.client.project.node.SourceFolderNode;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;

/**
 * Action to create new Java source file.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class NewJavaSourceFileAction extends ProjectAction {

    private static final String MAVEN = "maven";

    private final AppContext                 appContext;
    private       ProjectExplorerPresenter   projectExplorer;
    private       NewJavaSourceFilePresenter newJavaSourceFilePresenter;

    @Inject
    public NewJavaSourceFileAction(ProjectExplorerPresenter projectExplorer,
                                   NewJavaSourceFilePresenter newJavaSourceFilePresenter,
                                   JavaLocalizationConstant constant,
                                   JavaResources resources,
                                   AppContext appContext) {
        super(constant.actionNewClassTitle(), constant.actionNewClassDescription(), resources.javaFile());
        this.newJavaSourceFilePresenter = newJavaSourceFilePresenter;
        this.projectExplorer = projectExplorer;
        this.appContext = appContext;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        newJavaSourceFilePresenter.showDialog();
    }

    @Override
    public void updateProjectAction(ActionEvent e) {
        CurrentProject project = appContext.getCurrentProject();
        if (project == null || !MAVEN.equals(project.getRootProject().getType())) {
            e.getPresentation().setEnabledAndVisible(false);
            return;
        }

        Selection<?> selection = projectExplorer.getSelection();
        if (selection == null) {
            e.getPresentation().setEnabledAndVisible(false);
            return;
        }

        e.getPresentation().setVisible(true);
        e.getPresentation().setEnabled(selection.isSingleSelection() &&
                (selection.getHeadElement() instanceof SourceFolderNode || selection.getHeadElement() instanceof PackageNode));
    }
}
