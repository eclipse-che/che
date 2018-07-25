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
package org.eclipse.che.ide.factory.welcome;

import javax.inject.Inject;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.BaseAction;
import org.eclipse.che.ide.util.loging.Log;

/** @author Sergii Leschenko */
public class OpenWelcomePageAction extends BaseAction {
  private final GreetingPartPresenter greetingPart;

  @Inject
  public OpenWelcomePageAction(GreetingPartPresenter greetingPart) {
    this.greetingPart = greetingPart;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (e.getParameters() == null) {
      Log.error(getClass(), "Can't show welcome page without parameters");
      return;
    }

    greetingPart.showGreeting(e.getParameters());
  }
}
