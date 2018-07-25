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
package org.eclipse.che.ide.api.editor.texteditor;

import org.eclipse.che.ide.util.ListenerRegistrar.Remover;

/**
 * Interface for a {@link CursorModel} that also have {@link CursorHandler}s.
 *
 * @author "Mickaël Leduque"
 */
public interface CursorModelWithHandler extends CursorModel {

  Remover addCursorHandler(CursorHandler handler);

  interface CursorHandler {
    void onCursorChange(int line, int column, boolean isExplicitChange);
  }
}
