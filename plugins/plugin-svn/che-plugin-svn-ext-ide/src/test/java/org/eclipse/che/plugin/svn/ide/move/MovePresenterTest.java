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
package org.eclipse.che.plugin.svn.ide.move;

import com.google.gwt.user.client.rpc.AsyncCallback;

import org.eclipse.che.ide.api.project.tree.TreeNode;
import org.eclipse.che.ide.api.project.tree.generic.FileNode;
import org.eclipse.che.ide.api.project.tree.generic.FolderNode;
import org.eclipse.che.ide.api.project.tree.generic.ProjectNode;
import org.eclipse.che.ide.api.project.tree.generic.StorableNode;
import org.eclipse.che.ide.api.selection.Selection;
import org.eclipse.che.plugin.svn.ide.common.BaseSubversionPresenterTest;
import org.eclipse.che.plugin.svn.ide.common.filteredtree.FilteredTreeStructure;
import org.eclipse.che.plugin.svn.ide.common.filteredtree.FilteredTreeStructureProvider;
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
 * Unit tests for {@link org.eclipse.che.plugin.svn.ide.move.MovePresenter}.
 *
 * @author Vladyslav Zhukovskyi
 */
public class MovePresenterTest extends BaseSubversionPresenterTest {
    @Captor
    private ArgumentCaptor<AsyncCallback<List<TreeNode<?>>>> asyncRequestCallbackStatusCaptor;

    private MovePresenter presenter;

    @Mock
    MoveView view;

    @Mock
    FilteredTreeStructureProvider treeStructureProvider;

    @Mock
    FilteredTreeStructure filteredTreeStructure;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        presenter =
                new MovePresenter(appContext, subversionOutputConsoleFactory, consolesPanelPresenter, projectExplorerPart, view,
                                  treeStructureProvider, notificationManager, service, dtoUnmarshallerFactory, constants);
    }

    @Test
    public void testMoveViewShouldBeShowed() throws Exception {
        final Selection selection = mock(Selection.class);
        final List<StorableNode> allItems = new ArrayList<>();
        final FileNode fileNode = mock(FileNode.class);
        final FolderNode folderNode = mock(FolderNode.class);

        when(treeStructureProvider.get()).thenReturn(filteredTreeStructure);
        when(projectExplorerPart.getSelection()).thenReturn(selection);
        when(selection.isEmpty()).thenReturn(false);
        when(selection.getAllElements()).thenReturn(allItems);

        allItems.add(fileNode);
        allItems.add(folderNode);

        presenter.showMove();

        verify(view).onShow(false);
    }

    @Test
    public void testMoveViewShouldSetProjectNode() throws Exception {
        final Selection selection = mock(Selection.class);
        final List<StorableNode> allItems = new ArrayList<>();
        final FileNode fileNode = mock(FileNode.class);
        final FolderNode folderNode = mock(FolderNode.class);

        when(treeStructureProvider.get()).thenReturn(filteredTreeStructure);
        when(projectExplorerPart.getSelection()).thenReturn(selection);
        when(selection.isEmpty()).thenReturn(false);
        when(selection.getAllElements()).thenReturn(allItems);

        allItems.add(fileNode);
        allItems.add(folderNode);

        presenter.showMove();

        List<TreeNode<?>> children = new ArrayList<>();
        children.add(mock(ProjectNode.class));

        verify(filteredTreeStructure).getRootNodes(asyncRequestCallbackStatusCaptor.capture());
        AsyncCallback<List<TreeNode<?>>> requestCallback = asyncRequestCallbackStatusCaptor.getValue();
        GwtReflectionUtils.callPrivateMethod(requestCallback, "onSuccess", children);

        verify(view).setProjectNodes(eq(children));
    }
}
