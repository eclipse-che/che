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
package org.eclipse.che.ide.ui.list;

import com.google.gwt.dom.client.Element;
import java.util.Collection;

/** @author Evgen Vidolob */
public class Category<T> {

  private String title;
  private CategoryRenderer<T> renderer;
  private Collection<T> data;
  private CategoryEventDelegate<T> eventDelegate;

  public Category(
      String title,
      CategoryRenderer<T> renderer,
      Collection<T> data,
      CategoryEventDelegate<T> eventDelegate) {
    this.title = title;
    this.renderer = renderer;
    this.data = data;
    this.eventDelegate = eventDelegate;
  }

  public String getTitle() {
    return title;
  }

  public CategoryRenderer<T> getRenderer() {
    return renderer;
  }

  public Collection<T> getData() {
    return data;
  }

  public CategoryEventDelegate<T> getEventDelegate() {
    return eventDelegate;
  }

  /** Receives events fired on items in the category. */
  public interface CategoryEventDelegate<M> {
    void onListItemClicked(Element listItemBase, M itemData);
  }
}
