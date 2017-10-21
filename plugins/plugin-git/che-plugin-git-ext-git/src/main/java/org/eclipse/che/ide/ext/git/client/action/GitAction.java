/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ext.git.client.action;

import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.part.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.ext.git.client.GitUtil;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * @author Roman Nikitenko
 * @author Dmitry Shnurenko
 * @author Vlad Zhukovskyi
 */
public abstract class GitAction extends AbstractPerspectiveAction {

  protected final AppContext appContext;

  public GitAction(String title, String description, SVGResource icon, AppContext appContext) {
    super(singletonList(PROJECT_PERSPECTIVE_ID), title, description, icon);
    this.appContext = appContext;
  }

  public GitAction(String title, String description, String icon, AppContext appContext) {
    super(singletonList(PROJECT_PERSPECTIVE_ID), title, description, icon);
    this.appContext = appContext;
  }

  public GitAction(String title, String description, AppContext appContext) {
    super(singletonList(PROJECT_PERSPECTIVE_ID), title, description);
    this.appContext = appContext;
  }

  @Override
  public void updateInPerspective(@NotNull ActionEvent event) {
    event.getPresentation().setVisible(true);

    final Project project = appContext.getRootProject();

    event.getPresentation().setEnabled(project != null && GitUtil.isUnderGit(project));
  }
}
