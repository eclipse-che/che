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
import org.eclipse.che.ide.ext.java.client.JavaResources;
import org.eclipse.che.ide.ext.java.shared.dto.model.CompilationUnit;
import org.eclipse.che.ide.ext.java.shared.dto.model.PackageFragment;
import org.eclipse.che.ide.ext.java.shared.dto.model.PackageFragmentRoot;
import org.eclipse.che.ide.ext.java.shared.dto.model.Type;
import org.eclipse.che.ide.ext.java.shared.dto.search.Match;
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.eclipse.che.ide.ui.smartTree.presentation.AbstractPresentationNode;
import org.eclipse.che.ide.ui.smartTree.presentation.NodePresentation;
import org.eclipse.che.ide.util.Pair;

/**
 * Node represent package fragment.
 *
 * @author Evgen Vidolob
 */
public class PackageFragmentNode extends AbstractPresentationNode {

  private JavaResources resources;
  private NodeFactory nodeFactory;
  private PackageFragment packageFragment;
  private Map<String, List<Match>> matches;
  private PackageFragmentRoot parent;

  @Inject
  public PackageFragmentNode(
      JavaResources resources,
      NodeFactory nodeFactory,
      @Assisted PackageFragment packageFragment,
      @Assisted Map<String, List<Match>> matches,
      @Assisted PackageFragmentRoot parent) {
    this.resources = resources;
    this.nodeFactory = nodeFactory;
    this.packageFragment = packageFragment;
    this.matches = matches;
    this.parent = parent;
  }

  @Override
  protected Promise<List<Node>> getChildrenImpl() {
    return createFromAsyncRequest(
        callback -> {
          final List<Node> children = new ArrayList<>();
          if (packageFragment.getKind() == PackageFragmentRoot.K_SOURCE) {
            for (CompilationUnit compilationUnit : packageFragment.getCompilationUnits()) {
              final List<Type> types = compilationUnit.getTypes();
              final List<Node> nodes =
                  types
                      .stream()
                      .filter(Type::isPrimary)
                      .map(type -> nodeFactory.create(type, compilationUnit, null, matches))
                      .collect(Collectors.toList());
              children.addAll(nodes);
            }
          } else {
            children.addAll(
                packageFragment
                    .getClassFiles()
                    .stream()
                    .map(
                        classFile ->
                            nodeFactory.create(classFile.getType(), null, classFile, matches))
                    .collect(Collectors.toList()));
          }

          callback.onSuccess(children);
        });
  }

  @Override
  public void updatePresentation(@NotNull NodePresentation presentation) {
    presentation.setPresentableText(packageFragment.getElementName());
    if (parent.getKind() == PackageFragmentRoot.K_BINARY) {
      presentation.setInfoText(parent.getElementName());
    } else {

      presentation.setInfoText(parent.getPath().substring(parent.getProjectPath().length() + 1));
    }
    presentation.setInfoTextWrapper(Pair.of("- ", ""));
    presentation.setPresentableIcon(resources.packageItem());
  }

  @Override
  public String getName() {
    return packageFragment.getElementName();
  }

  @Override
  public boolean isLeaf() {
    return false;
  }
}
