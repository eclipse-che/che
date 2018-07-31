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
package org.eclipse.che.ide.api.editor.codeassist;

import org.eclipse.che.ide.api.editor.partition.DocumentPartitioner;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;

/** Factory for {@link CodeAssistant} objects. */
public interface CodeAssistantFactory {

  /**
   * Create a {@link CodeAssistant} for the given editor.
   *
   * @param textEditor the editor
   * @param partitioner the partitioner
   * @return a {@link CodeAssistant}
   */
  CodeAssistant create(TextEditor textEditor, DocumentPartitioner partitioner);
}
