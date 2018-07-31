/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ext.git.client.action;

import static com.google.common.base.Preconditions.checkState;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.GitResources;
import org.eclipse.che.ide.ext.git.client.delete.DeleteRepositoryPresenter;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;

/** @author Andrey Plotnikov */
@Singleton
public class DeleteRepositoryAction extends GitAction {
  private final DeleteRepositoryPresenter presenter;
  private final DialogFactory dialogFactory;
  private GitLocalizationConstant constant;

  @Inject
  public DeleteRepositoryAction(
      DeleteRepositoryPresenter presenter,
      AppContext appContext,
      GitResources resources,
      GitLocalizationConstant constant,
      DialogFactory dialogFactory) {
    super(
        constant.deleteControlTitle(),
        constant.deleteControlPrompt(),
        resources.deleteRepo(),
        appContext);
    this.presenter = presenter;
    this.constant = constant;
    this.dialogFactory = dialogFactory;
  }

  /** {@inheritDoc} */
  @Override
  public void actionPerformed(ActionEvent e) {
    final Project project = appContext.getRootProject();

    checkState(project != null, "Null project occurred");

    dialogFactory
        .createConfirmDialog(
            constant.deleteGitRepositoryTitle(),
            constant.deleteGitRepositoryQuestion(project.getName()),
            () -> presenter.deleteRepository(project),
            null)
        .show();
  }
}
