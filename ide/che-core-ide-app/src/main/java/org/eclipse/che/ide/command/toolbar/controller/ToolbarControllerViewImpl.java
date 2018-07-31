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
package org.eclipse.che.ide.command.toolbar.controller;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/** Top menu widget implementing ToolbarControllerView. */
@Singleton
public class ToolbarControllerViewImpl extends Composite implements ToolbarControllerView {

  interface ToolbarControllerViewImplUiBinder extends UiBinder<Widget, ToolbarControllerViewImpl> {}

  private ActionDelegate delegate;

  @Inject
  public ToolbarControllerViewImpl(ToolbarControllerViewImplUiBinder uiBinder) {
    initWidget(uiBinder.createAndBindUi(this));

    addDomHandler(
        new ClickHandler() {
          @Override
          public void onClick(ClickEvent clickEvent) {
            int left = getAbsoluteLeft(getWidget().getElement());
            int top = getAbsoluteTop(getWidget().getElement());
            delegate.showMenu(left + 20, top + 35);
          }
        },
        ClickEvent.getType());
  }

  /**
   * Returns absolute left position of the element.
   *
   * @param element element
   * @return element left position
   */
  private native int getAbsoluteLeft(JavaScriptObject element) /*-{
    return element.getBoundingClientRect().left;
  }-*/;

  /**
   * Returns absolute top position of the element.
   *
   * @param element element
   * @return element top position
   */
  private native int getAbsoluteTop(JavaScriptObject element) /*-{
    return element.getBoundingClientRect().top;
  }-*/;

  @Override
  public void setDelegate(ActionDelegate delegate) {
    this.delegate = delegate;
  }
}
