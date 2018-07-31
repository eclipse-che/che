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
package org.eclipse.che.ide.editor.synchronization;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.NOT_EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.SUCCESS;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorInput;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.EditorWithAutoSave;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.document.DocumentEventBus;
import org.eclipse.che.ide.api.editor.document.DocumentHandle;
import org.eclipse.che.ide.api.editor.document.DocumentStorage;
import org.eclipse.che.ide.api.editor.document.DocumentStorage.DocumentCallback;
import org.eclipse.che.ide.api.editor.events.DocumentChangedEvent;
import org.eclipse.che.ide.api.editor.events.FileContentUpdateEvent;
import org.eclipse.che.ide.api.editor.texteditor.EditorWidget;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.resource.Path;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/** @author Roman Nikitenko */
@RunWith(MockitoJUnitRunner.class)
public class EditorGroupSynchronizationImplTest {
  private static final String FILE_CONTENT = "some content";
  private static final String FILE_NEW_CONTENT = "some content to update";
  private static final String FILE_LOCATION =
      "testProject/src/main/java/org/eclipse/che/examples/someFile";

  @Mock private EventBus eventBus;
  @Mock private EditorAgent editorAgent;
  @Mock private Document document;
  @Mock private DocumentHandle documentHandle;
  @Mock private DocumentEventBus documentEventBus;
  @Mock private DocumentStorage documentStorage;
  @Mock private NotificationManager notificationManager;
  @Mock private HandlerRegistration handlerRegistration;
  @Mock private DocumentChangedEvent documentChangeEvent;
  @Mock private FileContentUpdateEvent fileContentUpdateEvent;
  @Mock private EditorInput editorInput;
  @Mock private EditorWidget activeEditorWidget;
  @Mock private EditorWidget editor_1_Widget;
  @Mock private EditorWidget editor_2_Widget;
  @Captor private ArgumentCaptor<DocumentCallback> documentCallbackCaptor;

  private EditorPartPresenter activeEditor;
  private EditorPartPresenter openedEditor1;
  private EditorPartPresenter openedEditor2;
  private VirtualFile virtualFile;
  private EditorGroupSynchronizationImpl editorGroupSynchronization;

  @Before
  public void init() {
    activeEditor =
        mock(
            EditorPartPresenter.class,
            withSettings().extraInterfaces(TextEditor.class, EditorWithAutoSave.class));
    openedEditor1 =
        mock(
            EditorPartPresenter.class,
            withSettings().extraInterfaces(TextEditor.class, EditorWithAutoSave.class));
    openedEditor2 =
        mock(
            EditorPartPresenter.class,
            withSettings().extraInterfaces(TextEditor.class, EditorWithAutoSave.class));
    virtualFile = mock(VirtualFile.class, withSettings().extraInterfaces(File.class));

    when(((EditorWithAutoSave) openedEditor1).isAutoSaveEnabled()).thenReturn(true);
    when(((EditorWithAutoSave) openedEditor2).isAutoSaveEnabled()).thenReturn(true);

    when(editorAgent.getActiveEditor()).thenReturn(activeEditor);
    when(document.getDocumentHandle()).thenReturn(documentHandle);
    when(documentHandle.getDocEventBus()).thenReturn(documentEventBus);
    when(documentHandle.getDocument()).thenReturn(document);

    when(((TextEditor) activeEditor).getDocument()).thenReturn(document);
    when(((TextEditor) openedEditor1).getDocument()).thenReturn(document);
    when(((TextEditor) openedEditor2).getDocument()).thenReturn(document);

    when(((TextEditor) activeEditor).getEditorWidget()).thenReturn(activeEditorWidget);
    when(((TextEditor) openedEditor1).getEditorWidget()).thenReturn(editor_1_Widget);
    when(((TextEditor) openedEditor2).getEditorWidget()).thenReturn(editor_2_Widget);

    when(document.getContents()).thenReturn(FILE_CONTENT);
    when(openedEditor1.getEditorInput()).thenReturn(editorInput);
    when(editorInput.getFile()).thenReturn(virtualFile);
    when(virtualFile.getLocation()).thenReturn(new Path(FILE_LOCATION));

    when(documentEventBus.addHandler((Event.Type<Object>) anyObject(), anyObject()))
        .thenReturn(handlerRegistration);

    editorGroupSynchronization =
        new EditorGroupSynchronizationImpl(eventBus, documentStorage, notificationManager);
  }

  @Test
  public void shouldUpdateContentOnFileContentUpdateEvent() {
    editorGroupSynchronization.addEditor(openedEditor1);
    reset(documentEventBus);
    when(fileContentUpdateEvent.getFilePath()).thenReturn(FILE_LOCATION);

    editorGroupSynchronization.onFileContentUpdate(fileContentUpdateEvent);

    verify(documentStorage).getDocument(anyObject(), documentCallbackCaptor.capture());
    documentCallbackCaptor.getValue().onDocumentReceived(FILE_NEW_CONTENT);

    verify(document).replace(anyInt(), anyInt(), eq(FILE_NEW_CONTENT));
    verify(notificationManager, never())
        .notify(anyString(), (StatusNotification.Status) anyObject(), anyObject());
  }

  @Test
  public void shouldSkipUpdateContentOnFileContentUpdateEventWhenContentTheSame() {
    editorGroupSynchronization.addEditor(openedEditor1);
    reset(documentEventBus);
    when(fileContentUpdateEvent.getFilePath()).thenReturn(FILE_LOCATION);

    editorGroupSynchronization.onFileContentUpdate(fileContentUpdateEvent);

    verify(documentStorage).getDocument(anyObject(), documentCallbackCaptor.capture());
    documentCallbackCaptor.getValue().onDocumentReceived(FILE_CONTENT);

    verify(document, never()).replace(anyInt(), anyInt(), anyString());
    verify(notificationManager, never())
        .notify(anyString(), (StatusNotification.Status) anyObject(), anyObject());
  }

  @Test
  public void shouldNotifyAboutExternalOperationAtUpdateContentWhenStampIsDifferent() {
    editorGroupSynchronization.addEditor(openedEditor1);
    reset(documentEventBus);
    when(fileContentUpdateEvent.getFilePath()).thenReturn(FILE_LOCATION);
    when(fileContentUpdateEvent.getModificationStamp()).thenReturn("some stamp");
    when(((File) virtualFile).getModificationStamp()).thenReturn("current modification stamp");

    editorGroupSynchronization.onFileContentUpdate(fileContentUpdateEvent);

    verify(documentStorage).getDocument(anyObject(), documentCallbackCaptor.capture());
    documentCallbackCaptor.getValue().onDocumentReceived(FILE_NEW_CONTENT);

    verify(notificationManager).notify(anyString(), anyString(), eq(SUCCESS), eq(NOT_EMERGE_MODE));
  }

  @Test
  public void shouldUpdateDirtyStateForEditors() {
    addEditorsToGroup();

    editorGroupSynchronization.onEditorDirtyStateChanged(activeEditor);

    verify(editor_1_Widget).markClean();
    verify(editor_2_Widget).markClean();
    verify((TextEditor) openedEditor1).updateDirtyState(false);
    verify((TextEditor) openedEditor2).updateDirtyState(false);
    // we should not update 'dirty' state for the ACTIVE editor
    verify(activeEditorWidget, never()).markClean();
    verify((TextEditor) activeEditor, never()).updateDirtyState(false);
  }

  @Test
  public void shouldSkipUpdatingDirtyStateWhenNotActiveEditorWasSaved() {
    addEditorsToGroup();

    editorGroupSynchronization.onEditorDirtyStateChanged(openedEditor1);

    // we sync 'dirty' state of editors when content of an ACTIVE editor is saved
    verify(editor_1_Widget, never()).markClean();
    verify(editor_2_Widget, never()).markClean();
    verify(activeEditorWidget, never()).markClean();
    verify((TextEditor) openedEditor1, never()).updateDirtyState(anyBoolean());
    verify((TextEditor) openedEditor2, never()).updateDirtyState(anyBoolean());
    verify((TextEditor) activeEditor, never()).updateDirtyState(anyBoolean());
  }

  @Test
  public void shouldSkipUpdatingDirtyStateWhenHasNotEditorsToSync() {
    editorGroupSynchronization.addEditor(activeEditor);

    editorGroupSynchronization.onEditorDirtyStateChanged(activeEditor);

    verify(editor_1_Widget, never()).markClean();
    verify(editor_2_Widget, never()).markClean();
    verify(activeEditorWidget, never()).markClean();
    verify((TextEditor) openedEditor1, never()).updateDirtyState(anyBoolean());
    verify((TextEditor) openedEditor2, never()).updateDirtyState(anyBoolean());
    verify((TextEditor) activeEditor, never()).updateDirtyState(anyBoolean());
  }

  @Test
  public void shouldAddEditor() {
    reset(documentEventBus);

    editorGroupSynchronization.addEditor(activeEditor);

    verify(documentEventBus)
        .addHandler(
            org.mockito.ArgumentMatchers.<DocumentChangedEvent.Type>anyObject(),
            eq(editorGroupSynchronization));
  }

  @Test
  public void shouldUpdateContentAtAddingEditorWhenGroupHasUnsavedData() {
    editorGroupSynchronization.addEditor(openedEditor1);
    reset(documentEventBus);
    when(((EditorWithAutoSave) openedEditor1).isAutoSaveEnabled()).thenReturn(false);

    editorGroupSynchronization.addEditor(activeEditor);
    editorGroupSynchronization.onActiveEditorChanged(activeEditor);

    verify((EditorWithAutoSave) openedEditor1).isAutoSaveEnabled();
    verify(document, times(2)).getContents();
    verify(document).replace(anyInt(), anyInt(), anyString());
    verify(documentEventBus)
        .addHandler(
            org.mockito.ArgumentMatchers.<DocumentChangedEvent.Type>anyObject(),
            eq(editorGroupSynchronization));
  }

  @Test
  public void shouldRemoveEditorFromGroup() {
    editorGroupSynchronization.addEditor(activeEditor);

    editorGroupSynchronization.removeEditor(activeEditor);

    verify(handlerRegistration).removeHandler();
  }

  @Test
  public void shouldRemoveAllEditorsFromGroup() {
    addEditorsToGroup();

    editorGroupSynchronization.unInstall();

    verify(handlerRegistration, times(3)).removeHandler();
  }

  @Test
  public void shouldNotApplyChangesFromNotActiveEditor() {
    DocumentHandle documentHandle1 = mock(DocumentHandle.class);
    when(documentChangeEvent.getDocument()).thenReturn(documentHandle1);
    when(documentHandle1.isSameAs(documentHandle)).thenReturn(false);

    editorGroupSynchronization.onDocumentChanged(documentChangeEvent);

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

    addEditorsToGroup();
    editorGroupSynchronization.onDocumentChanged(documentChangeEvent);

    verify(document, times(2)).replace(eq(offset), eq(removeCharCount), eq(text));
  }

  @Test
  public void shouldResolveAutoSave() {
    addEditorsToGroup();

    // AutoSave for active editor should always be enabled,
    // but AutoSave for other editors with the same path should be disabled
    verify(((EditorWithAutoSave) activeEditor)).enableAutoSave();
    verify(((EditorWithAutoSave) openedEditor1)).disableAutoSave();
    verify(((EditorWithAutoSave) openedEditor2)).disableAutoSave();
  }

  private void addEditorsToGroup() {
    editorGroupSynchronization.addEditor(openedEditor1);
    editorGroupSynchronization.addEditor(openedEditor2);
    editorGroupSynchronization.addEditor(activeEditor);

    editorGroupSynchronization.onActiveEditorChanged(activeEditor);
  }
}
