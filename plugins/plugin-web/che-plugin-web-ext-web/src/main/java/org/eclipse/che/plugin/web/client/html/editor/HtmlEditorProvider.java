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
package org.eclipse.che.plugin.web.client.html.editor;

import com.google.inject.Inject;
import org.eclipse.che.ide.api.editor.EditorProvider;
import org.eclipse.che.ide.api.editor.defaulteditor.AbstractTextEditorProvider;
import org.eclipse.che.ide.api.editor.editorconfig.TextEditorConfiguration;

/** {@link EditorProvider} for HTML files. */
public class HtmlEditorProvider extends AbstractTextEditorProvider {
  private final HTMLEditorConfigurationProvider configurationProvider;

  @Inject
  public HtmlEditorProvider(HTMLEditorConfigurationProvider htmlEditorConfigurationProvider) {
    this.configurationProvider = htmlEditorConfigurationProvider;
  }

  @Override
  public String getId() {
    return "codenvyHTMLEditor";
  }

  @Override
  public String getDescription() {
    return "Codenvy HTML Editor";
  }

  @Override
  protected TextEditorConfiguration getEditorConfiguration() {
    return configurationProvider.get();
  }
}
