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
package org.eclipse.che.plugin.testing.junit.ide.action;

import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.plugin.testing.ide.TestServiceClient;
import org.eclipse.che.plugin.testing.ide.action.RunTestActionDelegate;
import org.eclipse.che.plugin.testing.ide.view.TestResultPresenter;
import org.eclipse.che.plugin.testing.junit.ide.JUnitTestLocalizationConstant;
import org.eclipse.che.plugin.testing.junit.ide.JUnitTestResources;

import com.google.inject.Inject;

/**
 * @author Mirage Abeysekara
 * @author David Festal
 */
public class RunAllTestAction extends AbstractPerspectiveAction
                                     implements RunTestActionDelegate.Source {

    private final NotificationManager   notificationManager;
    private final TestResultPresenter   presenter;
    private final TestServiceClient     service;
    private final AppContext            appContext;
    private final RunTestActionDelegate delegate;

    @Inject
    public RunAllTestAction(JUnitTestResources resources,
                            NotificationManager notificationManager,
                            AppContext appContext,
                            TestResultPresenter presenter,
                            TestServiceClient service,
                            JUnitTestLocalizationConstant localization) {
        super(singletonList(PROJECT_PERSPECTIVE_ID), localization.actionRunAllTitle(),
              localization.actionRunAllDescription(), null, resources.testAllIcon());
        this.notificationManager = notificationManager;
        this.presenter = presenter;
        this.service = service;
        this.appContext = appContext;
        this.delegate = new RunTestActionDelegate(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Resource resource = appContext.getResource();
        if (resource != null) {
            Project project = resource.getProject();
            if (project != null) {
                Map<String, String> parameters = new HashMap<>();
                delegate.doRunTests(e, parameters);
            }
        }
    }

    @Override
    public void updateInPerspective(@NotNull ActionEvent e) {
        Resource resource = appContext.getResource();
        if (resource == null || resource.getProject() == null) {
            e.getPresentation().setEnabledAndVisible(false);
            return;
        }

        e.getPresentation().setVisible(true);

        String projectType = resource.getProject().getType();
        boolean enable = "maven".equals(projectType);
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
        return "junit";
    }
}
