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

import org.eclipse.che.ide.ui.tree.TreeNodeElement;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Dmitry Shnurenko
 */
@RunWith(MockitoJUnitRunner.class)
public class MachineDataAdapterTest {

    private static final String SOME_TEXT = "someText";

    @Mock
    private MachineTreeNode                  node1;
    @Mock
    private MachineTreeNode                  node2;
    @Mock
    private TreeNodeElement<MachineTreeNode> renderedNode;

    @InjectMocks
    private MachineDataAdapter adapter;

    @Test
    public void nodesShouldBeCompared() {
        when(node1.getId()).thenReturn("a");
        when(node2.getId()).thenReturn("b");

        assertThat(adapter.compare(node1, node2) == -1, is(true));

        when(node1.getId()).thenReturn("b");
        when(node2.getId()).thenReturn("a");

        assertThat(adapter.compare(node1, node2) == 1, is(true));

        when(node1.getId()).thenReturn("a");
        when(node2.getId()).thenReturn("a");

        assertThat(adapter.compare(node1, node2) == 0, is(true));
    }

    @Test
    public void trueValueShouldBeReturnedWhenChildrenExist() {
        when(node1.getChildren()).thenReturn(Arrays.asList(node1));

        assertThat(adapter.hasChildren(node1), is(true));
    }

    @Test
    public void falseShouldBeReturnedWhenListIsEmptyOrChildrenDoNotExist() {
        //noinspection unchecked
        when(node1.getChildren()).thenReturn(Collections.EMPTY_LIST);

        assertThat(adapter.hasChildren(node1), is(false));

        when(node1.getChildren()).thenReturn(null);

        assertThat(adapter.hasChildren(node1), is(false));
    }

    @Test
    public void emptyArrayShouldBeReturnedWhenChildrenNotExist() {
        when(node1.getChildren()).thenReturn(null);

        List<MachineTreeNode> nodes = adapter.getChildren(node1);

        assertThat(nodes.isEmpty(), is(true));
    }

    @Test
    public void arrayWithNodesShouldBeReturned() {
        when(node1.getChildren()).thenReturn(Arrays.asList(node1, node2));

        List<MachineTreeNode> nodes = adapter.getChildren(node1);

        assertThat(nodes.size(), equalTo(2));
    }

    @Test
    public void nodeIdShouldBeReturned() {
        adapter.getNodeId(node1);

        verify(node1).getId();
    }

    @Test
    public void nodeNameShouldBeReturned() {
        adapter.getNodeName(node1);

        verify(node1).getName();
    }

    @Test
    public void nodeParentShouldBeReturned() {
        adapter.getParent(node1);

        verify(node1).getParent();
    }

    @Test
    public void renderedTreeNodeShouldBeReturned() {
        adapter.getRenderedTreeNode(node1);

        verify(node1).getTreeNodeElement();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void unsupportedOperationExceptionShouldBeThrownWhenWeTrySetNodeName() {
        adapter.setNodeName(node1, SOME_TEXT);
    }

    @Test
    public void renderedTreeNodeShouldBeSet() {
        adapter.setRenderedTreeNode(node1, renderedNode);

        verify(node1).setTreeNodeElement(renderedNode);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void unsupportedOperationExceptionShouldBeThrownWhenWeTryDragAndDropElement() {
        adapter.getDragDropTarget(node1);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void unsupportedOperationExceptionShouldBeThrownWhenWeTryGetNodePath() {
        adapter.getNodePath(node1);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void unsupportedOperationExceptionShouldBeThrownWhenWeTryGetNodeByPath() {
        //noinspection unchecked
        List<String> relativeNodePath = mock(List.class);
        adapter.getNodeByPath(node1, relativeNodePath);
    }
}