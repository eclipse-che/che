/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * <p>
 * Contributors:
 * Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ext.java.testing.client.view;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import org.eclipse.che.api.git.shared.CheckoutRequest;
//import org.eclipse.che.api.project.gwt.client.ProjectServiceClient;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.text.TextPosition;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.event.FileEvent;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.api.project.ProjectServiceClient;
import org.eclipse.che.ide.api.project.tree.VirtualFile;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.java.client.projecttree.JavaSourceFolderUtil;
import org.eclipse.che.ide.ext.java.testing.client.TestServiceClient;
import org.eclipse.che.ide.ext.java.testing.shared.Failure;
import org.eclipse.che.ide.ext.java.testing.shared.TestResult;
import org.eclipse.che.ide.extension.machine.client.processes.ConsolesPanelPresenter;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;

import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.ide.websocket.rest.RequestCallback;
import org.eclipse.che.ide.websocket.rest.StringUnmarshallerWS;
import org.eclipse.che.ide.websocket.rest.Unmarshallable;

import static org.eclipse.che.ide.api.event.FileEvent.FileOperation.CLOSE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.*;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.*;

/**
 * Presenter for checkout reference(branch, tag) name or commit hash.
 *
 * @author Roman Nikitenko
 */
@Singleton
public class TestRunnerPresenter implements TestRunnerView.ActionDelegate {
    public static final String CHECKOUT_COMMAND_NAME = "Git checkout";

    private final NotificationManager notificationManager;
    private final TestServiceClient service;
    private final AppContext appContext;
    private final TestRunnerView view;
    private final ProjectExplorerPresenter projectExplorer;
    private final DtoFactory dtoFactory;
    private final EditorAgent editorAgent;
    private final EventBus eventBus;
    private final ProjectServiceClient projectService;
    private final DtoUnmarshallerFactory dtoUnmarshallerFactory;
    private final ConsolesPanelPresenter consolesPanelPresenter;

    @Inject
    public TestRunnerPresenter(TestRunnerView view,
                               TestServiceClient service,
                               AppContext appContext,
                               NotificationManager notificationManager,
                               ProjectExplorerPresenter projectExplorer,
                               DtoFactory dtoFactory,
                               EditorAgent editorAgent,
                               EventBus eventBus,
                               ProjectServiceClient projectServiceClient,
                               DtoUnmarshallerFactory dtoUnmarshallerFactory,
                               ConsolesPanelPresenter consolesPanelPresenter) {
        this.view = view;
        this.projectExplorer = projectExplorer;
        this.dtoFactory = dtoFactory;
        this.editorAgent = editorAgent;
        this.eventBus = eventBus;
        this.service = service;
        this.appContext = appContext;

        this.notificationManager = notificationManager;
        this.projectService = projectServiceClient;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.consolesPanelPresenter = consolesPanelPresenter;
        this.view.setDelegate(this);
    }

    /**
     * Show dialog.
     */
    public void showDialog() {
        view.showDialog();
    }

    @Override
    public void onCancelClicked() {
        Log.info(TestRunnerPresenter.class,"click started");
        EditorPartPresenter editorPart = editorAgent.getActiveEditor();
        final VirtualFile file = editorPart.getEditorInput().getFile();
        Document doc = ((TextEditor)editorPart).getDocument();
        Log.info(TestRunnerPresenter.class,doc.getLineStart(2));
        doc.setCursorPosition(new TextPosition(5,0));
//        eventBus.fireEvent(new FileEvent(file, CLOSE));
        view.close();
        Log.info(TestRunnerPresenter.class,"click fnished");
    }

    @Override
    public void onRunClicked() {
        view.setText("Running test cases...");
        final StatusNotification notification = new StatusNotification("Running Tests...", PROGRESS, FLOAT_MODE);
        notificationManager.notify(notification);
        final ProjectConfigDto project = appContext.getCurrentProject().getRootProject();
        EditorPartPresenter editorPart = editorAgent.getActiveEditor();
        final VirtualFile file = editorPart.getEditorInput().getFile();
        String fqn = JavaSourceFolderUtil.getFQNForFile(file);
        Unmarshallable<TestResult> unmarshaller = dtoUnmarshallerFactory.newWSUnmarshaller(TestResult.class);

        service.runTest(appContext.getWorkspaceId(),
                project,
                fqn,
                new RequestCallback<TestResult>(unmarshaller) {
                    @Override
                    protected void onSuccess(TestResult result) {
                        if(result.isSuccess()){
                            notification.setTitle("Test runner executed successfully");
                            notification.setContent("All tests are passed");
                            notification.setStatus(SUCCESS);
                            view.setText("All tests are passed");
                        }
                        else {
                            notification.setTitle("Test runner executed successfully with test failures.");
                            StringBuilder sb = new StringBuilder(result.getFailureCount() + " tests are failed.\n");

                            for(Failure failure : result.getFailures()){
                                sb.append(failure.getMessage()).append("\n")
                                        .append(failure.getTrace()).append("\n");
                            }
                            notification.setContent(sb.toString());
                            notification.setStatus(FAIL);
                            view.setText(sb.toString());
                        }

                    }

                    @Override
                    protected void onFailure(Throwable exception) {
                        final String errorMessage = (exception.getMessage() != null)
                                ? exception.getMessage()
                                : "Failed to run test cases";
                        notification.setContent(errorMessage);
                        notification.setStatus(FAIL);
                    }
                }
        );
    }


    @Override
    public void onEnterClicked() {
        onRunClicked();
    }


}
