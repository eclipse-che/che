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
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.filetypes.FileTypeRegistry;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.java.client.action.JavaEditorAction;
import org.eclipse.che.plugin.testing.ide.TestServiceClient;
import org.eclipse.che.plugin.testing.ide.action.RunTestActionDelegate;
import org.eclipse.che.plugin.testing.ide.handler.TestingHandler;
import org.eclipse.che.plugin.testing.ide.view.TestResultPresenter;
import org.eclipse.che.plugin.testing.testng.ide.TestNGLocalizationConstant;
import org.eclipse.che.plugin.testing.testng.ide.TestNGResources;

/**
 * @author Mirage Abeysekara
 */
public class RunClassTestAction extends JavaEditorAction
        implements RunTestActionDelegate.Source {

    private final NotificationManager notificationManager;
    private final DtoFactory dtoFactory;
    private final TestingHandler testingHandler;
    private final TestResultPresenter presenter;
    private final TestServiceClient service;
    private final RunTestActionDelegate delegate;

    @Inject
    public RunClassTestAction(TestNGResources resources,
                              NotificationManager notificationManager,
                              EditorAgent editorAgent,
                              FileTypeRegistry fileTypeRegistry,
                              TestResultPresenter presenter,
                              TestServiceClient service,
                              TestNGLocalizationConstant localization,
                              DtoFactory dtoFactory,
                              TestingHandler testingHandler) {
        super(localization.actionRunClassTitle(), localization.actionRunClassDescription(), resources.testIcon(),
                editorAgent, fileTypeRegistry);
        this.notificationManager = notificationManager;
        this.dtoFactory = dtoFactory;
        this.testingHandler = testingHandler;
        this.editorAgent = editorAgent;
        this.presenter = presenter;
        this.service = service;
        this.delegate = new RunTestActionDelegate(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final VirtualFile file = editorAgent.getActiveEditor().getEditorInput().getFile();
        TestExecutionContext context = dtoFactory.createDto(TestExecutionContext.class);
        context.setTestType(TestExecutionContext.TestType.FILE);
        context.setFilePath(file.getLocation().toString());
        delegate.doRunTests(e, context);
    }

    @Override
    protected void updateProjectAction(ActionEvent e) {
        super.updateProjectAction(e);
        e.getPresentation().setVisible(true);
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

    @Override
    public TestingHandler getTestingHandler() {
        return testingHandler;
    }
}
