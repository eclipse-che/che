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
