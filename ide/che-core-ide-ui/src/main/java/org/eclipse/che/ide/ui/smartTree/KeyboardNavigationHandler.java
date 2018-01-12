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
package org.eclipse.che.ide.ui.smartTree;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Widget;
import org.eclipse.che.ide.util.browser.UserAgent;

/** @author Vlad Zhukovskiy */
public class KeyboardNavigationHandler {
  class InternalHandler implements KeyDownHandler, KeyPressHandler {
    @Override
    public void onKeyDown(KeyDownEvent event) {
      handleEvent(event.getNativeEvent());
    }

    @Override
    public void onKeyPress(KeyPressEvent event) {
      handleEvent(event.getNativeEvent());
    }
  }

  private static int keyEvent = Event.ONKEYDOWN;
  private Widget component;
  private boolean preventDefault;

  static {
    if (UserAgent.isIE() || UserAgent.isWebkit()) {
      keyEvent = Event.ONKEYDOWN;
    } else {
      keyEvent = Event.ONKEYPRESS;
    }
  }

  public static int getKeyEvent() {
    return keyEvent;
  }

  private InternalHandler handler = new InternalHandler();
  private HandlerRegistration registration;

  public KeyboardNavigationHandler() {}

  public KeyboardNavigationHandler(Widget target) {
    bind(target);
  }

  public void bind(Widget target) {
    if (component != null) {
      registration.removeHandler();
    }

    if (target != null) {
      if (keyEvent == Event.ONKEYDOWN) {
        registration = target.addDomHandler(handler, KeyDownEvent.getType());
      } else {
        registration = target.addDomHandler(handler, KeyPressEvent.getType());
      }
    }

    component = target;
  }

  public Widget getComponent() {
    return component;
  }

  public boolean getPreventDefault() {
    return preventDefault;
  }

  public void handleEvent(NativeEvent event) {
    int code = event.getKeyCode();

    NativeEvent e = event;

    String type = event.getType();

    if ("keydown".equals(type) && keyEvent == Event.ONKEYDOWN
        || "keypress".equals(type) && keyEvent == Event.ONKEYPRESS) {

    } else {
      return;
    }

    if (component != null && component.getElement() != e.getCurrentEventTarget().cast()) {
      return;
    }

    if (preventDefault) {
      event.preventDefault();
    }

    onKeyPress(e);

    switch (code) {
      case KeyCodes.KEY_ALT:
        onAlt(e);
        break;
      case KeyCodes.KEY_BACKSPACE:
        onBackspace(e);
        break;
      case KeyCodes.KEY_CTRL:
        onControl(e);
        break;
      case KeyCodes.KEY_DELETE:
        onDelete(e);
        break;
      case KeyCodes.KEY_DOWN:
        onDown(e);
        break;
      case KeyCodes.KEY_END:
        onEnd(e);
        break;
      case KeyCodes.KEY_ENTER:
        onEnter(e);
        break;
      case KeyCodes.KEY_ESCAPE:
        onEsc(e);
        break;
      case KeyCodes.KEY_HOME:
        onHome(e);
        break;
      case KeyCodes.KEY_LEFT:
        onLeft(e);
        break;
      case KeyCodes.KEY_PAGEDOWN:
        onPageDown(e);
        break;
      case KeyCodes.KEY_PAGEUP:
        onPageUp(e);
        break;
      case KeyCodes.KEY_SHIFT:
        onShift(e);
        break;
      case KeyCodes.KEY_TAB:
        onTab(e);
        break;
      case KeyCodes.KEY_RIGHT:
        onRight(e);
        break;
      case KeyCodes.KEY_UP:
        onUp(e);
        break;
    }
  }

  public void onAlt(NativeEvent evt) {}

  public void onBackspace(NativeEvent evt) {}

  public void onControl(NativeEvent evt) {}

  public void onDelete(NativeEvent evt) {}

  public void onDown(NativeEvent evt) {}

  public void onEnd(NativeEvent evt) {}

  public void onEnter(NativeEvent evt) {}

  public void onEsc(NativeEvent evt) {}

  public void onHome(NativeEvent evt) {}

  public void onKeyPress(NativeEvent evt) {}

  public void onLeft(NativeEvent evt) {}

  public void onPageDown(NativeEvent evt) {}

  public void onPageUp(NativeEvent evt) {}

  public void onRight(NativeEvent evt) {}

  public void onShift(NativeEvent evt) {}

  public void onTab(NativeEvent evt) {}

  public void onUp(NativeEvent evt) {}

  public void setPreventDefault(boolean preventDefault) {
    this.preventDefault = preventDefault;
  }
}
