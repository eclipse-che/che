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
package org.eclipse.che.ide.ext.git.client.add;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.git.GitServiceClient;
import org.eclipse.che.ide.api.machine.DevMachine;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsole;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsoleFactory;
import org.eclipse.che.ide.extension.machine.client.processes.panel.ProcessesPanelPresenter;
import org.eclipse.che.ide.resource.Path;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;

/**
 * Presenter for add changes to Git index.
 *
 * @author Ann Zhuleva
 * @author Vlad Zhukovskyi
 * @author Igor Vinokur
 */
@Singleton
public class AddToIndexPresenter implements AddToIndexView.ActionDelegate {
    private static final String ADD_TO_INDEX_COMMAND_NAME = "Git add to index";

    private final AddToIndexView          view;
    private final GitServiceClient        service;
    private final GitLocalizationConstant constant;
    private final AppContext              appContext;
    private final NotificationManager     notificationManager;
    private final GitOutputConsoleFactory gitOutputConsoleFactory;
    private final ProcessesPanelPresenter consolesPanelPresenter;

    @Inject
    public AddToIndexPresenter(AddToIndexView view,
                               AppContext appContext,
                               GitLocalizationConstant constant,
                               GitOutputConsoleFactory gitOutputConsoleFactory,
                               ProcessesPanelPresenter processesPanelPresenter,
                               GitServiceClient service,
                               NotificationManager notificationManager) {
        this.view = view;
        this.view.setDelegate(this);
        this.service = service;
        this.constant = constant;
        this.appContext = appContext;
        this.notificationManager = notificationManager;
        this.gitOutputConsoleFactory = gitOutputConsoleFactory;
        this.consolesPanelPresenter = processesPanelPresenter;
    }

    public void showDialog() {
        if (appContext.getResources().length == 1) {
            Resource resource = appContext.getResource();
            if (resource instanceof Container) {
                view.setMessage(constant.addToIndexFolder(resource.getName()));
            } else {
                view.setMessage(constant.addToIndexFile(resource.getName()));
            }
        } else {
            view.setMessage(constant.addToIndexMultiSelect());
        }
        view.setUpdated(false);
        view.showDialog();
    }

    /** {@inheritDoc} */
    @Override
    public void onAddClicked() {
        DevMachine devMachine = appContext.getDevMachine();
        Resource[] resources = appContext.getResources();
        Path projectLocation = appContext.getRootProject().getLocation();
        Path[] paths = new Path[resources.length];
        for (int i = 0; i < resources.length; i++) {
            Path path = resources[i].getLocation().removeFirstSegments(projectLocation.segmentCount());
            paths[i] = path.segmentCount() == 0 ? Path.EMPTY : path;
        }
        final GitOutputConsole console = gitOutputConsoleFactory.create(ADD_TO_INDEX_COMMAND_NAME);
        consolesPanelPresenter.addCommandOutput(devMachine.getId(), console);
        service.add(devMachine, projectLocation, view.isUpdated(), paths)
               .then(new Operation<Void>() {
                   @Override
                   public void apply(Void arg) throws OperationException {
                       console.print(constant.addSuccess());
                       notificationManager.notify(constant.addSuccess());
                       view.close();
                   }
               })
               .catchError(new Operation<PromiseError>() {
                   @Override
                   public void apply(PromiseError arg) throws OperationException {
                       console.printError(constant.addFailed());
                       notificationManager.notify(constant.addFailed(), FAIL, FLOAT_MODE);
                       view.close();
                   }
               });
    }

    /** {@inheritDoc} */
    @Override
    public void onCancelClicked() {
        view.close();
    }
}
