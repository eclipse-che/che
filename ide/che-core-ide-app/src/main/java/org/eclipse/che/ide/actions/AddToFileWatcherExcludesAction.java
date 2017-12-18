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
package org.eclipse.che.ide.actions;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toSet;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.part.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.filewatcher.FileWatcherExcludesOperation;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.Resource;

/**
 * Adds resources which are in application context to File Watcher excludes.
 *
 * @author Roman Nikitenko
 */
@Singleton
public class AddToFileWatcherExcludesAction extends AbstractPerspectiveAction {

  private AppContext appContext;
  private NotificationManager notificationManager;
  private FileWatcherExcludesOperation fileWatcherExcludesOperation;

  @Inject
  public AddToFileWatcherExcludesAction(
      AppContext appContext,
      CoreLocalizationConstant locale,
      NotificationManager notificationManager,
      FileWatcherExcludesOperation fileWatcherExcludesOperation) {
    super(
        singletonList(PROJECT_PERSPECTIVE_ID),
        locale.addToFileWatcherExludesName(),
        locale.addToFileWatcherExludesDescription());
    this.appContext = appContext;
    this.notificationManager = notificationManager;
    this.fileWatcherExcludesOperation = fileWatcherExcludesOperation;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    final Resource[] resources = appContext.getResources();
    Set<String> pathsToExclude =
        stream(resources).map(resource -> resource.getLocation().toString()).collect(toSet());

    fileWatcherExcludesOperation
        .addToFileWatcherExcludes(pathsToExclude)
        .catchError(
            error -> {
              notificationManager.notify(error.getMessage(), FAIL, EMERGE_MODE);
            });
  }

  @Override
  public void updateInPerspective(@NotNull ActionEvent e) {
    Resource[] resources = appContext.getResources();

    e.getPresentation().setVisible(true);
    e.getPresentation().setEnabled(containsResourcesToExcludes(resources));
  }

  private boolean containsResourcesToExcludes(Resource[] resources) {
    if (resources == null || resources.length <= 0) {
      return false;
    }

    List<Resource> resourcesToExclude = asList(resources);
    return resourcesToExclude
        .stream()
        .map(resource -> resource.getLocation().toString())
        .anyMatch(pathToExclude -> !fileWatcherExcludesOperation.isExcluded(pathToExclude));
  }
}
