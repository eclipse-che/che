/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
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

import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.project.MutableProjectConfig;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.projecttype.wizard.presenter.ProjectWizardPresenter;
import org.eclipse.che.ide.resource.Path;

import javax.validation.constraints.NotNull;

import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

/**
 * The special action which allows call business logic which can convert folder to project.
 *
 * @author Valeriy Svydenko
 */
public class ConvertFolderToProjectAction extends AbstractPerspectiveAction {
    private final AppContext             appContext;
    private final ProjectWizardPresenter projectConfigWizard;

    @Inject
    public ConvertFolderToProjectAction(CoreLocalizationConstant locale,
                                        AppContext appContext,
                                        ProjectWizardPresenter projectConfigWizard) {
        super(singletonList(PROJECT_PERSPECTIVE_ID),
              locale.actionConvertFolderToProject(),
              locale.actionConvertFolderToProjectDescription(),
              null,
              null);
        this.appContext = appContext;
        this.projectConfigWizard = projectConfigWizard;
    }

    @Override
    public void updateInPerspective(@NotNull ActionEvent event) {
        event.getPresentation().setEnabledAndVisible(getSelectedItem() != null);
    }

    private Resource getSelectedItem() {
        Resource resource = appContext.getResource();
        if (resource != null && resource.isFolder()) {
            return resource;
        }

        return null;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        Resource folder = getSelectedItem();
        if (folder == null) {
            return;
        }

        Path location = folder.getLocation();
        if (location == null) {
            return;
        }

        MutableProjectConfig mutableProjectConfig = new MutableProjectConfig();
        mutableProjectConfig.setPath(location.toString());
        mutableProjectConfig.setName(folder.getName());

        projectConfigWizard.show(mutableProjectConfig);
    }
}
