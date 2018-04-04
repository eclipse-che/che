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
