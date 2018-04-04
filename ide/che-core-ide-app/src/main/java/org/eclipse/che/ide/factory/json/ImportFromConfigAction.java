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
package org.eclipse.che.ide.factory.json;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.BaseAction;
import org.eclipse.che.ide.factory.FactoryResources;

/** @author Sergii Leschenko */
@Singleton
public class ImportFromConfigAction extends BaseAction {

  private final ImportFromConfigPresenter presenter;

  @Inject
  public ImportFromConfigAction(
      final ImportFromConfigPresenter presenter,
      CoreLocalizationConstant locale,
      FactoryResources resources) {
    super(
        locale.importFromConfigurationName(),
        locale.importFromConfigurationDescription(),
        resources.importConfig());
    this.presenter = presenter;
  }

  /** {@inheritDoc} */
  @Override
  public void actionPerformed(ActionEvent e) {
    presenter.showDialog();
  }

  /** {@inheritDoc} */
  @Override
  public void update(ActionEvent event) {}
}
