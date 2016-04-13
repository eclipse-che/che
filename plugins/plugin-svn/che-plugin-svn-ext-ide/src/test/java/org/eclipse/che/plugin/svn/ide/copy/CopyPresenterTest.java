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
package org.eclipse.che.plugin.svn.ide.copy;

import com.google.gwt.user.client.rpc.AsyncCallback;

import org.eclipse.che.ide.api.project.tree.TreeNode;
import org.eclipse.che.plugin.svn.ide.common.BaseSubversionPresenterTest;
import org.eclipse.che.plugin.svn.ide.common.filteredtree.FilteredTreeStructure;
import org.eclipse.che.plugin.svn.ide.common.filteredtree.FilteredTreeStructureProvider;
import org.eclipse.che.ide.project.node.FileReferenceNode;
import org.eclipse.che.ide.project.node.ProjectNode;
import org.eclipse.che.ide.project.node.ResourceBasedNode;
import org.eclipse.che.test.GwtReflectionUtils;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link org.eclipse.che.plugin.svn.ide.copy.CopyPresenter}.
 *
 * @author Vladyslav Zhukovskyi
 */
public class CopyPresenterTest extends BaseSubversionPresenterTest {
    @Captor
    private ArgumentCaptor<AsyncCallback<List<TreeNode<?>>>> asyncRequestCallbackStatusCaptor;

    private CopyPresenter presenter;

    @Mock
    CopyView copyView;

    @Mock
    FilteredTreeStructureProvider treeStructureProvider;

    @Mock
    FilteredTreeStructure filteredTreeStructure;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        presenter =
                new CopyPresenter(appContext, subversionOutputConsoleFactory, consolesPanelPresenter, copyView, notificationManager,
                                  service, dtoUnmarshallerFactory, constants, projectExplorerPart, statusColors);
    }

    @Test
    public void testCopyViewShouldBeShowed() throws Exception {
        when(treeStructureProvider.get()).thenReturn(filteredTreeStructure);

        presenter.showCopy(mock(FileReferenceNode.class));

        verify(copyView).show();
    }

    //    @Test
    public void testCopyViewShouldSetProjectNode() throws Exception {
        when(treeStructureProvider.get()).thenReturn(filteredTreeStructure);

        presenter.showCopy(mock(FileReferenceNode.class));

        List<ResourceBasedNode<?>> children = new ArrayList<>();
        children.add(mock(ProjectNode.class));

        verify(filteredTreeStructure).getRootNodes(asyncRequestCallbackStatusCaptor.capture());
        AsyncCallback<List<TreeNode<?>>> requestCallback = asyncRequestCallbackStatusCaptor.getValue();
        GwtReflectionUtils.callPrivateMethod(requestCallback, "onSuccess", children);

        verify(copyView).setProjectNodes(eq(children));
    }

    @Test
    public void testEmptyTargetMessageAlertShouldAppear() throws Exception {
        when(constants.copyEmptyTarget()).thenReturn("message");

        presenter.onNewNameChanged("/foo");

        verify(copyView).showErrorMarker("message");
    }

    @Test
    public void testTargetUrlIsCorrect() throws Exception {
        when(copyView.isTargetCheckBoxSelected()).thenReturn(true);
        when(copyView.getTargetUrl()).thenReturn("http://github.com");

        presenter.onNewNameChanged("/foo");

        verify(copyView).hideErrorMarker();
    }

    @Test
    public void testTargetUrlIsWrong() throws Exception {
        when(constants.copyTargetWrongURL()).thenReturn("message");
        when(copyView.isTargetCheckBoxSelected()).thenReturn(true);
        when(copyView.getTargetUrl()).thenReturn("htp://github.com");

        presenter.onNewNameChanged("/foo");

        verify(copyView).showErrorMarker("message");
    }
}
