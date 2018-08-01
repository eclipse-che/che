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
  private boolean isUserInteraction;

  public PartStackStateChangedEvent(PartStack partStack) {
    this(partStack, false);
  }

  /**
   * Creates event to notify Part Stack state is changed.
   *
   * @param isUserInteraction pass {@code true} when hiding of the Part Stack is caused by user
   *     action (user clicked 'Hide' button, for example) or {@code false} otherwise
   */
  public PartStackStateChangedEvent(PartStack partStack, boolean isUserInteraction) {
    this.partStack = partStack;
    this.isUserInteraction = isUserInteraction;
  }

  public PartStack getPartStack() {
    return partStack;
  }

  /**
   * Returns {@code true} when hiding of the Part Stack is caused by user action (user clicked
   * 'Hide' button, for example) or {@code false} otherwise
   */
  public boolean isUserInteraction() {
    return isUserInteraction;
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
