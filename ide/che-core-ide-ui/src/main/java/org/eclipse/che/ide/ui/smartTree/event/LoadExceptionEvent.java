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
package org.eclipse.che.ide.ui.smartTree.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.eclipse.che.ide.ui.smartTree.event.LoadExceptionEvent.LoadExceptionHandler;

/**
 * Event fires when loading children weren't successful.
 *
 * @author Vlad Zhukovskiy
 */
public class LoadExceptionEvent extends GwtEvent<LoadExceptionHandler> {

  public interface HasLoadExceptionHandlers {
    public HandlerRegistration addLoadExceptionHandler(LoadExceptionHandler handler);
  }

  public interface LoadExceptionHandler extends EventHandler {
    void onLoadException(LoadExceptionEvent event);
  }

  private static Type<LoadExceptionHandler> TYPE;

  public static Type<LoadExceptionHandler> getType() {
    if (TYPE == null) {
      TYPE = new Type<>();
    }
    return TYPE;
  }

  private Node requestedNode;
  private Throwable exception;

  public LoadExceptionEvent(Node requestedNode, Throwable exception) {
    this.requestedNode = requestedNode;
    this.exception = exception;
  }

  @Override
  public Type<LoadExceptionHandler> getAssociatedType() {
    return TYPE;
  }

  public Throwable getException() {
    return exception;
  }

  public Node getRequestedNode() {
    return requestedNode;
  }

  /** {@inheritDoc} */
  @Override
  protected void dispatch(LoadExceptionHandler handler) {
    handler.onLoadException(this);
  }
}
