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
package org.eclipse.che.ide.part.editor.multipart;

import com.google.gwtmockito.GwtMockitoTestRunner;
import com.google.inject.Provider;
import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;

import org.eclipse.che.ide.api.constraints.Constraints;
import org.eclipse.che.ide.api.editor.AbstractEditorPresenter;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.event.ActivePartChangedEvent;
import org.eclipse.che.ide.api.parts.EditorPartStack;
import org.eclipse.che.ide.api.parts.PartPresenter;
import org.eclipse.che.ide.part.editor.EditorPartStackPresenter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;

import static org.eclipse.che.ide.api.constraints.Direction.HORIZONTALLY;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Roman Nikitenko
 */
@RunWith(GwtMockitoTestRunner.class)
public class EditorMultiPartStackPresenterTest {

    private static final String RELATIVE_PART_ID = "partID";

    private Constraints constraints = new Constraints(HORIZONTALLY, RELATIVE_PART_ID);

    //constructor mocks
    @Mock
    private EditorMultiPartStackView  view;
    @Mock
    private EventBus                  eventBus;
    @Mock
    private Provider<EditorPartStack> editorPartStackFactory;

    //additional mocks
    @Mock
    private EditorPartStackPresenter editorPartStack;
    @Mock
    private AbstractEditorPresenter  partPresenter1;
    @Mock
    private AbstractEditorPresenter  partPresenter2;
    @Mock
    private EditorPartPresenter      editorPartPresenter;
    @Mock
    private HandlerRegistration      handlerRegistration;

    private EditorMultiPartStackPresenter presenter;

    @Before
    public void setUp() {
        when(editorPartStackFactory.get()).thenReturn(editorPartStack);
        when(editorPartStack.containsPart(partPresenter1)).thenReturn(true);
        when(eventBus.addHandler((Event.Type<Object>)anyObject(), anyObject())).thenReturn(handlerRegistration);

        presenter = new EditorMultiPartStackPresenter(eventBus, view, editorPartStackFactory);
    }

    @Test
    public void constructorShouldBeVerified() {
        verify(eventBus).addHandler(Matchers.<ActivePartChangedEvent.Type>anyObject(), eq(presenter));
    }

    @Test
    public void shouldOpenPartInNewEditorPartStack() {
        presenter.addPart(partPresenter1, null);

        verify(editorPartStackFactory).get();
        verify(editorPartStack).addPart(partPresenter1);
        verify(view).addPartStack(eq(editorPartStack), isNull(EditorPartStack.class), isNull(Constraints.class), eq(-1.0));
    }

    @Test
    public void shouldOpenPartInActiveEditorPartStack() {
        presenter.addPart(partPresenter1);
        presenter.setActivePart(partPresenter1);
        reset(view);
        reset(editorPartStackFactory);

        presenter.addPart(partPresenter2, null);

        verify(editorPartStackFactory, never()).get();
        verify(editorPartStack).addPart(partPresenter2);
        verify(view, never()).addPartStack((EditorPartStack)anyObject(), (EditorPartStack)anyObject(), (Constraints)anyObject(), eq(-1.0));
    }

    @Test
    public void shouldSplitEditorPartStackAndOpenPart() {
        presenter.addPart(partPresenter1);
        reset(editorPartStackFactory);
        when(editorPartStackFactory.get()).thenReturn(editorPartStack);
        when(editorPartStack.getPartByTabId(RELATIVE_PART_ID)).thenReturn(partPresenter1);

        presenter.addPart(partPresenter2, constraints);

        verify(editorPartStackFactory).get();
        verify(editorPartStack).addPart(partPresenter2);
        verify(view).addPartStack(editorPartStack, editorPartStack, constraints, -1);
    }

    @Test
    public void focusShouldBeSet() {
        presenter.addPart(partPresenter1);
        presenter.setActivePart(partPresenter1);
        presenter.setFocus(true);

        verify(editorPartStack).setFocus(true);
    }

    @Test
    public void shouldSetActivePart() {
        presenter.addPart(partPresenter1);
        presenter.setActivePart(partPresenter1);

        verify(editorPartStack).containsPart(partPresenter1);
        verify(editorPartStack).setActivePart(partPresenter1);
    }

    @Test
    public void shouldRemovePart() {
        when(editorPartStack.getActivePart()).thenReturn(partPresenter2);

        presenter.addPart(partPresenter1);
        presenter.removePart(partPresenter1);

        verify(editorPartStack).containsPart(partPresenter1);
        verify(editorPartStack).removePart(partPresenter1);
    }

    @Test
    public void shouldRemovePartStackWhenPartStackIsEmpty() {
        when(editorPartStack.getActivePart()).thenReturn(null);

        presenter.addPart(partPresenter1);
        presenter.removePart(partPresenter1);

        verify(editorPartStack).containsPart(partPresenter1);
        verify(editorPartStack).removePart(partPresenter1);
        verify(view).removePartStack(editorPartStack);
    }

    @Test
    public void shouldNotRemovePartStackWhenPartStackIsNotEmpty() {
        when(editorPartStack.getActivePart()).thenReturn(partPresenter2);

        presenter.addPart(partPresenter1);
        presenter.removePart(partPresenter1);

        verify(editorPartStack).containsPart(partPresenter1);
        verify(editorPartStack).removePart(partPresenter1);
        verify(view, never()).removePartStack(editorPartStack);
    }

    @Test
    public void shouldOpenPreviousActivePartStack() {
        when(editorPartStack.containsPart(partPresenter1)).thenReturn(true);
        presenter.addPart(partPresenter1);
        presenter.addPart(partPresenter2);

        presenter.removePart(partPresenter1);

        verify(editorPartStack).containsPart((PartPresenter)anyObject());
        verify(view).removePartStack(editorPartStack);
        verify(editorPartStack).openPreviousActivePart();
    }

    @Test
    public void shouldOpenPreviousActivePart() {
        presenter.addPart(partPresenter1);
        presenter.setActivePart(partPresenter1);

        presenter.openPreviousActivePart();

        verify(editorPartStack).openPreviousActivePart();
    }
}
