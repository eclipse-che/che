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
package org.eclipse.che.ide.ext.java.client.tree.library;

import com.google.common.annotations.Beta;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.java.client.JavaResources;
import org.eclipse.che.ide.ext.java.client.service.JavaLanguageExtensionServiceClient;
import org.eclipse.che.ide.ext.java.client.tree.JavaNodeFactory;
import org.eclipse.che.ide.project.node.SyntheticNode;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.eclipse.che.ide.ui.smartTree.data.settings.NodeSettings;
import org.eclipse.che.ide.ui.smartTree.presentation.NodePresentation;
import org.eclipse.che.jdt.ls.extension.api.dto.ExternalLibrariesParameters;
import org.eclipse.che.jdt.ls.extension.api.dto.Jar;
import org.eclipse.che.jdt.ls.extension.api.dto.JarEntry;

/** @author Vlad Zhukovskiy */
@Beta
public class JarNode extends SyntheticNode<Jar> {

  private final Path project;
  private final JavaResources javaResources;
  private final DtoFactory dtoFactory;
  private final JavaLanguageExtensionServiceClient service;
  private final JavaNodeFactory nodeFactory;

  @Inject
  public JarNode(
      @Assisted Jar jar,
      @Assisted Path project,
      @Assisted NodeSettings nodeSettings,
      JavaResources javaResources,
      DtoFactory dtoFactory,
      JavaLanguageExtensionServiceClient service,
      JavaNodeFactory nodeFactory) {
    super(jar, nodeSettings);
    this.project = project;
    this.javaResources = javaResources;
    this.dtoFactory = dtoFactory;
    this.service = service;
    this.nodeFactory = nodeFactory;
  }

  @NotNull
  @Override
  protected Promise<List<Node>> getChildrenImpl() {
    ExternalLibrariesParameters params = dtoFactory.createDto(ExternalLibrariesParameters.class);
    params.setProjectUri(project.toString());
    params.setNodeId(getData().getId());
    return service
        .libraryChildren(params)
        .then(
            (Function<List<JarEntry>, List<Node>>)
                entries -> {
                  List<Node> nodes = new ArrayList<>();

                  for (JarEntry entry : entries) {
                    if ("FOLDER".equals(entry.getEntryType())
                        || "PACKAGE".equals(entry.getEntryType())) {
                      nodes.add(
                          nodeFactory.newJarFolderNode(
                              entry, getData().getId(), project, getSettings()));
                    } else if ("FILE".equals(entry.getEntryType())
                        || "CLASS_FILE".equals(entry.getEntryType())) {
                      nodes.add(
                          nodeFactory.newJarFileNode(
                              entry, getData().getId(), project, getSettings()));
                    }
                  }

                  return nodes;
                });
  }

  @Override
  public void updatePresentation(@NotNull NodePresentation presentation) {
    presentation.setPresentableIcon(javaResources.jarFileIcon());
    presentation.setPresentableText(getData().getName());
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
