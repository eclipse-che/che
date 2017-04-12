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

import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.api.selection.Selection;
import org.eclipse.che.ide.api.selection.SelectionAgent;
import org.eclipse.che.ide.resources.tree.ContainerNode;
import org.eclipse.che.ide.resources.tree.FileNode;
import org.eclipse.che.plugin.maven.shared.MavenAttributes;
import org.eclipse.che.plugin.testing.ide.TestServiceClient;
import org.eclipse.che.plugin.testing.ide.action.RunTestActionDelegate;
import org.eclipse.che.plugin.testing.ide.view.TestResultPresenter;
import org.eclipse.che.plugin.testing.testng.ide.TestNGLocalizationConstant;
import org.eclipse.che.plugin.testing.testng.ide.TestNGResources;

import com.google.inject.Inject;

/**
 * @author Mirage Abeysekara
 * @author David Festal
 */
public class RunTestXMLContextAction extends AbstractPerspectiveAction
                              implements RunTestActionDelegate.Source {

    private final NotificationManager   notificationManager;
    private final TestResultPresenter   presenter;
    private final TestServiceClient     service;
    private final AppContext            appContext;
    private final SelectionAgent        selectionAgent;
    private final RunTestActionDelegate delegate;

    @Inject
    public RunTestXMLContextAction(TestNGResources resources,
                                   NotificationManager notificationManager,
                                   AppContext appContext,
                                   TestResultPresenter presenter,
                                   TestServiceClient service,
                                   SelectionAgent selectionAgent,
                                   TestNGLocalizationConstant localization) {
        super(Arrays.asList(PROJECT_PERSPECTIVE_ID), localization.actionRunXMLTitle(),
              localization.actionRunXMLDescription(), null, resources.testAllIcon());
        this.notificationManager = notificationManager;
        this.presenter = presenter;
        this.service = service;
        this.appContext = appContext;
        this.selectionAgent = selectionAgent;
        this.delegate = new RunTestActionDelegate(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final Selection< ? > selection = selectionAgent.getSelection();
        final Object possibleNode = selection.getHeadElement();
        if (possibleNode instanceof ContainerNode) {
            Container container = ((ContainerNode)possibleNode).getData();
            Project project = container.getProject();
            if (project != null) {
                Map<String, String> parameters = new HashMap<>();
                parameters.put("testngXML",
                               project.getPath() + "/" + MavenAttributes.DEFAULT_TEST_RESOURCES_FOLDER + "/testng.xml");
                delegate.doRunTests(e, parameters);
            }
        }
    }

    @Override
    public void updateInPerspective(@NotNull ActionEvent e) {
        e.getPresentation().setEnabledAndVisible(false);
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
        String expectedXmlFile = MavenAttributes.DEFAULT_TEST_RESOURCES_FOLDER + "/testng.xml";
        final Object possibleNode = selection.getHeadElement();
        if (possibleNode instanceof FileNode) {
            VirtualFile file = ((FileNode)possibleNode).getData();
            if (file instanceof File) {
                e.getPresentation().setEnabledAndVisible(file.getLocation().toString().endsWith(expectedXmlFile));
            } else {
                e.getPresentation().setEnabledAndVisible(false);
            }
        }
        if (possibleNode instanceof ContainerNode) {
            Container container = ((ContainerNode)possibleNode).getData();
            Project project = container.getProject();
            if (project != null) {
                String projectType = project.getType();
                boolean enable = "maven".equals(projectType);
                e.getPresentation().setEnabledAndVisible(enable);
                return;
            } else {
                e.getPresentation().setEnabledAndVisible(false);
            }
        }
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
