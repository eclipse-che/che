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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.ext.java.client.JavaResources;
import org.eclipse.che.ide.ext.java.client.util.Flags;
import org.eclipse.che.ide.ext.java.shared.dto.model.ClassFile;
import org.eclipse.che.ide.ext.java.shared.dto.model.CompilationUnit;
import org.eclipse.che.ide.ext.java.shared.dto.model.Method;
import org.eclipse.che.ide.ext.java.shared.dto.search.Match;
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.eclipse.che.ide.ui.smartTree.presentation.AbstractPresentationNode;
import org.eclipse.che.ide.ui.smartTree.presentation.NodePresentation;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Represents java method
 *
 * @author Evgen Vidolob
 */
public class MethodNode extends AbstractPresentationNode {

  private NodeFactory nodeFactory;
  private JavaResources resources;
  private Method method;
  private Map<String, List<Match>> matches;
  private CompilationUnit compilationUnit;
  private ClassFile classFile;

  @Inject
  public MethodNode(
      NodeFactory nodeFactory,
      JavaResources resources,
      @Assisted Method method,
      @Assisted Map<String, List<Match>> matches,
      @Assisted CompilationUnit compilationUnit,
      @Assisted ClassFile classFile) {
    this.nodeFactory = nodeFactory;
    this.resources = resources;
    this.method = method;
    this.matches = matches;
    this.compilationUnit = compilationUnit;
    this.classFile = classFile;
  }

  @Override
  protected Promise<List<Node>> getChildrenImpl() {
    return createFromAsyncRequest(
        callback -> {
          final List<Node> children = new ArrayList<>();
          final String handleIdentifier = method.getHandleIdentifier();

          if (matches.containsKey(handleIdentifier)) {
            final List<Node> nodes =
                matches
                    .get(handleIdentifier)
                    .stream()
                    .map(match -> nodeFactory.create(match, compilationUnit, classFile))
                    .collect(Collectors.toList());
            children.addAll(nodes);
          }

          Collections.sort(children, new NodeComparator());
          callback.onSuccess(children);
        });
  }

  @Override
  public void updatePresentation(@NotNull NodePresentation presentation) {
    // TODO set proper icon
    presentation.setPresentableText(method.getLabel());
    int flags = method.getFlags();
    SVGResource icon;
    if (Flags.isPublic(flags)) {
      icon = resources.publicMethod();
    } else if (Flags.isPrivate(flags)) {
      icon = resources.privateMethod();
    } else if (Flags.isProtected(flags)) {
      icon = resources.protectedMethod();
    } else {
      icon = resources.publicMethod();
    }
    presentation.setPresentableIcon(icon);
  }

  @Override
  public String getName() {
    return method.getElementName();
  }

  @Override
  public boolean isLeaf() {
    return !matches.containsKey(method.getHandleIdentifier());
  }

  public List<Match> getMatches() {
    return matches.get(method.getHandleIdentifier());
  }
}
