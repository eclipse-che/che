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

import static com.google.common.base.Preconditions.checkState;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.BaseAction;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.reference.ShowReferencePresenter;

/**
 * @author Dmitry Shnurenko
 * @author Vlad Zhukovskyi
 */
@Singleton
public class ShowReferenceAction extends BaseAction {

  private final ShowReferencePresenter showReferencePresenter;
  private final AppContext appContext;

  @Inject
  public ShowReferenceAction(
      CoreLocalizationConstant locale,
      ShowReferencePresenter showReferencePresenter,
      AppContext appContext) {
    super(locale.showReference());

    this.showReferencePresenter = showReferencePresenter;
    this.appContext = appContext;
  }

  /** {@inheritDoc} */
  @Override
  public void update(ActionEvent event) {
    final Resource[] resources = appContext.getResources();

    event.getPresentation().setVisible(true);
    event.getPresentation().setEnabled(resources != null && resources.length == 1);
  }

  /** {@inheritDoc} */
  @Override
  public void actionPerformed(ActionEvent event) {
    final Resource resource = appContext.getResource();

    checkState(resource != null, "Null resource occurred");

    showReferencePresenter.show(resource);
  }
}
