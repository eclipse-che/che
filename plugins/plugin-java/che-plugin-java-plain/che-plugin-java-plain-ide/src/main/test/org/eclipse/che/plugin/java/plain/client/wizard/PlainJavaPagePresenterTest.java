/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.java.plain.client.wizard;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwtmockito.GwtMockitoTestRunner;

import org.eclipse.che.ide.ui.smartTree.data.tree.Node;
import org.eclipse.che.ide.api.project.MutableProjectConfig;
import org.eclipse.che.ide.api.project.type.wizard.ProjectWizardRegistrar;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.wizard.Wizard;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.resources.tree.ResourceNode;
import org.eclipse.che.plugin.java.plain.client.wizard.selector.SelectNodePresenter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.eclipse.che.ide.ext.java.shared.Constants.SOURCE_FOLDER;
import static org.eclipse.che.plugin.java.plain.shared.PlainJavaProjectConstants.DEFAULT_SOURCE_FOLDER_VALUE;
import static org.eclipse.che.plugin.java.plain.shared.PlainJavaProjectConstants.LIBRARY_FOLDER;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Valeriy Svydenko
 */
@RunWith(GwtMockitoTestRunner.class)
public class PlainJavaPagePresenterTest {
    @Mock
    private PlainJavaPageView     view;
    @Mock
    private MutableProjectConfig  dataObject;
    @Mock
    private AcceptsOneWidget      container;
    @Mock
    private Wizard.UpdateDelegate updateDelegate;

    @Mock
    private SelectNodePresenter selectNodePresenter;

    @InjectMocks
    private PlainJavaPagePresenter page;

    private Map<String, List<String>> attributes = new HashMap<>();

    @Before
    public void setUp() throws Exception {
        Map<String, String> context = new HashMap<>();
        context.put(ProjectWizardRegistrar.WIZARD_MODE_KEY, "create");
        page.setContext(context);

        when(dataObject.getAttributes()).thenReturn(attributes);
        when(dataObject.getName()).thenReturn("name");
        when(view.getSourceFolder()).thenReturn("src");
    }

    @Test
    public void ActionDelegateShouldBeSet() throws Exception {
        verify(view).setDelegate(page);
    }

    @Test
    public void pageShouldBeInitialized() throws Exception {
        page.init(dataObject);

        assertTrue(dataObject.getAttributes().size() == 1);
        assertEquals(DEFAULT_SOURCE_FOLDER_VALUE, dataObject.getAttributes().get(SOURCE_FOLDER).get(0));
    }

    @Test
    public void pageIsNotReadyIfSourceFolderValueIsNotDefine() throws Exception {
        page.init(dataObject);

        attributes.clear();

        assertFalse(page.isCompleted());
    }

    @Test
    public void pageShouldBeCompleted() throws Exception {
        page.init(dataObject);

        assertTrue(page.isCompleted());
    }

    @Test
    public void pageShouldBeUpdated() throws Exception {
        page.setUpdateDelegate(updateDelegate);

        page.init(dataObject);
        page.go(container);

        verify(container).setWidget(view);
        verify(updateDelegate).updateControls();
        verify(view).changeBrowseBtnVisibleState(false);
        verify(view).changeSourceFolderFieldState(true);
        verify(view).setSourceFolder(DEFAULT_SOURCE_FOLDER_VALUE);
        verify(view).showSourceFolderMissingIndicator(false);
    }

    @Test
    public void pageStateShouldBeUpdateIfAttributeWasChanged() throws Exception {
        when(view.getLibraryFolder()).thenReturn("lib1,   lib2");

        page.setUpdateDelegate(updateDelegate);
        page.init(dataObject);
        page.onCoordinatesChanged();

        verify(view, times(2)).getSourceFolder();
        verify(updateDelegate).updateControls();
        verify(view).showSourceFolderMissingIndicator(false);
    }

    @Test
    public void severalSourceFoldersShouldBeSet() throws Exception {
        when(view.getLibraryFolder()).thenReturn("lib1,   lib2");
        when(view.getSourceFolder()).thenReturn("src1,   src2");

        page.setUpdateDelegate(updateDelegate);
        page.init(dataObject);
        page.onCoordinatesChanged();

        verify(view, times(2)).getSourceFolder();
        verify(updateDelegate).updateControls();
        verify(view).showSourceFolderMissingIndicator(false);

        assertEquals(2, dataObject.getAttributes().get(LIBRARY_FOLDER).size());
        assertEquals(2, dataObject.getAttributes().get(SOURCE_FOLDER).size());

        assertThat(dataObject.getAttributes().get(LIBRARY_FOLDER), hasItems("lib1", "lib2"));
        assertThat(dataObject.getAttributes().get(SOURCE_FOLDER), hasItems("src1", "src2"));
    }

    @Test
    public void showSelectWindowWhenBrowseButtonWasClicked() throws Exception {
        page.init(dataObject);
        page.onBrowseSourceButtonClicked();

        verify(selectNodePresenter).show(page, "name");
    }

    @Test
    public void selectedNodeShouldBeWithRelativePath() throws Exception {
        when(view.getLibraryFolder()).thenReturn("lib1,   lib2");
        when(view.getSourceFolder()).thenReturn("src1,   src2");
        List<Node> selectedNodes = new ArrayList<>();
        ResourceNode selectedNode1 = mock(ResourceNode.class);
        ResourceNode selectedNode2 = mock(ResourceNode.class);
        Resource resource1 = mock(Resource.class);
        Resource resource2 = mock(Resource.class);

        selectedNodes.add(selectedNode1);
        selectedNodes.add(selectedNode2);

        when(selectedNode1.getData()).thenReturn(resource1);
        when(resource1.getLocation()).thenReturn(Path.valueOf("projectName/folder1/folder2"));

        when(selectedNode2.getData()).thenReturn(resource2);
        when(resource2.getLocation()).thenReturn(Path.valueOf("projectName/folder"));

        when(dataObject.getName()).thenReturn("projectName");

        page.setUpdateDelegate(updateDelegate);
        page.init(dataObject);
        page.onNodeSelected(selectedNodes);

        verify(view).setLibraryFolder(eq("folder1/folder2,   folder"));
    }
}
