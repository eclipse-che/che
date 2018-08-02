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
package org.eclipse.che.plugin.languageserver.ide.rename;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import elemental.events.Event;
import elemental.events.KeyboardEvent;
import javax.inject.Inject;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.ide.api.keybinding.KeyBindingAgent;
import org.eclipse.che.ide.runtime.OperationCanceledException;
import org.eclipse.che.ide.util.input.CharCodeWithModifiers;
import org.eclipse.che.ide.util.input.SignalEvent;
import org.eclipse.che.ide.util.input.SignalEventUtils;

/** Input box overlay for entering new name */
class RenameInputBox extends PopupPanel {

  private final PromiseProvider promiseProvider;
  private final int keyDigest;
  private TextBox valueTextBox;

  @Inject
  public RenameInputBox(PromiseProvider promiseProvider, KeyBindingAgent keyBindingAgent) {
    super(true, true);
    this.promiseProvider = promiseProvider;
    valueTextBox = new TextBox();
    CharCodeWithModifiers keyBinding = keyBindingAgent.getKeyBinding("LS.rename");
    keyDigest = keyBinding.getKeyDigest();
    valueTextBox.addStyleName("orionCodenvy");
    setWidget(valueTextBox);
  }

  Promise<String> setPositionAndShow(int x, int y, String value, Runnable openWindow) {
    setPopupPosition(x, y);
    valueTextBox.setValue(value);
    return promiseProvider.create(
        callback -> {
          show();

          HandlerRegistration registration =
              addCloseHandler(
                  event -> {
                    if (event.isAutoClosed()) {
                      callback.onFailure(new OperationCanceledException());
                    }
                  });

          KeyDownHandler handler =
              event -> {
                if (KeyboardEvent.KeyCode.ESC == event.getNativeEvent().getKeyCode()) {
                  event.stopPropagation();
                  event.preventDefault();
                  callback.onFailure(new OperationCanceledException());
                  registration.removeHandler();
                  hide(false);
                } else if (KeyboardEvent.KeyCode.ENTER == event.getNativeEvent().getKeyCode()) {
                  event.stopPropagation();
                  event.preventDefault();
                  registration.removeHandler();
                  hide(false);
                  callback.onSuccess(valueTextBox.getValue());
                } else {
                  SignalEvent signalEvent = SignalEventUtils.create((Event) event.getNativeEvent());
                  if (keyDigest == CharCodeWithModifiers.computeKeyDigest(signalEvent)) {
                    event.preventDefault();
                    event.stopPropagation();
                    openWindow.run();
                  }
                }
              };
          valueTextBox.addDomHandler(handler, KeyDownEvent.getType());
        });
  }

  String getInputValue() {
    return valueTextBox.getValue();
  }

  @Override
  public void show() {
    super.show();
    Scheduler.get()
        .scheduleDeferred(
            () -> {
              valueTextBox.selectAll();
              valueTextBox.setFocus(true);
            });
  }
}
