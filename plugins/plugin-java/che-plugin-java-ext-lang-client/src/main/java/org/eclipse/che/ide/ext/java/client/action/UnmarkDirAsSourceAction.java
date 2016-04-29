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

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.selection.Selection;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.JavaResources;
import org.eclipse.che.ide.ext.java.client.project.classpath.ClasspathResolver;
import org.eclipse.che.ide.ext.java.client.project.classpath.service.ClasspathServiceClient;
import org.eclipse.che.ide.ext.java.client.project.node.SourceFolderNode;
import org.eclipse.che.ide.ext.java.shared.dto.classpath.ClasspathEntryDTO;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.project.node.FolderReferenceNode;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

/**
 * The action which unmarks a folder into the project as source folder.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class UnmarkDirAsSourceAction extends AbstractPerspectiveAction {
    private final AppContext               appContext;
    private final ClasspathServiceClient   classpathService;
    private final ClasspathResolver        classpathResolver;
    private final NotificationManager      notificationManager;
    private final ProjectExplorerPresenter projectExplorer;

    @Inject
    public UnmarkDirAsSourceAction(JavaResources javaResources,
                                   AppContext appContext,
                                   ClasspathServiceClient classpathService,
                                   ClasspathResolver classpathResolver,
                                   NotificationManager notificationManager,
                                   ProjectExplorerPresenter projectExplorerPresenter,
                                   JavaLocalizationConstant locale) {
        super(singletonList(PROJECT_PERSPECTIVE_ID),
              locale.unmarkDirectoryAsSourceAction(),
              locale.unmarkDirectoryAsSourceDescription(),
              null,
              javaResources.sourceFolder());

        this.appContext = appContext;
        this.classpathService = classpathService;
        this.classpathResolver = classpathResolver;
        this.notificationManager = notificationManager;
        this.projectExplorer = projectExplorerPresenter;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        CurrentProject currentProject = appContext.getCurrentProject();
        if (currentProject == null) {
            return;
        }

        FolderReferenceNode folder = (FolderReferenceNode)(projectExplorer.getSelection().getHeadElement());

        updateClasspath(currentProject, folder);
    }

    @Override
    public void updateInPerspective(ActionEvent e) {
        Selection<?> selection = projectExplorer.getSelection();
        if (selection == null) {
            e.getPresentation().setEnabledAndVisible(false);
            return;
        }

        e.getPresentation().setVisible(selection.getHeadElement() instanceof SourceFolderNode);
        e.getPresentation().setEnabled(selection.isSingleSelection());
    }

    private void updateClasspath(final CurrentProject currentProject, final FolderReferenceNode folder) {
        classpathService.getClasspath(currentProject.getProjectConfig().getPath()).then(new Operation<List<ClasspathEntryDTO>>() {
            @Override
            public void apply(List<ClasspathEntryDTO> arg) throws OperationException {
                classpathResolver.resolveClasspathEntries(arg);
                classpathResolver.getSources().remove(folder.getStorablePath());
                classpathResolver.updateClasspath();
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError arg) throws OperationException {
                notificationManager.notify("Can't get classpath", arg.getMessage(), FAIL, EMERGE_MODE);
            }
        });
    }
}
