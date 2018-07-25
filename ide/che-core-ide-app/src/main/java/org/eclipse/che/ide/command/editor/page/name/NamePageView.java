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
package org.eclipse.che.ide.command.editor.page.name;

import org.eclipse.che.ide.api.mvp.View;

/**
 * The view for {@link NamePage}.
 *
 * @author Artem Zatsarynnyi
 */
public interface NamePageView extends View<NamePageView.ActionDelegate> {

  /** Sets the command's name value. */
  void setCommandName(String name);

  /** Sets the focus on name field. */
  void setFocusOnName();

  /** The action delegate for this view. */
  interface ActionDelegate {

    /**
     * Called when command's name has been changed.
     *
     * @param name changed value of the command's name
     */
    void onNameChanged(String name);

    /** Called when executing command is requested. */
    void onCommandRun();
  }
}
