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
package org.eclipse.che.ide.ext.java.client.progressor.background;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.ext.java.client.progressor.ProgressMonitorPresenter;
import org.eclipse.che.ide.ext.java.shared.dto.progressor.ProgressReportDto;

/**
 * Loader for displaying information about resolving task from the jdt.ls server.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class BackgroundLoaderPresenter implements BackgroundLoaderView.ActionDelegate {
  private final ProgressMonitorPresenter progressMonitorPresenter;
  private final BackgroundLoaderView view;

  @Inject
  public BackgroundLoaderPresenter(
      ProgressMonitorPresenter progressMonitorPresenter, BackgroundLoaderView view) {
    this.progressMonitorPresenter = progressMonitorPresenter;
    this.view = view;
    this.view.setDelegate(this);
  }

  /** @return custom Widget that represents the loader's action in UI. */
  Widget getCustomComponent() {
    return view.asWidget();
  }

  /** Hide the loader. */
  public void hide() {
    view.hide();
    progressMonitorPresenter.hide();
  }

  /** Show the loader. */
  public void show() {
    view.show();
  }

  /** Change the value of resolved modules of the project. */
  public void updateProgressBar(ProgressReportDto progress) {
    view.setOperationLabel(progress.getTask());
    int totalWork = progress.getTotalWork();
    if (totalWork > 0) {
      double percent = ((double) progress.getWorkDone() / totalWork);
      view.updateProgressBar(percent * 100);
    }
    progressMonitorPresenter.updateProgress(progress);
  }

  @Override
  public void showResolverInfo() {
    progressMonitorPresenter.show();
  }

  /**
   * Adds new running task to the progress monitor.
   *
   * @param progress information about the task
   */
  public void addProgress(ProgressReportDto progress) {
    progressMonitorPresenter.addProgress(progress);
  }

  /**
   * Removes the task from the progress monitor.
   *
   * @param progress information about the task
   */
  public void removeProgress(ProgressReportDto progress) {
    progressMonitorPresenter.removeProgress(progress);
  }
}
