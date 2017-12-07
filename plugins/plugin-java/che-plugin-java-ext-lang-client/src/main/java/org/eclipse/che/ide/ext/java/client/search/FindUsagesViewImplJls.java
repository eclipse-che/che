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
package org.eclipse.che.ide.ext.java.client.search;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Collections;
import java.util.List;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.parts.base.BaseActionDelegate;
import org.eclipse.che.ide.api.parts.base.BaseView;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ui.smartTree.NodeLoader;
import org.eclipse.che.ide.ui.smartTree.NodeStorage;
import org.eclipse.che.ide.ui.smartTree.Tree;
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.eclipse.che.ide.ui.smartTree.data.NodeInterceptor;
import org.eclipse.che.jdt.ls.extension.api.dto.UsagesResponse;

/**
 * Implementation for FindUsages view. Uses tree for presenting search results.
 *
 * @author Evgen Vidolob
 */
@Singleton
class FindUsagesViewImplJls extends BaseView<BaseActionDelegate> {

  private final Tree tree;
  private final NodeFactory nodeFactory;

  @Inject
  public FindUsagesViewImplJls(
      NodeFactory nodeFactory, JavaLocalizationConstant localizationConstant) {
    this.nodeFactory = nodeFactory;
    setTitle(localizationConstant.findUsagesPartTitle());
    DockLayoutPanel panel = new DockLayoutPanel(Style.Unit.PX);

    NodeStorage storage = new NodeStorage(item -> String.valueOf(item.hashCode()));
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

  public void showUsages(UsagesResponse response) {
    tree.getNodeStorage().clear();
    if (response != null) {
      UsagesNode root = nodeFactory.createRoot(response);
      tree.getNodeStorage().add(root);
      tree.setExpanded(root, true);
      Promise<List<Node>> children = root.getChildren(false);
      children.then(
          (projects) -> {
            if (!projects.isEmpty()) {
              tree.setExpanded(projects.get(0), true, true);
            }
          });
    }

    if (!tree.getRootNodes().isEmpty()) {
      tree.getSelectionModel().select(tree.getRootNodes().get(0), false);
    }
  }
}
