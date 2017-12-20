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
package org.eclipse.che.ide.resources.tree;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayListWithExpectedSize;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static org.eclipse.che.ide.api.resources.Resource.FILE;
import static org.eclipse.che.ide.api.resources.Resource.FOLDER;
import static org.eclipse.che.ide.api.resources.Resource.PROJECT;
import static org.eclipse.che.ide.api.vcs.VcsStatus.NOT_MODIFIED;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.web.bindery.event.shared.EventBus;
import java.util.List;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.resources.marker.Marker;
import org.eclipse.che.ide.api.resources.marker.MarkerChangedEvent;
import org.eclipse.che.ide.api.resources.marker.PresentableTextMarker;
import org.eclipse.che.ide.api.resources.modification.CutResourceMarker;
import org.eclipse.che.ide.api.vcs.VcsStatus;
import org.eclipse.che.ide.project.node.icon.NodeIconProvider;
import org.eclipse.che.ide.project.shared.NodesResources;
import org.eclipse.che.ide.ui.smartTree.data.AbstractTreeNode;
import org.eclipse.che.ide.ui.smartTree.data.HasDataObject;
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.eclipse.che.ide.ui.smartTree.data.settings.HasSettings;
import org.eclipse.che.ide.ui.smartTree.data.settings.NodeSettings;
import org.eclipse.che.ide.ui.smartTree.presentation.HasPresentation;
import org.eclipse.che.ide.ui.smartTree.presentation.NodePresentation;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Abstract based implementation for all resource based nodes in the IDE.
 *
 * @author Vlad Zhukovskiy
 * @see Resource
 * @see ContainerNode
 * @see FileNode
 * @since 4.4.0
 */
@Beta
public abstract class ResourceNode<R extends Resource> extends AbstractTreeNode
    implements HasDataObject<R>, HasPresentation, HasSettings, Comparable<ResourceNode> {

  private static final List<Node> NO_CHILDREN = emptyList();

  private R resource;
  private NodeSettings nodeSettings;
  private NodePresentation nodePresentation;
  private boolean resourceIsCut;
  private final NodeFactory nodeFactory;
  protected final NodesResources nodesResources;
  private final Set<NodeIconProvider> nodeIconProviders;

  protected ResourceNode(
      R resource,
      NodeSettings nodeSettings,
      NodesResources nodesResources,
      NodeFactory nodeFactory,
      EventBus eventBus,
      Set<NodeIconProvider> nodeIconProviders) {
    this.resource = resource;
    this.nodeSettings = nodeSettings;
    this.nodeFactory = nodeFactory;
    this.nodesResources = nodesResources;
    this.nodeIconProviders = nodeIconProviders;

    eventBus.addHandler(
        MarkerChangedEvent.getType(),
        new MarkerChangedEvent.MarkerChangedHandler() {
          @Override
          public void onMarkerChanged(MarkerChangedEvent event) {
            if (event.getMarker().getType().equals(CutResourceMarker.ID)
                && getData().equals(event.getResource())) {
              resourceIsCut = event.getStatus() != Marker.REMOVED;
            }
          }
        });
  }

  @Override
  public NodeSettings getSettings() {
    return nodeSettings;
  }

  @Override
  public R getData() {
    return resource;
  }

  @Override
  public void setData(R data) {
    this.resource = data;
  }

  @Override
  protected Promise<List<Node>> getChildrenImpl() {
    checkState(getData() instanceof Container, "Not a container");

    return ((Container) getData())
        .getChildren()
        .then(
            (Function<Resource[], List<Node>>)
                children -> {
                  if (children == null || children.length == 0) {
                    return NO_CHILDREN;
                  }

                  final List<Node> nodes = newArrayListWithExpectedSize(children.length);

                  for (Resource child : children) {
                    nodes.add(createNode(child));
                  }

                  return unmodifiableList(nodes);
                });
  }

  @Override
  public final NodePresentation getPresentation(boolean update) {
    if (nodePresentation == null) {
      nodePresentation = new NodePresentation();
    }

    // need to force update presentation to correct display node in project explorer after restart
    // workspace details https://github.com/eclipse/che/issues/6314
    // problem reproduce randomly
    updatePresentation(nodePresentation);

    return nodePresentation;
  }

  @Override
  public void updatePresentation(@NotNull NodePresentation presentation) {
    final StringBuilder cssBuilder = new StringBuilder();

    final Optional<Marker> presentableTextMarker = getData().getMarker(PresentableTextMarker.ID);
    if (presentableTextMarker.isPresent() && getData() instanceof Container) {
      presentation.setPresentableText(
          ((PresentableTextMarker) presentableTextMarker.get()).getPresentableText());
    } else {
      presentation.setPresentableText(getData().getName());
    }

    if (resourceIsCut) {
      cssBuilder.append("opacity:0.5;");
    } else {
      cssBuilder.append("opacity:1;");
    }

    SVGResource icon = null;

    for (NodeIconProvider iconProvider : nodeIconProviders) {
      icon = iconProvider.getIcon(getData());

      if (icon != null) {
        break;
      }
    }

    if (icon != null) {
      presentation.setPresentableIcon(icon);
    } else {
      if (getData().getResourceType() == FOLDER) {
        presentation.setPresentableIcon(
            getData().getName().startsWith(".")
                ? nodesResources.hiddenSimpleFolder()
                : nodesResources.simpleFolder());
      } else if (getData().getResourceType() == PROJECT) {
        presentation.setPresentableIcon(
            ((Project) getData()).isProblem()
                ? nodesResources.notValidProjectFolder()
                : nodesResources.projectFolder());
        cssBuilder.append("font-weight:bold;");

      } else if (getData().getResourceType() == FILE) {
        presentation.setPresentableIcon(nodesResources.file());
      }
    }

    presentation.setPresentableTextCss(cssBuilder.toString());

    if (getData().isFile() && getData().asFile().getVcsStatus() != null) {
      VcsStatus vcsStatus = getData().asFile().getVcsStatus();
      if (vcsStatus != NOT_MODIFIED) {
        presentation.setPresentableTextCss("color: " + vcsStatus.getColor() + ";");
      }
    }
  }

  @Override
  public String getName() {
    return getData().getName();
  }

  @Override
  public boolean isLeaf() {
    return getData().getResourceType() == Resource.FILE;
  }

  @Override
  public boolean supportGoInto() {
    return getData() instanceof Container;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ResourceNode)) return false;
    ResourceNode<?> that = (ResourceNode<?>) o;
    return Objects.equal(resource, that.resource);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(resource);
  }

  @Override
  public int compareTo(ResourceNode o) {
    return getData().compareTo(o.getData());
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("resource", resource).toString();
  }

  public interface NodeFactory {
    FileNode newFileNode(File resource, NodeSettings nodeSettings);

    ContainerNode newContainerNode(Container resource, NodeSettings nodeSettings);
  }

  protected Node createNode(Resource resource) {
    checkArgument(resource != null, "Not a resource");

    switch (resource.getResourceType()) {
      case PROJECT:
      case FOLDER:
        return nodeFactory.newContainerNode((Container) resource, getSettings());
      case FILE:
        return nodeFactory.newFileNode((File) resource, getSettings());
      default:
        throw new IllegalArgumentException("Resource type was not recognized");
    }
  }
}
