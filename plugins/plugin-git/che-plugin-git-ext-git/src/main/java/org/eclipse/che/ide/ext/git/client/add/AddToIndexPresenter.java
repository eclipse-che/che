/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ext.git.client.add;

import static com.google.common.base.Preconditions.checkState;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.GitServiceClient;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsole;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsoleFactory;
import org.eclipse.che.ide.processes.panel.ProcessesPanelPresenter;
import org.eclipse.che.ide.resource.Path;

/**
 * Presenter for add changes to Git index.
 *
 * @author Ann Zhuleva
 * @author Vlad Zhukovskyi
 * @author Igor Vinokur
 */
@Singleton
public class AddToIndexPresenter implements AddToIndexView.ActionDelegate {

  private final AddToIndexView view;
  private final GitServiceClient service;
  private final GitLocalizationConstant constant;
  private final AppContext appContext;
  private final NotificationManager notificationManager;
  private final GitOutputConsoleFactory gitOutputConsoleFactory;
  private final ProcessesPanelPresenter consolesPanelPresenter;

  @Inject
  public AddToIndexPresenter(
      AddToIndexView view,
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
    Resource[] resources = appContext.getResources();
    checkState(resources != null && resources.length > 0);
    if (resources.length == 1) {
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

  @Override
  public void onAddClicked() {
    Resource[] resources = appContext.getResources();
    Path projectLocation = appContext.getRootProject().getLocation();
    Path[] paths = new Path[resources.length];
    for (int i = 0; i < resources.length; i++) {
      Path path = resources[i].getLocation().removeFirstSegments(projectLocation.segmentCount());
      paths[i] = path.segmentCount() == 0 ? Path.EMPTY : path;
    }
    final GitOutputConsole console =
        gitOutputConsoleFactory.create(constant.addToIndexCommandName());
    consolesPanelPresenter.addCommandOutput(console);
    service
        .add(projectLocation, view.isUpdated(), paths)
        .then(
            arg -> {
              console.print(constant.addSuccess());
              notificationManager.notify(constant.addSuccess());
              view.close();
            })
        .catchError(
            arg -> {
              console.printError(constant.addFailed());
              notificationManager.notify(constant.addFailed(), FAIL, FLOAT_MODE);
              view.close();
            });
  }

  @Override
  public void onCancelClicked() {
    view.close();
  }
}
