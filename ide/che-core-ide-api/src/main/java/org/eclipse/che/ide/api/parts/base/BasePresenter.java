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
package org.eclipse.che.ide.api.parts.base;

import org.eclipse.che.ide.api.parts.AbstractPartPresenter;
import org.eclipse.che.ide.api.parts.PartStack;

/**
 * Base presenter for parts that support minimizing by part toolbar button.
 *
 * @author Evgen Vidolob
 */
public abstract class BasePresenter extends AbstractPartPresenter implements BaseActionDelegate {

  @Override
  public void onToggleMaximize() {
    if (partStack != null) {
      if (partStack.getPartStackState() == PartStack.State.MAXIMIZED) {
        partStack.restore();
      } else {
        partStack.maximize();
      }
    }
  }

  /** {@inheritDoc} */
  @Override
  public void onActivate() {
    partStack.setActivePart(this);
  }
}
