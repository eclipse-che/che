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
package org.eclipse.che.ide.api.parts;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 * Is fired when a part stack state is changed.
 *
 * @author Vitaliy Guliy
 */
public class PartStackStateChangedEvent extends GwtEvent<PartStackStateChangedEvent.Handler> {

  /** Implement to handle changing the part stack state. */
  public interface Handler extends EventHandler {

    void onPartStackStateChanged(PartStackStateChangedEvent event);
  }

  public static final GwtEvent.Type<Handler> TYPE = new GwtEvent.Type<Handler>();

  private PartStack partStack;

  public PartStackStateChangedEvent(PartStack partStack) {
    this.partStack = partStack;
  }

  public PartStack getPartStack() {
    return partStack;
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onPartStackStateChanged(this);
  }
}
