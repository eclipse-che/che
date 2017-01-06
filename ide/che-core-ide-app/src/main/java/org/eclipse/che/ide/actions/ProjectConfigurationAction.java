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
import com.google.inject.Singleton;

import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.project.MutableProjectConfig;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.projecttype.wizard.presenter.ProjectWizardPresenter;

import javax.validation.constraints.NotNull;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.api.resources.Resource.PROJECT;
import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

/**
 * Call Project wizard to change project type
 *
 * @author Evgen Vidolob
 * @author Dmitry Shnurenko
 * @author Vlad Zhukovskyi
 */
@Singleton
public class ProjectConfigurationAction extends AbstractPerspectiveAction {

    private final AppContext             appContext;
    private final ProjectWizardPresenter projectWizard;

    @Inject
    public ProjectConfigurationAction(AppContext appContext,
                                      CoreLocalizationConstant localization,
                                      Resources resources,
                                      ProjectWizardPresenter projectWizard) {
        super(singletonList(PROJECT_PERSPECTIVE_ID),
              localization.actionProjectConfigurationTitle(),
              localization.actionProjectConfigurationDescription(),
              null,
              resources.projectConfiguration());
        this.appContext = appContext;
        this.projectWizard = projectWizard;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final Resource[] resources = appContext.getResources();

        checkState(resources != null && resources.length == 1);

        final Resource resource = resources[0];

        checkState(resource instanceof Container);

        if (resource.getResourceType() == PROJECT) {
            final MutableProjectConfig config = new MutableProjectConfig((Project)resource);

            projectWizard.show(config);
        }
    }

    @Override
    public void updateInPerspective(@NotNull ActionEvent event) {
        final Resource[] resources = appContext.getResources();

        if (resources != null && resources.length == 1) {
            final Resource resource = resources[0];

            if (resource.getResourceType() == PROJECT) {
                event.getPresentation().setEnabledAndVisible(true);
                event.getPresentation().setText("Update Project Configuration...");
            } else {
                event.getPresentation().setEnabledAndVisible(false);
            }
        }
    }
}
