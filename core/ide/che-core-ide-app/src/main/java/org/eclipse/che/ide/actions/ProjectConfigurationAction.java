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
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.analytics.client.logger.AnalyticsEventLogger;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.event.ConfigureProjectEvent;

import javax.validation.constraints.NotNull;
import java.util.Arrays;

import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

/**
 * Call Project wizard to change project type
 *
 * @author Evgen Vidolob
 * @author Dmitry Shnurenko
 */
@Singleton
public class ProjectConfigurationAction extends AbstractPerspectiveAction {

    private final AnalyticsEventLogger eventLogger;
    private final EventBus             eventBus;
    private final AppContext           appContext;

    @Inject
    public ProjectConfigurationAction(AppContext appContext,
                                      CoreLocalizationConstant localization,
                                      AnalyticsEventLogger eventLogger,
                                      Resources resources,
                                      EventBus eventBus) {
        super(Arrays.asList(PROJECT_PERSPECTIVE_ID),
              localization.actionProjectConfigurationTitle(),
              localization.actionProjectConfigurationDescription(),
              null,
              resources.projectConfiguration());
        this.eventLogger = eventLogger;
        this.eventBus = eventBus;
        this.appContext = appContext;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (appContext.getCurrentProject() == null) {
            return;
        }

        eventLogger.log(this);
        eventBus.fireEvent(new ConfigureProjectEvent(appContext.getCurrentProject().getProjectConfig()));
    }

    @Override
    public void updateInPerspective(@NotNull ActionEvent event) {
        event.getPresentation().setVisible(true);
        event.getPresentation().setEnabled(appContext.getCurrentProject() != null);
    }
}
