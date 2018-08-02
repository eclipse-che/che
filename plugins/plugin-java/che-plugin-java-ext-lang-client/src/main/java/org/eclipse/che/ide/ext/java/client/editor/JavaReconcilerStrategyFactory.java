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
package org.eclipse.che.ide.ext.java.client.editor;

import org.eclipse.che.ide.api.editor.annotation.AnnotationModel;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;

/** Factory of reconciler factories for java documents. */
public interface JavaReconcilerStrategyFactory {
  JavaReconcilerStrategy create(
      TextEditor editor,
      JavaCodeAssistProcessor codeAssistProcessor,
      AnnotationModel annotationModel);
}
