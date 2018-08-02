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
package org.eclipse.che.ide.api.selection;

import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;

/**
 * This class contains a single selected object or the bulk of selected objects. Selection can
 * contain any type of Objects and any number of them. <br>
 * Single selection can be created using {@link Selection#Selection(Object)} constructor that accept
 * one Object. <br>
 * Multiselection can be created with the help of {@link Selection#Selection(List)}.
 *
 * @author Nikolay Zamosenchuk
 */
public class Selection<T> {
  /** The selection. */
  private final List<T> elements;

  /** The head of the selection. */
  private final T head;

  /** Creates an empty selection */
  public Selection() {
    this.elements = java.util.Collections.emptyList();
    this.head = null;
  }

  /**
   * Creates SingleSelection, with only one item in it.
   *
   * @param item actual Selected object
   */
  public Selection(final T item) {
    if (item == null) {
      this.elements = java.util.Collections.emptyList();
      this.head = null;
    } else {
      this.elements = java.util.Collections.singletonList(item);
      this.head = item;
    }
  }

  public Selection(final List<T> list) {
    if (list == null || list.isEmpty()) {
      this.elements = java.util.Collections.emptyList();
      this.head = null;
    } else {
      this.elements = list;
      this.head = this.elements.get(0);
    }
  }

  public Selection(final List<T> list, @NotNull final T head) {
    this.elements = list;
    this.head = head;
  }

  public T getHeadElement() {
    return this.head;
  }

  /**
   * Returns the first element of the selection.
   *
   * @return the first element of the selection
   * @deprecated use {@link #getHeadElement()}
   */
  @Deprecated
  public T getFirstElement() {
    return getHeadElement();
  }

  /**
   * Tells if the selection is empty.
   *
   * @return <code>true</code> if Selection is empty
   */
  public boolean isEmpty() {
    return this.elements.isEmpty();
  }

  /** @return <code>true</code> if Selection contains only one element. */
  public boolean isSingleSelection() {
    return this.elements.size() == 1;
  }

  public boolean isMultiSelection() {
    return this.elements.size() > 1;
  }

  /**
   * Returns all the selected elements.
   *
   * @return all the selected elements.
   * @deprecated use {@link #getAllElements()}
   */
  @Deprecated
  public List<T> getAll() {
    final List<T> copy = new ArrayList<>();
    for (final T item : this.elements) {
      copy.add(item);
    }
    return copy;
  }

  /**
   * Returns all the selected elements.
   *
   * @return all the selected elements.
   */
  public List<T> getAllElements() {
    return new ArrayList<>(this.elements);
  }

  public static class NoSelectionProvided extends Selection<Void> {}
}
