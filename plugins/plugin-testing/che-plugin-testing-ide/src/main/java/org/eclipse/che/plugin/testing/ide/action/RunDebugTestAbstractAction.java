/*******************************************************************************
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.testing.ide.action;

import com.google.common.base.Optional;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcError;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcPromise;
import org.eclipse.che.api.testing.shared.TestDetectionContext;
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
import org.eclipse.che.ide.api.parts.ActivePartChangedEvent;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.api.parts.PartPresenter;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.java.client.editor.JavaReconsilerEvent;
import org.eclipse.che.ide.ext.java.client.resource.SourceFolderMarker;
import org.eclipse.che.ide.ext.java.client.util.JavaUtil;
import org.eclipse.che.ide.util.Pair;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.plugin.testing.ide.TestServiceClient;
import org.eclipse.che.plugin.testing.ide.handler.TestingHandler;
import org.eclipse.che.plugin.testing.ide.model.GeneralTestingEventsProcessor;
import org.eclipse.che.plugin.testing.ide.view2.TestResultPresenter;
import org.vectomatic.dom.svg.ui.SVGResource;

import javax.validation.constraints.NotNull;
import java.util.List;

import static java.util.Collections.emptyMap;
import static org.eclipse.che.api.testing.shared.TestExecutionContext.ContextType.CURSOR_POSITION;
import static org.eclipse.che.api.testing.shared.TestExecutionContext.ContextType.PROJECT;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.PROGRESS;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.SUCCESS;
import static org.eclipse.che.ide.api.resources.Resource.FILE;
import static org.eclipse.che.ide.ext.java.client.util.JavaUtil.isJavaFile;

/** Abstract action that analyzes active editor and makes run/debug test actions active if some test methods are exist. */
public abstract class RunDebugTestAbstractAction extends AbstractPerspectiveAction {
    public static final String JUNIT_FRAMEWORK_NAME  = "junit";
    public static final String TESTNG_FRAMEWORK_NAME = "testng";

    private TextEditor                       currentEditor;
    private List<TestPosition>               testPosition;
    private TestResultPresenter              testResultPresenter;
    private TestingHandler                   testingHandler;
    private DebugConfigurationsManager       debugConfigurationsManager;
    private TestServiceClient                client;
    private DtoFactory                       dtoFactory;
    private AppContext                       appContext;
    private NotificationManager              notificationManager;
    private TestExecutionContext.ContextType contextType;
    private String                           selectedNodePath;

    protected boolean isEnable;
    protected boolean isEditorInFocus;

    public RunDebugTestAbstractAction(EventBus eventBus,
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
        super(perspectives, text, description, null, icon);
        this.testResultPresenter = testResultPresenter;
        this.testingHandler = testingHandler;
        this.debugConfigurationsManager = debugConfigurationsManager;
        this.client = client;
        this.dtoFactory = dtoFactory;
        this.appContext = appContext;
        this.notificationManager = notificationManager;

        isEnable = false;

        eventBus.addHandler(JavaReconsilerEvent.TYPE, event -> detectTests(event.getEditor()));
        eventBus.addHandler(ActivePartChangedEvent.TYPE, event -> {
            PartPresenter activePart = event.getActivePart();
            if (activePart instanceof TextEditor) {
                isEditorInFocus = true;
                contextType = CURSOR_POSITION;
                TextEditor activeEditor = (TextEditor)activePart;
                if (activeEditor.getEditorInput().getFile().getName().endsWith(".java")) {
                    detectTests(activeEditor);
                } else {
                    isEnable = false;
                }
            } else {
                isEditorInFocus = false;
            }
        });
    }

    @Override
    public abstract void updateInPerspective(@NotNull ActionEvent event);

    @Override
    public abstract void actionPerformed(ActionEvent e);

    private void detectTests(TextEditor editor) {
        this.currentEditor = editor;
        TestDetectionContext context = dtoFactory.createDto(TestDetectionContext.class);
        context.setFilePath(currentEditor.getEditorInput().getFile().getLocation().toString());
        context.setOffset(currentEditor.getCursorOffset());
        context.setProjectPath(appContext.getRootProject().getPath());
        client.detectTests(context).onSuccess(testDetectionResult -> {
            isEnable = testDetectionResult.isTestFile();
            testPosition = testDetectionResult.getTestPosition();
        }).onFailure(jsonRpcError -> {
            Log.error(getClass(), jsonRpcError);
            isEnable = false;
            notificationManager.notify("Can't detect test methods");
        });
    }

    /**
     * Creates test execution context which describes the configuration for the current test
     *
     * @param frameworkAndTestName
     *         name of the test framework
     * @return test execution context
     */
    protected TestExecutionContext createTestExecutionContext(Pair<String, String> frameworkAndTestName,
                                                              TestExecutionContext.ContextType contextType,
                                                              String selectedNodePath) {
        final Project project = appContext.getRootProject();
        TestExecutionContext context = dtoFactory.createDto(TestExecutionContext.class);

        context.setProjectPath(project.getPath());
        context.setContextType(contextType);
        if (contextType == CURSOR_POSITION) {
            context.setFilePath(currentEditor.getEditorInput().getFile().getLocation().toString());
            context.setCursorOffset(currentEditor.getCursorOffset());
        } else {
            context.setFilePath(selectedNodePath);
        }
        context.setFrameworkName(frameworkAndTestName.first);

        return context;
    }

    /**
     * Method analyzes cursor position.
     * The result is a pair which has framework name as a first parameter and method name as a second if cursor is in body of test method
     * otherwise the second parameter is null.
     */
    protected Pair<String, String> getTestingFrameworkAndTestName() {
        int cursorOffset = currentEditor.getCursorOffset();
        for (TestPosition position : testPosition) {
            int testNameStartOffset = position.getTestNameStartOffset();
            if (testNameStartOffset <= cursorOffset && testNameStartOffset + position.getTestBodyLength() >= cursorOffset) {
                return Pair.of(position.getFrameworkName(), position.getTestName());
            }
        }
        return Pair.of(testPosition.iterator().next().getFrameworkName(), null);
    }

    /**
     * Runs an action.
     *
     * @param frameworkAndTestName
     *         contains name of the test framework and test methods
     * @param isDebugMode
     *         is {@code true} if the action uses for debugging
     */
    protected void actionPerformed(Pair<String, String> frameworkAndTestName, boolean isDebugMode) {
        final StatusNotification notification = new StatusNotification("Running Tests...", PROGRESS, FLOAT_MODE);
        notificationManager.notify(notification);

        TestExecutionContext context = createTestExecutionContext(frameworkAndTestName, contextType, selectedNodePath);
        context.withDebugModeEnable(isDebugMode);

        GeneralTestingEventsProcessor eventsProcessor = new GeneralTestingEventsProcessor(frameworkAndTestName.first,
                                                                                          testResultPresenter.getRootState());
        testingHandler.setProcessor(eventsProcessor);
        eventsProcessor.addListener(testResultPresenter.getEventListener());

        JsonRpcPromise<TestLaunchResult> testResultPromise = client.runTests(context);
        testResultPromise.onSuccess(result -> onTestRanSuccessfully(result,
                                                                    eventsProcessor,
                                                                    notification,
                                                                    frameworkAndTestName.first,
                                                                    isDebugMode))
                         .onFailure(exception -> onTestRanFailed(exception, notification));
    }

    /**
     * Analyzes project tree selection. Needs for detecting context type for the test runner.
     */
    protected void analyzeProjectTreeSelection() {
        Resource[] resources = appContext.getResources();
        if (resources == null || resources.length > 1) {
            isEnable = false;
            return;
        }

        Resource resource = resources[0];
        if (resource.isProject() && JavaUtil.isJavaProject((Project)resource)) {
            contextType = PROJECT;
            isEnable = true;
            return;
        }

        Project project = resource.getProject();
        if (!JavaUtil.isJavaProject(project)) {
            isEnable = false;
            return;
        }

        Optional<Resource> srcFolder = resource.getParentWithMarker(SourceFolderMarker.ID);
        if (!srcFolder.isPresent() || resource.getLocation().equals(srcFolder.get().getLocation())) {
            isEnable = false;
            return;
        }

        if (resource.getResourceType() == FILE && isJavaFile(resource)) {
            contextType = TestExecutionContext.ContextType.FILE;
        } else if (resource instanceof Container) {
            contextType = TestExecutionContext.ContextType.FOLDER;
        }
        selectedNodePath = resource.getLocation().toString();
        isEnable = true;
    }

    private void onTestRanSuccessfully(TestLaunchResult result,
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
        final String errorMessage = (exception.getMessage() != null) ? exception.getMessage()
                                                                     : "Failed to run test cases";
        notification.setContent(errorMessage);
        notification.setStatus(FAIL);
    }

    private void runRemoteJavaDebugger(GeneralTestingEventsProcessor eventsProcessor, String frameworkName, int port) {
        DebugConfiguration debugger = debugConfigurationsManager.createConfiguration("jdb",
                                                                                     frameworkName,
                                                                                     "localhost",
                                                                                     port,
                                                                                     emptyMap());
        eventsProcessor.setDebuggerConfiguration(debugger, debugConfigurationsManager);

        debugConfigurationsManager.apply(debugger);
    }
}
