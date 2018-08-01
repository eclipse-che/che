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
package org.eclipse.che.ide.ui.smartTree.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.eclipse.che.ide.ui.smartTree.event.GoIntoStateEvent.GoIntoStateHandler;

/** @author Vlad Zhukovskiy */
public class GoIntoStateEvent extends GwtEvent<GoIntoStateHandler> {

  public interface GoIntoStateHandler extends EventHandler {
    void onGoIntoStateChanged(GoIntoStateEvent event);
  }

  public interface HasGoIntoStateHandlers extends HasHandlers {
    HandlerRegistration addGoIntoHandler(GoIntoStateHandler handler);
  }

  private static Type<GoIntoStateHandler> TYPE;

  public static Type<GoIntoStateHandler> getType() {
    if (TYPE == null) {
      TYPE = new Type<>();
    }
    return TYPE;
  }

  public enum State {
    ACTIVATED,
    DEACTIVATED
  }

  private State state;
  private Node node;

  public GoIntoStateEvent(State state, Node node) {
    this.state = state;
    this.node = node;
  }

  public State getState() {
    return state;
  }

  public Node getNode() {
    return node;
  }

  @Override
  public Type<GoIntoStateHandler> getAssociatedType() {
    return TYPE;
  }

  /** {@inheritDoc} */
  @Override
  protected void dispatch(GoIntoStateHandler handler) {
    handler.onGoIntoStateChanged(this);
  }
}
