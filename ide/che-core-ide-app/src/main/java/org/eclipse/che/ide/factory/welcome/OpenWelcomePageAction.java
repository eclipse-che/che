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
