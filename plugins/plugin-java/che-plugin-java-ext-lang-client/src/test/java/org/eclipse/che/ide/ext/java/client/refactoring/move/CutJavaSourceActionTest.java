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
package org.eclipse.che.ide.ext.java.client.refactoring.move;

import com.google.gwtmockito.GwtMockitoTestRunner;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.Presentation;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.event.ActivePartChangedEvent;
import org.eclipse.che.ide.api.parts.PartPresenter;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Valeriy Svydenko
 */
@RunWith(GwtMockitoTestRunner.class)
public class CutJavaSourceActionTest {
    @Mock
    private JavaLocalizationConstant locale;
    @Mock
    private MoveAction               moveAction;
    @Mock
    private EventBus                 eventBus;

    @Mock
    private ActivePartChangedEvent event;
    @Mock
    private ActionEvent            updateActionEvent;
    @Mock
    private EditorPartPresenter    editorPartPresenter;
    @Mock
    private PartPresenter          partPresenter;
    @Mock
    private Presentation           presentation;

    private CutJavaSourceAction action;

    @Before
    public void setUp() throws Exception {
        action = new CutJavaSourceAction(locale, moveAction, eventBus);
    }

    @Test
    public void constructorShouldPerform() throws Exception {
        verify(locale).moveActionName();
        verify(locale).moveActionDescription();

        verify(eventBus).addHandler(ActivePartChangedEvent.TYPE, action);
    }

    @Test
    public void actionShouldBeEnabledIfEditorPartIsNotActiveAndMoveActionIsEnable() throws Exception {
        when(updateActionEvent.getPresentation()).thenReturn(presentation);
        when(event.getActivePart()).thenReturn(partPresenter);
        when(moveAction.isActionEnable()).thenReturn(true);

        action.onActivePartChanged(event);
        action.update(updateActionEvent);

        verify(presentation).setEnabled(true);
    }

    @Test
    public void actionShouldBeDisabledIfEditorPartIsNotActiveAndMoveActionIsNotEnable() throws Exception {
        when(updateActionEvent.getPresentation()).thenReturn(presentation);
        when(event.getActivePart()).thenReturn(partPresenter);
        when(moveAction.isActionEnable()).thenReturn(false);

        action.onActivePartChanged(event);
        action.update(updateActionEvent);

        verify(presentation).setEnabled(false);
    }

    @Test
    public void actionShouldBeDisabledIfEditorPartIsActive() throws Exception {
        when(updateActionEvent.getPresentation()).thenReturn(presentation);
        when(event.getActivePart()).thenReturn(editorPartPresenter);

        action.onActivePartChanged(event);
        action.update(updateActionEvent);

        verify(presentation).setEnabled(false);
    }

    @Test
    public void actionPerformsIfEditorPartIsNotActiveAndMoveActionIsEnable() throws Exception {
        when(updateActionEvent.getPresentation()).thenReturn(presentation);
        when(event.getActivePart()).thenReturn(partPresenter);
        when(moveAction.isActionEnable()).thenReturn(true);

        action.onActivePartChanged(event);
        action.actionPerformed(updateActionEvent);

        verify(moveAction).actionPerformed(updateActionEvent);
    }

    @Test
    public void actionDoesNotPerformIfEditorPartIsNotActiveAndMoveActionIsNotEnable() throws Exception {
        when(updateActionEvent.getPresentation()).thenReturn(presentation);
        when(event.getActivePart()).thenReturn(partPresenter);
        when(moveAction.isActionEnable()).thenReturn(false);

        action.onActivePartChanged(event);
        action.actionPerformed(updateActionEvent);

        verify(moveAction, never()).actionPerformed(updateActionEvent);
    }

    @Test
    public void actionDoesPerformIfEditorPartIsActive() throws Exception {
        when(updateActionEvent.getPresentation()).thenReturn(presentation);
        when(event.getActivePart()).thenReturn(editorPartPresenter);

        action.onActivePartChanged(event);
        action.actionPerformed(updateActionEvent);

        verify(moveAction, never()).actionPerformed(updateActionEvent);
    }

    @Test
    public void partShouldBeChanged() throws Exception {
        when(event.getActivePart()).thenReturn(editorPartPresenter);

        action.onActivePartChanged(event);

        assertTrue(event.getActivePart() instanceof EditorPartPresenter);
    }
}