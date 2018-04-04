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
package org.eclipse.che.ide.ext.java.client.navigation.node;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gwtmockito.GwtMockitoTestRunner;
import org.eclipse.che.ide.ext.java.client.JavaResources;
import org.eclipse.che.ide.ext.java.client.navigation.filestructure.FileStructurePresenter;
import org.eclipse.che.ide.ext.java.shared.dto.model.Field;
import org.eclipse.che.ide.ui.smartTree.presentation.NodePresentation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.vectomatic.dom.svg.ui.SVGResource;

/** @author Valeriy Svydenko */
@RunWith(GwtMockitoTestRunner.class)
public class FieldNodeTest {
  private static final String BINARY_CLASS_PATH = "path.class";
  private static final String SOURCE_CLASS_PATH = "path/class.java";
  private static final String NAME = "name";

  @Mock private JavaResources resources;
  @Mock private FileStructurePresenter fileStructurePresenter;
  @Mock private Field field;
  @Mock private NodePresentation presentation;
  @Mock private SVGResource svgResource;

  private FieldNode fieldNode;

  @Before
  public void setUp() throws Exception {
    when(field.getRootPath()).thenReturn(BINARY_CLASS_PATH);
    when(field.getFlags()).thenReturn(1);
    when(resources.publicMethod()).thenReturn(svgResource);
    when(field.getElementName()).thenReturn(NAME);

    fieldNode = new FieldNode(resources, field, true, false, fileStructurePresenter);
  }

  @Test
  public void fieldDoesNotHaveChildren() throws Exception {
    assertNull(fieldNode.getChildrenImpl());
  }

  @Test
  public void presentationNameShouldBeUpdatedIfFieldIsFromBinary() throws Exception {
    when(field.isBinary()).thenReturn(true);
    when(field.getRootPath()).thenReturn(BINARY_CLASS_PATH);

    fieldNode.updatePresentation(presentation);

    verify(presentation).setPresentableText("name -> class");
    verify(presentation).setPresentableIcon(svgResource);
  }

  @Test
  public void presentationNameShouldBeUpdatedIfFieldIsNotFromBinary() throws Exception {
    when(field.isBinary()).thenReturn(false);
    when(field.getRootPath()).thenReturn(SOURCE_CLASS_PATH);

    fieldNode.updatePresentation(presentation);

    verify(presentation).setPresentableText("name -> class");
    verify(presentation).setPresentableIcon(svgResource);
  }

  @Test
  public void presentationNameShouldBeUpdatedIfInheritedMemberHide() throws Exception {
    fieldNode = new FieldNode(resources, field, false, false, fileStructurePresenter);

    fieldNode.updatePresentation(presentation);

    verify(presentation).setPresentableText("name");
    verify(presentation).setPresentableIcon(svgResource);
  }

  @Test
  public void nameShouldBeReturn() throws Exception {
    fieldNode.getName();

    verify(field).getElementName();
    assertEquals(fieldNode.getName(), NAME);
  }

  @Test
  public void fieldIsLeaf() throws Exception {
    assertTrue(fieldNode.isLeaf());
  }

  @Test
  public void actionShouldBePerformed() throws Exception {
    fieldNode.actionPerformed();

    verify(fileStructurePresenter).actionPerformed(field);
  }
}
