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
package org.eclipse.che.ide.devmode;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.BaseAction;

@Singleton
public class DevModeOffAction extends BaseAction {

  private final GWTDevMode gwtDevMode;

  @Inject
  public DevModeOffAction(CoreLocalizationConstant messages, GWTDevMode gwtDevMode) {
    super(messages.gwtDevModeOffActionTitle());

    this.gwtDevMode = gwtDevMode;
  }

  @Override
  public void actionPerformed(ActionEvent event) {
    gwtDevMode.off();
  }
}
