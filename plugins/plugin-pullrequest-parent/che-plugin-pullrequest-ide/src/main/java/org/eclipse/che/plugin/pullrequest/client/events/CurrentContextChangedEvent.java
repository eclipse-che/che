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
package org.eclipse.che.plugin.pullrequest.client.events;

import com.google.gwt.event.shared.GwtEvent;
import org.eclipse.che.plugin.pullrequest.client.workflow.Context;

/**
 * Sent when current plugin context is changed to an existing one.
 *
 * <p>Note that if context is just created then this event won't be fired.
 *
 * @author Yevhenii Voevodin
 */
public class CurrentContextChangedEvent extends GwtEvent<CurrentContextChangedHandler> {

  public static final Type<CurrentContextChangedHandler> TYPE = new Type<>();

  private final Context context;

  public CurrentContextChangedEvent(final Context context) {
    this.context = context;
  }

  @Override
  public Type<CurrentContextChangedHandler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(CurrentContextChangedHandler handler) {
    handler.onContextChanged(context);
  }
}
