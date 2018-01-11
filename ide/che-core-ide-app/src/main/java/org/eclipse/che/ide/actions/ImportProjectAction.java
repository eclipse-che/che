/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.actions;

import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.part.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.projectimport.wizard.presenter.ImportProjectWizardPresenter;

/**
 * Import project from location action
 *
 * @author Roman Nikitenko
 * @author Dmitry Shnurenko
 */
@Singleton
public class ImportProjectAction extends AbstractPerspectiveAction {

  private final ImportProjectWizardPresenter presenter;

  @Inject
  public ImportProjectAction(
      ImportProjectWizardPresenter presenter,
      CoreLocalizationConstant locale,
      Resources resources) {
    super(
        singletonList(PROJECT_PERSPECTIVE_ID),
        locale.importProjectFromLocationName(),
        locale.importProjectFromLocationDescription(),
        resources.importProjectFromLocation());
    this.presenter = presenter;
  }

  /** {@inheritDoc} */
  @Override
  public void actionPerformed(ActionEvent event) {
    presenter.show();
  }

  /** {@inheritDoc} */
  @Override
  public void updateInPerspective(@NotNull ActionEvent event) {
    event.getPresentation().setEnabledAndVisible(true);
  }
}
