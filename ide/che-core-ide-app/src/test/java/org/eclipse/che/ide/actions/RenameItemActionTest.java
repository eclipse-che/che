/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.actions;

import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gwtmockito.GwtMockitoTestRunner;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.Presentation;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.parts.PartPresenter;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.api.resources.RenamingSupport;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.project.ProjectServiceClient;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

/** @author Valeriy Svydenko */
@RunWith(GwtMockitoTestRunner.class)
public class RenameItemActionTest {
  @InjectMocks private RenameItemAction action;

  @Mock private Resources resources;
  @Mock private CoreLocalizationConstant localization;
  @Mock private DtoFactory dtoFactory;
  @Mock private DialogFactory dialogFactory;
  @Mock private ProjectServiceClient projectServiceClient;
  @Mock private EditorAgent editorAgent;
  @Mock private NotificationManager notificationManager;
  @Mock private AppContext appContext;
  @Mock private RenamingSupport renamingSupportValidator;

  @Mock private Resource resource;
  @Mock private ActionEvent event;
  @Mock private Presentation presentation;

  @Mock private WorkspaceAgent workspaceAgent;
  @Mock private PartPresenter partPresenter;

  private Resource[] selectedResources = new Resource[1];
  private Set<RenamingSupport> renamingSupport = new HashSet<>();

  @Before
  public void setUp() throws Exception {
    selectedResources[0] = resource;

    when(appContext.getResources()).thenReturn(selectedResources);
    when(event.getPresentation()).thenReturn(presentation);
    when(workspaceAgent.getActivePart()).thenReturn(partPresenter);
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
    when(renamingSupportValidator.isRenameAllowed((Resource) anyObject())).thenReturn(false);

    action =
        new RenameItemAction(
            resources,
            localization,
            renamingSupport,
            editorAgent,
            notificationManager,
            dialogFactory,
            appContext,
            workspaceAgent);
    action.updateInPerspective(event);

    verify(presentation).setVisible(true);
    verify(presentation).setEnabled(false);
  }

  @Test
  public void actionShouldBeEnabled() throws Exception {
    renamingSupport.add(renamingSupportValidator);
    when(renamingSupportValidator.isRenameAllowed((Resource) anyObject())).thenReturn(true);

    action =
        new RenameItemAction(
            resources,
            localization,
            renamingSupport,
            editorAgent,
            notificationManager,
            dialogFactory,
            appContext,
            workspaceAgent);
    action.updateInPerspective(event);

    verify(presentation).setVisible(true);
    verify(presentation).setEnabled(true);
  }

  @Test
  public void actionShouldBeDisabledIfEditorActive() {
    when(workspaceAgent.getActivePart()).thenReturn(mock(EditorPartPresenter.class));

    action.updateInPerspective(event);

    verify(presentation).setEnabledAndVisible(false);
  }
}
