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
package org.eclipse.che.ide.api.editor.quickfix;

import javax.validation.constraints.NotNull;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;

/**
 * MachineContext information for quick fix and quick assist processors.
 *
 * <p>This interface can be implemented by clients.
 */
public final class QuickAssistInvocationContext {

  private final int offset;
  private final TextEditor textEditor;

  public QuickAssistInvocationContext(
      @Nullable final Integer offset, @NotNull final TextEditor textEditor) {
    if (textEditor == null) {
      throw new IllegalArgumentException("editor handle cannot be null");
    }
    this.offset = offset;
    this.textEditor = textEditor;
  }

  /**
   * Returns the offset where quick assist was invoked.
   *
   * @return the invocation offset or <code>-1</code> if unknown
   */
  @Nullable
  public Integer getOffset() {
    return this.offset;
  }

  /**
   * Returns the editor handle for this context.
   *
   * @return the editor handle
   */
  @NotNull
  public TextEditor getTextEditor() {
    return this.textEditor;
  }
}
