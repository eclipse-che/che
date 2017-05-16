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
package org.eclipse.che.plugin.testing.testng.ide.action;

import com.google.inject.Inject;
import org.eclipse.che.api.testing.shared.TestExecutionContext;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.api.selection.Selection;
import org.eclipse.che.ide.api.selection.SelectionAgent;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.resources.tree.FileNode;
import org.eclipse.che.plugin.testing.ide.TestServiceClient;
import org.eclipse.che.plugin.testing.ide.action.RunTestActionDelegate;
import org.eclipse.che.plugin.testing.ide.view.TestResultPresenter;
import org.eclipse.che.plugin.testing.testng.ide.TestNGLocalizationConstant;
import org.eclipse.che.plugin.testing.testng.ide.TestNGResources;

import javax.validation.constraints.NotNull;
import java.util.Arrays;

import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

/**
 * @author Mirage Abeysekara
 */
public class RunClassContextTestAction extends AbstractPerspectiveAction
                                       implements RunTestActionDelegate.Source {

    private final NotificationManager   notificationManager;
    private final TestResultPresenter   presenter;
    private final TestServiceClient     service;
    private final AppContext            appContext;
    private final SelectionAgent        selectionAgent;
    private final DtoFactory dtoFactory;
    private final RunTestActionDelegate delegate;

    @Inject
    public RunClassContextTestAction(TestNGResources resources,
                                     NotificationManager notificationManager,
                                     AppContext appContext,
                                     TestResultPresenter presenter,
                                     TestServiceClient service,
                                     SelectionAgent selectionAgent,
                                     TestNGLocalizationConstant localization,
                                     DtoFactory dtoFactory) {
        super(Arrays.asList(PROJECT_PERSPECTIVE_ID), localization.actionRunClassContextTitle(),
              localization.actionRunClassContextDescription(), null, resources.testIcon());
        this.notificationManager = notificationManager;
        this.presenter = presenter;
        this.service = service;
        this.appContext = appContext;
        this.selectionAgent = selectionAgent;
        this.dtoFactory = dtoFactory;
        this.delegate = new RunTestActionDelegate(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final Selection< ? > selection = selectionAgent.getSelection();
        final Object possibleNode = selection.getHeadElement();
        TestExecutionContext context = dtoFactory.createDto(TestExecutionContext.class);
        if (possibleNode instanceof FileNode) {
            VirtualFile file = ((FileNode)possibleNode).getData();
            context.setTestType(TestExecutionContext.TestType.FILE);
            context.setFilePath(file.getLocation().toString());
            delegate.doRunTests(e, context);
        }
    }

    @Override
    public void updateInPerspective(@NotNull ActionEvent e) {
        if ((appContext.getRootProject() == null)) {
            e.getPresentation().setEnabledAndVisible(false);
            return;
        }
        final Selection< ? > selection = selectionAgent.getSelection();
        if (selection == null || selection.isEmpty()) {
            e.getPresentation().setEnabledAndVisible(false);
            return;
        }
        if (selection.isMultiSelection()) {
            e.getPresentation().setEnabledAndVisible(false);
            return;
        }
        final Object possibleNode = selection.getHeadElement();
        if (!(possibleNode instanceof FileNode)) {
            e.getPresentation().setEnabledAndVisible(false);
            return;
        }

        e.getPresentation().setVisible(true);
        boolean enable = "java".equals(((FileNode)possibleNode).getData().getExtension());
        e.getPresentation().setEnabled(enable);
    }

    @Override
    public NotificationManager getNotificationManager() {
        return notificationManager;
    }

    @Override
    public AppContext getAppContext() {
        return appContext;
    }

    @Override
    public TestServiceClient getService() {
        return service;
    }

    @Override
    public TestResultPresenter getPresenter() {
        return presenter;
    }

    @Override
    public String getTestingFramework() {
        return "testng";
    }
}
