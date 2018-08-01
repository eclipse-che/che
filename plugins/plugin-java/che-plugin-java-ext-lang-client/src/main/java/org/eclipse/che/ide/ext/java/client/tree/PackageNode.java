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

import static com.google.common.base.Preconditions.checkArgument;
import static org.eclipse.che.ide.api.resources.Resource.FILE;
import static org.eclipse.che.ide.api.resources.Resource.FOLDER;
import static org.eclipse.che.ide.api.resources.Resource.PROJECT;

import com.google.common.annotations.Beta;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.web.bindery.event.shared.EventBus;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.ext.java.client.JavaResources;
import org.eclipse.che.ide.project.node.icon.NodeIconProvider;
import org.eclipse.che.ide.project.shared.NodesResources;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.resources.tree.ContainerNode;
import org.eclipse.che.ide.resources.tree.ResourceNode;
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.eclipse.che.ide.ui.smartTree.data.settings.NodeSettings;
import org.eclipse.che.ide.ui.smartTree.presentation.NodePresentation;

/** @author Vlad Zhukovskiy */
@Beta
public class PackageNode extends ContainerNode {

  private final JavaNodeFactory nodeFactory;
  private final JavaResources javaResources;

  @Inject
  public PackageNode(
      @Assisted Container resource,
      @Assisted NodeSettings nodeSettings,
      JavaNodeFactory nodeFactory,
      NodesResources nodesResources,
      JavaResources javaResources,
      EventBus eventBus,
      Set<NodeIconProvider> nodeIconProviders) {
    super(resource, nodeSettings, nodeFactory, nodesResources, eventBus, nodeIconProviders);
    this.nodeFactory = nodeFactory;
    this.javaResources = javaResources;
  }

  /** {@inheritDoc} */
  @Override
  public void updatePresentation(@NotNull NodePresentation presentation) {
    super.updatePresentation(presentation);

    presentation.setPresentableText(getDisplayPackage());
    presentation.setPresentableIcon(javaResources.packageItem());
  }

  protected Node createNode(Resource resource) {
    checkArgument(resource != null, "Not a resource");

    switch (resource.getResourceType()) {
      case PROJECT:
      case FOLDER:
        return nodeFactory.newPackage((Container) resource, getSettings());
      case FILE:
        return nodeFactory.newFileNode((File) resource, getSettings());
      default:
        throw new IllegalArgumentException("Resource type was not recognized");
    }
  }

  /**
   * Return display FQN for the package according to the parent. E.g. from package a.b.c.d will
   * displayed only c.d
   *
   * @return partially displayed FQN name.
   */
  protected String getDisplayPackage() {
    final Node parent = getParent();

    if (parent instanceof ResourceNode) {
      final Path parentLocation = ((ResourceNode) parent).getData().getLocation();

      return getData()
          .getLocation()
          .removeFirstSegments(parentLocation.segmentCount())
          .toString()
          .replace('/', '.');
    } else {
      return getName();
    }
  }

  @Override
  public int compareTo(ResourceNode o) {
    if (o instanceof PackageNode) {
      return getDisplayPackage().compareTo(((PackageNode) o).getDisplayPackage());
    }

    return super.compareTo(o);
  }
}
