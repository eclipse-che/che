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

import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.ext.java.client.tree.library.EntryType.CLASS_FILE;
import static org.eclipse.che.ide.ext.java.client.tree.library.EntryType.FILE;
import static org.eclipse.che.ide.ext.java.client.tree.library.EntryType.FOLDER;
import static org.eclipse.che.ide.ext.java.client.tree.library.EntryType.PACKAGE;

import com.google.common.annotations.Beta;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.theme.Style;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.java.client.JavaResources;
import org.eclipse.che.ide.ext.java.client.service.JavaLanguageExtensionServiceClient;
import org.eclipse.che.ide.ext.java.client.tree.JavaNodeFactory;
import org.eclipse.che.ide.project.node.SyntheticNode;
import org.eclipse.che.ide.project.shared.NodesResources;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.eclipse.che.ide.ui.smartTree.data.settings.NodeSettings;
import org.eclipse.che.ide.ui.smartTree.presentation.NodePresentation;
import org.eclipse.che.jdt.ls.extension.api.dto.ExternalLibrariesParameters;
import org.eclipse.che.jdt.ls.extension.api.dto.JarEntry;

/** @author Vlad Zhukovskiy */
@Beta
public class JarFolderNode extends SyntheticNode<JarEntry> {
  private final String libId;
  private final Path project;
  private final JavaResources javaResources;
  private final NodesResources nodesResources;
  private final JavaNodeFactory nodeFactory;
  private DtoFactory dtoFactory;
  private final JavaLanguageExtensionServiceClient service;

  @Inject
  public JarFolderNode(
      @Assisted JarEntry jarEntry,
      @Assisted String libId,
      @Assisted Path project,
      @Assisted NodeSettings nodeSettings,
      JavaResources javaResources,
      DtoFactory dtoFactory,
      JavaLanguageExtensionServiceClient service,
      NodesResources nodesResources,
      JavaNodeFactory nodeFactory) {
    super(jarEntry, nodeSettings);
    this.libId = libId;
    this.project = project;
    this.dtoFactory = dtoFactory;
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
    ExternalLibrariesParameters params = dtoFactory.createDto(ExternalLibrariesParameters.class);
    params.setProjectUri(project.toString());
    params.setNodeId(libId);
    params.setNodePath(getData().getPath());
    return service
        .externalLibrariesChildren(params)
        .then(
            (Function<List<JarEntry>, List<Node>>)
                entries -> {
                  List<Node> nodes = new ArrayList<>();

                  for (JarEntry entry : entries) {
                    if (FOLDER.name().equals(entry.getEntryType())
                        || PACKAGE.name().equals(entry.getEntryType())) {
                      nodes.add(nodeFactory.newJarFolderNode(entry, libId, project, getSettings()));
                    } else if (FILE.name().equals(entry.getEntryType())
                        || CLASS_FILE.name().equals(entry.getEntryType())) {
                      nodes.add(nodeFactory.newJarFileNode(entry, libId, project, getSettings()));
                    }
                  }

                  return nodes;
                });
  }

  @Override
  public void updatePresentation(@NotNull NodePresentation presentation) {
    presentation.setPresentableText(getData().getName());
    presentation.setPresentableIcon(
        PACKAGE.name().equals(getData().getEntryType())
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
