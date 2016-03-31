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
package org.eclipse.che.plugin.python.ide.action;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;
import org.eclipse.che.ide.newresource.AbstractNewResourceAction;
import org.eclipse.che.plugin.python.ide.PythonLocalizationConstant;
import org.eclipse.che.plugin.python.ide.PythonResources;

import javax.validation.constraints.NotNull;

import static org.eclipse.che.plugin.python.shared.ProjectAttributes.PYTHON_EXT;
import static org.eclipse.che.plugin.python.shared.ProjectAttributes.PYTHON_ID;

/**
 * Action to create new Python source file.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class CreatePythonFileAction extends AbstractNewResourceAction {
    private final AppContext appContext;

    @Inject
    public CreatePythonFileAction(PythonLocalizationConstant localizationConstant,
                                  PythonResources pythonResources,
                                  AppContext appContext) {
        super(localizationConstant.createPythonFileActionTitle(),
              localizationConstant.createPythonFileActionDescription(),
              pythonResources.pythonFile());
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
        event.getPresentation().setEnabledAndVisible(PYTHON_ID.equals(type));
    }

    @Override
    protected String getExtension() {
        return PYTHON_EXT;
    }

    @Override
    protected String getDefaultContent() {
        return "";
    }

}
