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
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;

/** Factory of {@link QuickAssistAssistant}. */
public interface QuickAssistantFactory {

  /**
   * Create a QuickAssist assistant instance.
   *
   * @param textEditor the related editor
   * @return a new quick assist assistant
   */
  @NotNull
  QuickAssistAssistant createQuickAssistant(TextEditor textEditor);
}
