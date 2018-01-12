/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ext.java.client.organizeimports;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.base.Optional;
import com.google.gwtmockito.GwtMockitoTestRunner;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.editor.EditorInput;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.filewatcher.ClientServerEventService;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.editor.JavaCodeAssistClient;
import org.eclipse.che.ide.ext.java.client.resource.SourceFolderMarker;
import org.eclipse.che.ide.ext.java.shared.dto.Change;
import org.eclipse.che.ide.ext.java.shared.dto.ConflictImportDTO;
import org.eclipse.che.ide.ext.java.shared.dto.OrganizeImportResult;
import org.eclipse.che.ide.resource.Path;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;

/**
 * @author Valeriy Svydenko
 * @author Vlad Zhukovskyi
 */
@RunWith(GwtMockitoTestRunner.class)
public class OrganizeImportsPresenterTest {
  @Mock private OrganizeImportsView view;
  @Mock private JavaCodeAssistClient javaCodeAssistClient;
  @Mock private DtoFactory dtoFactory;
  @Mock private JavaLocalizationConstant locale;
  @Mock private NotificationManager notificationManager;
  @Mock private ClientServerEventService clientServerEventService;

  private OrganizeImportsPresenter presenter;
  @Mock private File file;
  @Mock private Project relatedProject;
  @Mock private Container srcFolder;
  @Mock private EditorInput editorInput;
  @Mock private TextEditor editor;

  @Mock private Document document;
  @Mock private Promise<OrganizeImportResult> importsPromise;
  @Mock private Promise<List<Change>> resolveConflictsPromise;
  @Mock private Promise<String> contentPromise;
  @Mock private Promise<Boolean> fileTrackingSuspendEventPromise;
  @Mock private OrganizeImportResult organizeImportResult;
  @Mock private Change change;

  @Captor private ArgumentCaptor<Operation<OrganizeImportResult>> importsOperation;
  @Captor private ArgumentCaptor<Operation<List<Change>>> resolveConflictsOperation;
  @Captor private ArgumentCaptor<Operation<String>> contentCaptor;
  @Captor private ArgumentCaptor<Operation<Boolean>> clientServerSuspendOperation;

  private ConflictImportDTO conflict1;
  private ConflictImportDTO conflict2;

  @Before
  public void setUp() throws Exception {
    when(editor.getEditorInput()).thenReturn(editorInput);
    when(editorInput.getFile()).thenReturn(file);
    when(editor.getDocument()).thenReturn(document);
    when(document.getContents()).thenReturn("content");
    when(file.getRelatedProject()).thenReturn(Optional.of(relatedProject));
    when(file.getParentWithMarker(eq(SourceFolderMarker.ID))).thenReturn(Optional.of(srcFolder));
    when(file.getName()).thenReturn("A.java");
    when(file.getLocation()).thenReturn(Path.valueOf("/project/src/a/b/A.java"));
    when(file.getExtension()).thenReturn("java");
    when(file.getResourceType()).thenReturn(Resource.FILE);
    when(srcFolder.getLocation()).thenReturn(Path.valueOf("/project/src"));
    when(relatedProject.getLocation()).thenReturn(Path.valueOf("/project"));

    when(clientServerEventService.sendFileTrackingSuspendEvent())
        .thenReturn(fileTrackingSuspendEventPromise);
    when(fileTrackingSuspendEventPromise.then(
            org.mockito.ArgumentMatchers.<Operation<Boolean>>anyObject()))
        .thenReturn(fileTrackingSuspendEventPromise);

    when(javaCodeAssistClient.organizeImports(anyString(), anyString())).thenReturn(importsPromise);
    when(importsPromise.then(
            org.mockito.ArgumentMatchers.<Operation<OrganizeImportResult>>anyObject()))
        .thenReturn(importsPromise);

    presenter =
        new OrganizeImportsPresenter(
            view,
            javaCodeAssistClient,
            dtoFactory,
            locale,
            notificationManager,
            clientServerEventService);

    prepareConflicts();
  }

  @Test
  public void organizeImportsShouldBeDoneWithoutConflicts() throws Exception {
    when(file.getContent()).thenReturn(contentPromise);
    when(contentPromise.then(any(Operation.class))).thenReturn(contentPromise);
    when(organizeImportResult.getConflicts()).thenReturn(null);
    when(organizeImportResult.getChanges()).thenReturn(Collections.singletonList(change));
    when(change.getOffset()).thenReturn(0);
    when(change.getLength()).thenReturn("content".length());
    when(change.getText()).thenReturn("content");

    presenter.organizeImports(editor);

    verify(fileTrackingSuspendEventPromise).then(clientServerSuspendOperation.capture());
    clientServerSuspendOperation.getValue().apply(null);

    verify(javaCodeAssistClient).organizeImports(eq("/project"), eq("a.b.A"));
    verify(importsPromise).then(importsOperation.capture());
    importsOperation.getValue().apply(organizeImportResult);

    verify(document).replace(eq(0), eq("content".length()), eq("content"));
  }

  private void prepareConflicts() {
    conflict1 = Mockito.mock(ConflictImportDTO.class);
    conflict2 = Mockito.mock(ConflictImportDTO.class);

    List<String> imports1 = Arrays.asList("import1", "import2");
    List<String> imports2 = Arrays.asList("import3", "import4");

    when(conflict1.getTypeMatches()).thenReturn(imports1);
    when(conflict2.getTypeMatches()).thenReturn(imports2);
  }

  @Test
  public void openWindowForResolvingConflicts() throws Exception {
    showOrganizeImportsWindow();

    verify(view).setSelectedImport("import1");

    verify(view).setEnableBackButton(false);
    verify(view).setEnableFinishButton(false);
    verify(view).setEnableNextButton(true);

    verify(view).show(conflict1);
    verify(clientServerEventService).sendFileTrackingResumeEvent();
  }

  private void showOrganizeImportsWindow() throws Exception {
    presenter.organizeImports(editor);

    verify(fileTrackingSuspendEventPromise).then(clientServerSuspendOperation.capture());
    clientServerSuspendOperation.getValue().apply(null);

    List<ConflictImportDTO> result = Arrays.asList(conflict1, conflict2);
    when(organizeImportResult.getConflicts()).thenReturn(result);

    verify(javaCodeAssistClient).organizeImports(eq("/project"), eq("a.b.A"));
    verify(importsPromise).then(importsOperation.capture());
    importsOperation.getValue().apply(organizeImportResult);
  }

  @Test
  public void showNextConflictPage() throws Exception {
    when(view.getSelectedImport()).thenReturn("import1");

    showOrganizeImportsWindow();
    presenter.onNextButtonClicked();

    verify(view).getSelectedImport();
    verify(view).setSelectedImport("import3");
    verify(view).changePage(conflict2);

    verify(view).setEnableBackButton(true);
    verify(view).setEnableFinishButton(true);
    verify(view).setEnableNextButton(false);
    verify(clientServerEventService).sendFileTrackingResumeEvent();
  }

  @Test
  public void showPreviousConflictPage() throws Exception {
    when(view.getSelectedImport()).thenReturn("import4");

    showOrganizeImportsWindow();
    presenter.onNextButtonClicked();
    reset(view);
    presenter.onBackButtonClicked();

    verify(view).getSelectedImport();
    verify(view).setSelectedImport("import4");
    verify(view).changePage(conflict1);

    verify(view).setEnableBackButton(false);
    verify(view).setEnableFinishButton(true);
    verify(view).setEnableNextButton(true);
    verify(clientServerEventService).sendFileTrackingResumeEvent();
  }

  @Test
  public void focusShouldBeSetAfterClosingWindow() throws Exception {
    showOrganizeImportsWindow();
    presenter.onCancelButtonClicked();

    verify(editor).setFocus();
  }

  @Test
  public void focusShouldBeSetAfterApplyingConflicts() throws Exception {
    when(view.getSelectedImport()).thenReturn("import1");
    when(dtoFactory.createDto(ConflictImportDTO.class)).thenReturn(conflict1);
    when(conflict1.withTypeMatches(org.mockito.ArgumentMatchers.<List<String>>anyObject()))
        .thenReturn(conflict1);
    when(javaCodeAssistClient.applyChosenImports(anyString(), anyString(), any()))
        .thenReturn(resolveConflictsPromise);
    when(resolveConflictsPromise.then(
            org.mockito.ArgumentMatchers.<Operation<List<Change>>>anyObject()))
        .thenReturn(resolveConflictsPromise);

    showOrganizeImportsWindow();
    presenter.onFinishButtonClicked();

    verify(resolveConflictsPromise).then(resolveConflictsOperation.capture());
    resolveConflictsOperation.getValue().apply(null);

    verify(editor).setFocus();
    verify(view).hide();
    verify(clientServerEventService).sendFileTrackingResumeEvent();
  }
}
