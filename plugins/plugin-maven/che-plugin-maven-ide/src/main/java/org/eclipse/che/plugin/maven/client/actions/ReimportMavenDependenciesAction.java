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
package org.eclipse.che.plugin.maven.client.actions;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.selection.Selection;
import org.eclipse.che.ide.api.selection.SelectionAgent;
import org.eclipse.che.ide.project.node.ProjectNode;
import org.eclipse.che.plugin.maven.client.MavenLocalizationConstant;
import org.eclipse.che.plugin.maven.client.service.MavenServerServiceClient;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    private final SelectionAgent selectionAgent;
    private final NotificationManager notificationManager;
    private final MavenServerServiceClient mavenServerServiceClient;

    @Inject
    public ReimportMavenDependenciesAction(MavenLocalizationConstant constant,
                                           SelectionAgent selectionAgent,
                                           NotificationManager notificationManager,
                                           Resources resources,
                                           MavenServerServiceClient mavenServerServiceClient) {
        super(Collections.singletonList(PROJECT_PERSPECTIVE_ID),
              constant.actionReimportDependenciesTitle(),
              constant.actionReimportDependenciesDescription(),
              null,
              resources.refresh());
        this.selectionAgent = selectionAgent;
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
        List<String> paths = new ArrayList<>();
        Selection<?> selection = selectionAgent.getSelection();
        for (Object aSelection: selection.getAllElements()) {
            if (!(aSelection instanceof ProjectNode)) {
                continue;
            }

            ProjectConfigDto projectConfig = ((ProjectNode)aSelection).getProjectConfig();
            if (MAVEN_ID.equals(projectConfig.getType())) {
                paths.add(projectConfig.getPath());
            }
        }
        return paths;
    }
}
