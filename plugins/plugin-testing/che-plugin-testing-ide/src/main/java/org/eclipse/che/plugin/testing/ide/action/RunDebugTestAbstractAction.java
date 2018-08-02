/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.testing.ide.action;

import static java.util.Collections.emptyMap;
import static org.eclipse.che.api.testing.shared.TestExecutionContext.ContextType.CURSOR_POSITION;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.PROGRESS;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.SUCCESS;

import java.util.List;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcError;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcPromise;
import org.eclipse.che.api.testing.shared.TestExecutionContext;
import org.eclipse.che.api.testing.shared.TestLaunchResult;
import org.eclipse.che.api.testing.shared.TestPosition;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.debug.DebugConfiguration;
import org.eclipse.che.ide.api.debug.DebugConfigurationsManager;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.util.Pair;
import org.eclipse.che.plugin.testing.ide.TestServiceClient;
import org.eclipse.che.plugin.testing.ide.detector.TestDetector;
import org.eclipse.che.plugin.testing.ide.handler.TestingHandler;
import org.eclipse.che.plugin.testing.ide.model.GeneralTestingEventsProcessor;
import org.eclipse.che.plugin.testing.ide.view.TestResultPresenter;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Abstract action that analyzes active editor and makes run/debug test actions active if some test
 * methods are exist.
 */
public abstract class RunDebugTestAbstractAction extends AbstractPerspectiveAction {
  private TestDetector testDetector;
  private TestResultPresenter testResultPresenter;
  private TestingHandler testingHandler;
  private DebugConfigurationsManager debugConfigurationsManager;
  private TestServiceClient client;
  private DtoFactory dtoFactory;
  private AppContext appContext;
  private NotificationManager notificationManager;

  protected String selectedNodePath;

  public RunDebugTestAbstractAction(
      TestDetector testDetector,
      TestResultPresenter testResultPresenter,
      TestingHandler testingHandler,
      DebugConfigurationsManager debugConfigurationsManager,
      TestServiceClient client,
      DtoFactory dtoFactory,
      AppContext appContext,
      NotificationManager notificationManager,
      @Nullable List<String> perspectives,
      @NotNull String description,
      @NotNull String text,
      SVGResource icon) {
    super(perspectives, text, description, icon);
    this.testDetector = testDetector;
    this.testResultPresenter = testResultPresenter;
    this.testingHandler = testingHandler;
    this.debugConfigurationsManager = debugConfigurationsManager;
    this.client = client;
    this.dtoFactory = dtoFactory;
    this.appContext = appContext;
    this.notificationManager = notificationManager;
  }

  @Override
  public abstract void updateInPerspective(@NotNull ActionEvent event);

  @Override
  public abstract void actionPerformed(ActionEvent e);

  /**
   * Creates test execution context which describes the configuration for the current test
   *
   * @param frameworkAndTestName name of the test framework
   * @return test execution context
   */
  private TestExecutionContext createTestExecutionContext(
      Pair<String, String> frameworkAndTestName,
      TestExecutionContext.ContextType contextType,
      String selectedNodePath) {
    Project project;
    Resource resource = appContext.getResource();
    if (resource == null || resource.getProject() == null) {
      project = appContext.getRootProject();
    } else {
      project = resource.getProject();
    }
    TestExecutionContext context = dtoFactory.createDto(TestExecutionContext.class);

    context.setProjectPath(project.getPath());
    context.setContextType(contextType);
    if (contextType == CURSOR_POSITION) {
      TextEditor currentEditor = testDetector.getCurrentEditor();
      context.setFilePath(currentEditor.getEditorInput().getFile().getLocation().toString());
      context.setCursorOffset(currentEditor.getCursorOffset());
    } else {
      context.setFilePath(selectedNodePath);
    }
    context.setFrameworkName(frameworkAndTestName.first);

    return context;
  }

  /**
   * Method analyzes cursor position. The result is a pair which has framework name as a first
   * parameter and method name as a second if cursor is in body of test method otherwise the second
   * parameter is null.
   */
  protected Pair<String, String> getTestingFrameworkAndTestName() {
    TextEditor currentEditor = testDetector.getCurrentEditor();
    List<TestPosition> testPosition = testDetector.getTestPosition();
    int cursorOffset = currentEditor.getCursorOffset();
    for (TestPosition position : testPosition) {
      int testNameStartOffset = position.getTestNameStartOffset();
      if (testNameStartOffset <= cursorOffset
          && testNameStartOffset + position.getTestBodyLength() >= cursorOffset) {
        return Pair.of(position.getFrameworkName(), position.getTestName());
      }
    }
    return Pair.of(testPosition.iterator().next().getFrameworkName(), null);
  }

  /**
   * Runs an action.
   *
   * @param frameworkAndTestName contains name of the test framework and test methods
   * @param isDebugMode is {@code true} if the action uses for debugging
   */
  protected void actionPerformed(Pair<String, String> frameworkAndTestName, boolean isDebugMode) {
    final StatusNotification notification =
        new StatusNotification("Running Tests...", PROGRESS, FLOAT_MODE);
    notificationManager.notify(notification);

    TestExecutionContext context =
        createTestExecutionContext(
            frameworkAndTestName, testDetector.getContextType(), selectedNodePath);
    context.withDebugModeEnable(isDebugMode);

    GeneralTestingEventsProcessor eventsProcessor =
        new GeneralTestingEventsProcessor(
            frameworkAndTestName.first, testResultPresenter.getRootState());
    testingHandler.setProcessor(eventsProcessor);
    eventsProcessor.addListener(testResultPresenter.getEventListener());

    JsonRpcPromise<TestLaunchResult> testResultPromise = client.runTests(context);
    testResultPromise
        .onSuccess(
            result ->
                onTestRanSuccessfully(
                    result, eventsProcessor, notification, frameworkAndTestName.first, isDebugMode))
        .onFailure(exception -> onTestRanFailed(exception, notification));
  }

  private void onTestRanSuccessfully(
      TestLaunchResult result,
      GeneralTestingEventsProcessor eventsProcessor,
      StatusNotification notification,
      String frameworkName,
      boolean isDebugMode) {
    notification.setStatus(SUCCESS);
    if (result.isSuccess()) {
      notification.setTitle("Test runner executed successfully.");
      testResultPresenter.handleResponse();
      if (isDebugMode) {
        runRemoteJavaDebugger(eventsProcessor, frameworkName, result.getDebugPort());
      }
    } else {
      notification.setTitle("Test runner failed to execute.");
    }
  }

  private void onTestRanFailed(JsonRpcError exception, StatusNotification notification) {
    final String errorMessage =
        (exception.getMessage() != null) ? exception.getMessage() : "Failed to run test cases";
    notification.setContent(errorMessage);
    notification.setStatus(FAIL);
  }

  private void runRemoteJavaDebugger(
      GeneralTestingEventsProcessor eventsProcessor, String frameworkName, int port) {
    DebugConfiguration debugger =
        debugConfigurationsManager.createConfiguration(
            "jdb", frameworkName, "localhost", port, emptyMap());
    eventsProcessor.setDebuggerConfiguration(debugger, debugConfigurationsManager);

    debugConfigurationsManager.apply(debugger);
  }
}
