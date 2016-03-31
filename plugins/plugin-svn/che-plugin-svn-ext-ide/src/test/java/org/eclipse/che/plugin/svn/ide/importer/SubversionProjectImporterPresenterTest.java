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
package org.eclipse.che.plugin.svn.ide.importer;

import com.google.gwt.user.client.ui.AcceptsOneWidget;

import org.eclipse.che.api.project.shared.dto.ProjectImporterDescriptor;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.SourceStorageDto;
import org.eclipse.che.ide.api.wizard.Wizard;
import org.eclipse.che.plugin.svn.ide.SubversionExtensionLocalizationConstants;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Map;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link org.eclipse.che.plugin.svn.ide.importer.SubversionProjectImporterPresenter}.
 */
@RunWith(MockitoJUnitRunner.class)
public class SubversionProjectImporterPresenterTest {
    @Mock
    private ProjectConfigDto                         dataObject;
    @Mock
    private SourceStorageDto                         sourceStorage;
    @Mock
    private Map<String, String>                      parameters;
    @Mock
    private SubversionProjectImporterView            view;
    @Mock
    private SubversionExtensionLocalizationConstants constants;
    @InjectMocks
    private SubversionProjectImporterPresenter       presenter;
    @Mock
    private Wizard.UpdateDelegate                    updateDelegate;

    /**
     * Setup the tests.
     *
     * @throws Exception if anything goes wrong
     */
    @Before
    public void setUp() throws Exception {
        when(sourceStorage.getParameters()).thenReturn(parameters);
        when(dataObject.getSource()).thenReturn(sourceStorage);
        when(view.getProjectRelativePath()).thenReturn("");

        presenter.setUpdateDelegate(updateDelegate);
        presenter.init(dataObject);
    }

    /**
     * Test for {@link SubversionProjectImporterPresenter#onProjectNameChanged()} with an empty name.
     *
     * @throws Exception if anything goes wrong
     */
    @Test
    public void testEmptyProjectName() throws Exception {
        final String projectName = "";

        when(view.getProjectName()).thenReturn(projectName);

        presenter.onProjectNameChanged();

        verify(dataObject).setName(eq(projectName));
        verify(updateDelegate).updateControls();
        verify(view).setNameErrorVisibility(eq(true));
    }

    /**
     * Test for {@link SubversionProjectImporterPresenter#onProjectNameChanged()} with an invalid name.
     *
     * @throws Exception if anything goes wrong
     */
    @Test
    public void testInvalidProjectName() throws Exception {
        final String projectName = "+subversion+";

        when(view.getProjectName()).thenReturn(projectName);

        presenter.onProjectNameChanged();

        verify(dataObject).setName(eq(projectName));
        verify(updateDelegate).updateControls();
        verify(view).setNameErrorVisibility(eq(true));
    }

    /**
     * Test for {@link SubversionProjectImporterPresenter#onProjectNameChanged()} with a valid name.
     *
     * @throws Exception if anything goes wrong
     */
    @Test
    public void testValidProjectName() throws Exception {
        final String projectName = "subversion";

        when(view.getProjectName()).thenReturn(projectName);

        presenter.onProjectNameChanged();

        verify(dataObject).setName(eq(projectName));
        verify(updateDelegate).updateControls();
        verify(view).setNameErrorVisibility(eq(false));
    }

    /**
     * Test for {@link SubversionProjectImporterPresenter#onProjectUrlChanged()} with an non-URL value.
     *
     * @throws Exception if anything goes wrong
     */
    @Test
    public void testNonUrlProjectUrl() throws Exception {
        final String projectUrl = "subversion";

        when(view.getProjectUrl()).thenReturn(projectUrl);

        presenter.onProjectUrlChanged();

        verify(view).setProjectName(eq(projectUrl));
        verify(view, never()).setNameErrorVisibility(anyBoolean());
        verify(dataObject.getSource()).setLocation(projectUrl + "/");
        verify(updateDelegate, times(1)).updateControls();
    }

    /**
     * Test for {@link SubversionProjectImporterPresenter#onProjectUrlChanged()} with a valid URL value.
     *
     * @throws Exception if anything goes wrong
     */
    @Test
    public void testValidProjectUrl() throws Exception {
        final String projectUrl = "https://svn.apache.org/repos/asf/subversion/trunk/";

        when(view.getProjectUrl()).thenReturn(projectUrl);

        presenter.onProjectUrlChanged();

        verify(view).setProjectName(eq("trunk"));
        verify(dataObject.getSource()).setLocation(projectUrl);
        verify(updateDelegate).updateControls();
    }

    /**
     * Test for {@link SubversionProjectImporterPresenter#onProjectDescriptionChanged()}.
     *
     * @throws Exception if anything goes wrong
     */
    @Test
    public void testProjectDescription() throws Exception {
        final String description = "Some description.";

        when(view.getProjectDescription()).thenReturn(description);

        presenter.onProjectDescriptionChanged();

        verify(dataObject).setDescription(eq(description));
        verify(view, never()).setNameErrorVisibility(anyBoolean());
        verify(updateDelegate).updateControls();
    }

    /**
     * Test for {@link SubversionProjectImporterPresenter#go(AcceptsOneWidget)}.
     *
     * @throws Exception if anything goes wrong
     */
    @Test
    public void testGo() throws Exception {
        final String importerDescription = "Some description.";
        final AcceptsOneWidget container = mock(AcceptsOneWidget.class);
        final ProjectImporterDescriptor projectImporter = mock(ProjectImporterDescriptor.class);

        when(projectImporter.getDescription()).thenReturn(importerDescription);

        presenter.go(container);

        verify(container).setWidget(eq(view));
        verify(view).setProjectName(anyString());
        verify(view).setProjectDescription(anyString());
        verify(view).setProjectUrl(anyString());
        verify(view).setUrlTextBoxFocused();
    }

}
