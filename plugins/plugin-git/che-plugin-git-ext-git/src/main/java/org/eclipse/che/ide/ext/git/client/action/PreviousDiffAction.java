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
package org.eclipse.che.ide.ext.git.client.action;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.BaseAction;
import org.eclipse.che.ide.ext.git.client.compare.ComparePresenter;

/** @author Mykola Morhun */
@Singleton
public class PreviousDiffAction extends BaseAction {

  private final ComparePresenter comparePresenter;

  @Inject
  public PreviousDiffAction(ComparePresenter comparePresenter) {
    super(null, null);
    this.comparePresenter = comparePresenter;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (comparePresenter.isShown()) {
      comparePresenter.onPreviousDiffClicked();
    }
  }
}
