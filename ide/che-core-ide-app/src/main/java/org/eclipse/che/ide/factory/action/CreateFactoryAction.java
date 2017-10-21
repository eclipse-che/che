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
package org.eclipse.che.ide.factory.action;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Collections;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.factory.configure.CreateFactoryPresenter;

/** @author Anton Korneta */
@Singleton
public class CreateFactoryAction extends AbstractPerspectiveAction {

  private final CreateFactoryPresenter presenter;

  @Inject
  public CreateFactoryAction(
      CreateFactoryPresenter presenter, CoreLocalizationConstant localizationConstant) {
    super(
        Collections.singletonList("Project Perspective"),
        localizationConstant.createFactoryActionTitle());
    this.presenter = presenter;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    presenter.showDialog();
  }

  @Override
  public void updateInPerspective(@NotNull ActionEvent event) {}
}
