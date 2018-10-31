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
package org.eclipse.che.ide.ext.java.client.progressor.background;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.ext.java.client.progressor.ProgressMonitorPresenter;
import org.eclipse.che.jdt.ls.extension.api.dto.ProgressReport;

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
  public void updateProgressBar(ProgressReport progress) {
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
  public void addProgress(ProgressReport progress) {
    progressMonitorPresenter.addProgress(progress);
  }

  /**
   * Removes the task from the progress monitor.
   *
   * @param progress information about the task
   */
  public void removeProgress(ProgressReport progress) {
    progressMonitorPresenter.removeProgress(progress);
  }
}
