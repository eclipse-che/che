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
package org.eclipse.che.ide.ext.java.client.search;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Collections;
import java.util.List;
import org.eclipse.che.ide.api.parts.base.BaseView;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ui.smartTree.NodeLoader;
import org.eclipse.che.ide.ui.smartTree.NodeStorage;
import org.eclipse.che.ide.ui.smartTree.Tree;
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.eclipse.che.ide.ui.smartTree.data.NodeInterceptor;
import org.eclipse.che.ide.ui.smartTree.event.ExpandNodeEvent;
import org.eclipse.che.ide.ui.smartTree.event.LoadExceptionEvent;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.jdt.ls.extension.api.dto.UsagesResponse;

/**
 * Implementation for FindUsages view. Uses tree for presenting search results.
 *
 * @author Evgen Vidolob
 */
@Singleton
public class FindUsagesViewImpl extends BaseView<FindUsagesPresenter> implements FindUsagesView {
  private final Tree tree;
  private final NodeFactory nodeFactory;

  @Inject
  public FindUsagesViewImpl(
      NodeFactory nodeFactory, JavaLocalizationConstant localizationConstant) {
    this.nodeFactory = nodeFactory;
    setTitle(localizationConstant.findUsagesPartTitle());
    DockLayoutPanel panel = new DockLayoutPanel(Style.Unit.PX);

    NodeStorage storage = new NodeStorage(item -> String.valueOf(item.hashCode()));
    NodeLoader loader = new NodeLoader(Collections.<NodeInterceptor>emptySet());
    loader.addLoadExceptionHandler(
        new LoadExceptionEvent.LoadExceptionHandler() {

          @Override
          public void onLoadException(LoadExceptionEvent event) {
            Log.error(FindUsagesViewImpl.class, event.getException());
          }
        });
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
  public void showUsages(UsagesResponse response) {
    tree.getNodeStorage().clear();
    if (response != null) {
      UsagesNode root = nodeFactory.createRoot(response);
      tree.getNodeStorage().add(root);
      new TreeExpander(100).expandNodes(root);
    }

    if (!tree.getRootNodes().isEmpty()) {
      tree.getSelectionModel().select(tree.getRootNodes().get(0), false);
    }
  }

  private class TreeExpander {
    private int expanded;
    private int target;
    private HandlerRegistration registration;

    public TreeExpander(int target) {
      this.target = target;
    }

    void expandNodes(Node root) {
      Node start = findNextNodeToExpand(root);
      if (start != null) {
        registration =
            tree.addExpandHandler(
                new ExpandNodeEvent.ExpandNodeHandler() {

                  @Override
                  public void onExpand(ExpandNodeEvent event) {
                    expanded++;
                    if (expanded >= target) {
                      registration.removeHandler();
                      return;
                    } else {
                      Node next = findNextNodeToExpand(event.getNode());
                      if (next != null) {
                        tree.setExpanded(next, true);
                      } else {
                        registration.removeHandler();
                      }
                    }
                  }
                });
        tree.setExpanded(root, true);
      }
    }

    private Node findNextNodeToExpand(Node node) {
      Node descendant = findExpandableInTree(node);
      if (descendant != null) {
        return descendant;
      }
      NodeStorage nodes = tree.getNodeStorage();

      Node parent = nodes.getParent(node);
      if (parent == null) {
        return null;
      }
      List<Node> siblings = nodes.getChildren(parent);
      for (Node sibling : siblings) {
        if (canExpand(sibling)) {
          return sibling;
        }
      }
      return findNextNodeToExpand(parent);
    }

    private Node findExpandableInTree(Node node) {
      if (canExpand(node)) {
        return node;
      }
      List<Node> children = tree.getNodeStorage().getChildren(node);
      for (Node child : children) {
        Node descendant = findExpandableInTree(child);
        if (descendant != null) {
          return descendant;
        }
      }
      return null;
    }

    private boolean canExpand(Node node) {
      return !tree.isLeaf(node) && !tree.isExpanded(node);
    }
  }
}
