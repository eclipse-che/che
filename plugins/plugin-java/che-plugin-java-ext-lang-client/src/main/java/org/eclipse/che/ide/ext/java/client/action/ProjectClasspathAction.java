/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ext.java.client.action;

import static org.eclipse.che.ide.ext.java.client.util.JavaUtil.isJavaProject;
import static org.eclipse.che.ide.part.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Collections;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.project.classpath.ProjectClasspathPresenter;

/**
 * Call classpath wizard to see the information about classpath.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class ProjectClasspathAction extends AbstractPerspectiveAction {

  private final ProjectClasspathPresenter projectClasspathPresenter;
  private final AppContext appContext;

  @Inject
  public ProjectClasspathAction(
      AppContext appContext,
      ProjectClasspathPresenter projectClasspathPresenter,
      JavaLocalizationConstant localization) {
    super(
        Collections.singletonList(PROJECT_PERSPECTIVE_ID),
        localization.projectClasspathTitle(),
        localization.projectClasspathDescriptions());
    this.projectClasspathPresenter = projectClasspathPresenter;
    this.appContext = appContext;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    projectClasspathPresenter.show();
  }

  @Override
  public void updateInPerspective(@NotNull ActionEvent event) {
    final Resource resource = appContext.getResource();
    if (resource == null) {
      event.getPresentation().setEnabledAndVisible(false);
      return;
    }

    final Optional<Project> project = resource.getRelatedProject();
    if (!project.isPresent()) {
      event.getPresentation().setEnabledAndVisible(false);
      return;
    }

    event.getPresentation().setEnabledAndVisible(isJavaProject(project.get()));
  }
}
