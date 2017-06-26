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

import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.testing.shared.TestDetectionContext;
import org.eclipse.che.api.testing.shared.TestExecutionContext;
import org.eclipse.che.api.testing.shared.TestPosition;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.Presentation;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.event.ActivePartChangedEvent;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.java.client.editor.JavaReconsilerEvent;
import org.eclipse.che.ide.util.Pair;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.plugin.testing.ide.TestServiceClient;
import org.vectomatic.dom.svg.ui.SVGResource;

import javax.validation.constraints.NotNull;
import java.util.List;

import static org.eclipse.che.api.testing.shared.TestExecutionContext.TestType.CURSOR_POSITION;

/** Abstract action that analyzes active editor and makes run/debug test actions active if some test methods are exist. */
public abstract class RunDebugTestAbstractAction extends AbstractPerspectiveAction {
    TextEditor currentEditor;

    private TestExecutionContext.TestType testType;
    private List<TestPosition>            testPosition;
    private boolean                       hasTests;
    private TestServiceClient             client;
    private DtoFactory                    dtoFactory;
    private AppContext                    appContext;
    private NotificationManager           notificationManager;

    RunDebugTestAbstractAction(EventBus eventBus,
                               TestServiceClient client,
                               DtoFactory dtoFactory,
                               AppContext appContext,
                               NotificationManager notificationManager,
                               @Nullable List<String> perspectives,
                               @NotNull String description,
                               @NotNull String text,
                               SVGResource icon) {
        super(perspectives, text, description, null, icon);
        this.client = client;
        this.dtoFactory = dtoFactory;
        this.appContext = appContext;
        this.notificationManager = notificationManager;

        hasTests = false;

        eventBus.addHandler(JavaReconsilerEvent.TYPE, event -> detectTests(event.getEditor()));
        eventBus.addHandler(ActivePartChangedEvent.TYPE, event -> {
            if (event.getActivePart() instanceof TextEditor) {
                testType = CURSOR_POSITION;
                TextEditor activeEditor = (TextEditor)event.getActivePart();
                if (activeEditor.getEditorInput().getFile().getName().endsWith(".java")) {
                    detectTests(activeEditor);
                } else {
                    hasTests = false;
                }
            }
        });
    }

    @Override
    public void updateInPerspective(@NotNull ActionEvent event) {
        Presentation presentation = event.getPresentation();
        presentation.setEnabled(hasTests);
    }

    @Override
    public abstract void actionPerformed(ActionEvent e);

    private void detectTests(TextEditor editor) {
        this.currentEditor = editor;
        TestDetectionContext context = dtoFactory.createDto(TestDetectionContext.class);
        context.setFilePath(currentEditor.getEditorInput().getFile().getLocation().toString());
        context.setOffset(currentEditor.getCursorOffset());
        context.setProjectPath(appContext.getRootProject().getPath());
        client.detectTests(context).onSuccess(testDetectionResult -> {
            hasTests = testDetectionResult.isTestFile();
            testPosition = testDetectionResult.getTestPosition();
        }).onFailure(jsonRpcError -> {
            Log.error(getClass(), jsonRpcError);
            hasTests = false;
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
    protected TestExecutionContext createTestExecutionContext(Pair<String, String> frameworkAndTestName) {
        final Project project = appContext.getRootProject();
        TestExecutionContext context = dtoFactory.createDto(TestExecutionContext.class);

        context.setProjectPath(project.getPath());
        context.setTestType(testType);
        context.setFilePath(currentEditor.getEditorInput().getFile().getLocation().toString());
        context.setCursorOffset(currentEditor.getCursorOffset());
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
}
