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

import static java.lang.System.currentTimeMillis;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.ide.ext.java.client.inject.factories.ProgressWidgetFactory;
import org.eclipse.che.ide.util.Pair;
import org.eclipse.che.jdt.ls.extension.api.dto.ProgressReport;

/**
 * Presenter of the window which describes information about all running tasks.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class ProgressMonitorPresenter {
  private static final long UPDATE_PERIOD = 1_000L; // don't update more often then 1 sec

  private final ProgressMonitorView view;
  private final ProgressWidgetFactory progressFactory;

  private Map<String, Pair<ProgressView, Long>> progresses = new HashMap<>();

  @Inject
  public ProgressMonitorPresenter(ProgressMonitorView view, ProgressWidgetFactory progressFactory) {
    this.view = view;
    this.progressFactory = progressFactory;
  }

  /** Shows the widget. */
  public void show() {
    view.showDialog();
  }

  /**
   * Updates progress for one task.
   *
   * @param progress updated progress
   */
  public void updateProgress(ProgressReport progress) {
    String taskId = progress.getId();
    if (!progresses.containsKey(taskId)) {
      return;
    }
    Pair<ProgressView, Long> updatedView = progresses.get(taskId);
    ProgressView progressView = updatedView.getFirst();
    if (progress.isComplete()) {
      view.remove(progressView);
      progresses.remove(taskId);
      return;
    }
    Long updated = updatedView.getSecond();
    if (currentTimeMillis() - updated < UPDATE_PERIOD) {
      return;
    }
    progressView.updateProgressBar(progress);
  }

  /** Hides the widget. */
  public void hide() {
    view.close();
  }

  /**
   * Adds new progress.
   *
   * @param progress information about progress
   */
  public void addProgress(ProgressReport progress) {
    String taskId = progress.getId();
    if (progresses.containsKey(taskId)) {
      updateProgress(progress);
      return;
    }
    ProgressView progressView = progressFactory.create();
    progressView.updateProgressBar(progress);
    progresses.put(taskId, Pair.of(progressView, currentTimeMillis()));
    view.add(progressView);
  }

  /**
   * Removes progress.
   *
   * @param progress information about progress
   */
  public void removeProgress(ProgressReport progress) {
    String taskId = progress.getId();
    if (!progresses.containsKey(taskId)) {
      return;
    }
    view.remove(progresses.remove(taskId).getFirst());
  }
}
