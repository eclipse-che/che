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
package org.eclipse.che.ide.factory.welcome;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.Frame;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.parts.base.BaseView;

/** @author Vitaliy Guliy */
@Singleton
public class GreetingPartViewImpl extends BaseView<GreetingPartView.ActionDelegate>
    implements GreetingPartView {

  private Frame frame;

  public GreetingPartViewImpl() {
    frame = new Frame();
    frame.setWidth("100%");
    frame.setHeight("100%");
    frame.getElement().getStyle().setBorderStyle(Style.BorderStyle.NONE);
    frame.getElement().getStyle().setVisibility(Style.Visibility.HIDDEN);

    frame.getElement().setAttribute("id", "greetingFrame");
    frame.getElement().setAttribute("tabindex", "0");

    setContentWidget(frame);

    frame.addLoadHandler(
        event -> frame.getElement().getStyle().setVisibility(Style.Visibility.VISIBLE));

    handleFrameEvents(frame.getElement());
  }

  /**
   * Adds handlers to the greeting frame and window to catch mouse clicking on the frame.
   *
   * @param frame native frame object
   */
  private native void handleFrameEvents(final JavaScriptObject frame) /*-{
        var instance = this;
        frame["hovered"] = false;

        frame.addEventListener('mouseover', function (e) {
            frame["hovered"] = true;
        }, false);

        frame.addEventListener('mouseout', function (e) {
            frame["hovered"] = false;
        }, false);

        $wnd.addEventListener('blur', function (e) {
            if (frame["hovered"] == true) {
                instance.@org.eclipse.che.ide.factory.welcome.GreetingPartViewImpl::activatePart()();
            }
        }, false);
    }-*/;

  @Override
  public void showGreeting(String url) {
    frame.getElement().getStyle().setVisibility(Style.Visibility.HIDDEN);

    if (url == null || url.trim().isEmpty()) {
      frame.setUrl("about:blank");
    } else {
      frame.setUrl(url);
    }
  }

  /** Ensures the view is activated when clicking the mouse. */
  private void activatePart() {
    if (!isFocused()) {
      setFocus(true);
      if (delegate != null) {
        delegate.onActivate();
      }
    }
  }

  @Override
  protected void focusView() {
    frame.getElement().focus();
  }
}
