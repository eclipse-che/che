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

import com.google.gwt.event.shared.EventHandler;

/** Handles file content change events. */
public interface FileContentUpdateHandler extends EventHandler {
  /**
   * The file content has changed/
   *
   * @param event the event
   */
  void onFileContentUpdate(FileContentUpdateEvent event);
}
