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
package org.eclipse.che.ide.part.editor.recent;

import static com.google.common.collect.Lists.newArrayListWithCapacity;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.resources.tree.FileNode;
import org.eclipse.che.ide.resources.tree.ResourceNode;
import org.eclipse.che.ide.ui.smartTree.data.settings.SettingsProvider;

/**
 * Presenter for showing recently opened files.
 *
 * @author Vlad Zhukovskiy
 */
@Singleton
public class OpenRecentFilesPresenter implements OpenRecentFilesView.ActionDelegate {

  private final OpenRecentFilesView view;
  private final ResourceNode.NodeFactory nodeFactory;
  private final SettingsProvider settingsProvider;

  @Inject
  public OpenRecentFilesPresenter(
      OpenRecentFilesView view,
      ResourceNode.NodeFactory nodeFactory,
      SettingsProvider settingsProvider) {
    this.view = view;
    this.nodeFactory = nodeFactory;
    this.settingsProvider = settingsProvider;

    view.setDelegate(this);
  }

  /** Show dialog. */
  public void show() {
    view.show();
  }

  /**
   * Set recent file list.
   *
   * @param recentFiles recent file list
   */
  public void setRecentFiles(List<File> recentFiles) {
    final List<FileNode> nodes = newArrayListWithCapacity(recentFiles.size());

    for (File recentFile : recentFiles) {
      nodes.add(nodeFactory.newFileNode(recentFile, settingsProvider.getSettings()));
    }

    view.setRecentFiles(nodes);
  }

  /** Clear recent file list. */
  public void clearRecentFiles() {
    view.clearRecentFiles();
  }
}
