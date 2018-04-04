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
package org.eclipse.che.plugin.web.client.js.editor;

import com.google.inject.Inject;
import org.eclipse.che.ide.api.editor.EditorProvider;
import org.eclipse.che.ide.api.editor.defaulteditor.AbstractTextEditorProvider;
import org.eclipse.che.ide.api.editor.editorconfig.TextEditorConfiguration;

/**
 * {@link EditorProvider} for JavaScript files.
 *
 * @author Evgen Vidolob
 */
public class JsEditorProvider extends AbstractTextEditorProvider {
  private final JsEditorConfigurationProvider configurationProvider;

  @Inject
  public JsEditorProvider(JsEditorConfigurationProvider configurationProvider) {
    this.configurationProvider = configurationProvider;
  }

  @Override
  public String getId() {
    return "codenvyJavaScriptEditor";
  }

  @Override
  public String getDescription() {
    return "Codenvy JavaScript Editor";
  }

  @Override
  protected TextEditorConfiguration getEditorConfiguration() {
    return configurationProvider.get();
  }
}
