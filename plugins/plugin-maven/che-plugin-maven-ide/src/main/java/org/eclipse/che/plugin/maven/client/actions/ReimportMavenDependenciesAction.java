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
package org.eclipse.che.plugin.maven.client.actions;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.plugin.maven.client.MavenLocalizationConstant;
import org.eclipse.che.plugin.maven.client.service.MavenServerServiceClient;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.MAVEN_ID;

/**
 * Action for reimport maven dependencies.
 *
 * @author Roman Nikitenko
 */
@Singleton
public class ReimportMavenDependenciesAction extends AbstractPerspectiveAction {

    private final AppContext appContext;
    private final NotificationManager notificationManager;
    private final MavenServerServiceClient mavenServerServiceClient;

    @Inject
    public ReimportMavenDependenciesAction(MavenLocalizationConstant constant,
                                           AppContext appContext,
                                           NotificationManager notificationManager,
                                           Resources resources,
                                           MavenServerServiceClient mavenServerServiceClient) {
        super(Collections.singletonList(PROJECT_PERSPECTIVE_ID),
              constant.actionReimportDependenciesTitle(),
              constant.actionReimportDependenciesDescription(),
              null,
              resources.refresh());
        this.appContext = appContext;
        this.notificationManager = notificationManager;
        this.mavenServerServiceClient = mavenServerServiceClient;
    }


    @Override
    public void updateInPerspective(@NotNull ActionEvent event) {
        event.getPresentation().setEnabledAndVisible(isMavenProjectSelected());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        mavenServerServiceClient.reImportProjects(getPathsToSelectedMavenProject()).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError arg) throws OperationException {
                notificationManager.notify("Problem with reimporting maven dependencies", arg.getMessage(), FAIL, EMERGE_MODE);
            }
        });
    }

    private boolean isMavenProjectSelected() {
        return !getPathsToSelectedMavenProject().isEmpty();
    }

    private List<String> getPathsToSelectedMavenProject() {

        final Resource[] resources = appContext.getResources();

        if (resources == null) {
            return Collections.emptyList();
        }

        Set<String> paths = new HashSet<>();

        for (Resource resource : resources) {
            final Optional<Project> project = resource.getRelatedProject();

            if (project.isPresent() && project.get().isTypeOf(MAVEN_ID)) {
                paths.add(project.get().getLocation().toString());
            }
        }

        return new ArrayList<>(paths);
    }
}
