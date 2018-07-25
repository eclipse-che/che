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
package org.eclipse.che.ide.api.editor.keymap;

import com.google.gwt.event.shared.EventHandler;

/**
 * Handler for {@link KeymapChangeEvent}.
 *
 * @author "Mickaël Leduque"
 */
public interface KeymapChangeHandler extends EventHandler {

  /**
   * Action triggered by the event.
   *
   * @param event the event
   */
  void onKeymapChanged(KeymapChangeEvent event);
}
