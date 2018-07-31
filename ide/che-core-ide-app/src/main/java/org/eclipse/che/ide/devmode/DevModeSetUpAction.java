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
package org.eclipse.che.ide.devmode;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.BaseAction;
import org.eclipse.che.ide.core.BrowserTabCloseHandler;

@Singleton
public class DevModeSetUpAction extends BaseAction {

  private final GWTDevMode gwtDevMode;
  private final BrowserTabCloseHandler browserTabCloseHandler;

  @Inject
  public DevModeSetUpAction(
      CoreLocalizationConstant messages,
      GWTDevMode gwtDevMode,
      BrowserTabCloseHandler browserTabCloseHandler) {
    super(messages.gwtDevModeSetUpActionTitle());

    this.gwtDevMode = gwtDevMode;
    this.browserTabCloseHandler = browserTabCloseHandler;
  }

  @Override
  public void actionPerformed(ActionEvent event) {
    browserTabCloseHandler.setCloseImmediately(true);
    gwtDevMode.setUp();
  }
}
