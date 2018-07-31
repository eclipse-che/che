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

import java.util.List;
import java.util.Set;
import org.eclipse.che.ide.ext.git.client.compare.changespanel.ChangesPanelView;
import org.eclipse.che.ide.resource.Path;

/**
 * The view of {@link SelectableChangesPanelPresenter}.
 *
 * @author Igor Vinokur
 */
public interface SelectableChangesPanelView extends ChangesPanelView {

  /** Needs for delegating actions into {@link SelectableChangesPanelPresenter}. */
  interface ActionDelegate {

    /** Refresh all nodes in the panel. */
    void refreshNodes();

    /** Is called when item check-box changed selection state. */
    void onFileNodeCheckBoxValueChanged(Path path, boolean newCheckBoxValue);

    /** Get list of all file paths. */
    List<String> getSelectedFiles();

    /** Get list of selected file paths. */
    List<String> getAllFiles();
  }

  /** Set implemented actions to {@link SelectableChangesPanelViewImpl} */
  void setDelegate(SelectableChangesPanelView.ActionDelegate delegate);

  /**
   * Set check-boxes state of given paths as checked.
   *
   * @param paths paths of nodes
   */
  void setMarkedCheckBoxes(Set<Path> paths);
}
