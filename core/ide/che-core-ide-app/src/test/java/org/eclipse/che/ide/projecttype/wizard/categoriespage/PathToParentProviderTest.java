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
package org.eclipse.che.ide.projecttype.wizard.categoriespage;

import org.eclipse.che.api.core.model.project.ProjectConfig;
import org.eclipse.che.ide.api.selection.Selection;
import org.eclipse.che.ide.api.selection.SelectionAgent;
import org.eclipse.che.ide.project.node.FolderReferenceNode;
import org.eclipse.che.ide.project.node.ProjectNode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;

import static org.eclipse.che.ide.api.project.type.wizard.ProjectWizardMode.CREATE;
import static org.eclipse.che.ide.api.project.type.wizard.ProjectWizardMode.UPDATE;
import static org.eclipse.che.ide.projecttype.wizard.categoriespage.PathToParentProvider.DEFAULT_PARENT_DIRECTORY;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

/**
 * @author Dmitry Shnurenko
 */
@RunWith(MockitoJUnitRunner.class)
public class PathToParentProviderTest {
    private static final String PATH_TO_PROJECT = "/parent/project";
    private static final String PATH_TO_PARENT  = "/parent";

    @Mock
    private SelectionAgent selectionAgent;

    //additional mocks
    @Mock
    private ProjectConfig       projectConfig;
    @Mock
    private ProjectConfig       parentConfig;
    @Mock
    private ProjectNode         projectNode;
    @Mock
    private ProjectNode         parentNode;
    @Mock
    private Selection           selection;
    @Mock
    private FolderReferenceNode folderNode;

    @InjectMocks
    private PathToParentProvider provider;

    @Before
    public void setUp() {
        when(projectConfig.getPath()).thenReturn(PATH_TO_PROJECT);
        //noinspection unchecked
        when(selectionAgent.getSelection()).thenReturn(selection);

        when(selection.getHeadElement()).thenReturn(projectNode);
        when(projectNode.getParent()).thenReturn(parentNode);
        when(parentNode.getStorablePath()).thenReturn(PATH_TO_PARENT);
    }

    @Test
    public void pathToParentShouldBeReturnedFromProjectConfigInUpdateMode() {
        String pathToParent = provider.getPathToParent(UPDATE, projectConfig);

        assertThat(pathToParent, is(equalTo("/parent")));
    }

    @Test
    public void defaultPathToParentShouldBeReturnedWhenPathIsNullInUpdateMode() {
        when(projectConfig.getPath()).thenReturn(null);

        String pathToParent = provider.getPathToParent(UPDATE, projectConfig);

        assertThat(pathToParent, is(equalTo(DEFAULT_PARENT_DIRECTORY)));
    }

    @Test
    public void defaultPathToParentShouldBeReturnedWhenPathIsEmptyInUpdateMode() {
        when(projectConfig.getPath()).thenReturn("");

        String pathToParent = provider.getPathToParent(UPDATE, projectConfig);

        assertThat(pathToParent, is(equalTo(DEFAULT_PARENT_DIRECTORY)));
    }

    @Test
    public void defaultPathToParentShouldBeReturnedWhenPathGetsFromParentInUpdateMode() {
        when(projectConfig.getPath()).thenReturn("/parent");

        String pathToParent = provider.getPathToParent(UPDATE, projectConfig);

        assertThat(pathToParent, is(equalTo(DEFAULT_PARENT_DIRECTORY)));
    }

    @Test
    public void defaultPathToParentShouldBeReturnedWhenSelectionIsNullInCreateMode() {
        when(selectionAgent.getSelection()).thenReturn(null);

        String pathToParent = provider.getPathToParent(CREATE, projectConfig);

        assertThat(pathToParent, is(equalTo(DEFAULT_PARENT_DIRECTORY)));
    }

    @Test
    public void defaultPathToParentShouldBeReturnedWhenSelectionIsEmptyInCreateMode() {
        when(selection.isEmpty()).thenReturn(true);

        String pathToParent = provider.getPathToParent(CREATE, projectConfig);

        assertThat(pathToParent, is(equalTo(DEFAULT_PARENT_DIRECTORY)));
    }

    @Test
    public void defaultPathToParentShouldBeReturnedWhenSelectionIsMultiSelectionInCreateMode() {
        when(selection.getAllElements()).thenReturn(Arrays.asList("one", "two"));

        String pathToParent = provider.getPathToParent(CREATE, projectConfig);

        assertThat(pathToParent, is(equalTo(DEFAULT_PARENT_DIRECTORY)));
    }

    @Test
    public void pathToParentShouldBeReturnedFromProjectNodeInCreateMode() {
        when(selection.getHeadElement()).thenReturn(projectNode);

        String pathToParent = provider.getPathToParent(CREATE, projectConfig);

        assertThat(pathToParent, is(equalTo("/parent/")));
    }
}
