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
package org.eclipse.che.ide.api.editor;

/**
 * Empty implementation of the {@link org.eclipse.che.ide.api.editor.EditorAgent.OpenEditorCallback}
 *
 * @author Alexander Andrienko
 */
public class OpenEditorCallbackImpl implements EditorAgent.OpenEditorCallback {

  @Override
  public void onEditorOpened(EditorPartPresenter editor) {}

  @Override
  public void onEditorActivated(EditorPartPresenter editor) {}

  @Override
  public void onInitializationFailed() {}
}
