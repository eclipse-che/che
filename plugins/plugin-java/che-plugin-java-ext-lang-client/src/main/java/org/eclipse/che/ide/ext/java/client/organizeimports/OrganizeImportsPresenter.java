/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.filewatcher.ClientServerEventService;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.resource.SourceFolderMarker;
import org.eclipse.che.ide.ext.java.client.service.JavaLanguageExtensionServiceClient;
import org.eclipse.che.jdt.ls.extension.api.dto.OrganizeImportParams;
import org.eclipse.che.jdt.ls.extension.api.dto.OrganizeImportsResult;
import org.eclipse.che.plugin.languageserver.ide.editor.quickassist.ApplyWorkspaceEditAction;

/**
 * The class that manages conflicts with organize imports if if they occur.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class OrganizeImportsPresenter implements OrganizeImportsView.ActionDelegate {
  private final OrganizeImportsView view;
  private final JavaLanguageExtensionServiceClient client;
  private final DtoFactory dtoFactory;
  private final JavaLocalizationConstant locale;
  private final NotificationManager notificationManager;
  private final ClientServerEventService clientServerEventService;
  private final ApplyWorkspaceEditAction applyWorkspaceEditAction;

  private int page;
  private List<List<String>> ambiguousTypes;
  private Map<Integer, String> selected;
  private VirtualFile file;
  private EditorPartPresenter editor;

  @Inject
  public OrganizeImportsPresenter(
      OrganizeImportsView view,
      JavaLanguageExtensionServiceClient client,
      DtoFactory dtoFactory,
      JavaLocalizationConstant locale,
      NotificationManager notificationManager,
      ClientServerEventService clientServerEventService,
      ApplyWorkspaceEditAction applyWorkspaceEditAction) {
    this.view = view;
    this.client = client;
    this.clientServerEventService = clientServerEventService;
    this.applyWorkspaceEditAction = applyWorkspaceEditAction;
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
    this.file = editor.getEditorInput().getFile();

    if (file instanceof Resource) {
      final Optional<Resource> srcFolder =
          ((Resource) file).getParentWithMarker(SourceFolderMarker.ID);

      if (!srcFolder.isPresent()) {
        return;
      }

      clientServerEventService
          .sendFileTrackingSuspendEvent()
          .then(
              success -> {
                doOrganizeImports(file.getLocation().toString());
              });
    }
  }

  private Promise<OrganizeImportsResult> doOrganizeImports(String path) {
    OrganizeImportParams organizeImports = dtoFactory.createDto(OrganizeImportParams.class);
    organizeImports.setChoices(Collections.emptyList());
    organizeImports.setResourceUri(path);

    return client
        .organizeImports(organizeImports)
        .then(
            result -> {
              try {
                if (result.getAmbiguousTypes() != null && !result.getAmbiguousTypes().isEmpty()) {
                  show(result.getAmbiguousTypes());
                } else {
                  applyWorkspaceEditAction.applyWorkspaceEdit(result.getWorkspaceEdit());
                }
              } finally {
                clientServerEventService.sendFileTrackingResumeEvent();
              }
            })
        .catchError(
            error -> {
              try {
                String title = locale.failedToProcessOrganizeImports();
                String message = error.getMessage();
                notificationManager.notify(title, message, FAIL, FLOAT_MODE);
              } finally {
                clientServerEventService.sendFileTrackingResumeEvent();
              }
            });
  }

  /** {@inheritDoc} */
  @Override
  public void onNextButtonClicked() {
    selected.put(page++, view.getSelectedImport());
    if (!selected.containsKey(page)) {
      String newSelection = ambiguousTypes.get(page).get(0);
      selected.put(page, newSelection);
    }
    view.setSelectedImport(selected.get(page));
    view.changePage(ambiguousTypes.get(page));
    updateButtonsState();
  }

  /** {@inheritDoc} */
  @Override
  public void onBackButtonClicked() {
    selected.put(page--, view.getSelectedImport());
    view.setSelectedImport(selected.get(page));
    view.changePage(ambiguousTypes.get(page));
    updateButtonsState();
  }

  /** {@inheritDoc} */
  @Override
  public void onFinishButtonClicked() {
    selected.put(page, view.getSelectedImport());

    OrganizeImportParams organizeImports = dtoFactory.createDto(OrganizeImportParams.class);
    organizeImports.setResourceUri(file.getLocation().toString());
    organizeImports.setChoices(new ArrayList<>(selected.values()));

    clientServerEventService
        .sendFileTrackingSuspendEvent()
        .then(
            successful -> {
              client
                  .organizeImports(organizeImports)
                  .then(
                      result -> {
                        try {
                          applyWorkspaceEditAction.applyWorkspaceEdit(result.getWorkspaceEdit());
                        } finally {
                          clientServerEventService.sendFileTrackingResumeEvent();
                          view.close();
                        }
                      })
                  .catchError(
                      error -> {
                        try {
                          String title = locale.failedToProcessOrganizeImports();
                          String message = error.getMessage();
                          notificationManager.notify(title, message, FAIL, FLOAT_MODE);
                        } finally {
                          clientServerEventService.sendFileTrackingResumeEvent();
                          view.close();
                        }
                      });
            });
  }

  /** {@inheritDoc} */
  @Override
  public void onCancelButtonClicked() {
    ((TextEditor) editor).setFocus();
  }

  /** Show Organize Imports panel with the special information. */
  private void show(List<List<String>> ambiguousTypes) {
    if (ambiguousTypes == null || ambiguousTypes.isEmpty()) {
      return;
    }

    this.ambiguousTypes = ambiguousTypes;

    page = 0;
    selected = new HashMap<>(ambiguousTypes.size());

    String selection = ambiguousTypes.get(0).get(0);
    selected.put(page, selection);
    view.setSelectedImport(selection);

    updateButtonsState();

    view.show(ambiguousTypes.get(page));
  }

  private void updateButtonsState() {
    view.setEnableBackButton(!isFirstPage());
    view.setEnableNextButton(!isLastPage());
    view.setEnableFinishButton(selected.size() == ambiguousTypes.size());
  }

  private boolean isFirstPage() {
    return page == 0;
  }

  private boolean isLastPage() {
    return (ambiguousTypes.size() - 1) == page;
  }
}
