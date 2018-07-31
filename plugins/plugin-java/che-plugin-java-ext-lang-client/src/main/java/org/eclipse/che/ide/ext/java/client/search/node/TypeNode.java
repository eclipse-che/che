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
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.ext.java.client.JavaResources;
import org.eclipse.che.ide.ext.java.client.util.Flags;
import org.eclipse.che.ide.ext.java.shared.dto.model.ClassFile;
import org.eclipse.che.ide.ext.java.shared.dto.model.CompilationUnit;
import org.eclipse.che.ide.ext.java.shared.dto.model.Field;
import org.eclipse.che.ide.ext.java.shared.dto.model.ImportDeclaration;
import org.eclipse.che.ide.ext.java.shared.dto.model.Initializer;
import org.eclipse.che.ide.ext.java.shared.dto.model.Type;
import org.eclipse.che.ide.ext.java.shared.dto.search.Match;
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.eclipse.che.ide.ui.smartTree.presentation.AbstractPresentationNode;
import org.eclipse.che.ide.ui.smartTree.presentation.NodePresentation;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Represent java type. Imports form compilation unit will places as child of this node.
 *
 * @author Evgen Vidolob
 */
public class TypeNode extends AbstractPresentationNode {

  private final JavaResources resources;
  private final Type type;
  private final CompilationUnit compilationUnit;
  private NodeFactory nodeFactory;
  private ClassFile classFile;
  private Map<String, List<Match>> matches;

  @Inject
  public TypeNode(
      JavaResources resources,
      NodeFactory nodeFactory,
      @Assisted Type type,
      @Nullable @Assisted CompilationUnit compilationUnit,
      @Assisted ClassFile classFile,
      @Assisted Map<String, List<Match>> matches) {
    this.resources = resources;
    this.nodeFactory = nodeFactory;
    this.type = type;
    this.compilationUnit = compilationUnit;
    this.classFile = classFile;
    this.matches = matches;
  }

  @Override
  protected Promise<List<Node>> getChildrenImpl() {
    return createFromAsyncRequest(
        callback -> {
          List<Node> children = new ArrayList<>();
          if (compilationUnit != null && type.isPrimary()) {
            for (ImportDeclaration importDeclaration : compilationUnit.getImports()) {
              createNodeForAllMatches(importDeclaration.getHandleIdentifier(), children);
            }
            for (Type subType : compilationUnit.getTypes()) {
              if (subType == type) {
                continue;
              }
              children.add(nodeFactory.create(subType, compilationUnit, classFile, matches));
            }
          }
          createNodeForAllMatches(type.getHandleIdentifier(), children);

          for (Initializer initializer : type.getInitializers()) {
            createNodeForAllMatches(initializer.getHandleIdentifier(), children);
          }

          for (Field field : type.getFields()) {
            createNodeForAllMatches(field.getHandleIdentifier(), children);
          }

          final List<Node> typeNodes =
              type.getTypes()
                  .stream()
                  .map(subType -> nodeFactory.create(subType, compilationUnit, classFile, matches))
                  .collect(Collectors.toList());
          children.addAll(typeNodes);

          final List<Node> methodNodes =
              type.getMethods()
                  .stream()
                  .map(method -> nodeFactory.create(method, matches, compilationUnit, classFile))
                  .collect(Collectors.toList());
          children.addAll(methodNodes);

          Collections.sort(children, new NodeComparator());
          callback.onSuccess(children);
        });
  }

  private void createNodeForAllMatches(String id, List<Node> list) {
    if (matches.containsKey(id)) {
      for (Match match : matches.get(id)) {
        list.add(nodeFactory.create(match, compilationUnit, classFile));
      }
    }
  }

  /**
   * Collect all matches for this type node.
   *
   * @return the list of matches.
   */
  public List<Match> getMatches() {
    List<Match> matches = new ArrayList<>();
    if (compilationUnit != null && type.isPrimary()) {
      for (ImportDeclaration importDeclaration : compilationUnit.getImports()) {
        addAllMatches(importDeclaration.getHandleIdentifier(), matches);
      }
    }
    addAllMatches(type.getHandleIdentifier(), matches);

    for (Initializer initializer : type.getInitializers()) {
      addAllMatches(initializer.getHandleIdentifier(), matches);
    }

    for (Field field : type.getFields()) {
      addAllMatches(field.getHandleIdentifier(), matches);
    }

    return matches;
  }

  private void addAllMatches(String id, List<Match> matches) {
    if (this.matches.containsKey(id)) {
      matches.addAll(this.matches.get(id));
    }
  }

  @Override
  public void updatePresentation(@NotNull NodePresentation presentation) {
    presentation.setPresentableText(type.getLabel());
    int flags = type.getFlags();
    SVGResource icon;
    if (Flags.isInterface(flags)) {
      icon = resources.interfaceItem();
    } else if (Flags.isEnum(flags)) {
      icon = resources.enumItem();
    } else if (Flags.isAnnotation(flags)) {
      icon = resources.annotationItem();
    } else {
      icon = resources.javaFile();
    }
    presentation.setPresentableIcon(icon);
  }

  @Override
  public String getName() {
    return type.getElementName();
  }

  @Override
  public boolean isLeaf() {
    return false;
  }
}
