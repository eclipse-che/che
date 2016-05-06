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
package org.eclipse.che.ide.newresource;

import com.google.inject.Inject;

import org.eclipse.che.api.git.gwt.client.GitServiceClient;
import org.eclipse.che.api.project.gwt.client.ProjectServiceClient;
import org.eclipse.che.api.project.shared.dto.ItemReference;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.project.node.HasStorablePath;
import org.eclipse.che.ide.api.project.node.Node;
import org.eclipse.che.ide.api.selection.Selection;
import org.eclipse.che.ide.json.JsonHelper;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.project.node.FileReferenceNode;
import org.eclipse.che.ide.project.node.ResourceBasedNode;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.api.dialogs.CancelCallback;
import org.eclipse.che.ide.api.dialogs.ConfirmCallback;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.ide.api.dialogs.InputCallback;
import org.eclipse.che.ide.api.dialogs.InputDialog;
import org.eclipse.che.ide.api.dialogs.InputValidator;
import org.eclipse.che.ide.util.NameUtils;
import org.eclipse.che.ide.websocket.WebSocketException;
import org.eclipse.che.ide.websocket.rest.RequestCallback;
import org.vectomatic.dom.svg.ui.SVGResource;

import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

/**
 * Implementation of an {@link Action} that provides an ability to create new resource (e.g. file, folder).
 * After performing this action, it asks user for the resource's name
 * and then creates resource in the selected folder.
 *
 * @author Artem Zatsarynnyi
 * @author Dmitry Shnurenko
 */
public abstract class AbstractNewResourceAction extends AbstractPerspectiveAction {
    protected final InputValidator           fileNameValidator;
    protected final InputValidator           folderNameValidator;
    protected final String                   title;
    protected       ProjectServiceClient     projectServiceClient;
    protected       DtoUnmarshallerFactory   dtoUnmarshallerFactory;
    protected       DialogFactory            dialogFactory;
    protected       CoreLocalizationConstant coreLocalizationConstant;
    protected       ProjectExplorerPresenter projectExplorer;

    @Inject
    private AppContext               appContext;
    @Inject
    private GitServiceClient         gitServiceClient;
    @Inject
    private NotificationManager      notificationManager;
    @Inject
    private CoreLocalizationConstant localizationConstant;

    /**
     * Creates new action.
     *
     * @param title
     *         action's title
     * @param description
     *         action's description
     * @param svgIcon
     *         action's SVG icon
     */
    public AbstractNewResourceAction(String title, String description, @Nullable SVGResource svgIcon) {
        super(Arrays.asList(PROJECT_PERSPECTIVE_ID), title, description, null, svgIcon);
        fileNameValidator = new FileNameValidator();
        folderNameValidator = new FolderNameValidator();
        this.title = title;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        InputDialog inputDialog = dialogFactory.createInputDialog(
                coreLocalizationConstant.newResourceTitle(title),
                coreLocalizationConstant.newResourceLabel(title.toLowerCase()),
                new InputCallback() {
                    @Override
                    public void accepted(String value) {
                        onAccepted(value);
                    }
                }, null).withValidator(fileNameValidator);
        inputDialog.show();
    }

    private void onAccepted(String value) {
        final String name = getExtension().isEmpty() ? value : value + '.' + getExtension();
        final ResourceBasedNode<?> parent = getResourceBasedNode();

        if (parent == null) {
            throw new IllegalStateException("Invalid parent node.");
        }

        projectServiceClient.createFile(appContext.getDevMachine(),
                                        ((HasStorablePath)parent).getStorablePath(),
                                        name,
                                        getDefaultContent(),
                                        createCallback(parent));
    }

    protected AsyncRequestCallback<ItemReference> createCallback(final ResourceBasedNode<?> parent) {
        return new AsyncRequestCallback<ItemReference>(dtoUnmarshallerFactory.newUnmarshaller(ItemReference.class)) {
            @Override
            protected void onSuccess(final ItemReference itemReference) {
                HasStorablePath path = new HasStorablePath.StorablePath(itemReference.getPath());

                projectExplorer.getNodeByPath(path, true)
                               .then(selectNode())
                               .then(openNode());

                if ("file".equals(itemReference.getType())) {
                    askAddToIndex(path.getStorablePath(), itemReference.getName());
                }
            }

            @Override
            protected void onFailure(Throwable exception) {
                dialogFactory.createMessageDialog("", JsonHelper.parseJsonMessage(exception.getMessage()), null).show();
            }
        };
    }

    private void askAddToIndex(final String path, final String fileName) {
        final ProjectConfigDto project = appContext.getCurrentProject().getRootProject();
        Map<String, List<String>> attributes = project.getAttributes();
        if (!(attributes.containsKey("vcs.provider.name") && attributes.get("vcs.provider.name").contains("git"))) {
            return;
        }

        ConfirmCallback confirmCallback = new ConfirmCallback() {
            @Override
            public void accepted() {
                String filePath = path.substring(project.getName().length() + 2);
                try {
                    gitServiceClient
                            .add(appContext.getDevMachine(), project, false, Collections.singletonList(filePath),
                                 new RequestCallback<Void>() {
                                     @Override
                                     protected void onSuccess(Void result) {
                                         notificationManager.notify(localizationConstant.actionGitIndexUpdated(),
                                                                    localizationConstant.actionNewFileAddToIndexNotification(fileName));
                                     }

                                     @Override
                                     protected void onFailure(Throwable exception) {
                                         notificationManager.notify(localizationConstant.actionGitIndexUpdateFailed(),
                                                                    exception.getMessage());
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

        dialogFactory.createConfirmDialog(localizationConstant.actionNewFileAddToIndexTitle(),
                                          localizationConstant.actionNewFileAddToIndexText(fileName),
                                          "Yes",
                                          "No",
                                          confirmCallback,
                                          cancelCallback).show();
    }

    protected Function<Node, Node> selectNode() {
        return new Function<Node, Node>() {
            @Override
            public Node apply(Node node) throws FunctionException {
                projectExplorer.select(node, false);

                return node;
            }
        };
    }

    protected Function<Node, Node> openNode() {
        return new Function<Node, Node>() {
            @Override
            public Node apply(Node node) throws FunctionException {
                if (node instanceof FileReferenceNode) {
                    ((FileReferenceNode)node).actionPerformed();
                }

                return node;
            }
        };
    }

    @Override
    public void updateInPerspective(@NotNull ActionEvent e) {
        e.getPresentation().setEnabled(getResourceBasedNode() != null);
    }

    /**
     * Returns extension (without dot) for a new resource.
     * By default, returns an empty string.
     */
    protected String getExtension() {
        return "";
    }

    /**
     * Returns default content for a new resource.
     * By default, returns an empty string.
     */
    protected String getDefaultContent() {
        return "";
    }

    /** Returns parent for creating new item or {@code null} if resource can not be created. */
    @Nullable
    protected ResourceBasedNode<?> getResourceBasedNode() {
        Selection<?> selection = projectExplorer.getSelection();

        //we should be sure that user selected single element to work with it
        if (selection == null || selection.isEmpty()) {
            return null;
        }

        Object o = selection.getHeadElement();

        if (o instanceof ResourceBasedNode<?>) {
            ResourceBasedNode<?> node = (ResourceBasedNode<?>)o;
            //it may be file node, so we should take parent node
            if (node.isLeaf() && isResourceAndStorableNode(node.getParent())) {
                return (ResourceBasedNode<?>)node.getParent();
            }

            return isResourceAndStorableNode(node) ? node : null;
        }

        return null;
    }

    protected boolean isResourceAndStorableNode(@Nullable Node node) {
        return node != null && node instanceof ResourceBasedNode<?> && node instanceof HasStorablePath;
    }

    @Inject
    private void init(ProjectServiceClient projectServiceClient,
                      DtoUnmarshallerFactory dtoUnmarshallerFactory,
                      DialogFactory dialogFactory,
                      CoreLocalizationConstant coreLocalizationConstant,
                      ProjectExplorerPresenter projectExplorer) {
        this.projectServiceClient = projectServiceClient;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.dialogFactory = dialogFactory;
        this.coreLocalizationConstant = coreLocalizationConstant;
        this.projectExplorer = projectExplorer;
    }

    private class FileNameValidator implements InputValidator {
        @Nullable
        @Override
        public Violation validate(String value) {
            if (!NameUtils.checkFileName(value)) {
                return new Violation() {
                    @Override
                    public String getMessage() {
                        return coreLocalizationConstant.invalidName();
                    }

                    @Nullable
                    @Override
                    public String getCorrectedValue() {
                        return null;
                    }
                };
            }
            return null;
        }
    }

    private class FolderNameValidator implements InputValidator {
        @Nullable
        @Override
        public Violation validate(String value) {
            if (!NameUtils.checkFolderName(value)) {
                return new Violation() {
                    @Override
                    public String getMessage() {
                        return coreLocalizationConstant.invalidName();
                    }

                    @Nullable
                    @Override
                    public String getCorrectedValue() {
                        return null;
                    }
                };
            }
            return null;
        }
    }
}
