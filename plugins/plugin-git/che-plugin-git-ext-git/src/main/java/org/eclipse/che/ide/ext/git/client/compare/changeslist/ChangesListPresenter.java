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
package org.eclipse.che.ide.ext.git.client.compare.changeslist;

import static com.google.common.collect.Iterables.getFirst;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.ext.git.client.compare.AlteredFiles;
import org.eclipse.che.ide.ext.git.client.compare.ComparePresenter;
import org.eclipse.che.ide.ext.git.client.compare.changespanel.ChangedFolderNode;
import org.eclipse.che.ide.ext.git.client.compare.changespanel.ChangesPanelPresenter;
import org.eclipse.che.ide.ext.git.client.compare.changespanel.ChangesPanelView;
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.eclipse.che.ide.ui.smartTree.event.SelectionChangedEvent.SelectionChangedHandler;

/**
 * Presenter for displaying window with list of changed files.
 *
 * @author Igor Vinokur
 * @author Vlad Zhukovskyi
 * @author Mykola Morhun
 */
@Singleton
public class ChangesListPresenter implements ChangesListView.ActionDelegate {
  private final ChangesListView view;
  private final ChangesPanelPresenter changesPanelPresenter;
  private final ComparePresenter comparePresenter;

  private AlteredFiles alteredFiles;
  private String file;
  private String revisionA;
  private String revisionB;

  @Inject
  public ChangesListPresenter(
      ChangesListView view,
      ComparePresenter comparePresenter,
      ChangesPanelPresenter changesPanelPresenter) {
    this.comparePresenter = comparePresenter;
    this.view = view;

    this.changesPanelPresenter = changesPanelPresenter;
    this.changesPanelPresenter.setFileNodeDoubleClickHandler(
        (path, status) -> this.onCompareClicked());
    this.view.setDelegate(this);

    SelectionChangedHandler handler =
        event -> {
          Node node = getFirst(event.getSelection(), null);
          if (node == null) {
            return;
          }
          if (node instanceof ChangedFolderNode) {
            ChangesListPresenter.this.view.setEnableCompareButton(false);
            return;
          }
          ChangesListPresenter.this.view.setEnableCompareButton(true);
          ChangesListPresenter.this.file = node.getName();
        };

    ChangesPanelView changesPanelView = changesPanelPresenter.getView();
    changesPanelView.addSelectionHandler(handler);
    this.view.setChangesPanelView(changesPanelView);
  }

  /**
   * Shows window with changed files.
   *
   * @param alteredFiles files and their status
   * @param revisionA hash of the first revision or branch. If it is set to {@code null}, compare
   *     with empty repository state will be performed
   * @param revisionB hash of the second revision or branch. If it is set to {@code null}, compare
   *     with latest repository state will be performed
   */
  public void show(
      AlteredFiles alteredFiles, @Nullable String revisionA, @Nullable String revisionB) {
    this.alteredFiles = alteredFiles;
    this.revisionA = revisionA;
    this.revisionB = revisionB;

    view.setEnableCompareButton(false);
    view.showDialog();

    changesPanelPresenter.show(alteredFiles);
  }

  @Override
  public void onCloseClicked() {
    view.close();
  }

  @Override
  public void onCompareClicked() {
    if (revisionB == null) {

      comparePresenter.showCompareWithLatest(alteredFiles, file, revisionA);
    } else {
      comparePresenter.showCompareBetweenRevisions(alteredFiles, file, revisionA, revisionB);
    }
  }
}
