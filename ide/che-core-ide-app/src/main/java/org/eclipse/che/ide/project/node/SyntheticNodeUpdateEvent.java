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
package org.eclipse.che.ide.project.node;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/** @author Vlad Zhukovskiy */
public class SyntheticNodeUpdateEvent
    extends GwtEvent<SyntheticNodeUpdateEvent.SyntheticNodeUpdateHandler> {

  private SyntheticNode node;

  public interface SyntheticNodeUpdateHandler extends EventHandler {
    void onSyntheticNodeUpdate(SyntheticNodeUpdateEvent event);
  }

  private static Type<SyntheticNodeUpdateHandler> TYPE;

  public static Type<SyntheticNodeUpdateHandler> getType() {
    if (TYPE == null) {
      TYPE = new Type<>();
    }
    return TYPE;
  }

  public SyntheticNodeUpdateEvent(SyntheticNode node) {
    this.node = node;
  }

  public SyntheticNode getNode() {
    return node;
  }

  @Override
  public Type<SyntheticNodeUpdateHandler> getAssociatedType() {
    return TYPE;
  }

  /** {@inheritDoc} */
  @Override
  protected void dispatch(SyntheticNodeUpdateHandler handler) {
    handler.onSyntheticNodeUpdate(this);
  }
}
