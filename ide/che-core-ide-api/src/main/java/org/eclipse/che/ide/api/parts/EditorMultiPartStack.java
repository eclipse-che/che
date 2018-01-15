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
package org.eclipse.che.ide.api.parts;

import javax.validation.constraints.NotNull;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.constraints.Constraints;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;

/**
 * Multi Part Stack is layout element, containing {@link EditorPartStack}s and provides methods to
 * control them.
 *
 * @author Roman Nikitenko
 */
public interface EditorMultiPartStack extends PartStack {

  /**
   * Get active {@link EditorPartStack}
   *
   * @return active editor part stack or null if this one is absent
   */
  @Nullable
  EditorPartStack getActivePartStack();

  /**
   * Get {@link EditorPartStack} for given {@code part}
   *
   * @param part editor part to find corresponding editor part stack
   * @return editor part stack which contains given {@code part} or null if this one is not found in
   *     any {@link EditorPartStack}
   */
  @Nullable
  EditorPartStack getPartStackByPart(PartPresenter part);

  /**
   * Get {@link EditorPartStack} by given {@code tabId}
   *
   * @param tabId ID of editor tab to find part stack which contains corresponding editor part
   * @return editor part stack which contains part with given {@code tabId} or null if this one is
   *     not found in any {@link EditorPartStack}
   */
  @Nullable
  EditorPartStack getPartStackByTabId(@NotNull String tabId);

  /**
   * Get editor part which associated with given {@code tabId}
   *
   * @param tabId ID of tab to find corresponding editor part
   * @return editor part or null if this one is not found in any {@link EditorPartStack}
   */
  @Nullable
  EditorPartPresenter getPartByTabId(@NotNull String tabId);

  /**
   * Get {@link EditorTab} for given {@code editorPart}
   *
   * @param editorPart editor part to find corresponding editor tab
   * @return tab for given {@code editorPart} or null if this one is not found in any {@link
   *     EditorPartStack}
   */
  @Nullable
  EditorTab getTabByPart(EditorPartPresenter editorPart);

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
   * Create first(root) part stack
   *
   * @return the first part stack
   */
  EditorPartStack createRootPartStack();

  /**
   * Remove given part stack. Note: All opened parts will be closed, use {@link
   * EditorPartStack#getParts()} to avoid removing not empty part stack
   *
   * @param partStackToRemove part stack to remove
   */
  void removePartStack(EditorPartStack partStackToRemove);

  /**
   * Split part stack
   *
   * @param relativePartStack the relative part stack
   * @param constraints the constraints of split(should contains direction of splitting:vertical or
   *     horizontal)
   * @param size the size of splits part stack (use -1 if not set)
   * @return the new splits part stack
   */
  EditorPartStack split(EditorPartStack relativePartStack, Constraints constraints, double size);

  /** @return the editor multi part stack state */
  EditorMultiPartStackState getState();
}
