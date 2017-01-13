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
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.Presentation;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.project.ProjectServiceClient;
import org.eclipse.che.ide.api.resources.RenamingSupport;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.dto.DtoFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Valeriy Svydenko
 */
@RunWith(GwtMockitoTestRunner.class)
public class RenameItemActionTest {
    @InjectMocks
    private RenameItemAction action;

    @Mock
    private Resources                resources;
    @Mock
    private CoreLocalizationConstant localization;
    @Mock
    private DtoFactory               dtoFactory;
    @Mock
    private DialogFactory            dialogFactory;
    @Mock
    private ProjectServiceClient     projectServiceClient;
    @Mock
    private EditorAgent              editorAgent;
    @Mock
    private NotificationManager      notificationManager;
    @Mock
    private AppContext               appContext;
    @Mock
    private RenamingSupport          renamingSupportValidator;

    @Mock
    private Resource     resource;
    @Mock
    private ActionEvent  event;
    @Mock
    private Presentation presentation;

    private Resource[]           selectedResources = new Resource[1];
    private Set<RenamingSupport> renamingSupport   = new HashSet<>();

    @Before
    public void setUp() throws Exception {
        selectedResources[0] = resource;

        when(appContext.getResources()).thenReturn(selectedResources);
        when(event.getPresentation()).thenReturn(presentation);
    }

    @Test
    public void actionShouldBeDisabledIfSelectedResourceIsNull() throws Exception {
        when(appContext.getResources()).thenReturn(null);

        action.updateInPerspective(event);

        verify(presentation).setVisible(true);
        verify(presentation).setEnabled(false);
    }

    @Test
    public void actionShouldBeDisabledIfMultiselectionIs() throws Exception {
        selectedResources = new Resource[2];
        selectedResources[0] = resource;
        selectedResources[1] = resource;

        when(appContext.getResources()).thenReturn(selectedResources);

        action.updateInPerspective(event);

        verify(presentation).setVisible(true);
        verify(presentation).setEnabled(false);
    }

    @Test
    public void actionShouldBeDisabledIfSomeRenameValidatorForbidsRenameOperation() throws Exception {
        renamingSupport.add(renamingSupportValidator);
        when(renamingSupportValidator.isRenameAllowed((Resource)anyObject())).thenReturn(false);

        action =
                new RenameItemAction(resources, localization, renamingSupport, editorAgent, notificationManager, dialogFactory, appContext);
        action.updateInPerspective(event);

        verify(presentation).setVisible(true);
        verify(presentation).setEnabled(false);
    }

    @Test
    public void actionShouldBeEnabled() throws Exception {
        renamingSupport.add(renamingSupportValidator);
        when(renamingSupportValidator.isRenameAllowed((Resource)anyObject())).thenReturn(true);

        action =
                new RenameItemAction(resources, localization, renamingSupport, editorAgent, notificationManager, dialogFactory, appContext);
        action.updateInPerspective(event);

        verify(presentation).setVisible(true);
        verify(presentation).setEnabled(true);
    }
}
