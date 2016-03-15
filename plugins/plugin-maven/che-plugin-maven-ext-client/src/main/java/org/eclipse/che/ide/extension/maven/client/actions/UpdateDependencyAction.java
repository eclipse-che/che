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
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.ext.java.client.JavaResources;
import org.eclipse.che.ide.ext.java.client.dependenciesupdater.DependenciesUpdater;

import javax.validation.constraints.NotNull;
import java.util.Arrays;

import static org.eclipse.che.ide.extension.maven.shared.MavenAttributes.MAVEN_ID;
import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

/**
 * @author Evgen Vidolob
 * @author Dmitry Shnurenko
 */
@Singleton
public class UpdateDependencyAction extends AbstractPerspectiveAction {

    private final AppContext           appContext;
    private final AnalyticsEventLogger eventLogger;
    private final DependenciesUpdater  dependenciesUpdater;

    @Inject
    public UpdateDependencyAction(AppContext appContext,
                                  AnalyticsEventLogger eventLogger,
                                  JavaResources resources,
                                  DependenciesUpdater dependenciesUpdater) {
        super(Arrays.asList(PROJECT_PERSPECTIVE_ID), "Update Dependencies", "Update Dependencies", null, resources.updateDependencies());
        this.appContext = appContext;
        this.eventLogger = eventLogger;
        this.dependenciesUpdater = dependenciesUpdater;
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent e) {
        eventLogger.log(this);
        dependenciesUpdater.updateDependencies(appContext.getCurrentProject().getProjectConfig());
    }

    /** {@inheritDoc} */
    @Override
    public void updateInPerspective(@NotNull ActionEvent event) {
        event.getPresentation().setEnabledAndVisible(appContext.getCurrentProject() != null &&
                                                     MAVEN_ID.equals(appContext.getCurrentProject().getProjectConfig().getType()));
    }
}
