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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.ide.ext.java.client.inject.factories.ProgressWidgetFactory;
import org.eclipse.che.ide.ext.java.shared.dto.progressor.ProgressReportDto;

/**
 * Presenter of the window which describes information about all running tasks.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class ProgressMonitorPresenter {
  private final ProgressMonitorView view;
  private final ProgressWidgetFactory progressFactory;

  private Map<String, ProgressView> progresses = new HashMap<>();

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
  public void updateProgress(ProgressReportDto progress) {
    String taskId = progress.getId();
    if (!progresses.containsKey(taskId)) {
      return;
    }
    ProgressView progressView = progresses.get(taskId);
    if (progress.isComplete()) {
      view.remove(progressView);
      progresses.remove(taskId);
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
  public void addProgress(ProgressReportDto progress) {
    String taskId = progress.getId();
    if (progresses.containsKey(taskId)) {
      updateProgress(progress);
      return;
    }
    ProgressView progressView = progressFactory.create();
    progressView.updateProgressBar(progress);
    progresses.put(taskId, progressView);
    view.add(progressView);
  }

  /**
   * Removes progress.
   *
   * @param progress information about progress
   */
  public void removeProgress(ProgressReportDto progress) {
    String taskId = progress.getId();
    if (!progresses.containsKey(taskId)) {
      return;
    }
    view.remove(progresses.remove(taskId));
  }
}
