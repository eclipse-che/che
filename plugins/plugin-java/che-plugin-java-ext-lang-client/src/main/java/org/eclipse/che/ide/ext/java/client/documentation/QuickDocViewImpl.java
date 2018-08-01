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
package org.eclipse.che.ide.ext.java.client.documentation;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/** @author Evgen Vidolob */
@Singleton
public class QuickDocViewImpl extends PopupPanel implements QuickDocView {

  private ActionDelegate delegate;
  private Frame frame;

  @Inject
  public QuickDocViewImpl() {
    super(true, true);
    addCloseHandler(
        new CloseHandler<PopupPanel>() {
          @Override
          public void onClose(CloseEvent<PopupPanel> event) {
            if (delegate != null) {
              delegate.onCloseView();
            }
          }
        });

    setSize("400px", "200px");
    Style style = getElement().getStyle();
    style.setProperty("resize", "both");
    style.setPaddingBottom(0, Style.Unit.PX);
    style.setPaddingTop(3, Style.Unit.PX);
    style.setPaddingLeft(3, Style.Unit.PX);
    style.setPaddingRight(3, Style.Unit.PX);
    createFrame();
    add(frame);
  }

  private void createFrame() {
    frame = new Frame();
    frame.setSize("100%", "100%");
    frame.getElement().getStyle().setBorderStyle(Style.BorderStyle.NONE);
    frame
        .getElement()
        .setAttribute("sandbox", "allow-scripts allow-same-origin"); // empty value, not null
    frame.getElement().getStyle().setProperty("resize", "both");
  }

  @Override
  public void setDelegate(ActionDelegate delegate) {
    this.delegate = delegate;
  }

  @Override
  public Widget asWidget() {
    return this;
  }

  @Override
  public void show(String url, int x, int y) {
    remove(frame);
    createFrame();
    add(frame);
    frame.setUrl(url);
    setPopupPosition(x, y);
    show();
  }

  @Override
  protected void onPreviewNativeEvent(Event.NativePreviewEvent event) {
    super.onPreviewNativeEvent(event);
    switch (event.getTypeInt()) {
      case Event.ONKEYDOWN:
        if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ESCAPE) {
          hide();
        }
        break;
    }
  }
}
