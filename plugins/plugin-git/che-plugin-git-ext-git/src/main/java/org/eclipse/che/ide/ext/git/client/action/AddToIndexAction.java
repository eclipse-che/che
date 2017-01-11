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
package org.eclipse.che.ide.ext.git.client.action;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.git.shared.Status;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.git.GitServiceClient;
import org.eclipse.che.ide.api.machine.DevMachine;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.add.AddToIndexPresenter;
import org.eclipse.che.ide.FontAwesome;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsole;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsoleFactory;
import org.eclipse.che.ide.extension.machine.client.processes.panel.ProcessesPanelPresenter;
import org.eclipse.che.ide.resource.Path;

import java.util.List;

import static com.google.common.base.Preconditions.checkState;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;

/**
 * @author Andrey Plotnikov
 * @author Vlad Zhukovskyi
 * @author Igor Vinokur
 **/
@Singleton
public class AddToIndexAction extends GitAction {

    private final AddToIndexPresenter     presenter;
    private final GitLocalizationConstant constant;
    private final GitOutputConsoleFactory gitOutputConsoleFactory;
    private final ProcessesPanelPresenter consolesPanelPresenter;
    private final GitServiceClient        service;
    private final NotificationManager     notificationManager;

    @Inject
    public AddToIndexAction(AddToIndexPresenter presenter,
                            AppContext appContext,
                            GitLocalizationConstant constant,
                            GitOutputConsoleFactory gitOutputConsoleFactory,
                            ProcessesPanelPresenter consolesPanelPresenter,
                            GitServiceClient service,
                            NotificationManager notificationManager) {
        super(constant.addToIndexTitle(), constant.addToIndexTitle(), FontAwesome.PLUS, appContext);
        this.presenter = presenter;
        this.constant = constant;
        this.gitOutputConsoleFactory = gitOutputConsoleFactory;
        this.consolesPanelPresenter = consolesPanelPresenter;
        this.service = service;
        this.notificationManager = notificationManager;
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent e) {
        final Resource[] resources = appContext.getResources();
        checkState(resources != null);
        final DevMachine devMachine = appContext.getDevMachine();
        final GitOutputConsole console = gitOutputConsoleFactory.create(constant.addToIndexCommandName());
        consolesPanelPresenter.addCommandOutput(devMachine.getId(), console);
        service.getStatus(devMachine, appContext.getRootProject().getLocation())
               .then(new Operation<Status>() {
                   @Override
                   public void apply(Status status) throws OperationException {
                       if (containsInSelected(status.getUntracked())) {
                           presenter.showDialog();
                       } else if (containsInSelected(status.getModified())) {
                           addToIndex(console);
                       } else {
                           String message = resources.length > 1 ? constant.nothingAddToIndexMultiSelect() : constant.nothingAddToIndex();
                           console.print(message);
                           notificationManager.notify(message);
                       }
                   }
               })
               .catchError(new Operation<PromiseError>() {
                   @Override
                   public void apply(PromiseError error) throws OperationException {
                       console.printError(constant.statusFailed());
                       notificationManager.notify(constant.statusFailed(), FAIL, FLOAT_MODE);
                   }
               });
    }

    private void addToIndex(final GitOutputConsole console) {
        Resource[] resources = appContext.getResources();
        Path[] paths = new Path[resources.length];
        for (int i = 0; i < resources.length; i++) {
            Path path = resources[i].getLocation().removeFirstSegments(1);
            paths[i] = path.segmentCount() == 0 ? Path.EMPTY : path;
        }
        service.add(appContext.getDevMachine(), appContext.getRootProject().getLocation(), false, paths)
               .then(new Operation<Void>() {
                   @Override
                   public void apply(Void arg) throws OperationException {
                       console.print(constant.addSuccess());
                       notificationManager.notify(constant.addSuccess());
                   }
               })
               .catchError(new Operation<PromiseError>() {
                   @Override
                   public void apply(PromiseError arg) throws OperationException {
                       console.printError(constant.addFailed());
                       notificationManager.notify(constant.addFailed(), FAIL, FLOAT_MODE);
                   }
               });
    }

    private boolean containsInSelected(List<String> items) {
        for (String item : items) {
            for (Resource selectedItem : appContext.getResources()) {
                String selectedItemPath = selectedItem.getLocation()
                                                      .removeFirstSegments(appContext.getRootProject().getLocation().segmentCount())
                                                      .toString();
                if (item.startsWith(selectedItemPath)) {
                    return true;
                }
            }
        }
        return false;
    }
}
