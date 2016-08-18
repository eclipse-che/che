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
package org.eclipse.che.ide.editor.synchronization;

import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.HandlerRegistration;

import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.document.DocumentEventBus;
import org.eclipse.che.ide.api.editor.document.DocumentHandle;
import org.eclipse.che.ide.api.editor.events.DocumentChangeEvent;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/** @author Roman Nikitenko */
@RunWith(MockitoJUnitRunner.class)
public class EditorGroupSynchronizationImplTest {

    @Mock
    private EditorAgent         editorAgent;
    @Mock
    private Document            document;
    @Mock
    private DocumentHandle      documentHandle;
    @Mock
    private DocumentEventBus    documentEventBus;
    @Mock
    private HandlerRegistration handlerRegistration;
    @Mock
    private DocumentChangeEvent documentChangeEvent;

    private EditorPartPresenter            activeEditor;
    private EditorGroupSynchronizationImpl editorGroupSynchronization;

    @Before
    public void init() {
        activeEditor = mock(EditorPartPresenter.class, withSettings().extraInterfaces(TextEditor.class));
        EditorPartPresenter openedEditor1 = mock(EditorPartPresenter.class, withSettings().extraInterfaces(TextEditor.class));
        EditorPartPresenter openedEditor2 = mock(EditorPartPresenter.class, withSettings().extraInterfaces(TextEditor.class));
        List<EditorPartPresenter> openedEditors = new ArrayList<>(2);
        openedEditors.add(openedEditor1);
        openedEditors.add(openedEditor2);

        when(editorAgent.getActiveEditor()).thenReturn(activeEditor);
        when(document.getDocumentHandle()).thenReturn(documentHandle);
        when(documentHandle.getDocEventBus()).thenReturn(documentEventBus);
        when(documentHandle.getDocument()).thenReturn(document);

        when(((TextEditor)activeEditor).getDocument()).thenReturn(document);
        when(((TextEditor)openedEditor1).getDocument()).thenReturn(document);
        when(((TextEditor)openedEditor2).getDocument()).thenReturn(document);

        when(documentEventBus.addHandler((Event.Type<Object>)anyObject(), anyObject())).thenReturn(handlerRegistration);

        editorGroupSynchronization = new EditorGroupSynchronizationImpl(editorAgent, openedEditors);
    }

    @Test
    public void shouldAddEditor() {
        reset(documentEventBus);

        editorGroupSynchronization.addEditor(activeEditor);

        verify(documentEventBus).addHandler(Matchers.<DocumentChangeEvent.Type>anyObject(), eq(editorGroupSynchronization));
    }

    @Test
    public void shouldRemoveEditorFromGroup() {
        editorGroupSynchronization.addEditor(activeEditor);

        editorGroupSynchronization.removeEditor(activeEditor);

        //should save content before closing (autosave will not have time to do it)
        verify(activeEditor).doSave();
        verify(handlerRegistration).removeHandler();
    }

    @Test
    public void shouldRemoveAllEditorsFromGroup() {
        editorGroupSynchronization.unInstall();

        verify(handlerRegistration, times(2)).removeHandler();
    }

    @Test
    public void shouldNotApplyChangesFromNotActiveEditor() {
        DocumentHandle documentHandle1 = mock(DocumentHandle.class);
        when(documentChangeEvent.getDocument()).thenReturn(documentHandle1);
        when(documentHandle1.isSameAs(documentHandle)).thenReturn(false);

        editorGroupSynchronization.onDocumentChange(documentChangeEvent);

        verify(document, never()).replace(anyInt(), anyInt(), anyString());
    }

    @Test
    public void shouldApplyChangesFromActiveEditor() {
        int offset = 10;
        int removeCharCount = 100;
        String text = "someText";
        when(documentChangeEvent.getOffset()).thenReturn(offset);
        when(documentChangeEvent.getRemoveCharCount()).thenReturn(removeCharCount);
        when(documentChangeEvent.getText()).thenReturn(text);
        DocumentHandle documentHandle1 = mock(DocumentHandle.class);
        when(documentChangeEvent.getDocument()).thenReturn(documentHandle1);
        when(documentHandle1.isSameAs(documentHandle)).thenReturn(true);

        editorGroupSynchronization.onDocumentChange(documentChangeEvent);

        verify(document, times(2)).replace(eq(offset), eq(removeCharCount), eq(text));
    }
}
