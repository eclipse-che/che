/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ext.git.client.compare;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.event.FileContentUpdateEvent;
import org.eclipse.che.ide.api.git.GitServiceClient;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.GitUtil;
import org.eclipse.che.ide.ext.git.client.compare.FileStatus.Status;
import org.eclipse.che.ide.api.dialogs.CancelCallback;
import org.eclipse.che.ide.api.dialogs.ConfirmCallback;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.ide.resource.Path;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.NOT_EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.ext.git.client.compare.FileStatus.Status.ADDED;
import static org.eclipse.che.ide.ext.git.client.compare.FileStatus.Status.DELETED;

/**
 * Presenter for comparing current files with files from specified revision or branch.
 *
 * @author Igor Vinokur
 * @author Vlad Zhukovskyi
 * @author Mykola Morhun
 */
@Singleton
public class ComparePresenter implements CompareView.ActionDelegate {

    private final AppContext              appContext;
    private final EventBus                eventBus;
    private final DialogFactory           dialogFactory;
    private final CompareView             view;
    private final GitServiceClient        service;
    private final GitLocalizationConstant locale;
    private final NotificationManager     notificationManager;

    private boolean      compareWithLatest;
    private ChangedItems changedItems;
    private int          currentItemIndex;

    private File    comparedFile;
    private String  revision;
    private String  localContent;

    private Path   projectLocation;
    private String revisionA;
    private String revisionB;

    @Inject
    public ComparePresenter(AppContext appContext,
                            EventBus eventBus,
                            DialogFactory dialogFactory,
                            CompareView view,
                            GitServiceClient service,
                            GitLocalizationConstant locale,
                            NotificationManager notificationManager) {
        this.appContext = appContext;
        this.eventBus = eventBus;
        this.dialogFactory = dialogFactory;
        this.view = view;
        this.service = service;
        this.locale = locale;
        this.notificationManager = notificationManager;
        this.view.setDelegate(this);
    }

    /**
     * Show compare window for given set of files between given revision and latest code version.
     *
     * @param changedItems
     *         ordered touched files
     * @param currentFile
     *         file which will be shown first, if null then the first from the list will be shown
     * @param revision
     *         hash of revision or branch
     */
    public void showCompareWithLatest(final ChangedItems changedItems,
                                      @Nullable final String currentFile,
                                      final String revision) {
        this.changedItems = changedItems;
        this.revision = revision;

        this.compareWithLatest = true;

        setupCurrentFile(currentFile);
        showCompareForCurrentFile();
    }

    /**
     * Shows compare window for given set of files between specified revisions.
     *
     * @param changedItems
     *         ordered touched files
     * @param currentFile
     *         file which will be shown first, if null then the first from the list will be shown
     * @param revisionA
     *         hash of the first revision or branch.
     *         If it is set to {@code null}, compare with empty repository state will be performed
     * @param revisionB
     *         hash of the second revision or branch.
     *         If it is set to {@code null}, compare with latest repository state will be performed
     */
    public void showCompareBetweenRevisions(final ChangedItems changedItems,
                                            @Nullable final String currentFile,
                                            @Nullable final String revisionA,
                                            @Nullable final String revisionB) {
        this.changedItems = changedItems;
        this.revisionA = revisionA;
        this.revisionB = revisionB;

        this.compareWithLatest = false;
        this.projectLocation = appContext.getRootProject().getLocation();

        setupCurrentFile(currentFile);
        showCompareForCurrentFile();
    }

    /**
     * Shows comparison for selected file.
     * Type of comparison to show depends on {@code compareWithLatest} field.
     */
    private void showCompareForCurrentFile() {
        view.setEnableNextDiffButton(currentItemIndex != (changedItems.getFilesQuantity() - 1));
        view.setEnablePreviousDiffButton(currentItemIndex != 0);

        changedItems.getProject()
                    .getFile(changedItems.getItemByIndex(currentItemIndex))
                    .then(file -> {
                        if (file.isPresent()) {
                            if (compareWithLatest) {
                                showCompareWithLatestForFile(file.get(), changedItems.getStatusByIndex(currentItemIndex));
                            } else {
                                showCompareBetweenRevisionsForFile(file.get(), changedItems.getStatusByIndex(currentItemIndex));
                            }
                        }
                    }).catchError(error -> {
                        notificationManager.notify(error.getMessage(), FAIL, NOT_EMERGE_MODE);
                    });
    }

    private void showCompareBetweenRevisionsForFile(final File file, final Status status) {
        // Get project/module in which git repository is located.
        final Container rootProject = GitUtil.getRootProject(file);
        if (rootProject == null) {
            return;
        }

        final Path relPath = file.getLocation().removeFirstSegments(rootProject.getLocation().segmentCount());

        view.setTitle(relPath.toString());
        if (status == Status.ADDED) {
            service.showFileContent(projectLocation, relPath, revisionB)
                   .then(response -> {
                       view.setColumnTitles(revisionB + locale.compareReadOnlyTitle(),
                                            revisionA == null ? "" : revisionA + locale.compareReadOnlyTitle());
                       view.show("", response.getContent(), relPath.toString(), true);
                   })
                   .catchError(error -> {
                       notificationManager.notify(error.getMessage(), FAIL, NOT_EMERGE_MODE);
                   });
        } else if (status == Status.DELETED) {
            service.showFileContent(projectLocation, relPath, revisionA)
                   .then(response -> {
                       view.setColumnTitles(revisionB + locale.compareReadOnlyTitle(), revisionA + locale.compareReadOnlyTitle());
                       view.show(response.getContent(), "", relPath.toString(), true);
                   })
                   .catchError(error -> {
                       notificationManager.notify(error.getMessage(), FAIL, NOT_EMERGE_MODE);
                   });
        } else {
            service.showFileContent(projectLocation, relPath, revisionA)
                   .then(contentAResponse -> {
                       service.showFileContent(projectLocation, relPath, revisionB)
                              .then(contentBResponse -> {
                                  view.setColumnTitles(revisionB + locale.compareReadOnlyTitle(),
                                                       revisionA + locale.compareReadOnlyTitle());
                                  view.show(contentAResponse.getContent(), contentBResponse.getContent(), relPath.toString(), true);
                              })
                              .catchError(error -> {
                                  notificationManager.notify(error.getMessage(), FAIL, NOT_EMERGE_MODE);
                              });
                   });
        }
    }

    private void showCompareWithLatestForFile(final File file, final Status status) {
        this.comparedFile = file;

        if (status.equals(ADDED)) {
            showCompare("");
            return;
        }

        // Get project/module in which git repository is located.
        final Container rootProject = GitUtil.getRootProject(file);
        if (rootProject == null) {
            return;
        }

        final Path relPath = file.getLocation().removeFirstSegments(rootProject.getLocation().segmentCount());

        if (status.equals(DELETED)) {
            service.showFileContent(rootProject.getLocation(), relPath, revision)
                   .then(content -> {
                       view.setTitle(file.getLocation().toString());
                       view.setColumnTitles(locale.compareYourVersionTitle(), revision + locale.compareReadOnlyTitle());
                       view.show(content.getContent(), "", file.getLocation().toString(), false);
                   })
                   .catchError(error -> {
                       notificationManager.notify(error.getMessage(), FAIL, NOT_EMERGE_MODE);
                   });
        } else {
            service.showFileContent(rootProject.getLocation(), relPath, revision)
                   .then(content -> {
                       showCompare(content.getContent());
                   })
                   .catchError(error -> {
                       notificationManager.notify(error.getMessage(), FAIL, NOT_EMERGE_MODE);
                   });
        }
    }

    @Override
    public void onClose(final String newContent) {
        if (!isEdited(newContent)) {
            view.hide();
            return;
        }

        ConfirmCallback confirmCallback = () -> {
            saveContent(newContent);
            view.hide();
        };

        CancelCallback cancelCallback = view::hide;

        dialogFactory.createConfirmDialog(locale.compareSaveTitle(), locale.compareSaveQuestion(), locale.buttonYes(), locale.buttonNo(),
                                          confirmCallback, cancelCallback).show();
    }

    @Override
    public void onSaveChangesClicked() {
        view.getEditableContent(content -> {
            if (isEdited(content)) {
                saveContent(content);
            }
        });
    }

    @Override
    public void onNextDiffClicked() {
        view.getEditableContent(content -> {
            if (isEdited(content)) {
                saveContent(content);
            }

            currentItemIndex++;
            showCompareForCurrentFile();
        });
    }

    @Override
    public void onPreviousDiffClicked() {
        view.getEditableContent(content -> {
            if (isEdited(content)) {
                saveContent(content);
            }

            currentItemIndex--;
            showCompareForCurrentFile();
        });
    }

    private void showCompare(final String remoteContent) {
        comparedFile.getContent().then(local -> {
            localContent = local;
            final String path = comparedFile.getLocation().removeFirstSegments(1).toString();
            view.setTitle(path);
            view.setColumnTitles(locale.compareYourVersionTitle(), revision + locale.compareReadOnlyTitle());
            view.show(remoteContent, localContent, path, false);
        });
    }

    /**
     * Searches for given file in the changes files list and save it sequential number to class field.
     *
     * @param currentFile
     *         name of file to set up as current; if null or invalid, the first one will be chosen.
     */
    private void setupCurrentFile(@Nullable  String currentFile) {
        currentItemIndex = changedItems.getChangedItemsList().indexOf(currentFile);
        if (currentItemIndex == -1) {
            currentItemIndex = 0;
        }
    }

    /** @return true if user edited content in the compare widget i.e. initial and current isn't equal. */
    private boolean isEdited(String newContent) {
        return compareWithLatest && this.localContent != null && !newContent.equals(localContent);
    }

    /** Saves given contents into file under edit. */
    private void saveContent(String content) {
        comparedFile.updateContent(content)
                    .then(ignored -> {
                        final Container parent = comparedFile.getParent();

                        if (parent != null) {
                            parent.synchronize();
                        }

                        eventBus.fireEvent(new FileContentUpdateEvent(comparedFile.getLocation()
                                                                                  .toString()));
                    })
                    .catchError(error -> {
                        notificationManager.notify(error.getMessage(), FAIL, NOT_EMERGE_MODE);
                    });
    }

}
