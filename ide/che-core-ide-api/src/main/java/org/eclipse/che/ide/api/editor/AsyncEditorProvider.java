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
package org.eclipse.che.ide.api.editor;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.resources.VirtualFile;

/**
 * Extension interface for {@link EditorProvider} Add ability to create editor asynchronously.
 * {@link EditorAgent} should use this interface to crate editor instance
 *
 * @author Evgen Vidolob
 */
public interface AsyncEditorProvider {

  /**
   * Create promise for creating new editor instance.
   *
   * @param file the file for which editor should crated
   * @return promise
   */
  Promise<EditorPartPresenter> createEditor(VirtualFile file);
}
