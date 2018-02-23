/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.resources;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.stream.Collectors.toList;
import static org.eclipse.che.ide.api.resources.ResourceDelta.ADDED;
import static org.eclipse.che.ide.api.resources.ResourceDelta.REMOVED;
import static org.eclipse.che.ide.api.resources.ResourceDelta.UPDATED;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.api.project.shared.FileChange;
import org.eclipse.che.api.project.shared.dto.event.FileWatcherEventType;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.ide.DelayedTask;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.resources.ExternalResourceDelta;
import org.eclipse.che.ide.resource.Path;

/**
 * Consumes input {@link FileChange} changes and collects into temporary buffer, which is processes
 * after 500 ms after last change consuming. Calculates common path to update folder and perform
 * file tree synchronization based on calculated rules.
 *
 * @author Vlad Zhukovskyi
 * @since 6.2.0
 * @see FileChange
 */
@Singleton
public class ProjectTreeChangeHandler {

  private final AppContext appContext;
  private final PromiseProvider promises;
  private final List<FileChange> workingQueue;
  private final ProjectTreeChangeHandler.FileChangeHandler changeHandler;
  private final int UPDATE_PERIOD_MS = 500;

  @Inject
  public ProjectTreeChangeHandler(AppContext appContext, PromiseProvider promises) {
    this.appContext = appContext;
    this.promises = promises;

    workingQueue = new ArrayList<>();
    changeHandler = this.new FileChangeHandler();
  }

  public void handleFileChange(FileChange fileChange) {
    if (fileChange == null) {
      throw new NullPointerException("File change can not be null");
    }

    if (workingQueue.contains(fileChange)) {
      return;
    }

    workingQueue.add(fileChange);
    changeHandler.delay(UPDATE_PERIOD_MS);
  }

  private class FileChangeHandler extends DelayedTask {

    Promise<Void> chainPromise = ProjectTreeChangeHandler.this.promises.resolve(null);

    @Override
    public void onExecute() {
      if (ProjectTreeChangeHandler.this.workingQueue.isEmpty()) {
        return;
      }

      List<FileChange> localCopy =
          new ArrayList<>(ProjectTreeChangeHandler.this.workingQueue.size());
      localCopy.addAll(ProjectTreeChangeHandler.this.workingQueue);
      ProjectTreeChangeHandler.this.workingQueue.clear();

      if (localCopy.size() == 1) {
        processSingleChange(localCopy.get(0));
      } else {
        processMultipleChanges(localCopy);
      }
    }

    private void processSingleChange(FileChange fileChange) {
      if (isNullOrEmpty(fileChange.getPath())) {
        chainPromise.thenPromise(
            arg -> ProjectTreeChangeHandler.this.appContext.getWorkspaceRoot().synchronize());
      } else {
        chainPromise.thenPromise(
            arg -> {
              Path newPath = Path.valueOf(fileChange.getPath());
              Path oldPath = Path.valueOf(fileChange.getPath());
              int status = getStatus(fileChange.getType());

              return ProjectTreeChangeHandler.this
                  .appContext
                  .getWorkspaceRoot()
                  .synchronize(new ExternalResourceDelta(newPath, oldPath, status));
            });
      }
    }

    private void processMultipleChanges(List<FileChange> fileChanges) {
      List<Path> paths =
          fileChanges.stream().map(FileChange::getPath).map(Path::valueOf).collect(toList());
      Path commonUpdatePath = Path.commonPath(paths.toArray(new Path[paths.size()]));

      chainPromise.thenPromise(
          arg ->
              ProjectTreeChangeHandler.this
                  .appContext
                  .getWorkspaceRoot()
                  .synchronize(
                      new ExternalResourceDelta(commonUpdatePath, commonUpdatePath, UPDATED)));
    }

    private int getStatus(FileWatcherEventType eventType) {
      switch (eventType) {
        case CREATED:
          {
            return ADDED;
          }
        case DELETED:
          {
            return REMOVED;
          }
        case MODIFIED:
          {
            return UPDATED;
          }
        default:
          {
            return UPDATED;
          }
      }
    }
  }
}
