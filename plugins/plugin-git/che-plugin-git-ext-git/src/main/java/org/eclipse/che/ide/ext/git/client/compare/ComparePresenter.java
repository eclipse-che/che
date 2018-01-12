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
package org.eclipse.che.ide.ext.git.client.compare;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.NOT_EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.ext.git.client.compare.FileStatus.Status.ADDED;
import static org.eclipse.che.ide.ext.git.client.compare.FileStatus.Status.DELETED;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.editor.events.FileContentUpdateEvent;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.GitServiceClient;
import org.eclipse.che.ide.ext.git.client.compare.FileStatus.Status;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.ui.dialogs.CancelCallback;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.eclipse.che.ide.ui.dialogs.confirm.ConfirmCallback;

/**
 * Presenter for comparing current files with files from specified revision or branch.
 *
 * @author Igor Vinokur
 * @author Vlad Zhukovskyi
 * @author Mykola Morhun
 */
@Singleton
public class ComparePresenter implements CompareView.ActionDelegate {

  private final EventBus eventBus;
  private final DialogFactory dialogFactory;
  private final CompareView view;
  private final GitServiceClient service;
  private final GitLocalizationConstant locale;
  private final NotificationManager notificationManager;

  private boolean compareWithLatest;
  private AlteredFiles alteredFiles;
  private int currentFileIndex;
  private File comparedFile;
  private String revision;
  private String localContent;
  private String revisionA;
  private String revisionB;

  @Inject
  public ComparePresenter(
      EventBus eventBus,
      DialogFactory dialogFactory,
      CompareView view,
      GitServiceClient service,
      GitLocalizationConstant locale,
      NotificationManager notificationManager) {

    this.eventBus = eventBus;
    this.dialogFactory = dialogFactory;
    this.view = view;
    this.service = service;
    this.locale = locale;
    this.notificationManager = notificationManager;
    this.view.setDelegate(this);
  }

  public boolean isShown() {
    return view.isVisible();
  }

  /**
   * Show compare window for given set of files between given revision and HEAD.
   *
   * @param alteredFiles ordered changed files
   * @param currentFile file which will be shown first, if null then the first from the list will be
   *     shown
   * @param revision hash of revision or branch
   */
  public void showCompareWithLatest(
      final AlteredFiles alteredFiles, @Nullable final String currentFile, final String revision) {
    this.alteredFiles = alteredFiles;
    this.revision = revision;

    this.compareWithLatest = true;

    currentFileIndex = findFileIndexOrFirst(currentFile);
    showCompareForCurrentFile();
  }

  /**
   * Shows compare window for given set of files between specified revisions.
   *
   * @param alteredFiles ordered changed files
   * @param currentFile file which will be shown first, if null then the first from the list will be
   *     shown
   * @param revisionA hash of the first revision or branch. If it is set to {@code null}, compare
   *     with empty repository state will be performed
   * @param revisionB hash of the second revision or branch. If it is set to {@code null}, compare
   *     with latest repository state will be performed
   */
  public void showCompareBetweenRevisions(
      final AlteredFiles alteredFiles,
      @Nullable final String currentFile,
      @Nullable final String revisionA,
      @Nullable final String revisionB) {
    this.alteredFiles = alteredFiles;
    this.revisionA = revisionA;
    this.revisionB = revisionB;

    this.compareWithLatest = false;

    currentFileIndex = findFileIndexOrFirst(currentFile);
    showCompareForCurrentFile();
  }

  /**
   * Shows comparison for selected file. Type of comparison to show depends on {@code
   * compareWithLatest} field.
   */
  private void showCompareForCurrentFile() {
    view.setEnableNextDiffButton(currentFileIndex != (alteredFiles.getFilesQuantity() - 1));
    view.setEnablePreviousDiffButton(currentFileIndex != 0);

    alteredFiles
        .getProject()
        .getFile(alteredFiles.getFileByIndex(currentFileIndex))
        .then(
            file -> {
              if (file.isPresent()) {
                this.comparedFile = file.get();
                view.setEnableSaveChangesButton(true);
              } else { // file is deleted
                this.comparedFile = null;
                view.setEnableSaveChangesButton(false);
              }

              // For now git repository supported only in project root folder
              final Path gitDirLocation = alteredFiles.getProject().getLocation();
              final Path relPath = Path.valueOf(alteredFiles.getFileByIndex(currentFileIndex));

              if (compareWithLatest) {
                showCompareWithLatestForFile(
                    gitDirLocation, relPath, alteredFiles.getStatusByIndex(currentFileIndex));
              } else {
                showCompareBetweenRevisionsForFile(
                    gitDirLocation, relPath, alteredFiles.getStatusByIndex(currentFileIndex));
              }
            })
        .catchError(
            error -> {
              notificationManager.notify(error.getMessage(), FAIL, NOT_EMERGE_MODE);
            });
  }

  private void showCompareWithLatestForFile(
      final Path gitDirLocation, final Path relPath, final Status status) {
    if (status.equals(ADDED)) {
      showCompare("");
      return;
    }

    if (status.equals(DELETED)) {
      service
          .showFileContent(gitDirLocation, relPath, revision)
          .then(
              content -> {
                view.setTitle(getTitleForFile(relPath.toString()));
                view.setColumnTitles(
                    locale.compareYourVersionTitle(), revision + locale.compareReadOnlyTitle());
                view.show(content.getContent(), "", relPath.toString(), true);
              })
          .catchError(
              error -> {
                notificationManager.notify(error.getMessage(), FAIL, NOT_EMERGE_MODE);
              });
    } else {
      service
          .showFileContent(gitDirLocation, relPath, revision)
          .then(
              content -> {
                showCompare(content.getContent());
              })
          .catchError(
              error -> {
                notificationManager.notify(error.getMessage(), FAIL, NOT_EMERGE_MODE);
              });
    }
  }

  private void showCompareBetweenRevisionsForFile(
      final Path gitDir, final Path relPath, final Status status) {
    view.setTitle(getTitleForFile(relPath.toString()));

    if (status == Status.ADDED) {
      service
          .showFileContent(gitDir, relPath, revisionB)
          .then(
              response -> {
                view.setColumnTitles(
                    revisionB + locale.compareReadOnlyTitle(),
                    revisionA == null ? "" : revisionA + locale.compareReadOnlyTitle());
                view.show("", response.getContent(), relPath.toString(), true);
              })
          .catchError(
              error -> {
                notificationManager.notify(error.getMessage(), FAIL, NOT_EMERGE_MODE);
              });
    } else if (status == Status.DELETED) {
      service
          .showFileContent(gitDir, relPath, revisionA)
          .then(
              response -> {
                view.setColumnTitles(
                    revisionB + locale.compareReadOnlyTitle(),
                    revisionA + locale.compareReadOnlyTitle());
                view.show(response.getContent(), "", relPath.toString(), true);
              })
          .catchError(
              error -> {
                notificationManager.notify(error.getMessage(), FAIL, NOT_EMERGE_MODE);
              });
    } else {
      service
          .showFileContent(gitDir, relPath, revisionA)
          .then(
              contentAResponse -> {
                service
                    .showFileContent(gitDir, relPath, revisionB)
                    .then(
                        contentBResponse -> {
                          view.setColumnTitles(
                              revisionB + locale.compareReadOnlyTitle(),
                              revisionA + locale.compareReadOnlyTitle());
                          view.show(
                              contentAResponse.getContent(),
                              contentBResponse.getContent(),
                              relPath.toString(),
                              true);
                        })
                    .catchError(
                        error -> {
                          notificationManager.notify(error.getMessage(), FAIL, NOT_EMERGE_MODE);
                        });
              });
    }
  }

  @Override
  public void onClose() {
    final String newContent = view.getEditableContent();

    if (!isSaveNeeded(newContent)) {
      view.hide();
      return;
    }

    ConfirmCallback confirmCallback =
        () -> {
          saveContent(newContent);
          view.hide();
        };

    CancelCallback cancelCallback = view::hide;

    dialogFactory
        .createConfirmDialog(
            locale.compareSaveTitle(),
            locale.compareSaveQuestion(),
            locale.buttonYes(),
            locale.buttonNo(),
            confirmCallback,
            cancelCallback)
        .show();
  }

  @Override
  public void onSaveChangesClicked() {
    if (compareWithLatest) {
      final String content = view.getEditableContent();
      if (isSaveNeeded(content)) {
        saveContent(content);
      }
    }
  }

  @Override
  public void onNextDiffClicked() {
    if (currentFileIndex < alteredFiles.getFilesQuantity() - 1) {
      onSaveChangesClicked();

      currentFileIndex++;
      showCompareForCurrentFile();
    }
  }

  @Override
  public void onPreviousDiffClicked() {
    if (currentFileIndex > 0) {
      onSaveChangesClicked();

      currentFileIndex--;
      showCompareForCurrentFile();
    }
  }

  private void showCompare(final String remoteContent) {
    comparedFile
        .getContent()
        .then(
            local -> {
              localContent = local;
              final String path = comparedFile.getLocation().removeFirstSegments(1).toString();
              view.setTitle(getTitleForFile(path));
              view.setColumnTitles(
                  locale.compareYourVersionTitle(), revision + locale.compareReadOnlyTitle());
              view.show(remoteContent, localContent, path, false);
            });
  }

  /**
   * Searches for given file in the changes files list and save it sequential number to class field.
   *
   * @param currentFile name of file to set up as current; if null or invalid, the first one will be
   *     chosen.
   * @return given file index or first index if specified file isn't found.
   */
  private int findFileIndexOrFirst(@Nullable String currentFile) {
    if (currentFile == null) {
      return 0;
    }

    int fileIndex = alteredFiles.getAlteredFilesList().indexOf(currentFile);
    if (fileIndex == -1) {
      return 0;
    }
    return fileIndex;
  }

  /** Returns true if is required to save new content. */
  private boolean isSaveNeeded(final String newContent) {
    return compareWithLatest
        && comparedFile != null
        && this.localContent != null
        && !newContent.equals(localContent);
  }

  /** Saves given contents into file under edit. */
  private void saveContent(final String content) {
    localContent = content;
    comparedFile
        .updateContent(content)
        .then(
            ignored -> {
              final Container parent = comparedFile.getParent();

              if (parent != null) {
                parent.synchronize();
              }

              eventBus.fireEvent(new FileContentUpdateEvent(comparedFile.getLocation().toString()));
            })
        .catchError(
            error -> {
              notificationManager.notify(error.getMessage(), FAIL, NOT_EMERGE_MODE);
            });
  }

  private String getTitleForFile(String file) {
    return "Review diff in: "
        + file
        + " ("
        + (currentFileIndex + 1)
        + '/'
        + alteredFiles.getFilesQuantity()
        + ')';
  }
}
