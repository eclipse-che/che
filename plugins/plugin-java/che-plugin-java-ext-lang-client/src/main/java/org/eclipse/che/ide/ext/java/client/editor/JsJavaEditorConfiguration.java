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
package org.eclipse.che.ide.ext.java.client.editor;

import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.ide.api.editor.codeassist.CodeAssistProcessor;
import org.eclipse.che.ide.api.editor.editorconfig.DefaultTextEditorConfiguration;
import org.eclipse.che.ide.api.editor.formatter.ContentFormatter;
import org.eclipse.che.ide.api.editor.partition.DocumentPositionMap;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;

/** Text editor configuration for java files. */
public class JsJavaEditorConfiguration extends DefaultTextEditorConfiguration {

  private final Map<String, CodeAssistProcessor> codeAssistProcessors;
  private final DocumentPositionMap documentPositionMap;
  private final ContentFormatter contentFormatter;

  @AssistedInject
  public JsJavaEditorConfiguration(
      @Assisted final TextEditor editor,
      final Provider<DocumentPositionMap> docPositionMapProvider,
      final ContentFormatter contentFormatter) {
    this.contentFormatter = contentFormatter;

    this.codeAssistProcessors = new HashMap<>();

    this.documentPositionMap = docPositionMapProvider.get();
  }

  @Override
  public Map<String, CodeAssistProcessor> getContentAssistantProcessors() {
    return this.codeAssistProcessors;
  }

  @Override
  public DocumentPositionMap getDocumentPositionMap() {
    return this.documentPositionMap;
  }

  @Override
  public ContentFormatter getContentFormatter() {
    return contentFormatter;
  }
}
