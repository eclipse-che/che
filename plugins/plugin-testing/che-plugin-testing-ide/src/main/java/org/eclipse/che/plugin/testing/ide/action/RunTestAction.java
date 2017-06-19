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
package org.eclipse.che.plugin.testing.ide.action;

import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcError;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcPromise;
import org.eclipse.che.api.testing.shared.TestExecutionContext;
import org.eclipse.che.api.testing.shared.TestPosition;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.util.Pair;
import org.eclipse.che.plugin.testing.ide.TestServiceClient;
import org.eclipse.che.plugin.testing.ide.handler.TestingHandler;
import org.eclipse.che.plugin.testing.ide.model.GeneralTestingEventsProcessor;
import org.eclipse.che.plugin.testing.ide.view2.TestResultPresenter;

import javax.inject.Inject;

import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.PROGRESS;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.SUCCESS;
import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

/** Action that allows to run tests from current editor. */
@Singleton
public class RunTestAction extends RunDebugTestAbstractAction {
    private final TestServiceClient   client;
    private final DtoFactory          dtoFactory;
    private final AppContext          appContext;
    private final NotificationManager notificationManager;
    private final TestingHandler      testingHandler;
    private final TestResultPresenter testResultPresenter;

    @Inject
    public RunTestAction(EventBus eventBus,
                         TestServiceClient client,
                         DtoFactory dtoFactory,
                         AppContext appContext,
                         NotificationManager notificationManager,
                         TestingHandler testingHandler,
                         TestResultPresenter testResultPresenter) {
        super(eventBus, client, dtoFactory, appContext, notificationManager, singletonList(PROJECT_PERSPECTIVE_ID), "Run Test");
        this.client = client;
        this.dtoFactory = dtoFactory;
        this.appContext = appContext;
        this.notificationManager = notificationManager;
        this.testingHandler = testingHandler;
        this.testResultPresenter = testResultPresenter;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final StatusNotification notification = new StatusNotification("Running Tests...", PROGRESS, FLOAT_MODE);
        notificationManager.notify(notification);

        Pair<String, String> frameworkAndTestName = getTestingFrameworkAndTestName();
        TestExecutionContext context = createTestExecutionContext(frameworkAndTestName);

        GeneralTestingEventsProcessor eventsProcessor = new GeneralTestingEventsProcessor(frameworkAndTestName.first,
                                                                                          testResultPresenter.getRootState());
        testingHandler.setProcessor(eventsProcessor);
        eventsProcessor.addListener(testResultPresenter.getEventListener());

        JsonRpcPromise<Boolean> testResultPromise = client.runTests(context);
        testResultPromise.onSuccess(result -> onTestRanSuccessfully(result, notification))
                         .onFailure(exception -> onTestRanUnsuccessfully(exception, notification));
    }

    private void onTestRanSuccessfully(Boolean isExecutedSuccessfully, StatusNotification notification) {
        notification.setStatus(SUCCESS);
        if (isExecutedSuccessfully) {
            notification.setTitle("Test runner executed successfully.");
            testResultPresenter.handleResponse();
        } else {
            notification.setTitle("Test runner failed to execute.");
        }
    }

    private void onTestRanUnsuccessfully(JsonRpcError exception, StatusNotification notification) {
        final String errorMessage = (exception.getMessage() != null) ? exception.getMessage()
                                                                     : "Failed to run test cases";
        notification.setContent(errorMessage);
        notification.setStatus(FAIL);
    }

    private TestExecutionContext createTestExecutionContext(Pair<String, String> frameworkAndTestName) {
        final Project project = appContext.getRootProject();
        TestExecutionContext context = dtoFactory.createDto(TestExecutionContext.class);

        context.setProjectPath(project.getPath());
        context.setTestType(testType);
        context.setFilePath(currentEditor.getEditorInput().getFile().getLocation().toString());
        context.setCursorOffset(currentEditor.getCursorOffset());
        context.setFrameworkName(frameworkAndTestName.first);

        return context;
    }

    private Pair<String, String> getTestingFrameworkAndTestName() {
        int cursorOffset = currentEditor.getCursorOffset();
        for (TestPosition position : testPosition) {
            int testNameStartOffset = position.getTestNameStartOffset();
            if (testNameStartOffset <= cursorOffset && testNameStartOffset + position.getTestBodyLength() >= cursorOffset) {
                return Pair.of(position.getFrameworkName(), position.getTestName());
            }
        }
        return Pair.of(testPosition.iterator().next().getFrameworkName(), null);
    }
}
