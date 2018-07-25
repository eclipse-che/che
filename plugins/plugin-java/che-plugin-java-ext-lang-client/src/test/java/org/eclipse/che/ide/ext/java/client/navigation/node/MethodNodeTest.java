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
package org.eclipse.che.ide.ext.java.client.navigation.node;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gwtmockito.GwtMockitoTestRunner;
import org.eclipse.che.ide.ext.java.client.JavaResources;
import org.eclipse.che.ide.ext.java.client.navigation.filestructure.FileStructurePresenter;
import org.eclipse.che.ide.ext.java.shared.dto.model.Method;
import org.eclipse.che.ide.ui.smartTree.presentation.NodePresentation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.vectomatic.dom.svg.ui.SVGResource;

/** @author Valeriy Svydenko */
@RunWith(GwtMockitoTestRunner.class)
public class MethodNodeTest {
  private static final String BINARY_CLASS_PATH = "path.class";
  private static final String SOURCE_CLASS_PATH = "path/class.java";
  private static final String NAME = "method_name()";
  private static final String RETURN_TYPE = "returned";

  @Mock private JavaResources resources;
  @Mock private FileStructurePresenter fileStructurePresenter;
  @Mock private Method method;
  @Mock private NodePresentation presentation;
  @Mock private SVGResource svgResource;

  private MethodNode methodNode;

  @Before
  public void setUp() throws Exception {
    when(method.getReturnType()).thenReturn(RETURN_TYPE);
    when(method.getRootPath()).thenReturn(BINARY_CLASS_PATH);
    when(method.getFlags()).thenReturn(1);
    when(resources.publicMethod()).thenReturn(svgResource);
    when(method.getElementName()).thenReturn(NAME);
    when(method.getLabel()).thenReturn(NAME);

    methodNode = new MethodNode(resources, method, true, false, fileStructurePresenter);
  }

  @Test
  public void fieldDoesNotHaveChildren() throws Exception {
    assertNull(methodNode.getChildrenImpl());
  }

  @Test
  public void presentationNameShouldBeUpdatedIfFieldIsFromBinary() throws Exception {
    when(method.isBinary()).thenReturn(true);
    when(method.getRootPath()).thenReturn(BINARY_CLASS_PATH);

    methodNode.updatePresentation(presentation);

    verify(presentation).setPresentableText("method_name() : returned -> class");
    verify(presentation).setPresentableIcon(svgResource);
  }

  @Test
  public void presentationNameShouldBeUpdatedIfFieldIsNotFromBinary() throws Exception {
    when(method.isBinary()).thenReturn(false);
    when(method.getRootPath()).thenReturn(SOURCE_CLASS_PATH);

    methodNode.updatePresentation(presentation);

    verify(presentation).setPresentableText("method_name() : returned -> class");
    verify(presentation).setPresentableIcon(svgResource);
  }

  @Test
  public void presentationNameShouldBeUpdatedIfInheritedMemberHide() throws Exception {
    methodNode = new MethodNode(resources, method, false, false, fileStructurePresenter);

    methodNode.updatePresentation(presentation);

    verify(presentation).setPresentableText(NAME + " : " + RETURN_TYPE);
    verify(presentation).setPresentableIcon(svgResource);
  }

  @Test
  public void nameShouldBeReturn() throws Exception {
    methodNode.getName();

    verify(method).getElementName();
    assertEquals(methodNode.getName(), NAME);
  }

  @Test
  public void fieldIsLeaf() throws Exception {
    assertTrue(methodNode.isLeaf());
  }

  @Test
  public void actionShouldBePerformed() throws Exception {
    methodNode.actionPerformed();

    verify(fileStructurePresenter).actionPerformed(method);
  }
}
