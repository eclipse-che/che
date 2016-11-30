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

import com.google.inject.Provider;
import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.api.editor.EditorInput;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.EditorWithAutoSave;
import org.eclipse.che.ide.api.event.ActivePartChangedEvent;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.resources.ResourceChangedEvent;
import org.eclipse.che.ide.api.resources.ResourceDelta;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.resource.Path;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/** @author Roman Nikitenko */
@RunWith(MockitoJUnitRunner.class)
public class EditorContentSynchronizerImplTest {
    private static final String FOLDER_PATH = "testProject/src/main/java/org/eclipse/che/examples/";
    private static final String FILE_NAME = "someFile";
    private static final String FILE_PATH = FOLDER_PATH + FILE_NAME;

    //constructor mocks
    @Mock
    private EventBus                             eventBus;
    @Mock
    private Provider<EditorGroupSynchronization> editorGroupSyncProvider;

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
        when(virtualFile.getLocation()).thenReturn(new Path(FILE_PATH));
        when(editorGroupSyncProvider.get()).thenReturn(editorGroupSynchronization);
    }

    @Test
    public void constructorShouldBeVerified() {
        verify(eventBus, times(2)).addHandler(Matchers.<Event.Type<Object>>anyObject(), anyObject());
    }

    @Test
    public void shouldCreateNewEditorGroup() {
        EditorPartPresenter openedEditor = mock(EditorPartPresenter.class);
        when(openedEditor.getEditorInput()).thenReturn(editorInput);

        editorContentSynchronizer.trackEditor(activeEditor);

        verify(editorGroupSyncProvider).get();
    }

    @Test
    public void shouldAddEditorIntoExistGroup() {
        EditorPartPresenter openedEditor1 = mock(EditorPartPresenter.class);
        EditorPartPresenter openedEditor2 = mock(EditorPartPresenter.class);
        when(openedEditor1.getEditorInput()).thenReturn(editorInput);
        when(openedEditor2.getEditorInput()).thenReturn(editorInput);

        editorContentSynchronizer.trackEditor(openedEditor1);
        editorContentSynchronizer.trackEditor(openedEditor2);
        reset(editorGroupSyncProvider);

        editorContentSynchronizer.trackEditor(activeEditor);

        verify(editorGroupSyncProvider, never()).get();
        verify(editorGroupSynchronization).addEditor(activeEditor);
    }

    @Test
    public void shouldRemoveEditorFromGroup() {
        EditorPartPresenter openedEditor1 = mock(EditorPartPresenter.class);
        EditorPartPresenter openedEditor2 = mock(EditorPartPresenter.class);
        when(openedEditor1.getEditorInput()).thenReturn(editorInput);
        when(openedEditor2.getEditorInput()).thenReturn(editorInput);

        editorContentSynchronizer.trackEditor(openedEditor1);
        editorContentSynchronizer.trackEditor(openedEditor2);
        editorContentSynchronizer.trackEditor(activeEditor);

        editorContentSynchronizer.unTrackEditor(activeEditor);

        verify(editorGroupSynchronization).removeEditor(activeEditor);
    }

    @Test
    public void shouldRemoveGroup() {
        EditorPartPresenter openedEditor1 = mock(EditorPartPresenter.class);
        when(openedEditor1.getEditorInput()).thenReturn(editorInput);
        editorContentSynchronizer.trackEditor(openedEditor1);
        editorContentSynchronizer.trackEditor(activeEditor);

        editorContentSynchronizer.unTrackEditor(activeEditor);

        verify(editorGroupSynchronization).removeEditor(activeEditor);
        verify(editorGroupSynchronization).unInstall();
    }

    @Test
    public void shouldUpdatePathForGroupWhenFileLocationIsChanged() {
        Resource resource = mock(Resource.class);
        ResourceDelta delta = mock(ResourceDelta.class);
        ResourceChangedEvent resourceChangedEvent = new ResourceChangedEvent(delta);
        Path fromPath = new Path(FILE_PATH);
        Path toPath = new Path("testProject/src/main/java/org/eclipse/che/examples/changedFile");
        EditorPartPresenter openedEditor1 = mock(EditorPartPresenter.class);

        when(openedEditor1.getEditorInput()).thenReturn(editorInput);
        when(delta.getKind()).thenReturn(ResourceDelta.ADDED);
        when(delta.getFlags()).thenReturn(5632);
        when(delta.getFromPath()).thenReturn(fromPath);
        when(delta.getToPath()).thenReturn(toPath);
        when(delta.getResource()).thenReturn(resource);
        when(resource.isFile()).thenReturn(true);

        editorContentSynchronizer.trackEditor(openedEditor1);
        editorContentSynchronizer.onResourceChanged(resourceChangedEvent);

        final EditorGroupSynchronization oldGroup = editorContentSynchronizer.editorGroups.get(fromPath);
        final EditorGroupSynchronization newGroup = editorContentSynchronizer.editorGroups.get(toPath);

        assertNull(oldGroup);
        assertNotNull(newGroup);
    }

    @Test
    public void shouldUpdatePathForGroupWhenFolderLocationIsChanged() {
        Resource resource = mock(Resource.class);
        ResourceDelta delta = mock(ResourceDelta.class);
        ResourceChangedEvent resourceChangedEvent = new ResourceChangedEvent(delta);
        Path fromPath = new Path(FOLDER_PATH);
        Path toPath = new Path("testProject/src/main/java/org/eclipse/che/samples/");

        Path oldFilePath = new Path(FILE_PATH);
        Path newFilePath = new Path("testProject/src/main/java/org/eclipse/che/samples/" + FILE_NAME);
        EditorPartPresenter openedEditor1 = mock(EditorPartPresenter.class);

        when(openedEditor1.getEditorInput()).thenReturn(editorInput);
        when(delta.getKind()).thenReturn(ResourceDelta.ADDED);
        when(delta.getFlags()).thenReturn(5632);
        when(delta.getFromPath()).thenReturn(fromPath);
        when(delta.getToPath()).thenReturn(toPath);
        when(delta.getResource()).thenReturn(resource);
        when(resource.isFile()).thenReturn(false);

        editorContentSynchronizer.trackEditor(openedEditor1);
        editorContentSynchronizer.onResourceChanged(resourceChangedEvent);

        final EditorGroupSynchronization oldGroup = editorContentSynchronizer.editorGroups.get(oldFilePath);
        final EditorGroupSynchronization newGroup = editorContentSynchronizer.editorGroups.get(newFilePath);

        assertNull(oldGroup);
        assertNotNull(newGroup);
    }
}
