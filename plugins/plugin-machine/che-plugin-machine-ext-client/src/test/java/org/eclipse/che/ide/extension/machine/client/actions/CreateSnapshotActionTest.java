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
package org.eclipse.che.ide.extension.machine.client.actions;

import com.google.gwtmockito.GwtMockitoTestRunner;

import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.actions.WorkspaceSnapshotCreator;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.Presentation;
import org.eclipse.che.ide.api.app.AppContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link CreateSnapshotAction}.
 *
 * @author Yevhenii Voevodin
 */
@RunWith(GwtMockitoTestRunner.class)
public class CreateSnapshotActionTest {

    @Mock
    private WorkspaceSnapshotCreator snapshotCreator;

    @Mock
    private CoreLocalizationConstant coreLocalizationConstant;

    @Mock(answer = Answers.RETURNS_MOCKS)
    private ActionEvent event;

    @Mock(answer = Answers.RETURNS_MOCKS)
    private AppContext appContext;

    @InjectMocks
    private CreateSnapshotAction createSnapshotAction;

    @Test
    public void shouldSetTitleAndDescription() {
        verify(coreLocalizationConstant).createSnapshotTitle();
        verify(coreLocalizationConstant).createSnapshotDescription();
    }

    @Test
    public void eventPresentationShouldBeEnabledIfSnapshotCreatorIsNotInProgress() {
        when(event.getPresentation()).thenReturn(new Presentation());
        when(snapshotCreator.isInProgress()).thenReturn(false);

        createSnapshotAction.updateInPerspective(event);

        assertTrue(event.getPresentation().isEnabled());
    }

    @Test
    public void eventPresentationShouldBeDisabledIfSnapshotCreatorIsInProgress() {
        when(event.getPresentation()).thenReturn(new Presentation());
        when(snapshotCreator.isInProgress()).thenReturn(true);

        createSnapshotAction.updateInPerspective(event);

        assertFalse(event.getPresentation().isEnabled());
    }

    @Test
    public void shouldCreateSnapshotWithWorkspaceIdFromAppContextWhenActionPerformed() {
        when(appContext.getWorkspace()).thenReturn(newDto(WorkspaceDto.class).withId("workspace123"));

        createSnapshotAction.actionPerformed(event);

        verify(snapshotCreator).createSnapshot("workspace123");
    }
}
