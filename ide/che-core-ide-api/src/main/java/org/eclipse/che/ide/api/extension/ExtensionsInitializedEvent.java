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
package org.eclipse.che.ide.api.extension;

import com.google.common.annotations.Beta;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 * Event describes state when all extensions that marked with annotation {@link Extension} have
 * initialized. This event may be useful to perform some work when IDE actually has been initialized
 * and displayed.
 *
 * @author Vlad Zhukovskyi
 * @since 5.0.0
 */
@Beta
public class ExtensionsInitializedEvent
    extends GwtEvent<ExtensionsInitializedEvent.ExtensionsInitializedHandler> {

  /**
   * A listener is notified when extensions have been already initialized.
   *
   * @since 5.0.0
   */
  public interface ExtensionsInitializedHandler extends EventHandler {

    /**
     * Notifies the listener that extensions have been already initialized.
     *
     * @param event instance of {@link ExtensionsInitializedEvent}
     * @see ExtensionsInitializedEvent
     * @since 5.0.0
     */
    void onExtensionsInitialized(ExtensionsInitializedEvent event);
  }

  private static Type<ExtensionsInitializedHandler> TYPE;

  public static Type<ExtensionsInitializedHandler> getType() {
    if (TYPE == null) {
      TYPE = new Type<>();
    }
    return TYPE;
  }

  @Override
  public Type<ExtensionsInitializedHandler> getAssociatedType() {
    return getType();
  }

  @Override
  protected void dispatch(ExtensionsInitializedHandler handler) {
    handler.onExtensionsInitialized(this);
  }
}
