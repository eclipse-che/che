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
import com.google.inject.Singleton;

import org.eclipse.che.api.analytics.client.logger.AnalyticsEventLogger;
import org.eclipse.che.api.project.shared.Constants;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.projecttype.wizard.presenter.ProjectWizardPresenter;

import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.Map;

import static org.eclipse.che.api.project.shared.Constants.BLANK_ID;
import static org.eclipse.che.ide.api.project.type.wizard.ProjectWizardMode.CREATE;
import static org.eclipse.che.ide.api.project.type.wizard.ProjectWizardMode.UPDATE;
import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

/**
 * @author Evgen Vidolob
 * @author Dmitry Shnurenko
 */
@Singleton
public class CreateProjectAction extends AbstractPerspectiveAction {

    private final ProjectWizardPresenter wizard;
    private final AnalyticsEventLogger   eventLogger;
    private final DtoFactory dtoFactory;

    @Inject
    public CreateProjectAction(Resources resources,
                               ProjectWizardPresenter wizard,
                               AnalyticsEventLogger eventLogger,
                               DtoFactory dtoFactory) {
        super(Arrays.asList(PROJECT_PERSPECTIVE_ID), "Create Project...", "Create new project", null, resources.newProject());
        this.wizard = wizard;
        this.eventLogger = eventLogger;
        this.dtoFactory = dtoFactory;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        eventLogger.log(this);
        final Map<String, String> parameters = e.getParameters();
        if (parameters != null && !parameters.isEmpty()) {
            final ProjectConfigDto dataObject = dtoFactory.createDto(ProjectConfigDto.class);
            if (parameters.containsKey("projectName")) {
                dataObject.setName(parameters.get("projectName"));
            }

            if (parameters.containsKey("projectType")) {
                dataObject.setType(parameters.get("projectType"));
            } else {
                dataObject.setType(BLANK_ID);
            }
            wizard.show(dataObject, CREATE);
        } else {
           wizard.show();
        }
    }

    @Override
    public void updateInPerspective(@NotNull ActionEvent event) {

    }
}
