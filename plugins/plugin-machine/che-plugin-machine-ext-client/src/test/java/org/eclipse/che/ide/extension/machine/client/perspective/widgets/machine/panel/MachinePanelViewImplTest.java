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
package org.eclipse.che.ide.extension.machine.client.perspective.widgets.machine.panel;

import com.google.gwtmockito.GwtMockitoTestRunner;

import org.eclipse.che.ide.api.machine.MachineEntity;
import org.eclipse.che.ide.api.parts.PartStackUIResources;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.machine.panel.MachinePanelView.ActionDelegate;
import org.eclipse.che.ide.ui.tree.Tree;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Dmitry Shnurenko
 */
@RunWith(GwtMockitoTestRunner.class)
public class MachinePanelViewImplTest {

    private static final String SOME_TEXT = "someText";

    @Mock(answer = RETURNS_DEEP_STUBS)
    private PartStackUIResources          partStackResources;
    @Mock
    private org.eclipse.che.ide.Resources resources;
    @Mock
    private MachineDataAdapter            adapter;
    @Mock
    private MachineTreeRenderer           renderer;
    @Mock
    private ActionDelegate                delegate;
    @Mock
    private Tree.Css                      css;
    @Mock
    private MachineTreeNode               treeNode;
    @Mock
    private MachineEntity                 machineState;

    private MachinePanelViewImpl view;

    @Before
    public void setUp() {
        when(partStackResources.partStackCss().ideBasePartToolbar()).thenReturn(SOME_TEXT);
        when(partStackResources.partStackCss().ideBasePartTitleLabel()).thenReturn(SOME_TEXT);

        when(resources.treeCss()).thenReturn(css);

        view = new MachinePanelViewImpl(resources, partStackResources, adapter, renderer);

        view.setDelegate(delegate);
    }

    @Test
    public void nodeShouldBeSelected() {
        when(treeNode.getData()).thenReturn(machineState);

        view.selectNode(treeNode);

        verify(treeNode).getData();
        verify(delegate).onMachineSelected(machineState);

    }

    @Test
    public void nodeShouldNotBeSelected() {
        view.selectNode(null);

        verify(treeNode, never()).getData();
        verify(delegate, never()).onMachineSelected(machineState);
    }
}
