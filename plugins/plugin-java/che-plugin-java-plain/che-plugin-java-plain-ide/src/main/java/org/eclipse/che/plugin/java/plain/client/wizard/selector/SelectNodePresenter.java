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
package org.eclipse.che.plugin.java.plain.client.wizard.selector;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Collections;
import java.util.List;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.resources.tree.ResourceNode;
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.eclipse.che.ide.ui.smartTree.data.settings.SettingsProvider;

/**
 * Presenter for choosing source directory.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class SelectNodePresenter implements SelectNodeView.ActionDelegate {

  private final SelectNodeView view;
  private final AppContext appContext;
  private final ResourceNode.NodeFactory nodeFactory;
  private final SettingsProvider settingsProvider;
  private SelectionDelegate selectionDelegate;

  @Inject
  public SelectNodePresenter(
      SelectNodeView view,
      AppContext appContext,
      ResourceNode.NodeFactory nodeFactory,
      SettingsProvider settingsProvider) {
    this.view = view;
    this.appContext = appContext;
    this.nodeFactory = nodeFactory;
    this.settingsProvider = settingsProvider;
    this.view.setDelegate(this);
  }

  /**
   * Show tree of the project.
   *
   * @param projectName
   */
  public void show(SelectionDelegate selectionDelegate, String projectName) {
    this.selectionDelegate = selectionDelegate;

    appContext
        .getWorkspaceRoot()
        .getContainer(projectName)
        .then(
            new Operation<Optional<Container>>() {
              @Override
              public void apply(Optional<Container> container) throws OperationException {
                if (container.isPresent()) {
                  view.setStructure(
                      Collections.<Node>singletonList(
                          nodeFactory.newContainerNode(
                              container.get(), settingsProvider.getSettings())));

                  view.showDialog();
                }
              }
            });
  }

  /** {@inheritDoc} */
  @Override
  public void setSelectedNode(List<Node> selectedNodes) {
    selectionDelegate.onNodeSelected(selectedNodes);
  }
}
