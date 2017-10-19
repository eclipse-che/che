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
package org.eclipse.che.ide.ext.git.client.action;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.BaseAction;
import org.eclipse.che.ide.ext.git.client.compare.ComparePresenter;

/** @author Mykola Morhun */
@Singleton
public class NextDiffAction extends BaseAction {

  private final ComparePresenter comparePresenter;

  @Inject
  public NextDiffAction(ComparePresenter comparePresenter) {
    this.comparePresenter = comparePresenter;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (comparePresenter.isShown()) {
      comparePresenter.onNextDiffClicked();
    }
  }
}
