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
package org.eclipse.che.ide.ext.java.client.action;

import com.google.gwtmockito.GwtMockitoTestRunner;

import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.Presentation;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.project.classpath.ProjectClasspathPresenter;
import org.eclipse.che.ide.ext.java.shared.Constants;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link ProjectClasspathAction}.
 *
 * @author Valeriy Svydenko
 */
@RunWith(GwtMockitoTestRunner.class)
public class ProjectClasspathActionTest {

    @Mock
    private AppContext                appContext;
    @Mock
    private ProjectClasspathPresenter projectClasspathPresenter;
    @Mock
    private JavaLocalizationConstant  localization;

    @Mock
    private CurrentProject   currentProject;
    @Mock
    private ProjectConfigDto projectConfigDto;
    @Mock
    private ActionEvent      actionEvent;
    @Mock
    private Presentation     presentation;

    @InjectMocks
    private ProjectClasspathAction action;

    @Before
    public void setUp() throws Exception {
        Map<String, List<String>> javaProjectAttributes = new HashMap<>(1);
        javaProjectAttributes.put(Constants.LANGUAGE, Collections.singletonList(Constants.JAVA_ID));

        when(appContext.getCurrentProject()).thenReturn(currentProject);
        when(currentProject.getProjectConfig()).thenReturn(projectConfigDto);
        when(projectConfigDto.getAttributes()).thenReturn(javaProjectAttributes);
        when(actionEvent.getPresentation()).thenReturn(presentation);
    }

    @Test
    public void titleAndDescriptionShouldSet() throws Exception {
        verify(localization).projectClasspathTitle();
        verify(localization).projectClasspathDescriptions();
    }

    @Test
    public void actionShouldBePerformed() throws Exception {
        action.actionPerformed(actionEvent);

        verify(projectClasspathPresenter).show();
    }

    @Test
    public void skipPerformingActionIfCurrentProjectIsNull() throws Exception {
        when(appContext.getCurrentProject()).thenReturn(null);

        action.actionPerformed(actionEvent);

        verify(projectClasspathPresenter, never()).show();
    }

    @Test
    public void actionShouldBeVisibleAndEnableIfProjectHasJavaProjectType() throws Exception {
        action.updateInPerspective(actionEvent);

        verify(presentation).setEnabledAndVisible(true);
    }

    @Test
    public void actionShouldBeHiddenIfProjectHasNotJavaProjectType() throws Exception {
        Map<String, List<String>> javaProjectAttributes = new HashMap<>(1);
        javaProjectAttributes.put(Constants.LANGUAGE, Collections.singletonList("prType"));

        when(projectConfigDto.getAttributes()).thenReturn(javaProjectAttributes);

        action.updateInPerspective(actionEvent);

        verify(presentation).setEnabledAndVisible(false);
    }

    @Test
    public void actionShouldBeHiddenIfCurrentProjectIsNull() throws Exception {
        when(appContext.getCurrentProject()).thenReturn(null);

        action.updateInPerspective(actionEvent);

        verify(presentation).setVisible(false);
    }
}
