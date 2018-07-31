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

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gwtmockito.GwtMockitoTestRunner;
import org.eclipse.che.ide.ext.java.client.JavaResources;
import org.eclipse.che.ide.ext.java.client.navigation.filestructure.FileStructurePresenter;
import org.eclipse.che.ide.ext.java.shared.dto.model.Initializer;
import org.eclipse.che.ide.ui.smartTree.presentation.NodePresentation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.vectomatic.dom.svg.ui.SVGResource;

/** @author Valeriy Svydenko */
@RunWith(GwtMockitoTestRunner.class)
public class InitializerNodeTest {
  @Mock private JavaResources resources;
  @Mock private FileStructurePresenter fileStructurePresenter;
  @Mock private Initializer initializer;
  @Mock private NodePresentation presentation;
  @Mock private SVGResource svgResource;

  private InitializerNode initializerNode;

  @Before
  public void setUp() throws Exception {
    when(initializer.getFlags()).thenReturn(1);
    when(resources.publicMethod()).thenReturn(svgResource);

    initializerNode =
        new InitializerNode(resources, initializer, true, false, fileStructurePresenter);
  }

  @Test
  public void fieldDoesNotHaveChildren() throws Exception {
    assertNull(initializerNode.getChildrenImpl());
  }

  @Test
  public void presentationNameShouldBeUpdatedIfInheritedMemberHide() throws Exception {
    initializerNode =
        new InitializerNode(resources, initializer, false, false, fileStructurePresenter);

    initializerNode.updatePresentation(presentation);

    verify(presentation).setPresentableText(null);
    verify(presentation).setPresentableIcon(svgResource);
  }

  @Test
  public void nameShouldBeReturn() throws Exception {
    assertNull(initializerNode.getName());
  }

  @Test
  public void fieldIsLeaf() throws Exception {
    assertTrue(initializerNode.isLeaf());
  }

  @Test
  public void actionShouldBePerformed() throws Exception {
    initializerNode.actionPerformed();

    verify(fileStructurePresenter).actionPerformed(initializer);
  }
}
