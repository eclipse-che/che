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
package org.eclipse.che.ide.ui.multisplitpanel.menu;

import com.google.inject.ImplementedBy;
import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.ui.multisplitpanel.SubPanel;

/**
 * Contract for menu for managing widgets on the {@link SubPanel}.
 *
 * @author Artem Zatsarynnyi
 */
@ImplementedBy(MenuWidget.class)
public interface Menu extends View<Menu.ActionDelegate> {

  /** Add {@code menuItem} to the menu. */
  void addListItem(MenuItem menuItem);

  /** Remove {@code menuItem} from the menu. */
  void removeListItem(MenuItem menuItem);

  interface ActionDelegate {

    /** Called when {@code menuItem} has been selected. */
    void onMenuItemSelected(MenuItem menuItem);

    /** Called when {@code menuItem} is going to be closed. */
    void onMenuItemClosing(MenuItem menuItem);
  }
}
