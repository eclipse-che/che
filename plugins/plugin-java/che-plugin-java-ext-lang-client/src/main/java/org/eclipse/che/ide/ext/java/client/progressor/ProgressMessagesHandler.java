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
import org.eclipse.che.ide.ext.java.shared.dto.progressor.ProgressReportDto;
import org.eclipse.che.ide.util.Pair;

/**
 * Handler which receives messages from the jdt.ls server.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class ProgressMessagesHandler {
  private static final int DELAY_MS = 20_000; // wait before check if task was update
  private final EventBus eventBus;
  private final BackgroundLoaderPresenter progressResolver;
  private Map<String, Pair<ProgressReportDto, Long>> progresses = new LinkedHashMap<>();

  private ProgressReportDto currentProgress;

  @Inject
  public ProgressMessagesHandler(
      EventBus eventBus,
      ProgressorJsonRpcHandler progressorJsonRpcHandler,
      BackgroundLoaderPresenter progressResolver) {
    this.eventBus = eventBus;
    this.progressResolver = progressResolver;

    progressorJsonRpcHandler.addProgressReportHandler(this::handleProgressNotification);
    handleOperations();
  }

  private void handleOperations() {
    eventBus.addHandler(WorkspaceStoppedEvent.TYPE, event -> progressResolver.hide());
  }

  private void handleProgressNotification(ProgressReportDto progress) {
    if (isNullOrEmpty(progress.getId())) {
      return;
    }
    String taskId = progress.getId();

    if (taskFinished(progress)) {
      progresses.remove(taskId);
      progressResolver.updateProgressBar(progress);
      progressResolver.removeProgress(progress);
      updateCurrentTask();
      return;
    }

    if (progress.isComplete()) {
      return;
    }

    if (progresses.isEmpty()) {
      addFirstProgress(progress);
    }

    progresses.put(taskId, Pair.of(progress, currentTimeMillis()));
    progressResolver.addProgress(progress);

    if (currentProgress == null) {
      currentProgress = progress;
    }
    String currentTaskId = currentProgress.getId();

    if (taskId.equals(currentTaskId)) {
      currentProgress = progress;
      progressResolver.updateProgressBar(currentProgress);
    }
    progressResolver.show();
  }

  private void startProcessesCleaner() {
    Scheduler.get()
        .scheduleFixedDelay(
            () -> {
              for (Iterator<Map.Entry<String, Pair<ProgressReportDto, Long>>> it =
                      progresses.entrySet().iterator();
                  it.hasNext(); ) {
                Map.Entry<String, Pair<ProgressReportDto, Long>> entry = it.next();
                long lastUpdate = entry.getValue().getSecond();
                ProgressReportDto progress = entry.getValue().getFirst();
                if ((currentTimeMillis() - lastUpdate) > DELAY_MS) {
                  it.remove();
                  progressResolver.removeProgress(progress);
                  String currentTaskId = currentProgress.getId();
                  String taskId = progress.getId();
                  if (currentTaskId.equals(taskId)) {
                    updateCurrentTask();
                  }
                }
              }
              return !progresses.isEmpty();
            },
            DELAY_MS);
  }

  private void updateCurrentTask() {
    if (!progresses.isEmpty()) {
      currentProgress = progresses.values().iterator().next().getFirst();
      progressResolver.updateProgressBar(currentProgress);
    } else {
      progressResolver.hide();
    }
  }

  private boolean taskFinished(ProgressReportDto progress) {
    return progresses.containsKey(progress.getId())
        && (progress.isComplete() || progress.getTotalWork() == progress.getWorkDone());
  }

  private void addFirstProgress(ProgressReportDto progress) {
    currentProgress = progress;
    progresses.put(currentProgress.getId(), Pair.of(progress, currentTimeMillis()));
    startProcessesCleaner();
  }
}
