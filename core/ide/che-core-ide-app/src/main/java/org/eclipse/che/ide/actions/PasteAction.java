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
package org.eclipse.che.ide.actions;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.analytics.client.logger.AnalyticsEventLogger;
import org.eclipse.che.api.project.gwt.client.ProjectServiceClient;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.project.node.HasStorablePath;
import org.eclipse.che.ide.api.selection.Selection;
import org.eclipse.che.ide.commons.exception.ServerException;
import org.eclipse.che.ide.json.JsonHelper;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.project.node.ResourceBasedNode;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.ui.dialogs.CancelCallback;
import org.eclipse.che.ide.ui.dialogs.ConfirmCallback;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.eclipse.che.ide.ui.dialogs.InputCallback;
import org.eclipse.che.ide.ui.dialogs.choice.ChoiceDialog;

import java.util.List;

import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;

/**
 * Action to copy or move resources.
 *
 * @author Vitaliy Guliy
 */
@Singleton
public class PasteAction extends Action {

    private final AnalyticsEventLogger     eventLogger;
    private final CoreLocalizationConstant localization;
    private final AppContext               appContext;
    private final DialogFactory            dialogFactory;
    private final ProjectServiceClient     projectServiceClient;
    private final NotificationManager      notificationManager;
    private final ProjectExplorerPresenter projectExplorer;
    private final RenameItemAction         renameItemAction;
    private final String                   workspaceId;

    /** List of items to do. */
    private List<ResourceBasedNode<?>> items;

    /** Move items, don't copy. */
    private boolean moveItems = false;

    /** The path checked last time */
    private String checkedPath;

    /** Last checking result */
    private boolean checkResult;

    /** Index of current processing resource */
    private int itemIndex;

    /** Destination directory to paste. */
    private ResourceBasedNode<?> destination;


    @Inject
    public PasteAction(Resources resources,
                       AnalyticsEventLogger eventLogger,
                       CoreLocalizationConstant localization,
                       AppContext appContext,
                       DialogFactory dialogFactory,
                       ProjectServiceClient projectServiceClient,
                       NotificationManager notificationManager,
                       ProjectExplorerPresenter projectExplorer,
                       RenameItemAction renameItemAction) {
        super(localization.pasteItemsActionText(), localization.pasteItemsActionDescription(), null, resources.paste());
        this.eventLogger = eventLogger;
        this.localization = localization;
        this.appContext = appContext;
        this.dialogFactory = dialogFactory;
        this.projectServiceClient = projectServiceClient;
        this.notificationManager = notificationManager;
        this.projectExplorer = projectExplorer;
        this.renameItemAction = renameItemAction;
        
        this.workspaceId = appContext.getWorkspace().getId();
    }

    /** {@inheritDoc} */
    @Override
    public void update(ActionEvent e) {
        if ((appContext.getCurrentProject() == null && !appContext.getCurrentUser().isUserPermanent())) {
            e.getPresentation().setVisible(true);
            e.getPresentation().setEnabled(false);
            return;
        }

        e.getPresentation().setEnabled(canPaste());
    }

    /**
     * Sets list of items for copying.
     *
     * @param items
     *         items to copy
     */
    public void copyItems(List<ResourceBasedNode<?>> items) {
        this.items = items;
        moveItems = false;

        checkedPath = null;
    }

    /**
     * Sets list of items for moving.
     *
     * @param items
     *         items to move
     */
    public void moveItems(List<ResourceBasedNode<?>> items) {
        this.items = items;
        moveItems = true;

        checkedPath = null;
    }

    /**
     * Determines whether pasting can be performed.
     *
     * @return <b>true</b> if pasting can be performed, otherwise returns <b>false</b>
     */
    private boolean canPaste() {
        /** List of items must not be empty */
        if (items == null || items.isEmpty()) {
            return false;
        }

        /** Test current selection */
        Selection<?> selection = projectExplorer.getSelection();

        /** Only one resource must be selected */
        if (selection == null || !selection.isSingleSelection()) {
            return false;
        }

        Object headElement = selection.getHeadElement();

        /** Selected resource must be storable */
        if (!(headElement instanceof HasStorablePath
              && headElement instanceof ResourceBasedNode<?>
              && !((ResourceBasedNode)headElement).isLeaf())) {
            return false;
        }

        /** Test selected node */
        HasStorablePath selectedNode = (HasStorablePath)headElement;
        if (selectedNode.getStorablePath().equals(checkedPath)) {
            return checkResult;
        }
        checkedPath = selectedNode.getStorablePath();

        for (ResourceBasedNode<?> item : items) {

            if (item.isLeaf()) {
                // item is folder

                /** Unable to copy or move folder itself */
                if (((HasStorablePath)item).getStorablePath().equals(selectedNode.getStorablePath())) {
                    checkResult = false;
                    return false;
                }

                /** Unable to copy or move folder to its children */
                if (selectedNode.getStorablePath().startsWith(((HasStorablePath)item).getStorablePath())) {
                    checkResult = false;
                    return false;
                }

                /** Unable to move folder to its parent */
                if (moveItems) {
                    String folderDirectory = ((HasStorablePath)item).getStorablePath().substring(0,
                                                                                                 ((HasStorablePath)item).getStorablePath()
                                                                                                                        .lastIndexOf("/"));
                    if (!((ResourceBasedNode<?>)selectedNode).isLeaf()) {
                        // when selected a folder

                        if (selectedNode.getStorablePath().equals(folderDirectory)) {
                            checkResult = false;
                            return false;
                        }
                    } else {
                        // when selected a file

                        String fileDirectory = selectedNode.getStorablePath().substring(0, selectedNode.getStorablePath().lastIndexOf("/"));
                        if (fileDirectory.equals(fileDirectory)) {
                            checkResult = false;
                            return false;
                        }
                    }
                }

            } else {
                // item is file

                if (!((ResourceBasedNode<?>)selectedNode).isLeaf()) {
                    // when selected a folder

                    /** Unable to move file in the same directory */
                    if (moveItems) {
                        String folderPath = selectedNode.getStorablePath();
                        String fileDirectory = ((HasStorablePath)item).getStorablePath().substring(0,
                                                                                                   ((HasStorablePath)item).getStorablePath()
                                                                                                                          .lastIndexOf(
                                                                                                                                  "/"));

                        if (moveItems && folderPath.equals(fileDirectory)) {
                            checkResult = false;
                            return false;
                        }
                    }
                } else {
                    // when selected a file

                    /** Unable to move file in the same directory */
                    if (moveItems) {
                        String selectedFileDirectory =
                                selectedNode.getStorablePath().substring(0, selectedNode.getStorablePath().lastIndexOf("/"));
                        String fileDirectory = ((HasStorablePath)item).getStorablePath().substring(0,
                                                                                                   ((HasStorablePath)item).getStorablePath()
                                                                                                                          .lastIndexOf(
                                                                                                                                  "/"));

                        if (selectedFileDirectory.equals(fileDirectory)) {
                            checkResult = false;
                            return false;
                        }
                    }
                }

            }
        }

        checkResult = true;
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent e) {
        eventLogger.log(this);

        if (!canPaste()) {
            return;
        }

        /** Fetch destination directory from selection */
        destination = (ResourceBasedNode<?>)projectExplorer.getSelection().getHeadElement();
        /** Get parent directory if destination is file */
        if (destination.isLeaf()) {
            destination = (ResourceBasedNode<?>)destination.getParent();
        }

        /** Reset item index */
        itemIndex = -1;

        if (moveItems) {
            move();
        } else {
            copy();
        }
    }

    /**
     * Copies next item to destination.
     */
    private void copy() {
        /** Switch to next item and check item list */
        itemIndex++;
        if (itemIndex == items.size()) {
            /** Copying finished */
            return;
        }

        /** Get item to copy */
        final ResourceBasedNode<?> item = items.get(itemIndex);

        try {
            /** Copy the item */
            projectServiceClient
                    .copy(workspaceId, ((HasStorablePath)item).getStorablePath(), ((HasStorablePath)destination).getStorablePath(), null, copyCallback);
        } catch (Exception error) {
            /** Handle error and stop copying */
            notificationManager.notify(localization.failedToCopyItems(), error.getMessage(), FAIL, true, item.getProjectConfig());
            dialogFactory.createMessageDialog("ERROR", error.getMessage(), null).show();
        }
    }

    /**
     * Asks the user for a decision when copying existent resource.
     */
    private void resolveCopyConflict(String cause) {
        ChoiceDialog dialog = dialogFactory.createChoiceDialog("Copy conflict", cause, "Rename", "Skip", "Overwrite",
                                                               new ConfirmCallback() {
                                                                   @Override
                                                                   public void accepted() {
                                                                       /** Copy with new name */
                                                                       copyWithNewName();
                                                                   }
                                                               }, new ConfirmCallback() {
                    @Override
                    public void accepted() {
                        /** Skip resource and copy next */
                        copy();
                    }
                }, new ConfirmCallback() {
                    @Override
                    public void accepted() {
                        /** Copy with overwriting existent resource */
                        copyWithOverwriting();
                    }
                }
                                                              );
        dialog.show();
    }

    /**
     * Asks the user for new item name and retries copying.
     */
    private void copyWithNewName() {
        /** Get item to copy */
        final ResourceBasedNode<?> item = items.get(itemIndex);

        /** Ask user for new resource name. */
        renameItemAction.askForNewName(item, new InputCallback() {
            @Override
            public void accepted(String value) {
                try {
                    /** Copy the item, giving new name */
                    projectServiceClient
                            .copy(workspaceId, ((HasStorablePath)item).getStorablePath(), ((HasStorablePath)destination).getStorablePath(), value,
                                  copyCallback);
                } catch (Exception error) {
                    /** Handle error and stop copying */
                    notificationManager.notify(localization.failedToCopyItems(), error.getMessage(), FAIL, true, item.getProjectConfig());
                    dialogFactory.createMessageDialog("ERROR", error.getMessage(), null).show();
                }
            }
        }, new CancelCallback() {
            @Override
            public void cancelled() {
                /** Stop copying and do nothing */
            }
        });
    }

    /**
     * Copies with overwriting.
     * Delete destination resource and copy again.
     */
    private void copyWithOverwriting() {
        /** Get item to copy */
        final ResourceBasedNode<?> item = items.get(itemIndex);

        try {
            /** Delete destination item */
            String deletePath = ((HasStorablePath)destination).getStorablePath() + "/" + item.getName();
            projectServiceClient.delete(workspaceId, deletePath, new AsyncRequestCallback<Void>() {
                @Override
                protected void onSuccess(Void result) {
                    /** Copy the item */
                    projectServiceClient
                            .copy(workspaceId, ((HasStorablePath)item).getStorablePath(), ((HasStorablePath)destination).getStorablePath(), null,
                                  copyCallback);
                }

                @Override
                protected void onFailure(Throwable error) {
                    /** Handle error and stop copying */
                    notificationManager.notify(localization.failedToCopyItems(), error.getMessage(), FAIL, true, item.getProjectConfig());
                    dialogFactory.createMessageDialog("ERROR", error.getMessage(), null).show();
                }
            });
        } catch (Exception error) {
            /** Handle error and stop copying */
            notificationManager.notify(localization.failedToCopyItems(), error.getMessage(), FAIL, true, item.getProjectConfig());
            dialogFactory.createMessageDialog("ERROR", error.getMessage(), null).show();
        }
    }

    /**
     * Callback for copy operation.
     */
    private final AsyncRequestCallback<Void> copyCallback = new AsyncRequestCallback<Void>() {
        @Override
        protected void onSuccess(Void result) {
            /** Item copied, refresh project explorer */
            projectExplorer.reloadChildren(destination);
            copy();
        }

        @Override
        protected void onFailure(Throwable exception) {
            /** Check for conflict */
            if (exception instanceof ServerException && ((ServerException)exception).getHTTPStatus() == 409) {
                /** Resolve conflicting situation */
                String message = JsonHelper.parseJsonMessage(exception.getMessage());
                resolveCopyConflict(message);
                return;
            }

            /** Handle error and stop copying */
            notificationManager
                    .notify(localization.failedToCopyItems(), exception.getMessage(), FAIL, true, destination.getProjectConfig());
            dialogFactory.createMessageDialog("ERROR", exception.getMessage(), null).show();
        }
    };

    /**
     * Moves next item to destination.
     */
    private void move() {
        /** Switch to next item and check item list */
        itemIndex++;
        if (items.isEmpty() || itemIndex == items.size()) {
            items.clear();
            /** Moving finished */
            return;
        }

        /** Get item to move */
        final ResourceBasedNode<?> item = items.get(itemIndex);

        try {
            /** Move the item */
            projectServiceClient
                    .move(workspaceId, ((HasStorablePath)item).getStorablePath(), ((HasStorablePath)destination).getStorablePath(), null, moveCallback);
        } catch (Exception error) {
            /** Handle error and stop moving */
            notificationManager.notify(localization.failedToMoveItems(), error.getMessage(), FAIL, true, item.getProjectConfig());
            dialogFactory.createMessageDialog("ERROR", error.getMessage(), null).show();

            /** Clears item list to disable Paste button */
            items.clear();
        }
    }

    /**
     * Asks the user for a decision when moving existent resource.
     */
    private void resolveMoveConflict(String cause) {
        ChoiceDialog dialog = dialogFactory.createChoiceDialog("Move conflict", cause, "Rename", "Skip", "Overwrite",
                                                               new ConfirmCallback() {
                                                                   @Override
                                                                   public void accepted() {
                                                                       /** Rename */
                                                                       moveWithNewName();
                                                                   }
                                                               }, new ConfirmCallback() {
                    @Override
                    public void accepted() {
                        /** Skip */
                        move();
                    }
                }, new ConfirmCallback() {
                    @Override
                    public void accepted() {
                        /** Overwrite */
                        moveWithOverwriting();
                    }
                }
                                                              );
        dialog.show();
    }

    /**
     * Asks the user for new item name and retries moving.
     */
    private void moveWithNewName() {
        /** Get item to move */
        final ResourceBasedNode<?> item = items.get(itemIndex);

        /** Ask user for new resource name. */
        renameItemAction.askForNewName(item, new InputCallback() {
            @Override
            public void accepted(String value) {
                try {
                    /** Move the item, giving new name */
                    projectServiceClient
                            .move(workspaceId, ((HasStorablePath)item).getStorablePath(), ((HasStorablePath)destination).getStorablePath(), value,
                                  moveCallback);
                } catch (Exception error) {
                    /** Handle error and stop moving */
                    notificationManager.notify(localization.failedToMoveItems(), error.getMessage(), FAIL, true, item.getProjectConfig());
                    dialogFactory.createMessageDialog("ERROR", error.getMessage(), null).show();

                    /** Clears item list to disable Paste button */
                    items.clear();
                }
            }
        }, new CancelCallback() {
            @Override
            public void cancelled() {
                /** Stop moving and clears item list to disable Paste button */
                items.clear();
            }
        });
    }

    /**
     * Moves with overwriting.
     * Delete destination resource and move again.
     */
    private void moveWithOverwriting() {
        /** Get item to move */
        final ResourceBasedNode<?> item = items.get(itemIndex);

        try {
            /** Delete destination item */
            String deletePath = ((HasStorablePath)destination).getStorablePath() + "/" + item.getName();
            projectServiceClient.delete(workspaceId, deletePath, new AsyncRequestCallback<Void>() {
                @Override
                protected void onSuccess(Void result) {
                    /** Move the item */
                    projectServiceClient
                            .move(workspaceId, ((HasStorablePath)item).getStorablePath(), ((HasStorablePath)destination).getStorablePath(), null,
                                  moveCallback);
                }

                @Override
                protected void onFailure(Throwable error) {
                    /** Handle error and stop moving */
                    notificationManager.notify(localization.failedToMoveItems(), error.getMessage(), FAIL, true, item.getProjectConfig());
                    dialogFactory.createMessageDialog("ERROR", error.getMessage(), null).show();

                    /** Clears item list to disable Paste button */
                    items.clear();
                }
            });
        } catch (Exception error) {
            /** Handle error and stop copying */
            notificationManager.notify(localization.failedToMoveItems(), error.getMessage(), FAIL, true, item.getProjectConfig());
            dialogFactory.createMessageDialog("ERROR", error.getMessage(), null).show();

            /** Clears item list to disable Paste button */
            items.clear();
        }
    }

    /**
     * Callback for move operation.
     */
    private final AsyncRequestCallback<Void> moveCallback = new AsyncRequestCallback<Void>() {
        @Override
        protected void onSuccess(Void result) {
            /** Item moved, refresh project explorer */
            /** Source and destination directories are to be refreshed */
            refreshSourcePath();
        }

        @Override
        protected void onFailure(Throwable exception) {
            /** Check for conflict */
            if (exception instanceof ServerException && ((ServerException)exception).getHTTPStatus() == 409) {
                /** Resolve conflicting situation */
                String message = JsonHelper.parseJsonMessage(exception.getMessage());
                resolveMoveConflict(message);
                return;
            }

            /** Handle error and stop moving */
            notificationManager
                    .notify(localization.failedToMoveItems(), exception.getMessage(), FAIL, true, destination.getProjectConfig());
            dialogFactory.createMessageDialog("ERROR", exception.getMessage(), null).show();

            /** Clears item list to disable Paste button */
            items.clear();
        }
    };

    /**
     * Refreshes item parent directory.
     */
    private void refreshSourcePath() {
        projectExplorer.reloadChildren(items.get(itemIndex).getParent());
        refreshDestinationPath();
    }

    /**
     * Refreshes destination directory.
     */
    private void refreshDestinationPath() {
        projectExplorer.reloadChildren(destination);
        move();
    }
}
