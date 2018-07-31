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
package org.eclipse.che.ide.editor.macro;

import com.google.common.annotations.Beta;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.resource.Path;

/**
 * Provider which is responsible for retrieving the absolute file path from the opened editor.
 *
 * <p>Macro provided: <code>${editor.current.file.path}</code>
 *
 * @see AbstractEditorMacro
 * @see EditorAgent
 * @since 4.7.0
 */
@Beta
@Singleton
public class EditorCurrentFilePathMacro extends AbstractEditorMacro {

  public static final String KEY = "${editor.current.file.path}";

  private PromiseProvider promises;
  private AppContext appContext;
  private final CoreLocalizationConstant localizationConstants;

  @Inject
  public EditorCurrentFilePathMacro(
      EditorAgent editorAgent,
      PromiseProvider promises,
      AppContext appContext,
      CoreLocalizationConstant localizationConstants) {
    super(editorAgent);
    this.promises = promises;
    this.appContext = appContext;
    this.localizationConstants = localizationConstants;
  }

  /** {@inheritDoc} */
  @Override
  public String getName() {
    return KEY;
  }

  @Override
  public String getDescription() {
    return localizationConstants.macroEditorCurrentFilePathDescription();
  }

  /** {@inheritDoc} */
  @Override
  public Promise<String> expand() {
    final EditorPartPresenter editor = getActiveEditor();

    if (editor == null) {
      return promises.resolve("");
    }

    final Path absolutePath =
        appContext.getProjectsRoot().append(editor.getEditorInput().getFile().getLocation());

    return promises.resolve(absolutePath.toString());
  }
}
