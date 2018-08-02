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
package org.eclipse.che.ide.actions;

import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.part.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.navigation.NavigateToFilePresenter;

/**
 * Action for finding file by name and opening it.
 *
 * @author Ann Shumilova
 * @author Dmitry Shnurenko
 * @author Vlad Zhukovskyi
 */
@Singleton
public class NavigateToFileAction extends AbstractPerspectiveAction {

  private final Provider<NavigateToFilePresenter> navigateToFilePresenterProvider;

  @Inject
  public NavigateToFileAction(
      Provider<NavigateToFilePresenter> navigateToFilePresenterProvider,
      Resources resources,
      CoreLocalizationConstant localizationConstant) {
    super(
        singletonList(PROJECT_PERSPECTIVE_ID),
        localizationConstant.actionNavigateToFileText(),
        localizationConstant.actionNavigateToFileDescription(),
        resources.navigateToFile());
    this.navigateToFilePresenterProvider = navigateToFilePresenterProvider;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    navigateToFilePresenterProvider.get().showDialog();
  }

  @Override
  public void updateInPerspective(@NotNull ActionEvent event) {
    event.getPresentation().setEnabledAndVisible(true);
  }
}
