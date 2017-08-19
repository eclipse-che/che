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
package org.eclipse.che.ide.command.toolbar.controller;

import org.eclipse.che.ide.api.mvp.View;

/** Represents button in the top menu for managing the Toolbar. */
public interface ToolbarControllerView extends View<ToolbarControllerView.ActionDelegate> {

  interface ActionDelegate {

    /** Shows menu by clicking on selector button. */
    void showMenu(int mouseX, int mouseY);
  }
}
