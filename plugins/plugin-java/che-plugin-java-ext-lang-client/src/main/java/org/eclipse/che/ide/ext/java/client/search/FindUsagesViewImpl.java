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
package org.eclipse.che.ide.ext.java.client.search;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Collections;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.api.parts.base.BaseView;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.search.node.NodeFactory;
import org.eclipse.che.ide.ext.java.shared.dto.search.FindUsagesResponse;
import org.eclipse.che.ide.ui.smartTree.NodeLoader;
import org.eclipse.che.ide.ui.smartTree.NodeStorage;
import org.eclipse.che.ide.ui.smartTree.NodeUniqueKeyProvider;
import org.eclipse.che.ide.ui.smartTree.Tree;
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.eclipse.che.ide.ui.smartTree.data.NodeInterceptor;

/**
 * Implementation for FindUsages view. Uses tree for presenting search results.
 *
 * @author Evgen Vidolob
 */
@Singleton
class FindUsagesViewImpl extends BaseView<FindUsagesView.ActionDelegate> implements FindUsagesView {

  private final Tree tree;
  private final NodeFactory nodeFactory;

  @Inject
  public FindUsagesViewImpl(
      NodeFactory nodeFactory, JavaLocalizationConstant localizationConstant) {
    this.nodeFactory = nodeFactory;
    setTitle(localizationConstant.findUsagesPartTitle());
    DockLayoutPanel panel = new DockLayoutPanel(Style.Unit.PX);

    NodeStorage storage =
        new NodeStorage(
            new NodeUniqueKeyProvider() {
              @Override
              public String getKey(@NotNull Node item) {
                return String.valueOf(item.hashCode());
              }
            });
    NodeLoader loader = new NodeLoader(Collections.<NodeInterceptor>emptySet());
    tree = new Tree(storage, loader);
    panel.add(tree);
    setContentWidget(panel);
    panel.ensureDebugId("findUsages-panel");
  }

  @Override
  protected void focusView() {
    tree.setFocus(true);
  }

  @Override
  public void showUsages(final FindUsagesResponse usagesResponse) {
    tree.getNodeStorage().clear();
    tree.getNodeStorage().add(nodeFactory.create(usagesResponse));

    tree.expandAll();

    if (!tree.getRootNodes().isEmpty()) {
      tree.getSelectionModel().select(tree.getRootNodes().get(0), false);
    }
  }
}
