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
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.project.node.HasProjectConfig;
import org.eclipse.che.ide.api.project.tree.VirtualFileImpl;
import org.eclipse.che.ide.api.project.tree.VirtualFileInfo;
import org.eclipse.che.plugin.maven.client.MavenLocalizationConstant;
import org.eclipse.che.plugin.maven.client.MavenResources;
import org.eclipse.che.plugin.maven.client.service.MavenServerServiceClient;

import javax.validation.constraints.NotNull;
import java.util.Collections;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.MAVEN_ID;
import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

/**
 * Action for generating effective pom.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class GetEffectivePomAction extends AbstractPerspectiveAction {
    private final static String NAME = "effective-pom.xml";
    private final static String PATH = "effective_pom";

    private final EditorAgent              editorAgent;
    private final NotificationManager      notificationManager;
    private final MavenServerServiceClient mavenServerServiceClient;
    private final AppContext               appContext;

    @Inject
    public GetEffectivePomAction(MavenLocalizationConstant constant,
                                 MavenResources mavenResources,
                                 EditorAgent editorAgent,
                                 NotificationManager notificationManager,
                                 MavenServerServiceClient mavenServerServiceClient,
                                 AppContext appContext) {
        super(Collections.singletonList(PROJECT_PERSPECTIVE_ID),
              constant.actionGetEffectivePomTitle(),
              constant.actionGetEffectivePomDescription(),
              null,
              mavenResources.maven());
        this.editorAgent = editorAgent;
        this.notificationManager = notificationManager;
        this.mavenServerServiceClient = mavenServerServiceClient;
        this.appContext = appContext;
    }


    @Override
    public void updateInPerspective(@NotNull ActionEvent event) {
        CurrentProject currentProject = appContext.getCurrentProject();
        if (currentProject == null) {
            event.getPresentation().setEnabledAndVisible(false);
            return;
        }
        event.getPresentation().setEnabledAndVisible(MAVEN_ID.equals(currentProject.getProjectConfig().getType()));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        mavenServerServiceClient.getEffectivePom(appContext.getCurrentProject().getProjectConfig().getPath()).then(new Operation<String>() {
            @Override
            public void apply(String content) throws OperationException {
                HasProjectConfig.ProjectConfig project = new HasProjectConfig.ProjectConfig(appContext.getCurrentProject()
                                                                                                      .getProjectConfig());
                VirtualFileInfo fileInfo = VirtualFileInfo.newBuilder()
                                                          .setContent(content)
                                                          .setName(NAME)
                                                          .setDisplayName(NAME)
                                                          .setProject(project)
                                                          .setPath(PATH)
                                                          .build();

                editorAgent.openEditor(new VirtualFileImpl(fileInfo));
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError arg) throws OperationException {
                notificationManager.notify("Problem with generating effective pom file", arg.getMessage(), FAIL, EMERGE_MODE);
            }
        });
    }
}
