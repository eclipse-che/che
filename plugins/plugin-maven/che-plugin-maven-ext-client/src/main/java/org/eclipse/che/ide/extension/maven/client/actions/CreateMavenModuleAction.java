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
package org.eclipse.che.ide.extension.maven.client.actions;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.analytics.client.logger.AnalyticsEventLogger;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.Presentation;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;
import org.eclipse.che.ide.extension.maven.client.MavenLocalizationConstant;
import org.eclipse.che.ide.extension.maven.client.MavenResources;
import org.eclipse.che.ide.extension.maven.client.module.CreateMavenModulePresenter;
import org.eclipse.che.ide.extension.maven.shared.MavenAttributes;

import javax.validation.constraints.NotNull;
import java.util.Collections;

import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

/**
 * Action for creating Maven module.
 *
 * @author Evgen Vidolob
 * @author Artem Zatsarynnyi
 */
@Singleton
public class CreateMavenModuleAction extends AbstractPerspectiveAction {

    private final AnalyticsEventLogger       eventLogger;
    private final AppContext                 appContext;
    private final CreateMavenModulePresenter presenter;

    @Inject
    public CreateMavenModuleAction(MavenLocalizationConstant constant,
                                   CreateMavenModulePresenter presenter,
                                   AnalyticsEventLogger eventLogger,
                                   AppContext appContext,
                                   MavenResources mavenResources) {
        super(Collections.singletonList(PROJECT_PERSPECTIVE_ID),
              constant.actionCreateMavenModuleText(),
              constant.actionCreateMavenModuleDescription(),
              null,
              mavenResources.maven());
        this.presenter = presenter;
        this.eventLogger = eventLogger;
        this.appContext = appContext;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        eventLogger.log(this);
        if (appContext.getCurrentProject() != null) {
            presenter.showDialog(appContext.getCurrentProject());
        }
    }

    @Override
    public void updateInPerspective(@NotNull ActionEvent event) {
        final Presentation presentation = event.getPresentation();
        final CurrentProject currentProject = appContext.getCurrentProject();
        if (currentProject == null) {
            presentation.setEnabledAndVisible(false);
            return;
        }

        presentation.setVisible(MavenAttributes.MAVEN_ID.equals(currentProject.getRootProject().getType()));
        presentation.setEnabled("pom".equals(currentProject.getAttributeValue(MavenAttributes.PACKAGING)));
    }
}
