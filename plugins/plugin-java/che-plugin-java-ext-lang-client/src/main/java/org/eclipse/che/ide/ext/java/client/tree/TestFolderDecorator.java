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
package org.eclipse.che.ide.ext.java.client.tree;

import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.project.node.SyntheticNode.CUSTOM_BACKGROUND_FILL;

import com.google.common.annotations.Beta;
import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.resources.marker.Marker;
import org.eclipse.che.ide.api.theme.Style;
import org.eclipse.che.ide.ext.java.client.resource.SourceFolderMarker;
import org.eclipse.che.ide.ext.java.shared.ContentRoot;
import org.eclipse.che.ide.resources.tree.ResourceNode;
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.eclipse.che.ide.ui.smartTree.data.NodeInterceptor;

/**
 * Decorates child of test content root with custom background color.
 *
 * @author Vlad Zhukovskiy
 */
@Beta
@Singleton
public class TestFolderDecorator implements NodeInterceptor {

  private PromiseProvider promises;

  @Inject
  public TestFolderDecorator(PromiseProvider promises) {
    this.promises = promises;
  }

  /** {@inheritDoc} */
  @Override
  public Promise<List<Node>> intercept(Node parent, List<Node> children) {

    final List<Node> intercepted = new ArrayList<>();

    for (Node node : children) {

      if (node instanceof ResourceNode) {
        intercepted.add(transform((ResourceNode) node));
      } else {
        intercepted.add(node);
      }
    }

    return promises.resolve(intercepted);
  }

  /** {@inheritDoc} */
  @Override
  public int getPriority() {
    return MAX_PRIORITY;
  }

  protected Node transform(ResourceNode resourceNode) {
    final Optional<Resource> srcFolder =
        resourceNode.getData().getParentWithMarker(SourceFolderMarker.ID);

    if (!srcFolder.isPresent()) {
      return resourceNode;
    }

    final Optional<Marker> marker = srcFolder.get().getMarker(SourceFolderMarker.ID);

    final ContentRoot contentRoot = ((SourceFolderMarker) marker.get()).getContentRoot();

    if (contentRoot == ContentRoot.TEST_SOURCE
        && !resourceNode.getData().getLocation().equals(srcFolder.get().getLocation())) {
      resourceNode
          .getAttributes()
          .put(
              CUSTOM_BACKGROUND_FILL,
              singletonList(Style.theme.projectExplorerTestItemBackground()));
    }

    return resourceNode;
  }
}
