/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ext.java.client.progressor;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.System.currentTimeMillis;

import com.google.gwt.core.client.Scheduler;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStoppedEvent;
import org.eclipse.che.ide.ext.java.client.progressor.background.BackgroundLoaderPresenter;
import org.eclipse.che.ide.util.Pair;
import org.eclipse.che.jdt.ls.extension.api.dto.ProgressReport;

/**
 * Handler which receives messages from the jdt.ls server.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class ProgressMessagesHandler {
  private static final int DELAY_MS = 20_000; // wait before check if task was update
  private final EventBus eventBus;
  private final BackgroundLoaderPresenter backgroundLoader;
  private Map<String, Pair<ProgressReport, Long>> progresses = new LinkedHashMap<>();

  private ProgressReport currentProgress;

  @Inject
  public ProgressMessagesHandler(
      EventBus eventBus,
      ProgressorJsonRpcHandler progressorJsonRpcHandler,
      BackgroundLoaderPresenter backgroundLoader) {
    this.eventBus = eventBus;
    this.backgroundLoader = backgroundLoader;

    progressorJsonRpcHandler.addProgressReportHandler(this::handleProgressNotification);
    handleOperations();
  }

  private void handleOperations() {
    eventBus.addHandler(WorkspaceStoppedEvent.TYPE, event -> backgroundLoader.hide());
  }

  private void handleProgressNotification(ProgressReport progress) {
    if (isNullOrEmpty(progress.getId())) {
      return;
    }
    String progressId = progress.getId();

    if (progressFinished(progress)) {
      progresses.remove(progressId);
      backgroundLoader.updateProgressBar(progress);
      backgroundLoader.removeProgress(progress);
      updateCurrentProgress();
      return;
    }

    if (progress.isComplete()) {
      return;
    }

    if (progresses.isEmpty()) {
      addFirstProgress(progress);
    }

    progresses.put(progressId, Pair.of(progress, currentTimeMillis()));
    backgroundLoader.addProgress(progress);

    if (currentProgress == null) {
      currentProgress = progress;
    }
    String currentTaskId = currentProgress.getId();

    if (progressId.equals(currentTaskId)) {
      currentProgress = progress;
      backgroundLoader.updateProgressBar(currentProgress);
    }
    backgroundLoader.show();
  }

  private void startProcessesCleaner() {
    Scheduler.get()
        .scheduleFixedDelay(
            () -> {
              for (Iterator<Map.Entry<String, Pair<ProgressReport, Long>>> it =
                      progresses.entrySet().iterator();
                  it.hasNext(); ) {
                Map.Entry<String, Pair<ProgressReport, Long>> entry = it.next();
                long lastUpdate = entry.getValue().getSecond();
                ProgressReport progress = entry.getValue().getFirst();
                if ((currentTimeMillis() - lastUpdate) > DELAY_MS) {
                  it.remove();
                  backgroundLoader.removeProgress(progress);
                  String currentTaskId = currentProgress.getId();
                  String taskId = progress.getId();
                  if (currentTaskId.equals(taskId)) {
                    updateCurrentProgress();
                  }
                }
              }
              return !progresses.isEmpty();
            },
            DELAY_MS);
  }

  private void updateCurrentProgress() {
    if (!progresses.isEmpty()) {
      currentProgress = progresses.values().iterator().next().getFirst();
      backgroundLoader.updateProgressBar(currentProgress);
    } else {
      backgroundLoader.hide();
    }
  }

  private boolean progressFinished(ProgressReport progress) {
    return progresses.containsKey(progress.getId())
        && (progress.isComplete() || progress.getTotalWork() == progress.getWorkDone());
  }

  private void addFirstProgress(ProgressReport progress) {
    currentProgress = progress;
    progresses.put(currentProgress.getId(), Pair.of(progress, currentTimeMillis()));
    startProcessesCleaner();
  }
}
