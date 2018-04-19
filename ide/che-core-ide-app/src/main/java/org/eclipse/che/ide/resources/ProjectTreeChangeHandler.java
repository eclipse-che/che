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
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static org.eclipse.che.api.project.shared.dto.event.FileWatcherEventType.CREATED;
import static org.eclipse.che.api.project.shared.dto.event.FileWatcherEventType.DELETED;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.SUCCESS;
import static org.eclipse.che.ide.api.resources.ResourceDelta.ADDED;
import static org.eclipse.che.ide.api.resources.ResourceDelta.REMOVED;
import static org.eclipse.che.ide.api.resources.ResourceDelta.UPDATED;
import static org.eclipse.che.ide.resource.Path.commonPath;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.api.project.shared.FileChange;
import org.eclipse.che.api.project.shared.dto.event.FileWatcherEventType;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.DelayedTask;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.ExternalResourceDelta;
import org.eclipse.che.ide.api.resources.ResourceDelta;
import org.eclipse.che.ide.resource.Path;

/**
 * Consumes input {@link FileChange} changes and collects into temporary buffer, which is processed
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
  private final NotificationManager notificationManager;
  private final CoreLocalizationConstant constants;
  private final List<FileChange> workingQueue;
  private final FileChangesDeferredScheduler changesScheduler;
  private static final int UPDATE_PERIOD_MS = 500;

  @Inject
  public ProjectTreeChangeHandler(
      AppContext appContext,
      PromiseProvider promises,
      NotificationManager notificationManager,
      CoreLocalizationConstant constants) {
    this.appContext = appContext;
    this.promises = promises;
    this.notificationManager = notificationManager;
    this.constants = constants;

    workingQueue = new ArrayList<>();
    changesScheduler = this.new FileChangesDeferredScheduler();
  }

  public void handleFileChange(FileChange fileChange) {
    if (fileChange == null) {
      throw new NullPointerException("File change can not be null");
    }

    if (workingQueue.contains(fileChange)) {
      return;
    }

    workingQueue.add(fileChange);
    changesScheduler.delay(UPDATE_PERIOD_MS);
  }

  private class FileChangesDeferredScheduler extends DelayedTask {

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

      chainPromise =
          chainPromise
              .thenPromise(
                  ignored -> {
                    if (localCopy.size() == 1) {
                      return doProcessSingleChange(localCopy.get(0));
                    } else {
                      return doProcessMultipleChanges(localCopy);
                    }
                  })
              .catchErrorPromise(this::onCatchErrorPromise);
    }

    private Promise<Void> doProcessMultipleChanges(List<FileChange> changes) {
      Promise<Void> multipleChainPromise = ProjectTreeChangeHandler.this.promises.resolve(null);

      multipleChainPromise =
          multipleChainPromise.thenPromise(ignored -> doProcessDeleteChanges(changes));

      multipleChainPromise =
          multipleChainPromise.thenPromise(ignored -> doProcessUpdateChanges(changes));

      return multipleChainPromise;
    }

    private Promise<Void> doProcessDeleteChanges(List<FileChange> changes) {
      List<FileChange> deleteChanges = getDeleteFileChanges(changes);

      if (deleteChanges.isEmpty()) {
        return ProjectTreeChangeHandler.this.promises.resolve(null);
      }

      List<ResourceDelta> deleteResourceDeltas = getDeleteResourceDeltas(deleteChanges);

      return synchronizeChanges(deleteResourceDeltas);
    }

    private Promise<Void> doProcessUpdateChanges(List<FileChange> changes) {
      List<FileChange> updateChanges = getUpdateFileChanges(changes);

      if (updateChanges.isEmpty()) {
        return ProjectTreeChangeHandler.this.promises.resolve(null);
      }

      List<Path> updatePaths = getUpdateFileChangePaths(updateChanges);
      Path commonUpdatePath = getCommonUpdatePath(updatePaths);

      Promise<Optional<Container>> containerByPath = getContainerByPath(commonUpdatePath);

      return containerByPath.thenPromise(
          container -> {
            if (container.isPresent()) {
              return synchronizeChanges(container.get());
            } else {
              return ProjectTreeChangeHandler.this.promises.resolve(null);
            }
          });
    }

    private Promise<Optional<Container>> getContainerByPath(Path path) {
      return ProjectTreeChangeHandler.this.appContext.getWorkspaceRoot().getContainer(path);
    }

    private List<ResourceDelta> getDeleteResourceDeltas(List<FileChange> changes) {
      return changes.stream().map(this::getDeleteResourceDelta).collect(toList());
    }

    private int getStatus(FileWatcherEventType eventType) {
      if (eventType == CREATED) {
        return ADDED;
      } else if (eventType == DELETED) {
        return REMOVED;
      } else {
        return UPDATED;
      }
    }

    private ResourceDelta getDeleteResourceDelta(FileChange change) {
      Path removedPath = Path.valueOf(change.getPath());

      return new ExternalResourceDelta(removedPath, removedPath, REMOVED);
    }

    private boolean isDeleteChange(FileChange change) {
      return change.getType() == DELETED;
    }

    private boolean isNotDeleteChange(FileChange change) {
      return change.getType() != DELETED;
    }

    private List<FileChange> getDeleteFileChanges(List<FileChange> changes) {
      return changes.stream().filter(this::isDeleteChange).collect(toList());
    }

    private List<FileChange> getUpdateFileChanges(List<FileChange> changes) {
      return changes.stream().filter(this::isNotDeleteChange).collect(toList());
    }

    private List<Path> getUpdateFileChangePaths(List<FileChange> changes) {
      return changes.stream().map(FileChange::getPath).map(Path::valueOf).collect(toList());
    }

    private Path getCommonUpdatePath(List<Path> paths) {
      return commonPath(paths.toArray(new Path[paths.size()]));
    }

    private Promise<Void> onCatchErrorPromise(PromiseError ignored) {
      return ProjectTreeChangeHandler.this.promises.resolve(null);
    }

    private Promise<Void> resolvePromise(Object ignored) {
      return ProjectTreeChangeHandler.this.promises.resolve(null);
    }

    private Promise<Void> synchronizeChanges(List<ResourceDelta> deltas) {
      return synchronizeChanges(deltas.toArray(new ResourceDelta[deltas.size()]));
    }

    private Promise<Void> synchronizeChanges(ResourceDelta... deltas) {
      Container workspaceRoot = ProjectTreeChangeHandler.this.appContext.getWorkspaceRoot();

      return synchronizeChanges(workspaceRoot, deltas);
    }

    private Promise<Void> synchronizeChanges(Container container, ResourceDelta... deltas) {
      Promise<?> promise;

      if (deltas == null || deltas.length == 0) {
        promise = container.synchronize();
      } else {
        stream(deltas).filter(this::isProjectCreatedDelta).forEach(this::notifyProjectCreated);
        promise = container.synchronize(deltas);
      }

      return promise.thenPromise(this::resolvePromise);
    }

    private ResourceDelta getResourceDelta(FileChange change) {
      Path newPath = Path.valueOf(change.getPath());
      Path oldPath = Path.valueOf(change.getPath());
      int status = getStatus(change.getType());

      return new ExternalResourceDelta(newPath, oldPath, status);
    }

    private Promise<Void> doProcessSingleChange(FileChange change) {
      return isNullOrEmpty(change.getPath())
          ? synchronizeChanges()
          : synchronizeChanges(getResourceDelta(change));
    }

    private boolean isProjectCreatedDelta(ResourceDelta delta) {
      return delta.getKind() == ADDED && delta.getToPath().segmentCount() == 1;
    }

    private void notifyProjectCreated(ResourceDelta delta) {
      String message =
          ProjectTreeChangeHandler.this.constants.projectCreated(delta.getToPath().segment(0));
      ProjectTreeChangeHandler.this.notificationManager.notify(message, SUCCESS, FLOAT_MODE);
    }
  }
}
