/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ext.git.client.remove;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.git.gwt.client.GitServiceClient;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.event.FileEvent;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.project.node.HasStorablePath;
import org.eclipse.che.ide.api.project.node.Node;
import org.eclipse.che.ide.api.project.tree.VirtualFile;
import org.eclipse.che.ide.api.selection.Selection;
import org.eclipse.che.ide.api.selection.SelectionAgent;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsole;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsoleFactory;
import org.eclipse.che.ide.extension.machine.client.processes.ConsolesPanelPresenter;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.project.node.FileReferenceNode;
import org.eclipse.che.ide.project.node.FolderReferenceNode;
import org.eclipse.che.ide.project.node.ResourceBasedNode;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.upload.BasicUploadPresenter;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;

/**
 * Presenter for removing files from index and file system.
 *
 * @author Ann Zhuleva
 */
public class RemoveFromIndexPresenter extends BasicUploadPresenter implements RemoveFromIndexView.ActionDelegate {
    public static final String REMOVE_FROM_INDEX_COMMAND_NAME = "Git remove from index";

    private final RemoveFromIndexView      view;
    private final EventBus                 eventBus;
    private final ProjectExplorerPresenter projectExplorer;
    private final GitServiceClient         service;
    private final GitLocalizationConstant  constant;
    private final AppContext               appContext;
    private final SelectionAgent           selectionAgent;
    private final NotificationManager      notificationManager;
    private final EditorAgent              editorAgent;
    private final GitOutputConsoleFactory  gitOutputConsoleFactory;
    private final ConsolesPanelPresenter   consolesPanelPresenter;

    private CurrentProject project;

    /**
     * Create presenter
     *
     * @param view
     * @param service
     * @param constant
     * @param appContext
     * @param notificationManager
     */
    @Inject
    public RemoveFromIndexPresenter(RemoveFromIndexView view,
                                    EventBus eventBus,
                                    GitServiceClient service,
                                    GitLocalizationConstant constant,
                                    AppContext appContext,
                                    SelectionAgent selectionAgent,
                                    NotificationManager notificationManager,
                                    EditorAgent editorAgent,
                                    ProjectExplorerPresenter projectExplorer,
                                    GitOutputConsoleFactory gitOutputConsoleFactory,
                                    ConsolesPanelPresenter consolesPanelPresenter) {
        super(projectExplorer);
        this.view = view;
        this.eventBus = eventBus;
        this.projectExplorer = projectExplorer;
        this.gitOutputConsoleFactory = gitOutputConsoleFactory;
        this.consolesPanelPresenter = consolesPanelPresenter;
        this.view.setDelegate(this);
        this.service = service;
        this.constant = constant;
        this.appContext = appContext;
        this.selectionAgent = selectionAgent;
        this.notificationManager = notificationManager;
        this.editorAgent = editorAgent;
    }

    /** Show dialog. */
    public void showDialog() {
        project = appContext.getCurrentProject();
        String workDir = project.getRootProject().getPath();
        view.setMessage(formMessage(workDir));
        view.setRemoved(false);
        view.showDialog();
    }

    /**
     * Form the message to display for removing from index, telling the user what is gonna to be removed.
     *
     * @return {@link String} message to display
     */
    @NotNull
    private String formMessage(@NotNull String workDir) {
        Selection<ResourceBasedNode<?>> selection = (Selection<ResourceBasedNode<?>>)selectionAgent.getSelection();

        String path;
        if (selection == null || selection.getHeadElement() == null) {
            path = project.getRootProject().getPath();
        } else {
            path = ((HasStorablePath)selection.getHeadElement()).getStorablePath();
        }

        String pattern = path.replaceFirst(workDir, "");
        pattern = (pattern.startsWith("/")) ? pattern.replaceFirst("/", "") : pattern;

        // Root of the working tree:
        if (pattern.isEmpty() || "/".equals(pattern)) {
            return constant.removeFromIndexAll();
        }

        // Do not display path longer 40 characters
        if (pattern.length() > 40) {
            pattern = pattern.substring(0, 40) + "...";
        }

        if (selection != null && selection.getHeadElement() instanceof FolderReferenceNode) {
            return constant.removeFromIndexFolder(pattern).asString();
        } else {
            return constant.removeFromIndexFile(pattern).asString();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onRemoveClicked() {
        final GitOutputConsole console = gitOutputConsoleFactory.create(REMOVE_FROM_INDEX_COMMAND_NAME);
        service.remove(appContext.getDevMachine(), project.getRootProject(), getFilePatterns(), view.isRemoved(),
                       new AsyncRequestCallback<String>() {
                           @Override
                           protected void onSuccess(String result) {
                               console.print(constant.removeFilesSuccessfull());
                               consolesPanelPresenter.addCommandOutput(appContext.getDevMachine().getId(), console);
                               notificationManager.notify(constant.removeFilesSuccessfull(), project.getRootProject());

                               if (!view.isRemoved()) {
                                   projectExplorer.reloadChildren(getResourceBasedNode());

                                   if (projectExplorer.getSelection().getHeadElement() instanceof FileReferenceNode) {
                                       FileReferenceNode selectFile = ((FileReferenceNode)projectExplorer.getSelection().getHeadElement());
                                       //to close selected file if it open
                                       EditorPartPresenter openedEditor =
                                               editorAgent.getOpenedEditor(Path.valueOf(selectFile.getStorablePath()));
                                       if (openedEditor != null) {
                                           VirtualFile openedFile = openedEditor.getEditorInput().getFile();
                                           eventBus.fireEvent(new FileEvent(openedFile, FileEvent.FileOperation.CLOSE));
                                       }
                                   }
                               }
                           }

                           @Override
                           protected void onFailure(Throwable exception) {
                               handleError(exception, console);
                               consolesPanelPresenter.addCommandOutput(appContext.getDevMachine().getId(), console);
                           }
                       }
                      );
        view.close();
    }

    /**
     * Returns pattern of the items to be removed.
     *
     * @return pattern of the items to be removed
     */
    @NotNull
    private List<String> getFilePatterns() {
        Selection<ResourceBasedNode<?>> selection = (Selection<ResourceBasedNode<?>>)selectionAgent.getSelection();
        String path;
        if (selection == null || selection.getHeadElement() == null) {
            path = project.getRootProject().getPath();
        } else {
            path = ((HasStorablePath)selection.getHeadElement()).getStorablePath();
        }

        String pattern = path.replaceFirst(project.getRootProject().getPath(), "");
        pattern = (pattern.startsWith("/")) ? pattern.replaceFirst("/", "") : pattern;

        return (pattern.length() == 0 || "/".equals(pattern)) ? new ArrayList<>(Arrays.asList("."))
                                                              : new ArrayList<>(Arrays.asList(pattern));
    }

    /**
     * Handler some action whether some exception happened.
     *
     * @param e
     *         exception what happened
     */
    private void handleError(@NotNull Throwable e, GitOutputConsole console) {
        String errorMessage = (e.getMessage() != null && !e.getMessage().isEmpty()) ? e.getMessage() : constant.removeFilesFailed();
        console.printError(errorMessage);
        consolesPanelPresenter.addCommandOutput(appContext.getDevMachine().getId(), console);
        notificationManager.notify(constant.removeFilesFailed(), FAIL, FLOAT_MODE, project.getRootProject());
    }

    /** {@inheritDoc} */
    @Override
    public void onCancelClicked() {
        view.close();
    }

}
