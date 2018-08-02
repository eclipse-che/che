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
package org.eclipse.che.ide.actions;

/**
 * Contains IDs for editor's actions
 *
 * @author Roman Nikitenko
 */
public final class EditorActions {
  public static final String CLOSE = "closeEditor";
  public static final String CLOSE_ALL = "closeAllEditors";
  public static final String CLOSE_ALL_EXCEPT_PINNED = "closeAllEditorExceptPinned";
  public static final String CLOSE_OTHER = "closeOtherEditorExceptCurrent";
  public static final String REOPEN_CLOSED = "reopenClosedEditorTab";
  public static final String PIN_TAB = "pinEditorTab";
  public static final String SPLIT_HORIZONTALLY = "splitHorizontally";
  public static final String SPLIT_VERTICALLY = "splitVertically";
}
