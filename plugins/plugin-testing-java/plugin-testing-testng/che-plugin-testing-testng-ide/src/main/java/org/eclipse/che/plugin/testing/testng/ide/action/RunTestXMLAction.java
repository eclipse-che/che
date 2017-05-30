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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.filetypes.FileTypeRegistry;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.ext.java.client.action.JavaEditorAction;
import org.eclipse.che.plugin.maven.shared.MavenAttributes;
import org.eclipse.che.plugin.testing.ide.TestServiceClient;
import org.eclipse.che.plugin.testing.ide.action.RunTestActionDelegate;
import org.eclipse.che.plugin.testing.ide.view.TestResultPresenter;
import org.eclipse.che.plugin.testing.testng.ide.TestNGLocalizationConstant;
import org.eclipse.che.plugin.testing.testng.ide.TestNGResources;

import com.google.inject.Inject;

/**
 * @author Mirage Abeysekara
 */
public class RunTestXMLAction extends JavaEditorAction
                              implements RunTestActionDelegate.Source {

    private final NotificationManager   notificationManager;
    private TestResultPresenter         presenter;
    private final TestServiceClient     service;
    private final RunTestActionDelegate delegate;

    @Inject
    public RunTestXMLAction(TestNGResources resources,
                            NotificationManager notificationManager,
                            EditorAgent editorAgent,
                            TestResultPresenter presenter,
                            FileTypeRegistry fileTypeRegistry,
                            TestServiceClient service,
                            TestNGLocalizationConstant localization) {
        super(localization.actionRunXMLTitle(), localization.actionRunXMLDescription(), resources.testAllIcon(),
              editorAgent, fileTypeRegistry);
        this.notificationManager = notificationManager;
        this.editorAgent = editorAgent;
        this.presenter = presenter;
        this.service = service;
        this.delegate = new RunTestActionDelegate(this);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final Project project = appContext.getRootProject();
        Map<String, String> parameters = new HashMap<>();

        parameters.put("testngXML",
                       project.getPath() + "/" + MavenAttributes.DEFAULT_TEST_RESOURCES_FOLDER + "/testng.xml");

        delegate.doRunTests(e, parameters);
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
