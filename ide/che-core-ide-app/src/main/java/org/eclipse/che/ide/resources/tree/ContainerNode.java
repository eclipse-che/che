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
package org.eclipse.che.ide.resources.tree;

import com.google.common.annotations.Beta;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.web.bindery.event.shared.EventBus;
import java.util.Set;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.project.node.icon.NodeIconProvider;
import org.eclipse.che.ide.project.shared.NodesResources;
import org.eclipse.che.ide.ui.smartTree.data.settings.NodeSettings;

/**
 * Node that represents container based node in the project tree.
 *
 * @author Vlad Zhukovskiy
 * @see Container
 * @see ResourceNode
 * @since 4.4.0
 */
@Beta
public class ContainerNode extends ResourceNode<Container> {
  @Inject
  public ContainerNode(
      @Assisted Container resource,
      @Assisted NodeSettings nodeSettings,
      NodeFactory nodeFactory,
      NodesResources nodesResources,
      EventBus eventBus,
      Set<NodeIconProvider> nodeIconProviders) {
    super(resource, nodeSettings, nodesResources, nodeFactory, eventBus, nodeIconProviders);
  }
}
