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
package org.eclipse.che.plugin.svn.ide.copy;

import com.google.common.base.Strings;
import com.google.gwt.regexp.shared.RegExp;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.api.project.node.HasStorablePath;
import org.eclipse.che.ide.extension.machine.client.processes.ConsolesPanelPresenter;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.project.node.FileReferenceNode;
import org.eclipse.che.ide.project.node.FolderReferenceNode;
import org.eclipse.che.ide.project.node.ProjectNode;
import org.eclipse.che.ide.project.node.ResourceBasedNode;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.util.RegExpUtils;
import org.eclipse.che.plugin.svn.ide.SubversionClientService;
import org.eclipse.che.plugin.svn.ide.SubversionExtensionLocalizationConstants;
import org.eclipse.che.plugin.svn.ide.common.StatusColors;
import org.eclipse.che.plugin.svn.ide.common.SubversionActionPresenter;
import org.eclipse.che.plugin.svn.ide.common.SubversionOutputConsoleFactory;
import org.eclipse.che.plugin.svn.shared.CLIOutputResponse;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.PROGRESS;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.SUCCESS;

/**
 * Presenter for the {@link CopyView}.
 *
 * @author Vladyslav Zhukovskyi
 */
@Singleton
public class CopyPresenter extends SubversionActionPresenter implements CopyView.ActionDelegate {

    private       CopyView                                 view;
    private       NotificationManager                      notificationManager;
    private       SubversionClientService                  service;
    private       DtoUnmarshallerFactory                   dtoUnmarshallerFactory;
    private       SubversionExtensionLocalizationConstants constants;
    private       ResourceBasedNode<?>                     sourceNode;
    private TargetHolder targetHolder = new TargetHolder();

    private RegExp urlRegExp = RegExp.compile("^(https?|ftp)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");

    private class TargetHolder {
        // /project/path/to/destination/directory
        String dir;
        // /project/path/to/destination/directory/[item_name]
        String name;

        /** Prepare target directory and item name to normal path. */
        String normalize() {
            dir = dir.endsWith("/") ? dir : dir + '/';

            if (!Strings.isNullOrEmpty(name)) {
                name = name.startsWith("/") ? name.substring(1) : name;
            } else if (!Strings.isNullOrEmpty(view.getNewName())) {
                name = view.getNewName();
            } else if (sourceNode != null) {
                name = sourceNode.getName();
            }

            return dir + name;
        }
    }

    @Inject
    protected CopyPresenter(AppContext appContext,
                            SubversionOutputConsoleFactory consoleFactory,
                            ConsolesPanelPresenter consolesPanelPresenter,
                            CopyView view,
                            NotificationManager notificationManager,
                            SubversionClientService service,
                            DtoUnmarshallerFactory dtoUnmarshallerFactory,
                            SubversionExtensionLocalizationConstants constants,
                            final ProjectExplorerPresenter projectExplorerPart,
                            final StatusColors statusColors) {
        super(appContext, consoleFactory, consolesPanelPresenter, projectExplorerPart, statusColors);
        this.view = view;
        this.notificationManager = notificationManager;
        this.service = service;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.constants = constants;
        this.view.setDelegate(this);
    }

    /** Show copy dialog. */
    public void showCopy(ResourceBasedNode<?> sourceNode) {
        if (sourceNode == null) {
            return;
        }

        this.sourceNode = sourceNode;

        if (sourceNode instanceof FileReferenceNode) {
            view.setDialogTitle(constants.copyViewTitleFile());
        } else if (sourceNode instanceof FolderReferenceNode || sourceNode instanceof ProjectNode) {
            view.setDialogTitle(constants.copyViewTitleDirectory());
        }

        targetHolder.name = sourceNode.getName();

        view.setNewName(targetHolder.name);
        view.setComment(targetHolder.name);
        view.setSourcePath(getStorableNodePath(sourceNode), false);

        validate();

        view.show();
    }

    /** {@inheritDoc} */
    @Override
    public void onCopyClicked() {
        final String projectPath = getCurrentProjectPath();

        if (projectPath == null) {
            return;
        }

        final String src = view.isSourceCheckBoxSelected() ? view.getSourcePath() : relPath(projectPath, getStorableNodePath(sourceNode));
        final String target = view.isTargetCheckBoxSelected() ? view.getTargetUrl() : relPath(projectPath, targetHolder.normalize());
        final String comment = view.isTargetCheckBoxSelected() ? view.getComment() : null;

        final StatusNotification notification = new StatusNotification(constants.copyNotificationStarted(src), PROGRESS, FLOAT_MODE);
        notificationManager.notify(notification);

        view.hide();

        service.copy(projectPath, src, target, comment,
                     new AsyncRequestCallback<CLIOutputResponse>(dtoUnmarshallerFactory.newUnmarshaller(CLIOutputResponse.class)) {
                         @Override
                         protected void onSuccess(CLIOutputResponse result) {
                             printResponse(result.getCommand(), result.getOutput(), result.getErrOutput(), constants.commandCopy());

                             notification.setTitle(constants.copyNotificationSuccessful());
                             notification.setStatus(SUCCESS);
                         }

                         @Override
                         protected void onFailure(Throwable exception) {
                             String errorMessage = exception.getMessage();

                             notification.setTitle(constants.copyNotificationFailed() + ": " + errorMessage);
                             notification.setStatus(FAIL);
                         }
                     });
    }

    /** {@inheritDoc} */
    @Override
    public void onNewNameChanged(String newName) {
        targetHolder.name = newName;
        validate();
    }

    /** {@inheritDoc} */
    @Override
    public void onCancelClicked() {
        view.hide();
    }

    /** {@inheritDoc} */
    @Override
    public void onNodeSelected(ResourceBasedNode<?> destinationNode) {
        targetHolder.dir = getStorableNodePath(destinationNode);
        validate();
    }

    /** {@inheritDoc} */
    @Override
    public void onSourcePathChanged() {
        validate();
    }

    /** {@inheritDoc} */
    @Override
    public void onTargetUrlChanged() {
        validate();
    }

    /** {@inheritDoc} */
    @Override
    public void onSourceCheckBoxChanged() {
        // url path chosen
        if (view.isSourceCheckBoxSelected()) {
            view.setSourcePath("", true);
            view.setNewName("name");
            targetHolder.name = null;
        } else {
            view.setSourcePath(getStorableNodePath(sourceNode), false);
            view.setNewName(sourceNode.getName());
            targetHolder.name = sourceNode.getName();
        }

        validate();
    }

    /** {@inheritDoc} */
    @Override
    public void onTargetCheckBoxChanged() {
        validate();
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
    public void onNodeExpanded(final ResourceBasedNode<?> node) {

    }

    private String relPath(String base, String path) {
        if (!path.startsWith(base)) {
            return null;
        }

        final String temp = path.substring(base.length());

        return temp.startsWith("/") ? temp.substring(1) : temp;
    }

    private void validate() {
        ValidationStrategy strategy;

        if (view.isSourceCheckBoxSelected()) {
            if (view.isTargetCheckBoxSelected()) {
                strategy = new UrlUrlValidation();
            } else {
                strategy = new UrlFileValidation();
            }
        } else {
            if (view.isTargetCheckBoxSelected()) {
                strategy = new FileUrlValidation();
            } else {
                strategy = new FileFileValidation();
            }
        }

        if (strategy.isValid()) {
            view.hideErrorMarker();
        }
    }

    @Nullable
    private String getStorableNodePath(ResourceBasedNode<?> node) {
        return node instanceof HasStorablePath ? ((HasStorablePath)node).getStorablePath() : null;
    }

    private interface ValidationStrategy {
        boolean isValid();
    }

    private class FileFileValidation implements ValidationStrategy {
        @Override
        public boolean isValid() {
            if (Strings.isNullOrEmpty(targetHolder.dir)) {
                view.showErrorMarker(constants.copyEmptyTarget());
                return false;
            }

            if (targetHolder.normalize().equals(getStorableNodePath(sourceNode))) {
                view.showErrorMarker(constants.copyItemEqual());
                return false;
            }

            if (targetHolder.dir.startsWith(Strings.nullToEmpty(getStorableNodePath(sourceNode)))) {
                view.showErrorMarker(constants.copyItemChildDetect());
                return false;
            }

            return true;
        }
    }

    private class UrlFileValidation implements ValidationStrategy {
        @Override
        public boolean isValid() {
            if (Strings.isNullOrEmpty(targetHolder.dir)) {
                view.showErrorMarker(constants.copyEmptyTarget());
                return false;
            }

            if (!RegExpUtils.resetAndTest(urlRegExp, view.getSourcePath())) {
                view.showErrorMarker(constants.copySourceWrongURL());
                return false;
            }

            return true;
        }
    }

    private class FileUrlValidation implements ValidationStrategy {
        @Override
        public boolean isValid() {
            if (!RegExpUtils.resetAndTest(urlRegExp, view.getTargetUrl())) {
                view.showErrorMarker(constants.copyTargetWrongURL());
                return false;
            }

            return true;
        }
    }

    private class UrlUrlValidation implements ValidationStrategy {
        @Override
        public boolean isValid() {
            if (!RegExpUtils.resetAndTest(urlRegExp, view.getSourcePath())) {
                view.showErrorMarker(constants.copySourceWrongURL());
                return false;
            }

            if (!RegExpUtils.resetAndTest(urlRegExp, view.getTargetUrl())) {
                view.showErrorMarker(constants.copyTargetWrongURL());
                return false;
            }

            return true;
        }
    }
}
