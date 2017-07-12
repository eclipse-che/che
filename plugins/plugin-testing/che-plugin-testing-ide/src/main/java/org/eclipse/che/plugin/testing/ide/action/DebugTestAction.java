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
import org.eclipse.che.api.project.shared.dto.ProjectTypeDto;
import org.eclipse.che.api.testing.shared.TestExecutionContext;
import org.eclipse.che.api.testing.shared.TestLaunchResult;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.debug.DebugConfiguration;
import org.eclipse.che.ide.api.debug.DebugConfigurationsManager;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.api.project.type.ProjectTypeRegistry;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.util.Pair;
import org.eclipse.che.plugin.testing.ide.TestResources;
import org.eclipse.che.plugin.testing.ide.TestServiceClient;
import org.eclipse.che.plugin.testing.ide.handler.TestingHandler;
import org.eclipse.che.plugin.testing.ide.model.GeneralTestingEventsProcessor;
import org.eclipse.che.plugin.testing.ide.view2.TestResultPresenter;

import javax.inject.Inject;

import static java.lang.Boolean.TRUE;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.PROGRESS;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.SUCCESS;
import static org.eclipse.che.ide.part.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

/** Action that allows to run tests from current editor. */
@Singleton
public class DebugTestAction extends RunDebugTestAbstractAction {
    private final TestServiceClient          client;
    private final DebugConfigurationsManager debugConfigurationsManager;
    private final ProjectTypeRegistry        projectTypeRegistry;
    private final AppContext                 appContext;
    private final NotificationManager        notificationManager;
    private final TestingHandler             testingHandler;
    private final TestResultPresenter        testResultPresenter;

    private Pair<String, String> frameworkAndTestName;

    @Inject
    public DebugTestAction(EventBus eventBus,
                           TestServiceClient client,
                           DebugConfigurationsManager debugConfigurationsManager,
                           DtoFactory dtoFactory,
                           TestResources testResources,
                           ProjectTypeRegistry projectTypeRegistry,
                           AppContext appContext,
                           NotificationManager notificationManager,
                           TestingHandler testingHandler,
                           TestResultPresenter testResultPresenter) {
        super(eventBus,
              client,
              dtoFactory,
              appContext,
              notificationManager,
              singletonList(PROJECT_PERSPECTIVE_ID),
              "Debug Test",
              "Debug Test",
              testResources.debugIcon());
        this.client = client;
        this.debugConfigurationsManager = debugConfigurationsManager;
        this.projectTypeRegistry = projectTypeRegistry;
        this.appContext = appContext;
        this.notificationManager = notificationManager;
        this.testingHandler = testingHandler;
        this.testResultPresenter = testResultPresenter;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final StatusNotification notification = new StatusNotification("Debugging Tests...", PROGRESS, FLOAT_MODE);
        notificationManager.notify(notification);

        frameworkAndTestName = getTestingFrameworkAndTestName();
        TestExecutionContext context = createTestExecutionContext(frameworkAndTestName);

        context.withDebugModeEnable(TRUE);

        GeneralTestingEventsProcessor eventsProcessor = new GeneralTestingEventsProcessor(frameworkAndTestName.first,
                                                                                          testResultPresenter.getRootState());
        testingHandler.setProcessor(eventsProcessor);
        eventsProcessor.addListener(testResultPresenter.getEventListener());

        JsonRpcPromise<TestLaunchResult> testResultPromise = client.runTests(context);
        testResultPromise.onSuccess(result -> onTestRanSuccessfully(result, eventsProcessor, notification))
                         .onFailure(exception -> onTestRanUnsuccessfully(exception, notification));
    }

    private void onTestRanSuccessfully(TestLaunchResult result,
                                       GeneralTestingEventsProcessor eventsProcessor,
                                       StatusNotification notification) {
        notification.setStatus(SUCCESS);
        if (result.isSuccess()) {
            notification.setTitle("Test runner executed successfully.");
            testResultPresenter.handleResponse();

            Project rootProject = appContext.getRootProject();
            String type = rootProject.getType();
            ProjectTypeDto projectType = projectTypeRegistry.getProjectType(type);

            if (projectType.getParents().contains("java")) {
                runRemoteJavaDebugger(eventsProcessor, result.getDebugPort());
            }
        } else {
            notification.setTitle("Test runner failed to execute.");
        }
    }

    private void runRemoteJavaDebugger(GeneralTestingEventsProcessor eventsProcessor, int port) {
        DebugConfiguration debugger = debugConfigurationsManager.createConfiguration("jdb",
                                                                                     frameworkAndTestName.first,
                                                                                     "localhost",
                                                                                     port,
                                                                                     emptyMap());
        eventsProcessor.setDebuggerConfiguration(debugger, debugConfigurationsManager);

        debugConfigurationsManager.apply(debugger);
    }

    private void onTestRanUnsuccessfully(JsonRpcError exception, StatusNotification notification) {
        final String errorMessage = (exception.getMessage() != null) ? exception.getMessage()
                                                                     : "Failed to run test cases";
        notification.setContent(errorMessage);
        notification.setStatus(FAIL);
    }

}
