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
package org.eclipse.che.ide.api.parts;

import java.util.List;
import javax.validation.constraints.NotNull;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.resource.Path;

/**
 * Part Stack is tabbed layout element, containing Parts. EditorPartStack is shared across the
 * Perspectives and allows to display EditorParts
 *
 * @author Nikolay Zamosenchuk
 * @author Roman Nikitenko
 */
public interface EditorPartStack extends PartStack {

  /**
   * Get opened editor by related file path
   *
   * @param path path of the file opened in editor
   * @return opened editor or null if it does not exist
   */
  @Nullable
  PartPresenter getPartByPath(Path path);

  /**
   * Get {@link EditorTab} for given {@code editorPart}
   *
   * @param editorPart editor part to find corresponding editor tab
   * @return tab for given {@code editorPart} or null if this one is not found in {@link
   *     EditorPartStack}
   */
  @Nullable
  EditorTab getTabByPart(EditorPartPresenter editorPart);

  /**
   * Get {@link EditorTab} for given path
   *
   * @param path path to file to find corresponding editor tab
   * @return tab for given {@code editorPart} or null if this one is not found in {@link
   *     EditorPartStack}
   */
  @Nullable
  EditorTab getTabByPath(Path path);

  /**
   * Get editor part which associated with given {@code tabId}
   *
   * @param tabId ID of tab to find corresponding editor part
   * @return editor part or null if this one is not found in {@link EditorPartStack}
   */
  @Nullable
  EditorPartPresenter getPartByTabId(@NotNull String tabId);

  /**
   * Get next opened editor based on given {@code editorPart}
   *
   * @param editorPart the starting point to evaluate next opened editor
   * @return opened editor or null if it does not exist
   */
  @Nullable
  EditorPartPresenter getNextFor(EditorPartPresenter editorPart);

  /**
   * Get previous opened editor based on given {@code editorPart}
   *
   * @param editorPart the starting point to evaluate previous opened editor
   * @return opened editor or null if it does not exist
   */
  @Nullable
  EditorPartPresenter getPreviousFor(EditorPartPresenter editorPart);

  /**
   * Get last closed editor for {@link EditorPartStack}
   *
   * @return opened editor or null if it does not exist
   */
  @Nullable
  EditorPartPresenter getLastClosed();

  /**
   * Get all parts, opened in this stack.
   *
   * @return the parts list.
   */
  @Override
  List<EditorPartPresenter> getParts();
}
