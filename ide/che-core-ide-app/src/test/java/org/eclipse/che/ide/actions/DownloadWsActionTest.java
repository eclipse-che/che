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
package org.eclipse.che.ide.actions;

import com.google.gwtmockito.GwtMockitoTestRunner;

import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.Presentation;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.machine.DevMachine;
import org.eclipse.che.ide.api.machine.WsAgentURLModifier;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.download.DownloadContainer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Valeriy Svydenko
 */
@RunWith(GwtMockitoTestRunner.class)
public class DownloadWsActionTest {
    @Mock
    private AppContext               appContext;
    @Mock
    private WsAgentURLModifier       wsAgentURLModifier;
    @Mock
    private DownloadContainer        downloadContainer;
    @Mock
    private CoreLocalizationConstant locale;
    @Mock
    private Resources                resources;
    @Mock
    private ActionEvent              actionEvent;
    @Mock
    private Presentation             presentation;

    @InjectMocks
    private DownloadWsAction action;

    @Test
    public void actionShouldBeInitialized() throws Exception {
        verify(locale).downloadProjectAsZipName();
        verify(locale).downloadProjectAsZipDescription();
        verify(resources).downloadZip();
    }

    @Test
    public void actionShouldBePerformed() throws Exception {
        String baseUrl = "baseUrl";

        DevMachine devMachine = mock(DevMachine.class);
        when(appContext.getDevMachine()).thenReturn(devMachine);
        when(devMachine.getWsAgentBaseUrl()).thenReturn(baseUrl);
        when(wsAgentURLModifier.modify(anyString())).thenReturn(baseUrl);

        action.actionPerformed(actionEvent);

        verify(downloadContainer).setUrl(eq(baseUrl));
    }

    @Test
    public void actionShouldBeEnable() throws Exception {
        Project project = mock(Project.class);

        when(actionEvent.getPresentation()).thenReturn(presentation);
        Project[] projects = new Project[1];
        projects[0] = project;
        when(appContext.getProjects()).thenReturn(projects);

        action.updateInPerspective(actionEvent);

        verify(presentation).setVisible(true);
        verify(presentation).setEnabled(true);
    }

    @Test
    public void actionShouldNotBeEnabledIfWsIsEmpty() throws Exception {
        when(actionEvent.getPresentation()).thenReturn(presentation);
        Project[] projects = new Project[0];
        when(appContext.getProjects()).thenReturn(projects);

        action.updateInPerspective(actionEvent);

        verify(presentation).setVisible(true);
        verify(presentation).setEnabled(false);
    }

    @Test
    public void actionShouldNotBeEnabledIfListOfProjectsIsNull() throws Exception {
        when(actionEvent.getPresentation()).thenReturn(presentation);
        when(appContext.getProjects()).thenReturn(null);

        action.updateInPerspective(actionEvent);

        verify(presentation).setVisible(true);
        verify(presentation).setEnabled(false);
    }
}
