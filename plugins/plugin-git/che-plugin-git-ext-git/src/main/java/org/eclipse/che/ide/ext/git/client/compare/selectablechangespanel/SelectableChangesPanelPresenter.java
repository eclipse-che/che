/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ext.git.client.compare.selectablechangespanel;

import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.eclipse.che.ide.ext.git.client.compare.AlteredFiles;
import org.eclipse.che.ide.ext.git.client.compare.ComparePresenter;
import org.eclipse.che.ide.ext.git.client.compare.changespanel.ChangesPanelPresenter;
import org.eclipse.che.ide.ext.git.client.compare.selectablechangespanel.SelectableChangesPanelView.ActionDelegate;
import org.eclipse.che.ide.resource.Path;

/**
 * Presenter for displaying list of changed files with ability to select files.
 *
 * @author Igor Vinokur
 */
public class SelectableChangesPanelPresenter extends ChangesPanelPresenter
    implements ActionDelegate {

  private final SelectableChangesPanelView view;
  private List<String> selectedFiles;
  private List<String> allFiles;
  private SelectionCallBack selectionCallBack;

  @Inject
  public SelectableChangesPanelPresenter(
      SelectableChangesPanelView view, ComparePresenter comparePresenter) {
    super(view, comparePresenter);
    view.setDelegate((ActionDelegate) this);
    this.view = view;
    selectedFiles = new ArrayList<>();
  }

  public void show(AlteredFiles alteredFiles, SelectionCallBack selectionCallBack) {
    selectedFiles.clear();
    allFiles = alteredFiles.getAlteredFilesList();
    this.selectionCallBack = selectionCallBack;
    super.show(alteredFiles);
  }

  public void setMarkedCheckBoxes(Set<Path> paths) {
    view.setMarkedCheckBoxes(paths);
  }

  @Override
  public List<String> getSelectedFiles() {
    return selectedFiles;
  }

  @Override
  public List<String> getAllFiles() {
    return allFiles;
  }

  @Override
  public void refreshNodes() {
    view.refreshNodes();
  }

  @Override
  public void onFileNodeCheckBoxValueChanged(Path path, boolean isSelected) {
    if (isSelected) {
      selectedFiles.add(path.toString());
    } else {
      selectedFiles.remove(path.toString());
    }
    selectionCallBack.onSelectionChanged(path, isSelected);
  }
}
