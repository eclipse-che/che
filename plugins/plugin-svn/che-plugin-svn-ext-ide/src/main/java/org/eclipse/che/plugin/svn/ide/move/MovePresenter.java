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
package org.eclipse.che.plugin.svn.ide.move;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.api.project.tree.TreeNode;
import org.eclipse.che.ide.api.project.tree.generic.Openable;
import org.eclipse.che.ide.api.project.tree.generic.StorableNode;
import org.eclipse.che.ide.api.selection.Selection;
import org.eclipse.che.ide.extension.machine.client.processes.ConsolesPanelPresenter;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.Unmarshallable;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.plugin.svn.ide.SubversionClientService;
import org.eclipse.che.plugin.svn.ide.SubversionExtensionLocalizationConstants;
import org.eclipse.che.plugin.svn.ide.common.StatusColors;
import org.eclipse.che.plugin.svn.ide.common.SubversionActionPresenter;
import org.eclipse.che.plugin.svn.ide.common.SubversionOutputConsoleFactory;
import org.eclipse.che.plugin.svn.ide.common.filteredtree.FilteredTreeStructureProvider;
import org.eclipse.che.plugin.svn.shared.CLIOutputResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.PROGRESS;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.SUCCESS;

/**
 * Presenter for the {@link MoveView}.
 *
 * @author Vladyslav Zhukovskyi
 */
@Singleton
public class MovePresenter extends SubversionActionPresenter implements MoveView.ActionDelegate {

    private ProjectExplorerPresenter                 projectExplorerPart;
    private MoveView                                 view;
    private SubversionExtensionLocalizationConstants locale;
    private FilteredTreeStructureProvider            treeStructureProvider;
    private NotificationManager                      notificationManager;
    private SubversionClientService                  service;
    private DtoUnmarshallerFactory                   dtoUnmarshallerFactory;
    private List<?>                                  sources;
    private String                                   projectPath;

    @Inject
    public MovePresenter(AppContext appContext,
                         SubversionOutputConsoleFactory consoleFactory,
                         ConsolesPanelPresenter consolesPanelPresenter,
                         ProjectExplorerPresenter projectExplorerPart,
                         MoveView view,
                         FilteredTreeStructureProvider treeStructureProvider,
                         NotificationManager notificationManager,
                         SubversionClientService service,
                         DtoUnmarshallerFactory dtoUnmarshallerFactory,
                         SubversionExtensionLocalizationConstants locale,
                         final StatusColors statusColors) {
        super(appContext, consoleFactory, consolesPanelPresenter, projectExplorerPart, statusColors);
        this.projectExplorerPart = projectExplorerPart;
        this.treeStructureProvider = treeStructureProvider;
        this.notificationManager = notificationManager;
        this.service = service;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;

        this.view = view;
        this.locale = locale;
        this.view.setDelegate(this);
    }

    public void showMove() {
        Selection<?> selection = projectExplorerPart.getSelection();

        sources = selection.getAllElements();
        projectPath = getCurrentProjectPath();

        treeStructureProvider.get().getRootNodes(new AsyncCallback<List<TreeNode<?>>>() {
            @Override
            public void onFailure(Throwable caught) {
                notificationManager.notify(locale.moveFailToGetProject(), FAIL, FLOAT_MODE);
            }

            @Override
            public void onSuccess(List<TreeNode<?>> result) {
                view.setProjectNodes(result);
            }
        });

        view.onShow(sources.size() == 1);
    }

    /** {@inheritDoc} */
    @Override
    public void onMoveClicked() {
        if (projectPath == null) {
            return;
        }

        final List<String> source = getSource();
        final String target = getTarget();
        final String comment = view.isURLSelected() ? view.getComment() : null;

        final StatusNotification notification =
                new StatusNotification(locale.moveNotificationStarted(Joiner.on(',').join(source)), PROGRESS, FLOAT_MODE);
        notificationManager.notify(notification);

        Unmarshallable<CLIOutputResponse> unmarshaller = dtoUnmarshallerFactory.newUnmarshaller(CLIOutputResponse.class);
        service.move(projectPath, source, target, comment, new AsyncRequestCallback<CLIOutputResponse>(unmarshaller) {
            @Override
            protected void onSuccess(CLIOutputResponse result) {
                notification.setTitle(locale.moveNotificationSuccessful());
                notification.setStatus(SUCCESS);
            }

            @Override
            protected void onFailure(Throwable exception) {
                String errorMessage = exception.getMessage();

                notification.setTitle(locale.moveNotificationFailed() + ": " + errorMessage);
                notification.setStatus(FAIL);
            }
        });

        view.onClose();
    }

    private List<String> getSource() {
        if (view.isURLSelected()) {
            return Collections.singletonList(view.getSourceUrl());
        } else {
            List<String> srcList = new ArrayList<>(sources.size());
            for (Object source : sources) {
                if (source instanceof StorableNode) {
                    srcList.add(relPath(projectPath, ((StorableNode)source).getPath()));
                }
            }

            return srcList;
        }
    }

    private String getTarget() {
        if (view.isURLSelected()) {
            return view.getTargetUrl();
        } else {
            TreeNode<?> destinationNode = view.getDestinationNode();
            if (destinationNode instanceof StorableNode) {
                final String relPath = relPath(projectPath, ((StorableNode)destinationNode).getPath());
                if (sources.size() > 1) {
                    return relPath;
                }

                final String newName = view.getNewName();

                if (Strings.isNullOrEmpty(newName)) {
                    return relPath;
                }

                return Strings.isNullOrEmpty(relPath) ? newName : relPath + '/' + newName;
            }
            return null;
        }
    }

    private String relPath(String base, String path) {
        if (!path.startsWith(base)) {
            return null;
        }

        final String temp = path.substring(base.length());

        return temp.startsWith("/") ? temp.substring(1) : temp;
    }

    /** {@inheritDoc} */
    @Override
    public void onCancelClicked() {
        view.onClose();
    }

    /** {@inheritDoc} */
    @Override
    public void minimize() {
        //stub
    }

    /** {@inheritDoc} */
    @Override
    public void activatePart() {
        //stub
    }

    /** {@inheritDoc} */
    @Override
    public void onNodeSelected(TreeNode<?> destinationNode) {
        final String target = getTarget();

        for (String source : getSource()) {
            if (target.startsWith(source)) {
                view.showErrorMarker(locale.moveItemChildDetected());
                return;
            }
        }

        view.hideErrorMarker();
    }

    /** {@inheritDoc} */
    @Override
    public void onUrlsChanged() {
        if (Strings.isNullOrEmpty(view.getSourceUrl())) {
            view.showErrorMarker(locale.moveSourceUrlEmpty());
            return;
        }

        if (Strings.isNullOrEmpty(view.getTargetUrl())) {
            view.showErrorMarker(locale.moveTargetUrlEmpty());
            return;
        }

        if (!getHostName(view.getSourceUrl()).equals(getHostName(view.getTargetUrl()))) {
            view.showErrorMarker(locale.moveSourceAndTargetNotEquals());
            return;
        }

        if (Strings.isNullOrEmpty(view.getComment())) {
            view.showErrorMarker(locale.moveCommentEmpty());
            return;
        }

        view.hideErrorMarker();
    }

    private static native String getHostName(String url) /*-{
        var parser = document.createElement('a')
        parser.href = url;
        return parser.hostname
    }-*/;

    /** {@inheritDoc} */
    @Override
    public void onNodeExpanded(final TreeNode<?> node) {
        if (node.getChildren().isEmpty()) {
            // If children is empty then node may be not refreshed yet?
            node.refreshChildren(new AsyncCallback<TreeNode<?>>() {
                @Override
                public void onSuccess(TreeNode<?> result) {
                    if (node instanceof Openable) {
                        ((Openable)node).open();
                    }
                    if (!result.getChildren().isEmpty()) {
                        view.updateProjectNode(result, result);
                    }
                }

                @Override
                public void onFailure(Throwable caught) {
                    Log.error(MovePresenter.class, caught);
                }
            });
        }
    }
}
