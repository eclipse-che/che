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
package org.eclipse.che.ide.part.editor;

import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.parts.EditorPartStack;
import org.eclipse.che.ide.api.parts.EditorTab;

/**
 * Editor tab context menu factory.
 *
 * @author Vlad Zhukovskiy
 */
public interface EditorTabContextMenuFactory {
  /**
   * Creates new context menu for editor tab.
   *
   * @param editorTab editor tab item
   * @return new context menu
   */
  EditorTabContextMenu newContextMenu(
      EditorTab editorTab, EditorPartPresenter editorPart, EditorPartStack editorPartStack);
}
