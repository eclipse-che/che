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
package org.eclipse.che.ide.ext.java.client;

import com.google.inject.name.Named;
import javax.inject.Inject;
import org.eclipse.che.ide.api.editor.EditorRegistry;
import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.ide.api.filetypes.FileType;
import org.eclipse.che.ide.ext.java.client.editor.JsJavaEditorProvider;

@Extension(title = "Java Editor", version = "3.1.0")
public class JavaEditorExtension {

  @Inject
  public JavaEditorExtension(
      final EditorRegistry editorRegistry,
      final @Named("JavaFileType") FileType javaFile,
      final @Named("JavaClassFileType") FileType classFile,
      final JsJavaEditorProvider javaEditorProvider,
      final JavaResources javaResources,
      Resources resources) {
    // register editor provider
    editorRegistry.registerDefaultEditor(javaFile, javaEditorProvider);
    editorRegistry.registerDefaultEditor(classFile, javaEditorProvider);

    javaResources.css().ensureInjected();
    resources.css().ensureInjected();
  }
}
