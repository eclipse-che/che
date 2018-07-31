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

/**
 * Extension interface to editor. Add indication if editor has errors or warnings. May use for
 * change icons in editor tab.
 *
 * @author Evgen Vidolob
 */
public interface EditorWithErrors {
  int ERROR_STATE = 0x110;

  EditorState getErrorState();

  void setErrorState(EditorState errorState);

  public enum EditorState {
    ERROR,
    WARNING,
    NONE
  }
}
