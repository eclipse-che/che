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

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.Callback;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.git.GitServiceClient;
import org.eclipse.che.api.git.shared.Status;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.callback.CallbackPromiseHelper;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.PromisableAction;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.project.node.Node;
import org.eclipse.che.ide.api.selection.Selection;
import org.eclipse.che.ide.api.selection.SelectionAgent;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.project.node.FileReferenceNode;
import org.eclipse.che.ide.project.node.FolderReferenceNode;
import org.eclipse.che.ide.project.node.ProjectNode;
import org.eclipse.che.ide.project.node.ResourceBasedNode;
import org.eclipse.che.ide.project.node.remove.DeleteNodeHandler;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.Unmarshallable;
import org.eclipse.che.ide.api.dialogs.CancelCallback;
import org.eclipse.che.ide.api.dialogs.ConfirmCallback;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.ide.websocket.WebSocketException;
import org.eclipse.che.ide.websocket.rest.RequestCallback;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.eclipse.che.api.promises.client.callback.CallbackPromiseHelper.createFromCallback;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.NOT_EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

/**
 * Action for deleting an item which is selected in 'Project Explorer'.
 *
 * @author Artem Zatsarynnyi
 * @author Dmitry Shnurenko
 * @author Vlad Zhukovskyi
 */
@Singleton
public class DeleteItemAction extends AbstractPerspectiveAction implements PromisableAction {
    private final SelectionAgent           selectionAgent;
    private final DeleteNodeHandler        deleteNodeHandler;
    private final AppContext               appContext;
    private final ProjectExplorerPresenter projectExplorer;
    private final DtoUnmarshallerFactory   dtoUnmarshallerFactory;
    private final DialogFactory            dialogFactory;
    private final GitServiceClient         gitService;
    private final CoreLocalizationConstant locale;
    private final NotificationManager      notificationManager;
    private final String                   workspaceId;

    private Callback<Void, Throwable> actionCompletedCallBack;

    @Inject
    public DeleteItemAction(Resources resources,
                            SelectionAgent selectionAgent,
                            DeleteNodeHandler deleteNodeHandler,
                            CoreLocalizationConstant localization,
                            AppContext appContext,
                            ProjectExplorerPresenter projectExplorer,
                            DtoUnmarshallerFactory dtoUnmarshallerFactory,
                            DialogFactory dialogFactory,
                            GitServiceClient gitServiceClient,
                            CoreLocalizationConstant coreLocalizationConstant,
                            NotificationManager notificationManager) {
        super(Collections.singletonList(PROJECT_PERSPECTIVE_ID),
              localization.deleteItemActionText(),
              localization.deleteItemActionDescription(),
              null,
              resources.delete());
        this.selectionAgent = selectionAgent;
        this.deleteNodeHandler = deleteNodeHandler;
        this.appContext = appContext;
        this.projectExplorer = projectExplorer;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.dialogFactory = dialogFactory;
        this.gitService = gitServiceClient;
        this.locale = coreLocalizationConstant;
        this.notificationManager = notificationManager;
        
        this.workspaceId = appContext.getWorkspaceId();
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent e) {
        final Selection<?> selection = selectionAgent.getSelection();

        if (selection == null || selection.isEmpty()) {
            throw new IllegalStateException("Nodes weren't found in the selection agent");
        }

        if (Iterables.all(selection.getAllElements(), isResourceBasedNode())) {
            final List<ResourceBasedNode<?>> nodes = Lists.newArrayList(Iterables.transform(selection.getAllElements(), castNode()));

            Node selectedNode = nodes.get(0);

            Node parentNode = selectedNode.getParent();

            deleteNodeHandler.deleteAll(nodes, true).then(synchronizeProjectView(parentNode))
                             .then(actionComplete(nodes));

        }
    }

    private Operation<Void> synchronizeProjectView(final Node parent) {
        return new Operation<Void>() {
            @Override
            public void apply(Void arg) throws OperationException {
                projectExplorer.reloadChildren(parent);
            }
        };
    }

    private Operation<Void> actionComplete(final List<ResourceBasedNode<?>> nodes) {
        final ProjectConfigDto project = appContext.getCurrentProject().getRootProject();
        return new Operation<Void>() {
            @Override
            public void apply(Void arg) throws OperationException {
                if (actionCompletedCallBack != null) {
                    actionCompletedCallBack.onSuccess(null);
                }

                Map<String, List<String>> atributes = project.getAttributes();
                if (!(atributes.containsKey("vcs.provider.name") && atributes.get("vcs.provider.name").contains("git"))
                    || nodes.get(0) instanceof ProjectNode) {
                    return;
                }

                final Unmarshallable<Status> unmarshall = dtoUnmarshallerFactory.newUnmarshaller(Status.class);
                gitService.status(appContext.getDevMachine(), project, new AsyncRequestCallback<Status>(unmarshall) {
                    @Override
                    protected void onSuccess(final Status result) {
                        if (!result.getMissing().isEmpty()) {
                            askAddToIndex(project, nodes, result.getMissing());
                        }
                    }

                    @Override
                    protected void onFailure(Throwable exception) {
                        notificationManager.notify(exception.getMessage(), FAIL, NOT_EMERGE_MODE);
                    }
                });
            }
        };
    }

    private void askAddToIndex(final ProjectConfigDto project, List<ResourceBasedNode<?>> nodes, List<String> missing) {
        final List<String> itemsToAddToIndex = new ArrayList<>();
        for (ResourceBasedNode<?> node : nodes) {
            for (String missingItem : missing) {
                String itemPath = getNodePath(node, project.getName());
                if (!itemPath.isEmpty() && missingItem.contains(itemPath)) {
                    itemsToAddToIndex.add(itemPath);
                }
            }
        }

        if (itemsToAddToIndex.isEmpty()) {
            return;
        }

        ConfirmCallback confirmCallback = new ConfirmCallback() {
            @Override
            public void accepted() {
                try {
                    gitService.add(appContext.getDevMachine(), project, false, itemsToAddToIndex, new RequestCallback<Void>() {
                        @Override
                        protected void onSuccess(Void result) {
                            notificationManager.notify(locale.deleteAddToIndexIndexUpdated(), locale.deleteAddToIndexDialogNotification());
                        }

                        @Override
                        protected void onFailure(Throwable exception) {
                            notificationManager.notify(locale.deleteAddToIndexIndexFailedToUpdate(), exception.getMessage(), FAIL, NOT_EMERGE_MODE);
                        }
                    });
                } catch (WebSocketException e) {
                    e.printStackTrace();
                }
            }
        };

        CancelCallback cancelCallback = new CancelCallback() {
            @Override
            public void cancelled() {
                //Do nothing
            }
        };

        dialogFactory.createConfirmDialog(locale.deleteAddToIndexDialogTitle(),
                                          locale.deleteAddToIndexDialogText(),
                                          "Yes",
                                          "No",
                                          confirmCallback,
                                          cancelCallback).show();
    }

    private String getNodePath(ResourceBasedNode<?> node, String projectName) {
        String path = "";
        if (node instanceof ProjectNode) {
            path = ((ProjectNode)node).getStorablePath();
        } else if (node instanceof FolderReferenceNode) {
            path = ((FolderReferenceNode)node).getStorablePath();
        } else if (node instanceof FileReferenceNode) {
            path = ((FileReferenceNode)node).getStorablePath();
        }
        if (!path.isEmpty()) {
            return path.substring(projectName.length() + 2);
        } else {
            return path;
        }
    }

    private Predicate<Object> isResourceBasedNode() {
        return new Predicate<Object>() {
            @Override
            public boolean apply(@Nullable Object node) {
                return node instanceof ResourceBasedNode;
            }
        };
    }

    private com.google.common.base.Function<Object, ResourceBasedNode<?>> castNode() {
        return new com.google.common.base.Function<Object, ResourceBasedNode<?>>() {
            @Nullable
            @Override
            public ResourceBasedNode<?> apply(Object o) {
                if (o instanceof ResourceBasedNode<?>) {
                    return (ResourceBasedNode<?>)o;
                }

                throw new IllegalArgumentException("Node isn't resource based");
            }
        };
    }

    /** {@inheritDoc} */
    @Override
    public void updateInPerspective(@NotNull ActionEvent event) {
        event.getPresentation().setVisible(true);

        final Selection<?> selection = selectionAgent.getSelection();

        if (selection == null || selection.isEmpty()) {
            event.getPresentation().setEnabled(false);
            return;
        }

        boolean enable = Iterables.all(selection.getAllElements(), isResourceBasedNode());

        event.getPresentation().setEnabled(enable && appContext.getCurrentUser().isUserPermanent());
    }

    @Override
    public Promise<Void> promise(final ActionEvent event) {
        final CallbackPromiseHelper.Call<Void, Throwable> call = new CallbackPromiseHelper.Call<Void, Throwable>() {
            @Override
            public void makeCall(Callback<Void, Throwable> callback) {
                actionCompletedCallBack = callback;
                actionPerformed(event);
            }
        };

        return createFromCallback(call);
    }
}
