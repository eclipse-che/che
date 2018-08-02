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

import java.util.logging.Logger;
import javax.inject.Inject;
import org.eclipse.che.ide.api.editor.defaulteditor.AbstractTextEditorProvider;
import org.eclipse.che.ide.api.editor.editorconfig.TextEditorConfiguration;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.editor.orion.client.OrionEditorPresenter;

/** EditorProvider that provides a text editor configured for java source files. */
public class JsJavaEditorProvider extends AbstractTextEditorProvider {

  private static final Logger LOG = Logger.getLogger(JsJavaEditorProvider.class.getName());

  private final FileWatcher watcher;
  private final JsJavaEditorConfigurationFactory configurationFactory;

  @Inject
  public JsJavaEditorProvider(
      FileWatcher watcher, JsJavaEditorConfigurationFactory jsJavaEditorConfigurationFactory) {
    this.watcher = watcher;
    this.configurationFactory = jsJavaEditorConfigurationFactory;
  }

  @Override
  public String getId() {
    return "JavaEditor";
  }

  @Override
  public String getDescription() {
    return "Java Editor";
  }

  @Override
  public TextEditor getEditor() {
    LOG.fine("JsJavaEditor instance creation.");

    final TextEditor textEditor = super.getEditor();

    if (textEditor instanceof OrionEditorPresenter) {
      final OrionEditorPresenter editor = (OrionEditorPresenter) textEditor;
      final TextEditorConfiguration configuration = configurationFactory.create(editor);
      editor.initialize(configuration);
    }

    watcher.editorOpened(textEditor);

    return textEditor;
  }
}
