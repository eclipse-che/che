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
package org.eclipse.che.ide.actions;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.part.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.upload.file.UploadFilePresenter;

/**
 * Upload file Action
 *
 * @author Roman Nikitenko
 * @author Dmitry Shnurenko
 * @author Vlad Zhukovskyi
 */
@Singleton
public class UploadFileAction extends AbstractPerspectiveAction {

  private final UploadFilePresenter presenter;
  private final AppContext appContext;

  @Inject
  public UploadFileAction(
      UploadFilePresenter presenter,
      CoreLocalizationConstant locale,
      Resources resources,
      AppContext appContext) {
    super(
        singletonList(PROJECT_PERSPECTIVE_ID),
        locale.uploadFileName(),
        locale.uploadFileDescription(),
        resources.uploadFile());
    this.presenter = presenter;
    this.appContext = appContext;
  }

  /** {@inheritDoc} */
  @Override
  public void actionPerformed(ActionEvent e) {
    final Resource[] resources = appContext.getResources();

    checkState(resources != null && resources.length == 1 && resources[0] instanceof Container);

    presenter.showDialog((Container) resources[0]);
  }

  /** {@inheritDoc} */
  @Override
  public void updateInPerspective(@NotNull ActionEvent e) {
    final Resource[] resources = appContext.getResources();

    e.getPresentation().setVisible(true);
    e.getPresentation()
        .setEnabled(
            resources != null && resources.length == 1 && resources[0] instanceof Container);
  }
}
