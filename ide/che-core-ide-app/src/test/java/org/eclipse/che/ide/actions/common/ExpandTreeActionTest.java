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
package org.eclipse.che.ide.actions.common;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gwtmockito.GwtMockitoTestRunner;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.Presentation;
import org.eclipse.che.ide.ui.smartTree.data.TreeExpander;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

/**
 * Unit tests for the {@link ExpandTreeAction}.
 *
 * @author Vlad Zhukovskyi
 */
@RunWith(GwtMockitoTestRunner.class)
public class ExpandTreeActionTest {

  @Mock TreeExpander treeExpander;
  @Mock ActionEvent actionEvent;
  @Mock Presentation presentation;

  private ExpandTreeAction action;

  @Before
  public void setUp() throws Exception {
    action =
        new ExpandTreeAction() {
          @Override
          public TreeExpander getTreeExpander() {
            return treeExpander;
          }
        };

    when(actionEvent.getPresentation()).thenReturn(presentation);
  }

  @Test
  public void testShouldNotFireTreeCollapse() throws Exception {
    when(treeExpander.isExpandEnabled()).thenReturn(false);

    action.actionPerformed(actionEvent);

    verify(treeExpander, never()).expandTree();
  }

  @Test
  public void testShouldFireTreeCollapse() throws Exception {
    when(treeExpander.isExpandEnabled()).thenReturn(true);

    action.actionPerformed(actionEvent);

    verify(treeExpander).expandTree();
  }

  @Test
  public void testShouldUpdatePresentationBasedOnStatus() throws Exception {
    when(treeExpander.isExpandEnabled()).thenReturn(true);

    action.update(actionEvent);

    verify(presentation).setEnabledAndVisible(eq(true));
  }
}
