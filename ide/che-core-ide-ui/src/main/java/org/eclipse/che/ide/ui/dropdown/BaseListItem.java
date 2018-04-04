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

/**
 * Base implementation of {@link DropdownListItem} which represents some value in a {@link
 * DropdownList}.
 *
 * @param <T> type of the value that this item represents
 * @see StringItemRenderer
 */
public class BaseListItem<T> implements DropdownListItem {

  private final T value;

  /**
   * Creates a new item that represents the given {@code value}.
   *
   * @param value value to represent in a {@link DropdownList}.
   */
  public BaseListItem(T value) {
    this.value = value;
  }

  /**
   * Returns the represented value.
   *
   * @return value this item represents
   */
  public T getValue() {
    return value;
  }
}
