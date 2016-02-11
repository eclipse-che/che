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

import org.eclipse.che.api.analytics.client.logger.AnalyticsEventLogger;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;

import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import java.util.Arrays;

import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

/**
 * Exports workspace config.
 *
 * @author Sergii Leschenko
 * @author Max Shaposhnik
 */
@Singleton
public class ExportConfigAction extends AbstractPerspectiveAction {
    private final AppContext                  appContext;
    private final AnalyticsEventLogger        eventLogger;
    private final String                      exportConfigURL;

    @Inject
    public ExportConfigAction(CoreLocalizationConstant locale,
                              AppContext appContext,
                              AnalyticsEventLogger eventLogger) {
        super(Arrays.asList(PROJECT_PERSPECTIVE_ID), locale.exportConfigText(), null, null, null);
        this.appContext = appContext;
        this.eventLogger = eventLogger;
        this.exportConfigURL = "/api/workspace/" + appContext.getWorkspaceId();
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent e) {
        eventLogger.log(this);
        downloadFile(exportConfigURL, appContext.getWorkspace().getName());
    }

    /** {@inheritDoc} */
    @Override
    public void updateInPerspective(@NotNull ActionEvent event) {
        CurrentProject activeProject = appContext.getCurrentProject();
        event.getPresentation().setEnabled(activeProject != null);
    }

    /*
    * Trick to force browser download the file instead of opening json on new tab
    */
    private native void downloadFile(String url, String wsName)/*-{
        var xhrReq = new XMLHttpRequest();
        xhrReq.responseType = 'blob';
        xhrReq.onload = function() {
            var link = document.createElement('a');
            link.href = window.URL.createObjectURL(xhrReq.response);
            link.download = wsName;
            link.style.display = 'none';
            document.body.appendChild(link);
            link.click();
            delete link;
        };
        xhrReq.open('GET', url);
        xhrReq.send();
    }-*/;
}
