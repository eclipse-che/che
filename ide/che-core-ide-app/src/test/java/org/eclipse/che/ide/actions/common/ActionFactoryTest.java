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

import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.data.tree.TreeExpander;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static org.junit.Assert.*;

/**
 * Unit tests for the {@link ActionFactory}.
 *
 * @author Vlad Zhukovskyi
 */
@RunWith(GwtMockitoTestRunner.class)
public class ActionFactoryTest {

    @Mock
    TreeExpander treeExpander;

    private ActionFactory actionFactory;

    @Before
    public void setUp() throws Exception {
        actionFactory = new ActionFactory();
    }

    @Test
    public void testShouldCreateTreeExpandAction() throws Exception {
        final Action action = actionFactory.createExpandTreeAction(treeExpander);

        assertTrue(action instanceof ExpandTreeAction);
        assertSame(((ExpandTreeAction)action).getTreeExpander(), treeExpander);
    }

    @Test
    public void testShouldCreateTreeCollapseAction() throws Exception {
        final Action action = actionFactory.createCollapseTreeAction(treeExpander);

        assertTrue(action instanceof CollapseTreeAction);
        assertSame(((CollapseTreeAction)action).getTreeExpander(), treeExpander);
    }
}