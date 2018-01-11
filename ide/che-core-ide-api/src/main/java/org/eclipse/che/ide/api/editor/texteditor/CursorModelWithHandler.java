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
