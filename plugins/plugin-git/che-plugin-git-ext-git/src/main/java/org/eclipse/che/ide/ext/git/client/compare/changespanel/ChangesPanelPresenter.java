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
package org.eclipse.che.ide.ext.git.client.compare.changespanel;

import static org.eclipse.che.ide.ext.git.client.compare.changespanel.ViewMode.LIST;
import static org.eclipse.che.ide.ext.git.client.compare.changespanel.ViewMode.TREE;

import com.google.inject.Inject;
import org.eclipse.che.ide.ext.git.client.compare.AlteredFiles;
import org.eclipse.che.ide.ext.git.client.compare.ComparePresenter;
import org.eclipse.che.ide.ext.git.client.compare.FileStatus.Status;

/**
 * Presenter for displaying list of changed files.
 *
 * @author Igor Vinokur
 * @author Vlad Zhukovskyi
 */
public class ChangesPanelPresenter implements ChangesPanelView.ActionDelegate {

  private static final String REVISION = "HEAD";
  private final ChangesPanelView view;

  private AlteredFiles changedFiles;
  private ViewMode viewMode;

  private FileNodeDoubleClickHandler fileNodeDoubleClickHandler;

  @Inject
  public ChangesPanelPresenter(ChangesPanelView view, ComparePresenter comparePresenter) {
    this.view = view;
    this.view.setDelegate(this);
    this.viewMode = TREE;

    this.fileNodeDoubleClickHandler =
        (path, status) -> comparePresenter.showCompareWithLatest(changedFiles, path, REVISION);
  }

  /**
   * Show panel with changed files. If empty map with changed files is received, all buttons would
   * be disabled.
   *
   * @param changedFiles altered files to show
   */
  public void show(AlteredFiles changedFiles) {
    this.changedFiles = changedFiles;
    if (changedFiles.isEmpty()) {
      view.updateChangeViewModeButton(viewMode);
      view.setEnabledChangeViewModeButton(false);
      view.setEnableExpandCollapseButtons(false);
      view.resetPanelState();
    } else {
      view.setEnabledChangeViewModeButton(true);
      view.setEnableExpandCollapseButtons(viewMode == TREE);
      viewChangedFiles();
    }
  }

  public ChangesPanelView getView() {
    return view;
  }

  @Override
  public void onFileNodeDoubleClicked(String path, final Status status) {
    fileNodeDoubleClickHandler.onFileNodeDoubleClicked(path, status);
  }

  @Override
  public void onChangeViewModeButtonClicked() {
    viewMode = viewMode == TREE ? LIST : TREE;
    view.setEnableExpandCollapseButtons(viewMode == TREE);
    viewChangedFiles();
  }

  @Override
  public void onExpandButtonClicked() {
    view.expandAllDirectories();
  }

  @Override
  public void onCollapseButtonClicked() {
    view.collapseAllDirectories();
  }

  private void viewChangedFiles() {
    view.viewChangedFiles(changedFiles, viewMode);
    view.updateChangeViewModeButton(viewMode);
  }

  public void setFileNodeDoubleClickHandler(FileNodeDoubleClickHandler fileNodeDoubleClickHandler) {
    this.fileNodeDoubleClickHandler = fileNodeDoubleClickHandler;
  }

  /** Describes behaviour on double click action on a selected path. */
  public interface FileNodeDoubleClickHandler {
    void onFileNodeDoubleClicked(String path, final Status status);
  }
}
