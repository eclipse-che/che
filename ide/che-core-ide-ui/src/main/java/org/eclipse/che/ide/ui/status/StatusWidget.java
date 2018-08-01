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
package org.eclipse.che.ide.ui.status;

import com.google.common.base.Predicate;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

/** Empty status that can render any widget */
public class StatusWidget<T extends Widget> implements EmptyStatus<T> {

  private final IsWidget widget;
  private Predicate<T> showPredicate;
  private T parent;

  public StatusWidget(IsWidget widget) {
    this.widget = widget;
  }

  @Override
  public void paint() {
    if (showPredicate.apply(parent)) {
      parent.getElement().appendChild(widget.asWidget().getElement());
    }
  }

  @Override
  public void init(T widget, Predicate<T> showPredicate) {
    parent = widget;
    this.showPredicate = showPredicate;
  }
}
