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
package org.eclipse.che.ide.ext.git.client.remove;

import com.google.inject.Inject;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.git.GitServiceClient;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsole;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsoleFactory;
import org.eclipse.che.ide.extension.machine.client.processes.panel.ProcessesPanelPresenter;
import org.eclipse.che.ide.resource.Path;

import javax.validation.constraints.NotNull;

import static com.google.common.base.Preconditions.checkState;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.util.Arrays.isNullOrEmpty;

/**
 * Presenter for removing files from index and file system.
 *
 * @author Ann Zhuleva
 * @author Vlad Zhukovskyi
 */
public class RemoveFromIndexPresenter implements RemoveFromIndexView.ActionDelegate {
    public static final String REMOVE_FROM_INDEX_COMMAND_NAME = "Git remove from index";

    private final RemoveFromIndexView     view;
    private final GitServiceClient        service;
    private final GitLocalizationConstant constant;
    private final AppContext              appContext;
    private final NotificationManager     notificationManager;
    private final GitOutputConsoleFactory gitOutputConsoleFactory;
    private final ProcessesPanelPresenter consolesPanelPresenter;

    private Project project;

    @Inject
    public RemoveFromIndexPresenter(RemoveFromIndexView view,
                                    GitServiceClient service,
                                    GitLocalizationConstant constant,
                                    AppContext appContext,
                                    NotificationManager notificationManager,
                                    GitOutputConsoleFactory gitOutputConsoleFactory,
                                    ProcessesPanelPresenter processesPanelPresenter) {
        this.view = view;
        this.gitOutputConsoleFactory = gitOutputConsoleFactory;
        this.consolesPanelPresenter = processesPanelPresenter;
        this.view.setDelegate(this);
        this.service = service;
        this.constant = constant;
        this.appContext = appContext;
        this.notificationManager = notificationManager;
    }

    public void showDialog(Project project) {
        Resource[] resources = appContext.getResources();
        checkState(resources != null && resources.length > 0);
        this.project = project;
        if (resources.length == 1) {
            Resource resource = appContext.getResource();
            String selectedItemName = resource.getName();
            if (resource instanceof Container) {
                view.setMessage(constant.removeFromIndexFolder(selectedItemName));
            } else {
                view.setMessage(constant.removeFromIndexFile(selectedItemName));
            }
        } else {
            view.setMessage(constant.removeFromIndexAll());
        }
        view.setRemoved(false);
        view.showDialog();
    }

    /** {@inheritDoc} */
    @Override
    public void onRemoveClicked() {
        final GitOutputConsole console = gitOutputConsoleFactory.create(REMOVE_FROM_INDEX_COMMAND_NAME);

        final Resource[] resources = appContext.getResources();

        checkState(!isNullOrEmpty(resources));

        service.remove(appContext.getDevMachine(), project.getLocation(), toRelativePaths(resources), view.isRemoved()).then(new Operation<Void>() {
            @Override
            public void apply(Void ignored) throws OperationException {
                console.print(constant.removeFilesSuccessfull());
                consolesPanelPresenter.addCommandOutput(appContext.getDevMachine().getId(), console);
                notificationManager.notify(constant.removeFilesSuccessfull());

                project.synchronize();
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError error) throws OperationException {
                handleError(error.getCause(), console);
                consolesPanelPresenter.addCommandOutput(appContext.getDevMachine().getId(), console);
            }
        });

        view.close();
    }

    protected Path[] toRelativePaths(Resource[] resources) {
        final Path[] paths = new Path[resources.length];

        for (int i = 0; i < resources.length; i++) {
            checkState(project.getLocation().isPrefixOf(resources[i].getLocation()));

            final Path tmpPath = resources[i].getLocation().removeFirstSegments(project.getLocation().segmentCount());

            paths[i] = tmpPath.isEmpty() ? tmpPath.append(".") : tmpPath;
        }

        return paths;
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
        notificationManager.notify(constant.removeFilesFailed(), FAIL, FLOAT_MODE);
    }

    /** {@inheritDoc} */
    @Override
    public void onCancelClicked() {
        view.close();
    }

}
