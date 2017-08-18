/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.workspace;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gwtmockito.GwtMockitoTestRunner;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.workspace.model.RuntimeImpl;
import org.eclipse.che.ide.api.workspace.model.WorkspaceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

/** @author Dmitry Shnurenko */
@RunWith(GwtMockitoTestRunner.class)
public class StopWorkspaceActionTest {

  @Mock private CoreLocalizationConstant locale;
  @Mock private AppContext appContext;
  @Mock private CurrentWorkspaceManager workspaceManager;
  @Mock private WorkspaceImpl workspace;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private ActionEvent actionEvent;

  @Mock private Promise<Void> voidPromise;

  @Captor private ArgumentCaptor<Operation<Void>> operationCaptor;

  @InjectMocks private StopWorkspaceAction action;

  @Test
  public void titleAndDescriptionShouldBeSet() {
    verify(locale).stopWsTitle();
    verify(locale).stopWsDescription();
  }

  @Test
  public void actionShouldBeUpdated() {
    when(workspace.getRuntime()).thenReturn(mock(RuntimeImpl.class));
    when(appContext.getWorkspace()).thenReturn(workspace);

    action.updateInPerspective(actionEvent);

    verify(actionEvent, times(2)).getPresentation();
  }

  @Test
  public void actionShouldBePerformed() throws Exception {
    when(appContext.getWorkspace()).thenReturn(workspace);
    when(workspace.getId()).thenReturn("id");

    action.actionPerformed(actionEvent);

    verify(workspaceManager).stopWorkspace();
  }
}
