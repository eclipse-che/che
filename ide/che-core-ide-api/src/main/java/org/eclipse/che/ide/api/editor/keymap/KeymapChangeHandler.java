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
