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
