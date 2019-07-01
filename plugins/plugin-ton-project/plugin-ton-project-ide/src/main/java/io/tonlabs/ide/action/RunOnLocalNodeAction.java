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
package io.tonlabs.ide.action;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.tonlabs.ide.TonProjectResources;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;

/** Action for running the contract on local node. */
@Singleton
public class RunOnLocalNodeAction extends TonProjectAction {
  /**
   * Constructor.
   *
   * @param appContext the IDE application context
   */
  @Inject
  public RunOnLocalNodeAction(AppContext appContext) {
    super(
        appContext,
        "Run on local node",
        "Compile the smart contract and run it on local node.",
        TonProjectResources.INSTANCE.runIcon());
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    throw new UnsupportedOperationException("Not implemented yet");
  }
}
