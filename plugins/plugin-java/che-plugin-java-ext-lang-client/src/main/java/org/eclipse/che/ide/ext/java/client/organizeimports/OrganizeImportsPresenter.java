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
package org.eclipse.che.ide.ext.java.client.organizeimports;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.texteditor.HandlesUndoRedo;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.editor.texteditor.UndoableEditor;
import org.eclipse.che.ide.api.filewatcher.ClientServerEventService;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.editor.JavaCodeAssistClient;
import org.eclipse.che.ide.ext.java.client.resource.SourceFolderMarker;
import org.eclipse.che.ide.ext.java.client.util.JavaUtil;
import org.eclipse.che.ide.ext.java.shared.dto.Change;
import org.eclipse.che.ide.ext.java.shared.dto.ConflictImportDTO;
import org.eclipse.che.ide.ext.java.shared.dto.OrganizeImportResult;
import org.eclipse.che.ide.util.loging.Log;

/**
 * The class that manages conflicts with organize imports if if they occur.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class OrganizeImportsPresenter implements OrganizeImportsView.ActionDelegate {
  private final OrganizeImportsView view;
  private final JavaCodeAssistClient javaCodeAssistClient;
  private final DtoFactory dtoFactory;
  private final JavaLocalizationConstant locale;
  private final NotificationManager notificationManager;
  private final ClientServerEventService clientServerEventService;

  private int page;
  private List<ConflictImportDTO> choices;
  private Map<Integer, String> selected;
  private VirtualFile file;
  private Document document;
  private EditorPartPresenter editor;

  @Inject
  public OrganizeImportsPresenter(
      OrganizeImportsView view,
      JavaCodeAssistClient javaCodeAssistClient,
      DtoFactory dtoFactory,
      JavaLocalizationConstant locale,
      NotificationManager notificationManager,
      ClientServerEventService clientServerEventService) {
    this.view = view;
    this.javaCodeAssistClient = javaCodeAssistClient;
    this.clientServerEventService = clientServerEventService;
    this.view.setDelegate(this);

    this.dtoFactory = dtoFactory;
    this.locale = locale;
    this.notificationManager = notificationManager;
  }

  /**
   * Make Organize imports operation. If the operation doesn't have conflicts all imports will be
   * applied otherwise a special window will be showed for resolving conflicts.
   *
   * @param editor current active editor
   */
  public void organizeImports(EditorPartPresenter editor) {
    this.editor = editor;
    this.document = ((TextEditor) editor).getDocument();
    this.file = editor.getEditorInput().getFile();

    if (file instanceof Resource) {
      final Optional<Project> project = ((Resource) file).getRelatedProject();

      final Optional<Resource> srcFolder =
          ((Resource) file).getParentWithMarker(SourceFolderMarker.ID);

      if (!srcFolder.isPresent()) {
        return;
      }

      final String fqn = JavaUtil.resolveFQN((Container) srcFolder.get(), (Resource) file);
      clientServerEventService
          .sendFileTrackingSuspendEvent()
          .then(
              arg -> {
                doOrganizeImports(fqn, project);
              });
    }
  }

  private Promise<OrganizeImportResult> doOrganizeImports(String fqn, Optional<Project> project) {
    return javaCodeAssistClient
        .organizeImports(project.get().getLocation().toString(), fqn)
        .then(
            result -> {
              if (result.getConflicts() != null && !result.getConflicts().isEmpty()) {
                show(result.getConflicts());
              } else {
                applyChanges(document, result.getChanges());
              }

              clientServerEventService.sendFileTrackingResumeEvent();
            })
        .catchError(
            error -> {
              String title = locale.failedToProcessOrganizeImports();
              String message = error.getMessage();
              notificationManager.notify(title, message, FAIL, FLOAT_MODE);

              clientServerEventService.sendFileTrackingResumeEvent();
            });
  }

  /** {@inheritDoc} */
  @Override
  public void onNextButtonClicked() {
    selected.put(page++, view.getSelectedImport());
    if (!selected.containsKey(page)) {
      String newSelection = choices.get(page).getTypeMatches().get(0);
      selected.put(page, newSelection);
    }
    view.setSelectedImport(selected.get(page));
    view.changePage(choices.get(page));
    updateButtonsState();
  }

  /** {@inheritDoc} */
  @Override
  public void onBackButtonClicked() {
    selected.put(page--, view.getSelectedImport());
    view.setSelectedImport(selected.get(page));
    view.changePage(choices.get(page));
    updateButtonsState();
  }

  /** {@inheritDoc} */
  @Override
  public void onFinishButtonClicked() {
    selected.put(page, view.getSelectedImport());

    ConflictImportDTO result =
        dtoFactory
            .createDto(ConflictImportDTO.class)
            .withTypeMatches(new ArrayList<>(selected.values()));

    if (file instanceof Resource) {
      final Optional<Project> project = ((Resource) file).getRelatedProject();

      javaCodeAssistClient
          .applyChosenImports(
              project.get().getLocation().toString(), JavaUtil.resolveFQN(file), result)
          .then(
              new Operation<List<Change>>() {
                @Override
                public void apply(List<Change> result) throws OperationException {
                  applyChanges(((TextEditor) editor).getDocument(), result);
                  view.hide();
                  ((TextEditor) editor).setFocus();
                }
              })
          .catchError(
              new Operation<PromiseError>() {
                @Override
                public void apply(PromiseError arg) throws OperationException {
                  String title = locale.failedToProcessOrganizeImports();
                  String message = arg.getMessage();
                  notificationManager.notify(title, message, FAIL, FLOAT_MODE);
                }
              });
    }
  }

  /** {@inheritDoc} */
  @Override
  public void onCancelButtonClicked() {
    ((TextEditor) editor).setFocus();
  }

  /** Show Organize Imports panel with the special information. */
  private void show(List<ConflictImportDTO> choices) {
    if (choices == null || choices.isEmpty()) {
      return;
    }

    this.choices = choices;

    page = 0;
    selected = new HashMap<>(choices.size());

    String selection = choices.get(0).getTypeMatches().get(0);
    selected.put(page, selection);
    view.setSelectedImport(selection);

    updateButtonsState();

    view.show(choices.get(page));
  }

  /**
   * Update content of the file.
   *
   * @param document current document
   * @param changes
   */
  private void applyChanges(Document document, List<Change> changes) {
    HandlesUndoRedo undoRedo = null;
    if (editor instanceof UndoableEditor) {
      undoRedo = ((UndoableEditor) editor).getUndoRedo();
    }
    try {
      if (undoRedo != null) {
        undoRedo.beginCompoundChange();
      }
      for (Change change : changes) {
        document.replace(change.getOffset(), change.getLength(), change.getText());
      }
    } catch (final Exception e) {
      Log.error(getClass(), e);
    } finally {
      if (undoRedo != null) {
        undoRedo.endCompoundChange();
      }
    }
  }

  private void updateButtonsState() {
    view.setEnableBackButton(!isFirstPage());
    view.setEnableNextButton(!isLastPage());
    view.setEnableFinishButton(selected.size() == choices.size());
  }

  private boolean isFirstPage() {
    return page == 0;
  }

  private boolean isLastPage() {
    return (choices.size() - 1) == page;
  }
}
