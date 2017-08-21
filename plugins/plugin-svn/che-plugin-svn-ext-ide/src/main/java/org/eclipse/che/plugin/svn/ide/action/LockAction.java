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
package org.eclipse.che.plugin.svn.ide.action;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.plugin.svn.ide.SubversionExtensionLocalizationConstants;
import org.eclipse.che.plugin.svn.ide.SubversionExtensionResources;
import org.eclipse.che.plugin.svn.ide.lockunlock.LockUnlockPresenter;

/** Extension of {@link SubversionAction} for implementing the "svn lock" command. */
@Singleton
public class LockAction extends SubversionAction {

  private final LockUnlockPresenter presenter;

  @Inject
  public LockAction(
      AppContext appContext,
      LockUnlockPresenter presenter,
      SubversionExtensionLocalizationConstants constants,
      SubversionExtensionResources resources) {
    super(
        constants.lockTitle(),
        constants.lockDescription(),
        resources.lock(),
        appContext,
        constants,
        resources);
    this.presenter = presenter;
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    presenter.showLockDialog();
  }
}
