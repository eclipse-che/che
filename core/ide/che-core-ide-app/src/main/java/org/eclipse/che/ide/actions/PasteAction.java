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

import org.eclipse.che.api.project.gwt.client.ProjectServiceClient;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.project.node.HasStorablePath;
import org.eclipse.che.ide.api.project.node.Node;
import org.eclipse.che.ide.api.selection.Selection;
import org.eclipse.che.ide.commons.exception.ServerException;
import org.eclipse.che.ide.json.JsonHelper;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.project.node.ResourceBasedNode;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.ui.dialogs.CancelCallback;
import org.eclipse.che.ide.ui.dialogs.ConfirmCallback;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.eclipse.che.ide.ui.dialogs.InputCallback;
import org.eclipse.che.ide.ui.dialogs.choice.ChoiceDialog;

import java.util.List;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;

/**
 * Action to copy or move resources.
 *
 * @author Vitaliy Guliy
 */
@Singleton
public class PasteAction extends Action {

    private final CoreLocalizationConstant localization;
    private final AppContext               appContext;
    private final DialogFactory            dialogFactory;
    private final ProjectServiceClient     projectServiceClient;
    private final NotificationManager      notificationManager;
    private final ProjectExplorerPresenter projectExplorer;
    private final RenameItemAction         renameItemAction;

    /** List of items to do. */
    private List<ResourceBasedNode<?>> itemsToProcess;

    /** Move items, don't copy. */
    private boolean move;

    /** Index of current processing resource */
    private int itemIndex;

    /** Destination directory to paste. */
    private ResourceBasedNode<?> destination;

    @Inject
    public PasteAction(Resources resources,
                       CoreLocalizationConstant localization,
                       AppContext appContext,
                       DialogFactory dialogFactory,
                       ProjectServiceClient projectServiceClient,
                       NotificationManager notificationManager,
                       ProjectExplorerPresenter projectExplorer,
                       RenameItemAction renameItemAction) {
        super(localization.pasteItemsActionText(), localization.pasteItemsActionDescription(), null, resources.paste());
        this.localization = localization;
        this.appContext = appContext;
        this.dialogFactory = dialogFactory;
        this.projectServiceClient = projectServiceClient;
        this.notificationManager = notificationManager;
        this.projectExplorer = projectExplorer;
        this.renameItemAction = renameItemAction;
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
        itemsToProcess = items;
        move = false;
    }

    /**
     * Sets list of items for moving.
     *
     * @param items
     *         items to move
     */
    public void moveItems(List<ResourceBasedNode<?>> items) {
        itemsToProcess = items;
        move = true;
    }

    /**
     * Determines whether pasting can be performed.
     *
     * @return <b>true</b> if pasting can be performed, otherwise returns <b>false</b>
     */
    private boolean canPaste() {
        if (itemsToProcess == null || itemsToProcess.isEmpty()) {
            return false;
        }

        Selection<?> selection = projectExplorer.getSelection();
        if (selection == null || selection.isMultiSelection()) {
            return false;
        }

        final Node targetNode = selection.getHeadElement() instanceof Node ? (Node)selection.getHeadElement() : null;
        final Path targetPath = targetNode != null && targetNode instanceof HasStorablePath
                                ? Path.valueOf(((HasStorablePath)targetNode).getStorablePath())
                                : null;

        if (targetPath == null || targetNode.isLeaf()) {
            return false;
        }

        for (ResourceBasedNode<?> proceedItem : itemsToProcess) {

            if (!(proceedItem instanceof HasStorablePath)) {
                return false;
            }

            final Path proceedPath = Path.valueOf(((HasStorablePath)proceedItem).getStorablePath());

            if (proceedPath.equals(targetPath) || proceedPath.isPrefixOf(targetPath)) {
                //source == target or target path is prefix or source, e.g. src => /a/b/c, target => /a/b/c/d
                return false;
            } else if (targetPath.equals(proceedPath.removeLastSegments(1))) {
                //target == source's parent, e.g. src => /a/b/resource, target => /a/b, src parent => /a/b
                return false;
            } else if (!proceedItem.isLeaf() && targetNode.isLeaf() && targetPath.removeLastSegments(1).isPrefixOf(proceedPath)) {
                //source is folder, target is file, e.g. src => /a/b/c, target => /a/b/file, target parent => /a/b
                return false;
            }
        }

        return true;
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent e) {
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

        if (move) {
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
        if (itemIndex == itemsToProcess.size()) {
            /** Copying finished */
            if (itemIndex > 0) {
                projectExplorer.reloadChildren();
                itemsToProcess.clear();
            }
            return;
        }

        /** Get item to copy */
        final ResourceBasedNode<?> item = itemsToProcess.get(itemIndex);

        try {
            /** Copy the item */
            projectServiceClient
                    .copy(appContext.getDevMachine(), ((HasStorablePath)item).getStorablePath(), ((HasStorablePath)destination).getStorablePath(), null,
                          copyCallback);
        } catch (Exception error) {
            /** Handle error and stop copying */
            notificationManager.notify(localization.failedToCopyItems(), error.getMessage(), FAIL, FLOAT_MODE, item.getProjectConfig());
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
        final ResourceBasedNode<?> item = itemsToProcess.get(itemIndex);

        /** Ask user for new resource name. */
        renameItemAction.askForNewName(item, new InputCallback() {
            @Override
            public void accepted(String value) {
                try {
                    /** Copy the item, giving new name */
                    projectServiceClient
                            .copy(appContext.getDevMachine(), ((HasStorablePath)item).getStorablePath(), ((HasStorablePath)destination).getStorablePath(),
                                  value,
                                  copyCallback);
                } catch (Exception error) {
                    /** Handle error and stop copying */
                    notificationManager.notify(localization.failedToCopyItems(), error.getMessage(), FAIL, FLOAT_MODE, item.getProjectConfig());
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
        final ResourceBasedNode<?> item = itemsToProcess.get(itemIndex);

        try {
            /** Delete destination item */
            String deletePath = ((HasStorablePath)destination).getStorablePath() + "/" + item.getName();
            projectServiceClient.delete(appContext.getDevMachine(), deletePath, new AsyncRequestCallback<Void>() {
                @Override
                protected void onSuccess(Void result) {
                    /** Copy the item */
                    projectServiceClient
                            .copy(appContext.getDevMachine(), ((HasStorablePath)item).getStorablePath(), ((HasStorablePath)destination).getStorablePath(),
                                  null,
                                  copyCallback);
                }

                @Override
                protected void onFailure(Throwable error) {
                    /** Handle error and stop copying */
                    notificationManager.notify(localization.failedToCopyItems(), error.getMessage(), FAIL, FLOAT_MODE, item.getProjectConfig());
                    dialogFactory.createMessageDialog("ERROR", error.getMessage(), null).show();
                }
            });
        } catch (Exception error) {
            /** Handle error and stop copying */
            notificationManager.notify(localization.failedToCopyItems(), error.getMessage(), FAIL, FLOAT_MODE, item.getProjectConfig());
            dialogFactory.createMessageDialog("ERROR", error.getMessage(), null).show();
        }
    }

    /**
     * Callback for copy operation.
     */
    private final AsyncRequestCallback<Void> copyCallback = new AsyncRequestCallback<Void>() {
        @Override
        protected void onSuccess(Void result) {
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
                    .notify(localization.failedToCopyItems(), exception.getMessage(), FAIL, FLOAT_MODE, destination.getProjectConfig());
            dialogFactory.createMessageDialog("ERROR", exception.getMessage(), null).show();
        }
    };

    /**
     * Moves next item to destination.
     */
    private void move() {
        /** Switch to next item and check item list */
        itemIndex++;
        if (itemIndex == itemsToProcess.size()) {
            /** Moving finished */
            if (itemIndex > 0) {
                projectExplorer.reloadChildren();
                itemsToProcess.clear();
            }
            return;
        }

        /** Get item to move */
        final ResourceBasedNode<?> item = itemsToProcess.get(itemIndex);

        try {
            /** Move the item */
            projectServiceClient
                    .move(appContext.getDevMachine(), ((HasStorablePath)item).getStorablePath(), ((HasStorablePath)destination).getStorablePath(), null,
                          moveCallback);
        } catch (Exception error) {
            /** Handle error and stop moving */
            notificationManager.notify(localization.failedToMoveItems(), error.getMessage(), FAIL, FLOAT_MODE, item.getProjectConfig());
            dialogFactory.createMessageDialog("ERROR", error.getMessage(), null).show();

            /** Clears item list to disable Paste button */
            itemsToProcess.clear();
        }
    }

    /**
     * Asks the user for a decision when moving existent resource.
     */
    private void resolveMoveConflict(String cause) {
        dialogFactory.createChoiceDialog("Move conflict", cause, "Rename", "Skip", "Overwrite",
                                         new ConfirmCallback() {
                                             @Override
                                             public void accepted() {
                                                 /** Rename */
                                                 moveWithNewName();
                                             }
                                         },
                                         new ConfirmCallback() {
                                             @Override
                                             public void accepted() {
                                                 /** Skip */
                                                 move();
                                             }
                                         },
                                         new ConfirmCallback() {
                                             @Override
                                             public void accepted() {
                                                 /** Overwrite */
                                                 moveWithOverwriting();
                                             }
                                         }
                                        ).show();
    }

    /**
     * Asks the user for new item name and retries moving.
     */
    private void moveWithNewName() {
        /** Get item to move */
        final ResourceBasedNode<?> item = itemsToProcess.get(itemIndex);

        /** Ask user for new resource name. */
        renameItemAction.askForNewName(item, new InputCallback() {
            @Override
            public void accepted(String value) {
                try {
                    /** Move the item, giving new name */
                    projectServiceClient
                            .move(appContext.getDevMachine(), ((HasStorablePath)item).getStorablePath(), ((HasStorablePath)destination).getStorablePath(),
                                  value,
                                  moveCallback);
                } catch (Exception error) {
                    /** Handle error and stop moving */
                    notificationManager.notify(localization.failedToMoveItems(), error.getMessage(), FAIL, FLOAT_MODE, item.getProjectConfig());
                    dialogFactory.createMessageDialog("ERROR", error.getMessage(), null).show();

                    /** Clears item list to disable Paste button */
                    itemsToProcess.clear();
                }
            }
        }, new CancelCallback() {
            @Override
            public void cancelled() {
                /** Stop moving and clears item list to disable Paste button */
                itemsToProcess.clear();
            }
        });
    }

    /**
     * Moves with overwriting.
     * Delete destination resource and move again.
     */
    private void moveWithOverwriting() {
        /** Get item to move */
        final ResourceBasedNode<?> item = itemsToProcess.get(itemIndex);

        try {
            /** Delete destination item */
            String deletePath = ((HasStorablePath)destination).getStorablePath() + "/" + item.getName();
            projectServiceClient.delete(appContext.getDevMachine(), deletePath, new AsyncRequestCallback<Void>() {
                @Override
                protected void onSuccess(Void result) {
                    /** Move the item */
                    projectServiceClient
                            .move(appContext.getDevMachine(), ((HasStorablePath)item).getStorablePath(), ((HasStorablePath)destination).getStorablePath(),
                                  null,
                                  moveCallback);
                }

                @Override
                protected void onFailure(Throwable error) {
                    /** Handle error and stop moving */
                    notificationManager.notify(localization.failedToMoveItems(), error.getMessage(), FAIL, FLOAT_MODE, item.getProjectConfig());
                    dialogFactory.createMessageDialog("ERROR", error.getMessage(), null).show();

                    /** Clears item list to disable Paste button */
                    itemsToProcess.clear();
                }
            });
        } catch (Exception error) {
            /** Handle error and stop copying */
            notificationManager.notify(localization.failedToMoveItems(), error.getMessage(), FAIL, FLOAT_MODE, item.getProjectConfig());
            dialogFactory.createMessageDialog("ERROR", error.getMessage(), null).show();

            /** Clears item list to disable Paste button */
            itemsToProcess.clear();
        }
    }

    /**
     * Callback for move operation.
     */
    private final AsyncRequestCallback<Void> moveCallback = new AsyncRequestCallback<Void>() {
        @Override
        protected void onSuccess(Void result) {
            move();
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
                    .notify(localization.failedToMoveItems(), exception.getMessage(), FAIL, FLOAT_MODE, destination.getProjectConfig());
            dialogFactory.createMessageDialog("ERROR", exception.getMessage(), null).show();

            /** Clears item list to disable Paste button */
            itemsToProcess.clear();
        }
    };
}
