/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ui.dropdown;

import com.google.gwt.user.client.ui.Widget;

/**
 * Interface for specifying an arbitrary renderer for {@link DropdownList}'s items.
 *
 * <p><strong>Important:</strong> {@link #renderHeaderWidget()} and {@link #renderListWidget()}
 * <strong>mustn't</strong> return the same instance.
 *
 * @see StringItemRenderer
 */
public interface DropdownListItemRenderer {

  /**
   * Returns widget for representing the {@link DropdownListItem} in the list's header (chosen
   * item).
   */
  Widget renderHeaderWidget();

  /** Returns widget for representing the {@link DropdownListItem} in the list. */
  Widget renderListWidget();
}
