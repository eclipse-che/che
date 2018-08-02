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
package org.eclipse.che.ide.ext.git.client.action;

import static com.google.common.base.Preconditions.checkState;
import static org.eclipse.che.ide.api.resources.Resource.FILE;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.compare.revisionslist.RevisionListPresenter;

/**
 * Action for comparing with revision.
 *
 * @author Igor Vinokur
 * @author Vlad Zhukovskyi
 */
@Singleton
public class CompareWithRevisionAction extends GitAction {
  private final RevisionListPresenter presenter;

  @Inject
  public CompareWithRevisionAction(
      RevisionListPresenter presenter, AppContext appContext, GitLocalizationConstant locale) {
    super(locale.compareWithRevisionTitle(), locale.compareWithRevisionTitle(), appContext);
    this.presenter = presenter;
  }

  /** {@inheritDoc} */
  @Override
  public void actionPerformed(ActionEvent e) {
    final Project project = appContext.getRootProject();
    final Resource resource = appContext.getResource();

    checkState(project != null, "Null project occurred");
    checkState(resource instanceof File, "Invalid file occurred");

    presenter.showRevisions(project, (File) resource);
  }

  @Override
  public void updateInPerspective(@NotNull ActionEvent event) {
    super.updateInPerspective(event);

    final Resource resource = appContext.getResource();

    event.getPresentation().setEnabled(resource != null && resource.getResourceType() == FILE);
  }
}
