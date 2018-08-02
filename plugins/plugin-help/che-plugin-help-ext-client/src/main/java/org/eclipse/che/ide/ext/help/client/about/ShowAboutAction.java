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
package org.eclipse.che.ide.ext.help.client.about;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.BaseAction;
import org.eclipse.che.ide.ext.help.client.AboutResources;

/**
 * Action for showing About application information.
 *
 * @author Ann Shumilova
 */
@Singleton
public class ShowAboutAction extends BaseAction {

  private final AboutPresenter presenter;

  @Inject
  public ShowAboutAction(
      AboutPresenter presenter, AboutLocalizationConstant locale, AboutResources resources) {
    super(locale.aboutControlTitle(), "Show about application", resources.about());
    this.presenter = presenter;
  }

  /** {@inheritDoc} */
  @Override
  public void actionPerformed(ActionEvent e) {
    presenter.showAbout();
  }
}
