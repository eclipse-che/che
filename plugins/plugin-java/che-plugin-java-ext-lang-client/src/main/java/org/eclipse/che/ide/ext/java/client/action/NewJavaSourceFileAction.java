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

import static com.google.common.base.Preconditions.checkState;
import static org.eclipse.che.ide.ext.java.client.resource.SourceFolderMarker.ID;
import static org.eclipse.che.ide.ext.java.client.util.JavaUtil.isJavaProject;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.ProjectAction;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.JavaResources;
import org.eclipse.che.ide.ext.java.client.newsourcefile.NewJavaSourceFilePresenter;

/**
 * Action to create new Java source file.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class NewJavaSourceFileAction extends ProjectAction {

  private final AppContext appContext;
  private NewJavaSourceFilePresenter newJavaSourceFilePresenter;

  @Inject
  public NewJavaSourceFileAction(
      NewJavaSourceFilePresenter newJavaSourceFilePresenter,
      JavaLocalizationConstant constant,
      JavaResources resources,
      AppContext appContext) {
    super(
        constant.actionNewClassTitle(), constant.actionNewClassDescription(), resources.javaFile());
    this.newJavaSourceFilePresenter = newJavaSourceFilePresenter;
    this.appContext = appContext;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    final Resource[] resources = appContext.getResources();
    final boolean inJavaProject =
        resources != null
            && resources.length == 1
            && isJavaProject(resources[0].getRelatedProject().get());

    checkState(inJavaProject && resources[0].getParentWithMarker(ID).isPresent());

    final Resource resource = resources[0];

    if (resource instanceof Container) {
      newJavaSourceFilePresenter.showDialog((Container) resource);
    } else {
      final Container parent = resource.getParent();
      if (parent != null) {
        newJavaSourceFilePresenter.showDialog(parent);
      } else {
        throw new IllegalStateException("Failed to get parent container");
      }
    }
  }

  @Override
  public void updateProjectAction(ActionEvent e) {
    final Resource resource = appContext.getResource();
    if (resource == null) {
      e.getPresentation().setEnabledAndVisible(false);
      return;
    }

    final Optional<Project> project = resource.getRelatedProject();
    if (!project.isPresent()) {
      e.getPresentation().setEnabledAndVisible(false);
      return;
    }

    e.getPresentation()
        .setEnabledAndVisible(
            isJavaProject(project.get()) && resource.getParentWithMarker(ID).isPresent());
  }
}
