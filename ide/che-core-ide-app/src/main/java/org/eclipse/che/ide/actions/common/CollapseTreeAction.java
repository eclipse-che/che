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
package org.eclipse.che.ide.actions.common;

import static com.google.common.base.Preconditions.checkNotNull;

import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.BaseAction;
import org.eclipse.che.ide.ui.smartTree.data.TreeExpander;

/**
 * Base tree collapse action which consumes instance of {@link TreeExpander}.
 *
 * @author Vlad Zhukovskyi
 * @see TreeExpander
 * @since 5.0.0
 */
public abstract class CollapseTreeAction extends BaseAction {

  public abstract TreeExpander getTreeExpander();

  public CollapseTreeAction() {
    super("Collapse All");
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    final TreeExpander treeExpander = getTreeExpander();

    checkNotNull(treeExpander);

    if (!treeExpander.isCollapseEnabled()) {
      return;
    }

    treeExpander.collapseTree();
  }

  @Override
  public void update(ActionEvent e) {
    final TreeExpander treeExpander = getTreeExpander();

    e.getPresentation().setEnabledAndVisible(treeExpander.isCollapseEnabled());
  }
}
