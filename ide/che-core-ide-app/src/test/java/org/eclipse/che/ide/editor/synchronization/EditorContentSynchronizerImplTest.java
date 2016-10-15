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

import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorInput;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.EditorWithAutoSave;
import org.eclipse.che.ide.api.event.ActivePartChangedEvent;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.resource.Path;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/** @author Roman Nikitenko */
@RunWith(MockitoJUnitRunner.class)
public class EditorContentSynchronizerImplTest {

    //constructor mocks
    @Mock
    private EventBus                         eventBus;
    @Mock
    private EditorAgent                      editorAgent;
    @Mock
    private EditorGroupSychronizationFactory editorGroupSychronizationFactory;

    //additional mocks
    @Mock
    private EditorInput                editorInput;
    @Mock
    private VirtualFile                virtualFile;
    @Mock
    private EditorGroupSynchronization editorGroupSynchronization;
    @Mock
    private ActivePartChangedEvent     activePartChangedEvent;
    private EditorPartPresenter        activeEditor;

    @InjectMocks
    EditorContentSynchronizerImpl editorContentSynchronizer;

    @Before
    public void init() {
        activeEditor = mock(EditorPartPresenter.class, withSettings().extraInterfaces(EditorWithAutoSave.class));
        when(activeEditor.getEditorInput()).thenReturn(editorInput);
        when(editorInput.getFile()).thenReturn(virtualFile);
        when(virtualFile.getLocation()).thenReturn(new Path("somePath"));
        when(editorAgent.getActiveEditor()).thenReturn(activeEditor);
        when(editorGroupSychronizationFactory.create()).thenReturn(editorGroupSynchronization);
    }

    @Test
    public void constructorShouldBeVerified() {
        verify(eventBus).addHandler(Matchers.<ActivePartChangedEvent.Type>anyObject(), eq(editorContentSynchronizer));
    }

    @Test
    public void shouldCreateNewEditorGroup() {
        EditorPartPresenter openedEditor = mock(EditorPartPresenter.class);
        when(openedEditor.getEditorInput()).thenReturn(editorInput);
        List<EditorPartPresenter> openedFiles = new ArrayList<>(1);
        openedFiles.add(openedEditor);
        when(editorAgent.getOpenedEditors()).thenReturn(openedFiles);

        editorContentSynchronizer.trackEditor(activeEditor);

        verify(editorGroupSychronizationFactory).create();
    }

    @Test
    public void shouldAddEditorIntoExistGroup() {
        EditorPartPresenter openedEditor1 = mock(EditorPartPresenter.class);
        EditorPartPresenter openedEditor2 = mock(EditorPartPresenter.class);
        when(openedEditor1.getEditorInput()).thenReturn(editorInput);
        when(openedEditor2.getEditorInput()).thenReturn(editorInput);
        List<EditorPartPresenter> openedFiles = new ArrayList<>(1);
        openedFiles.add(openedEditor1);
        openedFiles.add(openedEditor2);
        when(editorAgent.getOpenedEditors()).thenReturn(openedFiles);

        editorContentSynchronizer.trackEditor(openedEditor1);
        editorContentSynchronizer.trackEditor(openedEditor2);
        reset(editorGroupSychronizationFactory);

        editorContentSynchronizer.trackEditor(activeEditor);

        verify(editorGroupSychronizationFactory, never()).create();
        verify(editorGroupSynchronization).addEditor(activeEditor);
    }

    @Test
    public void shouldRemoveEditorFromGroup() {
        EditorPartPresenter openedEditor1 = mock(EditorPartPresenter.class);
        EditorPartPresenter openedEditor2 = mock(EditorPartPresenter.class);
        when(openedEditor1.getEditorInput()).thenReturn(editorInput);
        when(openedEditor2.getEditorInput()).thenReturn(editorInput);
        List<EditorPartPresenter> openedFiles = new ArrayList<>(1);
        openedFiles.add(openedEditor1);
        openedFiles.add(openedEditor2);
        when(editorAgent.getOpenedEditors()).thenReturn(openedFiles);

        editorContentSynchronizer.trackEditor(openedEditor1);
        editorContentSynchronizer.trackEditor(openedEditor2);
        editorContentSynchronizer.trackEditor(activeEditor);
        reset(editorGroupSychronizationFactory);

        editorContentSynchronizer.unTrackEditor(activeEditor);

        verify(editorGroupSynchronization).removeEditor(activeEditor);
    }

    @Test
    public void shouldRemoveGroup() {
        EditorPartPresenter openedEditor1 = mock(EditorPartPresenter.class);
        when(openedEditor1.getEditorInput()).thenReturn(editorInput);
        List<EditorPartPresenter> openedFiles = new ArrayList<>(1);
        openedFiles.add(openedEditor1);
        when(editorAgent.getOpenedEditors()).thenReturn(openedFiles);
        editorContentSynchronizer.trackEditor(openedEditor1);
        editorContentSynchronizer.trackEditor(activeEditor);

        editorContentSynchronizer.unTrackEditor(activeEditor);

        verify(editorGroupSynchronization).removeEditor(activeEditor);
        verify(editorGroupSynchronization).unInstall();
    }
}
