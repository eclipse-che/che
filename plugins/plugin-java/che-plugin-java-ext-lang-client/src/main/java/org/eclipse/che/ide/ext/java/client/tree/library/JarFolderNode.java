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
package org.eclipse.che.ide.ext.java.client.tree.library;

import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.ext.java.shared.JarEntry.JarEntryType.CLASS_FILE;
import static org.eclipse.che.ide.ext.java.shared.JarEntry.JarEntryType.FILE;
import static org.eclipse.che.ide.ext.java.shared.JarEntry.JarEntryType.FOLDER;
import static org.eclipse.che.ide.ext.java.shared.JarEntry.JarEntryType.PACKAGE;

import com.google.common.annotations.Beta;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.theme.Style;
import org.eclipse.che.ide.ext.java.client.JavaResources;
import org.eclipse.che.ide.ext.java.client.navigation.service.JavaNavigationService;
import org.eclipse.che.ide.ext.java.client.tree.JavaNodeFactory;
import org.eclipse.che.ide.ext.java.shared.JarEntry;
import org.eclipse.che.ide.project.node.SyntheticNode;
import org.eclipse.che.ide.project.shared.NodesResources;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.eclipse.che.ide.ui.smartTree.data.settings.NodeSettings;
import org.eclipse.che.ide.ui.smartTree.presentation.NodePresentation;

/** @author Vlad Zhukovskiy */
@Beta
public class JarFolderNode extends SyntheticNode<JarEntry> {
  private final int libId;
  private final Path project;
  private final JavaNavigationService service;
  private final JavaResources javaResources;
  private final NodesResources nodesResources;
  private final JavaNodeFactory nodeFactory;

  @Inject
  public JarFolderNode(
      @Assisted JarEntry jarEntry,
      @Assisted int libId,
      @Assisted Path project,
      @Assisted NodeSettings nodeSettings,
      JavaNavigationService service,
      JavaResources javaResources,
      NodesResources nodesResources,
      JavaNodeFactory nodeFactory) {
    super(jarEntry, nodeSettings);
    this.libId = libId;
    this.project = project;
    this.service = service;
    this.javaResources = javaResources;
    this.nodesResources = nodesResources;
    this.nodeFactory = nodeFactory;

    getAttributes()
        .put(
            CUSTOM_BACKGROUND_FILL,
            singletonList(Style.theme.projectExplorerReadonlyItemBackground()));
  }

  @NotNull
  @Override
  protected Promise<List<Node>> getChildrenImpl() {
    return service
        .getChildren(project, libId, Path.valueOf(getData().getPath()))
        .then(
            new Function<List<JarEntry>, List<Node>>() {
              @Override
              public List<Node> apply(List<JarEntry> entries) throws FunctionException {
                List<Node> nodes = new ArrayList<>();

                for (JarEntry entry : entries) {
                  if (entry.getType() == FOLDER || entry.getType() == PACKAGE) {
                    nodes.add(nodeFactory.newJarFolderNode(entry, libId, project, getSettings()));
                  } else if (entry.getType() == FILE || entry.getType() == CLASS_FILE) {
                    nodes.add(nodeFactory.newJarFileNode(entry, libId, project, getSettings()));
                  }
                }

                return nodes;
              }
            });
  }

  @Override
  public void updatePresentation(@NotNull NodePresentation presentation) {
    presentation.setPresentableText(getData().getName());
    presentation.setPresentableIcon(
        getData().getType() == PACKAGE
            ? javaResources.packageItem()
            : nodesResources.simpleFolder());
  }

  @NotNull
  @Override
  public String getName() {
    return getData().getName();
  }

  @Override
  public boolean isLeaf() {
    return false;
  }

  @Override
  public Path getProject() {
    return project;
  }
}
