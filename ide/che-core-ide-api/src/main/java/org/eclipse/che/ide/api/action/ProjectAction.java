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
package org.eclipse.che.ide.api.action;

import com.google.inject.Inject;
import org.eclipse.che.ide.api.app.AppContext;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Action that make sense only in project scope. This action disables if current project null or
 * user has read only rights.
 *
 * @author Evgen Vidolob
 */
public abstract class ProjectAction extends BaseAction {

  protected AppContext appContext;

  protected ProjectAction() {}

  protected ProjectAction(String text) {
    super(text);
  }

  protected ProjectAction(String text, String description) {
    super(text, description);
  }

  protected ProjectAction(String text, String description, SVGResource svgIcon) {
    super(text, description, svgIcon);
  }

  @Override
  public final void update(ActionEvent e) {
    if (appContext.getRootProject() == null) {
      e.getPresentation().setEnabledAndVisible(false);
      return;
    }

    updateProjectAction(e);
  }

  protected abstract void updateProjectAction(ActionEvent e);

  @Inject
  private void injectAppContext(AppContext appContext) {
    this.appContext = appContext;
  }
}
