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

import static com.google.common.base.Preconditions.checkState;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.GitResources;
import org.eclipse.che.ide.ext.git.client.GitUtil;
import org.eclipse.che.ide.ext.git.client.init.InitRepositoryPresenter;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;

/**
 * @author Andrey Plotnikov
 * @author Vlad Zhukovskyi
 */
@Singleton
public class InitRepositoryAction extends GitAction {
  private final InitRepositoryPresenter presenter;
  private final DialogFactory dialogFactory;
  private GitLocalizationConstant constant;

  @Inject
  public InitRepositoryAction(
      InitRepositoryPresenter presenter,
      GitResources resources,
      GitLocalizationConstant constant,
      AppContext appContext,
      DialogFactory dialogFactory) {
    super(
        constant.initControlTitle(),
        constant.initControlPrompt(),
        resources.initRepo(),
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
            constant.createTitle(),
            constant.messagesInitRepoQuestion(project.getName()),
            () -> presenter.initRepository(project),
            null)
        .show();
  }

  @Override
  public void updateInPerspective(@NotNull ActionEvent event) {
    super.updateInPerspective(event);

    final Project project = appContext.getRootProject();

    event.getPresentation().setEnabled(project != null && !GitUtil.isUnderGit(project));
  }
}
