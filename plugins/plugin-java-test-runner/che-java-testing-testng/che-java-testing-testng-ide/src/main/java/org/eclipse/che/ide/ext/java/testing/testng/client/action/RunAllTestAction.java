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
package org.eclipse.che.ide.ext.java.testing.testng.client.action;

import com.google.inject.Inject;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.filetypes.FileTypeRegistry;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.ext.java.client.action.JavaEditorAction;
import org.eclipse.che.ide.ext.java.testing.core.client.TestServiceClient;
import org.eclipse.che.ide.ext.java.testing.core.client.view.TestResultPresenter;
import org.eclipse.che.ide.ext.java.testing.core.shared.TestResult;
import org.eclipse.che.ide.ext.java.testing.testng.client.TestNGLocalizationConstant;
import org.eclipse.che.ide.ext.java.testing.testng.client.TestNGResources;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;

import java.util.HashMap;
import java.util.Map;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.PROGRESS;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.SUCCESS;

/**
 *
 * @author Mirage Abeysekara
 */
public class RunAllTestAction extends JavaEditorAction {

    private final NotificationManager notificationManager;
    private final EditorAgent editorAgent;
    private TestResultPresenter presenter;
    private final TestServiceClient service;
    private final DtoUnmarshallerFactory dtoUnmarshallerFactory;

    @Inject
    public RunAllTestAction(TestNGResources resources, NotificationManager notificationManager, EditorAgent editorAgent,
                            TestResultPresenter presenter, FileTypeRegistry fileTypeRegistry, TestServiceClient service,
                            DtoUnmarshallerFactory dtoUnmarshallerFactory, TestNGLocalizationConstant localization) {
        super(localization.actionRunAllTitle(), localization.actionRunAllDescription(), resources.testAllIcon(),
                editorAgent, fileTypeRegistry);
        this.notificationManager = notificationManager;
        this.editorAgent = editorAgent;
        this.presenter =  presenter;
        this.service = service;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final StatusNotification notification = new StatusNotification("Running Tests...", PROGRESS, FLOAT_MODE);
        notificationManager.notify(notification);

        final Project project = appContext.getRootProject();

        Map<String, String> parameters = new HashMap<>();
        parameters.put("updateClasspath", "true");

        Promise<TestResult> testResultPromise = service.getTestResult(project.getPath(), "testng", parameters);
        testResultPromise.then(new Operation<TestResult>() {
            @Override
            public void apply(TestResult result) throws OperationException {
                notification.setStatus(SUCCESS);
                if (result.isSuccess()) {
                    notification.setTitle("Test runner executed successfully");
                    notification.setContent("All tests are passed");
                } else {
                    notification.setTitle("Test runner executed successfully with test failures.");
                    notification.setContent(result.getFailureCount() + " test(s) failed.\n");
                }
                presenter.handleResponse(result);
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError exception) throws OperationException {
                final String errorMessage = (exception.getMessage() != null)
                        ? exception.getMessage()
                        : "Failed to run test cases";
                notification.setContent(errorMessage);
                notification.setStatus(FAIL);
            }
        });

    }

}
