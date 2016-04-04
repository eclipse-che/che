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
package org.eclipse.che.plugin.svn.ide.export;

import com.google.common.base.Strings;
import com.google.gwt.user.client.Window;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.api.project.node.HasStorablePath;
import org.eclipse.che.ide.extension.machine.client.processes.ConsolesPanelPresenter;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.plugin.svn.ide.SubversionExtensionLocalizationConstants;
import org.eclipse.che.plugin.svn.ide.common.SubversionActionPresenter;
import org.eclipse.che.plugin.svn.ide.common.SubversionOutputConsoleFactory;
import org.eclipse.che.plugin.svn.ide.common.SubversionOutputConsolePresenter;

/**
 * Presenter for the {@link ExportView}.
 *
 * @author Vladyslav Zhukovskyi
 */
@Singleton
public class ExportPresenter extends SubversionActionPresenter implements ExportView.ActionDelegate {

    private       ExportView                               view;
    private       NotificationManager                      notificationManager;
    private       SubversionExtensionLocalizationConstants constants;
    private final String                                   baseHttpUrl;

    private HasStorablePath selectedNode;

    @Inject
    public ExportPresenter(@Named("cheExtensionPath") String extPath,
                           AppContext appContext,
                           SubversionOutputConsoleFactory consoleFactory,
                           ConsolesPanelPresenter consolesPanelPresenter,
                           ProjectExplorerPresenter projectExplorerPart,
                           ExportView view,
                           NotificationManager notificationManager,
                           SubversionExtensionLocalizationConstants constants) {
        super(appContext, consoleFactory, consolesPanelPresenter, projectExplorerPart);
        this.view = view;
        this.notificationManager = notificationManager;
        this.constants = constants;
        this.view.setDelegate(this);

        this.baseHttpUrl = extPath + "/svn/" + appContext.getWorkspaceId();
    }

    public void showExport(HasStorablePath selectedNode) {
        this.selectedNode = selectedNode;

        view.onShow();
    }

    /** {@inheritDoc} */
    @Override
    public void onCancelClicked() {
        view.onClose();
    }

    /** {@inheritDoc} */
    @Override
    public void onExportClicked() {
        final String projectPath = getCurrentProjectPath();

        if (projectPath == null) {
            return;
        }

        final String exportPath = Strings.emptyToNull(relPath(projectPath, selectedNode.getStorablePath()));
        final String revision = view.isRevisionSpecified() ? view.getRevision() : null;

        notificationManager.notify(constants.exportStarted(exportPath));

        view.onClose();

        char prefix = '?';
        StringBuilder url = new StringBuilder(baseHttpUrl + "/export" + projectPath);

        if (!Strings.isNullOrEmpty(exportPath)) {
            url.append(prefix).append("path").append('=').append(exportPath);
            prefix = '&';
        }

        if (!Strings.isNullOrEmpty(revision)) {
            url.append(prefix).append("revision").append('=').append(revision);
        }

        Window.open(url.toString(), "_self", "");
    }

    /** {@inheritDoc} */
    @Override
    public void minimize() {

    }

    /** {@inheritDoc} */
    @Override
    public void activatePart() {

    }

    private String relPath(String base, String path) {
        if (!path.startsWith(base)) {
            return null;
        }

        final String temp = path.substring(base.length());

        return temp.startsWith("/") ? temp.substring(1) : temp;
    }
}
