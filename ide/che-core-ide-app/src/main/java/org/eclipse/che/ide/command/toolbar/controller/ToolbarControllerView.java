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
package org.eclipse.che.ide.command.toolbar.controller;

import org.eclipse.che.ide.api.mvp.View;

/** Represents button in the top menu for managing the Toolbar. */
public interface ToolbarControllerView extends View<ToolbarControllerView.ActionDelegate> {

  interface ActionDelegate {

    /** Shows menu by clicking on selector button. */
    void showMenu(int mouseX, int mouseY);
  }
}
