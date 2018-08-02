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
package org.eclipse.che.ide.factory.welcome;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.UIObject;

/** @author Vitaliy Guliy */
public class TooltipHint extends UIObject {

  private static TooltipHintUiBinder uiBinder = GWT.create(TooltipHintUiBinder.class);

  interface TooltipHintUiBinder extends UiBinder<Element, TooltipHint> {}

  @UiField TableCellElement messageElement;

  @UiField DivElement closeButton;

  private int opacity = 0;

  private int top = 1;
  private int delta = 3;

  public TooltipHint(String text) {
    setElement(uiBinder.createAndBindUi(this));
    messageElement.setInnerHTML(SafeHtmlUtils.htmlEscape(text));

    DOM.sinkEvents((com.google.gwt.dom.client.Element) closeButton.cast(), Event.ONCLICK);
    DOM.setEventListener((com.google.gwt.dom.client.Element) closeButton.cast(), event -> close());

    getElement().getStyle().setProperty("opacity", "0");
    getElement().getStyle().setTop(top, Unit.PX);
    RootPanel.get().getElement().appendChild(getElement());

    getElement().getStyle().setZIndex(Integer.MAX_VALUE);

    new Timer() {
      @Override
      public void run() {
        opacity += 1;
        top += delta;
        getElement().getStyle().setTop(top, Unit.PX);

        if (opacity >= 10) {
          getElement().getStyle().setProperty("opacity", "1");
          cancel();
        } else {
          getElement().getStyle().setProperty("opacity", "0." + opacity);
        }
      }
    }.scheduleRepeating(50);
  }

  private void close() {
    opacity = 10;

    // Hide animation
    new Timer() {
      @Override
      public void run() {
        opacity--;
        if (opacity <= 0) {
          cancel();
          getElement().getParentElement().removeChild(getElement());
        } else {
          getElement().getStyle().setProperty("opacity", "0." + opacity);
        }
      }
    }.scheduleRepeating(50);
  }
}
