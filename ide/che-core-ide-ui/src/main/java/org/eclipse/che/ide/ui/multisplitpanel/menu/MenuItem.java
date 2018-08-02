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
package org.eclipse.che.ide.ui.multisplitpanel.menu;

import org.eclipse.che.ide.api.mvp.View;

/**
 * Item of the {@link Menu}.
 *
 * @author Artem Zatsarynnyi
 */
public interface MenuItem<T> extends View<MenuItem.ActionDelegate> {

  /** Returns associated data. */
  T getData();

  interface ActionDelegate {

    /** Called when {@code menuItem} has been selected. */
    void onItemSelected(MenuItem menuItem);

    /** Called when {@code menuItem} is going to be closed. */
    void onItemClosing(MenuItem menuItem);
  }
}
