/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ext.java.client.search.node;

import static org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper.createFromAsyncRequest;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.ext.java.shared.dto.model.JavaProject;
import org.eclipse.che.ide.ext.java.shared.dto.model.PackageFragment;
import org.eclipse.che.ide.ext.java.shared.dto.model.PackageFragmentRoot;
import org.eclipse.che.ide.ext.java.shared.dto.search.Match;
import org.eclipse.che.ide.project.shared.NodesResources;
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.eclipse.che.ide.ui.smartTree.presentation.AbstractPresentationNode;
import org.eclipse.che.ide.ui.smartTree.presentation.NodePresentation;

/** @author Evgen Vidolob */
public class JavaProjectNode extends AbstractPresentationNode {

  private NodesResources resources;
  private NodeFactory nodeFactory;
  private JavaProject project;
  private Map<String, List<Match>> matches;

  @Inject
  public JavaProjectNode(
      NodesResources resources,
      NodeFactory nodeFactory,
      @Assisted @NotNull JavaProject project,
      @Assisted Map<String, List<Match>> matches) {
    this.resources = resources;
    this.nodeFactory = nodeFactory;
    this.project = project;
    this.matches = matches;
  }

  @Override
  protected Promise<List<Node>> getChildrenImpl() {
    return createFromAsyncRequest(
        callback -> {
          final List<Node> childrenNodes = new ArrayList<>();
          for (PackageFragmentRoot packageFragmentRoot : project.getPackageFragmentRoots()) {
            final List<PackageFragment> packageFragments =
                packageFragmentRoot.getPackageFragments();
            final List<Node> nodes =
                packageFragments
                    .stream()
                    .map(
                        packageFragment ->
                            nodeFactory.create(packageFragment, matches, packageFragmentRoot))
                    .collect(Collectors.toList());
            childrenNodes.addAll(nodes);
          }
          callback.onSuccess(childrenNodes);
        });
  }

  @Override
  public void updatePresentation(@NotNull NodePresentation presentation) {
    presentation.setPresentableText(project.getName());
    presentation.setPresentableIcon(resources.projectFolder());
  }

  @Override
  public String getName() {
    return project.getName();
  }

  @Override
  public boolean isLeaf() {
    return false;
  }
}
