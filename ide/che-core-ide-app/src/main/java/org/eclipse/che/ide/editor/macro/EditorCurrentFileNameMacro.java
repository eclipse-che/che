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
package org.eclipse.che.ide.editor.macro;

import com.google.common.annotations.Beta;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;

/**
 * Provider which is responsible for retrieving the file name from the opened editor.
 *
 * <p>Macro provided: <code>${editor.current.file.name}</code>
 *
 * @see AbstractEditorMacro
 * @see EditorAgent
 * @since 4.7.0
 */
@Beta
@Singleton
public class EditorCurrentFileNameMacro extends AbstractEditorMacro {

  public static final String KEY = "${editor.current.file.name}";

  private PromiseProvider promises;
  private final CoreLocalizationConstant localizationConstants;

  @Inject
  public EditorCurrentFileNameMacro(
      EditorAgent editorAgent,
      PromiseProvider promises,
      CoreLocalizationConstant localizationConstants) {
    super(editorAgent);
    this.promises = promises;
    this.localizationConstants = localizationConstants;
  }

  /** {@inheritDoc} */
  @Override
  public String getName() {
    return KEY;
  }

  @Override
  public String getDescription() {
    return localizationConstants.macroEditorCurrentFileNameDescription();
  }

  /** {@inheritDoc} */
  @Override
  public Promise<String> expand() {
    final EditorPartPresenter editor = getActiveEditor();

    if (editor == null) {
      return promises.resolve("");
    }

    return promises.resolve(editor.getEditorInput().getFile().getName());
  }
}
