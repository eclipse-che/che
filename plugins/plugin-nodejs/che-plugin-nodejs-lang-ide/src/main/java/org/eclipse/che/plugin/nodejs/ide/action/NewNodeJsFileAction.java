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
package org.eclipse.che.plugin.nodejs.ide.action;

import com.google.inject.Inject;

import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;
import org.eclipse.che.ide.newresource.AbstractNewResourceAction;
import org.eclipse.che.plugin.nodejs.ide.NodeJsLocalizationConstant;
import org.eclipse.che.plugin.nodejs.ide.NodeJsResources;
import org.eclipse.che.plugin.nodejs.shared.Constants;

import javax.validation.constraints.NotNull;

import static org.eclipse.che.plugin.nodejs.shared.Constants.NODE_JS_PROJECT_TYPE_ID;

/**
 * @author Dmitry Shnurenko
 */
public class NewNodeJsFileAction extends AbstractNewResourceAction {

    private static final String DEFAULT_CONTENT = "/* eslint-env node */";

    private final AppContext appContext;

    @Inject
    public NewNodeJsFileAction(NodeJsLocalizationConstant locale, NodeJsResources resources, AppContext appContext) {
        super(locale.newNodeJsFileTitle(), locale.newNodeJsFileDescription(), resources.jsIcon());
        this.appContext = appContext;
    }

    @Override
    public void updateInPerspective(@NotNull ActionEvent event) {
        CurrentProject currentProject = appContext.getCurrentProject();

        if (currentProject == null) {
            return;
        }

        ProjectConfigDto projectConfig = currentProject.getProjectConfig();

        String type = projectConfig.getType();

        event.getPresentation().setEnabledAndVisible(NODE_JS_PROJECT_TYPE_ID.equals(type));
    }

    @Override
    protected String getExtension() {
        return Constants.NODE_JS_FILE_EXT;
    }

    @Override
    protected String getDefaultContent() {
        return DEFAULT_CONTENT;
    }
}
