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
