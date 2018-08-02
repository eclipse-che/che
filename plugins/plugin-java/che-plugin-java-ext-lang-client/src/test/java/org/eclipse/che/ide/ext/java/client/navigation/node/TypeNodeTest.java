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
package org.eclipse.che.ide.ext.java.client.navigation.node;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gwtmockito.GwtMockitoTestRunner;
import java.util.List;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper;
import org.eclipse.che.ide.ext.java.client.JavaResources;
import org.eclipse.che.ide.ext.java.client.navigation.factory.NodeFactory;
import org.eclipse.che.ide.ext.java.client.navigation.filestructure.FileStructurePresenter;
import org.eclipse.che.ide.ext.java.shared.dto.model.CompilationUnit;
import org.eclipse.che.ide.ext.java.shared.dto.model.Type;
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.eclipse.che.ide.ui.smartTree.presentation.NodePresentation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.vectomatic.dom.svg.ui.SVGResource;

/** @author Valeriy Svydenko */
@RunWith(GwtMockitoTestRunner.class)
public class TypeNodeTest {
  private static final String BINARY_CLASS_PATH = "path.class";
  private static final String SOURCE_CLASS_PATH = "path/class.java";
  private static final String NAME = "name";

  @Mock private JavaResources resources;
  @Mock private FileStructurePresenter fileStructurePresenter;
  @Mock private CompilationUnit compilationUnit;
  @Mock private Type type;
  @Mock private NodePresentation presentation;
  @Mock private SVGResource svgResource;
  @Mock private NodeFactory nodeFactory;

  @Mock private Promise<List<Node>> promise;

  @Captor private ArgumentCaptor<AsyncPromiseHelper.RequestCall<List<Node>>> callArgumentCaptor;

  private TypeNode typeNode;

  @Before
  public void setUp() throws Exception {
    when(type.getRootPath()).thenReturn(BINARY_CLASS_PATH);
    when(type.getFlags()).thenReturn(1);
    when(resources.javaFile()).thenReturn(svgResource);
    when(type.getElementName()).thenReturn(NAME);
    when(type.getLabel()).thenReturn(NAME);

    typeNode =
        new TypeNode(
            resources, nodeFactory, fileStructurePresenter, type, compilationUnit, true, false);
  }

  @Test
  public void presentationNameShouldBeUpdatedIfFieldIsFromBinary() throws Exception {
    when(type.isBinary()).thenReturn(true);
    when(type.getRootPath()).thenReturn(BINARY_CLASS_PATH);

    typeNode.updatePresentation(presentation);

    verify(presentation).setPresentableText("name -> class");
    verify(presentation).setPresentableIcon(svgResource);
  }

  @Test
  public void presentationNameShouldBeUpdatedIfFieldIsNotFromBinary() throws Exception {
    when(type.isBinary()).thenReturn(false);
    when(type.getRootPath()).thenReturn(SOURCE_CLASS_PATH);

    typeNode.updatePresentation(presentation);

    verify(presentation).setPresentableText("name -> class");
    verify(presentation).setPresentableIcon(svgResource);
  }

  @Test
  public void presentationNameShouldBeUpdatedIfInheritedMemberHide() throws Exception {
    typeNode =
        new TypeNode(
            resources, nodeFactory, fileStructurePresenter, type, compilationUnit, false, false);

    typeNode.updatePresentation(presentation);

    verify(presentation).setPresentableText("name");
    verify(presentation).setPresentableIcon(svgResource);
  }

  @Test
  public void nameShouldBeReturn() throws Exception {
    typeNode.getName();

    verify(type).getElementName();
    assertEquals(typeNode.getName(), NAME);
  }

  @Test
  public void fieldIsLeaf() throws Exception {
    assertTrue(typeNode.isLeaf());
  }

  @Test
  public void actionShouldBePerformed() throws Exception {
    typeNode.actionPerformed();

    verify(fileStructurePresenter).actionPerformed(type);
  }
}
