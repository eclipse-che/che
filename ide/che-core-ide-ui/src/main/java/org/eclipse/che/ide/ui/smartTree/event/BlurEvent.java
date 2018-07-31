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
package org.eclipse.che.ide.ui.smartTree.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import org.eclipse.che.ide.ui.smartTree.event.BlurEvent.BlurHandler;

/**
 * Fires after a widget is blurred. Unlike the GWT {@link
 * com.google.gwt.event.dom.client.BlurEvent}, this event is NOT a dom event to allow components
 * flexibility in when the focus event is fired.
 *
 * @author Vlad Zhukovskiy
 */
public class BlurEvent extends GwtEvent<BlurHandler> {

  public interface BlurHandler extends EventHandler {
    void onBlur(BlurEvent event);
  }

  public interface HasBlurHandlers {
    HandlerRegistration addBlurHandler(BlurHandler handler);
  }

  private static Type<BlurHandler> TYPE;

  public static Type<BlurHandler> getType() {
    if (TYPE == null) {
      TYPE = new Type<>();
    }
    return TYPE;
  }

  @Override
  public Type<BlurHandler> getAssociatedType() {
    return TYPE;
  }

  /** {@inheritDoc} */
  @Override
  protected void dispatch(BlurHandler handler) {
    handler.onBlur(this);
  }
}
