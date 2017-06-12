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

import com.google.inject.Inject;

import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.ext.java.client.util.JavaUtil;
import org.eclipse.che.plugin.testing.ide.TestServiceClient;
import org.eclipse.che.plugin.testing.ide.action.RunTestActionDelegate;
import org.eclipse.che.plugin.testing.ide.view.TestResultPresenter;
import org.eclipse.che.plugin.testing.junit.ide.JUnitTestLocalizationConstant;
import org.eclipse.che.plugin.testing.junit.ide.JUnitTestResources;

import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.eclipse.che.ide.part.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

/**
 * @author Mirage Abeysekara
 * @author David Festal
 */
public class RunClassTestAction extends AbstractPerspectiveAction
                                       implements RunTestActionDelegate.Source {

    private final NotificationManager   notificationManager;
    private final TestResultPresenter   presenter;
    private final TestServiceClient     service;
    private final AppContext            appContext;
    private final RunTestActionDelegate delegate;

    @Inject
    public RunClassTestAction(JUnitTestResources resources,
                                     NotificationManager notificationManager,
                                     AppContext appContext,
                                     TestResultPresenter presenter,
                                     TestServiceClient service,
                                     JUnitTestLocalizationConstant localization) {
        super(Arrays.asList(PROJECT_PERSPECTIVE_ID), localization.actionRunClassTitle(),
              localization.actionRunClassDescription(), null, resources.testIcon());
        this.notificationManager = notificationManager;
        this.presenter = presenter;
        this.service = service;
        this.appContext = appContext;
        this.delegate = new RunTestActionDelegate(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Resource resource = appContext.getResource();
        if (resource instanceof File) {
            File file = ((File)resource);
            String fqn = JavaUtil.resolveFQN((VirtualFile)file);
            Map<String, String> parameters = new HashMap<>();
            parameters.put("fqn", fqn);
            parameters.put("runClass", "true");
            delegate.doRunTests(e, parameters);
        }
    }

    @Override
    public void updateInPerspective(@NotNull ActionEvent e) {
        Resource resource = appContext.getResource();
        if (! (resource instanceof File)) {
            e.getPresentation().setEnabledAndVisible(false);
            return;
        }
        
        Project project = resource.getProject();
        if (project == null) {
            e.getPresentation().setEnabledAndVisible(false);
        }
        
        e.getPresentation().setVisible(true);

        String projectType = project.getType();
        if (! "maven".equals(projectType)) {
            e.getPresentation().setEnabled(false);
        }

        boolean enable = "java".equals(((File)resource).getExtension());
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
