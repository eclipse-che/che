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
package org.eclipse.che.ide.ext.java.client.tree.library;

import static com.google.common.collect.Lists.newArrayListWithCapacity;
import static org.eclipse.che.ide.api.resources.ResourceDelta.UPDATED;

import com.google.common.annotations.Beta;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.web.bindery.event.shared.EventBus;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.resources.ResourceChangedEvent;
import org.eclipse.che.ide.api.resources.ResourceChangedEvent.ResourceChangedHandler;
import org.eclipse.che.ide.api.resources.ResourceDelta;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.java.client.JavaResources;
import org.eclipse.che.ide.ext.java.client.project.classpath.ProjectClasspathChangedEvent;
import org.eclipse.che.ide.ext.java.client.project.classpath.ProjectClasspathChangedEvent.ProjectClasspathChangedHandler;
import org.eclipse.che.ide.ext.java.client.service.JavaLanguageExtensionServiceClient;
import org.eclipse.che.ide.ext.java.client.tree.JavaNodeFactory;
import org.eclipse.che.ide.project.node.SyntheticNode;
import org.eclipse.che.ide.project.node.SyntheticNodeUpdateEvent;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.eclipse.che.ide.ui.smartTree.data.settings.NodeSettings;
import org.eclipse.che.ide.ui.smartTree.presentation.NodePresentation;
import org.eclipse.che.jdt.ls.extension.api.dto.ExternalLibrariesParameters;
import org.eclipse.che.jdt.ls.extension.api.dto.Jar;

/** @author Vlad Zhukovskiy */
@Beta
public class LibrariesNode extends SyntheticNode<Path>
    implements ResourceChangedHandler, ProjectClasspathChangedHandler {

  private final JavaNodeFactory nodeFactory;
  private DtoFactory dtoFactory;
  private final JavaResources javaResources;
  private final EventBus eventBus;
  private final JavaLanguageExtensionServiceClient service;

  @Inject
  public LibrariesNode(
      @Assisted Path project,
      @Assisted NodeSettings nodeSettings,
      JavaLanguageExtensionServiceClient service,
      JavaNodeFactory nodeFactory,
      DtoFactory dtoFactory,
      JavaResources javaResources,
      EventBus eventBus) {
    super(project, nodeSettings);
    this.service = service;
    this.nodeFactory = nodeFactory;
    this.dtoFactory = dtoFactory;
    this.javaResources = javaResources;
    this.eventBus = eventBus;

    eventBus.addHandler(ResourceChangedEvent.getType(), this);
    eventBus.addHandler(ProjectClasspathChangedEvent.TYPE, this);
  }

  @Override
  protected Promise<List<Node>> getChildrenImpl() {
    ExternalLibrariesParameters params = dtoFactory.createDto(ExternalLibrariesParameters.class);
    params.setProjectUri(getData().toString());
    return service
        .externalLibraries(params)
        .then(
            (Function<List<Jar>, List<Node>>)
                jars -> {
                  List<Node> nodes = newArrayListWithCapacity(jars.size());

                  for (Jar jar : jars) {
                    nodes.add(nodeFactory.newJarNode(jar, getData(), getSettings()));
                  }

                  return nodes;
                });
  }

  @Override
  public void updatePresentation(@NotNull NodePresentation presentation) {
    presentation.setPresentableIcon(javaResources.externalLibraries());
    presentation.setPresentableText(getName());
  }

  @NotNull
  @Override
  public String getName() {
    return "External Libraries";
  }

  @Override
  public boolean isLeaf() {
    return false;
  }

  @Override
  public void onResourceChanged(ResourceChangedEvent event) {
    final ResourceDelta delta = event.getDelta();

    if (delta.getKind() == UPDATED && delta.getResource().getLocation().equals(getData())) {
      eventBus.fireEvent(new SyntheticNodeUpdateEvent(LibrariesNode.this));
    }
  }

  @Override
  public void onProjectClasspathChanged(ProjectClasspathChangedEvent event) {
    final Path project = new Path(event.getProject());

    if (getProject().equals(project)) {
      eventBus.fireEvent(new SyntheticNodeUpdateEvent(LibrariesNode.this));
    }
  }

  @Override
  public Path getProject() {
    return getData();
  }
}
