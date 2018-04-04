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

/**
 * Handle {@link EditorDirtyStateChangedEvent}
 *
 * @author <a href="mailto:evidolob@exoplatform.com">Evgen Vidolob</a>
 * @version $Id:
 */
public interface EditorDirtyStateChangedHandler extends EventHandler {

  /**
   * Editor became dirty, containing unsaved changes, or got saved
   *
   * @param event
   */
  void onEditorDirtyStateChanged(EditorDirtyStateChangedEvent event);
}
