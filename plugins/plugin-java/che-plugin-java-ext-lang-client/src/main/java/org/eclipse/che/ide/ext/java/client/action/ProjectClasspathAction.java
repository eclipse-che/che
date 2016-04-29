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
package org.eclipse.che.ide.ext.java.client.action;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.project.classpath.ProjectClasspathPresenter;
import org.eclipse.che.ide.ext.java.shared.Constants;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.eclipse.che.ide.ext.java.shared.Constants.JAVA_ID;
import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

/**
 * Call classpath wizard to see the information about classpath.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class ProjectClasspathAction extends AbstractPerspectiveAction {

    private final ProjectClasspathPresenter projectClasspathPresenter;
    private final AppContext                appContext;

    @Inject
    public ProjectClasspathAction(AppContext appContext,
                                  ProjectClasspathPresenter projectClasspathPresenter,
                                  JavaLocalizationConstant localization) {
        super(Collections.singletonList(PROJECT_PERSPECTIVE_ID),
              localization.projectClasspathTitle(),
              localization.projectClasspathDescriptions(),
              null,
              null);
        this.projectClasspathPresenter = projectClasspathPresenter;
        this.appContext = appContext;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (appContext.getCurrentProject() == null) {
            return;
        }
        projectClasspathPresenter.show();
    }

    @Override
    public void updateInPerspective(@NotNull ActionEvent event) {
        CurrentProject currentProject = appContext.getCurrentProject();
        if (currentProject == null) {
            event.getPresentation().setVisible(false);
            return;
        }

        event.getPresentation().setEnabledAndVisible(isJavaProject(currentProject));
    }

    private boolean isJavaProject(CurrentProject project) {
        Map<String, List<String>> attributes = project.getProjectConfig().getAttributes();
        return attributes.containsKey(Constants.LANGUAGE)
               && attributes.get(Constants.LANGUAGE) != null
               && JAVA_ID.equals(attributes.get(Constants.LANGUAGE).get(0));
    }
}
