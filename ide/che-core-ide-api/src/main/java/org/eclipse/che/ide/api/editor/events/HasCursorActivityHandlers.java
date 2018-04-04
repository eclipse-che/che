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
package org.eclipse.che.ide.api.editor.events;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;

/**
 * Interface for components that handle {@link CursorActivityEvent}.
 *
 * @author "Mickaël Leduque"
 */
public interface HasCursorActivityHandlers extends HasHandlers {
  /**
   * Add a {@link CursorActivityHandler}.
   *
   * @param handler the handler to add
   * @return a handler used to remove the handler
   */
  HandlerRegistration addCursorActivityHandler(CursorActivityHandler handler);
}
