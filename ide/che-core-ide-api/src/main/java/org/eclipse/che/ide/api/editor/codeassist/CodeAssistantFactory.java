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
