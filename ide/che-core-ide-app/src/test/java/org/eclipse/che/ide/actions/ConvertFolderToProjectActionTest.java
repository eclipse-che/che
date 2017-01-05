/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.actions;

import com.google.gwtmockito.GwtMockitoTestRunner;

import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.Presentation;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.project.MutableProjectConfig;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.projecttype.wizard.presenter.ProjectWizardPresenter;
import org.eclipse.che.ide.resource.Path;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Valeriy Svydenko
 */
@RunWith(GwtMockitoTestRunner.class)
public class ConvertFolderToProjectActionTest {

    private final static String TEXT = "to be or not to be";

    @Mock
    private CoreLocalizationConstant locale;
    @Mock
    private AppContext               appContext;
    @Mock
    private ProjectWizardPresenter   projectConfigWizard;

    @InjectMocks
    private ConvertFolderToProjectAction action;

    @Mock
    private Resource     resource;
    @Mock
    private ActionEvent  event;
    @Mock
    private Presentation presentation;

    @Captor
    private ArgumentCaptor<MutableProjectConfig> projectConfigCapture;

    @Before
    public void setUp() throws Exception {
        when(locale.actionConvertFolderToProject()).thenReturn(TEXT);
        when(locale.actionConvertFolderToProjectDescription()).thenReturn(TEXT);

        when(appContext.getResource()).thenReturn(resource);
        when(event.getPresentation()).thenReturn(presentation);

        when(resource.isFolder()).thenReturn(true);
        when(resource.getLocation()).thenReturn(Path.valueOf(TEXT));
    }

    @Test
    public void actionShouldBeInitialized() throws Exception {
        verify(locale).actionConvertFolderToProject();
        verify(locale).actionConvertFolderToProjectDescription();
    }

    @Test
    public void actionShouldBeVisibleIfFolderWasSelected() throws Exception {
        action.updateInPerspective(event);

        verify(presentation).setEnabledAndVisible(true);
    }

    @Test
    public void actionShouldBeHiddenIfSelectedElementIsNull() throws Exception {
        when(appContext.getResource()).thenReturn(null);

        action.updateInPerspective(event);

        verify(presentation).setEnabledAndVisible(false);
    }

    @Test
    public void actionShouldBeHiddenIfSelectedElementIsNotFolder() throws Exception {
        when(resource.isFolder()).thenReturn(false);

        action.updateInPerspective(event);

        verify(presentation).setEnabledAndVisible(false);
    }

    @Test
    public void configurationWizardShouldNotBeShowedIfSelectedElementIsNotFolder() throws Exception {
        when(resource.isFolder()).thenReturn(false);

        action.actionPerformed(event);

        verify(projectConfigWizard, never()).show((MutableProjectConfig)anyObject());
    }

    @Test
    public void configurationWizardShouldNotBeShowedIfSelectedElementIsNull() throws Exception {
        when(appContext.getResource()).thenReturn(null);

        action.actionPerformed(event);

        verify(projectConfigWizard, never()).show((MutableProjectConfig)anyObject());
    }

    @Test
    public void configurationWizardShouldNotBeShowedIfPathOfFolderIsNull() throws Exception {
        when(resource.getLocation()).thenReturn(null);

        action.actionPerformed(event);

        verify(projectConfigWizard, never()).show((MutableProjectConfig)anyObject());
    }

    @Test
    public void configurationWizardShouldBeShowed() throws Exception {
        when(resource.getName()).thenReturn(TEXT);

        action.actionPerformed(event);

        verify(resource).getName();
        verify(projectConfigWizard).show(projectConfigCapture.capture());

        MutableProjectConfig projectConfig = projectConfigCapture.getValue();
        assertEquals(TEXT, projectConfig.getName());
        assertEquals(TEXT, projectConfig.getPath());
    }
}
