/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.actions.common;

import com.google.gwtmockito.GwtMockitoTestRunner;

import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.Presentation;
import org.eclipse.che.ide.api.data.tree.TreeExpander;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the {@link CollapseTreeAction}.
 *
 * @author Vlad Zhukovskyi
 */
@RunWith(GwtMockitoTestRunner.class)
public class CollapseTreeActionTest {

    @Mock
    TreeExpander treeExpander;
    @Mock
    ActionEvent actionEvent;
    @Mock
    Presentation presentation;

    private CollapseTreeAction action;

    @Before
    public void setUp() throws Exception {
        action = new CollapseTreeAction() {
            @Override
            public TreeExpander getTreeExpander() {
                return treeExpander;
            }
        };

        when(actionEvent.getPresentation()).thenReturn(presentation);
    }

    @Test
    public void testShouldNotFireTreeCollapse() throws Exception {
        when(treeExpander.isCollapseEnabled()).thenReturn(false);

        action.actionPerformed(actionEvent);

        verify(treeExpander, never()).collapseTree();
    }

    @Test
    public void testShouldFireTreeCollapse() throws Exception {
        when(treeExpander.isCollapseEnabled()).thenReturn(true);

        action.actionPerformed(actionEvent);

        verify(treeExpander).collapseTree();
    }

    @Test
    public void testShouldUpdatePresentationBasedOnStatus() throws Exception {
        when(treeExpander.isCollapseEnabled()).thenReturn(true);

        action.update(actionEvent);

        verify(presentation).setEnabledAndVisible(eq(true));
    }

}
